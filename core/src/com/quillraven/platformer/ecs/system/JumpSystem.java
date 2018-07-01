package com.quillraven.platformer.ecs.system;
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
import com.quillraven.platformer.GameInputManager;
import com.quillraven.platformer.ParticleEffectManager;
import com.quillraven.platformer.SoundManager;
import com.quillraven.platformer.WorldContactManager;
import com.quillraven.platformer.ecs.EntityEngine;
import com.quillraven.platformer.ecs.component.Box2DComponent;
import com.quillraven.platformer.ecs.component.JumpComponent;
import com.quillraven.platformer.ecs.component.MoveComponent;
import com.quillraven.platformer.ecs.component.PlayerComponent;

/**
 * TODO add class description
 */
public class JumpSystem extends IteratingSystem implements WorldContactManager.GameContactListener, GameInputManager.GameKeyListener {
    private final ComponentMapper<Box2DComponent> b2dCmpMapper;
    private final ComponentMapper<JumpComponent> jumpCmpMapper;

    public JumpSystem(final ComponentMapper<Box2DComponent> b2dCmpMapper, final ComponentMapper<JumpComponent> jumpCmpMapper) {
        super(Family.all(Box2DComponent.class, MoveComponent.class, PlayerComponent.class).get());

        this.b2dCmpMapper = b2dCmpMapper;
        this.jumpCmpMapper = jumpCmpMapper;

        WorldContactManager.getInstance().addGameContactListener(this);
    }

    @Override
    protected void processEntity(final Entity entity, final float deltaTime) {
        final JumpComponent jumpCmp = jumpCmpMapper.get(entity);
        final Box2DComponent b2dCmp = b2dCmpMapper.get(entity);
        final Vector2 worldCenter = b2dCmp.body.getWorldCenter();

        if (jumpCmp.jump && (b2dCmp.numGroundContactsLeft > 0 || b2dCmp.numGroundContactsRight > 0)) {
            // impulse = velocity * mass / time
            // since we want instant movement we ignore the time factor
            // therefore if we want to move our objects with a constant speed of 5 units/seconds our impulse will be:
            // impulse = 5 - velocity.x * mass <-- the - velocity.x will adjust the impulse so that the result velocity.x will be 5
            b2dCmp.body.applyLinearImpulse(0, (jumpCmp.jumpSpeed - b2dCmp.body.getLinearVelocity().y) * b2dCmp.body.getMass(), worldCenter.x, worldCenter.y, true);
            SoundManager.getInstance().playSound(SoundManager.SoundType.SFX_JUMP);
            ParticleEffectManager.getInstance().spawnDustEffect(b2dCmp.body.getPosition().x, b2dCmp.body.getPosition().y - b2dCmp.height * 0.5f);
        }

        jumpCmp.jump = false;
    }

    @Override
    public void onBeginGroundContact(final Entity entity, final String userData) {
        if ("foot-left".equals(userData)) {
            ++b2dCmpMapper.get(entity).numGroundContactsLeft;
        } else if ("foot-right".equals(userData)) {
            ++b2dCmpMapper.get(entity).numGroundContactsRight;
        }
    }

    @Override
    public void onEndGroundContact(final Entity entity, final String userData) {
        if ("foot-left".equals(userData)) {
            --b2dCmpMapper.get(entity).numGroundContactsLeft;
        } else if ("foot-right".equals(userData)) {
            --b2dCmpMapper.get(entity).numGroundContactsRight;
        }
    }

    @Override
    public void onBeginObjectContact(final Entity player, final Entity object, final String objectUserData) {

    }

    @Override
    public void onEndObjectContact(final Entity player, final Entity object, final String objectUserData) {

    }

    @Override
    public void onBeginEnemyContact(final Entity player, final Entity enemy, final boolean killEnemy) {

    }

    @Override
    public void onEndEnemyContact(final Entity player, final Entity enemy, final boolean killEnemy) {

    }

    @Override
    public boolean onKeyPressed(final GameInputManager.GameKeys key) {
        if (key == GameInputManager.GameKeys.JUMP) {
            final Entity player = ((EntityEngine) getEngine()).getPlayer();
            if (player != null) {
                jumpCmpMapper.get(player).jump = true;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onKeyReleased(final GameInputManager.GameKeys key) {
        if (key == GameInputManager.GameKeys.JUMP) {
            final Entity player = ((EntityEngine) getEngine()).getPlayer();
            if (player != null) {
                final Box2DComponent b2dCmp = b2dCmpMapper.get(player);
                if (b2dCmp.body.getLinearVelocity().y > 0) {
                    final Vector2 worldCenter = b2dCmp.body.getWorldCenter();
                    b2dCmp.body.applyLinearImpulse(0, -b2dCmp.body.getLinearVelocity().y * b2dCmp.body.getMass(), worldCenter.x, worldCenter.y, true);
                }
            }
            return true;
        }
        return false;
    }
}
