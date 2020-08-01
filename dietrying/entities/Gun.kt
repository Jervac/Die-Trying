package killerspacewreck.entities

enum class Gun(val texture: String, val damage: Int, val velocity: Float, val hp: Int, val shotDelay: Float) {
    DEFAULT("bullet.png", 1, 70f, 1, .28f),
    BIGSHOT("bullet_1.png", 8, 60f, 5, .56f)
}