package jp.co.bitz.gkskbridge

import jp.co.bitz.gameplaykit.GKEntity
import jp.co.bitz.spritekit.SKNode
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertSame

class SKNodeEntityTest {
    @Test
    fun `entity is null by default`() {
        val node = SKNode()

        assertNull(node.entity)
    }

    @Test
    fun `entity can be set and read back directly`() {
        val node = SKNode()
        val entity = GKEntity()

        node.entity = entity

        assertSame(entity, node.entity)
    }

    @Test
    fun `entity can be cleared by setting it to null`() {
        val node = SKNode()
        node.entity = GKEntity()

        node.entity = null

        assertNull(node.entity)
    }

    @Test
    fun `each node has its own independent entity`() {
        val firstNode = SKNode()
        val secondNode = SKNode()
        val entity = GKEntity()

        firstNode.entity = entity

        assertSame(entity, firstNode.entity)
        assertNull(secondNode.entity)
    }
}
