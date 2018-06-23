package com.quillraven.platformer;
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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/**
 * TODO add class description
 */
public class PreferencesManager {
    private static final PreferencesManager instance = new PreferencesManager();
    private final Preferences preferences;

    private PreferencesManager() {
        preferences = Gdx.app.getPreferences("platformer");
    }

    public static PreferencesManager getInstance() {
        return instance;
    }

    public void setStringValue(final String key, final String value) {
        preferences.putString(key, value);
        preferences.flush();
    }

    public String getStringValue(final String key) {
        return preferences.getString(key, "");
    }

    public void setFloatValue(final String key, final float value) {
        preferences.putFloat(key, value);
        preferences.flush();
    }

    public float getFloatValue(final String key) {
        return preferences.getFloat(key, 0f);
    }

    public PreferencesManager removeValue(final String key) {
        preferences.remove(key);
        preferences.flush();
        return this;
    }
}
