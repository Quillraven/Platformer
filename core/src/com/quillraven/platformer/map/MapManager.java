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

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.quillraven.platformer.Platformer;
import com.quillraven.platformer.SoundManager;
import com.quillraven.platformer.ecs.EntityEngine;

import box2dLight.RayHandler;

import static com.quillraven.platformer.Platformer.PPM;

/**
 * TODO add class description
 */
public class MapManager {
    private final static String TAG = MapManager.class.getSimpleName();

    private static final MapManager instance = new MapManager();

    private final ObjectMap<MapType, Map> mapCache;
    private final Array<MapListener> mapListeners;
    private Map currentMap;
    private TiledMap currentTiledMap;
    private final BodyDef bodyDef;
    private final FixtureDef fixtureDef;
    private final Array<Body> worldBodies;
    private final float[] rectVertices = new float[8];

    private MapManager() {
        this.mapListeners = new Array<>();
        this.currentMap = null;
        this.currentTiledMap = null;
        this.mapCache = new ObjectMap<>();
        this.bodyDef = new BodyDef();
        this.fixtureDef = new FixtureDef();
        this.worldBodies = new Array<>();
    }

    public static MapManager getInstance() {
        return instance;
    }

    public void addMapListener(final MapListener listener) {
        this.mapListeners.add(listener);
    }

    public void removeMapListener(final MapListener listener) {
        this.mapListeners.removeValue(listener, true);
    }

