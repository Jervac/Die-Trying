package killerspacewreck.entities

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import killerspacewreck.Core
import app.dietrying.util.*
import app.dietrying.entities.*
import app.dietrying.*

class Bullet(npos: Vector2, nangle: Float) : Entity(npos, Vector2(Core.p.size.x, Core.p.size.y)) {
    internal var sprite: Sprite
    internal var flash: Sprite
    internal var damage: Float = 0.toFloat()
    internal var gun: Gun

    var flashed = false

    init {
        angle = nangle

        // Spawn a little above player
        pos.x += (Math.cos(Math.toRadians(angle.toDouble())) * vel * 4).toFloat()
        pos.y += (Math.sin(Math.toRadians(angle.toDouble())) * vel * 4).toFloat()

        gun = Core.p.gun
        damage = gun.damage.toFloat()
        vel = gun.velocity
        hp = gun.hp.toFloat()

        sprite = Sprite(Texture(gun.texture))
        sprite.setSize(size.x, size.y)
        sprite.flip(false, true)
        sprite.setOriginCenter()

        flash = Sprite(Texture("shot.png"))
        flash.setSize(size.x, size.y)
        flash.flip(false, true)
        flash.setOriginCenter()
        flash.setColor(Color.YELLOW)

    }

    override fun update() {

        pos.x += (Math.cos(Math.toRadians(angle.toDouble())) * vel).toFloat()
        pos.y += (Math.sin(Math.toRadians(angle.toDouble())) * vel).toFloat()

        for (a in Core.asteroids)
            if (a.alive && a.bounds().overlaps(this.bounds())) {
                a.knockBack(angle)
                a.hp -= damage
                a.gotHit = true
                hp--
                if (MathUtils.random(1, 1) == 1)
                    Core.hit.play()
                break
            }

        for (p in Core.items)
            if (p.alive && p.bounds().overlaps(this.bounds())) {
                p.hp -= damage
                hp--

                // use powerup
                if(p.hp <= 0 && p.alive) {
                    p.alive = false
                    p.choose_item()
                    if (p.ability_type == 0)
                        Core.p.gun = p.gun
                    if (p.ability_type == 1)
                        Core.nextPower = p.power
                    Core.powerup.play()
                }

                break
            }

        // outside of view window
        if (pos.x > Const.CENTER.x + Const.SPAWN_DISTANCE || pos.x < Const.CENTER.x - Const.SPAWN_DISTANCE ||
                pos.y > Const.CENTER.y + Const.SPAWN_DISTANCE || pos.y < Const.CENTER.y - Const.SPAWN_DISTANCE) {
            alive = false
        }

        if (hp <= 0)
            alive = false
    }

    override fun render() {
        //        sprite.setOrigin(Const.CENTER.x - (size.y/2), Const.CENTER.y );
        sprite.rotation = angle + 90
        Gfx.drawSprite(sprite, pos.x, pos.y)

        if (!flashed) {
            flashed = true
            Gfx.drawSprite(flash, pos.x, pos.y)
        }
    }

    override fun renderShapes() {
    }

    override fun dispose() {
        sprite.texture.dispose()
        flash.texture.dispose()
    }
}
