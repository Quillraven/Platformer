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
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

/**
 * TODO add class description
 */

public class AnimationManager {
    private static final String TAG = AnimationManager.class.getSimpleName();
    private static final AnimationManager instance = new AnimationManager();

    private final Array<Animation<Sprite>> animationCache;
    private AssetManager assetManager;


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
        Animation<Sprite> animation = animationCache.get(aniType.ordinal());
        if (animation == null) {
            Gdx.app.debug(TAG, "Creating new animation " + aniType);
            final TextureRegion[][] regions = assetManager.get(aniType.atlasPath, TextureAtlas.class).findRegion(aniType.atlasKey).split(aniType.frameWidth, aniType.frameHeight);
            final Array<Sprite> keyFrames = new Array<>();
            for (final TextureRegion[] rowFrames : regions) {
                for (final TextureRegion frame : rowFrames) {
                    keyFrames.add(new Sprite(frame));
                }
            }
            animation = new Animation<>(aniType.frameDuration, keyFrames);
            animationCache.set(aniType.ordinal(), animation);
        }
        return animation;
    }

    public void loadAnimations(final AssetManager assetManager) {
        if (!assetManager.isLoaded(AnimationType.PLAYER_IDLE.atlasPath)) {
            this.assetManager = assetManager;
            for (final AnimationType aniType : AnimationType.values()) {
                assetManager.load(aniType.atlasPath, TextureAtlas.class);
            }
        }
    }

    public enum AnimationType {
        PLAYER_IDLE("characters/characters.atlas", "playerStand", 66, 92, 0f),
        PLAYER_WALK("characters/characters.atlas", "playerWalk", 74, 97, 0.03f),
        PLAYER_JUMP("characters/characters.atlas", "playerJump", 67, 94, 0f);

        private final String atlasPath;
        private final String atlasKey;
        private final int frameWidth;
        private final int frameHeight;
        private final float frameDuration;

        AnimationType(final String atlasPath, final String atlasKey, final int frameWidth, final int frameHeight, final float frameDuration) {
            this.atlasPath = atlasPath;
            this.atlasKey = atlasKey;
            this.frameWidth = frameWidth;
            this.frameHeight = frameHeight;
            this.frameDuration = frameDuration;
        }
    }
}
