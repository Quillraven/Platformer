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
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.quillraven.platformer.GameInputManager;

/**
 * TODO add class description
 */
public abstract class HUD implements GameInputManager.GameKeyListener {
    final Skin skin;
    final Table table;
    private final Viewport hudViewport;
    private final Stage stage;
    private final I18NBundle i18nBundle;

    // on screen UI
    private final GamePad gamePad;
    private final Button btnBack;
    private final Button btnSelect;

    HUD(final Skin skin, final SpriteBatch spriteBatch, final Viewport hudViewport, final I18NBundle i18nBundle) {
        this.skin = skin;
        this.hudViewport = hudViewport;
        this.stage = new Stage(hudViewport, spriteBatch);
        this.table = new Table();
        this.table.setFillParent(true);
        this.stage.addActor(table);
        this.i18nBundle = i18nBundle;

        // on screen UI
        final Table onScreenUITable = new Table();
        onScreenUITable.setFillParent(true);
        stage.addActor(onScreenUITable);

        btnBack = new Button(skin, "back");
        onScreenUITable.add(btnBack).expand().left().top().padTop(5).padLeft(5).row();

        gamePad = new GamePad(skin);
        onScreenUITable.add(gamePad).left().bottom().padLeft(5).padBottom(5);

        btnSelect = new Button(skin, "select");
        onScreenUITable.add(btnSelect).expandX().bottom().right().padBottom(5).padRight(5);

        GameInputManager.getInstance().addGameKeyListener(this);
    }

    public Stage getStage() {
        return stage;
    }

    String getString(final String key) {
        return i18nBundle.format(key);
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

    @Override
    public boolean onKeyPressed(final GameInputManager.GameKeys key) {
        switch (key) {
            case UP:
                gamePad.setChecked(GamePad.GamePadDirection.UP, true);
                break;
            case LEFT:
                gamePad.setChecked(GamePad.GamePadDirection.LEFT, true);
                break;
            case RIGHT:
                gamePad.setChecked(GamePad.GamePadDirection.RIGHT, true);
                break;
            case DOWN:
                gamePad.setChecked(GamePad.GamePadDirection.DOWN, true);
                break;
            case JUMP:
                btnSelect.setChecked(true);
                break;
            case EXIT:
                btnBack.setChecked(true);
                break;
        }
        return false;
    }

    @Override
    public boolean onKeyReleased(final GameInputManager.GameKeys key) {
        switch (key) {
            case UP:
                gamePad.setChecked(GamePad.GamePadDirection.UP, false);
                break;
            case LEFT:
                gamePad.setChecked(GamePad.GamePadDirection.LEFT, false);
                break;
            case RIGHT:
                gamePad.setChecked(GamePad.GamePadDirection.RIGHT, false);
                break;
            case DOWN:
                gamePad.setChecked(GamePad.GamePadDirection.DOWN, false);
                break;
            case JUMP:
                btnSelect.setChecked(false);
                break;
            case EXIT:
                btnBack.setChecked(false);
                break;
        }
        return false;
    }

    public void onDispose() {
        stage.dispose();
    }
}
