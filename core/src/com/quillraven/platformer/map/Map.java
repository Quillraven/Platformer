package com.quillraven.platformer.map;
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

import com.badlogic.gdx.maps.MapProperties;

import static com.quillraven.platformer.Platformer.PPM;

/**
 * TODO add class description
 */
public class Map {
    private static final String TAG = Map.class.getSimpleName();

    private final MapManager.MapType mapType;
    private final float width;
    private final float height;
    private final int[] bgdLayerIdx;
    private final int[] fgdLayerIdx;

    Map(final MapManager.MapType mapType, final MapProperties mapProperties, final int gdLayerIdx, final int bgdLayerIdx, final int fgdLayerIdx) {
        this.mapType = mapType;
        this.width = mapProperties.get("width", Integer.class) * mapProperties.get("tilewidth", Integer.class) / PPM;
        this.height = mapProperties.get("height", Integer.class) * mapProperties.get("tileheight", Integer.class) / PPM;
        this.bgdLayerIdx = new int[]{gdLayerIdx, bgdLayerIdx};
        this.fgdLayerIdx = new int[]{fgdLayerIdx};
    }

    private MapManager.MapType getMapType() {
        return mapType;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public int[] getBackgroundLayerIndex() {
        return bgdLayerIdx;
    }

    public int[] getForegroundLayerIndex() {
        return fgdLayerIdx;
    }
}
