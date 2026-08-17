package jp.co.bitz.gkskbridge

import jp.co.bitz.gameplaykit.Vector2 as GKVector2
import jp.co.bitz.spritekit.Vector2 as SKVector2

/**
 * Converts a GameplayKit [GKVector2] to SpriteKit's identically-shaped but distinct [SKVector2].
 *
 * The two libraries have no dependency on each other, so despite matching `(x, y)` shapes —
 * SpriteKit's own `Vector2` KDoc calls this out explicitly — they're unrelated types the compiler
 * won't convert between automatically. Smoothing over exactly this kind of friction is what this
 * repo exists for.
 */
public fun GKVector2.toSKVector2(): SKVector2 = SKVector2(x, y)

/** The inverse of [toSKVector2]. */
public fun SKVector2.toGKVector2(): GKVector2 = GKVector2(x, y)
