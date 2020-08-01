package app.dietrying.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector3;

public class Inputer {

    // mouse position in World. It's unprojected in update()
    public static Vector3 mousePos = new Vector3();

    public static boolean active = true;

    public static boolean pressedKey(int key) {
        if (active)
            return Gdx.input.isKeyPressed(key);
        else
            return false;
    }

    public static boolean tappedKey(int key) {
        if (active)
            return Gdx.input.isKeyJustPressed(key);
        else
            return false;
    }

    public static Polygon mouseBoundsPolygon() {
        return new Polygon(new float[]{
                mousePos.x - 2, mousePos.y - 2, mousePos.x + 2, mousePos.y - 2,
                mousePos.x + 2, mousePos.y + 2, mousePos.x - 2, mousePos.y + 2
        });
    }

}