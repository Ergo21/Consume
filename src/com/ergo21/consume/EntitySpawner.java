package com.ergo21.consume;

import java.util.ArrayList;
import java.util.Collections;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import com.almasb.consume.Config;
import com.almasb.consume.ConsumeApp;
import com.almasb.consume.Event;
import com.almasb.consume.Config.Speed;
import com.almasb.consume.Types.Element;
import com.almasb.consume.Types.Powerup;
import com.almasb.consume.Types.Property;
import com.almasb.consume.Types.Type;
import com.almasb.consume.ai.AnubisControl;
import com.almasb.consume.ai.BayonetControl;
import com.almasb.consume.ai.BurnerControl;
import com.almasb.consume.ai.CannonControl;
import com.almasb.consume.ai.ChargeControl;
import com.almasb.consume.ai.ComplexJumpControl;
import com.almasb.consume.ai.ConsumeControl;
import com.almasb.consume.ai.DiveBombControl;
import com.almasb.consume.ai.DiveReturnControl;
import com.almasb.consume.ai.DogControl;
import com.almasb.consume.ai.GentlemanControl;
import com.almasb.consume.ai.KiboControl;
import com.almasb.consume.ai.KnifeControl;
import com.almasb.consume.ai.MagicianControl;
import com.almasb.consume.ai.MummyControl;
import com.almasb.consume.ai.MusicianControl;
import com.almasb.consume.ai.PhysicsControl;
import com.almasb.consume.ai.RifleControl;
import com.almasb.consume.ai.SandBossControl;
import com.almasb.consume.ai.ScorpionControl;
import com.almasb.consume.ai.ShakaControl;
import com.almasb.consume.ai.ShangoControl;
import com.almasb.consume.ai.ShooterControl;
import com.almasb.consume.ai.SimpleJumpControl;
import com.almasb.consume.ai.SimpleMoveControl;
import com.almasb.consume.ai.SpearThrowerControl;
import com.almasb.consume.ai.StoneThrowerControl;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;
import com.almasb.fxgl.entity.FXGLEventHandler;

public class EntitySpawner {
	private ConsumeApp consApp;

	public EntitySpawner(ConsumeApp a) {
		consApp = a;
	}

	public Entity spawnEnemy(Point2D spawnPoint) {
		Entity enemy = new Entity(Type.ENEMY);
		Rectangle rect = new Rectangle(30, 30);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setCollidable(true);
		enemy.setProperty(Property.DATA, new Enemy(consApp.assets.getText("enemies/enemy_FireElemental.txt")));
		enemy.setProperty("physics", consApp.physics);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		enemy.addControl(new PhysicsControl(consApp.physics));
		enemy.addControl(new SimpleMoveControl(consApp.player));
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
		enemy.addFXGLEventHandler(Event.ENEMY_SAW_PLAYER,
				event -> consApp.consController.aimedProjectile(enemy, consApp.player));
		enemy.setProperty(Property.ENABLE_GRAVITY, false);

		return enemy;
	}

	public Entity spawnCharger(Point2D spawnPoint) {
		Entity enemy = new Entity(Type.ENEMY);
		Rectangle rect = new Rectangle(30, 30);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setCollidable(true);
		enemy.setProperty(Property.DATA, new Enemy(consApp.assets.getText("enemies/enemy_FireElemental.txt")));
		enemy.setProperty("physics", consApp.physics);
		enemy.setProperty("shover", true);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		enemy.addControl(new ChargeControl(consApp.player));
		enemy.addControl(new PhysicsControl(consApp.physics));
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
		enemy.addFXGLEventHandler(Event.ENEMY_SAW_PLAYER, event -> {
			Entity tesEnemy = event.getTarget();

			Entity e = Entity.noType().setGraphics(new Text("!"));
			e.setPosition(tesEnemy.getTranslateX(), tesEnemy.getTranslateY());
			consApp.getSceneManager().addEntities(e);
			consApp.getTimerManager().runOnceAfter(() -> {
				consApp.getSceneManager().removeEntity(e);
			} , Config.ENEMY_CHARGE_DELAY);
		});

		return enemy;
	}

	public Entity spawnScarab(Point2D spawnPoint) {
		Entity enemy = new Entity(Type.ENEMY);
		Rectangle rect = new Rectangle(30, 30);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setCollidable(true);
		enemy.setProperty(Property.DATA, new Enemy(consApp.assets.getText("enemies/enemy_FireElemental.txt")));
		enemy.setProperty("physics", consApp.physics);
		enemy.setProperty("shover", true);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		enemy.addControl(new PhysicsControl(consApp.physics));
		enemy.addControl(new SimpleJumpControl(consApp, consApp.player, Speed.ENEMY_JUMP));
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);

