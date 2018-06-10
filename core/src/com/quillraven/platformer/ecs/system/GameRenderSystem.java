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
 * TODO add class description
 */
public class GameRenderSystem extends RenderSystem implements MapManager.MapListener {
    private final OrthogonalTiledMapRenderer tiledMapRenderer;
    private final Family renderFamily;
    private final ComponentMapper<Box2DComponent> b2dCmpMapper;
    private final ComponentMapper<AnimationComponent> aniCmpMapper;

    public GameRenderSystem(final EntityEngine engine, final SpriteBatch spriteBatch, final ComponentMapper<Box2DComponent> b2dCmpMapper, final ComponentMapper<AnimationComponent> aniCmpMapper) {
        super(engine);
        MapManager.getInstance().addMapListener(this);
        tiledMapRenderer = new OrthogonalTiledMapRenderer(null, 1 / PPM, spriteBatch);
        this.renderFamily = Family.all(AnimationComponent.class).get();
        this.b2dCmpMapper = b2dCmpMapper;
        this.aniCmpMapper = aniCmpMapper;
    }

    @Override
    public void onRender(final SpriteBatch spriteBatch, final Camera camera, final float alpha) {
        final ImmutableArray<Entity> entitiesFor = engine.getEntitiesFor(renderFamily);

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
//            final float radius = b2dCmp.body.getFixtureList().first().getShape().getRadius();
            final float invertAlpha = 1.0f - alpha;

            // calculate interpolated position for rendering
            final float x = (position.x * alpha + b2dCmp.positionBeforeUpdate.x * invertAlpha) - (72f / PPM / 2);
            final float y = (position.y * alpha + b2dCmp.positionBeforeUpdate.y * invertAlpha) - (96f / PPM / 2);

            aniCmp.texture.setColor(Color.WHITE);
            aniCmp.texture.setFlip(false, false);
            aniCmp.texture.setOriginCenter();
            aniCmp.texture.setRotation(b2dCmp.body.getAngle() * MathUtils.radiansToDegrees);
            aniCmp.texture.setBounds(x, y, 72 / PPM, 96 / PPM);

            spriteBatch.draw(aniCmp.texture.getTexture(), aniCmp.texture.getVertices(), 0, 20);
        }
        spriteBatch.end();
    }

    @Override
    public void onMapChanged(final Map currentMap, final Map newMap) {
        tiledMapRenderer.setMap(newMap.getTiledMap());
    }

    @Override
    public void onDispose() {
        tiledMapRenderer.dispose();
    }
}
