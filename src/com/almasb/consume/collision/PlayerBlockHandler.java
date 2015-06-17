package com.almasb.consume.collision;

import com.almasb.consume.Types.Block;
import com.almasb.consume.Types.Property;
import com.almasb.fxgl.entity.CollisionHandler;
import com.almasb.fxgl.entity.Entity;

public class PlayerBlockHandler implements CollisionHandler {

    @Override
    public void onCollision(Entity player, Entity block) {
        if (block.getProperty(Property.SUB_TYPE) == Block.BARRIER) {
            block.setProperty("state", "passing");

            if ("none".equals(block.getProperty("start"))) {
                if (player.getTranslateX() <= block.getTranslateX()) {
                    block.setProperty("start", "left");
                }
                else {
                    block.setProperty("start", "right");
                }
            }
        }
    }
}
