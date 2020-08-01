package killerspacewreck.entities

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.ParticleEffect
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Timer
import killerspacewreck.Core
import app.dietrying.util.*
import app.dietrying.entities.*
import app.dietrying.*

class Asteroids(npos: Vector2, nsize: Vector2, var enemy_type: EnemyType = EnemyType.NORMAL) : Entity(npos, nsize) {

    var effect: ParticleEffect
    var sprite: Sprite? = null
    var currentTexture: Texture
    var texture1: Texture
    var texture2: Texture

    var doneExploding = false

    var gotHit = false

    // for move shooters
    var startedMovingToEndPoint = false
    var endPoint: Vector2 = Vector2(0f, 0f)

    var bullets = Array<EnemyBullet>()

    var hostile = true

    // TODO: have several textures in Core that the sprite uses
    init {

        effect = ParticleEffect()
        effect.load(Gdx.files.internal("explosion.particle"), Gdx.files.internal("particles"))

        // without this there lateinit not initialized error for some fucking reason
        texture1 = Texture("enemy.png")
        texture2 = Texture("enemy.png")

        if (enemy_type == EnemyType.NORMAL) {
            hp = 2f
            sprite = Sprite(Texture("enemy.png"))
            texture1 = Texture("enemy.png")
            texture2 = Texture("enemy_2.png")
            sprite?.setSize(size.x, size.y)
        }
        if (enemy_type == EnemyType.BULKY) {
            hp = 6f
            size.set(size.x * 1.5f, size.y * 1.5f)
            sprite = Sprite(Texture("bulky.png"))
            texture1 = Texture("enemy.png")
            texture2 = Texture("enemy.png")
            sprite?.setSize(size.x, size.y)
        }
        if (enemy_type == EnemyType.TINY_SIN) {//TODO: where the fuck are these?
            // on left or right sides
            if (pos.x >= Const.CENTER.x + Const.SPAWN_DISTANCE || pos.x <= Const.CENTER.x - Const.SPAWN_DISTANCE) {
                size.set(size.x / 1.5f, size.y / 1.5f)
                sprite = Sprite(Texture("enemy.png"))
                texture1 = Texture("enemy.png")
                texture2 = Texture("enemy.png")
                sprite?.color = Color.CYAN
                sprite?.setSize(size.x, size.y)
            } else {
                alive = false
            }
        }
        if(enemy_type == EnemyType.MOVE_SHOOTER) {
            hp = 1f
            size.set(size.x / 1.5f, size.y / 1.5f)
            sprite = Sprite(Texture("enemy.png"))
            texture1 = Texture("enemy.png")
            texture2 = Texture("enemy.png")
            sprite?.color = Color.GREEN
            sprite?.setSize(size.x, size.y)
            hostile = false
        }

        // TODO: Make it try multiple times in a loop
        if (touching_others()) {
            val centerX = Const.CENTER.x
            val centerY = Const.CENTER.y
            val dist_from_center = Const.SPAWN_DISTANCE.toFloat()

            val k = MathUtils.random(1, 4)
            if (k == 1)
                pos.set(centerX + dist_from_center, MathUtils.random(centerY - dist_from_center, centerY + dist_from_center))
            if (k == 2)
                pos.set(centerX - dist_from_center, MathUtils.random(centerY - dist_from_center, centerY + dist_from_center))
            // bot
            if (k == 3)
                pos.set(MathUtils.random(centerX - dist_from_center, centerX + dist_from_center), centerY - dist_from_center)
            // top
            if (k == 4)
                pos.set(MathUtils.random(centerX - dist_from_center, centerX + dist_from_center), centerY + dist_from_center)
        }

        // still touching others?
        if (touching_others())
            alive = false

        if(pos.x > Const.CENTER.x && alive)
            sprite?.flip(true, false)


        vel = enemy_type.vel

        currentTexture = texture1
        animate()

    }

    private fun animate() {
        Timer.schedule(object : Timer.Task() {
            override fun run() {
                if (currentTexture == texture1)
                    currentTexture = texture2
                else
                    currentTexture = texture1
                animate()
            }
        }, .4f)
    }

    private fun touching_others(): Boolean {
        for (a in Core.asteroids) {
            if (a.alive && a.bounds().overlaps(this.bounds()))
                return true
        }
        return false
    }

