package jp.co.bitz.gkskbridge

import jp.co.bitz.gameplaykit.GKAgent2D
import jp.co.bitz.gameplaykit.GKEntity
import jp.co.bitz.spritekit.SKNode
import kotlin.test.Test
import kotlin.test.assertEquals
import jp.co.bitz.gameplaykit.Vector2 as GKVector2
import jp.co.bitz.spritekit.Vector2 as SKVector2

class GKAgentNodeComponentTest {
    @Test
    fun `sync is a no-op when the entity has no agent`() {
        val entity = GKEntity()
        val node = SKNode()
        entity.addComponent(GKSKNodeComponent(node))
        entity.addComponent(GKAgentNodeComponent())

        entity.component<GKAgentNodeComponent>()!!.sync()

        assertEquals(SKVector2.Zero, node.position)
    }

    @Test
    fun `sync is a no-op when the entity has no node component`() {
        val entity = GKEntity()
        val agent = GKAgent2D().apply { position = GKVector2(3f, 4f) }
        entity.addComponent(agent)
        entity.addComponent(GKAgentNodeComponent())

        // Just verifying this doesn't throw with no node component attached.
        entity.component<GKAgentNodeComponent>()!!.sync()
    }

    @Test
    fun `sync copies the agent's position and rotation onto the node, converting Vector2 types`() {
        val entity = GKEntity()
        val node = SKNode()
        val agent =
            GKAgent2D().apply {
                position = GKVector2(3f, 4f)
                rotation = 1.25f
            }
        entity.addComponent(GKSKNodeComponent(node))
        entity.addComponent(agent)
        entity.addComponent(GKAgentNodeComponent())

        entity.component<GKAgentNodeComponent>()!!.sync()

        assertEquals(SKVector2(3f, 4f), node.position)
        assertEquals(agent.rotation, node.zRotation)
    }

    @Test
    fun `sync reflects the entity's current agent component even if it was replaced`() {
        val entity = GKEntity()
        val node = SKNode()
        entity.addComponent(GKSKNodeComponent(node))
        entity.addComponent(GKAgentNodeComponent())
        entity.addComponent(GKAgent2D().apply { position = GKVector2(1f, 1f) })

        val replacementAgent = GKAgent2D().apply { position = GKVector2(9f, 9f) }
        entity.addComponent(replacementAgent)
        entity.component<GKAgentNodeComponent>()!!.sync()

        assertEquals(SKVector2(9f, 9f), node.position)
    }
}
