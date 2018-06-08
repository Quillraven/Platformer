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
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.quillraven.platformer.GameInputListener;
import com.quillraven.platformer.Platformer;
import com.quillraven.platformer.WorldContactListener;
import com.quillraven.platformer.ecs.EntityEngine;
import com.quillraven.platformer.ecs.components.Box2DComponent;
import com.quillraven.platformer.ecs.components.JumpComponent;
import com.quillraven.platformer.ecs.components.MoveComponent;

import static com.quillraven.platformer.Platformer.PPM;

/**
 * TODO add class description
 */

public class GSGame extends GameState {
    private final World world;
    private final Box2DDebugRenderer box2DRenderer;
    private final EntityEngine entityEngine;

    private final Entity player;

    public GSGame() {
        super();

        this.world = new World(new Vector2(0, -9.81f), true);
        final WorldContactListener contactListener = new WorldContactListener();
        world.setContactListener(contactListener);
        this.box2DRenderer = new Box2DDebugRenderer();

        entityEngine = new EntityEngine(contactListener);

        // create platform
        final BodyDef bodyDef = new BodyDef();
        final FixtureDef fixtureDef = new FixtureDef();
        Body body;
        for (int i = 0; i < 10; ++i) {
            bodyDef.type = BodyDef.BodyType.StaticBody;
            bodyDef.position.set((100 + i * 10) / PPM, 120 / PPM);
            body = world.createBody(bodyDef);

            ChainShape shape = new ChainShape();
            Vector2[] vertices = new Vector2[4];
            vertices[0] = new Vector2(5 / PPM, 5 / PPM);
            vertices[1] = new Vector2(-5 / PPM, 5 / PPM);
            vertices[2] = new Vector2(-5 / PPM, -5 / PPM);
            vertices[3] = new Vector2(5 / PPM, -5 / PPM);
            shape.createLoop(vertices);
            fixtureDef.shape = shape;
            fixtureDef.friction = 0;
            fixtureDef.filter.categoryBits = Platformer.BIT_GROUND;
            fixtureDef.filter.maskBits = Platformer.BIT_PLAYER;
            body.createFixture(fixtureDef).setUserData("platform");
            shape.dispose();
        }

        // create player
        player = entityEngine.createEntity(world, BodyDef.BodyType.DynamicBody, Platformer.BIT_GROUND, Platformer.BIT_PLAYER, 160, 200, 10, 10);

        // create second entity
        entityEngine.createEntity(world, BodyDef.BodyType.DynamicBody, Platformer.BIT_GROUND, Platformer.BIT_PLAYER, 167, 230, 14, 14);
    }

    @Override
    Viewport getViewport() {
        return new FitViewport(Platformer.V_WIDTH / PPM, Platformer.V_HEIGHT / PPM);
    }

    @Override
    public void onActivation(final AssetManager assetManager) {
        assetManager.load("hud/switchLeft.png", Texture.class);
        assetManager.load("hud/switchRight.png", Texture.class);
    }

    @Override
    public void onDeactivation(final AssetManager assetManager) {
    }

    @Override
    public boolean onKeyPressed(final GameStateManager gsManager, final GameInputListener inputListener, final GameInputListener.GameKeys key) {
        switch (key) {
            case JUMP: {
                player.getComponent(JumpComponent.class).jump = true;
                break;
            }
            case LEFT: {
                player.getComponent(MoveComponent.class).speed = -1;
                break;
            }
            case RIGHT: {
                player.getComponent(MoveComponent.class).speed = 1;
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
    }

    @Override
    public void onRender(final SpriteBatch spriteBatch, final float alpha) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
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
}
