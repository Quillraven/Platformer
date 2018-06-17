package com.quillraven.platformer.ui;
/*
 * Created by Quillraven on 09.06.2018.
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

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * TODO add class description
 */
public abstract class HUD {
    final Skin skin;
    final Table table;
    final Viewport hudViewport;
    final Stage stage;

    HUD(final Skin skin, final SpriteBatch spriteBatch, final Viewport hudViewport) {
        this.skin = skin;
        this.hudViewport = hudViewport;
        this.stage = new Stage(hudViewport, spriteBatch);
        this.table = new Table();
        this.table.setFillParent(true);
        this.stage.addActor(table);
    }

    public void onUpdate(final float fixedPhysicsSteps) {
        stage.act(fixedPhysicsSteps);
    }

    public void onRender() {
        hudViewport.apply();
        stage.draw();
    }

    public void onResize(final int width, final int height) {
        hudViewport.update(width, height, true);
    }

    public void onDispose() {
        stage.dispose();
    }
}
