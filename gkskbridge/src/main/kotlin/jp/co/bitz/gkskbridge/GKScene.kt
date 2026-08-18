package jp.co.bitz.gkskbridge

import jp.co.bitz.gameplaykit.GKEntity
import jp.co.bitz.gameplaykit.GKGraph
import jp.co.bitz.spritekit.SKNode

/**
 * A container pairing an [SKNode] scene tree with the [GKEntity]s and [GKGraph]s that drive it,
 * mirroring GameplayKit's `GKScene`.
 *
 * **Not implemented:** Apple's `GKScene(fileNamed:)` initializer, which loads a scene (and its
 * associated entities/graphs) from a file authored in Xcode's GameplayKit scene editor. That file
 * format is Xcode-specific tooling output with no Android equivalent to load — the same exclusion
 * [GameplayKit for Android](https://github.com/bitzgroup/GameplayKit)'s own `docs/ROADMAP.md`
 * already documents for `GKScene` in general. Build a `GKScene` in code instead: construct an
 * [SKNode]/[SKScene][jp.co.bitz.spritekit.SKScene] tree, assign it to [rootNode], and add
 * [GKEntity]s via [addEntity]/populate [graphs] directly.
 */
public open class GKScene {
    /** The root node of the SpriteKit scene tree associated with this scene, or `null`. */
    public var rootNode: SKNode? = null

    private val mutableEntities: MutableList<GKEntity> = mutableListOf()

    /** The entities associated with this scene. Add/remove via [addEntity]/[removeEntity]. */
    public val entities: List<GKEntity> get() = mutableEntities

    /** The pathfinding graphs associated with this scene, keyed by name. */
    public val graphs: MutableMap<String, GKGraph> = mutableMapOf()

    /** Adds [entity] to this scene's [entities], matching Apple's `addEntity(_:)`. */
    public fun addEntity(entity: GKEntity) {
        mutableEntities.add(entity)
    }

    /**
     * Removes [entity] from this scene's [entities], matching Apple's `removeEntity(_:)`. No-op
     * if it isn't present.
     */
    public fun removeEntity(entity: GKEntity) {
        mutableEntities.remove(entity)
    }
}
