package com.midnight.compact;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.TokenType;
import com.midnight.compact.psi.CompactTypes;

%%

%public
%class _CompactLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

%{
  public _CompactLexer() {
    this((java.io.Reader) null);
  }
%}

WHITE_SPACE   = [\ \t\f\r\n]+

LINE_COMMENT  = "//" [^\r\n]*
BLOCK_COMMENT = "/" "*" ( [^*] | "*"+ [^*/] )* ( "*"+ "/" )?

IDENTIFIER    = [a-zA-Z_] [a-zA-Z0-9_]*

DEC_NUMBER    = [0-9]+
HEX_NUMBER    = 0 [xX] [0-9a-fA-F]+
NUMBER        = {HEX_NUMBER} | {DEC_NUMBER}

STRING        = \" ( [^\\\"\r\n] | \\[^\r\n] )* \"?

%%

<YYINITIAL> {
  {WHITE_SPACE}        { return TokenType.WHITE_SPACE; }

  {LINE_COMMENT}       { return CompactTypes.LINE_COMMENT; }
  {BLOCK_COMMENT}      { return CompactTypes.BLOCK_COMMENT; }

  "true"               { return CompactTypes.BOOL_LITERAL; }
  "false"              { return CompactTypes.BOOL_LITERAL; }

  // Structural keywords — distinct tokens so the parser can recognize declarations.
  "circuit"            { return CompactTypes.CIRCUIT; }
  "witness"            { return CompactTypes.WITNESS; }
  "ledger"             { return CompactTypes.LEDGER; }
  "struct"             { return CompactTypes.STRUCT; }
  "enum"               { return CompactTypes.ENUM; }
  "module"             { return CompactTypes.MODULE; }
  "export"             { return CompactTypes.EXPORT; }
  "pure"               { return CompactTypes.PURE; }
  "sealed"             { return CompactTypes.SEALED; }

  // Remaining keywords share a single token; they only matter for highlighting.
  "pragma"             { return CompactTypes.KEYWORD; }
  "language_version"   { return CompactTypes.KEYWORD; }
  "import"             { return CompactTypes.KEYWORD; }
  "include"            { return CompactTypes.KEYWORD; }
  "contract"           { return CompactTypes.KEYWORD; }
  "constructor"        { return CompactTypes.KEYWORD; }
  "const"              { return CompactTypes.KEYWORD; }
  "return"             { return CompactTypes.KEYWORD; }
  "if"                 { return CompactTypes.KEYWORD; }
  "else"               { return CompactTypes.KEYWORD; }
  "for"                { return CompactTypes.KEYWORD; }
  "of"                 { return CompactTypes.KEYWORD; }
  "as"                 { return CompactTypes.KEYWORD; }
  "new"                { return CompactTypes.KEYWORD; }
  "assert"             { return CompactTypes.KEYWORD; }
  "disclose"           { return CompactTypes.KEYWORD; }
  "default"            { return CompactTypes.KEYWORD; }

  "Boolean"            { return CompactTypes.TYPE; }
  "Field"              { return CompactTypes.TYPE; }
  "Uint"               { return CompactTypes.TYPE; }
  "Bytes"              { return CompactTypes.TYPE; }
  "Vector"             { return CompactTypes.TYPE; }
  "Opaque"             { return CompactTypes.TYPE; }
  "Void"               { return CompactTypes.TYPE; }
  "Maybe"              { return CompactTypes.TYPE; }
  "Either"             { return CompactTypes.TYPE; }
  "Cell"               { return CompactTypes.TYPE; }
  "Counter"            { return CompactTypes.TYPE; }
  "Map"                { return CompactTypes.TYPE; }
  "Set"                { return CompactTypes.TYPE; }
  "List"               { return CompactTypes.TYPE; }
  "MerkleTree"         { return CompactTypes.TYPE; }
  "HistoricMerkleTree" { return CompactTypes.TYPE; }

  {STRING}             { return CompactTypes.STRING; }
  {NUMBER}             { return CompactTypes.NUMBER; }
  {IDENTIFIER}         { return CompactTypes.IDENTIFIER; }

  "("                  { return CompactTypes.LPAREN; }
  ")"                  { return CompactTypes.RPAREN; }
  "{"                  { return CompactTypes.LBRACE; }
  "}"                  { return CompactTypes.RBRACE; }
  "["                  { return CompactTypes.LBRACKET; }
  "]"                  { return CompactTypes.RBRACKET; }
  ";"                  { return CompactTypes.SEMICOLON; }
  ","                  { return CompactTypes.COMMA; }
  "."                  { return CompactTypes.DOT; }

  [+\-*/%=<>!&|\^~?:]+  { return CompactTypes.OPERATOR; }
}

[^]                    { return TokenType.BAD_CHARACTER; }
