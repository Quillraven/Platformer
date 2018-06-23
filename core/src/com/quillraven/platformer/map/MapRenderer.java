package com.quillraven.platformer.map;
/*
 * Created by Quillraven on 16.06.2018.
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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;

import static com.badlogic.gdx.graphics.g2d.Batch.C1;
import static com.badlogic.gdx.graphics.g2d.Batch.C2;
import static com.badlogic.gdx.graphics.g2d.Batch.C3;
import static com.badlogic.gdx.graphics.g2d.Batch.C4;
import static com.badlogic.gdx.graphics.g2d.Batch.U1;
import static com.badlogic.gdx.graphics.g2d.Batch.U2;
import static com.badlogic.gdx.graphics.g2d.Batch.U3;
import static com.badlogic.gdx.graphics.g2d.Batch.U4;
import static com.badlogic.gdx.graphics.g2d.Batch.V1;
import static com.badlogic.gdx.graphics.g2d.Batch.V2;
import static com.badlogic.gdx.graphics.g2d.Batch.V3;
import static com.badlogic.gdx.graphics.g2d.Batch.V4;
import static com.badlogic.gdx.graphics.g2d.Batch.X1;
import static com.badlogic.gdx.graphics.g2d.Batch.X2;
import static com.badlogic.gdx.graphics.g2d.Batch.X3;
import static com.badlogic.gdx.graphics.g2d.Batch.X4;
import static com.badlogic.gdx.graphics.g2d.Batch.Y1;
import static com.badlogic.gdx.graphics.g2d.Batch.Y2;
import static com.badlogic.gdx.graphics.g2d.Batch.Y3;
import static com.badlogic.gdx.graphics.g2d.Batch.Y4;
import static com.quillraven.platformer.Platformer.PPM;

/**
 * TODO add class description
 */

public class MapRenderer extends OrthogonalTiledMapRenderer {
    public MapRenderer(final SpriteBatch spriteBatch) {
        super(null, 1 / PPM, spriteBatch);
    }

    @Override
    public void render(final int[] layers) {
        AnimatedTiledMapTile.updateAnimationBaseTime();
        for (int layerIdx : layers) {
            MapLayer layer = map.getLayers().get(layerIdx);
            renderMapLayer(layer);
        }
    }

    @Override
    public void renderObject(final MapObject object) {
        if (object instanceof TiledMapTileMapObject) {
            if (!object.isVisible()) {
                return;
            }

            final TiledMapTileMapObject tileMapObj = (TiledMapTileMapObject) object;
            final TiledMapTile tile = tileMapObj.getTile();

            if (tile != null) {
                final Color batchColor = batch.getColor();
                final float color = Color.toFloatBits(batchColor.r, batchColor.g, batchColor.b, batchColor.a * tileMapObj.getOpacity());
                final TextureRegion region = tile.getTextureRegion();

                final float x1 = tileMapObj.getX() * unitScale + tile.getOffsetX() * unitScale;
                final float y1 = tileMapObj.getY() * unitScale + tile.getOffsetY() * unitScale;
                final float x2 = x1 + region.getRegionWidth() * unitScale;
                final float y2 = y1 + region.getRegionHeight() * unitScale;

                final float u1 = region.getU();
                final float v1 = region.getV2();
                final float u2 = region.getU2();
                final float v2 = region.getV();
                // bot-left
                vertices[X1] = x1;
                vertices[Y1] = y1;
                vertices[C1] = color;
                vertices[U1] = u1;
                vertices[V1] = v1;
                // top-left
                vertices[X2] = x1;
                vertices[Y2] = y2;
                vertices[C2] = color;
                vertices[U2] = u1;
                vertices[V2] = v2;
                // top-right
                vertices[X3] = x2;
                vertices[Y3] = y2;
                vertices[C3] = color;
                vertices[U3] = u2;
                vertices[V3] = v2;
                // bot-right
                vertices[X4] = x2;
                vertices[Y4] = y1;
                vertices[C4] = color;
                vertices[U4] = u2;
                vertices[V4] = v1;

                batch.draw(region.getTexture(), vertices, 0, NUM_VERTICES);
            }
        }
    }
}
