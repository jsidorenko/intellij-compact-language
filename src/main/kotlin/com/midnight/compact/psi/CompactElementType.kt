package com.midnight.compact.psi

import com.intellij.psi.tree.IElementType
import com.midnight.compact.CompactLanguage

/** Composite (rule) element type, referenced by the generated [CompactTypes] holder. */
class CompactElementType(debugName: String) : IElementType(debugName, CompactLanguage)
