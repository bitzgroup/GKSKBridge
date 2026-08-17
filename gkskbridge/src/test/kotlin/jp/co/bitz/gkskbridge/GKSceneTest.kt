package jp.co.bitz.gkskbridge

import jp.co.bitz.gameplaykit.GKEntity
import jp.co.bitz.gameplaykit.GKGraph
import jp.co.bitz.spritekit.SKNode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class GKSceneTest {
    @Test
    fun `rootNode, entities, and graphs are empty by default`() {
        val scene = GKScene()

        assertNull(scene.rootNode)
        assertTrue(scene.entities.isEmpty())
        assertTrue(scene.graphs.isEmpty())
    }

    @Test
    fun `rootNode can be assigned and read back`() {
        val scene = GKScene()
        val node = SKNode()

        scene.rootNode = node

        assertSame(node, scene.rootNode)
    }

    @Test
    fun `entities can be added and are kept in insertion order`() {
        val scene = GKScene()
        val first = GKEntity()
        val second = GKEntity()

        scene.entities.add(first)
        scene.entities.add(second)

        assertEquals(listOf(first, second), scene.entities)
    }

    @Test
    fun `graphs can be added and looked up by name`() {
        val scene = GKScene()
        val graph = GKGraph()

        scene.graphs["level"] = graph

        assertSame(graph, scene.graphs["level"])
    }
}
