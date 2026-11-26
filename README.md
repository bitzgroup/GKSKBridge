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

The full [`docs/ROADMAP.md`](docs/ROADMAP.md) plan is complete: entity ↔ node association
(`GKSKNodeComponent`, `SKNode.entity`), a scene container (`GKScene`), and an agent-steering-to-node
sync helper (`GKAgentNodeComponent`) are implemented and tested, plus — from a later Apple-parity
pass — `GKSKNodeComponent` as a `GKAgentDelegate`, `obstaclesFromNodeBounds` and `tileMapNodes`.

## Requirements

- Android `minSdk` 24, `compileSdk` 37
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

See [`CLAUDE.md`](CLAUDE.md) for the full command reference and project structure, and
[`CONTRIBUTING.md`](CONTRIBUTING.md) before opening a pull request.

## Usage

A quick tour of the API — see [`docs/API_COMPATIBILITY.md`](docs/API_COMPATIBILITY.md) for where
and why each one's shape differs from Apple's.

### Entity ↔ node association

```kotlin
val node = SKNode()
val entity = GKEntity()

entity.addComponent(GKSKNodeComponent(node))
node.entity // == entity, set automatically by GKSKNodeComponent

entity.removeComponent<GKSKNodeComponent>()
node.entity // == null again
```

### Scene container

```kotlin
val scene =
    GKScene().apply {
        rootNode = SKScene(size = Vector2(1080f, 1920f))
        addEntity(entity)
    }
```

### Agent steering → node sync

The Apple way: make the entity's `GKSKNodeComponent` the agent's delegate. The node then follows
the agent around every agent update (and a node the game moves itself seeds the agent first).

```kotlin
val agent = GKAgent2D().apply { behavior = GKBehavior.of(GKGoal.toSeekAgent(targetAgent), weight = 1f) }
agent.delegate = entity.component<GKSKNodeComponent>()
entity.addComponent(agent)
```

Or, to sync once per frame after actions and physics have settled, `GKAgentNodeComponent`:

```kotlin
val agent =
    GKAgent2D().apply {
        maxSpeed = 4f
        behavior = GKBehavior.of(GKGoal.toSeekAgent(targetAgent), weight = 1f)
    }
entity.addComponent(agent)
entity.addComponent(GKAgentNodeComponent())

class GameScene(size: Vector2, private val gkScene: GKScene) : SKScene(size) {
    override fun update(deltaTime: Duration) {
        gkScene.entities.forEach { it.update(deltaTime) } // advances each agent's steering simulation
    }

    override fun didFinishUpdate() {
        // Copy this frame's now-settled agent position/rotation onto every entity's node, after
        // actions and physics have already been simulated for the frame.
        gkScene.syncAgentNodes()
    }
}
```

### Obstacles from nodes

```kotlin
// One GKPolygonObstacle per node, from its bounds — Apple's SKNode.obstacles(fromNodeBounds:).
val graph = GKObstacleGraph(obstaclesFromNodeBounds(wallNodes), bufferRadius = 8f)
```

### Tile maps from noise

```kotlin
// Water everywhere, sand from -0.2 up, grass from 0.3 up: one stacked map per tile group —
// Apple's SKTileMapNode.tileMapNodes(tileSet:columns:rows:tileSize:from:tileTypeNoiseMapThresholds:).
val noiseMap = GKNoiseMap(GKNoise(GKPerlinNoiseSource()), sampleCount = Vector2Int(64, 64))
val layers = tileMapNodes(terrain, columns = 64, rows = 64, tileSize = Vector2(32f, 32f), from = noiseMap,
    tileTypeNoiseMapThresholds = listOf(-0.2, 0.3))
layers.forEach(scene::addChild)
```

### `Vector2` bridge

```kotlin
// GameplayKit's and SpriteKit's Vector2 are separate, identically-shaped types (see
// docs/API_COMPATIBILITY.md) — these convert between them.
val skPosition = agent.position.toSKVector2()
val gkPosition = node.position.toGKVector2()
```

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
