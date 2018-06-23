package com.quillraven.platformer;
/*
 * Created by Quillraven on 05.06.2018.
 *
 * MIT License
 *
 * Copyright (c) 2018 Quillraven
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.quillraven.platformer.gamestate.GameStateManager;

/**
 * This is the entry point of the libgdx application. We are not using {@link com.badlogic.gdx.Game} and {@link com.badlogic.gdx.Screen}
 * but instead we are using our own {@link GameStateManager} to be more flexible and to separate update and render calls.
 * <br>
 * We are also using the {@link ApplicationAdapter} because we do not care about {@link ApplicationAdapter#pause()} and
 * {@link ApplicationAdapter#resume()}.
 * <br>
 * This implementation is using a fixed timestep gameloop (refer to {@link #FIXED_TIME_STEP}.
 */
public class Platformer extends ApplicationAdapter {
    public static final String TITLE = "Platformer";
    public static final int V_WIDTH = 700;
    public static final int V_HEIGHT = 350;

    public static final float PPM = 70f;

    public static final short BIT_GROUND = 1 << 1;
    public static final short BIT_PLAYER = 1 << 2;
    public static final short BIT_OBJECT = 1 << 3;

    private static final float FIXED_TIME_STEP = 1 / 60f;
    private float accumulator;

    private GameStateManager gsManager;

    @Override
    public void create() {
        Gdx.app.setLogLevel(Gdx.app.LOG_DEBUG);

        this.accumulator = 0;
        gsManager = new GameStateManager(GameStateManager.GameStateType.MENU);
    }

    @Override
    public void resize(final int width, final int height) {
        gsManager.resize(width, height);
    }

    @Override
    public void render() {
        // use get raw delta time to get the real time between frames (getDeltaTime is smoothing)
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
        gsManager.dispose();
    }

}
