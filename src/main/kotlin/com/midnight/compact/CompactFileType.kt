package com.midnight.compact

import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

object CompactFileType : LanguageFileType(CompactLanguage) {
    override fun getName(): String = "Compact File"
    override fun getDescription(): String = "Compact smart contract language"
    override fun getDefaultExtension(): String = "compact"
    override fun getIcon(): Icon = CompactIcons.FILE
}
