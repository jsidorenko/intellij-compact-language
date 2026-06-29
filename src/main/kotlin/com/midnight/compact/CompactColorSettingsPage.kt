package com.midnight.compact

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import javax.swing.Icon

class CompactColorSettingsPage : ColorSettingsPage {

    override fun getIcon(): Icon = CompactIcons.FILE

    override fun getHighlighter(): SyntaxHighlighter = CompactSyntaxHighlighter()

    override fun getDemoText(): String = DEMO_TEXT

    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey>? = null

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> = DESCRIPTORS

    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY

    override fun getDisplayName(): String = "Compact"

    companion object {
        private val DESCRIPTORS = arrayOf(
            AttributesDescriptor("Keyword", CompactSyntaxHighlighter.KEYWORD),
            AttributesDescriptor("Built-in type", CompactSyntaxHighlighter.TYPE),
            AttributesDescriptor("Identifier", CompactSyntaxHighlighter.IDENTIFIER),
            AttributesDescriptor("String", CompactSyntaxHighlighter.STRING),
            AttributesDescriptor("Number", CompactSyntaxHighlighter.NUMBER),
            AttributesDescriptor("Line comment", CompactSyntaxHighlighter.LINE_COMMENT),
            AttributesDescriptor("Block comment", CompactSyntaxHighlighter.BLOCK_COMMENT),
            AttributesDescriptor("Operator", CompactSyntaxHighlighter.OPERATOR),
            AttributesDescriptor("Parentheses", CompactSyntaxHighlighter.PARENTHESES),
            AttributesDescriptor("Braces", CompactSyntaxHighlighter.BRACES),
            AttributesDescriptor("Brackets", CompactSyntaxHighlighter.BRACKETS),
            AttributesDescriptor("Semicolon", CompactSyntaxHighlighter.SEMICOLON),
            AttributesDescriptor("Comma", CompactSyntaxHighlighter.COMMA),
            AttributesDescriptor("Dot", CompactSyntaxHighlighter.DOT),
            AttributesDescriptor("Bad character", CompactSyntaxHighlighter.BAD_CHARACTER),
        )

        private val DEMO_TEXT = """
            pragma language_version >= 0.13;

            import CompactStandardLibrary;

            // A simple counter contract
            export ledger round: Counter;

            /* The constructor runs once, when the contract is deployed. */
            constructor() {
              round.increment(0);
            }

            export circuit increment(value: Uint<16>): [] {
              round.increment(disclose(value));
            }

            struct Account {
              owner: Bytes<32>,
              balance: Uint<64>,
            }

            enum State { Open, Closed }

            witness localSecret(): Bytes<32>;
        """.trimIndent()
    }
}
