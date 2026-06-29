package com.midnight.compact

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider

class CompactFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, CompactLanguage) {
    override fun getFileType(): FileType = CompactFileType
    override fun toString(): String = "Compact File"
}
