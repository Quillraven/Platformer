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
import com.quillraven.platformer.WorldContactManager;
import com.quillraven.platformer.ecs.EntityEngine;
import com.quillraven.platformer.ecs.component.GameObjectComponent;
import com.quillraven.platformer.ecs.component.RemoveComponent;

/**
 * TODO add class description
 */

public class GameObjectCollisionSystem extends IteratingSystem implements WorldContactManager.GameContactListener {
    private final ComponentMapper<GameObjectComponent> gameObjCmpMapper;
    private final ComponentMapper<RemoveComponent> removeCmpMapper;

    public GameObjectCollisionSystem() {
        super(Family.all(GameObjectComponent.class).get());
        WorldContactManager.getInstance().addGameContactListener(this);
        this.gameObjCmpMapper = ComponentMapper.getFor(GameObjectComponent.class);
        this.removeCmpMapper = ComponentMapper.getFor(RemoveComponent.class);
    }

    @Override
    public void onBeginGroundContact(final Entity entity, final String userData) {

    }

    @Override
    public void onEndGroundContact(final Entity entity, final String userData) {

    }

    @Override
    public void onBeginObjectContact(final Entity player, final Entity object, final String objectUserData) {
        if (removeCmpMapper.get(object) != null || gameObjCmpMapper.get(object).sleepTime > 0) {
            // object will be removed the next frame or is not ready yet to be collected -> do not process
            return;
        }
        if ("coin".equals(objectUserData)) {
            gameObjCmpMapper.get(object).mapObject.setTile(null);
            object.add(((EntityEngine) this.getEngine()).createComponent(RemoveComponent.class));
        } else if (objectUserData.startsWith("Info")) {
            gameObjCmpMapper.get(object).sleepTime = 2f;
        }
    }

    @Override
    public void onEndObjectContact(final Entity player, final Entity object, final String objectUserData) {

    }

    @Override
    protected void processEntity(final Entity entity, final float deltaTime) {
        if (removeCmpMapper.get(entity) == null) {
            final GameObjectComponent gameObjCmp = gameObjCmpMapper.get(entity);
            gameObjCmp.sleepTime = Math.max(0, gameObjCmp.sleepTime - deltaTime);
        } else {
            this.getEngine().removeEntity(entity);
        }
    }
}
