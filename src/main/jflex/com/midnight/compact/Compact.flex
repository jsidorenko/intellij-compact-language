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

  // Primitive / language built-in types.
  "Boolean"            { return CompactTypes.TYPE; }
  "Field"              { return CompactTypes.TYPE; }
  "Uint"               { return CompactTypes.TYPE; }
  "Bytes"              { return CompactTypes.TYPE; }
  "Vector"             { return CompactTypes.TYPE; }
  "Opaque"             { return CompactTypes.TYPE; }
  "Void"               { return CompactTypes.TYPE; }

  // CompactStandardLibrary types (ledger ADTs + library structs).
  "Cell"                      { return CompactTypes.STDLIB_TYPE; }
  "Counter"                   { return CompactTypes.STDLIB_TYPE; }
  "Map"                       { return CompactTypes.STDLIB_TYPE; }
  "Set"                       { return CompactTypes.STDLIB_TYPE; }
  "List"                      { return CompactTypes.STDLIB_TYPE; }
  "MerkleTree"                { return CompactTypes.STDLIB_TYPE; }
  "HistoricMerkleTree"        { return CompactTypes.STDLIB_TYPE; }
  "Maybe"                     { return CompactTypes.STDLIB_TYPE; }
  "Either"                    { return CompactTypes.STDLIB_TYPE; }
  "NativePoint"               { return CompactTypes.STDLIB_TYPE; }
  "MerkleTreeDigest"          { return CompactTypes.STDLIB_TYPE; }
  "MerkleTreePathEntry"       { return CompactTypes.STDLIB_TYPE; }
  "MerkleTreePath"            { return CompactTypes.STDLIB_TYPE; }
  "ContractAddress"           { return CompactTypes.STDLIB_TYPE; }
  "UserAddress"               { return CompactTypes.STDLIB_TYPE; }
  "CoinInfo"                  { return CompactTypes.STDLIB_TYPE; }
  "QualifiedCoinInfo"         { return CompactTypes.STDLIB_TYPE; }
  "ShieldedCoinInfo"          { return CompactTypes.STDLIB_TYPE; }
  "QualifiedShieldedCoinInfo" { return CompactTypes.STDLIB_TYPE; }
  "ZswapCoinPublicKey"        { return CompactTypes.STDLIB_TYPE; }
  "ShieldedSendResult"        { return CompactTypes.STDLIB_TYPE; }

  // CompactStandardLibrary circuits / functions.
  "some"                          { return CompactTypes.BUILTIN_FUNCTION; }
  "none"                          { return CompactTypes.BUILTIN_FUNCTION; }
  "left"                          { return CompactTypes.BUILTIN_FUNCTION; }
  "right"                         { return CompactTypes.BUILTIN_FUNCTION; }
  "transientHash"                 { return CompactTypes.BUILTIN_FUNCTION; }
  "transientCommit"               { return CompactTypes.BUILTIN_FUNCTION; }
  "persistentHash"                { return CompactTypes.BUILTIN_FUNCTION; }
  "persistentCommit"              { return CompactTypes.BUILTIN_FUNCTION; }
  "degradeToTransient"            { return CompactTypes.BUILTIN_FUNCTION; }
  "upgradeFromTransient"          { return CompactTypes.BUILTIN_FUNCTION; }
  "ecAdd"                         { return CompactTypes.BUILTIN_FUNCTION; }
  "ecMul"                         { return CompactTypes.BUILTIN_FUNCTION; }
  "ecMulGenerator"                { return CompactTypes.BUILTIN_FUNCTION; }
  "hashToCurve"                   { return CompactTypes.BUILTIN_FUNCTION; }
  "merkleTreePathRoot"            { return CompactTypes.BUILTIN_FUNCTION; }
  "merkleTreePathRootNoLeafHash"  { return CompactTypes.BUILTIN_FUNCTION; }
  "nativeToken"                   { return CompactTypes.BUILTIN_FUNCTION; }
  "tokenType"                     { return CompactTypes.BUILTIN_FUNCTION; }
  "mintShieldedToken"             { return CompactTypes.BUILTIN_FUNCTION; }
  "mintUnshieldedToken"           { return CompactTypes.BUILTIN_FUNCTION; }
  "evolveNonce"                   { return CompactTypes.BUILTIN_FUNCTION; }
  "shieldedBurnAddress"           { return CompactTypes.BUILTIN_FUNCTION; }
  "receiveShielded"               { return CompactTypes.BUILTIN_FUNCTION; }
  "receiveUnshielded"             { return CompactTypes.BUILTIN_FUNCTION; }
  "sendShielded"                  { return CompactTypes.BUILTIN_FUNCTION; }
  "sendImmediateShielded"         { return CompactTypes.BUILTIN_FUNCTION; }
  "sendUnshielded"                { return CompactTypes.BUILTIN_FUNCTION; }
  "mergeCoin"                     { return CompactTypes.BUILTIN_FUNCTION; }
  "mergeCoinImmediate"            { return CompactTypes.BUILTIN_FUNCTION; }
  "ownPublicKey"                  { return CompactTypes.BUILTIN_FUNCTION; }
  "createZswapInput"              { return CompactTypes.BUILTIN_FUNCTION; }
  "createZswapOutput"             { return CompactTypes.BUILTIN_FUNCTION; }
  "unshieldedBalance"             { return CompactTypes.BUILTIN_FUNCTION; }
  "unshieldedBalanceLt"           { return CompactTypes.BUILTIN_FUNCTION; }
  "unshieldedBalanceLte"          { return CompactTypes.BUILTIN_FUNCTION; }
  "unshieldedBalanceGt"           { return CompactTypes.BUILTIN_FUNCTION; }
  "unshieldedBalanceGte"          { return CompactTypes.BUILTIN_FUNCTION; }
  "blockTimeLt"                   { return CompactTypes.BUILTIN_FUNCTION; }
  "blockTimeLte"                  { return CompactTypes.BUILTIN_FUNCTION; }
  "blockTimeGt"                   { return CompactTypes.BUILTIN_FUNCTION; }
  "blockTimeGte"                  { return CompactTypes.BUILTIN_FUNCTION; }

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
