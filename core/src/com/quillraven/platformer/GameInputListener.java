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

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.quillraven.platformer.gamestate.GameStateManager;

/**
 * TODO add class description
 */
public class GameInputListener extends InputAdapter {
    public enum InputKeys {
        JUMP,
        RIGHT,
        LEFT,
        EXIT
    }

    private final GameStateManager gsManager;

    public GameInputListener(final GameStateManager gsManager) {
        this.gsManager = gsManager;
    }

    @Override
    public boolean keyDown(final int keycode) {
        switch (keycode) {
            case Input.Keys.A:
                return gsManager.onKeyPressed(InputKeys.LEFT);
            case Input.Keys.D:
                return gsManager.onKeyPressed(InputKeys.RIGHT);
            case Input.Keys.SPACE:
                return gsManager.onKeyPressed(InputKeys.JUMP);
            case Input.Keys.ESCAPE:
                return gsManager.onKeyPressed(InputKeys.EXIT);
        }
        return false;
    }

    @Override
    public boolean keyUp(final int keycode) {
        switch (keycode) {
            case Input.Keys.A:
                return gsManager.onKeyReleased(InputKeys.LEFT);
            case Input.Keys.D:
                return gsManager.onKeyReleased(InputKeys.RIGHT);
            case Input.Keys.SPACE:
                return gsManager.onKeyReleased(InputKeys.JUMP);
            case Input.Keys.ESCAPE:
                return gsManager.onKeyReleased(InputKeys.EXIT);
        }
        return false;
    }
}
