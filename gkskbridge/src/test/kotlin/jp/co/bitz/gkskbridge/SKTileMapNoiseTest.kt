package jp.co.bitz.gkskbridge

import jp.co.bitz.gameplaykit.GKNoise
import jp.co.bitz.gameplaykit.GKNoiseMap
import jp.co.bitz.gameplaykit.Vector2Int
import jp.co.bitz.spritekit.SKTileDefinition
import jp.co.bitz.spritekit.SKTileGroup
import jp.co.bitz.spritekit.SKTileMapNode
import jp.co.bitz.spritekit.SKTileSet
import jp.co.bitz.spritekit.Vector2
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

// Expectations are Apple's, recorded by the tic-tac-toe sample's GameplayKitParity probes
// (GKNoiseTileMapParityTests).
class SKTileMapNoiseTest {
    private fun group(name: String) = SKTileGroup(SKTileDefinition(size = Vector2(10f, 10f))).apply { this.name = name }

    private val abc = SKTileSet(listOf(group("a"), group("b"), group("c")))

    /** A noise map whose samples are exactly `value(x, y)`. */
    private fun noiseMap(
        samplesX: Int,
        samplesY: Int,
        value: (Int, Int) -> Float,
    ): GKNoiseMap {
        val map = GKNoiseMap(GKNoise(), sampleCount = Vector2Int(samplesX, samplesY))
        for (y in 0 until samplesY) {
            for (x in 0 until samplesX) map.setValue(value(x, y), Vector2Int(x, y))
        }
        return map
    }

    private fun maps(
        set: SKTileSet,
        noise: GKNoiseMap,
        thresholds: List<Double>,
        columns: Int = 4,
        rows: Int = 3,
    ) = tileMapNodes(set, columns, rows, Vector2(10f, 10f), noise, thresholds)

    /** A map's cells as group names, rows bottom to top separated by `/`, `-` for empty. */
    private fun layout(map: SKTileMapNode): String =
        (0 until map.numberOfRows).joinToString("/") { row ->
            (0 until map.numberOfColumns).joinToString("") { column -> map.tileGroup(column, row)?.name ?: "-" }
        }

    @Test
    fun `one map per threshold plus one, sharing the given layout`() {
        val generated = maps(abc, noiseMap(4, 3) { _, _ -> 0f }, listOf(-0.5, 0.5))

        assertEquals(3, generated.size)
        for (map in generated) {
            assertSame(abc, map.tileSet)
            assertEquals(4, map.numberOfColumns)
            assertEquals(3, map.numberOfRows)
            assertEquals(Vector2(10f, 10f), map.tileSize)
            assertEquals(Vector2.Zero, map.position)
            assertEquals(Vector2(0.5f, 0.5f), map.anchorPoint)
            assertEquals(true, map.enableAutomapping)
        }
        assertEquals(listOf("aaaa/aaaa/aaaa"), maps(abc, noiseMap(4, 3) { _, _ -> 0f }, emptyList()).map(::layout))
    }

    @Test
    fun `the first map is full and map k holds group k from threshold k - 1 up`() {
        val expected =
            mapOf(
                -1f to listOf("aaaa/aaaa/aaaa", "----/----/----", "----/----/----"),
                -0.6f to listOf("aaaa/aaaa/aaaa", "----/----/----", "----/----/----"),
                -0.5f to listOf("aaaa/aaaa/aaaa", "bbbb/bbbb/bbbb", "----/----/----"),
                0.2f to listOf("aaaa/aaaa/aaaa", "bbbb/bbbb/bbbb", "----/----/----"),
                0.5f to listOf("aaaa/aaaa/aaaa", "bbbb/bbbb/bbbb", "cccc/cccc/cccc"),
                1f to listOf("aaaa/aaaa/aaaa", "bbbb/bbbb/bbbb", "cccc/cccc/cccc"),
            )
        for ((value, layouts) in expected) {
            val generated = maps(abc, noiseMap(4, 3) { _, _ -> value }, listOf(-0.5, 0.5))
            assertEquals(layouts, generated.map(::layout), "noise $value")
        }
    }

    @Test
    fun `maps beyond the tile set's groups are empty`() {
        val two = SKTileSet(listOf(group("a"), group("b")))

        val generated = maps(two, noiseMap(4, 3) { _, _ -> 1f }, listOf(-0.5, 0.0, 0.5))

        assertEquals(
            listOf("aaaa/aaaa/aaaa", "bbbb/bbbb/bbbb", "----/----/----", "----/----/----"),
            generated.map(::layout),
        )
    }

    @Test
    fun `a gradient lands in bands`() {
        // Apple's layout for an 8-sample ramp from -1 to 1, over 8, 4 and 16 columns.
        val ramp = noiseMap(8, 2) { x, _ -> x / 7f * 2f - 1f }
        val thresholds = listOf(-0.5, 0.5)

        assertEquals(listOf("aaaaaaaa", "--bbbbbb", "-----ccc"), maps(abc, ramp, thresholds, 8, 1).map(::layout))
        assertEquals(listOf("aaaa", "-bbb", "---c"), maps(abc, ramp, thresholds, 4, 1).map(::layout))
        assertEquals(
            listOf("aaaaaaaaaaaaaaaa", "---bbbbbbbbbbbbb", "----------cccccc"),
            maps(abc, ramp, thresholds, 16, 1).map(::layout),
        )
    }

    @Test
    fun `each cell is the mean of 3 x 3 bilinear samples`() {
        // Values recorded on Apple for a ramp of 0.1 per sample (shown here in samples).
        val recorded =
            mapOf(
                (2 to 1) to listOf(0.7778f),
                (2 to 3) to listOf(0.3333f, 0.9259f, 1f),
                (4 to 1) to listOf(1.8889f),
                (4 to 2) to listOf(1f, 2.7778f),
                (5 to 4) to listOf(0.625f, 1.875f, 3.125f, 3.9861f),
                (8 to 2) to listOf(2f, 5.8889f),
                (8 to 3) to listOf(1.3333f, 4f, 6.4815f),
                (8 to 4) to listOf(1f, 3f, 5f, 6.7778f),
            )
        for ((key, expected) in recorded) {
            val (samples, cells) = key
            val across = cellNoise(noiseMap(samples, 2) { x, _ -> x * 0.1f }, cells, 1)[0].map { it * 10f }
            val up = cellNoise(noiseMap(2, samples) { _, y -> y * 0.1f }, 1, cells).map { it[0] * 10f }
            for (cell in expected.indices) {
                assertEquals(expected[cell], across[cell], 0.001f, "$samples samples, cell $cell of $cells")
                assertEquals(expected[cell], up[cell], 0.001f, "along y, $samples samples, cell $cell of $cells")
            }
        }
        // A curved field tells averaging apart from sampling at one point: x² over 8 samples.
        val square = cellNoise(noiseMap(8, 2) { x, _ -> x * x * 0.01f }, 4, 1)[0].map { it * 100f }
        listOf(1.4444f, 9.4444f, 25.4444f, 46.1111f).forEachIndexed { cell, value ->
            assertEquals(value, square[cell], 0.01f)
        }
    }
}
