plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.4.0"
    id("org.jetbrains.intellij.platform") version "2.17.0"
    id("org.jetbrains.intellij.platform.grammarkit") version "2.17.0"
}

group = "com.midnight.compact"
version = "0.1.0"

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
    // The grammarkit plugin pre-registers `generateLexer`. `targetRootOutputDir` is the
    // source root; `packageName` determines the package sub-directory the lexer lands in.
    generateLexer {
        sourceFile.set(file("src/main/jflex/com/midnight/compact/Compact.flex"))
        targetRootOutputDir.set(file("src/main/gen"))
        packageName.set("com.midnight.compact")
        purgeOldFiles.set(true)
    }

    compileKotlin {
        dependsOn(generateLexer)
    }
    compileJava {
        dependsOn(generateLexer)
    }
}
