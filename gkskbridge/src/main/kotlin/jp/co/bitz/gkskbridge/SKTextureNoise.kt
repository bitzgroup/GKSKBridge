package jp.co.bitz.gkskbridge

import android.graphics.Bitmap
import jp.co.bitz.gameplaykit.GKNoiseMap
import jp.co.bitz.gameplaykit.Vector2Int
import jp.co.bitz.spritekit.SKTexture

private const val OPAQUE_BLACK = 0xFF000000.toInt()
private const val MAX_CHANNEL = 255.0

/**
 * Creates a texture from [noiseMap]'s values — Apple's `SKTexture(noiseMap:)`. A top-level factory
 * function because `SKTexture` (in another library) can't gain a constructor from here.
 *
 * Matches Apple's (verified on iOS/tvOS simulators, recorded in the tic-tac-toe sample's
 * `GameplayKitParity` probes). The texture has one pixel per sample, so its size is the map's
 * `sampleCount`. Sample row 0 is the texture's bottom row, as in SpriteKit's y-up coordinates.
 * Each pixel is an opaque gray: a value `v`, clamped to `-1..1`, becomes the channel value
 * `(v + 1) / 2 × 255`, computed in double precision and rounded down. Apple colors the pixels
 * with the noise's `gradientColors` instead, which GameplayKit for Android doesn't have, so this
 * always uses Apple's default black-to-white gradient.
 */
@Suppress("FunctionName") // Named like the type it builds, as Apple's initializer is.
public fun SKTexture(noiseMap: GKNoiseMap): SKTexture {
    val width = noiseMap.sampleCount.x
    val height = noiseMap.sampleCount.y
    return SKTexture(Bitmap.createBitmap(noiseMapPixels(noiseMap), width, height, Bitmap.Config.ARGB_8888))
}

/** [noiseMap]'s pixels as ARGB colors, row by row from the top (the map's last sample row). */
internal fun noiseMapPixels(noiseMap: GKNoiseMap): IntArray {
    val width = noiseMap.sampleCount.x
    val height = noiseMap.sampleCount.y
    return IntArray(width * height) { index ->
        val value = noiseMap.value(Vector2Int(index % width, height - 1 - index / width)).coerceIn(-1f, 1f)
        // In double precision, as Apple computes it: -0.2f gives 101, where float math gives 102.
        val gray = ((value.toDouble() + 1.0) / 2.0 * MAX_CHANNEL).toInt()
        OPAQUE_BLACK or (gray shl 16) or (gray shl 8) or gray
    }
}
