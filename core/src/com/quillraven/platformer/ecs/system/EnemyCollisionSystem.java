package com.quillraven.platformer.ecs.system;
/*
 * Created by Quillraven on 24.06.2018.
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
import com.badlogic.gdx.Gdx;
import com.quillraven.platformer.SoundManager;
import com.quillraven.platformer.WorldContactManager;
import com.quillraven.platformer.ecs.EntityEngine;
import com.quillraven.platformer.ecs.component.AnimationComponent;
import com.quillraven.platformer.ecs.component.Box2DComponent;
import com.quillraven.platformer.ecs.component.EnemyComponent;
import com.quillraven.platformer.ecs.component.PlayerComponent;
import com.quillraven.platformer.ecs.component.RemoveComponent;
import com.quillraven.platformer.ui.AnimationManager;

/**
 * TODO add class description
 */
public class EnemyCollisionSystem extends IteratingSystem implements WorldContactManager.GameContactListener {
    private static final String TAG = EnemyCollisionSystem.class.getSimpleName();
    private final ComponentMapper<RemoveComponent> removeComponentComponentMapper;
    private final ComponentMapper<Box2DComponent> b2dCmpMapper;
    private boolean killPlayer;

    public EnemyCollisionSystem() {
        super(Family.one(EnemyComponent.class, PlayerComponent.class).get());
        WorldContactManager.getInstance().addGameContactListener(this);
        removeComponentComponentMapper = ComponentMapper.getFor(RemoveComponent.class);
        b2dCmpMapper = ComponentMapper.getFor(Box2DComponent.class);
        killPlayer = false;
    }

    @Override
    protected void processEntity(final Entity entity, final float deltaTime) {
        final RemoveComponent removeCmp = removeComponentComponentMapper.get(entity);
        if (removeCmp != null) {
            removeCmp.delay -= deltaTime;
            if (removeCmp.delay <= 0) {
                this.getEngine().removeEntity(entity);
            }
        } else if (killPlayer) {
            killPlayer = false;
            final Entity player = ((EntityEngine) this.getEngine()).getPlayer();
            if (player != null) {
                Gdx.app.debug(TAG, "Kill player!");
                final Box2DComponent b2dCmpPlayer = b2dCmpMapper.get(player);
                b2dCmpPlayer.body.setTransform(b2dCmpPlayer.body.getPosition().x, -2, 0);
            }
        }
    }

    @Override
    public void onBeginGroundContact(final Entity entity, final String userData) {

    }

    @Override
    public void onEndGroundContact(final Entity entity, final String userData) {

    }

    @Override
    public void onBeginObjectContact(final Entity player, final Entity object, final String objectUserData) {

    }

    @Override
    public void onEndObjectContact(final Entity player, final Entity object, final String objectUserData) {

    }

    @Override
    public void onBeginEnemyContact(final Entity player, final Entity enemy, final boolean killEnemy) {
        if (killEnemy) {
            SoundManager.getInstance().playSound(SoundManager.SoundType.SFX_DEATH);
            final RemoveComponent removeCmp = ((EntityEngine) this.getEngine()).createComponent(RemoveComponent.class);
            removeCmp.delay = 0.25f;
            enemy.add(removeCmp);
            final AnimationComponent aniCmp = enemy.getComponent(AnimationComponent.class);
            if (aniCmp.aniType == AnimationManager.AnimationType.SLIME_WALK) {
                aniCmp.aniType = AnimationManager.AnimationType.SLIME_DEAD;
            } else {
                aniCmp.aniType = AnimationManager.AnimationType.FLY_DEAD;
            }
        } else {
            killPlayer = true;
        }
    }

    @Override
    public void onEndEnemyContact(final Entity player, final Entity enemy, final boolean killEnemy) {

    }
}
