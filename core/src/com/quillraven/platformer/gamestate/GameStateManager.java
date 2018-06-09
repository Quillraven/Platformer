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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.quillraven.platformer.GameInputListener;

/**
 * GameStateManager is responsible to manage the states of a game like a main menu, game, game over screen, etc..
 * Instead of using the {@link com.badlogic.gdx.Game} class and its {@link com.badlogic.gdx.Screen} functionality
 * we will use this implementation because it offers more flexibility and has a clean split between update and render.
 * <br>
 * It also offers a save way of changing states anywhere in the code because it takes care that a state is always changed
 * at the beginning of a new frame instead of somewhere in the middle that could mess up a lot of things.
 * <br>
 * The implementation is based on a stack but internally it uses a {@link Array} for the stack for better performance
 * and a {@link ObjectMap} to cache states.
 */

public class GameStateManager {
    private final static String TAG = GameStateManager.class.getSimpleName();

    private final SpriteBatch spriteBatch;
    private final AssetManager assetManager;

    private final ObjectMap<GameStateType, GameState> gameStateCache;
    private final Array<GameState> stateStack;

    private GameState nextGSToPush;
    private boolean popState;

    public GameStateManager(final GameStateType initialGS) {
        final FileHandleResolver resolver = new InternalFileHandleResolver();
        this.assetManager = new AssetManager();
        assetManager.setLoader(TiledMap.class, new TmxMapLoader(resolver));

        this.spriteBatch = new SpriteBatch();

        this.gameStateCache = new ObjectMap<>();
        this.stateStack = new Array<>();
        this.popState = false;

        Gdx.input.setInputProcessor(new GameInputListener(this));

        activateGameState(getState(initialGS));
    }

    // retrieve state instance by type enum and if it does not exist then create it
    private GameState getState(final GameStateType gsType) {
        GameState gameState = gameStateCache.get(gsType);
        if (gameState == null) {
            try {
                Gdx.app.debug(TAG, "Creating new gamestate " + gsType);
                gameState = gsType.gsClass.getConstructor(AssetManager.class).newInstance(assetManager);
                gameStateCache.put(gsType, gameState);
            } catch (Exception e) {
                Gdx.app.error(TAG, "Could not create gamestate " + gsType, e);
                throw new GdxRuntimeException("Could not create gamestate " + gsType);
            }
        }
        return gameState;
    }

    public boolean update(final float fixedTimeStep) {
        if (popState) {
            // pop current state
            Gdx.app.debug(TAG, "Popping current gamestate " + stateStack.peek().getClass().getSimpleName());
            if (stateStack.size > 0) {
                stateStack.pop().onDeactivation();
                if (stateStack.size > 0) {
                    stateStack.peek().onResize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                    stateStack.peek().onActivation();
                }
            }
            popState = false;
        }

        if (nextGSToPush != null) {
            // push next state
            activateGameState(nextGSToPush);
        }

        if (stateStack.size == 0) {
            // no more states to process -> exit game
            Gdx.app.debug(TAG, "No more gamestates left --> EXIT");
            return false;
        }

        if (assetManager.getProgress() != 1 && !(stateStack.peek() instanceof GSLoading)) {
            // assets need to be loaded -> change to loading state
            activateGameState(getState(GameStateType.LOADING));
        }

        stateStack.peek().onUpdate(this, fixedTimeStep);

        return true;
    }

    /**
     * Replaces the current game state with the given one
     *
     * @param gsType new active game state
     */
    public void setState(final GameStateType gsType) {
        popState = true;
        nextGSToPush = getState(gsType);
    }

    /**
     * Removes the current game state. If there are no states left then the game will be closed
     */
    public void popState() {
        popState = true;
        nextGSToPush = null;
    }

    /**
     * Adds an additional game state on top of the current one (f.e. a menu on top of the game)
     *
     * @param gsType game state to push
     */
    public void pushState(final GameStateType gsType) {
        popState = false;
        nextGSToPush = getState(gsType);
    }

    public boolean onKeyPressed(final GameInputListener inputListener, final GameInputListener.GameKeys key) {
        return stateStack.size > 0 && stateStack.peek().onKeyPressed(this, inputListener, key);
    }

    public boolean onKeyReleased(final GameInputListener inputListener, final GameInputListener.GameKeys key) {
        return stateStack.size > 0 && stateStack.peek().onKeyReleased(this, inputListener, key);
    }

    private void activateGameState(final GameState gameState) {
        Gdx.app.debug(TAG, "Pushing new gamestate " + gameState.getClass().getSimpleName());
        stateStack.add(gameState);
        // call resize in case gamestate was not active during window resize event
        gameState.onResize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        gameState.onActivation();
        nextGSToPush = null;
    }

    public void render(final float alpha) {
        stateStack.peek().onRender(spriteBatch, alpha);
    }

    public void dispose() {
        Gdx.app.debug(TAG, "Disposing all gamestates: " + gameStateCache.size);
        for (final GameState gs : gameStateCache.values()) {
            gs.onDeactivation();
            gs.onDispose();
        }
        spriteBatch.dispose();
        assetManager.dispose();
    }

    public void resize(final int width, final int height) {
        Gdx.app.debug(TAG, "Resizing gamestate to " + width + "x" + height);
        stateStack.peek().onResize(width, height);
    }

    public enum GameStateType {
        GAME(GSGame.class),
        LOADING(GSLoading.class);

        private final Class<? extends GameState> gsClass;

        GameStateType(final Class<? extends GameState> gsClass) {
            this.gsClass = gsClass;
        }
    }
}
