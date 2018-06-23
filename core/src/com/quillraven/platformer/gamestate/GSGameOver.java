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

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.quillraven.platformer.GameInputManager;
import com.quillraven.platformer.SoundManager;
import com.quillraven.platformer.ui.GameOverHUD;

/**
 * TODO add class description
 */
public class GSGameOver extends GameState<GameOverHUD> implements GameInputManager.GameKeyListener {
    private boolean goToMenu;

    public GSGameOver(final AssetManager assetManager, final GameOverHUD hud, final SpriteBatch spriteBatch) {
        super(assetManager, hud, spriteBatch);
        this.goToMenu = false;
    }

    @Override
    public void onActivation() {
        goToMenu = false;
        if (!assetManager.isLoaded("hud/gameover.png", Texture.class)) {
            SoundManager.getInstance().loadSounds(assetManager);
            assetManager.load("hud/gameover.png", Texture.class);
        } else {
            SoundManager.getInstance().playSound(SoundManager.SoundType.GAME_OVER);
            GameInputManager.getInstance().addGameKeyListener(this);
            hud.setBackground(assetManager.get("hud/gameover.png", Texture.class));
        }
    }

    @Override
    public void onDeactivation() {
        GameInputManager.getInstance().removeGameKeyListener(this);
    }

    @Override
    public void onUpdate(final GameStateManager gsManager, final float fixedTimeStep) {
        if (goToMenu) {
            goToMenu = false;
            gsManager.setState(GameStateManager.GameStateType.MENU);
            return;
        }
        super.onUpdate(gsManager, fixedTimeStep);
    }

    @Override
    public boolean onKeyPressed(final GameInputManager.GameKeys key) {
        goToMenu = true;
        return false;
    }

    @Override
    public boolean onKeyReleased(final GameInputManager.GameKeys key) {
        return false;
    }
}
