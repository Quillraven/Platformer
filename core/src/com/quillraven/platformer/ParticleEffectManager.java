package com.quillraven.platformer;
/*
 * Created by Quillraven on 01.07.2018.
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
import com.badlogic.gdx.assets.loaders.ParticleEffectLoader;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.utils.Array;

import static com.quillraven.platformer.Platformer.PPM;

/**
 * TODO add class description
 */
public class ParticleEffectManager {
    private static final ParticleEffectManager instance = new ParticleEffectManager();
    private final Array<ParticleEffectPool.PooledEffect> currentEffects;
    private ParticleEffectPool effectPool;
    private AssetManager assetManager;

    private ParticleEffectManager() {
        this.effectPool = null;
        this.assetManager = null;
        this.currentEffects = new Array<>();
    }

    public static ParticleEffectManager getInstance() {
        return instance;
    }

    public void loadEffects(final AssetManager assetManager) {
        this.assetManager = assetManager;
        if (!assetManager.isLoaded("effects/dust.pe")) {
            ParticleEffectLoader.ParticleEffectParameter peParams = new ParticleEffectLoader.ParticleEffectParameter();
            peParams.atlasFile = "characters/characters.atlas";
            assetManager.load("effects/dust.pe", ParticleEffect.class, peParams);
        }
    }

    public void spawnDustEffect(final float x, final float y) {
        if (effectPool == null) {
            final ParticleEffect dustEffect = assetManager.get("effects/dust.pe", ParticleEffect.class);
            dustEffect.setEmittersCleanUpBlendFunction(false);
            dustEffect.scaleEffect(1f / PPM * 0.75f);
            this.effectPool = new ParticleEffectPool(dustEffect, 1, 2);
        }
        final ParticleEffectPool.PooledEffect effect = effectPool.obtain();
        effect.setPosition(x, y);
        currentEffects.add(effect);
    }

    public Array<ParticleEffectPool.PooledEffect> getEffects() {
        return currentEffects;
    }
}
