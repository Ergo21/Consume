package com.almasb.consume.collision;

import java.util.List;

import com.almasb.consume.Config;
import com.almasb.consume.Event;
import com.almasb.consume.Types.Element;
import com.almasb.consume.Types.Property;
import com.almasb.consume.Types.Type;
import com.almasb.fxgl.GameApplication;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;
import com.almasb.fxgl.physics.CollisionHandler;
import com.ergo21.consume.Enemy;

import javafx.animation.FadeTransition;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class ProjectileEnemyHandler extends CollisionHandler {

	private GameApplication app;

	public ProjectileEnemyHandler(GameApplication app) {
		super(Type.PLAYER_PROJECTILE, Type.ENEMY);
		this.app = app;
	}

	@Override
	public void onCollision(Entity projectile, Entity enemy) {
		Element element = projectile.getProperty(Property.SUB_TYPE);
		Enemy enemyData = enemy.getProperty(Property.DATA);

		List<Element> resists = enemyData.getResistances();
		List<Element> weaknesses = enemyData.getWeaknesses();

		int damage = Config.SPEAR_DAMAGE;
		switch (element) {
		case NEUTRAL2: {
			damage = Config.KNIFE_DAMAGE;
			break;
		}
		case CONSUME:
			damage = enemyData.getMaxHealth();
			break;
		case DEATH:
			damage = Config.DEATH_DAMAGE;
			break;
		case EARTH:
			damage = Config.SAND_DAMAGE;
			break;
		case FIRE:
			damage = Config.FIREBALL_DAMAGE;
			break;
		case LIGHTNING:
			damage = Config.LIGHTNING_DAMAGE;
			break;
		case METAL:
			damage = Config.BULLET_DAMAGE;
			break;
		default:
			break;
		}
		String modifier = " x1";

		if (resists.contains(element)) {
			damage = (int) (damage * 0.5);
			modifier = "x0.5";
		} else if (weaknesses.contains(element)) {
			damage *= 2;
			modifier = "x2";
		}

		Entity e = Entity.noType().setPosition(enemy.getTranslateX(), enemy.getTranslateY())
				.setGraphics(new Text(damage + "!  " + modifier));

		app.getSceneManager().addEntities(e);

		FadeTransition ft = new FadeTransition(Duration.seconds(1.5), e);
		ft.setToValue(0);
		ft.setOnFinished(event -> {
			app.getSceneManager().removeEntity(e);
		});
		ft.play();

		enemyData.takeDamage(damage);

		app.getSceneManager().removeEntity(projectile);
		
		enemy.setProperty("beenHit", true);
		
		app.getTimerManager().runOnceAfter(() -> {if(enemy != null){enemy.setProperty("beenHit", false);}}, Duration.seconds(0.5));

		if (enemyData.getCurrentHealth() <= 0) {
			enemy.fireFXGLEvent(new FXGLEvent(Event.DEATH));
		}
	}
}
