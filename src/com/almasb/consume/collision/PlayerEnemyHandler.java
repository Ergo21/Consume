package com.almasb.consume.collision;

import com.almasb.consume.Event;
import com.almasb.consume.Types.Property;
import com.almasb.consume.Types.Type;
import com.almasb.consume.ai.DiveBombControl;
import com.almasb.consume.ai.PhysicsControl;
import com.almasb.consume.ai.SimpleMoveControl;
import com.almasb.fxgl.GameApplication;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;
import com.almasb.fxgl.physics.CollisionHandler;
import com.ergo21.consume.Player;

import javafx.scene.text.Text;
import javafx.util.Duration;

public class PlayerEnemyHandler extends CollisionHandler {

	private GameApplication app;

	public PlayerEnemyHandler(GameApplication app) {
		super(Type.PLAYER, Type.ENEMY);
		this.app = app;
	}

	@Override
	public void onCollisionBegin(Entity player, Entity enemy) {
		enemy.fireFXGLEvent(new FXGLEvent(Event.ENEMY_HIT_PLAYER));
		Player playerData = player.getProperty(Property.DATA);
		if(enemy.<Boolean>getProperty("shover") != null && enemy.<Boolean>getProperty("shover")){
			playerData.setCurrentHealth(playerData.getCurrentHealth() - 3);
		}
		else{
			playerData.setCurrentHealth(playerData.getCurrentHealth() - 1);
		}

		int velocityX = (int) enemy.getControl(PhysicsControl.class).getVelocity().getX();
		if(enemy.getControl(DiveBombControl.class) != null){
			velocityX = enemy.getControl(DiveBombControl.class).getVelocity();
		}
		else if(enemy.getControl(SimpleMoveControl.class) != null){
			velocityX = enemy.getControl(SimpleMoveControl.class).getVelocity();
		}
		player.getControl(PhysicsControl.class).moveX(velocityX);

		if(playerData.getCurrentHealth() > 0){
			player.setCollidable(false);
			player.setProperty("stunned", true);
			Entity e = Entity.noType().setGraphics(new Text("INVINCIBLE"));
			e.translateXProperty().bind(player.translateXProperty());
			e.translateYProperty().bind(player.translateYProperty().subtract(20));

			app.getSceneManager().addEntities(e);

			app.getTimerManager().runOnceAfter(() -> {
				player.getControl(PhysicsControl.class).moveX(0);
				player.setProperty("stunned", false);
			} , Duration.seconds(0.5));

			app.getTimerManager().runOnceAfter(() -> {
				app.getSceneManager().removeEntity(e);
				player.setCollidable(true);
			} , Duration.seconds(2));
		}
	}
}