    override fun update() {

        if (hp <= 0) {
            // small thread sleep for impact if larger enemy
//            if(enemy_type == EnemyType.BULKY)
//                Thread.sleep(100L)

            val id = Core.explosion.play(2f)
            Core.explosion.setPitch(id, 2f)
            Const.KILL_COUNT++
            effect.start()
            alive = false
            Timer.schedule(object : Timer.Task() {
                override fun run() {
                    doneExploding = true
                }
            }, 1.8f)

            Core.shaker.rumble(50f, .5f)

            // quick flash
            Gfx.begin_shape_batch(ShapeRenderer.ShapeType.Filled)
            Gfx.setColor(Color.WHITE)
            Gfx.fillCircle(sprite!!.x + (sprite!!.width / 2f), sprite!!.y + (sprite!!.height / 2f), size.x)
            Gfx.end_shape_batch()


            // kill others near by after a fraction of a second
            // the fraction of a second is to show player what killed what for a clear chain reaction
            for (a in Core.asteroids)
                if (a.alive && a.explosionBounds().overlaps(explosionBounds()))
                    Timer.schedule(object : Timer.Task() {
                        override fun run() {
                            a.hp = 0f
                        }
                    }, .15f)

        } else {

            for(b in bullets)
                if(b.alive)
                    b.update()

            if(enemy_type != EnemyType.MOVE_SHOOTER) {
                normalize_direction()

                pos.x += dx * vel
                if (enemy_type === EnemyType.TINY_SIN) {
                    // max distance            // speed
                    pos.y = 500 * Math.sin((.02f * pos.x).toDouble()).toFloat()
                } else
                    pos.y += dy * vel
            }

            // move to random point near core
            if(enemy_type == EnemyType.MOVE_SHOOTER) {

                if(!startedMovingToEndPoint) {
                    startedMovingToEndPoint = true

                    //up

                    // down

                    // left

                    // right
                    endPoint.x = MathUtils.random(Const.CENTER.x + 500, Const.CENTER.x + 1200)
                    endPoint.y = MathUtils.random(Const.CENTER.y - 200, Const.CENTER.y + 200)

                    // NOTE: this enemy doesn't harm player. It can go through core but shoots at it
                    dx = endPoint.x - pos.x - (size.x / 2)
                    dy = endPoint.y - pos.y - (size.y / 2)
                    ass = Math.sqrt((dx * dx) + (dy * dy).toDouble()).toFloat()
                    dx /= ass
                    dy /= ass


                } else {
                    if(bounds().contains(endPoint) && vel != 0f) {
                        bullets.add(EnemyBullet(this))
                        vel = 0f

                        Timer.schedule(object : Timer.Task() {
                            override fun run() {
                                vel = enemy_type.maxVel
                                startedMovingToEndPoint = false
                            }
                        },MathUtils.random(1f, 3f))
                    }
                }
                pos.x += dx * vel
                pos.y += dy * vel

            }

        }
    }

    fun knockBack(a: Float) {
        pos.x += (Math.cos(a.toDouble()) * vel).toFloat()
        pos.y += (Math.cos(a.toDouble()) * vel).toFloat()
    }

    override fun render() {
        effect.setPosition(pos.x, pos.y)
        effect.update(Gdx.graphics.deltaTime)
        effect.draw(Gfx.sb)
        if (alive) {
            sprite?.texture = currentTexture
            if (gotHit) {
                gotHit = false
                sprite!!.setColor(Color.YELLOW)
            } else
                sprite!!.setColor(Color.WHITE)
            Gfx.drawSprite(sprite, pos.x, pos.y)

            for(b in bullets)
                if(b.alive)
                    b.render()
        }
    }

    override fun renderShapes() {
        if (alive) {
            Gfx.setColor(Color.WHITE)
//            if (Const.DEBUG)
//            Gfx.drawRect(explosionBounds().x, explosionBounds().y, explosionBounds().width, explosionBounds().height)
            for(b in bullets)
                if(b.alive)
                    b.renderShapes()
        }
    }

    override fun dispose() {
        if (sprite != null)
            sprite?.texture?.dispose()
        effect.dispose()

        for(b in bullets)
            b.dispose()
    }

    fun explosionBounds(): Rectangle {
        var s = size.x * 2.4f
        return Rectangle(sprite!!.x + (sprite!!.width / 2) - (s / 2), sprite!!.y + (sprite!!.height / 2) - (s / 2), s, s)
    }

}


class EnemyBullet(val e: Entity) : Entity(Vector2(e.pos.x, e.pos.y), Vector2(20f, 20f)) {

    init {
        vel = 8f
    }

    override fun update() {
        var dx = Const.CENTER.x - pos.x - size.x / 2
        var dy = Const.CENTER.y - pos.y - size.y / 2

        var ass = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()

        dx /= ass
        dy /= ass

        pos.x += dx * vel
        pos.y += dy * vel

        if(Intersector.overlaps(Core.planet.body, bounds())) {
            alive = false
            Core.planet.hp--
        }

    }

    override fun render() {

    }

    override fun renderShapes() {
        Gfx.setColor(Color.WHITE)
        Gfx.fillRect(pos.x, pos.y, size.x, size.y)
    }

    override fun dispose() {

    }

}