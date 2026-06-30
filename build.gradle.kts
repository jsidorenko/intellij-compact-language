plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.4.0"
    id("org.jetbrains.intellij.platform") version "2.17.0"
    id("org.jetbrains.intellij.platform.grammarkit") version "2.17.0"
}

group = "com.midnight.compact"
version = "0.3.1"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        // The IDE the plugin is built and run against. Bump this to match your IDE.
        intellijIdeaCommunity("2024.2")
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "242"
            // Leave the upper bound open so the plugin keeps loading in newer IDEs.
            untilBuild = provider { null }
        }
    }
}

kotlin {
    jvmToolchain(17)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

// The JFlex-generated lexer lands here and is compiled together with the Kotlin sources.
sourceSets {
    main {
        java.srcDirs("src/main/gen")
    }
}

tasks {
    // The grammarkit plugin pre-registers `generateLexer` / `generateParser`.
    // `targetRootOutputDir` is the source root; the package sub-directory is derived
    // from `packageName` (lexer) or the paths below (parser). Both write into
    // src/main/gen, so purging is disabled to avoid one task wiping the other's output.
    generateLexer {
        sourceFile.set(file("src/main/jflex/com/midnight/compact/Compact.flex"))
        targetRootOutputDir.set(file("src/main/gen"))
        packageName.set("com.midnight.compact")
        purgeOldFiles.set(false)
    }

    generateParser {
        sourceFile.set(file("src/main/grammar/Compact.bnf"))
        targetRootOutputDir.set(file("src/main/gen"))
        pathToParser.set("com/midnight/compact/parser/CompactParser.java")
        pathToPsiRoot.set("com/midnight/compact/psi")
        purgeOldFiles.set(false)
    }

    compileKotlin {
        dependsOn(generateLexer, generateParser)
    }
    compileJava {
        dependsOn(generateLexer, generateParser)
    }
}
