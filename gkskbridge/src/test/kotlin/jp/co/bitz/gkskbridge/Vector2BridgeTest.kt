package jp.co.bitz.gkskbridge

import kotlin.test.Test
import kotlin.test.assertEquals
import jp.co.bitz.gameplaykit.Vector2 as GKVector2
import jp.co.bitz.spritekit.Vector2 as SKVector2

class Vector2BridgeTest {
    @Test
    fun `toSKVector2 preserves x and y`() {
        val gkVector = GKVector2(1.5f, -2.5f)

        assertEquals(SKVector2(1.5f, -2.5f), gkVector.toSKVector2())
    }

    @Test
    fun `toGKVector2 preserves x and y`() {
        val skVector = SKVector2(1.5f, -2.5f)

        assertEquals(GKVector2(1.5f, -2.5f), skVector.toGKVector2())
    }

    @Test
    fun `round-tripping through both conversions is lossless`() {
        val original = GKVector2(3f, 4f)

        assertEquals(original, original.toSKVector2().toGKVector2())
    }
}
