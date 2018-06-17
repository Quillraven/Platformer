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
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import com.quillraven.platformer.ecs.EntityEngine;
import com.quillraven.platformer.ecs.component.AnimationComponent;
import com.quillraven.platformer.ecs.component.Box2DComponent;
import com.quillraven.platformer.map.Map;
import com.quillraven.platformer.map.MapManager;
import com.quillraven.platformer.map.MapRenderer;
import com.quillraven.platformer.ui.AnimationManager;

import static com.quillraven.platformer.Platformer.PPM;

/**
 * TODO add class description and use SortedIteratingSystem
 */
public class GameRenderSystem extends RenderSystem implements MapManager.MapListener {
    private final MapRenderer mapRenderer;
    private final Family renderFamily;
    private final ComponentMapper<Box2DComponent> b2dCmpMapper;
    private final ComponentMapper<AnimationComponent> aniCmpMapper;
    private float mapWidth;
    private float mapHeight;
    private int[] bgdLayerIdx;
    private int[] fgdLayerIdx;

    public GameRenderSystem(final EntityEngine engine, final SpriteBatch spriteBatch, final ComponentMapper<Box2DComponent> b2dCmpMapper, final ComponentMapper<AnimationComponent> aniCmpMapper) {
        super(engine);
        MapManager.getInstance().addMapListener(this);
        mapRenderer = new MapRenderer(spriteBatch);
        this.renderFamily = Family.all(AnimationComponent.class, Box2DComponent.class).get();
        this.b2dCmpMapper = b2dCmpMapper;
        this.aniCmpMapper = aniCmpMapper;
    }

    @Override
    public void onRender(final SpriteBatch spriteBatch, final Camera camera, final float alpha) {
        final ImmutableArray<Entity> animatedEntities = engine.getEntitiesFor(renderFamily);

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
        if (mapRenderer.getMap() != null) {
            mapRenderer.setView((OrthographicCamera) camera);
            mapRenderer.render(bgdLayerIdx);
        }

        for (final Entity entity : animatedEntities) {
            final Box2DComponent b2dCmp = b2dCmpMapper.get(entity);
            final Vector2 position = b2dCmp.body.getPosition();
            final AnimationComponent aniCmp = aniCmpMapper.get(entity);
            final float invertAlpha = 1.0f - alpha;

            // calculate interpolated position for rendering
            final float x = (position.x * alpha + b2dCmp.positionBeforeUpdate.x * invertAlpha) - (b2dCmp.width * 0.5f);
            final float y = (position.y * alpha + b2dCmp.positionBeforeUpdate.y * invertAlpha) - (b2dCmp.height * 0.5f);

            final Animation<Sprite> animation = AnimationManager.getInstance().getAnimation(aniCmp.aniType);
            final Sprite frame = animation.getKeyFrame(aniCmp.animationTime, true);
            frame.setColor(Color.WHITE);
            frame.setFlip(aniCmp.flipHoricontal, false);
            frame.setOriginCenter();
            if (b2dCmp.body.getLinearVelocity().y >= 5) {
                // jumping
                frame.setRotation(0);
            } else {
                frame.setRotation(b2dCmp.numGroundContactsLeft == 0 && b2dCmp.numGroundContactsRight > 0 ? 40 : b2dCmp.numGroundContactsLeft > 0 && b2dCmp.numGroundContactsRight == 0 ? 320 : 0);
            }
            frame.setBounds(x - (aniCmp.width - b2dCmp.width) * 0.5f, frame.getRotation() == 0 ? y - 2f / PPM : y - b2dCmp.height * 0.2f, aniCmp.width, aniCmp.height);

            spriteBatch.draw(frame.getTexture(), frame.getVertices(), 0, 20);
        }

        if (mapRenderer.getMap() != null) {
            mapRenderer.render(fgdLayerIdx);
        }
        spriteBatch.end();
    }

    @Override
    public void onMapChanged(final Map map, final TiledMap tiledMap) {
        mapWidth = map.getWidth();
        mapHeight = map.getHeight();
        bgdLayerIdx = map.getBackgroundLayerIndex();
        fgdLayerIdx = map.getForegroundLayerIndex();
        mapRenderer.setMap(tiledMap);
    }

    @Override
    public void onDispose() {
        mapRenderer.dispose();
    }
}
