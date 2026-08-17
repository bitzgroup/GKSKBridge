package jp.co.bitz.gkskbridge

import jp.co.bitz.gameplaykit.GKAgent2D
import jp.co.bitz.gameplaykit.GKComponent

/**
 * Marks an entity as wanting its [GKSKNodeComponent] node kept in sync with its [GKAgent2D]'s
 * simulated position/rotation.
 *
 * Has no direct Apple precedent — GameplayKit's `GKAgentDelegate` is itself framework-agnostic,
 * and Apple's own WWDC 2015 "DemoBots" sample shows apps wiring this sync themselves. This
 * component follows that sample's structure rather than any shipped API:
 *
 * - **No fixed reference to the agent.** [sync] looks up the entity's [GKAgent2D] by class each
 *   call, rather than a constructor capturing one directly — so it keeps working if the entity's
 *   agent component is ever replaced, and doesn't require a particular attachment order.
 * - **Not driven from `update()`.** Running the sync mid-frame, before this frame's physics/
 *   actions have been simulated, would copy a stale or about-to-be-corrected position. Call
 *   [sync] once per frame instead, after everything else has settled — e.g. from an
 *   [jp.co.bitz.spritekit.SKScene] subclass's `didFinishUpdate()` override (see
 *   [syncAgentNodes] to do this for every entity in a [GKScene] at once).
 * - **Converts between the two libraries' distinct `Vector2` types** via [toSKVector2] — see that
 *   function's KDoc.
 */
public open class GKAgentNodeComponent : GKComponent() {
    /**
     * Copies this entity's [GKAgent2D] position/rotation onto its [GKSKNodeComponent]'s node.
     * No-op if either component isn't attached to the same entity as this one.
     */
    public open fun sync() {
        val owner = entity ?: return
        val agent = owner.component<GKAgent2D>() ?: return
        owner.component<GKSKNodeComponent>()?.node?.let { node ->
            node.position = agent.position.toSKVector2()
            node.zRotation = agent.rotation
        }
    }
}

/**
 * Calls [GKAgentNodeComponent.sync] for every entity in this scene that has one — a batch
 * convenience for the "call once per frame after everything else has settled" pattern
 * [GKAgentNodeComponent] documents. Typically called from an [jp.co.bitz.spritekit.SKScene]
 * subclass's `didFinishUpdate()` override.
 */
public fun GKScene.syncAgentNodes() {
    entities.forEach { entity -> entity.component<GKAgentNodeComponent>()?.sync() }
}
