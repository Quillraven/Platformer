package com.quillraven.platformer.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.quillraven.platformer.Platformer;

class DesktopLauncher {
    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = Platformer.TITLE;
        config.width = 1024;
        config.height = 1024 / (Platformer.V_WIDTH / Platformer.V_HEIGHT);
        new LwjglApplication(new Platformer(), config);
    }
}
