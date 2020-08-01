package killerspacewreck.entities

enum class EnemyType(val vel: Float, val maxVel: Float = vel) {
    NORMAL(5.8f),
    BULKY(2.2f),
    TINY_SIN(0f),
    MOVE_SHOOTER(18f)
}