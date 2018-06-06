package com.quillraven.platformer.ecs;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.quillraven.platformer.ecs.components.ComponentBox2D;
import com.quillraven.platformer.ecs.components.ComponentJump;
import com.quillraven.platformer.ecs.components.ComponentMove;
import com.quillraven.platformer.ecs.systems.SystemJump;
import com.quillraven.platformer.ecs.systems.SystemMove;

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

    public EntityEngine() {
        super(20, 200, 10, 100);

        // add systems
        final ComponentMapper<ComponentBox2D> b2dCmpMapper = ComponentMapper.getFor(ComponentBox2D.class);
        final ComponentMapper<ComponentMove> moveCmpMapper = ComponentMapper.getFor(ComponentMove.class);
        this.addSystem(new SystemMove(b2dCmpMapper, moveCmpMapper));
        final ComponentMapper<ComponentJump> jumpCmpMapper = ComponentMapper.getFor(ComponentJump.class);
        this.addSystem(new SystemJump(b2dCmpMapper, jumpCmpMapper));

        // create box2d definitions
        this.bodyDef = new BodyDef();
        this.fixtureDef = new FixtureDef();
    }

    public Entity createEntity(final World world, final BodyDef.BodyType bodyType, final short maskBits, final short categoryBits, final float x, final float y, final float width, final float height) {
        final Entity entity = this.createEntity();

        // box2d component
        final ComponentBox2D b2dCmp = this.createComponent(ComponentBox2D.class);
        // body
        bodyDef.position.set(x, y);
        bodyDef.type = bodyType;
        b2dCmp.body = world.createBody(bodyDef);
        b2dCmp.body.setUserData(entity);
        // fixture
        fixtureDef.friction = 1;
        fixtureDef.isSensor = false;
        final PolygonShape shape = new PolygonShape();
        shape.setAsBox(width * 0.5f, height * 0.5f);
        fixtureDef.shape = shape;
        fixtureDef.filter.maskBits = maskBits;
        fixtureDef.filter.categoryBits = categoryBits;
        b2dCmp.body.createFixture(fixtureDef).setUserData("body");
        shape.dispose();
        entity.add(b2dCmp);

        // jump component
        final ComponentJump jumpCmp = this.createComponent(ComponentJump.class);
        jumpCmp.jumpSpeed = 3;
        entity.add(jumpCmp);

        // move component
        final ComponentMove moveCmp = this.createComponent(ComponentMove.class);
        moveCmp.maxSpeed = 1;
        entity.add(moveCmp);

        this.addEntity(entity);
        return entity;
    }
}
