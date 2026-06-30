package com.midnight.compact.psi

import com.intellij.psi.tree.IElementType
import com.midnight.compact.CompactLanguage

/** Token element type, referenced by the generated [CompactTypes] holder and the lexer. */
class CompactTokenType(debugName: String) : IElementType(debugName, CompactLanguage) {
    override fun toString(): String = "CompactTokenType." + super.toString()
}
