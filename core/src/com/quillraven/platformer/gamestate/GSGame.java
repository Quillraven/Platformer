package com.quillraven.platformer.gamestate;
/*
 * Created by Quillraven on 04.06.2018.
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
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.quillraven.platformer.GameInputListener;
import com.quillraven.platformer.Platformer;
import com.quillraven.platformer.WorldContactListener;
import com.quillraven.platformer.ecs.EntityEngine;
import com.quillraven.platformer.ecs.component.Box2DComponent;
import com.quillraven.platformer.ecs.component.JumpComponent;
import com.quillraven.platformer.ecs.component.MoveComponent;
import com.quillraven.platformer.map.Map;
import com.quillraven.platformer.map.MapManager;
import com.quillraven.platformer.map.MapObjectHandler;

import static com.quillraven.platformer.Platformer.PPM;

/**
 * TODO add class description
 */

public class GSGame extends GameState implements MapManager.MapListener {
    private final World world;
    private final Box2DDebugRenderer box2DRenderer;

    private final EntityEngine entityEngine;
    private final OrthogonalTiledMapRenderer mapRenderer;
    private final MapManager mapManager;
    private Entity player;
    private float minCameraWidth;
    private float minCameraHeight;
    private float maxCameraWidth;
    private float maxCameraHeight;

    public GSGame(final AssetManager assetManager) {
        super(assetManager);

        Box2D.init();
        this.world = new World(new Vector2(0, -70), true);
        final WorldContactListener contactListener = new WorldContactListener();
        world.setContactListener(contactListener);
        this.box2DRenderer = new Box2DDebugRenderer();

        entityEngine = new EntityEngine(contactListener);

        // create tile map renderer
        mapRenderer = new OrthogonalTiledMapRenderer(null, 1 / PPM);
        this.mapManager = new MapManager();
        this.mapManager.addMapListener(new MapObjectHandler(world, entityEngine));
        this.mapManager.addMapListener(this);
    }

    @Override
    Viewport getViewport() {
        return new FitViewport(Platformer.V_WIDTH / PPM, Platformer.V_HEIGHT / PPM);
    }

    @Override
    public void onActivation() {
        if (mapManager.changeMap(assetManager, MapManager.MapType.TEST)) {
            // create player
            player = entityEngine.createEntity(world, BodyDef.BodyType.DynamicBody, Platformer.BIT_GROUND, Platformer.BIT_PLAYER, 77, 150, 72, 96);

            // create second entity
            entityEngine.createEntity(world, BodyDef.BodyType.DynamicBody, Platformer.BIT_GROUND, Platformer.BIT_PLAYER, 217, 150, 72, 96);
        }
    }

    @Override
    public void onDeactivation() {
    }

    @Override
    public boolean onKeyPressed(final GameStateManager gsManager, final GameInputListener inputListener, final GameInputListener.GameKeys key) {
        switch (key) {
            case JUMP: {
                player.getComponent(JumpComponent.class).jump = true;
                break;
            }
            case LEFT: {
                player.getComponent(MoveComponent.class).speed = -6;
                break;
            }
            case RIGHT: {
                player.getComponent(MoveComponent.class).speed = 6;
                break;
            }
        }
        return true;
    }

    @Override
    public boolean onKeyReleased(final GameStateManager gsManager, final GameInputListener inputListener, final GameInputListener.GameKeys key) {
        switch (key) {
            case RIGHT:
            case LEFT: {
                if (!inputListener.isKeyPressed(GameInputListener.GameKeys.RIGHT) && !inputListener.isKeyPressed(GameInputListener.GameKeys.LEFT)) {
                    player.getComponent(MoveComponent.class).speed = 0;
                }
                break;
            }
            case JUMP:
                final Box2DComponent b2dCmp = entityEngine.getBox2DComponent(player);
                if (b2dCmp.body.getLinearVelocity().y > 0) {
                    final Vector2 worldCenter = b2dCmp.body.getWorldCenter();
                    b2dCmp.body.applyLinearImpulse(0, -b2dCmp.body.getLinearVelocity().y * b2dCmp.body.getMass(), worldCenter.x, worldCenter.y, true);
                }
                break;
            case EXIT: {
                gsManager.popState();
                break;
            }
        }
        return true;
    }

    @Override
    public void onUpdate(final GameStateManager gsManager, final float fixedTimeStep) {
        // save position before update for interpolated rendering
        for (Entity entity : entityEngine.getBox2DEntities()) {
            final Box2DComponent b2dCmp = entityEngine.getBox2DComponent(entity);
            b2dCmp.positionBeforeUpdate.set(b2dCmp.body.getPosition());
        }
        world.step(fixedTimeStep, 6, 2);
        entityEngine.update(fixedTimeStep);
        final Vector2 playerPos = entityEngine.getBox2DComponent(player).body.getPosition();
        camera.position.set(Math.min(maxCameraWidth, Math.max(playerPos.x, minCameraWidth)), Math.min(maxCameraHeight, Math.max(playerPos.y, minCameraHeight)), 0);
        camera.update();
    }

    @Override
    public void onRender(final SpriteBatch spriteBatch, final float alpha) {
        Gdx.gl.glClearColor(0.7f, 0.9f, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // AnimatedTiledMapTile.updateAnimationBaseTime();
        if (mapRenderer.getMap() != null) {
            mapRenderer.setView(camera);
            mapRenderer.render();
        }

        spriteBatch.begin();
        box2DRenderer.render(world, camera.combined);
        spriteBatch.end();

        /*
                     final float invertAlpha = 1.0f - alpha;

                     // calculate interpolated position for rendering
                     aniCmp.position.x = (position.x * alpha + b2dCmp.positionBeforeUpdate.x * invertAlpha) - radius;
                     aniCmp.position.y = (position.y * alpha + b2dCmp.positionBeforeUpdate.y * invertAlpha) - radius;
         */
    }

    @Override
    public void onDispose() {
        box2DRenderer.dispose();
        world.dispose();
    }

    @Override
    public void onMapChanged(final Map currentMap, final Map newMap) {
        maxCameraWidth = newMap.getWidth() - camera.viewportWidth * 0.5f;
        maxCameraHeight = newMap.getHeight() - camera.viewportHeight * 0.5f;
        minCameraWidth = camera.viewportWidth * 0.5f;
        minCameraHeight = camera.viewportHeight * 0.5f;
        mapRenderer.setMap(newMap.getTiledMap());
    }
}
