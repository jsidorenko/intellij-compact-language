package com.midnight.compact

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.midnight.compact.psi.CompactIdentifierRef
import com.midnight.compact.psi.CompactNamedElement
import com.midnight.compact.psi.CompactTypes

/**
 * Token-stream based name resolution for Compact. It does not rely on a deep grammar:
 * declarations are recognized from the lexer token sequence and scoped using brace/paren
 * matching. Supported declaration kinds:
 *
 *  - PARAM  parameters of a circuit / constructor (scoped to that circuit's body block)
 *  - LOCAL  `const NAME = ...` (scoped to the enclosing block, use-after-declaration)
 *  - FIELD  `NAME: Type` inside a `struct { ... }` body
 *  - TOP    top-level declarations (circuit/witness/ledger/struct/enum/module names)
 *
 * Field access (`receiver.member`) is type-aware: the receiver's struct type is inferred
 * from its declaration (parameters, annotated locals, and field chains), and the member is
 * resolved within that struct. When the type cannot be determined, it falls back to any
 * struct field with a matching name. Resolution is file-local.
 */
object CompactResolver {

    enum class Kind { PARAM, LOCAL, FIELD }

    /** A non-top-level declaration discovered in the token stream. */
    data class Decl(
        val name: String,
        val declStart: Int,
        val target: PsiElement,
        val kind: Kind,
        val scopeStart: Int,
        val scopeEnd: Int,
        /** Base type name (`name: Type`) for params, fields and annotated locals. */
        val typeName: String? = null,
        /** For FIELD declarations: the name of the enclosing struct. */
        val ownerStruct: String? = null,
    )

    private data class Leaf(val type: IElementType, val text: String, val start: Int, val psi: PsiElement) {
        val end: Int get() = start + text.length
    }

    private data class StructBody(val name: String, val open: Int, val close: Int)

    private class Model(val leaves: List<Leaf>, val decls: List<Decl>)

    /**
     * Resolves [usage] (an identifier-usage element) to its declaration, or null if it is
     * itself a declaration site or cannot be resolved.
     */
    fun resolve(usage: PsiElement): PsiElement? {
        val file = usage.containingFile ?: return null
        val model = buildModel(file)
        val sig = model.leaves
        val uStart = usage.textRange.startOffset
        val name = usage.text
        if (name.isEmpty()) return null

        // Don't resolve a declaration site as if it were a usage.
        if (model.decls.any { it.declStart == uStart }) return null

        val uIndex = sig.indexOfFirst { it.start == uStart && it.type == CompactTypes.IDENTIFIER }

        // Field access: `receiver . NAME`.
        if (uIndex > 0 && sig[uIndex - 1].type == CompactTypes.DOT) {
            return resolveMember(model, uIndex, name)
        }

        // Parameters / locals in scope, preferring the innermost, nearest-preceding one.
        inScopeDecl(model, uStart, name)?.let { return it.target }

        // Fall back to a top-level declaration.
        return topLevel(file, name)
    }

    /** Names offered for completion at [usage]: in-scope params/locals plus all top-level names. */
    fun variants(usage: PsiElement): List<String> {
        val file = usage.containingFile ?: return emptyList()
        val model = buildModel(file)
        val uStart = usage.textRange.startOffset
        val locals = model.decls
            .filter { it.kind != Kind.FIELD && uStart in it.scopeStart until it.scopeEnd }
            .map { it.name }
        val tops = PsiTreeUtil
            .findChildrenOfType(file, CompactNamedElement::class.java)
            .mapNotNull { it.name }
        return (locals + tops).distinct()
    }

    // --- field access -------------------------------------------------------

    private fun resolveMember(model: Model, memberIndex: Int, memberName: String): PsiElement? {
        val structName = receiverStructName(model, memberIndex - 1)
        if (structName != null) {
            // Type is known: resolve strictly within that struct (null if absent).
            return model.decls.firstOrNull {
                it.kind == Kind.FIELD && it.ownerStruct == structName && it.name == memberName
            }?.target
        }
        // Type unknown (untyped local, call result, …): fall back to any struct field.
        return model.decls.firstOrNull { it.kind == Kind.FIELD && it.name == memberName }?.target
    }

