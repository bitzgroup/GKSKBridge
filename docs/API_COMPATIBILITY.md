# API Compatibility Notes

This library mirrors the parts of Apple's [GameplayKit](https://developer.apple.com/documentation/gameplaykit)
and [SpriteKit](https://developer.apple.com/documentation/spritekit) APIs that reference each
other, but is written in idiomatic Kotlin rather than a literal Obj-C/Swift-to-Kotlin
transliteration (see `docs/ROADMAP.md`'s "Design Principle" section). This document is a quick
reference for developers who already know Apple's GameplayKit/SpriteKit and want to know exactly
where — and why — this library's shape differs. It does not restate behavior that matches Apple's
docs; only intentional deviations, omissions, and additions are listed.

Two deviation categories recur throughout and are called out once here rather than per item below:

- **Side-table stand-ins for properties Apple adds directly to a class this repo doesn't own.**
  Apple can add a real stored property to `SKNode` because GameplayKit and SpriteKit are both its
  own frameworks, tightly coupled at the ABI level. Here, `SKNode` lives in the separate
  [SpriteKit for Android](https://github.com/bitzgroup/SpriteKit) repo, so a Kotlin extension
  property can't add a backing field to it — an identity-keyed side table stands in instead.
- **No-Apple-precedent additions.** A few features exist here because they're exactly the kind of
  cross-framework glue this repo holds, even though Apple ships no equivalent type to mirror —
  their design instead follows the structure of Apple's own sample code (WWDC talks, etc.) where
  one exists.

## Entity ↔ node association (`GKSKNodeComponent`, `SKNode.entity`)

- **`SKNode.entity` is a side table, not a stored property.** On Apple platforms, GameplayKit adds
  a real stored `entity` property directly to SpriteKit's `SKNode`, since both frameworks are
  Apple's own and tightly coupled at the ABI level. Here, `SKNode` is defined in a separate module
  (the sibling [SpriteKit](https://github.com/bitzgroup/SpriteKit) repo) that this repo doesn't
  own, so a Kotlin extension property can't add a backing field to it. `SKNode.entity` is instead
  backed by a `WeakHashMap<SKNode, GKEntity>` side table keyed by node identity, so holding an
  entity reference doesn't keep an otherwise-unreferenced node alive. Behavior is otherwise
  identical from the caller's perspective: get/set works the same, and it's not thread-safe,
  matching `SKNode` itself (all access is expected to happen on the same thread that owns the
  node).

## Scene container (`GKScene`)

- **No `GKScene(fileNamed:)` initializer.** Apple's variant loads a scene, along with its
  associated entities and graphs, from a file authored in Xcode's GameplayKit scene editor. That
  format is Xcode-specific tooling output with no Android equivalent to load, so `GKScene` here is
  a plain in-memory container only — build the `rootNode`/`entities`/`graphs` in code and assign
  them directly. This mirrors the same exclusion
  [GameplayKit for Android](https://github.com/bitzgroup/GameplayKit)'s own docs already document
  for `GKScene` in general.

## Agent steering → node sync (`GKAgentNodeComponent`, `toSKVector2`/`toGKVector2`)

- **No Apple precedent.** `GKAgentDelegate` is itself framework-agnostic in real GameplayKit —
  apps are expected to implement it themselves to copy an agent's position onto a visual node.
  `GKAgentNodeComponent` follows the structure of Apple's own WWDC 2015 "DemoBots" sample instead
  of any shipped API: it looks up the entity's `GKAgent2D` by class on every `sync()` call rather
  than capturing a fixed reference, and `sync()` is meant to be called once per frame from outside
  the normal `GKComponent.update()` pass — e.g. an `SKScene` subclass's `didFinishUpdate()`
  override — once that frame's physics/actions have already been simulated.
- **`GameplayKit.Vector2` and `SpriteKit.Vector2` are unrelated types.** GameplayKit for Android
  and SpriteKit for Android each define their own single-precision `Vector2` — identically shaped
  (`x`/`y` `Float`s) but with no dependency between the libraries, so the compiler won't convert
  between them (SpriteKit's own `Vector2` KDoc calls this out explicitly). `toSKVector2()`/
  `toGKVector2()` are plain field-copy conversions bridging the two, used internally by
  `GKAgentNodeComponent.sync()` and available for any other GameplayKit ↔ SpriteKit position/
  vector handoff.
