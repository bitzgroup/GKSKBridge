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

- **`GKSKNodeComponent` is a `GKAgentDelegate`, as Apple's is.** `agentWillUpdate` copies the
  node's `position`/`zRotation` onto the agent, `agentDidUpdate` copies them back (verified on
  iOS/tvOS simulators, recorded in the tic-tac-toe sample's `GameplayKitParity` probes), so
  `agent.delegate = nodeComponent` ports unchanged. **Only `GKAgent2D` is synced; a `GKAgent3D` is
  left alone.** Apple's component treats any agent as 2D: it copies a 3D agent's `x`/`y` onto the
  node, but seeding one writes `z = 0` and the node's rotation and `y` into raw cells of the
  agent's rotation matrix — not behavior worth reproducing.

## Scene container (`GKScene`)

- **`rootNode` is a plain `SKNode?`.** Apple types it as `GKSceneRootNodeType`, a marker protocol
  that `SKScene` (and SceneKit's `SCNScene`) conform to. With SpriteKit the only renderer here,
  `SKNode?` covers the same use; assign the `SKScene` itself, as on Apple platforms.

- **No `GKScene(fileNamed:)` initializer.** Apple's variant loads a scene, along with its
  associated entities and graphs, from a file authored in Xcode's GameplayKit scene editor. That
  format is Xcode-specific tooling output with no Android equivalent to load, so `GKScene` here is
  a plain in-memory container only — build the scene tree in code, assign it to `rootNode`, and add
  entities via `addEntity(_:)`/populate `graphs` directly. This mirrors the same exclusion
  [GameplayKit for Android](https://github.com/bitzgroup/GameplayKit)'s own docs already document
  for `GKScene` in general.

## Agent steering → node sync (`GKAgentNodeComponent`, `toSKVector2`/`toGKVector2`)

- **No Apple precedent.** Apple's own way to make a node follow an agent is setting a
  `GKSKNodeComponent` as the agent's delegate, which this library supports too (above).
  `GKAgentNodeComponent` is an addition for syncing once per frame instead of around every agent
  update, following the structure of Apple's own WWDC 2015 "DemoBots" sample: it looks up the entity's `GKAgent2D` by class on every `sync()` call rather
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

## Obstacles from nodes (`obstaclesFromNodeBounds`)

- **A top-level function, not `SKNode.obstacles(fromNodeBounds:)`.** Apple's is a class method on
  `SKNode`; this library's `SKNode` lives in another repo and has no companion object to extend.
  Behavior matches Apple's (verified on iOS/tvOS simulators, recorded in the tic-tac-toe sample's
  `GameplayKitParity` probes): four vertices per node, the corners of the node's own content
  bounds through its own and every ancestor's transform — the root's included — so a rotated node
  gives the rotated rectangle and children don't widen it.
- **Not implemented yet:** `obstacles(fromNodePhysicsBodies:)` (needs SpriteKit for Android to
  expose physics-body shapes) and `obstacles(fromSpriteTextures:accuracy:)` (needs alpha-outline
  tracing) — see `docs/PARITY_GAP_PLAN.md` (K2).

## Tile maps from noise (`tileMapNodes`)

- **A top-level function, not `SKTileMapNode.tileMapNodes(tileSet:columns:rows:tileSize:from:
  tileTypeNoiseMapThresholds:)`**, for the same reason as `obstaclesFromNodeBounds`; the
  thresholds are a `List<Double>` rather than `[NSNumber]`. Behavior matches Apple's (verified on
  iOS/tvOS simulators, recorded in the tic-tac-toe sample's `GameplayKitParity` probes): one map
  per threshold plus one; map 0 full of the first group, map `k` holding group `k` wherever the
  noise is at least threshold `k − 1` (empty when the tile set has no group `k`); each cell's
  noise the mean of 3 × 3 linearly interpolated samples at 1/6, 1/2 and 5/6 of the cell, clamped
  to the noise map's last sample.
- **Tiles are placed with `setTileGroup`, so the maps automap**, as Apple's do. Which rule
  SpriteKit for Android picks at a map's edges can differ from Apple's — see SpriteKit's
  `docs/PARITY_GAP_PLAN.md` (M2).
