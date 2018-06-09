package com.quillraven.platformer.gamestate;
/*
 * Created by Quillraven on 08.06.2018.
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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.quillraven.platformer.GameInputListener;
import com.quillraven.platformer.Platformer;

/**
 * TODO add class description
 */
public class GSLoading extends GameState {
    private final static String TAG = GameState.class.getSimpleName();

    private long timeStartLoading;
    private Texture texture;

    public GSLoading(final AssetManager assetManager) {
        super(assetManager);

        texture = null;
    }

    @Override
    Viewport getViewport() {
        return new FitViewport(Platformer.V_WIDTH, Platformer.V_HEIGHT);
    }

    @Override
    public void onActivation() {
        timeStartLoading = TimeUtils.millis();
        Gdx.app.debug(TAG, "Loading assets");

        if (texture == null) {
            assetManager.load("hud/switchMid.png", Texture.class);
        }
    }

    @Override
    public void onDeactivation() {

    }

    @Override
    public boolean onKeyPressed(final GameStateManager gsManager, final GameInputListener inputListener, final GameInputListener.GameKeys key) {
        return false;
    }

    @Override
    public boolean onKeyReleased(final GameStateManager gsManager, final GameInputListener inputListener, final GameInputListener.GameKeys key) {
        return false;
    }

    @Override
    public void onUpdate(final GameStateManager gsManager, final float fixedTimeStep) {
        if (assetManager.update()) {
            Gdx.app.debug(TAG, "Finished loading assets in " + TimeUtils.timeSinceMillis(timeStartLoading) + " milliseconds");
            gsManager.popState();
        }

        if (texture == null && assetManager.isLoaded("hud/switchMid.png")) {
            texture = assetManager.get("hud/switchMid.png");
        }
    }

    @Override
    public void onRender(final SpriteBatch spriteBatch, final float alpha) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (texture != null) {
            spriteBatch.setProjectionMatrix(camera.combined);
            spriteBatch.begin();
            spriteBatch.draw(texture, 10, 10, 100, 100);
            spriteBatch.end();
        }
    }

    @Override
    public void onDispose() {

    }
}
