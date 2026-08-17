# GKSKBridge

A Kotlin library for Android that bridges
[GameplayKit for Android](https://github.com/bitzgroup/GameplayKit) and
[SpriteKit for Android](https://github.com/bitzgroup/SpriteKit) — the parts of Apple's
GameplayKit/SpriteKit APIs that reference each other (entity-component ↔ scene-graph binding,
agent steering driving an `SKNode`, and so on), which those two sibling libraries deliberately
leave out so each stays usable on its own.

See [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) for why this lives in its own repo instead of
inside either sibling library, and how it depends on both without vendoring copies of them.

## Status

Early scaffolding. The Gradle project builds, but no bridging API has landed yet — scope (which
cross-framework features this covers) isn't finalized.

## Requirements

- Android `minSdk` 24, `compileSdk`/`targetSdk` 34
- Kotlin 2.0+
- [GameplayKit](https://github.com/bitzgroup/GameplayKit) and
  [SpriteKit](https://github.com/bitzgroup/SpriteKit), checked out as sibling directories (see
  below)

## Building

This repo depends on GameplayKit and SpriteKit via Gradle project paths rather than vendoring
copies of them (see [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md)), so building it standalone
requires those two repos checked out next to it:

```
bitzgroup/
├── GameplayKit/
├── SpriteKit/
└── GKSKBridge/
```

```sh
./gradlew :gkskbridge:assemble          # build the library
./gradlew :gkskbridge:testDebugUnitTest # run unit tests
./gradlew :gkskbridge:ktlintCheck       # lint/format check
./gradlew :gkskbridge:detekt            # static analysis
```

(Commands are scoped to `:gkskbridge` — an unscoped run also runs the task on the included
GameplayKit/SpriteKit sibling projects, whose build scripts misresolve `$rootDir`-relative paths
against this repo's root. See [`CLAUDE.md`](CLAUDE.md#commands).)

See [`CLAUDE.md`](CLAUDE.md) for the full command reference and project structure.

## Usage as a git submodule

Like its two sibling repos, GKSKBridge is meant to be embedded into host apps as a git submodule,
alongside GameplayKit and SpriteKit — not vendored inside either of them. A host app's own
`settings.gradle.kts` includes all three directly:

```kotlin
include(":GameplayKit:gameplaykit")
project(":GameplayKit:gameplaykit").projectDir = file("GameplayKit/gameplaykit")

include(":SpriteKit:spritekit")
project(":SpriteKit:spritekit").projectDir = file("SpriteKit/spritekit")

include(":GKSKBridge:gkskbridge")
project(":GKSKBridge:gkskbridge").projectDir = file("GKSKBridge/gkskbridge")
```

## License

MIT — see [`LICENSE`](LICENSE).
