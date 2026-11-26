package jp.co.bitz.gkskbridge

import jp.co.bitz.gameplaykit.GKConstantNoiseSource
import jp.co.bitz.gameplaykit.GKNoise
import jp.co.bitz.gameplaykit.GKNoiseMap
import jp.co.bitz.gameplaykit.Vector2Int
import kotlin.test.Test
import kotlin.test.assertEquals

// Expectations are Apple's, recorded by the tic-tac-toe sample's GameplayKitParity probes
// (GKNoiseTextureParityTests).
class SKTextureNoiseTest {
    private fun map(
        columns: Int,
        rows: (Int) -> Pair<Float, Float>,
    ): GKNoiseMap =
        GKNoiseMap(GKNoise(GKConstantNoiseSource(0.0)), sampleCount = Vector2Int(columns, 2)).apply {
            for (x in 0 until columns) {
                val (bottom, top) = rows(x)
                setValue(bottom, Vector2Int(x, 0))
                setValue(top, Vector2Int(x, 1))
            }
        }

    private fun grays(pixels: IntArray): List<Int> {
        pixels.forEach { assertEquals(0xFF, it ushr 24, "opaque") }
        return pixels.map { it and 0xFF }
    }

    @Test
    fun `values map to gray from black at -1 to white at 1, top row first`() {
        val ramp = map(9) { x -> (x / 4f - 1f).let { it to -it } }

        assertEquals(
            listOf(255, 223, 191, 159, 127, 95, 63, 31, 0) + listOf(0, 31, 63, 95, 127, 159, 191, 223, 255),
            grays(noiseMapPixels(ramp)),
        )
    }

    @Test
    fun `channel values are rounded down`() {
        val fine = map(11) { x -> (x / 50f).let { it to -it } }

        assertEquals(
            listOf(127, 124, 122, 119, 117, 114, 112, 109, 107, 104, 101) +
                listOf(127, 130, 132, 135, 137, 140, 142, 145, 147, 150, 153),
            grays(noiseMapPixels(fine)),
        )
    }

    @Test
    fun `values outside -1 to 1 are clamped`() {
        val values = listOf(-3f, -1.5f, 1.5f, 3f)
        val outOfRange = map(4) { x -> values[x] to values[x] }

        assertEquals(listOf(0, 0, 255, 255, 0, 0, 255, 255), grays(noiseMapPixels(outOfRange)))
    }

    @Test
    fun `pixels are gray`() {
        val pixel = noiseMapPixels(map(1) { 0.5f to 0.5f }).first()

        assertEquals(0xFFBFBFBF.toInt(), pixel)
    }
}
