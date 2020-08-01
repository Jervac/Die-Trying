package app.dietrying.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.FileTextureData;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

import java.util.ArrayList;

public class Gfx {

    public static ShapeRenderer sr = new ShapeRenderer();
    public static ShapeRenderer uisr = new ShapeRenderer();

    public static SpriteBatch sb = new SpriteBatch();
    public static SpriteBatch uisb = new SpriteBatch();

    public static OrthographicCamera cam = new OrthographicCamera();

    public static ArrayList<Texture> textures = new ArrayList<Texture>();
    public static ArrayList<Sprite> sprites = new ArrayList<Sprite>();

    public static BitmapFont font = new BitmapFont();

    public static Color clearColor = Color.DARK_GRAY;

    public static void initOrtho(float w, float h, boolean flipped) {
        cam = new OrthographicCamera(w, h);
        cam.setToOrtho(flipped);
    }

    public static void initFont(int size, String file) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
                Gdx.files.internal(file));
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();

        parameter.size = size;
        font = generator.generateFont(parameter);
        generator.dispose();
    }

    public static void loadImage(String s) {
        textures.add(new Texture(Gdx.files.internal(s)));
    }

    public static void loadSprite(String s) {
        sprites.add(new Sprite(new Texture(Gdx.files.internal(s))));
    }

    public static void begin_sprite_batch() {
        sb.enableBlending();
        sb.begin();
    }

    public static void end_sprite_batch() {
        sb.end();
    }

    public static void begin_sprite_batch_ui() {
        uisb.enableBlending();
        uisb.begin();
    }

    public static void end_sprite_batch_ui() {
        uisb.end();
    }

    public static void begin_shape_batch(ShapeType st) {
        sr.begin(st);
    }

    public static void end_shape_batch() {
        sr.end();
    }

    public static void begin_shape_batch_ui(ShapeType st) {
        uisr.begin(st);
    }

    public static void end_shape_batch_ui() {
        uisr.end();
    }

    public static void update() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);
        cam.update();
        Inputer.mousePos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        cam.unproject(Inputer.mousePos);
    }

    public static void setClearColor(Color c) {
        clearColor = c;
    }

    public static void setColor(Color c) {
        sr.setColor(c);
    }

    public static void setColorUI(Color c) {
        uisr.setColor(c);
    }

    public static void fillCircle(float x, float y, float r) {
        sr.setProjectionMatrix(cam.combined);
        sr.end();
        sr.begin(ShapeType.Filled);
        sr.circle(x, y, r);
    }

    public static void drawCircle(float x, float y, float r) {
        sr.setProjectionMatrix(cam.combined);
        sr.end();
        sr.begin(ShapeType.Line);
        sr.circle(x, y, r);
    }

    public static void fillRect(float x, float y, float w, float h) {
        sr.setProjectionMatrix(cam.combined);
        sr.end();
        sr.begin(ShapeType.Filled);
        sr.rect(x, y, w, h);
    }

    public static void fillRectUI(float x, float y, float w, float h) {
        uisr.end();
        uisr.begin(ShapeType.Filled);
        uisr.rect(x, y, w, h);
    }

    public static void drawRect(float x, float y, float w, float h) {
        sr.setProjectionMatrix(cam.combined);
        sr.end();
        sr.begin(ShapeType.Line);
        sr.rect(x, y, w, h);
    }

    public static void drawRectUI(float x, float y, float w, float h) {
        uisr.end();
        uisr.begin(ShapeType.Line);
        uisr.rect(x, y, w, h);
    }

    public static void drawSprite(Sprite s, float x, float y, float w, float h) {
        sb.setProjectionMatrix(cam.combined);
        s.setPosition(x, y);
        s.setSize(w, h);
        s.draw(sb);

    }

    public static void drawSprite(Sprite s, float x, float y) {
        sb.setProjectionMatrix(cam.combined);
        s.setPosition(x, y);
        s.draw(sb);
    }

    public static void drawSprite(Sprite s) {
        sb.setProjectionMatrix(cam.combined);
        s.draw(sb);
    }

    public static void drawSpriteUI(Sprite s, float x, float y) {
        uisb.setProjectionMatrix(cam.combined);
        s.setPosition(x, y);
        s.draw(uisb);
    }

    public static void drawSpriteUI(Sprite s) {
        uisb.setProjectionMatrix(cam.combined);
        s.draw(uisb);
    }

    public static void drawImage(String texture, float x, float y, float w, float h) {
        sb.setProjectionMatrix(cam.combined);
        boolean hasTexture = false;
        for (Texture t : textures) {
            String path = ((FileTextureData) t.getTextureData())
                    .getFileHandle().path();
            if (path == texture) {
                t.setFilter(Texture.TextureFilter.MipMapLinearNearest,
                        Texture.TextureFilter.MipMapLinearNearest);
                hasTexture = true;
                sb.draw(t, x, y, w, h);
            }
        }

        if (!hasTexture) {
            Logger.textureNotFound(texture);
        }
    }

    public static void drawImage(String texture, float x, float y, float w, float h, Color c) {
        sb.setProjectionMatrix(cam.combined);
        boolean hasTexture = false;
        for (Texture t : textures) {
            String path = ((FileTextureData) t.getTextureData())
                    .getFileHandle().path();
            if (path == texture) {
                Color pre = sb.getColor();
                sb.setColor(c);
                hasTexture = true;
                sb.draw(t, x, y, w, h);
                sb.setColor(pre);
            }
        }

        if (!hasTexture) {
            Logger.textureNotFound(texture);
        }
    }

    public static void drawImageUI(String texture, float x, float y, float w, float h) {
        boolean hasTexture = false;
        for (Texture t : textures) {
            String path = ((FileTextureData) t.getTextureData())
                    .getFileHandle().path();
            if (path == texture) {
                hasTexture = true;
                uisb.draw(t, x, y, w, h);
            }
        }

        if (!hasTexture) {
            Logger.textureNotFound(texture);
        }
    }

    public static void drawImageUI(String texture, float x, float y, float w, float h, Color c) {
        boolean hasTexture = false;
        for (Texture t : textures) {
            String path = ((FileTextureData) t.getTextureData())
                    .getFileHandle().path();
            if (path == texture) {
                Color pre = uisb.getColor();
                uisb.setColor(c);
                hasTexture = true;
                uisb.draw(t, x, y, w, h);
                uisb.setColor(pre);
            }
        }

        if (!hasTexture) {
            Logger.textureNotFound(texture);
        }
    }

    public static Texture getImage(String texture) {
        for (Texture t : textures) {
            String path = ((FileTextureData) t.getTextureData())
                    .getFileHandle().path();
            if (path == texture) {
                return t;
            }
        }
        Logger.textureNotFound(texture);
        return null;
    }

    public static Sprite getSprite(String texture) {
        for (Sprite s : sprites) {
            String path = ((FileTextureData) s.getTexture().getTextureData())
                    .getFileHandle().path();
            if (path == texture) {
                return s;
            }
        }
        Logger.textureNotFound(texture);
        return null;
    }

    public static void drawLine(float x, float y, float x1, float y1) {
        sr.line(x, y, x1, y1);
    }

    public static void drawText(String s, float x, float y) {
        font.draw(sb, s, x, y);
    }

