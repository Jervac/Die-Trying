package killerspacewreck.entities

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Vector2
import java.util.*
import app.dietrying.util.*
import app.dietrying.entities.*
import app.dietrying.*

class Player(npos: Vector2, nsize: Vector2) : Entity(npos, nsize) {

    var sprite: Sprite
    var rot_speed = 4f
    var height = 190f
    var bullets = ArrayList<Bullet>()
    var SHOT_DELAY = .08f
    var DEFAULT_SHOT_DELAY = .21f
    var GUN_1_SHOT_DELAY = .8f

    var gun = Gun.DEFAULT
    var power: Power? = null

    var raise = 90f  // sprite height from planet

    var new_power = false

    init {

        sprite = Sprite(Texture("badlogic.png"))
        //        sprite.setColor(Color.YELLOW);
        sprite.setSize(size.x, size.y)


        angle = 45f
    }

    override fun update() {
        pos.x = Math.cos(Math.toRadians(angle.toDouble())).toFloat() * (height + raise)
        pos.y = Math.sin(Math.toRadians(angle.toDouble())).toFloat() * (height + raise)

        //        Const.CENTER.set(sprite.getOriginX(), sprite.getOriginY());

        for (b in bullets)
            if (b.alive)
                b.update()

        if (gun === Gun.DEFAULT)
            SHOT_DELAY = DEFAULT_SHOT_DELAY
        if (gun === Gun.BIGSHOT)
            SHOT_DELAY = GUN_1_SHOT_DELAY
    }

    override fun render() {
        if (alive) {
            sprite.setPosition(pos.x, pos.y)
            sprite.setOriginCenter()
            //        sprite.setOrigin(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2);
            sprite.rotation = angle - 90
            //        Gfx.setColor(Color.CLEAR);
            Gfx.drawSprite(sprite)
        }

        for (b in bullets)
            if (b.alive)
                b.render()


    }

    override fun renderShapes() {
        for (b in bullets)
            if (b.alive)
                b.renderShapes()
    }

    override fun dispose() {
        sprite.texture.dispose()
        for (b in bullets)
            b.dispose()
        bullets.clear()
    }

}
