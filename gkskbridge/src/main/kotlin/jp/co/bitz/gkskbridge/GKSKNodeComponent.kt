package jp.co.bitz.gkskbridge

import jp.co.bitz.gameplaykit.GKComponent
import jp.co.bitz.spritekit.SKNode

/**
 * A [GKComponent] that associates a [GKEntity][jp.co.bitz.gameplaykit.GKEntity] with an
 * [SKNode], mirroring GameplayKit's `GKSKNodeComponent`. Adding this component to an entity
 * automatically sets [SKNode.entity] on [node] to that entity (and clears it again on removal),
 * matching Apple's documented behavior.
 *
 * @property node The SpriteKit node associated with this component's entity.
 */
public open class GKSKNodeComponent(public val node: SKNode) : GKComponent() {
    override fun didAddToEntity() {
        node.entity = entity
    }

    override fun willRemoveFromEntity() {
        node.entity = null
    }
}