    public boolean changeMap(final AssetManager assetManager, final MapType mapType, final World world, final RayHandler rayHandler, final EntityEngine entityEngine, boolean resetMap) {
        if (assetManager.isLoaded(mapType.filePath)) {
            if (!resetMap && currentMap != null && mapType.equals(currentMap.getMapType())) {
                // map already loaded
                SoundManager.getInstance().playSound(SoundManager.SoundType.valueOf(currentTiledMap.getProperties().get("music", String.class)));
                return true;
            }

            // map loaded -> change it
            Gdx.app.debug(TAG, "Changing map to " + mapType);
            Map map = mapCache.get(mapType);
            currentTiledMap = assetManager.get(mapType.filePath, TiledMap.class);
            final MapLayers mapLayers = currentTiledMap.getLayers();
            if (map == null) {
                Gdx.app.debug(TAG, "Creating new map " + mapType);
                map = new Map(mapType, currentTiledMap);
                mapCache.put(mapType, map);
            }

            if (currentMap != null) {
                removeMapBodies(world, entityEngine);
            }

            currentMap = map;
            currentMap.setMaxCoins(0);
            createMapBodies(mapLayers, world, rayHandler, entityEngine);

            SoundManager.getInstance().playSound(SoundManager.SoundType.valueOf(currentTiledMap.getProperties().get("music", String.class)));

            for (final MapListener listener : mapListeners) {
                listener.onMapChanged(currentMap, currentTiledMap);
            }

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

    private void removeMapBodies(final World world, final EntityEngine entityEngine) {
        world.getBodies(worldBodies);
        for (final Body body : worldBodies) {
            final Object userData = body.getUserData();
            if (userData instanceof Entity) {
                // entities are linked to the box2d body and they will destroy it themselves
                entityEngine.removeEntity((Entity) userData);
            } else {
                world.destroyBody(body);
            }
        }

        // remove any remaining entities
        entityEngine.removeAllEntities();
    }

    private void createMapBodies(final MapLayers mapLayers, final World world, final RayHandler rayHandler, final EntityEngine entityEngine) {
        createMapBodiesForLayer(mapLayers, "collisions", world, rayHandler, entityEngine);
        createMapBodiesForLayer(mapLayers, "objects", world, rayHandler, entityEngine);
        createMapBodiesForLayer(mapLayers, "enemies", world, rayHandler, entityEngine);
    }

    private void createMapBodiesForLayer(final MapLayers mapLayers, final String layerName, final World world, final RayHandler rayHandler, final EntityEngine entityEngine) {
        final MapLayer layer = mapLayers.get(layerName);
        if (layer == null) {
            Gdx.app.log(TAG, "Map does not have layer " + layerName);
            return;
        }

        for (MapObject mapObj : layer.getObjects()) {
            if (mapObj instanceof RectangleMapObject) {
                final RectangleMapObject rectMapObj = (RectangleMapObject) mapObj;
                if (rectMapObj.getRectangle().width == 0) {
                    // point object --> create enemy
                    createEnemy(rectMapObj, world, rayHandler, entityEngine);
                } else {
                    // rect object --> create collision object
                    createRectangleCollisionBody(rectMapObj, world);
                }
            } else if (mapObj instanceof PolylineMapObject) {
                // create polyline collision object
                createPolylineCollisionBody(((PolylineMapObject) mapObj).getPolyline(), world);
            } else if (mapObj instanceof TiledMapTileMapObject) {
                createMapObject((TiledMapTileMapObject) mapObj, world, entityEngine);
            } else {
                Gdx.app.error(TAG, "Unsupported map object type: " + mapObj.getClass().getSimpleName());
            }
        }
    }

    private void createEnemy(final RectangleMapObject mapObj, final World world, final RayHandler rayHandler, final EntityEngine entityEngine) {
        final MapProperties properties = mapObj.getProperties();

        final float x = properties.get("x", Float.class) / PPM;
        final float y = properties.get("y", Float.class) / PPM;
        entityEngine.createEnemy(world, rayHandler, x, y, properties.get("enemyType", String.class));
    }

    private Body createCollisionBody(final World world, final float x, final float y, final float[] vertices, final boolean createLoop, final short categoryBit, final boolean isSensor) {
        return createCollisionBody(world, x, y, vertices, createLoop, categoryBit, isSensor, null);
    }

    private Body createCollisionBody(final World world, final float x, final float y, final float[] vertices, final boolean createLoop, final short categoryBit, final boolean isSensor, final String userData) {
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(x, y);
        final Body body = world.createBody(bodyDef);
        final ChainShape shape = new ChainShape();
        if (createLoop) {
            shape.createLoop(vertices);
        } else {
            shape.createChain(vertices);
        }
        fixtureDef.shape = shape;
        fixtureDef.friction = 0;
        fixtureDef.filter.categoryBits = categoryBit;
        fixtureDef.filter.maskBits = Platformer.BIT_PLAYER | Platformer.BIT_ENEMY;
        fixtureDef.isSensor = isSensor;
        body.createFixture(fixtureDef).setUserData(userData);
        shape.dispose();

        if ("coin".equals(userData)) {
            currentMap.setMaxCoins(currentMap.getMaxCoins() + 1);
        }

        return body;
    }

    private void createRectangleCollisionBody(final RectangleMapObject mapObj, final World world) {
        final Rectangle rect = mapObj.getRectangle();
        final float halfW = rect.width / PPM * 0.5f;
        final float halfH = rect.height / PPM * 0.5f;
        // left-bot
        rectVertices[0] = -halfW;
        rectVertices[1] = -halfH;
        // left-top
        rectVertices[2] = -halfW;
        rectVertices[3] = halfH;
        // right-top
        rectVertices[4] = halfW;
        rectVertices[5] = halfH;
        // right-bot
        rectVertices[6] = halfW;
        rectVertices[7] = -halfH;

        createCollisionBody(world, rect.x / PPM + halfW, rect.y / PPM + halfH, rectVertices, true, Platformer.BIT_GROUND, false);
    }

    private void createPolylineCollisionBody(final Polyline polyline, final World world) {
        final float[] vertices = polyline.getVertices().clone();
        for (int i = 0; i < vertices.length; i += 2) {
            vertices[i] = vertices[i] / PPM;
            vertices[i + 1] = vertices[i + 1] / PPM;
        }

        createCollisionBody(world, polyline.getX() / PPM, polyline.getY() / PPM, vertices, false, Platformer.BIT_GROUND, false);
    }

    private void createMapObject(final TiledMapTileMapObject mapObj, final World world, final EntityEngine entityEngine) {
        mapObj.setVisible(true); // coins might be invisible if reloading a level (check GameObjectCollisionSystem)
        final MapProperties properties = mapObj.getProperties();
        final float halfW = properties.get("width", Float.class) / PPM * 0.5f;
        final float halfH = properties.get("height", Float.class) / PPM * 0.5f;

        // left-bot
        rectVertices[0] = -halfW;
        rectVertices[1] = -halfH;
        // left-top
        rectVertices[2] = -halfW;
        rectVertices[3] = halfH;
        // right-top
        rectVertices[4] = halfW;
        rectVertices[5] = halfH;
        // right-bot
        rectVertices[6] = halfW;
        rectVertices[7] = -halfH;

        final float centerX = properties.get("x", Float.class) / PPM + halfW;
        final float centerY = properties.get("y", Float.class) / PPM + halfH;
        final String userData = properties.get("userData", String.class);
        if ("coinFlag".equals(userData)) {
            currentMap.setCoinFlagObject(mapObj);
            final AnimatedTiledMapTile flagPoleTile = (AnimatedTiledMapTile) mapObj.getTile();
            final int[] intervals = flagPoleTile.getAnimationIntervals();
            intervals[0] = 150;
            intervals[1] = 150;
            intervals[2] = -1;
            flagPoleTile.setAnimationIntervals(intervals);
        }
        final Body body = createCollisionBody(world, centerX, centerY, rectVertices, true, Platformer.BIT_OBJECT, true, userData);
        entityEngine.createGameObj(body, mapObj);
    }

    public enum MapType {
        LEVEL_1("maps/level1.tmx"),
        LEVEL_2("maps/level2.tmx"),
        LEVEL_3("maps/level3.tmx");

        private final String filePath;

        MapType(final String filePath) {
            this.filePath = filePath;
        }
    }

    public interface MapListener {
        void onMapChanged(final Map map, final TiledMap tiledMap);
    }
}