    /** The struct type name of the expression ending immediately before [dotIndex], or null. */
    private fun receiverStructName(model: Model, dotIndex: Int): String? {
        val sig = model.leaves
        val recvIndex = dotIndex - 1
        if (recvIndex < 0 || sig[recvIndex].type != CompactTypes.IDENTIFIER) return null

        // Chained access `a.b.c`: type of the receiver is field `b`'s type within type(a).
        if (recvIndex - 1 >= 0 && sig[recvIndex - 1].type == CompactTypes.DOT) {
            val parentStruct = receiverStructName(model, recvIndex - 1) ?: return null
            return model.decls.firstOrNull {
                it.kind == Kind.FIELD && it.ownerStruct == parentStruct && it.name == sig[recvIndex].text
            }?.typeName
        }

        // Simple receiver: a parameter or local — use its declared type.
        return inScopeDecl(model, sig[recvIndex].start, sig[recvIndex].text)?.typeName
    }

    // --- scope --------------------------------------------------------------

    private fun inScopeDecl(model: Model, uStart: Int, name: String): Decl? {
        val inScope = model.decls.filter { d ->
            (d.kind == Kind.PARAM || d.kind == Kind.LOCAL) &&
                d.name == name &&
                uStart in d.scopeStart until d.scopeEnd &&
                (d.kind == Kind.PARAM || d.declStart <= uStart)
        }
        return inScope.maxWithOrNull(compareBy<Decl>({ it.scopeStart }).thenBy { it.declStart })
    }

    private fun topLevel(file: PsiFile, name: String): PsiElement? {
        var found: PsiElement? = null
        PsiTreeUtil.processElements(file) { e ->
            if (e is CompactNamedElement && e.name == name) {
                found = e; false
            } else {
                true
            }
        }
        return found
    }

    // --- model construction -------------------------------------------------

    private fun buildModel(file: PsiFile): Model {
        val sig = collectSignificantLeaves(file)
        val braceMatch = matchPairs(sig, CompactTypes.LBRACE, CompactTypes.RBRACE)
        val parenMatch = matchPairs(sig, CompactTypes.LPAREN, CompactTypes.RPAREN)
        val decls = ArrayList<Decl>()

        // Struct bodies: `struct NAME { ... }`.
        val structBodies = ArrayList<StructBody>()
        for (i in sig.indices) {
            if (sig[i].type != CompactTypes.STRUCT) continue
            val nameIdx = (i + 1 until sig.size).firstOrNull { sig[it].type == CompactTypes.IDENTIFIER }
            val open = (i + 1 until sig.size).firstOrNull { sig[it].type == CompactTypes.LBRACE }
            val close = open?.let { braceMatch[it] }
            if (nameIdx != null && open != null && close != null && nameIdx < open) {
                structBodies.add(StructBody(sig[nameIdx].text, open, close))
            }
        }

        // Fields: `NAME : Type` directly inside a struct body.
        for (sb in structBodies) {
            var i = sb.open + 1
            while (i < sb.close) {
                val leaf = sig[i]
                if (leaf.type == CompactTypes.IDENTIFIER &&
                    isColon(sig.getOrNull(i + 1)) &&
                    innermostEnclosingBrace(sig, braceMatch, i) == sb.open
                ) {
                    decls.add(
                        Decl(
                            leaf.text, leaf.start, declTarget(leaf), Kind.FIELD, 0, Int.MAX_VALUE,
                            typeName = baseTypeAfterColon(sig, i), ownerStruct = sb.name,
                        ),
                    )
                }
                i++
            }
        }

        // Parameters of circuits / constructors, scoped to the following body block.
        for (i in sig.indices) {
            val leaf = sig[i]
            val isCircuit = leaf.type == CompactTypes.CIRCUIT
            val isConstructor = leaf.type == CompactTypes.KEYWORD && leaf.text == "constructor"
            if (!isCircuit && !isConstructor) continue

            var j = i + 1
            if (isCircuit && sig.getOrNull(j)?.type == CompactTypes.IDENTIFIER) j++
            val open = j
            if (sig.getOrNull(open)?.type != CompactTypes.LPAREN) continue
            val close = parenMatch[open] ?: continue

            val bodyOpen = (close + 1 until sig.size).firstOrNull { sig[it].type == CompactTypes.LBRACE } ?: continue
            val bodyClose = braceMatch[bodyOpen] ?: continue
            val scopeStart = sig[bodyOpen].start
            val scopeEnd = sig[bodyClose].end

            var depth = 0
            var k = open + 1
            while (k < close) {
                when (sig[k].type) {
                    CompactTypes.LPAREN, CompactTypes.LBRACKET -> depth++
                    CompactTypes.RPAREN, CompactTypes.RBRACKET -> depth--
                    CompactTypes.IDENTIFIER ->
                        if (depth == 0 && isColon(sig.getOrNull(k + 1))) {
                            decls.add(
                                Decl(
                                    sig[k].text, sig[k].start, declTarget(sig[k]), Kind.PARAM,
                                    scopeStart, scopeEnd, typeName = baseTypeAfterColon(sig, k),
                                ),
                            )
                        }
                }
                k++
            }
        }

        // Locals: `const NAME` (optionally `const NAME: Type`), scoped to the enclosing block.
        for (i in sig.indices) {
            val leaf = sig[i]
            if (leaf.type != CompactTypes.IDENTIFIER) continue
            val prev = sig.getOrNull(i - 1) ?: continue
            if (!(prev.type == CompactTypes.KEYWORD && prev.text == "const")) continue
            val open = innermostEnclosingBrace(sig, braceMatch, i) ?: continue
            val close = braceMatch[open] ?: continue
            decls.add(
                Decl(
                    leaf.text, leaf.start, declTarget(leaf), Kind.LOCAL,
                    sig[open].start, sig[close].end, typeName = baseTypeAfterColon(sig, i),
                ),
            )
        }

        return Model(sig, decls)
    }

