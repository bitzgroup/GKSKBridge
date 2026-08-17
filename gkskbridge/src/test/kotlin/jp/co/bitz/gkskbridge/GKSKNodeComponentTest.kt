package jp.co.bitz.gkskbridge

import jp.co.bitz.gameplaykit.GKEntity
import jp.co.bitz.spritekit.SKNode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

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
}
