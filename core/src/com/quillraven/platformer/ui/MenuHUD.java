package com.quillraven.platformer.ui;
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

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * TODO add class description
 */
public class MenuHUD extends HUD {
    private int currentSelection;
    private TextButton continueItem;
    private Array<TextButton> menuItems;
    private TextButton credits;

    public MenuHUD(final Skin skin, final SpriteBatch spriteBatch, final Viewport hudViewport, final I18NBundle i18nBundle) {
        super(skin, spriteBatch, hudViewport, i18nBundle);

        currentSelection = 0;
        menuItems = new Array<>();

        // normal menu
        table.center();
        menuItems.add(new TextButton("menuItem.newGame", skin.get("big", TextButton.TextButtonStyle.class)));
        menuItems.add(new TextButton("menuItem.continue", skin.get("big", TextButton.TextButtonStyle.class)));
        menuItems.add(new TextButton("menuItem.credits", skin.get("big", TextButton.TextButtonStyle.class)));
        menuItems.add(new TextButton("menuItem.quitGame", skin.get("big", TextButton.TextButtonStyle.class)));

        for (int i = 0; i < menuItems.size; ++i) {
            final TextButton item = menuItems.get(i);
            item.setUserObject(item.getText().toString());
            item.setText(getString(item.getText().toString()));
            item.getStyle().font.getData().markupEnabled = true;
            if (i < menuItems.size - 1) {
                table.add(item).padBottom(55).row();
            } else {
                table.add(item);
            }
        }
        // disable continue because there is no game started yet
        continueItem = menuItems.get(1);
        continueItem.getLabel().getText().insert(0, "[Disabled]");
        menuItems.removeValue(continueItem, true);
        selectMenuItem(currentSelection);

        // credits
        credits = new TextButton(getString("credits"), skin.get("big", TextButton.TextButtonStyle.class));
    }

    private void selectMenuItem(final int selection) {
        menuItems.get(currentSelection).getLabel().getText().replace("[Highlight]", "");
        menuItems.get(currentSelection).getLabel().invalidateHierarchy();
        currentSelection = Math.max(0, Math.min(menuItems.size - 1, selection));
        menuItems.get(currentSelection).getLabel().getText().insert(0, "[Highlight]");
        menuItems.get(currentSelection).getLabel().invalidateHierarchy();
    }

    public void selectNextMenuItem() {
        selectMenuItem(currentSelection + 1);
    }

    public void selectPreviousMenuItem() {
        selectMenuItem(currentSelection - 1);
    }

    public String getCurrentSelection() {
        return (String) menuItems.get(currentSelection).getUserObject();
    }

    public void showCredits() {
        table.clear();
        table.add(credits);
    }

    public void enableContinueMenuItem() {
        if (!menuItems.get(1).equals(continueItem)) {
            continueItem.getLabel().getText().replace("[Disabled]", "");
            continueItem.getLabel().invalidateHierarchy();
            menuItems.insert(1, continueItem);
        }
    }

    public void hideCredits() {
        table.clear();
        final boolean continueAvailable = menuItems.get(1).equals(continueItem);
        if (!continueAvailable) {
            menuItems.insert(1, continueItem);
        }
        for (int i = 0; i < menuItems.size; ++i) {
            final TextButton item = menuItems.get(i);
            if (i < menuItems.size - 1) {
                table.add(item).padBottom(55).row();
            } else {
                table.add(item);
            }
        }

        if (!continueAvailable) {
            menuItems.removeValue(continueItem, true);
            selectMenuItem(1);
        } else {
            selectMenuItem(2);
        }
    }
}
