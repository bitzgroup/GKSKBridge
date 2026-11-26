package jp.co.bitz.gkskbridge

import jp.co.bitz.gameplaykit.GKNoiseMap
import jp.co.bitz.gameplaykit.Vector2Int
import jp.co.bitz.spritekit.SKTileMapNode
import jp.co.bitz.spritekit.SKTileSet
import jp.co.bitz.spritekit.Vector2
import kotlin.math.floor

/** Where, across a cell, the noise is sampled on each axis: at 1/6, 1/2 and 5/6 of it. */
private val CELL_SAMPLE_OFFSETS = floatArrayOf(1f / 6f, 0.5f, 5f / 6f)

/**
 * Builds tile maps from [from]'s noise — Apple's `SKTileMapNode.tileMapNodes(tileSet:columns:rows:
 * tileSize:from:tileTypeNoiseMapThresholds:)`. A top-level function because `SKTileMapNode` (in
 * another library) has no companion object to extend.
 *
 * Matches Apple's (verified on iOS/tvOS simulators, recorded in the tic-tac-toe sample's
 * `GameplayKitParity` probes):
 * - It returns one map per threshold plus one, each [columns] × [rows] of [tileSize] from
 *   [tileSet], at the origin with the default anchor point, for stacking as layers.
 * - Map 0 has [tileSet]'s first group in every cell. Map `k` has group `k` in every cell whose noise
 *   is at least `tileTypeNoiseMapThresholds[k - 1]`, and is empty if [tileSet] has no group `k`.
 * - A cell's noise is the mean of 3 × 3 samples across it, at 1/6, 1/2 and 5/6 of the cell on each
 *   axis. The cells span [from]'s samples (`sampleCount / columns` samples per cell, likewise for
 *   rows); a sample point between samples interpolates linearly between them, and one past the last
 *   sample takes the last sample's value.
 *
 * Tiles are placed with [SKTileMapNode.setTileGroup], so a map automaps them (as Apple's does).
 * Like any node access, call it on the scene's render thread if the maps are already in a scene.
 */
@Suppress("LongParameterList") // Apple's own six parameters
public fun tileMapNodes(
    tileSet: SKTileSet,
    columns: Int,
    rows: Int,
    tileSize: Vector2,
    from: GKNoiseMap,
    tileTypeNoiseMapThresholds: List<Double>,
): List<SKTileMapNode> {
    val noise = cellNoise(from, columns, rows)
    return List(tileTypeNoiseMapThresholds.size + 1) { layer ->
        val map = SKTileMapNode(tileSet, columns, rows, tileSize)
        val group = tileSet.tileGroups.getOrNull(layer)
        if (group != null) {
            val threshold = if (layer == 0) Double.NEGATIVE_INFINITY else tileTypeNoiseMapThresholds[layer - 1]
            for (row in 0 until rows) {
                for (column in 0 until columns) {
                    if (layer == 0 || noise[row][column] >= threshold) map.setTileGroup(group, column, row)
                }
            }
        }
        map
    }
}

/** Each cell's noise, `[row][column]`, as [tileMapNodes] describes. */
internal fun cellNoise(
    noiseMap: GKNoiseMap,
    columns: Int,
    rows: Int,
): Array<FloatArray> {
    val samplesX = noiseMap.sampleCount.x
    val samplesY = noiseMap.sampleCount.y
    val samples = Array(samplesY) { y -> FloatArray(samplesX) { x -> noiseMap.value(Vector2Int(x, y)) } }

    fun bilinear(
        x: Float,
        y: Float,
    ): Float {
        val (x0, x1, tx) = interpolationIndices(x, samplesX)
        val (y0, y1, ty) = interpolationIndices(y, samplesY)
        val bottom = samples[y0][x0] * (1f - tx) + samples[y0][x1] * tx
        val top = samples[y1][x0] * (1f - tx) + samples[y1][x1] * tx
        return bottom * (1f - ty) + top * ty
    }

    return Array(rows) { row ->
        FloatArray(columns) { column ->
            var sum = 0f
            for (offsetY in CELL_SAMPLE_OFFSETS) {
                val y = (row + offsetY) * samplesY / rows
                for (offsetX in CELL_SAMPLE_OFFSETS) sum += bilinear((column + offsetX) * samplesX / columns, y)
            }
            sum / (CELL_SAMPLE_OFFSETS.size * CELL_SAMPLE_OFFSETS.size)
        }
    }
}

/** The two samples around [position] (clamped to the last of [count]) and how far it is between them. */
private fun interpolationIndices(
    position: Float,
    count: Int,
): Triple<Int, Int, Float> {
    val clamped = position.coerceIn(0f, (count - 1).toFloat())
    val lower = floor(clamped).toInt()
    val upper = minOf(lower + 1, count - 1)
    return Triple(lower, upper, clamped - lower)
}
