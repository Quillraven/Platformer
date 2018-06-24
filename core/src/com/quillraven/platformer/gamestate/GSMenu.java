package com.quillraven.platformer.gamestate;
/*
 * Created by Quillraven on 23.06.2018.
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
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.quillraven.platformer.GameInputManager;
import com.quillraven.platformer.PreferencesManager;
import com.quillraven.platformer.SoundManager;
import com.quillraven.platformer.ui.MenuHUD;

/**
 * TODO add class description
 */
class GSMenu extends GameState<MenuHUD> implements GameInputManager.GameKeyListener {
    private int currentSelection;
    private boolean startGame;
    private boolean quitGame;
    private boolean showCredits;
    private boolean continueGame;

    public GSMenu(final AssetManager assetManager, final MenuHUD hud, final SpriteBatch spriteBatch) {
        super(assetManager, hud, spriteBatch);

        currentSelection = 0;
        startGame = false;
        quitGame = false;
        showCredits = false;
    }

    @Override
    public void onActivation() {
        super.onActivation();

        if (!assetManager.isLoaded("hud/menu.png", Texture.class)) {
            assetManager.load("hud/menu.png", Texture.class);
        } else {
            hud.setBackground(assetManager.get("hud/menu.png", Texture.class));
        }

        if (SoundManager.getInstance().loadSounds(assetManager)) {
            SoundManager.getInstance().playSound(SoundManager.SoundType.MENU);
            GameInputManager.getInstance().addGameKeyListener(this);

            if (PreferencesManager.getInstance().getStringValue("level").isEmpty()) {
                hud.disableContinueMenuItem();
            }
        }
    }

    @Override
    public void onDeactivation() {
        GameInputManager.getInstance().removeGameKeyListener(this);
    }

    @Override
    public void onUpdate(final GameStateManager gsManager, final float fixedTimeStep) {
        super.onUpdate(gsManager, fixedTimeStep);
        if (startGame) {
            PreferencesManager.getInstance().removeValue("level").removeValue("playerX").removeValue("playerY");
            startGame = false;
            hud.enableContinueMenuItem();
            gsManager.setState(GameStateManager.GameStateType.GAME);
        } else if (quitGame) {
            Gdx.app.exit();
        } else if (continueGame) {
            continueGame = false;
            gsManager.setState(GameStateManager.GameStateType.GAME);
        }
    }

    @Override
    public boolean onKeyPressed(final GameInputManager.GameKeys key) {
        if (showCredits) {
            hud.hideCredits();
            showCredits = false;
        }

        if (key == GameInputManager.GameKeys.UP) {
            SoundManager.getInstance().playSound(SoundManager.SoundType.SFX_SELECT);
            hud.selectPreviousMenuItem();
            return true;
        } else if (key == GameInputManager.GameKeys.DOWN) {
            SoundManager.getInstance().playSound(SoundManager.SoundType.SFX_SELECT);
            hud.selectNextMenuItem();
            return true;
        } else if (key == GameInputManager.GameKeys.JUMP) {
            if ("menuItem.newGame".equals(hud.getCurrentSelection())) {
                startGame = true;
            } else if ("menuItem.quitGame".equals(hud.getCurrentSelection())) {
                quitGame = true;
            } else if ("menuItem.credits".equals(hud.getCurrentSelection())) {
                showCredits = true;
                hud.showCredits();
            } else if ("menuItem.continue".equals(hud.getCurrentSelection())) {
                continueGame = true;
            }
            SoundManager.getInstance().playSound(SoundManager.SoundType.SFX_SELECT);
            return true;
        }
        return false;
    }

    @Override
    public boolean onKeyReleased(final GameInputManager.GameKeys key) {
        return false;
    }
}
