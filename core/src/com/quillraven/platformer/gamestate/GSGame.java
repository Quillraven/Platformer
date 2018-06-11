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
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.quillraven.platformer.Platformer;
import com.quillraven.platformer.WorldContactManager;
import com.quillraven.platformer.ecs.EntityEngine;
import com.quillraven.platformer.ecs.component.AnimationComponent;
import com.quillraven.platformer.map.MapManager;
import com.quillraven.platformer.ui.GameHUD;

import static com.quillraven.platformer.Platformer.PPM;

/**
 * TODO add class description
 */

public class GSGame extends GameState<GameHUD> {
    private final World world;
    private final EntityEngine entityEngine;
    private Entity player;
    private final Viewport gameViewport;
    private final OrthographicCamera gameCamera;

    public GSGame(final AssetManager assetManager, final GameHUD hud, final SpriteBatch spriteBatch) {
        super(assetManager, hud, spriteBatch);

        this.gameViewport = new FitViewport(Platformer.V_WIDTH / PPM, Platformer.V_HEIGHT / PPM);
        this.gameCamera = (OrthographicCamera) gameViewport.getCamera();

        // init box2d
        Box2D.init();
        this.world = new World(new Vector2(0, -70), true);
        world.setContactListener(WorldContactManager.getInstance());

        // init ashley entity component system
        entityEngine = new EntityEngine(world, spriteBatch);
    }

    @Override
    public void onActivation() {
        if (MapManager.getInstance().changeMap(assetManager, MapManager.MapType.TEST, world, entityEngine)) {
            // create player
            final short maskBits = Platformer.BIT_GROUND | Platformer.BIT_OBJECT;
            player = entityEngine.createEntity(world, BodyDef.BodyType.DynamicBody, maskBits, Platformer.BIT_PLAYER, 77, 150, 72, 96);
            final AnimationComponent aniCmp = entityEngine.getAnimationComponent(player);
            aniCmp.texture = new Sprite(assetManager.get("characters/slimeDead.png", Texture.class));
            aniCmp.width = 72;
            aniCmp.height = 96;
        } else {
            assetManager.load("characters/slimeDead.png", Texture.class);
        }
    }

    @Override
    public void onDeactivation() {
    }

    @Override
    public void onUpdate(final GameStateManager gsManager, final float fixedTimeStep) {
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

}
