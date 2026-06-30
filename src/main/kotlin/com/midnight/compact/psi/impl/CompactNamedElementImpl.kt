package com.midnight.compact.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.util.IncorrectOperationException
import com.midnight.compact.psi.CompactNamedElement
import com.midnight.compact.psi.CompactTypes

/**
 * Base class (mixin) for generated declaration-name PSI elements. Implements the
 * [com.intellij.psi.PsiNameIdentifierOwner] contract directly so the generated concrete
 * impl inherits it (no reliance on Grammar-Kit method delegation).
 */
abstract class CompactNamedElementImpl(node: ASTNode) :
    ASTWrapperPsiElement(node), CompactNamedElement {

    override fun getName(): String? = nameIdentifier?.text ?: node.text

    override fun getNameIdentifier(): PsiElement? =
        node.findChildByType(CompactTypes.IDENTIFIER)?.psi

    override fun setName(name: String): PsiElement {
        // Rename is out of scope for the current (go-to-definition only) feature set.
        throw IncorrectOperationException("Renaming Compact declarations is not supported yet")
    }
}
