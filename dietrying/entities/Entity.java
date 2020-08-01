package app.dietrying.entities;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import app.dietrying.Const;

public abstract class Entity {

    public Vector2 pos = new Vector2();
    public Vector2 size = new Vector2();
    public Vector2 dir = new Vector2(0f, 0f);//TODO: Replace dx dy with this
    public float angle = 0f;
    public boolean alive = true;
    public float hp = 1f;
    public float vel;

    // vector based movement
    public float dx, dy, ass;

    public Entity(Vector2 npos, Vector2 nsize) {
        pos = npos;
        size = nsize;
    }

    public abstract void update();

    public abstract void render();

    public abstract void renderShapes();

    public abstract void dispose();

    public Rectangle bounds() {
        return new Rectangle(pos.x, pos.y, size.x, size.y);
    }

    public void normalize_direction() {
        dx = Const.CENTER.x - pos.x - (size.x / 2);
        dy = Const.CENTER.y - pos.y - (size.y / 2);

        ass = (float) Math.sqrt((dx * dx) + (dy * dy));

        dx /= ass;
        dy /= ass;
    }

}