		return enemy;
	}

	public Entity spawnLocust(Point2D spawnPoint) {
		Entity enemy = new Entity(Type.ENEMY);
		Rectangle rect = new Rectangle(30, 30);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setCollidable(true);
		enemy.setProperty(Property.DATA, new Enemy(consApp.assets.getText("enemies/enemy_FireElemental.txt")));
		enemy.setProperty("physics", consApp.physics);
		enemy.setProperty("shover", true);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		enemy.addControl(new PhysicsControl(consApp.physics));
		enemy.addControl(new DiveBombControl(consApp.player, enemy.getPosition().getY()));
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
		enemy.setProperty(Property.ENABLE_GRAVITY, false);

		return enemy;
	}
	
	public Entity spawnEloko(Point2D spawnPoint){
		Entity enemy = new Entity(Type.ENEMY);
		Rectangle rect = new Rectangle(30, 30);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setCollidable(true);
		enemy.setProperty(Property.DATA, new Enemy(consApp.assets.getText("enemies/enemy_FireElemental.txt")));
		enemy.setProperty("physics", consApp.physics);
		enemy.setProperty("facingRight", true);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		enemy.addControl(new PhysicsControl(consApp.physics));
		enemy.addControl(new ConsumeControl(consApp.player));
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
		enemy.addFXGLEventHandler(Event.ENEMY_FIRED, event -> {
			consApp.consController.enemyShootProjectile(Element.NEUTRAL2, enemy);
			consApp.getTimerManager().runOnceAfter(() -> {
				//TODO: Change enemy animation
			}, Config.CONSUME_DECAY);
		});

		return enemy;
	}
	
	public Entity spawnMummy(Point2D spawnPoint){
		Entity enemy = new Entity(Type.ENEMY);
		Rectangle rect = new Rectangle(30, 30);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setCollidable(true);
		enemy.setProperty(Property.DATA, new Enemy(consApp.assets.getText("enemies/enemy_FireElemental.txt")));
		enemy.setProperty("physics", consApp.physics);
		enemy.setProperty("facingRight", true);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		enemy.addControl(new PhysicsControl(consApp.physics));
		enemy.addControl(new MummyControl(consApp.player));
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
		enemy.addFXGLEventHandler(Event.ENEMY_FIRED, event -> {
			consApp.consController.enemyShootProjectile(Element.NEUTRAL2, enemy);
			consApp.getTimerManager().runOnceAfter(() -> {
				//TODO: Change enemy animation
			}, Config.CONSUME_DECAY);
		});

		return enemy;
	}
	
	public Entity spawnSpearEnemy(Point2D spawnPoint){
		Entity enemy = new Entity(Type.ENEMY);
		Rectangle rect = new Rectangle(30, 30);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setCollidable(true);
		enemy.setProperty(Property.DATA, new Enemy(consApp.assets.getText("enemies/enemy_FireElemental.txt")));
		enemy.setProperty("physics", consApp.physics);
		enemy.setProperty("facingRight", true);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		enemy.addControl(new PhysicsControl(consApp.physics));
		enemy.addControl(new SpearThrowerControl(consApp.player));
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
		enemy.addFXGLEventHandler(Event.ENEMY_FIRED, event -> {
			consApp.consController.enemyShootProjectile(Element.NEUTRAL, enemy);
			consApp.getTimerManager().runOnceAfter(() -> {
				//TODO: Change enemy animation
			}, Config.CONSUME_DECAY);
			consApp.getTimerManager().runOnceAfter(() -> {
				if(enemy != null && enemy.getControl(SpearThrowerControl.class) != null){
					enemy.getControl(SpearThrowerControl.class).setSpearThrown(false);
				}			
			}, Config.ENEMY_SPEAR_DECAY);
		});

		return enemy;
	}
	
	public Entity spawnScorpion(Point2D spawnPoint){
		Entity enemy = new Entity(Type.ENEMY);
		Rectangle rect = new Rectangle(30, 30);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setCollidable(true);
		enemy.setProperty(Property.DATA, new Enemy(consApp.assets.getText("enemies/enemy_FireElemental.txt")));
		enemy.setProperty("physics", consApp.physics);
		enemy.setProperty("facingRight", true);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		enemy.addControl(new PhysicsControl(consApp.physics));
		enemy.addControl(new ScorpionControl(consApp.player));
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
		enemy.addFXGLEventHandler(Event.ENEMY_FIRED, event -> {
			consApp.getTimerManager().runOnceAfter(() -> {
				//TODO: Change enemy animation
			}, Config.CONSUME_DECAY);
			consApp.getTimerManager().runOnceAfter(() -> {
				if(enemy != null && enemy.getControl(ScorpionControl.class) != null){
					consApp.consController.enemyShootProjectile(Element.METAL, enemy);
				}			
			}, Config.ENEMY_SCORPION_DELAY);
			consApp.getTimerManager().runOnceAfter(() -> {
				if(enemy != null && enemy.getControl(ScorpionControl.class) != null){
					enemy.getControl(ScorpionControl.class).setSpearThrown(false);
				}			
			}, Config.ENEMY_SCORPION_DECAY);
		});

		return enemy;
	}
	
	public Entity spawnShooter(Point2D spawnPoint){
		Entity enemy = new Entity(Type.ENEMY);
		Rectangle rect = new Rectangle(30, 30);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setCollidable(false);
		enemy.setProperty(Property.DATA, new Enemy(consApp.assets.getText("enemies/enemy_FireElemental.txt")));
		enemy.setProperty("physics", consApp.physics);
		enemy.setProperty("facingRight", false);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		enemy.addControl(new PhysicsControl(consApp.physics));
		enemy.addControl(new ShooterControl(consApp.player));
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
		enemy.addFXGLEventHandler(Event.ENEMY_FIRED, event -> {
			consApp.getTimerManager().runOnceAfter(() -> {
				//TODO: Change enemy animation
			}, Config.CONSUME_DECAY);
			consApp.getTimerManager().runOnceAfter(() -> {
				if(enemy != null && enemy.getControl(ShooterControl.class) != null){
					consApp.consController.enemyShootProjectile(Element.METAL, enemy);
				}			
			}, Config.ENEMY_SCORPION_DELAY);
			consApp.getTimerManager().runOnceAfter(() -> {
				if(enemy != null && enemy.getControl(ShooterControl.class) != null){
					enemy.getControl(ShooterControl.class).setSpearThrown(false);
				}			
			}, Config.ENEMY_SCORPION_DECAY);
		});

		return enemy;
	}
	
	public Entity spawnBurner(Point2D spawnPoint){
		Entity enemy = new Entity(Type.ENEMY);
		Rectangle rect = new Rectangle(40, 40);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setCollidable(false);
		enemy.setProperty(Property.DATA, new Enemy(consApp.assets.getText("enemies/enemy_FireElemental.txt")));
		enemy.setProperty("physics", consApp.physics);
		enemy.setProperty("facingRight", false);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		enemy.addControl(new BurnerControl(consApp.player));
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
		enemy.addFXGLEventHandler(Event.ENEMY_FIRED, event -> {
			consApp.getTimerManager().runOnceAfter(() -> {
				//TODO: Change enemy animation
			}, Config.CONSUME_DECAY);
			consApp.getTimerManager().runOnceAfter(() -> {
				if(enemy != null && enemy.getControl(BurnerControl.class) != null){
					consApp.consController.enemyShootFireball(enemy);
				}			
			}, Config.ENEMY_SCORPION_DELAY);
			consApp.getTimerManager().runOnceAfter(() -> {
				if(enemy != null && enemy.getControl(BurnerControl.class) != null){
					enemy.getControl(BurnerControl.class).setSpearThrown(false);
				}			
			}, Config.ENEMY_FIRE_THROW_DELAY);
		});

		return enemy;
	}
	
	public Entity spawnStoneEnemy(Point2D spawnPoint){
		Entity enemy = new Entity(Type.ENEMY);
		Rectangle rect = new Rectangle(30, 30);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setCollidable(true);
		enemy.setProperty(Property.DATA, new Enemy(consApp.assets.getText("enemies/enemy_FireElemental.txt")));
		enemy.setProperty("physics", consApp.physics);
		enemy.setProperty("facingRight", true);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		enemy.addControl(new PhysicsControl(consApp.physics));
		enemy.addControl(new StoneThrowerControl(consApp.player));
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
		enemy.addFXGLEventHandler(Event.ENEMY_FIRED, event -> {
			consApp.consController.enemyShootStones(enemy);
			consApp.getTimerManager().runOnceAfter(() -> {
				//TODO: Change enemy animation
			}, Config.CONSUME_DECAY);
			consApp.getTimerManager().runOnceAfter(() -> {
				if(enemy != null && enemy.getControl(StoneThrowerControl.class) != null){
					enemy.getControl(StoneThrowerControl.class).setStonesThrown(false);
				}			
			}, Config.ENEMY_STONE_THROW_RECHARGE);
		});
		return enemy;
	}
	
	public Entity spawnMusician(Point2D spawnPoint) {
		Entity enemy = new Entity(Type.ENEMY);
		Rectangle rect = new Rectangle(30, 30);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setCollidable(true);
		enemy.setProperty(Property.DATA, new Enemy(consApp.assets.getText("enemies/enemy_FireElemental.txt")));
		enemy.setProperty("physics", consApp.physics);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		enemy.addControl(new PhysicsControl(consApp.physics));
		enemy.addControl(new MusicianControl(consApp, consApp.player, Speed.ENEMY_JUMP));
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
		enemy.addFXGLEventHandler(Event.ENEMY_FIRED, event -> {
			enemy.getControl(MusicianControl.class).setPlayed(true);
			consApp.consController.enemyPlayFlute(enemy);
		});

		return enemy;
	}
	
	public Entity spawnDancer(Point2D spawnPoint) {
		Entity enemy = new Entity(Type.ENEMY);
		Rectangle rect = new Rectangle(30, 30);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setCollidable(true);
		enemy.setProperty(Property.DATA, new Enemy(consApp.assets.getText("enemies/enemy_FireElemental.txt")));
		enemy.setProperty("physics", consApp.physics);
		enemy.setProperty("shover", true);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		enemy.addControl(new PhysicsControl(consApp.physics));
		enemy.addControl(new ComplexJumpControl(consApp, consApp.player, Speed.ENEMY_JUMP));
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);

		return enemy;
	}
	
	public Entity spawnDog(Point2D spawnPoint) {
		Entity enemy = new Entity(Type.ENEMY);
		Rectangle rect = new Rectangle(30, 30);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setCollidable(true);
		enemy.setProperty(Property.DATA, new Enemy(consApp.assets.getText("enemies/enemy_FireElemental.txt")));
		enemy.setProperty("physics", consApp.physics);
		enemy.setProperty("shover", true);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		enemy.addControl(new DogControl(consApp.player));
		enemy.addControl(new PhysicsControl(consApp.physics));
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
		enemy.addFXGLEventHandler(Event.ENEMY_SAW_PLAYER, event -> {
			Entity tesEnemy = event.getTarget();

			Entity e = Entity.noType().setGraphics(new Text("!"));
			e.setPosition(tesEnemy.getTranslateX(), tesEnemy.getTranslateY());
			consApp.getSceneManager().addEntities(e);
			consApp.getTimerManager().runOnceAfter(() -> {
				consApp.getSceneManager().removeEntity(e);
			} , Config.ENEMY_CHARGE_DELAY);
		});

		return enemy;
	}
	
	public Entity spawnIceSpirit(Point2D spawnPoint) {
		Entity enemy = new Entity(Type.ENEMY);
		Rectangle rect = new Rectangle(30, 30);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setCollidable(true);
		enemy.setProperty(Property.DATA, new Enemy(consApp.assets.getText("enemies/enemy_FireElemental.txt")));
		enemy.setProperty("physics", consApp.physics);
		enemy.setProperty("shover", true);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		enemy.addControl(new PhysicsControl(consApp.physics));
		enemy.addControl(new DiveReturnControl(consApp.player, enemy.getPosition().getY()));
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
		enemy.setProperty(Property.ENABLE_GRAVITY, false);

		return enemy;
	}
	
	public Entity spawnCannon(Point2D spawnPoint){
		Entity enemy = new Entity(Type.ENEMY);
		Rectangle rect = new Rectangle(30, 30);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setCollidable(true);
		enemy.setProperty(Property.DATA, new Enemy(consApp.assets.getText("enemies/enemy_FireElemental.txt")));
		enemy.setProperty("physics", consApp.physics);
		enemy.setProperty("facingRight", false);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		enemy.addControl(new PhysicsControl(consApp.physics));
		enemy.addControl(new CannonControl(consApp.player));
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
		enemy.addFXGLEventHandler(Event.ENEMY_FIRED, event -> {
			consApp.getTimerManager().runOnceAfter(() -> {
				//TODO: Change enemy animation
			}, Config.CONSUME_DECAY);
			consApp.getTimerManager().runOnceAfter(() -> {
				if(enemy != null && enemy.getControl(CannonControl.class) != null){
					consApp.consController.enemyShootProjectile(Element.METAL, enemy);
				}			
			}, Config.ENEMY_SCORPION_DELAY.multiply(1.25));
			consApp.getTimerManager().runOnceAfter(() -> {
				if(enemy != null && enemy.getControl(CannonControl.class) != null){
					enemy.getControl(CannonControl.class).setSpearThrown(false);
				}			
			}, Config.ENEMY_SCORPION_DECAY.multiply(1.25));
		});

		return enemy;
	}
	
	public Entity spawnRifler(Point2D spawnPoint){
		Entity enemy = new Entity(Type.ENEMY);
		Rectangle rect = new Rectangle(30, 30);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setCollidable(true);
		enemy.setProperty(Property.DATA, new Enemy(consApp.assets.getText("enemies/enemy_FireElemental.txt")));
		enemy.setProperty("physics", consApp.physics);
		enemy.setProperty("facingRight", false);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		enemy.addControl(new PhysicsControl(consApp.physics));
		enemy.addControl(new RifleControl(consApp.player));
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
		enemy.addFXGLEventHandler(Event.ENEMY_FIRED, event -> {
			consApp.getTimerManager().runOnceAfter(() -> {
				//TODO: Change enemy animation
			}, Config.CONSUME_DECAY);
			consApp.getTimerManager().runOnceAfter(() -> {
				if(enemy != null && enemy.getControl(RifleControl.class) != null){
					consApp.consController.enemyShootProjectile(Element.METAL, enemy);
				}			
			}, Config.ENEMY_SCORPION_DELAY);
			consApp.getTimerManager().runOnceAfter(() -> {
				if(enemy != null && enemy.getControl(RifleControl.class) != null){
					enemy.getControl(RifleControl.class).setSpearThrown(false);
				}			
			}, Config.ENEMY_SCORPION_DECAY);
		});

		return enemy;
	}
	
	public Entity spawnBayoneter(Point2D spawnPoint){
		Entity enemy = new Entity(Type.ENEMY);
		Rectangle rect = new Rectangle(30, 30);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setCollidable(true);
		enemy.setProperty(Property.DATA, new Enemy(consApp.assets.getText("enemies/enemy_FireElemental.txt")));
		enemy.setProperty("physics", consApp.physics);
		enemy.setProperty("facingRight", false);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		enemy.addControl(new PhysicsControl(consApp.physics));
		enemy.addControl(new BayonetControl(consApp.player));
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
		enemy.addFXGLEventHandler(Event.ENEMY_FIRED, event -> {
			consApp.getTimerManager().runOnceAfter(() -> {
				//TODO: Change enemy animation
			}, Config.CONSUME_DECAY);
			consApp.consController.enemyStab(enemy);
		});

		return enemy;
	}
	
	public Entity spawnKnifer(Point2D spawnPoint){
		Entity enemy = new Entity(Type.ENEMY);
		Rectangle rect = new Rectangle(30, 30);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setCollidable(true);
		enemy.setProperty(Property.DATA, new Enemy(consApp.assets.getText("enemies/enemy_FireElemental.txt")));
		enemy.setProperty("physics", consApp.physics);
		enemy.setProperty("facingRight", true);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		enemy.addControl(new PhysicsControl(consApp.physics));
		enemy.addControl(new KnifeControl(consApp.player));
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
		enemy.addFXGLEventHandler(Event.ENEMY_FIRED, event -> {
			consApp.consController.enemyShootProjectile(Element.NEUTRAL2, enemy);
			consApp.getTimerManager().runOnceAfter(() -> {
				//TODO: Change enemy animation
			}, Config.CONSUME_DECAY);
		});

		return enemy;
	}
	
	public Entity spawnMagician(Point2D spawnPoint){
		Entity enemy = new Entity(Type.ENEMY);
		Rectangle rect = new Rectangle(30, 30);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setCollidable(true);
		enemy.setProperty(Property.DATA, new Enemy(consApp.assets.getText("enemies/enemy_FireElemental.txt")));
		enemy.setProperty("physics", consApp.physics);
		enemy.setProperty("facingRight", false);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		enemy.addControl(new PhysicsControl(consApp.physics));
		enemy.addControl(new MagicianControl(consApp.player));
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
		enemy.addFXGLEventHandler(Event.ENEMY_FIRED, event -> {
			consApp.getTimerManager().runOnceAfter(() -> {
				//TODO: Change enemy animation
			}, Config.CONSUME_DECAY);
			consApp.getTimerManager().runOnceAfter(() -> {
				if(enemy != null && enemy.getControl(MagicianControl.class) != null){
					consApp.consController.enemyShootProjectile(Element.FIRE, enemy);
				}			
			}, Config.ENEMY_SCORPION_DELAY);
			consApp.getTimerManager().runOnceAfter(() -> {
				if(enemy != null && enemy.getControl(MagicianControl.class) != null){
					enemy.getControl(MagicianControl.class).setSpearThrown(false);
				}			
			}, Config.ENEMY_SCORPION_DECAY);
		});

		return enemy;
	}
	
	public Entity spawnBoss(Point2D spawnPoint) {
		Entity enemy = new Entity(Type.ENEMY);
		Rectangle rect = new Rectangle(30, 30);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setCollidable(true);
		enemy.setProperty(Property.DATA, new Enemy(consApp.assets.getText("enemies/enemy_FireElemental.txt")));
		enemy.setProperty(Property.SUB_TYPE, Type.BOSS);
		enemy.setProperty("physics", consApp.physics);
		enemy.setProperty("shover", true);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		enemy.addControl(new PhysicsControl(consApp.physics));
		enemy.addControl(new SimpleJumpControl(consApp, consApp.player, Speed.ENEMY_JUMP));
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);

		return enemy;
	}
	
	public Entity spawnSandBoss(Point2D spawnPoint) {
		Entity enemy = new Entity(Type.ENEMY);
		Rectangle rect = new Rectangle(30, 30);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setVisible(false);
		enemy.setCollidable(false);
		enemy.setProperty(Property.DATA, new Enemy(consApp.assets.getText("enemies/enemy_FireElemental.txt")));
		enemy.setProperty(Property.SUB_TYPE, Type.BOSS);
		enemy.setProperty("physics", consApp.physics);
		enemy.setProperty("shover", true);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		enemy.addControl(new PhysicsControl(consApp.physics));
		enemy.addControl(new SandBossControl(consApp.player));
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
		enemy.addFXGLEventHandler(Event.ENEMY_FIRED, event -> {
			consApp.getTimerManager().runOnceAfter(() -> {
				//TODO: Change enemy animation
			}, Config.CONSUME_DECAY);
			consApp.getTimerManager().runOnceAfter(() -> {
				if(enemy != null && enemy.getControl(SandBossControl.class) != null){
					consApp.consController.enemyShootProjectile(Element.EARTH, enemy);
				}			
			}, Config.ENEMY_SCORPION_DELAY);
			consApp.getTimerManager().runOnceAfter(() -> {
				if(enemy != null && enemy.getControl(SandBossControl.class) != null){
					enemy.getControl(SandBossControl.class).setAttackComplete(true);
				}			
			}, Config.ENEMY_SCORPION_DECAY);
		});

		return enemy;
	}
	
	public Entity spawnAnubisBoss(Point2D spawnPoint) {
		Entity enemy = new Entity(Type.ENEMY);
		Rectangle rect = new Rectangle(30, 30);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setVisible(true);
		enemy.setCollidable(true);
		enemy.setProperty(Property.DATA, new Enemy(consApp.assets.getText("enemies/enemy_FireElemental.txt")));
		enemy.setProperty(Property.SUB_TYPE, Type.BOSS);
		enemy.setProperty("physics", consApp.physics);
		enemy.setProperty("shover", true);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		enemy.addControl(new PhysicsControl(consApp.physics));
		enemy.addControl(new AnubisControl(consApp, consApp.player));
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
		enemy.addFXGLEventHandler(Event.ENEMY_FIRED, event -> {
			if((boolean)enemy.getProperty("jumping")){
				consApp.getTimerManager().runOnceAfter(() -> {
					if(enemy != null && enemy.getControl(AnubisControl.class) != null){
						consApp.consController.enemyShootProjectile(Element.DEATH, enemy);
					}			
				}, Config.ANUBIS_JATTACK_DELAY);
				consApp.getTimerManager().runOnceAfter(() -> {
					if(enemy != null && enemy.getControl(AnubisControl.class) != null){
						consApp.consController.enemyShootProjectile(Element.DEATH, enemy);
					}			
				}, Config.ANUBIS_JATTACK_DELAY.multiply(2));
				consApp.getTimerManager().runOnceAfter(() -> {
					if(enemy != null && enemy.getControl(AnubisControl.class) != null){
						consApp.consController.enemyShootProjectile(Element.DEATH, enemy);
						enemy.getControl(AnubisControl.class).setAttackComplete(true, false);
					}			
				}, Config.ANUBIS_JATTACK_DELAY.multiply(3));
			}
			else{
				consApp.consController.enemyShootProjectile(Element.NEUTRAL2, enemy);
				consApp.getTimerManager().runOnceAfter(() -> {
					if(enemy != null && enemy.getControl(AnubisControl.class) != null){
						enemy.getControl(AnubisControl.class).setAttackComplete(true, true);
					}			
				}, Config.ENEMY_SCORPION_DECAY);
			}
			
			consApp.getTimerManager().runOnceAfter(() -> {
				//TODO: Change enemy animation
			}, Config.CONSUME_DECAY);
		});

		return enemy;
	}
	
	public Entity spawnShangoBoss(Point2D spawnPoint) {
		Entity enemy = new Entity(Type.ENEMY);
		Rectangle rect = new Rectangle(30, 30);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setVisible(true);
		enemy.setCollidable(true);
		enemy.setProperty(Property.DATA, new Enemy(consApp.assets.getText("enemies/enemy_FireElemental.txt")));
		enemy.setProperty(Property.SUB_TYPE, Type.BOSS);
		enemy.setProperty("physics", consApp.physics);
		enemy.setProperty("shover", true);
		enemy.setProperty("facingRight", false);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		Point2D gPoint = spawnPoint.add(0, enemy.getHeight() + 5);
		enemy.addControl(new PhysicsControl(consApp.physics));
		enemy.addControl(new ShangoControl(consApp, consApp.player));
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
		enemy.addFXGLEventHandler(Event.ENEMY_FIRED, event -> {
			if((boolean)enemy.getProperty("jumping")){
				consApp.getTimerManager().runOnceAfter(() -> {
					if(enemy != null && enemy.getControl(ShangoControl.class) != null){
						if((boolean)enemy.getProperty("facingRight")){
							consApp.consController.aimedLightningBolt(enemy, new Point2D(enemy.getPosition().getX() - Config.BLOCK_SIZE*1.5, gPoint.getY()));	
						}
						else{
							consApp.consController.aimedLightningBolt(enemy, new Point2D(enemy.getPosition().getX() + Config.BLOCK_SIZE*1.5, gPoint.getY()));	
						}
						enemy.getControl(ShangoControl.class).setAttackComplete(true, false);
					}			
				}, Config.SHANGO_JATTACK_DELAY);
			}
			else{
				if(consApp.getRandom().nextBoolean()){
					if((boolean)enemy.getProperty("facingRight")){
						consApp.getTimerManager().runOnceAfter(() -> {
							consApp.consController.aimedLightningBolt(enemy, new Point2D(enemy.getPosition().getX() + Config.BLOCK_SIZE*1.5, gPoint.getY()));	
						}, Config.SHANGO_MATTACK_DELAY);
						consApp.getTimerManager().runOnceAfter(() -> {
							consApp.consController.aimedLightningBolt(enemy, new Point2D(enemy.getPosition().getX() + Config.BLOCK_SIZE*1.5*2, gPoint.getY()));
						}, Config.SHANGO_MATTACK_DELAY.multiply(2));
						consApp.getTimerManager().runOnceAfter(() -> {
							consApp.consController.aimedLightningBolt(enemy, new Point2D(enemy.getPosition().getX() + Config.BLOCK_SIZE*1.5*3, gPoint.getY()));
						}, Config.SHANGO_MATTACK_DELAY.multiply(3));
					}
					else{
						consApp.getTimerManager().runOnceAfter(() -> {
							consApp.consController.aimedLightningBolt(enemy, new Point2D(enemy.getPosition().getX() - Config.BLOCK_SIZE*1.5, gPoint.getY()));	
						}, Config.SHANGO_MATTACK_DELAY);
						consApp.getTimerManager().runOnceAfter(() -> {
							consApp.consController.aimedLightningBolt(enemy, new Point2D(enemy.getPosition().getX() - Config.BLOCK_SIZE*1.5*2, gPoint.getY()));
						}, Config.SHANGO_MATTACK_DELAY.multiply(2));
						consApp.getTimerManager().runOnceAfter(() -> {
							consApp.consController.aimedLightningBolt(enemy, new Point2D(enemy.getPosition().getX() - Config.BLOCK_SIZE*1.5*3, gPoint.getY()));
						}, Config.SHANGO_MATTACK_DELAY.multiply(3));
					}
					consApp.getTimerManager().runOnceAfter(() -> {
						if(enemy != null && enemy.getControl(ShangoControl.class) != null){
							enemy.getControl(ShangoControl.class).setAttackComplete(true, true);
						}				
					}, Config.ENEMY_SCORPION_DECAY);
					
				}
				else{
					Point2D pPos = new Point2D(consApp.player.getPosition().getX(), gPoint.getY());
					consApp.getTimerManager().runOnceAfter(() -> {
						consApp.consController.aimedLightningBolt(enemy, pPos);
					}, Config.SHANGO_MATTACK_DELAY.multiply(3));
					consApp.getTimerManager().runOnceAfter(() -> {
						if(enemy != null && enemy.getControl(ShangoControl.class) != null){
							enemy.getControl(ShangoControl.class).setAttackComplete(true, true);
						}			
					}, Config.ENEMY_SCORPION_DECAY);
				}			
			}
			
			consApp.getTimerManager().runOnceAfter(() -> {
				//TODO: Change enemy animation
			}, Config.CONSUME_DECAY);
		});

		return enemy;
	}
	
	public Entity spawnKiboBoss(Point2D spawnPoint) {
		Entity enemy = new Entity(Type.ENEMY);
		Rectangle rect = new Rectangle(30, 30);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setVisible(true);
		enemy.setCollidable(true);
		enemy.setProperty(Property.DATA, new Enemy(consApp.assets.getText("enemies/enemy_FireElemental.txt")));
		enemy.setProperty(Property.SUB_TYPE, Type.BOSS);
		enemy.setProperty("physics", consApp.physics);
		enemy.setProperty("shover", true);
		enemy.setProperty("facingRight", false);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		enemy.addControl(new PhysicsControl(consApp.physics));
		enemy.addControl(new KiboControl(consApp, consApp.player));
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
		enemy.addFXGLEventHandler(Event.ENEMY_FIRED, event -> {
			if(enemy != null && enemy.getControl(KiboControl.class) != null){
			
				switch(enemy.getControl(KiboControl.class).getAttackType()){
					case 0:{
						consApp.consController.aimedFireball(enemy, consApp.player);
						consApp.getTimerManager().runOnceAfter(() -> {
							if(enemy != null && enemy.getControl(KiboControl.class) != null){			
								enemy.getControl(KiboControl.class).setAttackComplete(true, true);
							}			
						}, Config.ENEMY_SCORPION_DELAY);				
						break;
					}
					case 1:{
						consApp.consController.enemyShootProjectile(Element.FIRE, enemy);
						if(enemy != null & enemy.getControl(PhysicsControl.class) != null){
							enemy.getControl(PhysicsControl.class).jump();
						}
						consApp.getTimerManager().runOnceAfter(() -> {
							if(enemy != null && enemy.getControl(KiboControl.class) != null){			
								consApp.consController.enemyShootProjectile(Element.FIRE, enemy);
							}			
						}, Config.ENEMY_SCORPION_DELAY);	
						consApp.getTimerManager().runOnceAfter(() -> {
							if(enemy != null && enemy.getControl(KiboControl.class) != null){	
								consApp.consController.enemyShootProjectile(Element.FIRE, enemy);
								enemy.getControl(KiboControl.class).setAttackComplete(true, true);
							}			
						}, Config.ENEMY_SCORPION_DELAY.multiply(2.2));	
						break;
					}
					case 2:{
						consApp.consController.enemyShootProjectile(Element.FIRE, enemy);
						consApp.getTimerManager().runOnceAfter(() -> {
							if(enemy != null && enemy.getControl(KiboControl.class) != null){	
								enemy.getControl(KiboControl.class).setAttackComplete(true, false);
							}			
						}, Config.ENEMY_SCORPION_DELAY.multiply(3));
						break;
					}
				}
			}
			
			consApp.getTimerManager().runOnceAfter(() -> {
				//TODO: Change enemy animation
			}, Config.CONSUME_DECAY);
		});

		return enemy;
	}
	
	public Entity spawnGentlemanBoss(Point2D spawnPoint) {
		Entity enemy = new Entity(Type.ENEMY);
		Rectangle rect = new Rectangle(30, 30);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setVisible(true);
		enemy.setCollidable(true);
		enemy.setProperty(Property.DATA, new Enemy(consApp.assets.getText("enemies/enemy_FireElemental.txt")));
		enemy.setProperty(Property.SUB_TYPE, Type.BOSS);
		enemy.setProperty("physics", consApp.physics);
		enemy.setProperty("shover", true);
		enemy.setProperty("facingRight", false);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		enemy.addControl(new PhysicsControl(consApp.physics));
		enemy.addControl(new GentlemanControl(consApp, consApp.player));
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
		enemy.addFXGLEventHandler(Event.ENEMY_FIRED, event -> {
			if((boolean)enemy.getProperty("jumping")){
				consApp.consController.enemyShootProjectile(Element.METAL, enemy);
				consApp.soundManager.playSFX(FileNames.RIFLE_SHOT);
				consApp.getTimerManager().runOnceAfter(() -> {
					if(enemy != null && enemy.getControl(GentlemanControl.class) != null){			
						consApp.consController.enemyShootProjectile(Element.METAL, enemy);
						consApp.soundManager.playSFX(FileNames.RIFLE_SHOT);
					}			
				}, Config.ENEMY_SCORPION_DELAY);	
				consApp.getTimerManager().runOnceAfter(() -> {
					if(enemy != null && enemy.getControl(GentlemanControl.class) != null){	
						consApp.consController.enemyShootProjectile(Element.METAL, enemy);
						consApp.soundManager.playSFX(FileNames.RIFLE_SHOT);
					}			
				}, Config.ENEMY_SCORPION_DELAY.multiply(2));	
			}
			else if((enemy != null && enemy.getControl(PhysicsControl.class) != null && enemy.getControl(PhysicsControl.class).getVelocity().getX() != 0)){
				consApp.consController.enemyShootProjectile(Element.METAL, enemy);
				consApp.soundManager.playSFX(FileNames.RIFLE_SHOT);
				consApp.getTimerManager().runOnceAfter(() -> {
					if(enemy != null && enemy.getControl(GentlemanControl.class) != null){			
						consApp.consController.enemyShootProjectile(Element.METAL, enemy);
						consApp.soundManager.playSFX(FileNames.RIFLE_SHOT);
					}			
				}, Config.GENTLE_CATTACK_DELAY);	
				consApp.getTimerManager().runOnceAfter(() -> {
					if(enemy != null && enemy.getControl(GentlemanControl.class) != null){	
						consApp.consController.enemyShootProjectile(Element.METAL, enemy);
						consApp.soundManager.playSFX(FileNames.RIFLE_SHOT);
					}			
				}, Config.GENTLE_CATTACK_DELAY.multiply(2));	
				consApp.getTimerManager().runOnceAfter(() -> {
					if(enemy != null && enemy.getControl(GentlemanControl.class) != null){	
						consApp.consController.enemyShootProjectile(Element.METAL, enemy);
						consApp.soundManager.playSFX(FileNames.RIFLE_SHOT);
					}			
				}, Config.GENTLE_CATTACK_DELAY.multiply(3));
				consApp.getTimerManager().runOnceAfter(() -> {
					if(enemy != null && enemy.getControl(GentlemanControl.class) != null){	
						consApp.consController.enemyShootProjectile(Element.METAL, enemy);
						consApp.soundManager.playSFX(FileNames.RIFLE_SHOT);
					}			
				}, Config.GENTLE_CATTACK_DELAY.multiply(4));
			}
			else{
				consApp.consController.enemyShootProjectile(Element.METAL, enemy);
				consApp.soundManager.playSFX(FileNames.RIFLE_SHOT);
				if(enemy != null && enemy.getControl(GentlemanControl.class) != null){
					enemy.getControl(GentlemanControl.class).setAttackComplete(true, true);
				}				
			}
			
			consApp.getTimerManager().runOnceAfter(() -> {
				//TODO: Change enemy animation
			}, Config.CONSUME_DECAY);
		});

		return enemy;
	}
	

	public Entity spawnShakaBoss(Point2D spawnPoint) {
		Entity enemy = new Entity(Type.ENEMY);
		Rectangle rect = new Rectangle(30, 30);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setVisible(true);
		enemy.setCollidable(true);
		enemy.setProperty(Property.DATA, new Enemy(consApp.assets.getText("enemies/enemy_FireElemental.txt")));
		enemy.setProperty(Property.SUB_TYPE, Type.BOSS);
		enemy.setProperty("physics", consApp.physics);
		enemy.setProperty("shover", true);
		enemy.setProperty("facingRight", false);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		enemy.addControl(new PhysicsControl(consApp.physics));
		enemy.addControl(new ShakaControl(consApp, consApp.player));
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
		enemy.addFXGLEventHandler(Event.ENEMY_FIRED, event -> {
			if((enemy != null && enemy.getControl(PhysicsControl.class) != null && enemy.getControl(PhysicsControl.class).getVelocity().getX() != 0)){
				consApp.consController.enemyShootProjectile(Element.NEUTRAL2, enemy);
				consApp.getTimerManager().runOnceAfter(() -> {
					if(enemy != null && enemy.getControl(ShakaControl.class) != null){			
						consApp.consController.enemyShootProjectile(Element.NEUTRAL2, enemy);
					}			
				}, Config.SHAKA_CATTACK_DELAY);	
				consApp.getTimerManager().runOnceAfter(() -> {
					if(enemy != null && enemy.getControl(ShakaControl.class) != null){	
						consApp.consController.enemyShootProjectile(Element.NEUTRAL2, enemy);
					}			
				}, Config.SHAKA_CATTACK_DELAY.multiply(2));	
				consApp.getTimerManager().runOnceAfter(() -> {
					if(enemy != null && enemy.getControl(ShakaControl.class) != null){	
						consApp.consController.enemyShootProjectile(Element.NEUTRAL2, enemy);
					}			
				}, Config.SHAKA_CATTACK_DELAY.multiply(3));	
				consApp.getTimerManager().runOnceAfter(() -> {
					if(enemy != null && enemy.getControl(ShakaControl.class) != null){	
						consApp.consController.enemyShootProjectile(Element.NEUTRAL2, enemy);
					}			
				}, Config.SHAKA_CATTACK_DELAY.multiply(4));	
				consApp.getTimerManager().runOnceAfter(() -> {
					if(enemy != null && enemy.getControl(ShakaControl.class) != null){	
						consApp.consController.enemyShootProjectile(Element.NEUTRAL2, enemy);
					}			
				}, Config.SHAKA_CATTACK_DELAY.multiply(5));
				consApp.getTimerManager().runOnceAfter(() -> {
					if(enemy != null && enemy.getControl(ShakaControl.class) != null){	
						consApp.consController.enemyShootProjectile(Element.NEUTRAL2, enemy);
					}			
				}, Config.SHAKA_CATTACK_DELAY.multiply(6));	
			}
			else if((enemy != null && enemy.getControl(PhysicsControl.class) != null && enemy.getControl(PhysicsControl.class).getVelocity().getX() == 0)){
				consApp.consController.enemyShootProjectile(Element.NEUTRAL, enemy);
				consApp.getTimerManager().runOnceAfter(() -> {
					if(enemy != null && enemy.getControl(ShakaControl.class) != null){			
						enemy.getControl(ShakaControl.class).setAttackComplete(true, true);
					}			
				}, Config.SHAKA_SPEAR_DECAY);	
			}
			
			consApp.getTimerManager().runOnceAfter(() -> {
				//TODO: Change enemy animation
			}, Config.CONSUME_DECAY);
		});

		return enemy;
	}

	private void onEnemyDeath(FXGLEvent event) {
		Entity enemy = event.getTarget();
		consApp.getSceneManager().removeEntity(enemy);

		// chance based drop logic
		if (consApp.getRandom().nextInt(100) <= 33 || enemy.getProperty(Property.SUB_TYPE) == Type.BOSS) { // check if dropping
			ArrayList<Powerup> drops = new ArrayList<>();
			drops.add(Powerup.RESTORE_HEALTH_12);
			drops.add(Powerup.RESTORE_MANA_12);

			Player p = consApp.player.getProperty(Property.DATA);
			double hpPercent = p.getCurrentHealth() * 1.0 / p.getMaxHealth();
			double manaPercent = p.getCurrentMana() * 1.0 / p.getMaxMana();

			if (hpPercent < 0.75)
				drops.add(Powerup.RESTORE_HEALTH_25);

			if (hpPercent < 0.5)
				drops.add(Powerup.RESTORE_HEALTH_50);

			if (manaPercent < 0.75)
				drops.add(Powerup.RESTORE_MANA_25);

			if (hpPercent < 0.5)
				drops.add(Powerup.RESTORE_MANA_50);

			if (hpPercent > manaPercent) {
				drops.remove(Powerup.RESTORE_HEALTH_12);
				drops.remove(Powerup.RESTORE_HEALTH_25);
				drops.remove(Powerup.RESTORE_HEALTH_50);
			} else {
				drops.remove(Powerup.RESTORE_MANA_12);
				drops.remove(Powerup.RESTORE_MANA_25);
				drops.remove(Powerup.RESTORE_MANA_50);
			}

			Collections.shuffle(drops);

			Entity e = new Entity(Type.POWERUP);
			e.setPosition((int) enemy.getPosition().getX(), (int) enemy.getPosition().getY());
			if(enemy.getProperty(Property.SUB_TYPE) == Type.BOSS){
				Enemy ed = enemy.getProperty(Property.DATA);
				e.setProperty(Property.SUB_TYPE, getPowerup(ed.curElement.getValue()));
			}
			else{
				e.setProperty(Property.SUB_TYPE, drops.get(0));
			}
			
			Rectangle r = new Rectangle(30, 30);
			r.setFill(Color.PINK);
			e.setGraphics(r);
			e.addControl(new PhysicsControl(consApp.physics));
			e.setCollidable(true);

			e.addFXGLEventHandler(Event.DEATH, new FXGLEventHandler() {
				@Override
				public void handle(FXGLEvent event) {
					consApp.getSceneManager().removeEntity(e);
				}
			});

			consApp.getSceneManager().addEntities(e);
		}
	}

	private Powerup getPowerup(Element cur) {
		switch(cur){
			case NEUTRAL2:{
				return Powerup.NEUTRAL2;
			}
			case FIRE:{
				return Powerup.FIRE;
			}
			case EARTH:{
				return Powerup.EARTH;
			}
			case LIGHTNING:{
				return Powerup.LIGHTNING;
			}
			case METAL:{
				return Powerup.METAL;
			}
			case DEATH:{
				return Powerup.DEATH;
			}
			case CONSUME:{
				return Powerup.CONSUME;
			}
			default:{
				return Powerup.NEUTRAL;
			}
		}
	}
}