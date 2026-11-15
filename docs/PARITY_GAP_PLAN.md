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
| K1 | `GKSKNodeComponent` as a `GKAgentDelegate` | Apple's `GKSKNodeComponent` conforms to `GKAgentDelegate` and moves its node from `agentDidUpdate(_:)` (and seeds the agent in `agentWillUpdate(_:)`). Here that job was given to a separate, no-Apple-precedent `GKAgentNodeComponent` + manual `sync()`. Add the delegate conformance so Apple-shaped code (`agent.delegate = nodeComponent`) ports unchanged; keep `GKAgentNodeComponent` as the documented alternative. | P1 |
| K2 | `SKNode.obstacles(fromNodeBounds:)`, `obstacles(fromNodePhysicsBodies:)`, `obstacles(fromSpriteTextures:accuracy:)` | Build `GKPolygonObstacle`s from nodes for `GKObstacleGraph`/`GKMeshGraph`. The bounds variant is trivial; the physics-body variant needs SpriteKit to expose body shapes; the texture variant needs alpha tracing (same prerequisite as `SKPhysicsBody(texture:size:)`). | P1 / P2 / P3 |
| K3 | `SKTileMapNode.tileMapNodes(tileSet:columns:rows:tileSize:from:tileTypeNoiseMapThresholds:)` | Procedural tile maps from a `GKNoiseMap`. | P2 |
| K4 | `SKTexture(noiseMap:)` | Texture from a `GKNoiseMap` (needs a public pixel-data `SKTexture` constructor in SpriteKit). | P2 |
| K5 | `GKScene` ↔ `SKScene` conveniences | Apple's `GKScene.rootNode` is typed to a `GKSceneRootNodeType` that `SKScene` conforms to; here it is a plain `SKNode?`. Fine as is — record it in `API_COMPATIBILITY.md`. | Doc only |

## Proposed work order

1. K1, K2 (bounds variant), K5 — no changes needed in the sibling libraries.
2. K3, K4 and the rest of K2 — after the SpriteKit prerequisites named above exist.

Update this file as items land; delete it once the table is empty.
