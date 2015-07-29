package com.almasb.consume.collision;

import com.almasb.consume.Event;
import com.almasb.consume.Types.Property;
import com.almasb.consume.Types.Type;
import com.almasb.consume.ai.ChargeControl;
import com.almasb.consume.ai.PhysicsControl;
import com.almasb.fxgl.GameApplication;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;
import com.almasb.fxgl.physics.CollisionHandler;
import com.ergo21.consume.Player;

import javafx.scene.text.Text;

public class PlayerEnemyHandler extends CollisionHandler {

    private GameApplication app;

    public PlayerEnemyHandler(GameApplication app) {
        super(Type.PLAYER, Type.ENEMY);
        this.app = app;
    }

    @Override
    public void onCollision(Entity player, Entity enemy) {
        if (enemy.getControl(ChargeControl.class) != null) {
        	Player playerData = player.getProperty(Property.DATA);
        	playerData.setCurrentHealth(playerData.getCurrentHealth() - 1);

            int velocityX = enemy.getControl(ChargeControl.class).getVelocity();
            player.getControl(PhysicsControl.class).moveX(velocityX);

            enemy.fireFXGLEvent(new FXGLEvent(Event.ENEMY_HIT_PLAYER));

            player.setCollidable(false);
            Entity e = Entity.noType().setGraphics(new Text("INVINCIBLE"));
            e.translateXProperty().bind(player.translateXProperty());
            e.translateYProperty().bind(player.translateYProperty().subtract(20));

            app.addEntities(e);

            app.runOnceAfter(() -> {
            	if(player.getControl(PhysicsControl.class).getVelocity().getX() == velocityX){
            		player.getControl(PhysicsControl.class).moveX(0);
                }
            }, GameApplication.SECOND/2);

            app.runOnceAfter(() -> {
                app.removeEntity(e);
                player.setCollidable(true);
            }, 2 * GameApplication.SECOND);
        }
        else{
        	enemy.fireFXGLEvent(new FXGLEvent(Event.ENEMY_HIT_PLAYER));
        	Player playerData = player.getProperty(Property.DATA);
        	playerData.setCurrentHealth(playerData.getCurrentHealth() - 1);

            player.setCollidable(false);
            Entity e = Entity.noType().setGraphics(new Text("INVINCIBLE"));
            e.translateXProperty().bind(player.translateXProperty());
            e.translateYProperty().bind(player.translateYProperty().subtract(20));

            app.addEntities(e);

            app.runOnceAfter(() -> {
                app.removeEntity(e);
                player.setCollidable(true);
            }, 2 * GameApplication.SECOND);
        }
    }

    @Override
    public void onCollisionBegin(Entity a, Entity b) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onCollisionEnd(Entity a, Entity b) {
        // TODO Auto-generated method stub

    }
}
