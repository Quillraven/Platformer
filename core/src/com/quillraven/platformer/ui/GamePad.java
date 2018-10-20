package com.quillraven.platformer.ui;
/*
 * Created by Quillraven on 24.06.2018.
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

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.quillraven.platformer.GameInputManager;

/**
 * TODO add class description
 */
public class GamePad extends WidgetGroup {
    public GamePad(final Skin skin) {
        super();

        Button btn = new Button(skin, "gamePadUp");
        btn.setUserObject(GamePadDirection.UP);
        btn.setPosition(50, 85);
        addActor(btn);

        btn = new Button(skin, "gamePadLeft");
        btn.setUserObject(GamePadDirection.LEFT);
        btn.setPosition(0, 50);
        addActor(btn);

        btn = new Button(skin, "gamePadRight");
        btn.setUserObject(GamePadDirection.RIGHT);
        btn.setPosition(85, 50);
        addActor(btn);

        btn = new Button(skin, "gamePadDown");
        btn.setUserObject(GamePadDirection.DOWN);
        btn.setPosition(50, 0);
        addActor(btn);
    }

    @Override
    public boolean addListener(final EventListener listener) {
        for (final Actor btn : getChildren()) {
            btn.addListener(listener);
        }
        return true;
    }

    public boolean contains(Actor actor) {
        for (final Actor btn : this.getChildren()) {
            if (btn.equals(actor)) {
                return true;
            }
        }

        return false;
    }

    public void setChecked(final GamePadDirection direction, final boolean checked) {
        ((Button) getChildren().get(direction.ordinal())).setChecked(checked);
    }

    public GameInputManager.GameKeys getRelatedKey(final Actor target) {
        for (final Actor btn : this.getChildren()) {
            if (btn.equals(target)) {
                if (btn.getUserObject().equals(GamePadDirection.UP)) {
                    return GameInputManager.GameKeys.UP;
                } else if (btn.getUserObject().equals(GamePadDirection.LEFT)) {
                    return GameInputManager.GameKeys.LEFT;
                } else if (btn.getUserObject().equals(GamePadDirection.RIGHT)) {
                    return GameInputManager.GameKeys.RIGHT;
                } else if (btn.getUserObject().equals(GamePadDirection.DOWN)) {
                    return GameInputManager.GameKeys.DOWN;
                }
            }
        }
        return null;
    }

    public enum GamePadDirection {
        UP, LEFT, RIGHT, DOWN
    }
}
