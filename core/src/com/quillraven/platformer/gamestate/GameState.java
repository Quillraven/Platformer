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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.quillraven.platformer.GameInputManager;
import com.quillraven.platformer.ui.HUD;

/**
 * TODO add class description
 */

abstract class GameState<T extends HUD> {
    final AssetManager assetManager;
    final T hud;
    final InputMultiplexer inputMultiplexer;

    GameState(final AssetManager assetManager, final T hud, final SpriteBatch spriteBatch) {
        this.hud = hud;
        this.assetManager = assetManager;
        this.inputMultiplexer = new InputMultiplexer(hud.getStage(), GameInputManager.getInstance());
    }

    public void onActivation() {
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    abstract public void onDeactivation();

    public void onUpdate(final GameStateManager gsManager, final float fixedTimeStep) {
        hud.onUpdate(fixedTimeStep);
    }

    public void onRender(final SpriteBatch spriteBatch, final float alpha) {
        hud.onRender();
    }

    public void onResize(final int width, final int height) {
        hud.onResize(width, height);
    }

    public void onDispose() {
        hud.onDispose();
    }
}
