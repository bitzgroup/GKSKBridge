# API Compatibility Notes

This library mirrors the parts of Apple's [GameplayKit](https://developer.apple.com/documentation/gameplaykit)
and [SpriteKit](https://developer.apple.com/documentation/spritekit) APIs that reference each
other, but is written in idiomatic Kotlin rather than a literal Obj-C/Swift-to-Kotlin
transliteration (see `docs/ROADMAP.md`'s "Design Principle" section). This document is a quick
reference for developers who already know Apple's GameplayKit/SpriteKit and want to know exactly
where — and why — this library's shape differs. It does not restate behavior that matches Apple's
docs; only intentional deviations, omissions, and additions are listed.

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
