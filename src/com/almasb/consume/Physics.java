package com.almasb.consume;

import com.almasb.consume.Types.Type;
import com.almasb.fxgl.GameApplication;
import com.almasb.fxgl.entity.Entity;

public class Physics {

    private GameApplication app;

    public Physics(GameApplication app) {
        this.app = app;
    }

    /**
     * Returns true iff entity has moved value units
     *
     * @param e
     * @param value
     * @return
     */
    public boolean moveX(Entity e, int value) {
        boolean movingRight = value > 0;

        for (int i = 0; i < Math.abs(value); i++) {
            for (Entity platform : app.getEntities(Type.PLATFORM)) {
                if (e.getBoundsInParent().intersects(platform.getBoundsInParent())) {
                    if (movingRight) {
                        if (e.getTranslateX() + e.getWidth() == platform.getTranslateX()) {
                            app.triggerCollision(e, platform);
                            e.translate(-1, 0);
                            return false;
                        }
                    }
                    else {
                        if (e.getTranslateX() == platform.getTranslateX() + platform.getWidth()) {
                            app.triggerCollision(e, platform);
                            e.translate(1, 0);
                            return false;
                        }
                    }
                }
            }
            e.setTranslateX(e.getTranslateX() + (movingRight ? 1 : -1));
        }

        return true;
    }

    public void moveY(Entity e, int value) {
        boolean movingDown = value > 0;

        for (int i = 0; i < Math.abs(value); i++) {
            for (Entity platform : app.getEntities(Type.PLATFORM)) {
                if (e.getBoundsInParent().intersects(platform.getBoundsInParent())) {
                    if (movingDown) {
                        if (e.getTranslateY() + e.getHeight() == platform.getTranslateY()) {
                            app.triggerCollision(e, platform);
                            e.setTranslateY(e.getTranslateY() - 1);
                            e.setProperty("jumping", false);
                            return;
                        }
                    }
                    else {
                        if (e.getTranslateY() == platform.getTranslateY() + platform.getHeight()) {
                            app.triggerCollision(e, platform);
                            return;
                        }
                    }
                }
            }
            e.setTranslateY(e.getTranslateY() + (movingDown ? 1 : -1));
            e.setProperty("jumping", true);
        }
    }
}
