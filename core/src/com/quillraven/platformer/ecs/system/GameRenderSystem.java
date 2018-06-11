package com.quillraven.platformer.ecs.system;
/*
 * Created by Quillraven on 10.06.2018.
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

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.quillraven.platformer.ecs.EntityEngine;
import com.quillraven.platformer.ecs.component.AnimationComponent;
import com.quillraven.platformer.ecs.component.Box2DComponent;
import com.quillraven.platformer.map.Map;
import com.quillraven.platformer.map.MapManager;

import static com.quillraven.platformer.Platformer.PPM;

/**
 * TODO add class description and use SortedIteratingSystem
 */
public class GameRenderSystem extends RenderSystem implements MapManager.MapListener {
    private final OrthogonalTiledMapRenderer tiledMapRenderer;
    private final Family renderFamily;
    private final ComponentMapper<Box2DComponent> b2dCmpMapper;
    private final ComponentMapper<AnimationComponent> aniCmpMapper;
    private float mapWidth;
    private float mapHeight;

    public GameRenderSystem(final EntityEngine engine, final SpriteBatch spriteBatch, final ComponentMapper<Box2DComponent> b2dCmpMapper, final ComponentMapper<AnimationComponent> aniCmpMapper) {
        super(engine);
        MapManager.getInstance().addMapListener(this);
        tiledMapRenderer = new OrthogonalTiledMapRenderer(null, 1 / PPM, spriteBatch);
        this.renderFamily = Family.all(AnimationComponent.class, Box2DComponent.class).get();
        this.b2dCmpMapper = b2dCmpMapper;
        this.aniCmpMapper = aniCmpMapper;
    }

    @Override
    public void onRender(final SpriteBatch spriteBatch, final Camera camera, final float alpha) {
        final ImmutableArray<Entity> entitiesFor = engine.getEntitiesFor(renderFamily);

        final Entity player = engine.getPlayer();
        if (player != null) {
            final Box2DComponent b2dCmp = b2dCmpMapper.get(player);
            final AnimationComponent aniCmp = aniCmpMapper.get(player);
            final Vector2 playerPos = b2dCmp.body.getPosition();
            final float invertAlpha = 1.0f - alpha;
            final float x = (playerPos.x * alpha + b2dCmp.positionBeforeUpdate.x * invertAlpha) - (aniCmp.width / PPM / 2);
            final float y = (playerPos.y * alpha + b2dCmp.positionBeforeUpdate.y * invertAlpha) - (aniCmp.height / PPM / 2);
            final float camWidth = camera.viewportWidth * 0.5f;
            final float camHeight = camera.viewportHeight * 0.5f;
            camera.position.set(Math.min(mapWidth - camWidth, Math.max(x, camWidth)), Math.min(mapHeight - camHeight, Math.max(y, camHeight)), 0);
            camera.update();
        }

        spriteBatch.begin();
        if (tiledMapRenderer.getMap() != null) {
            tiledMapRenderer.setView((OrthographicCamera) camera);
            AnimatedTiledMapTile.updateAnimationBaseTime();
            for (final MapLayer layer : tiledMapRenderer.getMap().getLayers()) {
                if (layer instanceof TiledMapTileLayer) {
                    tiledMapRenderer.renderTileLayer((TiledMapTileLayer) layer);
                }
            }
        }

        for (final Entity entity : entitiesFor) {
            final Box2DComponent b2dCmp = b2dCmpMapper.get(entity);
            final Vector2 position = b2dCmp.body.getPosition();
            final AnimationComponent aniCmp = aniCmpMapper.get(entity);
            final float invertAlpha = 1.0f - alpha;

            // calculate interpolated position for rendering
            final float x = (position.x * alpha + b2dCmp.positionBeforeUpdate.x * invertAlpha) - (aniCmp.width / PPM / 2);
            final float y = (position.y * alpha + b2dCmp.positionBeforeUpdate.y * invertAlpha) - (aniCmp.height / PPM / 2);

            aniCmp.texture.setColor(Color.WHITE);
            aniCmp.texture.setFlip(false, false);
            aniCmp.texture.setOriginCenter();
            aniCmp.texture.setRotation(b2dCmp.body.getAngle() * MathUtils.radiansToDegrees);
            aniCmp.texture.setBounds(x, y, aniCmp.width / PPM, aniCmp.height / PPM);

            spriteBatch.draw(aniCmp.texture.getTexture(), aniCmp.texture.getVertices(), 0, 20);
        }
        spriteBatch.end();
    }

    @Override
    public void onMapChanged(final Map map, final TiledMap tiledMap) {
        mapWidth = map.getWidth();
        mapHeight = map.getHeight();
        tiledMapRenderer.setMap(tiledMap);
    }

    @Override
    public void onDispose() {
        tiledMapRenderer.dispose();
    }
}
