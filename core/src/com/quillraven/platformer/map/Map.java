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

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.utils.Array;

import static com.quillraven.platformer.Platformer.PPM;

/**
 * TODO add class description
 */
public class Map {
    private static final String TAG = Map.class.getSimpleName();

    private final MapManager.MapType mapType;
    private final String name;
    private final float width;
    private final float height;
    private final Array<Integer> bgdLayerIdx;
    private final Array<Integer> fgdLayerIdx;
    private float startX;
    private float startY;
    private int maxCoins;
    private TiledMapTileMapObject coinFlagObject;

    Map(final MapManager.MapType mapType, final TiledMap tiledMap) {
        this.mapType = mapType;

        this.bgdLayerIdx = new Array<>();
        this.fgdLayerIdx = new Array<>();
        final MapProperties mapProperties = tiledMap.getProperties();
        for (final MapLayer mapLayer : tiledMap.getLayers()) {
            if ("objects".equals(mapLayer.getName()) || "ground".equals(mapLayer.getName()) || mapLayer.getName().startsWith("background")) {
                bgdLayerIdx.add(tiledMap.getLayers().getIndex(mapLayer));
            } else if (mapLayer.getName().startsWith("foreground")) {
                fgdLayerIdx.add(tiledMap.getLayers().getIndex(mapLayer));
            }
        }

        this.width = mapProperties.get("width", Integer.class) * mapProperties.get("tilewidth", Integer.class) / PPM;
        this.height = mapProperties.get("height", Integer.class) * mapProperties.get("tileheight", Integer.class) / PPM;
        this.name = mapProperties.get("name", String.class);
        this.maxCoins = 0;
        this.coinFlagObject = null;

        this.startX = mapProperties.get("startX", Integer.class) * PPM;
        this.startY = mapProperties.get("startY", Integer.class) * PPM;
    }

    public float getStartX() {
        return startX;
    }

    public float getStartY() {
        return startY;
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

    public Integer[] getBackgroundLayerIndex() {
        return bgdLayerIdx.toArray(Integer.class);
    }

    public Integer[] getForegroundLayerIndex() {
        return fgdLayerIdx.toArray(Integer.class);
    }

    public String getName() {
        return name;
    }

    public int getMaxCoins() {
        return maxCoins;
    }

    public void setMaxCoins(final int maxCoins) {
        this.maxCoins = maxCoins;
    }

    public TiledMapTileMapObject getCoinFlagObject() {
        return coinFlagObject;
    }

    public void setCoinFlagObject(final TiledMapTileMapObject mapObj) {
        this.coinFlagObject = mapObj;
    }
}
