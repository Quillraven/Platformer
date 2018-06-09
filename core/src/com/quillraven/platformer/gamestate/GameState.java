package com.quillraven.platformer.gamestate;
/*
 * Created by Quillraven on 04.06.2018.
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

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.quillraven.platformer.GameInputListener;

/**
 * TODO add class description
 */

abstract public class GameState {
    private final Viewport viewport;
    final OrthographicCamera camera;
    final AssetManager assetManager;

    GameState(final AssetManager assetManager) {
        this.viewport = getViewport();
        this.camera = (OrthographicCamera) viewport.getCamera();
        this.assetManager = assetManager;
    }

    abstract Viewport getViewport();

    abstract public void onActivation();

    abstract public void onDeactivation();

    abstract public boolean onKeyPressed(final GameStateManager gsManager, final GameInputListener inputListener, final GameInputListener.GameKeys key);

    abstract public boolean onKeyReleased(final GameStateManager gsManager, final GameInputListener inputListener, final GameInputListener.GameKeys key);

    abstract public void onUpdate(final GameStateManager gsManager, final float fixedTimeStep);

    abstract public void onRender(final SpriteBatch spriteBatch, final float alpha);

    public void onResize(final int width, final int height) {
        viewport.update(width, height, true);
    }

    abstract public void onDispose();
}
