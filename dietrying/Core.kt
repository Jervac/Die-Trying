package killerspacewreck

import com.badlogic.gdx.Application
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.*
import com.badlogic.gdx.utils.Timer
import killerspacewreck.entities.*
import app.dietrying.util.*
import app.dietrying.entities.*
import app.dietrying.*
import java.util.*

class Core : ApplicationAdapter() {

    private var shot: Sound? = null

    private var shooting = false

    var startedPowerUp = false
    var startTimeForPowerUp = 0L

    lateinit var theme: Music

    override fun create() {

        Gfx.initOrtho(Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat(), false)
        Gfx.cam.zoom = 4f

        theme = Gdx.audio.newMusic(Gdx.files.internal("purely_grey.mp3"))
        theme.isLooping = true
        theme.volume = .5f
        theme.play()

        powerup = Gdx.audio.newSound(Gdx.files.internal("powerup.wav"))
        shot = Gdx.audio.newSound(Gdx.files.internal("shot.wav"))
        hit = Gdx.audio.newSound(Gdx.files.internal("hit.wav"))
        explosion = Gdx.audio.newSound(Gdx.files.internal("explosion.wav"))

        p = Player(Vector2(100f, 80f), Vector2(100f, 100f))
        planet = Planet(p)

        clean_up()
        spawnBatch()
    }

    // remove dead entities every 5 seconds
    private fun clean_up() {
        Timer.schedule(object : Timer.Task() {
            override fun run() {
                // clears enemies
                var it: MutableIterator<*> = asteroids.iterator()
                while (it.hasNext()) {
                    val a = it.next() as Asteroids
                    if (!a.alive && a.doneExploding)
                        it.remove()
                }

                // clears bullets
                it = p.bullets.iterator()
                while (it.hasNext()) {
                    if (!it.next().alive)
                        it.remove()
                }

                clean_up()
            }
        }, 1f)
    }

    private fun update() {
        input()

        if(paused)
            theme.pause()
        if(!paused && !theme.isPlaying)
            theme.play()

        if(Const.MUTED) {
            theme.volume = 0f
        } else {
            theme.volume = 1f
        }

        if(!paused) {
            p.update()
            for (e in asteroids)
                if (e.alive)
                    e.update()
            for (i in items)
                if (i.alive)
                    i.update()


            planet.update()

            if (oppose_player != null)
                if (oppose_player!!.alive)
                    oppose_player!!.update()

            for (shield in shields)
                if (shield.alive)
                    shield.update()



            if (startedPowerUp && p.power != null) {
                // duration of power up over?
                if ((System.currentTimeMillis() - startTimeForPowerUp) / 1000L >= p.power!!.duration) {//TODO: non null assertion? Bugs may be here NOTE NOTE NOTE NOTE NOTE N O T E
                    startedPowerUp = false
                    p.power = null
                    p.new_power = true  // TODO: do i really need this boolean anymore?
                    managePowers()  // will kill any powers that are active
                }
            }
        }
    }

    private fun spawnBatch() {
        if(!paused) {
            Const.STAGE++

            var enemyAmount = MathUtils.random(2, 8)

            for (i in 0..enemyAmount) {
                var type = MathUtils.random(1, 10)
                if (type > 3)
                    spawnEnemy(EnemyType.MOVE_SHOOTER)
                if (type <= 3)
                    spawnEnemy(EnemyType.BULKY)
            }

            if (MathUtils.random(1, MathUtils.random(3, 4)) == 1)
                items.add(PowerUp())

            Timer.schedule(object : Timer.Task() {
                override fun run() {
                    spawnBatch()
                }
            }, MathUtils.random(4f, 10f))
        }
    }

