package com.quillraven.platformer.ecs.systems;
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

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.quillraven.platformer.WorldContactListener;
import com.quillraven.platformer.ecs.components.Box2DComponent;
import com.quillraven.platformer.ecs.components.JumpComponent;
import com.quillraven.platformer.ecs.components.MoveComponent;

/**
 * TODO add class description
 */
public class JumpSystem extends IteratingSystem implements WorldContactListener.GameContactListener {
    private final ComponentMapper<Box2DComponent> b2dCmpMapper;
    private final ComponentMapper<JumpComponent> jumpCmpMapper;

    public JumpSystem(final ComponentMapper<Box2DComponent> b2dCmpMapper, final ComponentMapper<JumpComponent> jumpCmpMapper) {
        super(Family.all(Box2DComponent.class, MoveComponent.class).get());

        this.b2dCmpMapper = b2dCmpMapper;
        this.jumpCmpMapper = jumpCmpMapper;
    }

    @Override
    protected void processEntity(final Entity entity, final float deltaTime) {
        final JumpComponent jumpCmp = jumpCmpMapper.get(entity);
        final Box2DComponent b2dCmp = b2dCmpMapper.get(entity);
        final Vector2 worldCenter = b2dCmp.body.getWorldCenter();

        if (jumpCmp.jump && b2dCmp.numGroundContacts > 0) {
            // impulse = velocity * mass / time
            // since we want instant movement we ignore the time factor
            // therefore if we want to move our objects with a constant speed of 5 units/seconds our impulse will be:
            // impulse = 5 - velocity.x * mass <-- the - velocity.x will adjust the impulse so that the result velocity.x will be 5
            b2dCmp.body.applyLinearImpulse(0, (jumpCmp.jumpSpeed - b2dCmp.body.getLinearVelocity().y) * b2dCmp.body.getMass(), worldCenter.x, worldCenter.y, true);
        }

        jumpCmp.jump = false;
    }

    @Override
    public void onBeginGroundContact(final Entity entity) {
        ++b2dCmpMapper.get(entity).numGroundContacts;
    }

    @Override
    public void onEndGroundContact(final Entity entity) {
        --b2dCmpMapper.get(entity).numGroundContacts;
    }

    @Override
    public void onBeginEntityContact(final Entity entityA, final Entity entityB) {
        b2dCmpMapper.get(entityA).contacts.add(entityB);
        b2dCmpMapper.get(entityB).contacts.add(entityA);
    }

    @Override
    public void onEndEntityContact(final Entity entityA, final Entity entityB) {
        b2dCmpMapper.get(entityA).contacts.removeValue(entityB, false);
        b2dCmpMapper.get(entityB).contacts.removeValue(entityA, false);
    }
}
