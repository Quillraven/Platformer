package com.quillraven.platformer.ui;
/*
 * Created by Quillraven on 16.06.2018.
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
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;

/**
 * TODO add class description
 */

public class AnimationManager {
    private static final String TAG = AnimationManager.class.getSimpleName();
    private static final AnimationManager instance = new AnimationManager();

    private final Array<Animation<Sprite>> animationCache;


    private AnimationManager() {
        this.animationCache = new Array<>(AnimationType.values().length);
        for (int i = 0; i < AnimationType.values().length; ++i) {
            animationCache.add(null);
        }
    }

    public static AnimationManager getInstance() {
        return instance;
    }

    public Animation<Sprite> getAnimation(final AnimationType aniType) {
        return animationCache.get(aniType.ordinal());
    }

    public void loadAnimation(final AssetManager assetManager, final AnimationType aniType) {
        if (assetManager.isLoaded(aniType.atlasPath, TextureAtlas.class)) {
            Animation<Sprite> animation = animationCache.get(aniType.ordinal());
            if (animation == null) {
                Gdx.app.debug(TAG, "Creating new animation " + aniType);
                final Array<Sprite> keyFrames = new Array<>();
                keyFrames.add(new Sprite(assetManager.get(aniType.atlasPath, TextureAtlas.class).findRegion(aniType.atlasKey)));
                animation = new Animation<>(0.25f, keyFrames);
                animationCache.set(aniType.ordinal(), animation);
            }
        } else {
            Gdx.app.debug(TAG, "Animation " + aniType + " not loaded yet");
            assetManager.load(aniType.atlasPath, TextureAtlas.class);
        }
    }

    public enum AnimationType {
        PLAYER_IDLE("characters/characters.atlas", "playerStand");

        private final String atlasPath;
        private final String atlasKey;

        AnimationType(final String atlasPath, final String atlasKey) {
            this.atlasPath = atlasPath;
            this.atlasKey = atlasKey;
        }
    }
}
