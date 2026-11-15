package jp.co.bitz.gkskbridge

import jp.co.bitz.gameplaykit.GKAgent
import jp.co.bitz.gameplaykit.GKAgent2D
import jp.co.bitz.gameplaykit.GKAgentDelegate
import jp.co.bitz.gameplaykit.GKComponent
import jp.co.bitz.spritekit.SKNode

/**
 * A [GKComponent] that associates a [GKEntity][jp.co.bitz.gameplaykit.GKEntity] with an
 * [SKNode], mirroring GameplayKit's `GKSKNodeComponent`. Adding this component to an entity
 * automatically sets [SKNode.entity] on [node] to that entity (and clears it again on removal),
 * matching Apple's documented behavior.
 *
 * Like Apple's, it is also a [GKAgentDelegate], so Apple-shaped steering code ports unchanged:
 * set `agent.delegate = nodeComponent` and the node follows the agent. Before each agent update
 * the agent is seeded from the node ([agentWillUpdate]), so the game can still move the node
 * directly; after it the node is moved to the agent ([agentDidUpdate]). Only [GKAgent2D] is
 * synced — see `docs/API_COMPATIBILITY.md` for 3D agents. [GKAgentNodeComponent] remains as an
 * alternative that syncs once per frame instead.
 *
 * @property node The SpriteKit node associated with this component's entity.
 */
public open class GKSKNodeComponent(public val node: SKNode) :
    GKComponent(),
    GKAgentDelegate {
    override fun didAddToEntity() {
        node.entity = entity
    }

    override fun willRemoveFromEntity() {
        node.entity = null
    }

    /** Copies [node]'s position and `zRotation` onto [agent] — Apple's `agentWillUpdate(_:)`. */
    override fun agentWillUpdate(agent: GKAgent) {
        if (agent !is GKAgent2D) return
        agent.position = node.position.toGKVector2()
        agent.rotation = node.zRotation
    }

    /** Copies [agent]'s position and rotation onto [node] — Apple's `agentDidUpdate(_:)`. */
    override fun agentDidUpdate(agent: GKAgent) {
        if (agent !is GKAgent2D) return
        node.position = agent.position.toSKVector2()
        node.zRotation = agent.rotation
    }
}
