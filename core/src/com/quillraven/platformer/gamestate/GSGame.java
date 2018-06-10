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
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2D;
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
import com.quillraven.platformer.ui.GameHUD;

import static com.quillraven.platformer.Platformer.PPM;

/**
 * TODO add class description
 */

public class GSGame extends GameState<GameHUD> implements MapManager.MapListener {
    private final World world;
    private final EntityEngine entityEngine;
    private Entity player;
    private final Viewport gameViewport;
    private final OrthographicCamera gameCamera;
    private float minCameraWidth;
    private float minCameraHeight;
    private float maxCameraWidth;
    private float maxCameraHeight;

    public GSGame(final AssetManager assetManager, final GameHUD hud, final SpriteBatch spriteBatch) {
        super(assetManager, hud, spriteBatch);

        this.gameViewport = new FitViewport(Platformer.V_WIDTH / PPM, Platformer.V_HEIGHT / PPM);
        this.gameCamera = (OrthographicCamera) gameViewport.getCamera();

        // init box2d
        Box2D.init();
        this.world = new World(new Vector2(0, -70), true);
        final WorldContactListener contactListener = new WorldContactListener();
        world.setContactListener(contactListener);

        // init ashley entity component system
        entityEngine = new EntityEngine(world, contactListener, spriteBatch);

        // create tile map renderer
        MapManager.getInstance().addMapListener(new MapObjectHandler(world, entityEngine));
        MapManager.getInstance().addMapListener(this);
    }

    @Override
    public void onActivation() {
        if (MapManager.getInstance().changeMap(assetManager, MapManager.MapType.TEST)) {
            // create player
            player = entityEngine.createEntity(world, BodyDef.BodyType.DynamicBody, Platformer.BIT_GROUND, Platformer.BIT_PLAYER, 77, 150, 72, 96);
            entityEngine.getAnimationComponent(player).texture = new Sprite(assetManager.get("characters/slimeDead.png", Texture.class));

            // create second entity
            final Entity entity = entityEngine.createEntity(world, BodyDef.BodyType.DynamicBody, Platformer.BIT_GROUND, Platformer.BIT_PLAYER, 217, 150, 72, 96);
            entityEngine.getAnimationComponent(entity).texture = new Sprite(assetManager.get("characters/slimeDead.png", Texture.class));
        } else {
            assetManager.load("characters/slimeDead.png", Texture.class);
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

        // update camera position
        final Vector2 playerPos = entityEngine.getBox2DComponent(player).body.getPosition();
        gameCamera.position.set(Math.min(maxCameraWidth, Math.max(playerPos.x, minCameraWidth)), Math.min(maxCameraHeight, Math.max(playerPos.y, minCameraHeight)), 0);
        gameCamera.update();
        super.onUpdate(gsManager, fixedTimeStep);
    }

    @Override
    public void onRender(final SpriteBatch spriteBatch, final float alpha) {
        entityEngine.onRender(spriteBatch, gameCamera, alpha);
        super.onRender(spriteBatch, alpha);
    }

    @Override
    public void onDispose() {
        world.dispose();
        entityEngine.dispose();
        super.onDispose();
    }

    @Override
    public void onResize(final int width, final int height) {
        super.onResize(width, height);
        gameViewport.update(width, height);
        updateCameraBoundaries(MapManager.getInstance().getCurrentMap());
    }

    @Override
    public void onMapChanged(final Map currentMap, final Map newMap) {
        updateCameraBoundaries(newMap);
    }

    private void updateCameraBoundaries(final Map map) {
        if (map == null) {
            return;
        }
        maxCameraWidth = map.getWidth() - gameCamera.viewportWidth * 0.5f;
        maxCameraHeight = map.getHeight() - gameCamera.viewportHeight * 0.5f;
        minCameraWidth = gameCamera.viewportWidth * 0.5f;
        minCameraHeight = gameCamera.viewportHeight * 0.5f;
    }
}
