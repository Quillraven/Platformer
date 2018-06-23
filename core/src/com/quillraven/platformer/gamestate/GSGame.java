package com.quillraven.platformer.gamestate;
/*
 * Created by Quillraven on 04.06.2018.
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

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.quillraven.platformer.GameInputManager;
import com.quillraven.platformer.Platformer;
import com.quillraven.platformer.SoundManager;
import com.quillraven.platformer.WorldContactManager;
import com.quillraven.platformer.ecs.EntityEngine;
import com.quillraven.platformer.ecs.component.PlayerComponent;
import com.quillraven.platformer.ecs.system.GameObjectCollisionSystem;
import com.quillraven.platformer.ecs.system.JumpSystem;
import com.quillraven.platformer.ecs.system.MoveSystem;
import com.quillraven.platformer.map.Map;
import com.quillraven.platformer.map.MapManager;
import com.quillraven.platformer.ui.AnimationManager;
import com.quillraven.platformer.ui.GameHUD;

import static com.quillraven.platformer.Platformer.PPM;

/**
 * TODO add class description
 */

public class GSGame extends GameState<GameHUD> implements MapManager.MapListener, GameObjectCollisionSystem.GameObjectListener, GameInputManager.GameKeyListener {
    private final World world;
    private final EntityEngine entityEngine;
    private Entity player;
    private final Viewport gameViewport;
    private final OrthographicCamera gameCamera;
    private int maxCoins;
    private boolean showMenu;

    public GSGame(final AssetManager assetManager, final GameHUD hud, final SpriteBatch spriteBatch) {
        super(assetManager, hud, spriteBatch);
        MapManager.getInstance().addMapListener(this);
        showMenu = false;

        this.gameViewport = new FitViewport(Platformer.V_WIDTH / PPM, Platformer.V_HEIGHT / PPM);
        this.gameCamera = (OrthographicCamera) gameViewport.getCamera();

        // init box2d
        Box2D.init();
        this.world = new World(new Vector2(0, -PPM), true);
        world.setContactListener(WorldContactManager.getInstance());

        // init ashley entity component system
        entityEngine = new EntityEngine(world, spriteBatch);
        entityEngine.getSystem(GameObjectCollisionSystem.class).addGameObjectListener(this);
    }

    @Override
    public void onActivation() {
        AnimationManager.getInstance().loadAnimations(assetManager);
        SoundManager.getInstance().loadSounds(assetManager);
        if (MapManager.getInstance().changeMap(assetManager, MapManager.MapType.LEVEL_1, world, entityEngine)) {
            GameInputManager.getInstance().addGameKeyListener(entityEngine.getSystem(MoveSystem.class));
            GameInputManager.getInstance().addGameKeyListener(entityEngine.getSystem(JumpSystem.class));
            GameInputManager.getInstance().addGameKeyListener(this);

            // create player
            final short maskBits = Platformer.BIT_GROUND | Platformer.BIT_OBJECT;
            player = entityEngine.createPlayer(world, BodyDef.BodyType.DynamicBody, maskBits, Platformer.BIT_PLAYER, MapManager.getInstance().getCurrentMap().getStartX(), MapManager.getInstance().getCurrentMap().getStartY(), 48, 64);
            hud.updateLifeInfo(player.getComponent(PlayerComponent.class).currentLife, player.getComponent(PlayerComponent.class).maxLife);
        }
    }

    @Override
    public void onDeactivation() {
        GameInputManager.getInstance().removeGameKeyListener(entityEngine.getSystem(MoveSystem.class));
        GameInputManager.getInstance().removeGameKeyListener(entityEngine.getSystem(JumpSystem.class));
        GameInputManager.getInstance().removeGameKeyListener(this);
    }

    @Override
    public void onUpdate(final GameStateManager gsManager, final float fixedTimeStep) {
        if (showMenu) {
            gsManager.pushState(GameStateManager.GameStateType.MENU);
            showMenu = false;
            return;
        }

        // important to update entity engine before updating the box2d world in order to store
        // the body position BEFORE the step in some components.
        // This is f.e. needed to interpolate the rendering
        entityEngine.update(fixedTimeStep);
        world.step(fixedTimeStep, 6, 2);

        super.onUpdate(gsManager, fixedTimeStep);
    }

    @Override
    public void onRender(final SpriteBatch spriteBatch, final float alpha) {
        gameViewport.apply();
        entityEngine.onRender(spriteBatch, gameCamera, alpha);
        super.onRender(spriteBatch, alpha);
    }

    @Override
    public void onDispose() {
        world.dispose();
        entityEngine.dispose();
        super.onDispose();
    }

    @Override
    public void onResize(final int width, final int height) {
        super.onResize(width, height);
        gameViewport.update(width, height);
    }

    @Override
    public void onMapChanged(final Map map, final TiledMap tiledMap) {
        hud.setLevelName(map.getName());
        this.maxCoins = map.getMaxCoins();
        hud.updateCoinInfo(0, maxCoins);
    }

    @Override
    public void onCoinPickup(final int numCoinsCollected) {
        hud.updateCoinInfo(numCoinsCollected, maxCoins);

        if (numCoinsCollected >= maxCoins) {
            SoundManager.getInstance().playSound(SoundManager.SoundType.SFX_ALL_COINS);
            // stop flag pole at the end of the level
            // flag pole has 3 animation frames; set the time for the first 2 frames to negative to skip them
            final Map currentMap = MapManager.getInstance().getCurrentMap();
            final AnimatedTiledMapTile flagTile = (AnimatedTiledMapTile) currentMap.getCoinFlagObject().getTile();
            final int[] intervals = flagTile.getAnimationIntervals();
            intervals[0] = -1;
            intervals[1] = -1;
            intervals[2] = 150;
            flagTile.setAnimationIntervals(intervals);
        }
    }

    @Override
    public void onInfoBoxActivation(final float x, final float y, final String infoBoxID) {
        hud.showInfoMessage(x, y, infoBoxID);
    }

    @Override
    public boolean onKeyPressed(final GameInputManager.GameKeys key) {
        if (key == GameInputManager.GameKeys.EXIT) {
            showMenu = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean onKeyReleased(final GameInputManager.GameKeys key) {
        return false;
    }
}
