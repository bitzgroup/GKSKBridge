package jp.co.bitz.gkskbridge

import jp.co.bitz.gameplaykit.GKEntity
import jp.co.bitz.spritekit.SKNode
import java.util.WeakHashMap

/**
 * Back-reference side table for [SKNode.entity]. Apple implements `entity` as a real stored
 * property GameplayKit adds directly to `SKNode` — both frameworks are Apple's own and tightly
 * coupled at the ABI level. Here, [SKNode] is defined in a separate module this repo doesn't own,
 * so a Kotlin extension property can't add a backing field to it; this table stands in for that
 * field instead. Keyed by node identity via [WeakHashMap] so holding an entity reference doesn't
 * keep an otherwise-unreferenced node alive. See `docs/API_COMPATIBILITY.md`.
 *
 * Not thread-safe, matching [SKNode] itself — all access is expected to happen on the same
 * thread that owns the node.
 */
private val nodeEntities = WeakHashMap<SKNode, GKEntity>()

/**
 * The [GKEntity] this node is associated with, or `null` if none — mirrors the `entity` property
 * GameplayKit adds to SpriteKit's `SKNode` on Apple platforms. Primarily set automatically by
 * [GKSKNodeComponent] when it's added to an entity, but assignable directly too.
 */
public var SKNode.entity: GKEntity?
    get() = nodeEntities[this]
    set(value) {
        if (value == null) {
            nodeEntities.remove(this)
        } else {
            nodeEntities[this] = value
        }
    }
