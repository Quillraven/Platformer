package com.quillraven.platformer.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

/*
 * Created by Quillraven on 12.02.2018.
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

/**
 * Custom skin class to avoid the "Pixmap already disposed" exception when disposing the skin.
 * The reason is that the bitmap fonts that are created on runtime are not handled correctly within
 * the {@link com.badlogic.gdx.assets.AssetManager} and get therefore disposed twice.
 */
public class Skin extends com.badlogic.gdx.scenes.scene2d.ui.Skin {
    Skin(final TextureAtlas atlas) {
        super(atlas);
    }

    @Override
    public void dispose() {
        for (String bitmapFontKey : this.getAll(BitmapFont.class).keys()) {
            remove(bitmapFontKey, BitmapFont.class);
        }
        super.dispose();
    }
}