//    // center ui text open x with given y
//    public static void allignTextX(String s, float y) {
//        GlyphLayout gl = new GlyphLayout();
//        gl.setText(Gfx.font, s);
//
//        uisb.begin();
//        font.draw(uisb, s, Const.WIDTH / 2 - (gl.width / 2), y);
//        uisb.end();
//    }
//
//    // center ui text open y with given x
//    public static void allignTextY(String s, float x) {
//        GlyphLayout gl = new GlyphLayout();
//        gl.setText(Gfx.font, s);
//
//        uisb.begin();
//        font.draw(uisb, s, x, Const.HEIGHT / 2 - (gl.height / 2));
//        uisb.end();
//    }

    public static void drawText(String s, float x, float y, Color c) {
        font.setColor(c);
        font.draw(sb, s, x, y);
        font.setColor(Color.WHITE);
    }

    public static void drawTextUI(String s, float x, float y) {
        font.draw(uisb, s, x, y);
    }

    public static void drawTextUI(String s, float x, float y, Color c) {
        font.setColor(c);
        font.draw(uisb, s, x, y);
        font.setColor(Color.WHITE);
    }

    public static void dispose() {
        font.dispose();
        uisb.dispose();
        uisr.dispose();
        sb.dispose();
        sr.dispose();
        for (Texture t : textures)
            t.dispose();
        textures.clear();
        for (Sprite s : sprites)
            s.getTexture().dispose();
        sprites.clear();
    }

    // TODO: Make this work
    // converts a path to work with windows file system
    private void converToWindowsFileFormat(String path) {

    }

}
