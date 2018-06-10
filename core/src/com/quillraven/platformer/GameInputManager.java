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
import com.badlogic.gdx.utils.Array;

/**
 * TODO add class description
 */
public class GameInputManager extends InputAdapter {
    private static final GameInputManager instance = new GameInputManager();

    private final Array<GameKeyListener> gameKeyListeners;
    private final GameKeys[] keyMapping;
    private final boolean keyState[];

    private GameInputManager() {
        this.gameKeyListeners = new Array<>();
        this.keyMapping = new GameKeys[256];
        for (GameKeys key : GameKeys.values()) {
            keyMapping[key.keyCode] = key;
        }
        this.keyState = new boolean[GameKeys.values().length];
    }

    public static GameInputManager getInstance() {
        return instance;
    }

    public void addGameKeyListener(final GameKeyListener listener) {
        this.gameKeyListeners.add(listener);
    }

    public void removeGameKeyListener(final GameKeyListener listener) {
        this.gameKeyListeners.removeValue(listener, true);
    }

    @Override
    public boolean keyDown(final int keycode) {
        final GameKeys gKey = keyMapping[keycode];
        if (gKey == null) {
            // no relevant key for game
            return false;
        }

        keyState[gKey.ordinal()] = true;
        boolean result = true;
        for (final GameKeyListener listener : gameKeyListeners) {
            result &= listener.onKeyPressed(gKey);
        }
        return result;
    }

    @Override
    public boolean keyUp(final int keycode) {
        final GameKeys gKey = keyMapping[keycode];
        if (gKey == null) {
            // no relevant key for game
            return false;
        }

        keyState[gKey.ordinal()] = false;
        boolean result = true;
        for (final GameKeyListener listener : gameKeyListeners) {
            result &= listener.onKeyReleased(gKey);
        }
        return result;
    }

    public boolean isKeyPressed(final GameKeys key) {
        return keyState[key.ordinal()];
    }

    public enum GameKeys {
        JUMP(Input.Keys.SPACE),
        RIGHT(Input.Keys.D),
        LEFT(Input.Keys.A),
        EXIT(Input.Keys.ESCAPE);

        private final int keyCode;

        GameKeys(final int keyCode) {
            this.keyCode = keyCode;
        }
    }

    public interface GameKeyListener {
        boolean onKeyPressed(final GameKeys key);

        boolean onKeyReleased(final GameKeys key);
    }
}
