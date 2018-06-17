package com.quillraven.platformer;
/*
 * Created by Quillraven on 17.06.2018.
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
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

/**
 * TODO add class description
 */

public class SoundManager {
    private static final SoundManager instance = new SoundManager();

    private Music currentMusic;
    private AssetManager assetManager;

    private SoundManager() {
        this.currentMusic = null;
    }

    public static SoundManager getInstance() {
        return instance;
    }

    public void loadSounds(final AssetManager assetManager) {
        if (!assetManager.isLoaded(SoundType.STAGE.filePath)) {
            this.assetManager = assetManager;
            for (final SoundType sndType : SoundType.values()) {
                assetManager.load(sndType.filePath, sndType.soundClass);
            }
        }
    }

    public void playSound(final SoundType sndType) {
        if (Music.class.equals(sndType.soundClass)) {
            if (currentMusic != null) {
                currentMusic.stop();
            }

            currentMusic = assetManager.get(sndType.filePath, Music.class);
            currentMusic.setLooping(true);
            currentMusic.play();
        } else {
            assetManager.get(sndType.filePath, Sound.class).play();
        }
    }

    public enum SoundType {
        STAGE("sounds/stage.mp3", Music.class),
        SFX_JUMP("sounds/jump.wav", Sound.class),
        SFX_COIN("sounds/coin.wav", Sound.class),
        SFX_ALL_COINS("sounds/all_coins.wav", Sound.class);

        private final String filePath;
        private final Class<?> soundClass;

        SoundType(final String filePath, final Class<?> soundClass) {
            this.filePath = filePath;
            this.soundClass = soundClass;
        }
    }
}
