package jp.co.bitz.gkskbridge

import jp.co.bitz.gameplaykit.GKPolygonObstacle
import jp.co.bitz.spritekit.SKNode
import jp.co.bitz.spritekit.SKSpriteNode
import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals
import jp.co.bitz.gameplaykit.Vector2 as GKVector2
import jp.co.bitz.spritekit.Vector2 as SKVector2

// Expectations are Apple's, recorded by the tic-tac-toe sample's GameplayKitParity probes
// (GKSKBridgeParityTests).
class SKNodeObstaclesTest {
    private fun sprite(
        width: Float,
        height: Float,
    ) = SKSpriteNode(size = SKVector2(width, height))

    private fun assertVertices(
        expected: List<Pair<Float, Float>>,
        obstacle: GKPolygonObstacle,
    ) {
        assertEquals(expected.size, obstacle.vertices.size)
        expected.zip(obstacle.vertices).forEach { (e, a: GKVector2) ->
            assertEquals(e.first, a.x, 0.001f)
            assertEquals(e.second, a.y, 0.001f)
        }
    }

    @Test
    fun `obstacles are the node's transformed bounds corners`() {
        val positioned = sprite(10f, 20f).apply { position = SKVector2(100f, 50f) }
        val rotated = sprite(10f, 20f).apply { zRotation = (PI / 2).toFloat() }
        val anchored = sprite(10f, 20f).apply { anchorPoint = SKVector2(0f, 0f) }
        val scaled = sprite(10f, 20f).apply { xScale = 2f }

        val obstacles = obstaclesFromNodeBounds(listOf(positioned, rotated, anchored, scaled))

        assertEquals(4, obstacles.size)
        assertVertices(listOf(95f to 40f, 105f to 40f, 105f to 60f, 95f to 60f), obstacles[0])
        assertVertices(listOf(10f to -5f, 10f to 5f, -10f to 5f, -10f to -5f), obstacles[1])
        assertVertices(listOf(0f to 0f, 10f to 0f, 10f to 20f, 0f to 20f), obstacles[2])
        assertVertices(listOf(-10f to -10f, 10f to -10f, 10f to 10f, -10f to 10f), obstacles[3])
    }

    @Test
    fun `obstacles go through every ancestor's transform, the root's included`() {
        val parent = SKNode().apply { position = SKVector2(1000f, 0f) }
        val child = sprite(4f, 4f)
        parent.addChild(child)

        val obstacle = obstaclesFromNodeBounds(listOf(child)).single()

        assertVertices(listOf(998f to -2f, 1002f to -2f, 1002f to 2f, 998f to 2f), obstacle)
    }

    @Test
    fun `obstacles use only the node's own content`() {
        val withChild = sprite(2f, 2f)
        withChild.addChild(sprite(2f, 2f).apply { position = SKVector2(10f, 0f) })
        val empty = SKNode().apply { position = SKVector2(3f, 3f) }

        val obstacles = obstaclesFromNodeBounds(listOf(withChild, empty))

        assertVertices(listOf(-1f to -1f, 1f to -1f, 1f to 1f, -1f to 1f), obstacles[0])
        assertVertices(listOf(3f to 3f, 3f to 3f, 3f to 3f, 3f to 3f), obstacles[1])
    }

    @Test
    fun `building obstacles leaves the node's transform unchanged`() {
        val node =
            sprite(10f, 20f).apply {
                position = SKVector2(7f, 8f)
                zRotation = 0.3f
                xScale = 2f
                yScale = 3f
            }

        obstaclesFromNodeBounds(listOf(node))

        assertEquals(SKVector2(7f, 8f), node.position)
        assertEquals(0.3f, node.zRotation)
        assertEquals(2f, node.xScale)
        assertEquals(3f, node.yScale)
    }
}
