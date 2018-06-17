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
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.StringBuilder;
import com.badlogic.gdx.utils.viewport.Viewport;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

/**
 * TODO add class description
 */
public class GameHUD extends HUD {
    private final TextButton coinInfo;
    private final StringBuilder coinStrBuilder;

    private final TextButton lifeInfo;
    private final StringBuilder lifeStrBuilder;

    private final TextButton levelInfo;

    private final TextButton infoBox;

    public GameHUD(final Skin skin, final SpriteBatch spriteBatch, final Viewport hudViewport) {
        super(skin, spriteBatch, hudViewport);

        lifeStrBuilder = new StringBuilder(10);
        lifeInfo = new TextButton("", skin.get("white-label", TextButton.TextButtonStyle.class));
        table.add(lifeInfo).expandX().padBottom(10);

        levelInfo = new TextButton("", skin.get("white-label", TextButton.TextButtonStyle.class));
        table.add(levelInfo).expandX().padBottom(10);

        coinStrBuilder = new StringBuilder(14);
        coinInfo = new TextButton("", skin.get("white-label", TextButton.TextButtonStyle.class));
        table.add(coinInfo).expandX().padBottom(10);

        table.bottom();

        infoBox = new TextButton("", skin.get("white-label", TextButton.TextButtonStyle.class));
        stage.addActor(infoBox);
    }

    public void updateCoinInfo(final int numCoins, final int maxCoins) {
        coinStrBuilder.setLength(0);
        coinStrBuilder.append("Coins: ").append(numCoins).append("/").append(maxCoins);
        coinInfo.getLabel().setText(coinStrBuilder);
    }

    public void updateLifeInfo(final int currentLife, final int maxLife) {
        lifeStrBuilder.setLength(0);
        lifeStrBuilder.append("Life: ").append(currentLife).append("/").append(maxLife);
        lifeInfo.getLabel().setText(lifeStrBuilder);
    }

    public void setLevelName(final String name) {
        levelInfo.setText(name);
    }

    public void showInfoMessage(final float x, final float y, final String infoBoxID) {
        if ("InfoBox1".equals(infoBoxID)) {
            infoBox.setText("Press some buttons to move and jump!");
        } else if ("InfoBox2".equals(infoBoxID)) {
            infoBox.setText("The pole indicates if you have collected all coins of a level :)");
        }

        infoBox.setPosition(stage.getWidth() / 2, Math.max(stage.getHeight() / 2 + 120, stage.getHeight() - 200));
        infoBox.addAction(sequence(Actions.alpha(0), Actions.fadeIn(0.4f, Interpolation.fade), Actions.delay(4f, Actions.fadeOut(0.4f))));
    }
}
