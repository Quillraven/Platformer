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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * TODO add class description
 */
public class MapManager {
    private final static String TAG = MapManager.class.getSimpleName();
    private final ObjectMap<MapType, Map> mapCache;
    private final Array<MapListener> mapListeners;
    private Map currentMap;

    public MapManager() {
        this.mapListeners = new Array<>();
        this.currentMap = null;
        this.mapCache = new ObjectMap<>();
    }

    public void addMapListener(final MapListener listener) {
        this.mapListeners.add(listener);
    }

    public void removeMapListener(final MapListener listener) {
        this.mapListeners.removeValue(listener, true);
    }

    public boolean changeMap(final AssetManager assetManager, final MapType mapType) {
        if (assetManager.isLoaded(mapType.filePath)) {
            // map loaded -> change it
            Gdx.app.debug(TAG, "Changing map to " + mapType);
            Map map = mapCache.get(mapType);
            if (map == null) {
                Gdx.app.debug(TAG, "Creating new map " + mapType);
                map = new Map(mapType, assetManager.get(mapType.filePath, TiledMap.class));
                mapCache.put(mapType, map);
            }
            for (final MapListener listener : mapListeners) {
                listener.onMapChanged(currentMap, map);
            }
            currentMap = map;
            return true;
        } else {
            // map not loaded yet
            Gdx.app.debug(TAG, "Map " + mapType + " not loaded yet");
            assetManager.load(mapType.filePath, TiledMap.class);
            return false;
        }
    }

    public Map getCurrentMap() {
        return currentMap;
    }

    public enum MapType {
        TEST("maps/test.tmx");

        private final String filePath;

        MapType(final String filePath) {
            this.filePath = filePath;
        }
    }

    public interface MapListener {
        void onMapChanged(final Map currentMap, final Map newMap);
    }
}
