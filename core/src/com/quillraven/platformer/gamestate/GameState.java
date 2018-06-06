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

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.quillraven.platformer.GameInputListener;
import com.quillraven.platformer.Platformer;

/**
 * TODO add class description
 */

abstract public class GameState {
    final Platformer game;
    final SpriteBatch spriteBatch;
    final Viewport viewport;
    final Camera camera;

    GameState(final Platformer game) {
        this.game = game;
        this.spriteBatch = game.getSpriteBatch();
        this.viewport = getViewport();
        this.camera = viewport.getCamera();
    }

    abstract Viewport getViewport();

    abstract public void onActivation();

    abstract public void onDeactivation();

    abstract public boolean onKeyPressed(final GameStateManager gsManager, final GameInputListener.InputKeys key);

    abstract public boolean onKeyReleased(final GameStateManager gsManager, final GameInputListener.InputKeys key);

    abstract public void onUpdate(final GameStateManager gsManager, final float fixedTimeStep);

    abstract public void onRender(final float alpha);

    public void onResize(final int width, final int height) {
        viewport.update(width, height, true);
    }

    abstract public void onDispose();
}
