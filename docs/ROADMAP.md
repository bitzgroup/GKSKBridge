# Implementation Roadmap

This document tracks progress implementing a Kotlin/Android library that mirrors the parts of
Apple's [GameplayKit](https://developer.apple.com/documentation/gameplaykit) and
[SpriteKit](https://developer.apple.com/documentation/spritekit) APIs that reference each other.
See [`docs/ARCHITECTURE.md`](ARCHITECTURE.md) for why this bridging surface lives in its own repo
instead of inside either sibling library.

Check off items as they are implemented and tested.

## Design Principle: Kotlin-Idiomatic, Not a Literal Port

Same principle as the sibling repos ([GameplayKit](https://github.com/bitzgroup/GameplayKit),
[SpriteKit](https://github.com/bitzgroup/SpriteKit)): the public API follows Apple's design and
behavior, expressed in idiomatic Kotlin rather than a literal Obj-C/Swift transliteration.
Class/member names follow Apple's naming where a direct equivalent exists, for discoverability.
Apple's actual cross-framework bridging surface is small — `GKSKNodeComponent`, the `SKNode.entity`
back-reference, and `GKScene` — so most of this roadmap mirrors those three directly. Where a phase
has no Apple precedent to mirror (Phase 4), that's called out explicitly.

## Phase 0 — Project Setup

- [x] Scaffold Gradle Android library module (`gkskbridge`, `jp.co.bitz.gkskbridge`)
- [x] Configure Kotlin, min/target/compile SDK versions (minSdk 24, compileSdk/targetSdk 34)
- [x] Configure `ktlint`/`detekt`
- [x] Set up CI (GitHub Actions; checks out GameplayKit/SpriteKit as sibling repos, not submodules)
- [x] Wire dependency on GameplayKit/SpriteKit via Gradle project-path (no submodules of its own)
      — see [`docs/ARCHITECTURE.md`](ARCHITECTURE.md)
- [x] Set up Gitflow branching + branch protection on `main`/`develop`
- [ ] Maven publishing scaffold — **not currently planned**: a `project(...)` dependency doesn't
      translate to a resolvable Maven coordinate, so this would need GameplayKit/SpriteKit to
      publish stable artifacts first. Revisit if that changes.

## Phase 1 — Entity ↔ Node: `GKSKNodeComponent`

- [x] `GKSKNodeComponent` — a `GKComponent` wrapping an `SKNode` (`node: SKNode`, `init(node:)`)
- [x] Adding the component to a `GKEntity` automatically sets the node's `entity` property to that
      entity (matches Apple's documented behavior); removing it clears the node's `entity` again
- [x] Unit tests

## Phase 2 — Node → Entity back-reference: `SKNode.entity`

- [x] `SKNode.entity: GKEntity?` — extension property, primarily set by `GKSKNodeComponent` (Phase
      1) but assignable directly too, matching Apple's API. Implemented as a `WeakHashMap` side
      table keyed by node identity rather than a real stored property, since `SKNode` is defined
      in a separate module this repo doesn't own — see `docs/API_COMPATIBILITY.md`
- [x] Unit tests

## Phase 3 — `GKScene` (in-memory container)

- [x] `GKScene` — `rootNode: SKNode?`, `entities: MutableList<GKEntity>`,
      `graphs: MutableMap<String, GKGraph>`
- [x] **Out of scope:** the `.sks`-file-loading initializer (`GKScene(fileNamed:)`) — that format
      is Xcode's GameplayKit scene editor output, with no Android equivalent to load; this mirrors
      the same exclusion GameplayKit's own `docs/ROADMAP.md` already documents for `GKScene`
- [x] Unit tests

## Phase 4 — Agent steering → node sync helper (no direct Apple precedent)

Apple's `GKAgentDelegate` itself lives in GameplayKit and has no SpriteKit dependency — apps are
expected to implement it themselves to copy a `GKAgent`'s simulated position onto a visual node.
That boilerplate is exactly the kind of cross-framework glue this repo exists to hold, so it's in
scope even without a literal Apple type to mirror.

Design (confirmed with maintainer, follows the structure of Apple's own WWDC 2015 "DemoBots"
sample rather than any shipped API):

- [x] `GKAgentNodeComponent` — marker `GKComponent`; its `sync()` looks up the entity's
      `GKAgent2D` *by class each call* rather than the constructor capturing a fixed reference, so
      it keeps working if the agent component is ever replaced
- [x] `sync()` is **not** called from `GKComponent.update()` — running it mid-frame, before that
      frame's physics/actions have been simulated, would copy a stale or about-to-be-corrected
      position. It's meant to be called once per frame after everything else has settled, e.g.
      from an `SKScene` subclass's `didFinishUpdate()` override (matching DemoBots' pattern;
      `SKScene.didFinishUpdate()` already exists in SpriteKit for Android)
- [x] `GKScene.syncAgentNodes()` — batch convenience calling `sync()` for every entity in a scene
- [x] `toSKVector2()`/`toGKVector2()` — GameplayKit's and SpriteKit's `Vector2` are two
      independently-defined, identically-shaped, unrelated types (no dependency between the
      libraries), discovered while wiring `sync()`'s position copy; see `docs/API_COMPATIBILITY.md`
- [x] Unit tests

## Phase 5 — Documentation

- [x] KDoc on every public API surface
- [x] `docs/API_COMPATIBILITY.md` — deviation log from Apple's API shape (matching sibling
      repos' convention)
- [x] README usage examples per bridging feature
