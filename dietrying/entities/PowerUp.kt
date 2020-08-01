package killerspacewreck.entities

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Timer
import killerspacewreck.Core
import app.dietrying.util.*
import app.dietrying.entities.*
import app.dietrying.*

class PowerUp : Entity(Vector2(
        MathUtils.random(Const.CENTER.x - Const.SPAWN_DISTANCE / 2,
                Const.CENTER.x + Const.SPAWN_DISTANCE / 2),
        MathUtils.random(Const.CENTER.y - Const.SPAWN_DISTANCE / 2,
                Const.CENTER.y + Const.SPAWN_DISTANCE / 2)), Vector2(170f, 170f)) {

    var up = MathUtils.randomBoolean()
    var right = MathUtils.randomBoolean()

    lateinit var power: Power
    lateinit var gun: Gun

    // 0 = gun | 1 = power
    val ability_type = MathUtils.random(0, 1)

    init {
        hp = 4f

        if (up)
            pos.y = MathUtils.random(Const.CENTER.y + Const.SPAWN_DISTANCE / 4, Const.CENTER.y + Const.SPAWN_DISTANCE / 2)
        if (!up)
            pos.y = MathUtils.random(Const.CENTER.y - Const.SPAWN_DISTANCE / 4, Const.CENTER.y - Const.SPAWN_DISTANCE / 2)

        if (right)
            pos.x = MathUtils.random(Const.CENTER.x + Const.SPAWN_DISTANCE / 4, Const.CENTER.x + Const.SPAWN_DISTANCE / 2)
        if (!right)
            pos.x = MathUtils.random(Const.CENTER.x - Const.SPAWN_DISTANCE / 4, Const.CENTER.x - Const.SPAWN_DISTANCE / 2)

        // spawned on any other powerups? kill it

        for(p in Core.items)
            if(p.bounds().overlaps(bounds()))
                alive = false

        if(alive) {
            Timer.schedule(object : Timer.Task() {
                override fun run() {
                    hp = 0f
                }
            }, 7f)
        }
    }

    fun choose_item() {
        // Gun
        if (ability_type == 0) {

            val choice = MathUtils.random(1, 2)

            if (choice == 1) {
                gun = Gun.BIGSHOT
            }
            if (choice == 2) {
                gun = Gun.DEFAULT
            }

            if (Core.p.gun === this.gun)
                choose_item()
            else {
                Core.p.new_power = true
            }
        } else if (ability_type == 1) {
            val choice = MathUtils.random(1, 2)

            if (choice == 1) {
                power = Power.SECOND_PLAYER
            }
            if (choice == 2) {
                power = Power.SHIELD// TODO: new_power bool prob not nbeeded
            }

//            if (Core.p.power === this.power)// if you want player to not get same item
//                choose_item()
//            else {
            Core.p.new_power = true
//            }
        }// Power
    }

    override fun update() {
        // on disapear
        if (hp <= 0 && alive) {
            size.x -= 4
            size.y -= 4
            // move wile resizing to look centered
            pos.x += 4
            pos.y += 4
            if (size.x <= 5)
                alive = false
        }
    }

    override fun render() {}

    override fun renderShapes() {
        // shadow
        Gfx.setColor(Color.BLACK)
        Gfx.fillRect(pos.x - 15, pos.y - 15, size.x, size.y)
        Gfx.setColor(Color.LIME)
        Gfx.fillRect(pos.x, pos.y, size.x, size.y)
    }

    override fun dispose() {}

}