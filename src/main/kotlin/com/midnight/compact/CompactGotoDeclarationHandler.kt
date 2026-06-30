package com.midnight.compact

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.midnight.compact.psi.CompactTypes

/**
 * Provides Ctrl/Cmd-click and Ctrl/Cmd-B navigation for Compact identifiers. This works
 * independently of the reference machinery: given the IDENTIFIER leaf under the caret, it
 * asks [CompactResolver] for the declaration and returns it as the navigation target.
 */
class CompactGotoDeclarationHandler : GotoDeclarationHandler {

    override fun getGotoDeclarationTargets(
        sourceElement: PsiElement?,
        offset: Int,
        editor: Editor?,
    ): Array<PsiElement>? {
        if (sourceElement == null) return null
        if (sourceElement.node?.elementType != CompactTypes.IDENTIFIER) return null

        // The reference resolver works on the wrapping identifier element.
        val usage = sourceElement.parent ?: return null
        val target = CompactResolver.resolve(usage) ?: return null
        if (target === usage || target === sourceElement) return null
        return arrayOf(target)
    }

    override fun getActionText(context: DataContext): String? = null
}
