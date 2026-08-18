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
    fun `addEntity appends and keeps insertion order`() {
        val scene = GKScene()
        val first = GKEntity()
        val second = GKEntity()

        scene.addEntity(first)
        scene.addEntity(second)

        assertEquals(listOf(first, second), scene.entities)
    }

    @Test
    fun `removeEntity removes a previously added entity`() {
        val scene = GKScene()
        val first = GKEntity()
        val second = GKEntity()
        scene.addEntity(first)
        scene.addEntity(second)

        scene.removeEntity(first)

        assertEquals(listOf(second), scene.entities)
    }

    @Test
    fun `removeEntity is a no-op for an entity that was never added`() {
        val scene = GKScene()
        val entity = GKEntity()

        scene.removeEntity(entity)

        assertTrue(scene.entities.isEmpty())
    }

    @Test
    fun `graphs can be added and looked up by name`() {
        val scene = GKScene()
        val graph = GKGraph()

        scene.graphs["level"] = graph

        assertSame(graph, scene.graphs["level"])
    }
}
