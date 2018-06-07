package com.quillraven.platformer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.quillraven.platformer.gamestate.GameStateManager;

public class Platformer extends ApplicationAdapter {
    public static final String TITLE = "Platformer";
    public static final int V_WIDTH = 320;
    public static final int V_HEIGHT = 240;

    public static final float PPM = 100f;

    public static final short BIT_GROUND = 1 << 1;
    public static final short BIT_PLAYER = 1 << 2;

    private static final float FIXED_TIME_STEP = 1 / 60f;
    private float accumulator;

    private SpriteBatch spriteBatch;

    private GameStateManager gsManager;

    @Override
    public void create() {
        Gdx.app.setLogLevel(Gdx.app.LOG_DEBUG);

        spriteBatch = new SpriteBatch();
        gsManager = new GameStateManager(this, GameStateManager.GameStateType.GAME);
    }

    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }

    @Override
    public void resize(final int width, final int height) {
        gsManager.resize(width, height);
    }

    @Override
    public void render() {
        accumulator += Math.min(0.25f, Gdx.graphics.getRawDeltaTime());
        while (accumulator >= FIXED_TIME_STEP) {
            if (!gsManager.update(FIXED_TIME_STEP)) {
                Gdx.app.exit();
                return;
            }
            accumulator -= FIXED_TIME_STEP;
        }
        gsManager.render(accumulator / FIXED_TIME_STEP);
    }

    @Override
    public void dispose() {
        spriteBatch.dispose();
        gsManager.dispose();
    }
}
