package app.dietrying.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import killerspacewreck.Core;

public class DesktopLauncher {
	public static void main(String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 800;
		config.height = 600;
		config.resizable = false;
		// samples for Multi Sampiling Anti Aliasing
		config.samples = 1;
		new LwjglApplication(new Core(), config);
	}
}