    private fun spawnEnemy(type: EnemyType) {
        //TODO: don't have variables created constantly
        val centerX = p.sprite.originX
        val centerY = p.sprite.originY
        val dist_from_center = Const.SPAWN_DISTANCE.toFloat()
        val size = 200f
            var k: Int = MathUtils.random(1, 4)
            // right
            if (k == 1)
                asteroids.add(Asteroids(Vector2(centerX + dist_from_center, MathUtils.random(centerY - dist_from_center, centerY + dist_from_center)), Vector2(size, size),type))
            // left
            if (k == 2)
                asteroids.add(Asteroids(Vector2(centerX - dist_from_center, MathUtils.random(centerY - dist_from_center, centerY + dist_from_center)), Vector2(size, size),type))
            // bot
            if (k == 3)
                asteroids.add(Asteroids(Vector2(MathUtils.random(centerX - dist_from_center, centerX + dist_from_center), centerY - dist_from_center), Vector2(size, size),type))
            // top
            if (k == 4)
                asteroids.add(Asteroids(Vector2(MathUtils.random(centerX - dist_from_center, centerX + dist_from_center), centerY + dist_from_center), Vector2(size, size),type))
    }

    private fun shoot() {
        val id = shot!!.play(.5f)
        shot!!.setPitch(id, MathUtils.random(1.8f, 2f))
        if (p.gun === Gun.DEFAULT)
        //TODO: GUN enum can have own shake value and duration
            shaker.rumble(7f, .5f)
        if (p.gun === Gun.BIGSHOT)
            shaker.rumble(12f, 26f)
        p.bullets.add(Bullet(Vector2(p.sprite.x, p.sprite.y), p.angle + MathUtils.random(-9, 9)))
        if (oppose_player != null && oppose_player!!.alive) {
            oppose_player!!.gun = p.gun
            oppose_player!!.bullets.add(Bullet(Vector2(oppose_player!!.sprite.x, oppose_player!!.sprite.y), oppose_player!!.angle + MathUtils.random(-9, 9)))
        }
        Timer.schedule(object : Timer.Task() {
            override fun run() {
                shooting = false
            }
        }, p.gun.shotDelay)
    }

    var tappedScreen = false

    private fun input() {
        if(!paused) {
            if (!shooting) {
                shoot()
                shooting = true
            }
            Inputer.mousePos.set(Gdx.input.x.toFloat(), Gdx.graphics.height - Gdx.input.y.toFloat(), 0f)
            if (Gdx.app.type == Application.ApplicationType.Android) {
                if (!tappedScreen && Gdx.input.isTouched && Inputer.mousePos.x > 0 && Inputer.mousePos.x < 50 && Inputer.mousePos.y > 0 && Inputer.mousePos.y < 50 && nextPower != null) {
                    nextPowerPress()
                    tappedScreen = true
                } else if (Gdx.input.isTouched && Gdx.input.x < Gdx.graphics.width / 2)
                    p.angle += p.rot_speed
                else if (Gdx.input.isTouched && Gdx.input.x > Gdx.graphics.width / 2)
                    p.angle -= p.rot_speed

                if (!Gdx.input.isTouched)
                    tappedScreen = false
            }

            if (Inputer.tappedKey(Input.Keys.ESCAPE))
                Gdx.app.exit()

            if (Inputer.pressedKey(Input.Keys.RIGHT)) {
                p.angle -= p.rot_speed
                if (oppose_player != null)
                    oppose_player!!.angle -= p.rot_speed
            }
            if (Inputer.pressedKey(Input.Keys.LEFT)) {
                p.angle += p.rot_speed
                if (oppose_player != null)
                    oppose_player!!.angle += p.rot_speed
            }

            if (Inputer.tappedKey(Input.Keys.DOWN) && nextPower != null)
                nextPowerPress()
        }

        if(paused && Gdx.input.isTouched || Inputer.tappedKey(Input.Keys.SPACE)) {
            paused = false
            planet.hp = planet.max_hp
            p.power = null
            p.gun = Gun.DEFAULT
            p.angle = 90f
            Const.STAGE = 0
            Const.KILL_COUNT = 0
            items.clear()
            asteroids.clear()
            nextPower = null
            // TODO: make it stop the timer responsible for spawning enemies so 2 batches aren't spawned at once
            spawnBatch()
        }
    }

    fun nextPowerPress() {
        startedPowerUp = true
        startTimeForPowerUp = System.currentTimeMillis()
        p.new_power = true
        p.power = nextPower
        nextPower = null
        managePowers()
    }

