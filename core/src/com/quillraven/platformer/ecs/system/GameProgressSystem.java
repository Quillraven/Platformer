package com.quillraven.platformer.ecs.system;
/*
 * Created by Quillraven on 23.06.2018.
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
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.utils.Array;
import com.quillraven.platformer.SoundManager;
import com.quillraven.platformer.ecs.component.Box2DComponent;
import com.quillraven.platformer.ecs.component.PlayerComponent;
import com.quillraven.platformer.map.Map;
import com.quillraven.platformer.map.MapManager;

import static com.quillraven.platformer.Platformer.PPM;

/**
 * TODO add class description
 */
public class GameProgressSystem extends IteratingSystem implements MapManager.MapListener {
    private static final String TAG = GameProgressSystem.class.getSimpleName();
    private final ComponentMapper<Box2DComponent> b2dCmpMapper;
    private final ComponentMapper<PlayerComponent> playerCmpMapper;
    private final Array<GameProgressListener> listeners;
    private float mapWidth;

    public GameProgressSystem(final ComponentMapper<Box2DComponent> b2dCmpMapper, final ComponentMapper<PlayerComponent> playerCmpMapper) {
        super(Family.all(PlayerComponent.class, Box2DComponent.class).get());
        MapManager.getInstance().addMapListener(this);
        this.listeners = new Array<>();
        this.b2dCmpMapper = b2dCmpMapper;
        this.playerCmpMapper = playerCmpMapper;
        this.mapWidth = 1000;
    }

    public void addGameProgressListener(final GameProgressListener listener) {
        listeners.add(listener);
    }

    public void removeGameProgressListener(final GameProgressListener listener) {
        listeners.removeValue(listener, true);
    }

    @Override
    protected void processEntity(final Entity entity, final float deltaTime) {
        final Box2DComponent b2dCmp = b2dCmpMapper.get(entity);
        final PlayerComponent playerCmp = playerCmpMapper.get(entity);

        if (b2dCmp.body.getPosition().y < -1 || b2dCmp.body.getPosition().x < -1) {
            // player fall down or left level on left site --> reduce life and respawn
            --playerCmp.currentLife;
            SoundManager.getInstance().playSound(SoundManager.SoundType.SFX_DEATH);
            for (final GameProgressListener listener : listeners) {
                listener.onPlayerDeath(playerCmp.currentLife, playerCmp.maxLife);
            }
            Gdx.app.debug(TAG, "Kill player!");
            b2dCmp.body.setTransform(MapManager.getInstance().getCurrentMap().getStartX() / PPM, MapManager.getInstance().getCurrentMap().getStartY() / PPM, 0);
        } else if (b2dCmp.body.getPosition().x > mapWidth) {
            // level completed
            playerCmp.coinsCollected = 0;
            for (final GameProgressListener listener : listeners) {
                listener.onLevelCompletion(MapManager.getInstance().getCurrentMap());
            }
        }
    }

    @Override
    public void onMapChanged(final Map map, final TiledMap tiledMap) {
        mapWidth = map.getWidth();
    }

    public interface GameProgressListener {
        void onPlayerDeath(final int remainingLife, final int maxLife);

        void onLevelCompletion(final Map map);
    }
}
