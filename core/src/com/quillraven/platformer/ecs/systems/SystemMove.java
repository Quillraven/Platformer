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
import com.quillraven.platformer.ecs.components.ComponentBox2D;
import com.quillraven.platformer.ecs.components.ComponentMove;

/**
 * TODO add class description
 */
public class SystemMove extends IteratingSystem {
    private final ComponentMapper<ComponentBox2D> b2dCmpMapper;
    private final ComponentMapper<ComponentMove> moveCmpMapper;

    public SystemMove(final ComponentMapper<ComponentBox2D> b2dCmpMapper, final ComponentMapper<ComponentMove> moveCmpMapper) {
        super(Family.all(ComponentBox2D.class, ComponentMove.class).get());

        this.b2dCmpMapper = b2dCmpMapper;
        this.moveCmpMapper = moveCmpMapper;
    }

    @Override
    protected void processEntity(final Entity entity, final float deltaTime) {
        final ComponentMove moveCmp = moveCmpMapper.get(entity);
        final ComponentBox2D b2dCmp = b2dCmpMapper.get(entity);
        final Vector2 worldCenter = b2dCmp.body.getWorldCenter();

        // cap movement speed by min/max
        moveCmp.speed = Math.max(-moveCmp.maxSpeed, Math.min(moveCmp.maxSpeed, moveCmp.speed));
        // apply force to box2d body
        b2dCmp.body.applyLinearImpulse((moveCmp.speed - b2dCmp.body.getLinearVelocity().x) * b2dCmp.body.getMass(), 0, worldCenter.x, worldCenter.y, true);
    }
}
