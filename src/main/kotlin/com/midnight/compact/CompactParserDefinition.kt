package com.midnight.compact

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet

class CompactParserDefinition : ParserDefinition {

    override fun createLexer(project: Project?): Lexer = CompactLexerAdapter()

    override fun createParser(project: Project?): PsiParser = CompactParser()

    override fun getFileNodeType(): IFileElementType = FILE

    override fun getWhitespaceTokens(): TokenSet = WHITE_SPACES

    override fun getCommentTokens(): TokenSet = COMMENTS

    override fun getStringLiteralElements(): TokenSet = STRINGS

    override fun createElement(node: ASTNode): PsiElement = ASTWrapperPsiElement(node)

    override fun createFile(viewProvider: FileViewProvider): PsiFile = CompactFile(viewProvider)

    companion object {
        val FILE = IFileElementType(CompactLanguage)
        val WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE)
        val COMMENTS = TokenSet.create(CompactTypes.LINE_COMMENT, CompactTypes.BLOCK_COMMENT)
        val STRINGS = TokenSet.create(CompactTypes.STRING)
    }
}
