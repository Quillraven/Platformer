package com.quillraven.platformer.ecs.system;
/*
 * Created by Quillraven on 17.06.2018.
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
import com.quillraven.platformer.ecs.component.AnimationComponent;
import com.quillraven.platformer.ecs.component.Box2DComponent;
import com.quillraven.platformer.ui.AnimationManager;

/**
 * TODO add class description
 */

public class AnimationSystem extends IteratingSystem {
    private final ComponentMapper<AnimationComponent> aniCmpMapper;
    private final ComponentMapper<Box2DComponent> b2dCmpMapper;

    public AnimationSystem() {
        super(Family.all(AnimationComponent.class, Box2DComponent.class).get());
        this.aniCmpMapper = ComponentMapper.getFor(AnimationComponent.class);
        this.b2dCmpMapper = ComponentMapper.getFor(Box2DComponent.class);
    }

    @Override
    protected void processEntity(final Entity entity, final float deltaTime) {
        final Box2DComponent b2dCmp = b2dCmpMapper.get(entity);
        final AnimationComponent aniCmp = aniCmpMapper.get(entity);
        final Vector2 velocity = b2dCmp.body.getLinearVelocity();

        aniCmp.animationTime += deltaTime;
        if (velocity.y >= 5 || velocity.y < 0) {
            changeAnimation(aniCmp, AnimationManager.AnimationType.PLAYER_JUMP);
        } else if (velocity.x == 0) {
            changeAnimation(aniCmp, AnimationManager.AnimationType.PLAYER_IDLE);
        } else {
            changeAnimation(aniCmp, AnimationManager.AnimationType.PLAYER_WALK);
        }
        aniCmp.flipHoricontal = velocity.x < 0;
    }

    private void changeAnimation(final AnimationComponent aniCmp, final AnimationManager.AnimationType newAniType) {
        if (aniCmp.aniType != newAniType) {
            aniCmp.animationTime = 0;
        }
        aniCmp.aniType = newAniType;
    }
}
