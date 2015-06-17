package com.almasb.consume.collision;

import javafx.scene.text.Text;

import com.almasb.consume.Event;
import com.almasb.consume.ai.ChargeControl;
import com.almasb.consume.ai.PhysicsControl;
import com.almasb.fxgl.GameApplication;
import com.almasb.fxgl.entity.CollisionHandler;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;

public class PlayerEnemyHandler implements CollisionHandler {

    private GameApplication app;

    public PlayerEnemyHandler(GameApplication app) {
        this.app = app;
    }

    @Override
    public void onCollision(Entity player, Entity enemy) {
        if (enemy.getControl(ChargeControl.class) != null) {
            int velocityX = enemy.getControl(ChargeControl.class).getVelocity();
            player.getControl(PhysicsControl.class).moveX(velocityX * 5);

            enemy.fireFXGLEvent(new FXGLEvent(Event.ENEMY_HIT_PLAYER));

            player.setUsePhysics(false);
            Entity e = Entity.noType().setGraphics(new Text("INVINCIBLE"));
            e.translateXProperty().bind(player.translateXProperty());
            e.translateYProperty().bind(player.translateYProperty().subtract(20));

            app.addEntities(e);

            app.runOnceAfter(() -> {
                app.removeEntity(e);
                player.setUsePhysics(true);
            }, 2 * GameApplication.SECOND);
        }
    }
}
