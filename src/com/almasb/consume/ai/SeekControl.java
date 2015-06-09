package com.almasb.consume.ai;

import com.almasb.fxgl.entity.Control;
import com.almasb.fxgl.entity.Entity;

public class SeekControl implements Control {

    private Entity target;

    public SeekControl(Entity target) {
        this.target = target;
    }

    @Override
    public void onUpdate(Entity entity, long now) {
        if (target.getPosition().distance(entity.getPosition()) <= 200) {
            if (Math.abs(target.getTranslateY() - entity.getTranslateY()) <= 10) {
                boolean right = target.getTranslateX() - entity.getTranslateX() > 0;
                entity.translate(right ? 5 : -5, 0);
            }
        }
    }
}
