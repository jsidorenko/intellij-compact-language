package com.midnight.compact

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase

/**
 * Resolves an identifier usage to its declaration. Scope handling (parameters, locals,
 * struct fields, top-level declarations) lives in [CompactResolver]. Declared soft so
 * unresolved identifiers (library symbols, types, etc.) are not flagged as errors.
 */
class CompactReference(element: PsiElement, rangeInElement: TextRange) :
    PsiReferenceBase<PsiElement>(element, rangeInElement, /* soft = */ true) {

    override fun resolve(): PsiElement? = CompactResolver.resolve(myElement)

    override fun getVariants(): Array<Any> = CompactResolver.variants(myElement).toTypedArray()
}
