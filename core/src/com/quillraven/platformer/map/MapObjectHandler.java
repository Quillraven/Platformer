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
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.quillraven.platformer.Platformer;
import com.quillraven.platformer.ecs.EntityEngine;

import static com.quillraven.platformer.Platformer.PPM;

/**
 * TODO add class description
 */
public class MapObjectHandler implements MapManager.MapListener {
    private static final String TAG = MapObjectHandler.class.getSimpleName();

    private final World world;
    private final EntityEngine entityEngine;
    private final Array<Body> worldBodies;
    private final BodyDef bodyDef;
    private final FixtureDef fixtureDef;

    public MapObjectHandler(final World world, final EntityEngine entityEngine) {
        this.world = world;
        this.entityEngine = entityEngine;
        this.worldBodies = new Array<>();
        this.bodyDef = new BodyDef();
        this.fixtureDef = new FixtureDef();
    }

    @Override
    public void onMapChanged(final Map currentMap, final Map newMap) {
        if (currentMap != null) {
            removeMapBodies();
        }
        createMapBodies(newMap);
    }

    private void removeMapBodies() {
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

        // remove any remaining entites
        entityEngine.removeAllEntities();
    }

    private void createMapBodies(final Map newMap) {
        createMapBodiesForLayer(newMap, "collision");
        createMapBodiesForLayer(newMap, "objects");
    }

    private void createMapBodiesForLayer(final Map newMap, final String layerName) {
        final MapLayer layer = newMap.getLayers().get(layerName);
        if (layer == null) {
            Gdx.app.log(TAG, "Map does not have layer " + layerName);
            return;
        }

        for (MapObject mapObj : layer.getObjects()) {
            final float[] vertices;
            final Object userData;
            final boolean isSensor;
            if (mapObj instanceof RectangleMapObject) {
                // create rectangle collision object
                final Rectangle rect = ((RectangleMapObject) mapObj).getRectangle();
                vertices = new float[8];
                userData = "platform";
                isSensor = false;
                // left-bot
                vertices[0] = rect.x / PPM;
                vertices[1] = rect.y / PPM;
                // left-top
                vertices[2] = rect.x / PPM;
                vertices[3] = rect.y / PPM + rect.height / PPM;
                // right-top
                vertices[4] = rect.x / PPM + rect.width / PPM;
                vertices[5] = rect.y / PPM + rect.height / PPM;
                // right-bot
                vertices[6] = rect.x / PPM + rect.width / PPM;
                vertices[7] = rect.y / PPM;
            } else if (mapObj instanceof PolylineMapObject) {
                // create polyline collision object
                final Polyline polyline = ((PolylineMapObject) mapObj).getPolyline();
                vertices = polyline.getVertices();
                userData = "platform";
                isSensor = false;
                final float x = polyline.getX() / PPM;
                final float y = polyline.getY() / PPM;
                for (int i = 0; i < vertices.length; i += 2) {
                    vertices[i] = vertices[i] / PPM + x;
                    vertices[i + 1] = vertices[i + 1] / PPM + y;
                }
            } else if (mapObj instanceof TiledMapTileMapObject) {
                final TiledMapTileMapObject obj = (TiledMapTileMapObject) mapObj;
                final float x = obj.getX() / PPM;
                final float y = obj.getY() / PPM;
                final float width = obj.getProperties().get("width", Float.class) / PPM;
                final float height = obj.getProperties().get("height", Float.class) / PPM;
                vertices = new float[8];
                //TODO userdata should be entity of object (f.e. coin or infobox)
                isSensor = true;
                userData = null;
                // left-bot
                vertices[0] = x;
                vertices[1] = y;
                // left-top
                vertices[2] = x;
                vertices[3] = y + height;
                // right-top
                vertices[4] = x + width;
                vertices[5] = y + height;
                // right-bot
                vertices[6] = x + width;
                vertices[7] = y;
            } else {
                Gdx.app.error(TAG, "Unsupported map object type: " + mapObj.getClass().getSimpleName());
                vertices = null;
                userData = null;
                isSensor = false;
            }

            if (vertices != null) {
                bodyDef.type = BodyDef.BodyType.StaticBody;
                bodyDef.position.set(0, 0);
                final Body body = world.createBody(bodyDef);
                final ChainShape shape = new ChainShape();
                if (vertices.length <= 4) {
                    // point or line
                    shape.createChain(vertices);
                } else {
                    // object with at least 3 corners
                    shape.createLoop(vertices);
                }
                fixtureDef.shape = shape;
                fixtureDef.friction = 0;
                fixtureDef.filter.categoryBits = Platformer.BIT_GROUND;
                fixtureDef.filter.maskBits = Platformer.BIT_PLAYER;
                fixtureDef.isSensor = isSensor;
                body.createFixture(fixtureDef).setUserData(userData);
                shape.dispose();
            }
        }
    }
}
