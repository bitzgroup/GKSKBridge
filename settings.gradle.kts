pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
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
include(":GameplayKit:gameplaykit")
project(":GameplayKit:gameplaykit").projectDir = file("../GameplayKit/gameplaykit")

include(":SpriteKit:spritekit")
project(":SpriteKit:spritekit").projectDir = file("../SpriteKit/spritekit")