    override fun render() {
        Gfx.update()
        update()
        Gfx.setClearColor(Color(28 / 255f, 28 / 255f, 28 / 255f, 1f))
        if(!paused) {
            Gfx.cam.position.x += (p.sprite.x - Gfx.cam.position.x) * .1f
            Gfx.cam.position.y += (p.sprite.y - Gfx.cam.position.y) * .1f

            shaker.tick()

            Gfx.setColor(Color.WHITE)

            Gfx.begin_sprite_batch()
            p.render()

            for (e in asteroids)
                if (e.alive || !e.doneExploding)
                    e.render()

            for (e in items)
                if (e.alive)
                    e.render()

            if (oppose_player != null)
                oppose_player!!.render()

            for (shield in shields)
                if (shield.alive)
                    shield.render()
            Gfx.end_sprite_batch()
            renderShapes()
        }

        Gfx.begin_sprite_batch_ui()

        if (Const.DEBUG) {
            Gfx.drawTextUI("Stage: " + Const.STAGE, 100f, Gfx.font.lineHeight)
            Gfx.drawTextUI("Loops: " + Const.LOOPS, 200f, Gfx.font.lineHeight)
            Gfx.drawTextUI("Enemy Collection Size: " + asteroids.size, 300f, Gfx.font.lineHeight)
            Gfx.drawTextUI("Bullet Collection Size: " + p.bullets.size, 500f, Gfx.font.lineHeight)
            Gfx.drawTextUI(nextPower.toString(), 500f, Gfx.font.lineHeight * 8)
            Gfx.drawTextUI(p.power.toString(), 500f, Gfx.font.lineHeight * 10)
        }
        if(!paused)
            Gfx.drawTextUI("Kills: " + Const.KILL_COUNT, 10f, Gdx.graphics.height - Gfx.font.lineHeight)
        if(paused) {
            Gfx.drawTextUI("Paused [Tap to play again]", Gdx.graphics.width / 2f, Gdx.graphics.height - Gfx.font.lineHeight)
            Gfx.drawTextUI("Kills: " + Const.KILL_COUNT, Gdx.graphics.width / 2f, Gdx.graphics.height / 2f)
        }
        Gfx.end_sprite_batch_ui()
    }

    private fun renderShapes() {
        if(!paused) {
            Gfx.setColor(Color.WHITE)
            Gfx.begin_shape_batch(ShapeRenderer.ShapeType.Filled)

            p.renderShapes()
            if (oppose_player != null) {
                oppose_player!!.renderShapes()
            }

            planet.renderShapes()

            for (e in asteroids)
                if (e.alive)
                    e.renderShapes()

            for (e in items)
                if (e.alive)
                    e.renderShapes()


            Gfx.end_shape_batch()

            if (nextPower != null) {
                Gfx.begin_shape_batch_ui(ShapeRenderer.ShapeType.Line)
                if (nextPower == Power.SHIELD)
                    Gfx.setColorUI(Color.SKY)
                if (nextPower == Power.SECOND_PLAYER)
                    Gfx.setColorUI(Color.RED)
                Gfx.drawRectUI(0f, 0f, 50f, 50f)
                Gfx.end_shape_batch_ui()
            }
        }
    }

    fun powerUpButton(): Rectangle {
        return Rectangle(0f, 0f, 50f, 50f)
    }

    override fun dispose() {
        Gfx.dispose()
        for (e in asteroids)
            e.dispose()
        planet.dispose()
        shot!!.dispose()
        hit.dispose()
        explosion.dispose()
        powerup.dispose()
        for (e in asteroids)
            e.dispose()
        if (oppose_player != null)
            oppose_player!!.dispose()
        for (shield in shields)
            shield.dispose()
        theme.dispose()
    }

    override fun pause() {
        paused = true
    }

    override fun resume() {
        paused = false
    }

