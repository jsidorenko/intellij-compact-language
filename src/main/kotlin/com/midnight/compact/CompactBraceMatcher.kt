package com.midnight.compact

import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType

class CompactBraceMatcher : PairedBraceMatcher {

    private val pairs = arrayOf(
        BracePair(CompactTypes.LBRACE, CompactTypes.RBRACE, true),
        BracePair(CompactTypes.LPAREN, CompactTypes.RPAREN, false),
        BracePair(CompactTypes.LBRACKET, CompactTypes.RBRACKET, false),
    )

    override fun getPairs(): Array<BracePair> = pairs

    override fun isPairedBracesAllowedBeforeType(
        lbraceType: IElementType,
        contextType: IElementType?,
    ): Boolean = true

    override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int): Int = openingBraceOffset
}
