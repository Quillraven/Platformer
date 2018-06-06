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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.quillraven.platformer.GameInputListener;
import com.quillraven.platformer.Platformer;
import com.quillraven.platformer.WorldContactListener;

import static com.quillraven.platformer.Platformer.PPM;

/**
 * TODO add class description
 */

public class GSGame extends GameState {
    private final World world;
    private final Box2DDebugRenderer box2DRenderer;
    private final Body box;

    private int numFootContacts;
    private int move;

    public GSGame(final Platformer game) {
        super(game);

        numFootContacts = 0;
        move = 0;

        this.world = new World(new Vector2(0, -9.81f), true);
        world.setContactListener(new WorldContactListener(this));
        this.box2DRenderer = new Box2DDebugRenderer();

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
            fixtureDef.filter.maskBits = Platformer.BIT_BALL | Platformer.BIT_BOX;
            body.createFixture(fixtureDef).setUserData("platform");
            shape.dispose();
        }

        // create box
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(160 / PPM, 200 / PPM);
        box = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(5 / PPM, 5 / PPM);
        fixtureDef.friction = 1;
        fixtureDef.shape = shape;
        fixtureDef.filter.categoryBits = Platformer.BIT_BOX;
        fixtureDef.filter.maskBits = Platformer.BIT_GROUND;
        box.createFixture(fixtureDef).setUserData("box");
        shape.dispose();

        // create foot sensor for box

        shape = new PolygonShape();
        shape.setAsBox(5 / PPM, 2 / PPM, new Vector2(0, -5 / PPM), 0);
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;
        box.createFixture(fixtureDef).setUserData("foot");
        shape.dispose();

        // create circle
        bodyDef.position.set(167 / PPM, 230 / PPM);
        body = world.createBody(bodyDef);

        CircleShape shape2 = new CircleShape();
        shape2.setRadius(7 / PPM);
        fixtureDef.shape = shape2;
        fixtureDef.isSensor = false;
        fixtureDef.filter.categoryBits = Platformer.BIT_BALL;
        fixtureDef.filter.maskBits = Platformer.BIT_GROUND;
        body.createFixture(fixtureDef).setUserData("circle");
        shape2.dispose();
    }

    @Override
    Viewport getViewport() {
        return new FitViewport(Platformer.V_WIDTH / PPM, Platformer.V_HEIGHT / PPM);
    }

    @Override
    public void onActivation() {
    }

    @Override
    public void onDeactivation() {
    }

    @Override
    public boolean onKeyPressed(final GameStateManager gsManager, final GameInputListener.InputKeys key) {
        switch (key) {
            case JUMP: {
                if (numFootContacts > 0) {
                    // impulse = velocity * mass / time
                    // since we want instant movement we ignore the time factor
                    // therefore if we want to move our objects with a constant speed of 5 units/seconds our impulse will be:
                    // impulse = 5 - velocity.x * mass <-- the - velocity.x will adjust the impulse so that the result velocity.x will be 5
                    box.applyLinearImpulse(0, (3 - box.getLinearVelocity().y) * box.getMass(), box.getWorldCenter().x, box.getWorldCenter().y, true);
                }
                break;
            }
            case LEFT: {
                move = -1;
                break;
            }
            case RIGHT: {
                move = 1;
                break;
            }
        }
        return true;
    }

    @Override
    public boolean onKeyReleased(final GameStateManager gsManager, final GameInputListener.InputKeys key) {
        switch (key) {
            case RIGHT:
            case LEFT: {
                move = 0;
                box.applyLinearImpulse(-box.getLinearVelocity().x * box.getMass(), 0, box.getWorldCenter().x, box.getWorldCenter().y, true);
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
        if (move != 0) {
            box.applyLinearImpulse((move - box.getLinearVelocity().x) * box.getMass(), 0, box.getWorldCenter().x, box.getWorldCenter().y, true);
        }
        world.step(fixedTimeStep, 6, 2);
    }

    @Override
    public void onRender(final float alpha) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        spriteBatch.begin();
        box2DRenderer.render(world, camera.combined);
        spriteBatch.end();
    }

    @Override
    public void onDispose() {
        box2DRenderer.dispose();
        world.dispose();
    }


    public void onGroundCollision() {
        ++numFootContacts;
        if (numFootContacts > 0) {
            System.out.println("CAN JUMP");
        }
    }

    public void onLeaveGround() {
        --numFootContacts;
        if (numFootContacts <= 0) {
            System.out.println("CANNOT JUMP");
        }
    }
}