    companion object {

        var paused = false

        lateinit var hit: Sound
        lateinit var explosion: Sound
        lateinit var powerup: Sound

        // TODO: have player arraylist
        var oppose_player: Player? = null
        var asteroids = ArrayList<Asteroids>()
        var items = ArrayList<PowerUp>()
        internal var shields = ArrayList<Shield>()

        lateinit var p: Player

        var nextPower: Power? = null
        internal var shaker = ScreenShaker()

        lateinit internal var planet: Planet

        internal fun shield_setup() {
            if (shields.size > 0)
                for (s in shields)
                    s.dispose()
            shields.add(Shield(p, 90f))
            shields.add(Shield(p, 45f))
            shields.add(Shield(p, 0f))
            shields.add(Shield(p, -45f))
            shields.add(Shield(p, -90f))
        }

        fun managePowers() {
            if (p.new_power) {
                p.new_power = false
                println("managing powers")

                if (p.power === Power.SECOND_PLAYER) {
                    println("secondplayer")
                    if (oppose_player == null) {
                        oppose_player = Player(Vector2(p.pos.x, p.pos.y), Vector2(p.size.x, p.size.y))
                        oppose_player!!.alive = false
                        oppose_player!!.height += 20f
                    }
                    if (!oppose_player!!.alive) {
                        println("was not alive")
                        oppose_player!!.alive = true
                        oppose_player!!.angle = p.angle + 180
                    }
                } else {
                    oppose_player?.alive = false
                    oppose_player?.bullets?.clear()//TODO: have it call a bullet dispose method to dispose before clear
                }

                if (p.power === Power.SHIELD)
                    shield_setup()
                else
                    if (shields.size > 0) {
                        for (s in shields)
                            s.dispose()
                        shields.clear()
                    }

            }
        }
    }

}

internal class Planet(var player: Player) : Entity(Vector2(player.sprite.originX, player.sprite.originY), Vector2(player.height, player.height)) {

    var body: Circle

    var max_hp: Float = 0f

    var tStart = System.currentTimeMillis()

    var bodyColor = Color.WHITE

    init {
        hp = 5f
        max_hp = hp
        body = Circle(pos.x, pos.y, size.x)
    }

    override fun update() {
        body.setPosition(player.sprite.originX, player.sprite.originY)

        for (e in Core.asteroids)
            if (e.alive && e.hostile && this.alive && Intersector.overlaps(body, e.bounds())) {
                e.hp = 0f
                hp--
                Core.shaker.rumble(30f, .5f)
                bodyColor = Color.RED
            }

        if (hp <= 0) {
            val tEnd = System.currentTimeMillis()
            val tDelta = tEnd - tStart
            val elapsedSeconds = tDelta / 1000.0
            println("Lasted " + elapsedSeconds)

            Core.paused = true
        }
    }

    override fun render() {}

    override fun renderShapes() {
        Gfx.setColor(Color.WHITE)

        // outer outline
        Gdx.gl.glLineWidth(3f)
        Gfx.drawCircle(body.x, body.y, body.radius + 30)
        // shadow
//        Gfx.setColor(Color.BLACK)
//        Gfx.drawCircle(body.x - 10f, body.y - 5f, body.radius + 40)

        Gfx.setColor(bodyColor)

        val hp_size = hp / max_hp * size.x
        Gfx.fillCircle(body.x, body.y, hp_size)

        if (bodyColor == Color.RED)
            bodyColor = Color.WHITE

    }

    override fun dispose() {

    }
}

internal class Shield(var p: Player, var offset: Float) : Entity(Vector2(p.pos.x, p.pos.y), Vector2(p.size.x * 1, p.size.y)) {

    var height: Float = 0.toFloat()
    var raise: Float = 0.toFloat()
    var sprite: Sprite

    init {
        height = p.height
        raise = p.raise

        hp = 5f

        sprite = Sprite(Texture("shield.png"))
        sprite.setSize(size.x, size.y)
        sprite.color = Color.YELLOW

    }

    override fun update() {
        angle = p.angle + 180f + offset

        pos.x = Math.cos(Math.toRadians(angle.toDouble())).toFloat() * (height + raise)
        pos.y = Math.sin(Math.toRadians(angle.toDouble())).toFloat() * (height + raise)

        for (e in Core.asteroids) {
            if (e.alive && e.bounds().overlaps(bounds())) {
                hp--
                e.hp = 0f
            }
        }

        if (hp <= 0)
            alive = false
    }

    override fun render() {
        sprite.setPosition(pos.x, pos.y)
        sprite.setOriginCenter()
        //        sprite.setOrigin(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2);
        sprite.rotation = angle - 90
        //        Gfx.setColor(Color.CLEAR);
        Gfx.drawSprite(sprite)
    }

    override fun renderShapes() {}

    override fun dispose() {
        sprite.texture.dispose()
    }
}