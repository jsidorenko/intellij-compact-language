package com.midnight.compact

import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.util.ProcessingContext
import com.midnight.compact.psi.CompactIdentifierRef

/** Attaches a resolving reference to every identifier usage element. */
class CompactReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(CompactIdentifierRef::class.java),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext,
                ): Array<PsiReference> {
                    val text = element.text
                    if (text.isEmpty()) return PsiReference.EMPTY_ARRAY
                    return arrayOf(CompactReference(element, TextRange(0, text.length)))
                }
            },
        )
    }
}
