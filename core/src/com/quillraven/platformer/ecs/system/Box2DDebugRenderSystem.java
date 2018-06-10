package com.quillraven.platformer.ecs.system;
/*
 * Created by Quillraven on 10.06.2018.
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

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.quillraven.platformer.ecs.EntityEngine;

/**
 * TODO add class description
 */
public class Box2DDebugRenderSystem extends RenderSystem {
    private final Box2DDebugRenderer box2DRenderer;
    private final World world;

    public Box2DDebugRenderSystem(final EntityEngine engine, final World world) {
        super(engine);
        this.box2DRenderer = new Box2DDebugRenderer();
        this.world = world;
    }

    @Override
    public void onRender(final SpriteBatch spriteBatch, final Camera camera, final float alpha) {
        box2DRenderer.render(world, camera.combined);
    }

    @Override
    public void onDispose() {
        box2DRenderer.dispose();
    }
}
