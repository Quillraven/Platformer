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
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.utils.Array;
import com.quillraven.platformer.SoundManager;
import com.quillraven.platformer.WorldContactManager;
import com.quillraven.platformer.ecs.EntityEngine;
import com.quillraven.platformer.ecs.component.Box2DComponent;
import com.quillraven.platformer.ecs.component.GameObjectComponent;
import com.quillraven.platformer.ecs.component.PlayerComponent;
import com.quillraven.platformer.ecs.component.RemoveComponent;

import static com.quillraven.platformer.Platformer.PPM;

/**
 * TODO add class description
 */

public class GameObjectCollisionSystem extends IteratingSystem implements WorldContactManager.GameContactListener {
    private static final String TAG = GameObjectCollisionSystem.class.getSimpleName();

    private final ComponentMapper<PlayerComponent> playerCmpMapper;
    private final ComponentMapper<GameObjectComponent> gameObjCmpMapper;
    private final ComponentMapper<RemoveComponent> removeCmpMapper;
    private final ComponentMapper<Box2DComponent> b2dCmpMapper;
    private final Array<GameObjectListener> gameObjectListeners;

    public GameObjectCollisionSystem() {
        super(Family.all(GameObjectComponent.class).get());
        this.gameObjectListeners = new Array<>();
        WorldContactManager.getInstance().addGameContactListener(this);
        this.playerCmpMapper = ComponentMapper.getFor(PlayerComponent.class);
        this.gameObjCmpMapper = ComponentMapper.getFor(GameObjectComponent.class);
        this.b2dCmpMapper = ComponentMapper.getFor(Box2DComponent.class);
        this.removeCmpMapper = ComponentMapper.getFor(RemoveComponent.class);
    }

    public void addGameObjectListener(final GameObjectListener listener) {
        this.gameObjectListeners.add(listener);
    }

    public void removeGameObjectListener(final GameObjectListener listener) {
        this.gameObjectListeners.removeValue(listener, true);
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

        final TiledMapTileMapObject mapObj = gameObjCmpMapper.get(object).mapObject;
        if (objectUserData == null) {
            Gdx.app.error(TAG, "Game object at " + mapObj.getX() / PPM + "/" + mapObj.getY() / PPM + " does not have a userData");
            return;
        }

        if ("coin".equals(objectUserData)) {
            ++playerCmpMapper.get(player).coinsCollected;
            mapObj.setVisible(false);
            object.add(((EntityEngine) this.getEngine()).createComponent(RemoveComponent.class));
            SoundManager.getInstance().playSound(SoundManager.SoundType.SFX_COIN);
            for (final GameObjectListener listener : gameObjectListeners) {
                listener.onCoinPickup(playerCmpMapper.get(player).coinsCollected);
            }
        } else if (objectUserData.startsWith("Info")) {
            final Box2DComponent b2dCmp = b2dCmpMapper.get(object);
            gameObjCmpMapper.get(object).sleepTime = 5f;
            for (final GameObjectListener listener : gameObjectListeners) {
                listener.onInfoBoxActivation(b2dCmp.body.getPosition().x, b2dCmp.body.getPosition().y, objectUserData);
            }
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

    public interface GameObjectListener {
        void onCoinPickup(final int numCoinsCollected);

        void onInfoBoxActivation(final float x, final float y, final String infoBoxID);
    }
}
