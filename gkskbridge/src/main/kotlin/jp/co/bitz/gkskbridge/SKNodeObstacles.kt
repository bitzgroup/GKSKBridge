package jp.co.bitz.gkskbridge

import jp.co.bitz.gameplaykit.GKPolygonObstacle
import jp.co.bitz.spritekit.Rect
import jp.co.bitz.spritekit.SKNode
import jp.co.bitz.spritekit.Vector2
import jp.co.bitz.spritekit.physicsBodyOutline
import kotlin.math.cos
import kotlin.math.sin

/**
 * Builds one [GKPolygonObstacle] per node from that node's own bounds — Apple's
 * `SKNode.obstacles(fromNodeBounds:)`, for feeding a `GKObstacleGraph`/`GKMeshGraph` or agent
 * avoidance goals. A top-level function because `SKNode` (in another library) has no companion
 * object to extend.
 *
 * Matches Apple's (verified on iOS/tvOS simulators, recorded in the tic-tac-toe sample's
 * `GameplayKitParity` probes): each obstacle has four vertices, the corners of the node's own
 * content bounds (bottom-left, bottom-right, top-right, top-left in the node's local space) taken
 * through the node's position, scale and rotation — a rotated node gives the rotated rectangle, not
 * a box around it — and then through every ancestor's, the root's own included (for a scene, whose
 * position is normally zero, that is scene space).
 * Children don't widen a node's obstacle, and a node with no content of its own (a plain [SKNode])
 * gives a degenerate obstacle at its position.
 *
 * Like any node access, call it on the scene's render thread.
 */
public fun obstaclesFromNodeBounds(nodes: List<SKNode>): List<GKPolygonObstacle> =
    nodes.map { node -> GKPolygonObstacle(node.boundsCornersInRootSpace().map { it.toGKVector2() }) }

/**
 * Builds a [GKPolygonObstacle] from the physics body of each node that has one — Apple's
 * `SKNode.obstacles(fromNodePhysicsBodies:)`. A top-level function for the same reason as
 * [obstaclesFromNodeBounds].
 *
 * Matches Apple's (verified on iOS/tvOS simulators, recorded in the tic-tac-toe sample's
 * `GameplayKitParity` probes): the vertices are the body's outline in its scene's space, as
 * SpriteKit's `physicsBodyOutline` describes. A rectangle or polygon gives its own vertices, a
 * circle an octagon, an edge chain its points (an edge loop repeats its first one). Nodes give no
 * obstacle when they have no body, when the body is a single edge, several parts or a concave
 * polygon, or when they aren't in a scene, so the result can be shorter than [nodes].
 *
 * Like any node access, call it on the scene's render thread.
 */
public fun obstaclesFromNodePhysicsBodies(nodes: List<SKNode>): List<GKPolygonObstacle> =
    nodes.mapNotNull {
            node ->
        physicsBodyOutline(node)?.let { outline -> GKPolygonObstacle(outline.map { it.toGKVector2() }) }
    }

private fun SKNode.boundsCornersInRootSpace(): List<Vector2> {
    val bounds = localContentBounds()
    val corners =
        listOf(
            Vector2(bounds.left, bounds.top),
            Vector2(bounds.right, bounds.top),
            Vector2(bounds.right, bounds.bottom),
            Vector2(bounds.left, bounds.bottom),
        )
    return generateSequence(this) { it.parent }.fold(corners) { points, node -> points.map(node::toParentSpace) }
}

/**
 * This node's own content bounds in its local space. SpriteKit exposes them only through [SKNode.frame]
 * (in the parent's space, or local space without a parent), so read `frame` with the node's
 * transform briefly set to identity.
 */
private fun SKNode.localContentBounds(): Rect {
    val savedPosition = position
    val savedRotation = zRotation
    val savedXScale = xScale
    val savedYScale = yScale
    try {
        position = Vector2.Zero
        zRotation = 0f
        xScale = 1f
        yScale = 1f
        return frame
    } finally {
        position = savedPosition
        zRotation = savedRotation
        xScale = savedXScale
        yScale = savedYScale
    }
}

/** Applies this node's own scale, rotation and position to [point] — SpriteKit's local-to-parent transform. */
private fun SKNode.toParentSpace(point: Vector2): Vector2 {
    val scaledX = point.x * xScale
    val scaledY = point.y * yScale
    val c = cos(zRotation)
    val s = sin(zRotation)
    return Vector2(position.x + scaledX * c - scaledY * s, position.y + scaledX * s + scaledY * c)
}
