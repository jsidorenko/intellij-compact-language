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
 *  - FIELD  `NAME: Type` inside a `struct { ... }` body (resolved for `expr.NAME` access)
 *  - TOP    top-level declarations (circuit/witness/ledger/struct/enum/module names)
 *
 * Resolution is file-local; cross-file imports are not handled.
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
    )

    private data class Leaf(val type: IElementType, val text: String, val start: Int, val psi: PsiElement) {
        val end: Int get() = start + text.length
    }

    private class Model(val leaves: List<Leaf>, val decls: List<Decl>)

    /**
     * Resolves [usage] (an identifier-usage element) to its declaration, or null if it is
     * itself a declaration site or cannot be resolved.
     */
    fun resolve(usage: PsiElement): PsiElement? {
        val file = usage.containingFile ?: return null
        val model = modelFor(file)
        val uStart = usage.textRange.startOffset
        val name = usage.text
        if (name.isEmpty()) return null

        // Don't resolve a declaration site as if it were a usage.
        if (model.decls.any { it.declStart == uStart }) return null

        val sig = model.leaves
        val uIndex = sig.indexOfFirst { it.start == uStart && it.type == CompactTypes.IDENTIFIER }

        // Field access: `something . NAME` resolves to a matching struct field.
        if (uIndex > 0 && prevSignificant(sig, uIndex)?.type == CompactTypes.DOT) {
            return model.decls.firstOrNull { it.kind == Kind.FIELD && it.name == name }?.target
        }

        // Parameters / locals in scope, preferring the innermost, nearest-preceding one.
        val inScope = model.decls.filter { d ->
            (d.kind == Kind.PARAM || d.kind == Kind.LOCAL) &&
                d.name == name &&
                uStart in d.scopeStart until d.scopeEnd &&
                (d.kind == Kind.PARAM || d.declStart <= uStart)
        }
        val best = inScope.maxWithOrNull(
            compareBy<Decl>({ it.scopeStart }).thenBy { it.declStart },
        )
        if (best != null) return best.target

        // Fall back to a top-level declaration.
        return topLevel(file, name)
    }

    /** Names offered for completion at [usage]: in-scope params/locals plus all top-level names. */
    fun variants(usage: PsiElement): List<String> {
        val file = usage.containingFile ?: return emptyList()
        val model = modelFor(file)
        val uStart = usage.textRange.startOffset
        val locals = model.decls
            .filter { it.kind != Kind.FIELD && uStart in it.scopeStart until it.scopeEnd }
            .map { it.name }
        val tops = PsiTreeUtil
            .findChildrenOfType(file, CompactNamedElement::class.java)
            .mapNotNull { it.name }
        return (locals + tops).distinct()
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

    private fun prevSignificant(sig: List<Leaf>, index: Int): Leaf? =
        if (index > 0) sig[index - 1] else null

    // Compact files are small, so the model is rebuilt on demand rather than cached.
    private fun modelFor(file: PsiFile): Model = buildModel(file)

    private fun buildModel(file: PsiFile): Model {
        val sig = collectSignificantLeaves(file)
        val braceMatch = matchPairs(sig, CompactTypes.LBRACE, CompactTypes.RBRACE)
        val parenMatch = matchPairs(sig, CompactTypes.LPAREN, CompactTypes.RPAREN)
        val decls = ArrayList<Decl>()

        // Struct body ranges: `struct NAME { ... }`.
        val structBodies = ArrayList<IntRange>()
        for (i in sig.indices) {
            if (sig[i].type == CompactTypes.STRUCT) {
                val open = (i + 1 until sig.size).firstOrNull { sig[it].type == CompactTypes.LBRACE }
                val close = open?.let { braceMatch[it] }
                if (open != null && close != null) structBodies.add(open..close)
            }
        }

        // Fields: `NAME :` directly inside a struct body.
        for (range in structBodies) {
            var i = range.first + 1
            while (i < range.last) {
                val leaf = sig[i]
                if (leaf.type == CompactTypes.IDENTIFIER &&
                    isColon(sig.getOrNull(i + 1)) &&
                    innermostEnclosingBrace(sig, braceMatch, i) == range.first
                ) {
                    decls.add(Decl(leaf.text, leaf.start, declTarget(leaf), Kind.FIELD, 0, Int.MAX_VALUE))
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

            // Skip an optional name, then expect the parameter list.
            var j = i + 1
            if (isCircuit && sig.getOrNull(j)?.type == CompactTypes.IDENTIFIER) j++
            val open = j
            if (sig.getOrNull(open)?.type != CompactTypes.LPAREN) continue
            val close = parenMatch[open] ?: continue

            // Body is the first brace block after the parameter list.
            val bodyOpen = (close + 1 until sig.size).firstOrNull { sig[it].type == CompactTypes.LBRACE } ?: continue
            val bodyClose = braceMatch[bodyOpen] ?: continue
            val scopeStart = sig[bodyOpen].start
            val scopeEnd = sig[bodyClose].end

            // Parameter names: identifiers at depth 0 within the list, followed by ':'.
            var depth = 0
            var k = open + 1
            while (k < close) {
                when (sig[k].type) {
                    CompactTypes.LPAREN, CompactTypes.LBRACKET -> depth++
                    CompactTypes.RPAREN, CompactTypes.RBRACKET -> depth--
                    CompactTypes.IDENTIFIER ->
                        if (depth == 0 && isColon(sig.getOrNull(k + 1))) {
                            decls.add(Decl(sig[k].text, sig[k].start, declTarget(sig[k]), Kind.PARAM, scopeStart, scopeEnd))
                        }
                }
                k++
            }
        }

        // Locals: `const NAME`, scoped to the enclosing brace block.
        for (i in sig.indices) {
            val leaf = sig[i]
            if (leaf.type != CompactTypes.IDENTIFIER) continue
            val prev = sig.getOrNull(i - 1) ?: continue
            if (!(prev.type == CompactTypes.KEYWORD && prev.text == "const")) continue
            val open = innermostEnclosingBrace(sig, braceMatch, i) ?: continue
            val close = braceMatch[open] ?: continue
            decls.add(Decl(leaf.text, leaf.start, declTarget(leaf), Kind.LOCAL, sig[open].start, sig[close].end))
        }

        return Model(sig, decls)
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
