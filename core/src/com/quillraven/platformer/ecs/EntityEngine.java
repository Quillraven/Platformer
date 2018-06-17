package com.quillraven.platformer.ecs;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.quillraven.platformer.Platformer;
import com.quillraven.platformer.ecs.component.AnimationComponent;
import com.quillraven.platformer.ecs.component.Box2DComponent;
import com.quillraven.platformer.ecs.component.GameObjectComponent;
import com.quillraven.platformer.ecs.component.JumpComponent;
import com.quillraven.platformer.ecs.component.MoveComponent;
import com.quillraven.platformer.ecs.component.PlayerComponent;
import com.quillraven.platformer.ecs.system.AnimationSystem;
import com.quillraven.platformer.ecs.system.GameObjectCollisionSystem;
import com.quillraven.platformer.ecs.system.GameRenderSystem;
import com.quillraven.platformer.ecs.system.JumpSystem;
import com.quillraven.platformer.ecs.system.MoveSystem;
import com.quillraven.platformer.ecs.system.RenderSystem;
import com.quillraven.platformer.ui.AnimationManager;

import static com.quillraven.platformer.Platformer.PPM;

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
public class EntityEngine extends PooledEngine {
    private final BodyDef bodyDef;
    private final FixtureDef fixtureDef;
    private final ComponentMapper<Box2DComponent> b2dCmpMapper;
    private final Family b2dFamily;
    private final ComponentMapper<AnimationComponent> aniCmpMapper;
    private final Family animationFamily;
    private final Family playerFamily;
    private Entity player;
    private final Array<RenderSystem> renderSystems;

    public EntityEngine(final World world, final SpriteBatch spriteBatch) {
        super(20, 200, 10, 100);

        this.renderSystems = new Array<>();

        this.b2dFamily = Family.all(Box2DComponent.class).get();
        this.aniCmpMapper = ComponentMapper.getFor(AnimationComponent.class);
        this.animationFamily = Family.all(AnimationComponent.class).get();
        this.playerFamily = Family.all(PlayerComponent.class).get();

        // add systems
        // movement
        b2dCmpMapper = ComponentMapper.getFor(Box2DComponent.class);
        final ComponentMapper<MoveComponent> moveCmpMapper = ComponentMapper.getFor(MoveComponent.class);
        this.addSystem(new MoveSystem(b2dCmpMapper, moveCmpMapper));
        // jump
        final ComponentMapper<JumpComponent> jumpCmpMapper = ComponentMapper.getFor(JumpComponent.class);
        this.addSystem(new JumpSystem(b2dCmpMapper, jumpCmpMapper));
        // game object collision system
        this.addSystem(new GameObjectCollisionSystem());
        // animation system
        this.addSystem(new AnimationSystem());
        // box2d debug
        //renderSystems.add(new Box2DDebugRenderSystem(this, world));
        renderSystems.add(new GameRenderSystem(this, spriteBatch, b2dCmpMapper, aniCmpMapper));

        // create box2d definitions
        this.bodyDef = new BodyDef();
        this.fixtureDef = new FixtureDef();
    }

    public Entity getPlayer() {
        return player;
    }

    public Entity createPlayer(final World world, final BodyDef.BodyType bodyType, final short maskBits, final short categoryBits, final float x, final float y, final float width, final float height) {
        player = this.createEntity();

        // box2d component
        final Box2DComponent b2dCmp = this.createComponent(Box2DComponent.class);
        b2dCmp.width = width / PPM;
        b2dCmp.height = height / PPM;
        // body
        bodyDef.position.set(x / PPM, y / PPM);
        bodyDef.type = bodyType;
        b2dCmp.body = world.createBody(bodyDef);
        b2dCmp.positionBeforeUpdate.set(b2dCmp.body.getPosition());
        b2dCmp.body.setUserData(player);
        // hitbox
        fixtureDef.isSensor = false;
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width * 0.5f / PPM, height * 0.5f / PPM);
        fixtureDef.shape = shape;
        fixtureDef.filter.maskBits = maskBits;
        fixtureDef.filter.categoryBits = categoryBits;
        b2dCmp.body.createFixture(fixtureDef).setUserData("body");
        shape.dispose();
        player.add(b2dCmp);
        // foot sensor
        shape = new PolygonShape();
        shape.setAsBox(width * 0.33f / PPM, 15f / PPM, new Vector2(-width * 0.17f / PPM, -height * 0.5f / PPM), 0);
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;
        fixtureDef.filter.maskBits = Platformer.BIT_GROUND;
        fixtureDef.filter.categoryBits = categoryBits;
        b2dCmp.body.createFixture(fixtureDef).setUserData("foot-left");
        shape.dispose();
        shape = new PolygonShape();
        shape.setAsBox(width * 0.33f / PPM, 15f / PPM, new Vector2(width * 0.17f / PPM, -height * 0.5f / PPM), 0);
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;
        fixtureDef.filter.maskBits = Platformer.BIT_GROUND;
        fixtureDef.filter.categoryBits = categoryBits;
        b2dCmp.body.createFixture(fixtureDef).setUserData("foot-right");
        shape.dispose();

        // jump component
        final JumpComponent jumpCmp = this.createComponent(JumpComponent.class);
        jumpCmp.jumpSpeed = 22;
        player.add(jumpCmp);

        // move component
        final MoveComponent moveCmp = this.createComponent(MoveComponent.class);
        moveCmp.maxSpeed = 6;
        player.add(moveCmp);

        // animation component
        final AnimationComponent aniCmp = this.createComponent(AnimationComponent.class);
        aniCmp.aniType = AnimationManager.AnimationType.PLAYER_WALK;
        aniCmp.width = 72 / PPM;
        aniCmp.height = 96 / PPM;
        player.add(aniCmp);

        // player component
        final PlayerComponent playerCmp = this.createComponent(PlayerComponent.class);
        playerCmp.maxLife = 5;
        playerCmp.currentLife = playerCmp.maxLife;
        player.add(playerCmp);

        this.addEntity(player);
        return player;
    }

    public Entity createGameObj(final Body body, final TiledMapTileMapObject mapObj) {
        final Entity gameObj = this.createEntity();

        final Box2DComponent b2dCmp = this.createComponent(Box2DComponent.class);
        b2dCmp.body = body;
        b2dCmp.positionBeforeUpdate.set(body.getPosition());
        b2dCmp.body.setUserData(gameObj);
        gameObj.add(b2dCmp);

        final GameObjectComponent gameObjCmp = this.createComponent(GameObjectComponent.class);
        gameObjCmp.mapObject = mapObj;
        gameObj.add(gameObjCmp);

        this.addEntity(gameObj);
        return gameObj;
    }

    public void onRender(final SpriteBatch spriteBatch, final Camera camera, final float alpha) {
        for (final RenderSystem renderSystem : renderSystems) {
            renderSystem.onRender(spriteBatch, camera, alpha);
        }
    }

    public void dispose() {
        for (final RenderSystem renderSystem : renderSystems) {
            renderSystem.onDispose();
        }
    }
}
