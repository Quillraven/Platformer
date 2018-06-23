package com.quillraven.platformer.ecs.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

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
public class Box2DComponent implements Component, Pool.Poolable {
    public final Array<Entity> contacts = new Array<>();
    public Body body;
    public int numGroundContactsLeft;
    public int numGroundContactsRight;
    public final Vector2 positionBeforeUpdate = new Vector2(0, 0);
    public float width;
    public float height;

    @Override
    public void reset() {
        if (body != null) {
            body.setUserData(null);
            for (final Fixture fix : body.getFixtureList()) {
                fix.setUserData(null);
            }
            body.getWorld().destroyBody(body);
            body = null;
        }
        numGroundContactsLeft = 0;
        numGroundContactsRight = 0;
        contacts.clear();
        positionBeforeUpdate.set(0, 0);
        this.width = 0;
        this.height = 0;
    }
}
