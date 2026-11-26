# Apple Parity Gap Audit & Remediation Plan

Audit date: 2026-11-15, against `develop` at `60592d6` (0.2.0).

This is the bridge-side part of an audit run across all three sibling libraries (see
`docs/PARITY_GAP_PLAN.md` in [SpriteKit](https://github.com/bitzgroup/SpriteKit) and
[GameplayKit](https://github.com/bitzgroup/GameplayKit)). It lists the Apple API that exists
*only because* GameplayKit and SpriteKit know about each other, and that this repo — the only
place such API can live — does not have yet and has not recorded as a deviation.

The comparison is from the source and Apple's documentation only; nothing was re-verified against
the real frameworks, and no build or test was run as part of the audit.

Currently present: `SKNode.entity`, `GKSKNodeComponent`, `GKScene` (in-memory container),
`GKAgentNodeComponent`, `Vector2` conversions.

## Missing API

| # | Missing | Notes | Pri |
| --- | --- | --- | --- |
| ~~K1~~ | ~~`GKSKNodeComponent` as a `GKAgentDelegate`~~ | **Done.** `GKSKNodeComponent` implements `GKAgentDelegate` (`agentWillUpdate` seeds the agent from the node, `agentDidUpdate` moves the node), verified against Apple on iOS/tvOS simulators. 2D agents only — Apple's handling of `GKAgent3D` is garbage-in, recorded as a deviation. `GKAgentNodeComponent` stays as the once-per-frame alternative. | P1 |
| K2 | `SKNode.obstacles(fromNodeBounds:)`, `obstacles(fromNodePhysicsBodies:)`, `obstacles(fromSpriteTextures:accuracy:)` | **Bounds and physics-body variants done** as top-level `obstaclesFromNodeBounds(nodes)` and `obstaclesFromNodePhysicsBodies(nodes)` (no `SKNode` companion to extend), matching Apple's vertices (verified on iOS/tvOS simulators); the physics-body variant reads SpriteKit's `physicsBodyOutline`. Remaining: the texture variant needs alpha tracing (same prerequisite as `SKPhysicsBody(texture:size:)`). | P3 |
| ~~K3~~ | ~~`SKTileMapNode.tileMapNodes(tileSet:columns:rows:tileSize:from:tileTypeNoiseMapThresholds:)`~~ | **Done** as top-level `tileMapNodes(...)`, matching Apple's layers, threshold comparison and cell sampling (each cell the mean of 3 × 3 interpolated samples), verified on iOS/tvOS simulators. | P2 |
| ~~K4~~ | ~~`SKTexture(noiseMap:)`~~ | **Done** as a top-level factory function `SKTexture(noiseMap)`, built on SpriteKit's public `SKTexture(bitmap)`. It matches Apple's grayscale pixels (verified on iOS/tvOS simulators). Apple's coloring by the noise's `gradientColors` isn't mirrored, since GameplayKit for Android's `GKNoise` has none. | P2 |
| ~~K5~~ | ~~`GKScene` ↔ `SKScene` conveniences~~ | **Done** — recorded in `API_COMPATIBILITY.md`. | Doc only |

## Proposed work order

1. ~~K1, K2 (bounds and physics-body variants), K3, K4, K5~~ — done.
2. The texture variant of K2 — after the SpriteKit prerequisite named above exists.

Update this file as items land; delete it once the table is empty.
