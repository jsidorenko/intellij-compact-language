package com.midnight.compact

import com.intellij.lexer.FlexAdapter

/**
 * Adapts the JFlex-generated [_CompactLexer] to the IntelliJ [com.intellij.lexer.Lexer] API.
 *
 * `_CompactLexer` is generated from `src/main/jflex/com/midnight/compact/Compact.flex`
 * by the `generateLexer` Gradle task and lives in `src/main/gen`.
 */
class CompactLexerAdapter : FlexAdapter(_CompactLexer(null))
