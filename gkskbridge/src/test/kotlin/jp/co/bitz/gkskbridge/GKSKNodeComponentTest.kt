package jp.co.bitz.gkskbridge

import jp.co.bitz.gameplaykit.GKAgent2D
import jp.co.bitz.gameplaykit.GKAgent3D
import jp.co.bitz.gameplaykit.GKEntity
import jp.co.bitz.spritekit.SKNode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.time.Duration.Companion.milliseconds
import jp.co.bitz.gameplaykit.Vector2 as GKVector2
import jp.co.bitz.spritekit.Vector2 as SKVector2

class GKSKNodeComponentTest {
    @Test
    fun `node is the node passed to the constructor`() {
        val node = SKNode()
        val component = GKSKNodeComponent(node)

        assertSame(node, component.node)
    }

    @Test
    fun `adding the component to an entity sets the node's entity`() {
        val entity = GKEntity()
        val node = SKNode()
        val component = GKSKNodeComponent(node)

        entity.addComponent(component)

        assertSame(entity, node.entity)
        assertEquals(entity, component.entity)
    }

    @Test
    fun `removing the component clears the node's entity`() {
        val entity = GKEntity()
        val node = SKNode()
        entity.addComponent(GKSKNodeComponent(node))

        entity.removeComponent<GKSKNodeComponent>()

        assertNull(node.entity)
    }

    @Test
    fun `replacing the entity's component with one for a different node updates both nodes' entity`() {
        val entity = GKEntity()
        val firstNode = SKNode()
        val secondNode = SKNode()
        entity.addComponent(GKSKNodeComponent(firstNode))

        entity.addComponent(GKSKNodeComponent(secondNode))

        assertNull(firstNode.entity)
        assertSame(entity, secondNode.entity)
    }

    // Expectations below are Apple's, recorded by the tic-tac-toe sample's GameplayKitParity
    // probes (GKSKBridgeParityTests).

    @Test
    fun `agentWillUpdate seeds a 2D agent from the node`() {
        val node =
            SKNode().apply {
                position = SKVector2(10f, 20f)
                zRotation = 0.5f
            }
        val agent = GKAgent2D()

        GKSKNodeComponent(node).agentWillUpdate(agent)

        assertEquals(GKVector2(10f, 20f), agent.position)
        assertEquals(0.5f, agent.rotation)
    }

    @Test
    fun `agentDidUpdate moves the node to a 2D agent`() {
        val node = SKNode()
        val agent =
            GKAgent2D().apply {
                position = GKVector2(30f, 40f)
                rotation = 1.25f
            }

        GKSKNodeComponent(node).agentDidUpdate(agent)

        assertEquals(SKVector2(30f, 40f), node.position)
        assertEquals(1.25f, node.zRotation)
    }

    @Test
    fun `as the agent's delegate the node drives and follows the agent`() {
        val node = SKNode()
        val agent = GKAgent2D()
        agent.delegate = GKSKNodeComponent(node)
        node.position = SKVector2(5f, 6f)

        agent.update(100.milliseconds)

        assertEquals(GKVector2(5f, 6f), agent.position)
        assertEquals(SKVector2(5f, 6f), node.position)
    }

    @Test
    fun `a 3D agent is left alone`() {
        val node = SKNode().apply { position = SKVector2(1f, 2f) }
        val agent = GKAgent3D()
        val component = GKSKNodeComponent(node)

        component.agentWillUpdate(agent)
        component.agentDidUpdate(agent)

        assertEquals(SKVector2(1f, 2f), node.position)
    }
}
