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
import com.quillraven.platformer.ecs.component.Box2DComponent;
import com.quillraven.platformer.ecs.component.EnemyComponent;
import com.quillraven.platformer.ecs.component.MoveComponent;
import com.quillraven.platformer.ecs.component.RemoveComponent;

/**
 * TODO add class description
 */
public class AISystem extends IteratingSystem {
    private final ComponentMapper<EnemyComponent> enemyCmpMapper;
    private final ComponentMapper<Box2DComponent> box2DComponentComponentMapper;
    private final ComponentMapper<MoveComponent> moveCmpMapper;
    private final ComponentMapper<RemoveComponent> removeCmpMapper;

    public AISystem(final ComponentMapper<Box2DComponent> box2DComponentComponentMapper, final ComponentMapper<MoveComponent> moveCmpMapper) {
        super(Family.all(EnemyComponent.class).get());
        this.enemyCmpMapper = ComponentMapper.getFor(EnemyComponent.class);
        this.box2DComponentComponentMapper = box2DComponentComponentMapper;
        this.moveCmpMapper = moveCmpMapper;
        this.removeCmpMapper = ComponentMapper.getFor(RemoveComponent.class);
    }

    @Override
    protected void processEntity(final Entity entity, final float deltaTime) {
        final RemoveComponent removeCmp = removeCmpMapper.get(entity);
        final MoveComponent moveCmp = moveCmpMapper.get(entity);
        if (removeCmp != null) {
            moveCmp.speed = 0;
            return;
        }

        final EnemyComponent enemyCmp = enemyCmpMapper.get(entity);
        final Box2DComponent b2dCmp = box2DComponentComponentMapper.get(entity);
        if (moveCmp.speed == 0) {
            // initialize enemies by moving left
            moveCmp.speed = -moveCmp.maxSpeed;
        }

        if (enemyCmp.spawnX - b2dCmp.body.getPosition().x >= 1) {
            moveCmp.speed = moveCmp.maxSpeed;
        } else if (enemyCmp.spawnX - b2dCmp.body.getPosition().x <= -1) {
            moveCmp.speed = -moveCmp.maxSpeed;
        }
    }
}
