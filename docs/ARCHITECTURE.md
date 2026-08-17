# Architecture

## Why this repo exists

On Apple platforms, GameplayKit and SpriteKit are both system frameworks, so a cross-reference
between them (e.g. `GKSKNodeComponent`) costs nothing extra to depend on. Their Android ports,
[GameplayKit](https://github.com/bitzgroup/GameplayKit) and
[SpriteKit](https://github.com/bitzgroup/SpriteKit), are separate OSS libraries that are each meant
to be usable standalone — SpriteKit's own `CLAUDE.md` explicitly states the two "are not integrated
with each other." GKSKBridge holds the parts that reference both (entity-component ↔ scene-graph
binding, agent steering driving an `SKNode`, and so on), so that a project needing only one of the
two libraries never has to pull in the other.

## Dependency mechanism: no submodules of its own

Both GameplayKit and SpriteKit are meant to be embedded into host apps as **git submodules**
(source, not published binaries — see each repo's README "Usage as a git submodule"). If GKSKBridge
also vendored its own nested submodule copies of them, a host app that separately embeds
GameplayKit and/or SpriteKit as its own top-level submodules would end up with **two independently
checked-out copies of the same classes** (e.g. `jp.co.bitz.spritekit.SKNode`) compiled from two
different working trees. Both copies share the same fully-qualified name but are distinct types to
the JVM/dex — this causes duplicate-class errors at dex-merge time, or, worse, silent version skew
where GKSKBridge's `SKNode` and the host app's `SKNode` are two different classes that happen to
look alike.

To avoid that, **GKSKBridge carries no submodules of its own.** It depends on GameplayKit and
SpriteKit via a plain Gradle project-path dependency:

```kotlin
// gkskbridge/build.gradle.kts
dependencies {
    implementation(project(":GameplayKit:gameplaykit"))
    implementation(project(":SpriteKit:spritekit"))
}
```

This only resolves if whichever `settings.gradle.kts` includes `:gkskbridge` *also* defines the
`:GameplayKit:gameplaykit` and `:SpriteKit:spritekit` project paths. Two builds do that today:

1. **This repo's own `settings.gradle.kts`** (standalone dev/CI) — points those project paths at
   plain sibling checkouts, `../GameplayKit/gameplaykit` and `../SpriteKit/spritekit`.
2. **A host app's `settings.gradle.kts`**, once it embeds all three repos as sibling git
   submodules — it defines the same project paths against its own submodule checkouts, the same
   way SpriteKit's README documents for `:SpriteKit:spritekit`:

   ```kotlin
   include(":GameplayKit:gameplaykit")
   project(":GameplayKit:gameplaykit").projectDir = file("GameplayKit/gameplaykit")

   include(":SpriteKit:spritekit")
   project(":SpriteKit:spritekit").projectDir = file("SpriteKit/spritekit")

   include(":GKSKBridge:gkskbridge")
   project(":GKSKBridge:gkskbridge").projectDir = file("GKSKBridge/gkskbridge")
   ```

Either way, GameplayKit's and SpriteKit's sources exist exactly once in the build, so their classes
are never duplicated.

## CI: sibling checkouts, not submodules

Because GKSKBridge itself needs GameplayKit/SpriteKit sources to build and test in isolation
(without a host app), `.github/workflows/ci.yml` checks out `bitzgroup/GameplayKit` and
`bitzgroup/SpriteKit` as plain sibling directories (`actions/checkout` with an explicit
`repository:` and `path:`) rather than registering them as `.gitmodules` entries. This keeps
GKSKBridge's own distributable tree submodule-free — the sibling checkouts only ever exist
transiently inside a CI run (or a contributor's local dev setup) and are also excluded via
`.gitignore` (`/GameplayKit/`, `/SpriteKit/`).

## Not yet settled

- **Publishing**: this module currently has no `maven-publish` scaffold, unlike its siblings.
  A `project(...)` dependency doesn't translate to a resolvable Maven coordinate, so a real Maven
  publish of GKSKBridge would need GameplayKit/SpriteKit to publish stable, resolvable artifacts
  first — revisit if/when that happens.
- **Scope**: exactly which cross-framework features this repo covers (entity↔node sync, agent
  steering, others) is not finalized yet.
