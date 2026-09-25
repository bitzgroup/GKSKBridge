pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    // Resolves/auto-provisions the JVM used to run the Gradle Daemon
    // (gradle/gradle-daemon-jvm.properties), so contributors don't need a matching JDK preinstalled.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "GKSKBridge"

include(":gkskbridge")

// Standalone dev/CI build only. GKSKBridge carries no submodules of its own (see
// docs/ARCHITECTURE.md) — this assumes GameplayKit and SpriteKit are checked out as sibling
// directories next to this repo (`../GameplayKit`, `../SpriteKit`), which is how the CI workflow
// (.github/workflows/ci.yml) sets things up. A host app embedding GKSKBridge as a git submodule
// wires these same project paths itself in its own settings.gradle.kts (see README "Usage as a
// git submodule") — this block is not part of that path.
// Gradle 9's stricter multi-project validation requires the implicit ":GameplayKit"/":SpriteKit"
// parent path segments to resolve to a real directory too (previously only a deprecation warning:
// "Configuring project without an existing directory is not allowed"). They're never meant to be
// built — pointing them at the sibling repo roots would make Gradle evaluate *those* repos' own
// root build.gradle.kts against this build's catalog (which lacks their plugins, e.g.
// kotlin.compose), so redirect each to a build file that doesn't exist instead: Gradle then treats
// it as an empty pass-through project, satisfying only the directory-exists check.
include(":GameplayKit")
project(":GameplayKit").apply {
    projectDir = file("../GameplayKit")
    buildFileName = "unused.gradle.kts"
}
include(":GameplayKit:gameplaykit")
project(":GameplayKit:gameplaykit").projectDir = file("../GameplayKit/gameplaykit")

include(":SpriteKit")
project(":SpriteKit").apply {
    projectDir = file("../SpriteKit")
    buildFileName = "unused.gradle.kts"
}
include(":SpriteKit:spritekit")
project(":SpriteKit:spritekit").projectDir = file("../SpriteKit/spritekit")