    /** Base type name in `NAME : Type ...`, or null if there is no type annotation. */
    private fun baseTypeAfterColon(sig: List<Leaf>, nameIndex: Int): String? {
        if (!isColon(sig.getOrNull(nameIndex + 1))) return null
        val t = sig.getOrNull(nameIndex + 2) ?: return null
        return if (t.type == CompactTypes.IDENTIFIER || t.type == CompactTypes.TYPE) t.text else null
    }

    /** Index of the open-brace of the innermost brace pair strictly enclosing [index], or null. */
    private fun innermostEnclosingBrace(sig: List<Leaf>, braceMatch: Map<Int, Int>, index: Int): Int? {
        var bestOpen: Int? = null
        for ((open, close) in braceMatch) {
            if (open < index && index < close) {
                if (bestOpen == null || open > bestOpen) bestOpen = open
            }
        }
        return bestOpen
    }

    private fun matchPairs(sig: List<Leaf>, openType: IElementType, closeType: IElementType): Map<Int, Int> {
        val result = HashMap<Int, Int>()
        val stack = ArrayDeque<Int>()
        for (i in sig.indices) {
            when (sig[i].type) {
                openType -> stack.addLast(i)
                closeType -> stack.removeLastOrNull()?.let { result[it] = i }
            }
        }
        return result
    }

    private fun isColon(leaf: Leaf?): Boolean =
        leaf != null && leaf.type == CompactTypes.OPERATOR && leaf.text == ":"

    /**
     * The navigation target for a declaration: the wrapping [CompactIdentifierRef] composite
     * rather than the bare IDENTIFIER leaf, since go-to-declaration navigates reliably to
     * composite PSI elements but not always to leaves.
     */
    private fun declTarget(leaf: Leaf): PsiElement {
        val parent = leaf.psi.parent
        return if (parent is CompactIdentifierRef) parent else leaf.psi
    }

    private fun collectSignificantLeaves(file: PsiFile): List<Leaf> {
        val res = ArrayList<Leaf>()
        fun walk(node: ASTNode) {
            val first = node.firstChildNode
            if (first == null) {
                val type = node.elementType
                if (type != com.intellij.psi.TokenType.WHITE_SPACE &&
                    type != CompactTypes.LINE_COMMENT &&
                    type != CompactTypes.BLOCK_COMMENT &&
                    node.textLength > 0
                ) {
                    res.add(Leaf(type, node.text, node.startOffset, node.psi))
                }
            } else {
                var c: ASTNode? = first
                while (c != null) {
                    walk(c)
                    c = c.treeNext
                }
            }
        }
        walk(file.node)
        return res
    }
}
