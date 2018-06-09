package com.quillraven.platformer.ui;
/*
 * Created by Quillraven on 09.06.2018.
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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.quillraven.platformer.Platformer;
import com.quillraven.platformer.map.Map;
import com.quillraven.platformer.map.MapManager;

import static com.quillraven.platformer.Platformer.PPM;

/**
 * TODO add class description
 */
public class GameView extends View implements MapManager.MapListener {
    private final Viewport gameViewport;
    private final OrthographicCamera gameCamera;

    private final OrthogonalTiledMapRenderer mapRenderer;


    private final Array<RenderData> renderData;
    private int renderDataCalls;

    private Box2DDebugRenderer box2DRenderer;
    private World world;

    public GameView(final Skin skin, final SpriteBatch spriteBatch) {
        super(skin, spriteBatch);

        this.renderData = new Array<>();
        renderDataCalls = 0;

        this.gameViewport = new FitViewport(Platformer.V_WIDTH / PPM, Platformer.V_HEIGHT / PPM);
        this.gameCamera = (OrthographicCamera) gameViewport.getCamera();

        this.box2DRenderer = null;
        this.world = null;

        mapRenderer = new OrthogonalTiledMapRenderer(null, 1 / PPM, spriteBatch);
    }

    @Override
    Viewport getHudViewport() {
        return new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    public void debugBox2D(final World world) {
        box2DRenderer = new Box2DDebugRenderer();
        this.world = world;
    }

    @Override
    public void onRender(final SpriteBatch spriteBatch, final float alpha) {
        Gdx.gl.glClearColor(0.7f, 0.9f, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (mapRenderer.getMap() != null) {
            mapRenderer.setView(gameCamera);
            AnimatedTiledMapTile.updateAnimationBaseTime();
            spriteBatch.begin();
            for (final MapLayer layer : mapRenderer.getMap().getLayers()) {
                if (layer instanceof TiledMapTileLayer) {
                    mapRenderer.renderTileLayer((TiledMapTileLayer) layer);
                }
            }
            for (int i = 0; i < renderDataCalls; ++i) {
                final RenderData rd = this.renderData.get(i);
                spriteBatch.draw(rd.texture, rd.vertices, 0, 20);
            }
            renderDataCalls = 0;
            spriteBatch.end();
        }

        if (box2DRenderer != null) {
            box2DRenderer.render(world, gameCamera.combined);
        }

        hudViewport.apply();
        stage.draw();
    }

    public void setGameCameraPosition(final float x, final float y) {
        gameCamera.position.set(x, y, 0);
        gameCamera.update();
    }

    public float getGameViewportWidth() {
        return gameCamera.viewportWidth;
    }

    public float getGameViewportHeight() {
        return gameCamera.viewportHeight;
    }

    @Override
    public void onResize(final int width, final int height) {
        super.onResize(width, height);
        gameViewport.update(width, height);
    }

    @Override
    public void onDispose() {
        if (box2DRenderer != null) {
            box2DRenderer.dispose();
        }
        mapRenderer.dispose();
        super.onDispose();
    }

    @Override
    public void onMapChanged(final Map currentMap, final Map newMap) {
        mapRenderer.setMap(newMap.getTiledMap());
    }

    public void addVertices(final Texture texture, final float[] vertices, final int z) {
        if (renderData.size == renderDataCalls) {
            renderData.add(new RenderData(texture, vertices, z));
        } else {
            renderData.get(renderDataCalls).set(texture, vertices, z);
        }
        ++renderDataCalls;
    }
}
