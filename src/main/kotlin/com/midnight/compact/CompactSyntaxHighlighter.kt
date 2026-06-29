package com.midnight.compact

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType

class CompactSyntaxHighlighter : SyntaxHighlighterBase() {

    override fun getHighlightingLexer(): Lexer = CompactLexerAdapter()

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> = when (tokenType) {
        CompactTypes.KEYWORD -> KEYWORD_KEYS
        CompactTypes.BOOL_LITERAL -> KEYWORD_KEYS
        CompactTypes.TYPE -> TYPE_KEYS
        CompactTypes.IDENTIFIER -> IDENTIFIER_KEYS
        CompactTypes.STRING -> STRING_KEYS
        CompactTypes.NUMBER -> NUMBER_KEYS
        CompactTypes.LINE_COMMENT -> LINE_COMMENT_KEYS
        CompactTypes.BLOCK_COMMENT -> BLOCK_COMMENT_KEYS
        CompactTypes.LPAREN, CompactTypes.RPAREN -> PARENTHESES_KEYS
        CompactTypes.LBRACE, CompactTypes.RBRACE -> BRACES_KEYS
        CompactTypes.LBRACKET, CompactTypes.RBRACKET -> BRACKETS_KEYS
        CompactTypes.SEMICOLON -> SEMICOLON_KEYS
        CompactTypes.COMMA -> COMMA_KEYS
        CompactTypes.DOT -> DOT_KEYS
        CompactTypes.OPERATOR -> OPERATOR_KEYS
        TokenType.BAD_CHARACTER -> BAD_CHARACTER_KEYS
        else -> EMPTY_KEYS
    }

    companion object {
        val KEYWORD: TextAttributesKey =
            createTextAttributesKey("COMPACT_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
        val TYPE: TextAttributesKey =
            createTextAttributesKey("COMPACT_TYPE", DefaultLanguageHighlighterColors.CLASS_NAME)
        val IDENTIFIER: TextAttributesKey =
            createTextAttributesKey("COMPACT_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER)
        val STRING: TextAttributesKey =
            createTextAttributesKey("COMPACT_STRING", DefaultLanguageHighlighterColors.STRING)
        val NUMBER: TextAttributesKey =
            createTextAttributesKey("COMPACT_NUMBER", DefaultLanguageHighlighterColors.NUMBER)
        val LINE_COMMENT: TextAttributesKey =
            createTextAttributesKey("COMPACT_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
        val BLOCK_COMMENT: TextAttributesKey =
            createTextAttributesKey("COMPACT_BLOCK_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT)
        val PARENTHESES: TextAttributesKey =
            createTextAttributesKey("COMPACT_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES)
        val BRACES: TextAttributesKey =
            createTextAttributesKey("COMPACT_BRACES", DefaultLanguageHighlighterColors.BRACES)
        val BRACKETS: TextAttributesKey =
            createTextAttributesKey("COMPACT_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS)
        val SEMICOLON: TextAttributesKey =
            createTextAttributesKey("COMPACT_SEMICOLON", DefaultLanguageHighlighterColors.SEMICOLON)
        val COMMA: TextAttributesKey =
            createTextAttributesKey("COMPACT_COMMA", DefaultLanguageHighlighterColors.COMMA)
        val DOT: TextAttributesKey =
            createTextAttributesKey("COMPACT_DOT", DefaultLanguageHighlighterColors.DOT)
        val OPERATOR: TextAttributesKey =
            createTextAttributesKey("COMPACT_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN)
        val BAD_CHARACTER: TextAttributesKey =
            createTextAttributesKey("COMPACT_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER)

        private val KEYWORD_KEYS = arrayOf(KEYWORD)
        private val TYPE_KEYS = arrayOf(TYPE)
        private val IDENTIFIER_KEYS = arrayOf(IDENTIFIER)
        private val STRING_KEYS = arrayOf(STRING)
        private val NUMBER_KEYS = arrayOf(NUMBER)
        private val LINE_COMMENT_KEYS = arrayOf(LINE_COMMENT)
        private val BLOCK_COMMENT_KEYS = arrayOf(BLOCK_COMMENT)
        private val PARENTHESES_KEYS = arrayOf(PARENTHESES)
        private val BRACES_KEYS = arrayOf(BRACES)
        private val BRACKETS_KEYS = arrayOf(BRACKETS)
        private val SEMICOLON_KEYS = arrayOf(SEMICOLON)
        private val COMMA_KEYS = arrayOf(COMMA)
        private val DOT_KEYS = arrayOf(DOT)
        private val OPERATOR_KEYS = arrayOf(OPERATOR)
        private val BAD_CHARACTER_KEYS = arrayOf(BAD_CHARACTER)
        private val EMPTY_KEYS = emptyArray<TextAttributesKey>()
    }
}
