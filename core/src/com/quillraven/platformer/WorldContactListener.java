package com.quillraven.platformer;
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
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.quillraven.platformer.gamestate.GSGame;

/**
 * TODO add class description
 */

public class WorldContactListener implements ContactListener {
    private final GSGame gsGame;

    public WorldContactListener(final GSGame gsGame) {
        this.gsGame = gsGame;
    }

    @Override
    public void beginContact(final Contact contact) {
        final Fixture fixtureA = contact.getFixtureA();
        final Fixture fixtureB = contact.getFixtureB();

        if ("foot".equals(fixtureA.getUserData()) && fixtureB.getFilterData().categoryBits == Platformer.BIT_GROUND) {
            gsGame.onGroundCollision((Entity) fixtureA.getBody().getUserData());
        } else if ("foot".equals(fixtureB.getUserData()) && fixtureA.getFilterData().categoryBits == Platformer.BIT_GROUND) {
            gsGame.onGroundCollision((Entity) fixtureB.getBody().getUserData());
        }
    }

    @Override
    public void endContact(final Contact contact) {
        final Fixture fixtureA = contact.getFixtureA();
        final Fixture fixtureB = contact.getFixtureB();

        if ("foot".equals(fixtureA.getUserData()) && fixtureB.getFilterData().categoryBits == Platformer.BIT_GROUND) {
            gsGame.onLeaveGround((Entity) fixtureA.getBody().getUserData());
        } else if ("foot".equals(fixtureB.getUserData()) && fixtureA.getFilterData().categoryBits == Platformer.BIT_GROUND) {
            gsGame.onLeaveGround((Entity) fixtureB.getBody().getUserData());
        }
    }

    @Override
    public void preSolve(final Contact contact, final Manifold oldManifold) {

    }

    @Override
    public void postSolve(final Contact contact, final ContactImpulse impulse) {

    }
}
