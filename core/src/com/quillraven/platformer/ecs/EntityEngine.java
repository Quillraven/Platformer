package com.quillraven.platformer.ecs;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.quillraven.platformer.Platformer;
import com.quillraven.platformer.WorldContactListener;
import com.quillraven.platformer.ecs.components.Box2DComponent;
import com.quillraven.platformer.ecs.components.JumpComponent;
import com.quillraven.platformer.ecs.components.MoveComponent;
import com.quillraven.platformer.ecs.systems.JumpSystem;
import com.quillraven.platformer.ecs.systems.MoveSystem;

import static com.quillraven.platformer.Platformer.PPM;

/*
 * Created by Quillraven on 06.06.2018.
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

/**
 * TODO add class description
 */
public class EntityEngine extends PooledEngine {
    private final BodyDef bodyDef;
    private final FixtureDef fixtureDef;
    private final ComponentMapper<Box2DComponent> b2dCmpMapper;
    private final Family b2dFamily;

    public EntityEngine(final WorldContactListener contactListener) {
        super(20, 200, 10, 100);

        this.b2dFamily = Family.all(Box2DComponent.class).get();

        // add systems
        // movement
        b2dCmpMapper = ComponentMapper.getFor(Box2DComponent.class);
        final ComponentMapper<MoveComponent> moveCmpMapper = ComponentMapper.getFor(MoveComponent.class);
        this.addSystem(new MoveSystem(b2dCmpMapper, moveCmpMapper));
        // jump
        final ComponentMapper<JumpComponent> jumpCmpMapper = ComponentMapper.getFor(JumpComponent.class);
        final JumpSystem jumpSystem = new JumpSystem(b2dCmpMapper, jumpCmpMapper);
        contactListener.addGameContactListener(jumpSystem);
        this.addSystem(jumpSystem);

        // create box2d definitions
        this.bodyDef = new BodyDef();
        this.fixtureDef = new FixtureDef();
    }

    public ImmutableArray<Entity> getBox2DEntities() {
        return getEntitiesFor(b2dFamily);
    }

    public Box2DComponent getBox2DComponent(final Entity entity) {
        return b2dCmpMapper.get(entity);
    }

    public Entity createEntity(final World world, final BodyDef.BodyType bodyType, final short maskBits, final short categoryBits, final float x, final float y, final float width, final float height) {
        final Entity entity = this.createEntity();

        // box2d component
        final Box2DComponent b2dCmp = this.createComponent(Box2DComponent.class);
        // body
        bodyDef.position.set(x / PPM, y / PPM);
        bodyDef.type = bodyType;
        b2dCmp.body = world.createBody(bodyDef);
        b2dCmp.body.setUserData(entity);
        // fixture
        fixtureDef.friction = 1;
        fixtureDef.isSensor = false;
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width * 0.5f / PPM, height * 0.5f / PPM);
        fixtureDef.shape = shape;
        fixtureDef.filter.maskBits = maskBits;
        fixtureDef.filter.categoryBits = categoryBits;
        b2dCmp.body.createFixture(fixtureDef).setUserData("body");
        shape.dispose();
        entity.add(b2dCmp);
        // foot sensor
        shape = new PolygonShape();
        shape.setAsBox(width * 0.3f / PPM, height * 0.3f / PPM, new Vector2(0, -height * 0.5f / PPM), 0);
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;
        fixtureDef.filter.maskBits = Platformer.BIT_GROUND;
        fixtureDef.filter.categoryBits = categoryBits;
        b2dCmp.body.createFixture(fixtureDef).setUserData("foot");
        shape.dispose();
        // hitbox
        shape = new PolygonShape();
        shape.setAsBox(width * 0.3f / PPM, height * 0.3f / PPM);
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;
        fixtureDef.filter.maskBits = Platformer.BIT_PLAYER;
        fixtureDef.filter.categoryBits = categoryBits;
        b2dCmp.body.createFixture(fixtureDef).setUserData("hitbox");
        shape.dispose();

        // jump component
        final JumpComponent jumpCmp = this.createComponent(JumpComponent.class);
        jumpCmp.jumpSpeed = 3;
        entity.add(jumpCmp);

        // move component
        final MoveComponent moveCmp = this.createComponent(MoveComponent.class);
        moveCmp.maxSpeed = 1;
        entity.add(moveCmp);

        this.addEntity(entity);
        return entity;
    }
}
