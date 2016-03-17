package com.ergo21.consume;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;

import com.almasb.consume.Config;
import com.almasb.consume.ConsumeApp;
import com.almasb.consume.Event;
import com.almasb.consume.Types;
import com.almasb.consume.Config.Speed;
import com.almasb.consume.Types.Element;
import com.almasb.consume.Types.Powerup;
import com.almasb.consume.Types.Property;
import com.almasb.consume.Types.Type;
import com.almasb.consume.ai.AnimatedElephantControl;
import com.almasb.consume.ai.AnimatedElephantControl.AnimationDetailsE;
import com.almasb.consume.ai.AnimatedEnemyControl;
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
import com.almasb.consume.ai.EshuControl;
import com.almasb.consume.ai.EshuIControl;
import com.almasb.consume.ai.GentlemanControl;
import com.almasb.consume.ai.KiboControl;
import com.almasb.consume.ai.KniferControl;
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
import com.almasb.consume.ai.AnimatedEnemyControl.AnimationDetails;
import com.almasb.fxgl.asset.Texture;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;
import com.almasb.fxgl.entity.FXGLEventHandler;

public class EntitySpawner {
	private ConsumeApp consApp;

	public EntitySpawner(ConsumeApp a) {
		consApp = a;
	}

	//TODO: Not currently used
	public Entity spawnEnemy(Point2D spawnPoint) {
		Entity enemy = new Entity(Type.ENEMY);
		Rectangle rect = new Rectangle(30, 30);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setCollidable(true);
		enemy.setProperty(Property.DATA, new Enemy(consApp.getAssetManager().loadText("enemies/enemy_FireElemental.txt")));
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

	public Pair<Entity, Entity> spawnCharger(Point2D spawnPoint) {
		Entity enemy = new Entity(Type.ENEMY);
		Rectangle rect = new Rectangle(20, 30);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setCollidable(true);
		enemy.setProperty(Property.DATA, new Enemy(consApp.getAssetManager().loadText("enemies/enemy_FireElemental.txt")));
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
		
		enemy.setVisible(false);
		enemy.setProperty("jumping", false);
		enemy.setProperty("attacking", true);
		
		Entity ePic = new Entity(Type.ENEMY);
		ePic.setVisible(true);
		ePic.setCollidable(false);
		ePic.setPosition(spawnPoint.getX(), spawnPoint.getY());
		ePic.translateXProperty().bind(enemy.translateXProperty());
		ePic.translateYProperty().bind(enemy.translateYProperty());
		HashMap<Types.AnimationActions, AnimationDetails> hashMap = new HashMap<>();
		for(Types.AnimationActions aa : Types.AnimationActions.values()){
			switch(aa){
				case ATK: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0,  0, 300, 300), 1, 1, true)); break;
				case IDLE: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 0, 300, 300), 1, 1, true)); break;
				case JATK: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 1200, 300, 300), 1, 1, true)); break;
				case JUMP: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 1200, 300, 300), 1, 1, true)); break;
				case MATK: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 300, 1200, 300), 4, 1, true)); break;
				case MOVE: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 300, 1200, 300), 4, 1, true)); break;
				default: break;
			}
		}
		ePic.addControl(new AnimatedEnemyControl(enemy, hashMap, consApp.getTexture(FileNames.ZULU_SH_TEX)));
		ePic.addFXGLEventHandler(Event.DEATH, (event) -> consApp.getSceneManager().removeEntity(ePic));
		enemy.aliveProperty().addListener(new ChangeListener<Boolean>(){
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
				if(!arg2){
					consApp.getTimerManager().runOnceAfter(() -> ePic.fireFXGLEvent(new FXGLEvent(Event.DEATH)), Duration.seconds(0.01));
				}
			}});
		
		//consApp.getSceneManager().addEntities(ePic);

		return new Pair<Entity, Entity>(enemy, ePic);
	}

	public Pair<Entity, Entity> spawnScarab(Point2D spawnPoint) {
		Entity enemy = new Entity(Type.ENEMY);
		Rectangle rect = new Rectangle(35, 15);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setCollidable(true);
		enemy.setProperty(Property.DATA, new Enemy(consApp.getAssetManager().loadText("enemies/enemy_FireElemental.txt")));
		enemy.setProperty("physics", consApp.physics);
		enemy.setProperty("shover", true);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		enemy.addControl(new PhysicsControl(consApp.physics));
		enemy.addControl(new SimpleJumpControl(consApp, consApp.player, Speed.ENEMY_JUMP));
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
		
		enemy.setVisible(false);
		enemy.setProperty("jumping", false);
		enemy.setProperty("attacking", false);
		
		Entity ePic = new Entity(Type.ENEMY);
		ePic.setVisible(true);
		ePic.setCollidable(false);
		ePic.setPosition(spawnPoint.getX(), spawnPoint.getY());
		ePic.translateXProperty().bind(enemy.translateXProperty().add(10));
		ePic.translateYProperty().bind(enemy.translateYProperty().add(-15));
		int fS = 300;
		HashMap<Types.AnimationActions, AnimationDetails> hashMap = new HashMap<>();
		for(Types.AnimationActions aa : Types.AnimationActions.values()){
			switch(aa){
				case ATK: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   0*fS, 1*fS, fS), 1, 1, true)); break;
				case IDLE: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   0*fS, 1*fS, fS), 1, 1, true)); break;
				case JATK: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   4*fS, 1*fS, fS), 1, 1, true)); break;
				case JUMP: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   4*fS, 1*fS, fS), 1, 1, true)); break;
				case MATK: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   0*fS, 1*fS, fS), 1, 1, true)); break;
				case MOVE: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   0*fS, 1*fS, fS), 1, 1, true)); break;
				default: break;
			}
		}
		ePic.addControl(new AnimatedEnemyControl(enemy, hashMap, consApp.getTexture(FileNames.SCARAB_TEX)));
		ePic.addFXGLEventHandler(Event.DEATH, (event) -> consApp.getSceneManager().removeEntity(ePic));
		enemy.aliveProperty().addListener(new ChangeListener<Boolean>(){
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
				if(!arg2){
					consApp.getTimerManager().runOnceAfter(() -> ePic.fireFXGLEvent(new FXGLEvent(Event.DEATH)), Duration.seconds(0.01));
				}
			}});
		
		//consApp.getSceneManager().addEntities(ePic);

		return new Pair<Entity, Entity>(enemy, ePic);
	}

	public Pair<Entity, Entity> spawnLocust(Point2D spawnPoint) {
		Entity enemy = new Entity(Type.ENEMY);
		Rectangle rect = new Rectangle(30, 30);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setCollidable(true);
		enemy.setProperty(Property.DATA, new Enemy(consApp.getAssetManager().loadText("enemies/enemy_FireElemental.txt")));
		enemy.setProperty("physics", consApp.physics);
		enemy.setProperty("shover", true);
		enemy.setProperty("facingRight", false);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		enemy.addControl(new PhysicsControl(consApp.physics));
		enemy.addControl(new DiveBombControl(consApp.player, enemy.getPosition().getY()));
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
		enemy.setProperty(Property.ENABLE_GRAVITY, false);
		
		
		enemy.setVisible(false);
		enemy.setProperty("jumping", false);
		enemy.setProperty("attacking", false);
		
		Entity ePic = new Entity(Type.ENEMY);
		ePic.setVisible(true);
		ePic.setCollidable(false);
		ePic.setPosition(spawnPoint.getX(), spawnPoint.getY());
		ePic.translateXProperty().bind(enemy.translateXProperty().add(5));
		ePic.translateYProperty().bind(enemy.translateYProperty());
		int fS = 300;
		HashMap<Types.AnimationActions, AnimationDetails> hashMap = new HashMap<>();
		for(Types.AnimationActions aa : Types.AnimationActions.values()){
			switch(aa){
				case ATK: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   1*fS, 2*fS, fS), 2, 0.05, true)); break;
				case IDLE: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   1*fS, 2*fS, fS), 2, 0.05, true)); break;
				case JATK: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   1*fS, 2*fS, fS), 2, 0.05, true)); break;
				case JUMP: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   1*fS, 2*fS, fS), 2, 0.05, true)); break;
				case MATK: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   1*fS, 2*fS, fS), 2, 0.05, true)); break;
				case MOVE: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   1*fS, 2*fS, fS), 2, 0.05, true)); break;
				default: break;
			}
		}
		ePic.addControl(new AnimatedEnemyControl(enemy, hashMap, consApp.getTexture(FileNames.LOCUST_TEX)));
		ePic.addFXGLEventHandler(Event.DEATH, (event) -> consApp.getSceneManager().removeEntity(ePic));
		enemy.aliveProperty().addListener(new ChangeListener<Boolean>(){
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
				if(!arg2){
					consApp.getTimerManager().runOnceAfter(() -> ePic.fireFXGLEvent(new FXGLEvent(Event.DEATH)), Duration.seconds(0.01));
				}
			}});
		
		////consApp.getSceneManager().addEntities(ePic);

		return new Pair<Entity, Entity>(enemy, ePic);
	}
	
	public Pair<Entity, Entity> spawnEloko(Point2D spawnPoint){
		Entity enemy = new Entity(Type.ENEMY);
		Rectangle rect = new Rectangle(20, 30);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setCollidable(true);
		enemy.setProperty(Property.DATA, new Enemy(consApp.getAssetManager().loadText(FileNames.STATS_ELOKO)));
		enemy.setProperty("physics", consApp.physics);
		enemy.setProperty("facingRight", true);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		enemy.addControl(new PhysicsControl(consApp.physics));
		enemy.addControl(new ConsumeControl(consApp.player));
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
		enemy.addFXGLEventHandler(Event.ENEMY_FIRED, event -> {
			//consApp.consController.enemyShootProjectile(Element.NEUTRAL2, enemy);
		});
		
		enemy.setVisible(false);
		enemy.setProperty("jumping", false);
		enemy.setProperty("attacking", false);
		
		Entity ePic = new Entity(Type.ENEMY);
		ePic.setVisible(true);
		ePic.setCollidable(false);
		ePic.setPosition(spawnPoint.getX(), spawnPoint.getY());
		ePic.translateXProperty().bind(enemy.translateXProperty());
		ePic.translateYProperty().bind(enemy.translateYProperty());
		int fS = 300;
		HashMap<Types.AnimationActions, AnimationDetails> hashMap = new HashMap<>();
		for(Types.AnimationActions aa : Types.AnimationActions.values()){
			switch(aa){
				case ATK: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   1*fS, 4*fS, fS), 4, 1, true)); break;
				case IDLE: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   0*fS, 1*fS, fS), 1, 1, true)); break;
				case JATK: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   1*fS, 4*fS, fS), 4, 1, true)); break;
				case JUMP: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   1*fS, 4*fS, fS), 4, 1, true)); break;
				case MATK: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   1*fS, 4*fS, fS), 4, 1, true)); break;
				case MOVE: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   1*fS, 4*fS, fS), 4, 1, true)); break;
				default: break;
			}
		}
		ePic.addControl(new AnimatedEnemyControl(enemy, hashMap, consApp.getTexture(FileNames.ELOKO_TEX)));
		ePic.addFXGLEventHandler(Event.DEATH, (event) -> consApp.getSceneManager().removeEntity(ePic));
		enemy.aliveProperty().addListener(new ChangeListener<Boolean>(){
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
				if(!arg2){
					consApp.getTimerManager().runOnceAfter(() -> ePic.fireFXGLEvent(new FXGLEvent(Event.DEATH)), Duration.seconds(0.01));
				}
			}});
		
		//consApp.getSceneManager().addEntities(ePic);

		return new Pair<Entity, Entity>(enemy, ePic);
	}
	
	public Pair<Entity, Entity> spawnMummy(Point2D spawnPoint){
		Entity enemy = new Entity(Type.ENEMY);
		Rectangle rect = new Rectangle(20, 30);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setCollidable(true);
		enemy.setProperty(Property.DATA, new Enemy(consApp.getAssetManager().loadText("enemies/enemy_FireElemental.txt")));
		enemy.setProperty("physics", consApp.physics);
		enemy.setProperty("facingRight", true);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		enemy.addControl(new PhysicsControl(consApp.physics));
		enemy.addControl(new MummyControl(consApp.player));
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
		enemy.addFXGLEventHandler(Event.ENEMY_FIRED, event -> {
			//consApp.consController.enemyShootProjectile(Element.NEUTRAL2, enemy);
		});
		
		enemy.setVisible(false);
		enemy.setProperty("jumping", false);
		enemy.setProperty("attacking", false);
		
		Entity ePic = new Entity(Type.ENEMY);
		ePic.setVisible(true);
		ePic.setCollidable(false);
		ePic.setPosition(spawnPoint.getX(), spawnPoint.getY());
		ePic.translateXProperty().bind(enemy.translateXProperty());
		ePic.translateYProperty().bind(enemy.translateYProperty());
		int fS = 300;
		HashMap<Types.AnimationActions, AnimationDetails> hashMap = new HashMap<>();
		for(Types.AnimationActions aa : Types.AnimationActions.values()){
			switch(aa){
				case ATK: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   1*fS, 4*fS, fS), 4, 1, true)); break;
				case IDLE: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   1*fS, 4*fS, fS), 4, 1, true)); break;
				case JATK: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   1*fS, 4*fS, fS), 4, 1, true)); break;
				case JUMP: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   1*fS, 4*fS, fS), 4, 1, true)); break;
				case MATK: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   1*fS, 4*fS, fS), 4, 1, true)); break;
				case MOVE: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   1*fS, 4*fS, fS), 4, 1, true)); break;
				default: break;
			}
		}
		ePic.addControl(new AnimatedEnemyControl(enemy, hashMap, consApp.getTexture(FileNames.MUMMY_TEX)));
		ePic.addFXGLEventHandler(Event.DEATH, (event) -> consApp.getSceneManager().removeEntity(ePic));
		enemy.aliveProperty().addListener(new ChangeListener<Boolean>(){
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
				if(!arg2){
					consApp.getTimerManager().runOnceAfter(() -> ePic.fireFXGLEvent(new FXGLEvent(Event.DEATH)), Duration.seconds(0.01));
				}
			}});
		
		//consApp.getSceneManager().addEntities(ePic);

		return new Pair<Entity, Entity>(enemy, ePic);
	}
	
	public Pair<Entity, Entity> spawnBSpearEnemy(Point2D spawnPoint){
		Entity enemy = new Entity(Type.ENEMY);
		Rectangle rect = new Rectangle(20, 30);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setCollidable(true);
		enemy.setProperty(Property.DATA, new Enemy(consApp.getAssetManager().loadText(FileNames.STATS_BANDIT_SPEAR)));
		enemy.setProperty("physics", consApp.physics);
		enemy.setProperty("facingRight", true);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		enemy.addControl(new PhysicsControl(consApp.physics));
		enemy.addControl(new SpearThrowerControl(consApp.player));
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
		enemy.addFXGLEventHandler(Event.ENEMY_FIRED, event -> {
			consApp.consController.enemyShootProjectile(Element.NEUTRAL, enemy);
			enemy.setProperty("attacking", true);
			consApp.getTimerManager().runOnceAfter(() -> {
				enemy.setProperty("attacking", false);
			}, Config.ENEMY_SPEAR_DECAY.divide(2));
			consApp.getTimerManager().runOnceAfter(() -> {
				if(enemy != null && enemy.getControl(SpearThrowerControl.class) != null){
					enemy.getControl(SpearThrowerControl.class).setSpearThrown(false);
				}			
			}, Config.ENEMY_SPEAR_DECAY);
		});
		
		enemy.setVisible(false);
		enemy.setProperty("jumping", false);
		enemy.setProperty("attacking", false);
		
		Entity ePic = new Entity(Type.ENEMY);
		ePic.setVisible(true);
		ePic.setCollidable(false);
		ePic.setPosition(spawnPoint.getX(), spawnPoint.getY());
		ePic.translateXProperty().bind(enemy.translateXProperty());
		ePic.translateYProperty().bind(enemy.translateYProperty());
		HashMap<Types.AnimationActions, AnimationDetails> hashMap = new HashMap<>();
		for(Types.AnimationActions aa : Types.AnimationActions.values()){
			switch(aa){
				case ATK: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 900, 300, 300), 1, 1, true)); break;
				case IDLE: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 0, 300, 300), 1, 1, true)); break;
				case JATK: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 900, 300, 300), 1, 1, true)); break;
				case JUMP: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 300, 1200, 300), 4, 1, true)); break;
				case MATK: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 600, 600, 300), 2, 1, true)); break;
				case MOVE: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 300, 1200, 300), 4, 1, true)); break;
				default: break;
			}
		}
		ePic.addControl(new AnimatedEnemyControl(enemy, hashMap, consApp.getTexture(FileNames.BANDIT_S_TEX)));
		ePic.addFXGLEventHandler(Event.DEATH, (event) -> consApp.getSceneManager().removeEntity(ePic));
		enemy.aliveProperty().addListener(new ChangeListener<Boolean>(){
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
				if(!arg2){
					consApp.getTimerManager().runOnceAfter(() -> ePic.fireFXGLEvent(new FXGLEvent(Event.DEATH)), Duration.seconds(0.01));
				}
			}});
		
		//consApp.getSceneManager().addEntities(ePic);

		return new Pair<Entity, Entity>(enemy, ePic);
	}
	
	public Pair<Entity, Entity> spawnZSpearEnemy(Point2D spawnPoint){
		Entity enemy = new Entity(Type.ENEMY);
		Rectangle rect = new Rectangle(20, 30);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setCollidable(true);
		enemy.setProperty(Property.DATA, new Enemy(consApp.getAssetManager().loadText("enemies/enemy_FireElemental.txt")));
		enemy.setProperty("physics", consApp.physics);
		enemy.setProperty("facingRight", true);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		enemy.addControl(new PhysicsControl(consApp.physics));
		enemy.addControl(new SpearThrowerControl(consApp.player));
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
		enemy.addFXGLEventHandler(Event.ENEMY_FIRED, event -> {
			consApp.consController.enemyShootProjectile(Element.NEUTRAL, enemy);
			enemy.setProperty("attacking", true);
			consApp.getTimerManager().runOnceAfter(() -> {
				enemy.setProperty("attacking", false);
			}, Config.ENEMY_SPEAR_DECAY.divide(2));
			consApp.getTimerManager().runOnceAfter(() -> {
				if(enemy != null && enemy.getControl(SpearThrowerControl.class) != null){
					enemy.getControl(SpearThrowerControl.class).setSpearThrown(false);
				}			
			}, Config.ENEMY_SPEAR_DECAY);
		});
		
		enemy.setVisible(false);
		enemy.setProperty("jumping", false);
		enemy.setProperty("attacking", false);
		
		Entity ePic = new Entity(Type.ENEMY);
		ePic.setVisible(true);
		ePic.setCollidable(false);
		ePic.setPosition(spawnPoint.getX(), spawnPoint.getY());
		ePic.translateXProperty().bind(enemy.translateXProperty());
		ePic.translateYProperty().bind(enemy.translateYProperty());
		HashMap<Types.AnimationActions, AnimationDetails> hashMap = new HashMap<>();
		for(Types.AnimationActions aa : Types.AnimationActions.values()){
			switch(aa){
				case ATK: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 900, 300, 300), 1, 1, true)); break;
				case IDLE: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 0, 300, 300), 1, 1, true)); break;
				case JATK: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 900, 300, 300), 1, 1, true)); break;
				case JUMP: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 300, 1200, 300), 4, 1, true)); break;
				case MATK: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 300, 1200, 300), 4, 1, true)); break;
				case MOVE: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 300, 1200, 300), 4, 1, true)); break;
				default: break;
			}
		}
		ePic.addControl(new AnimatedEnemyControl(enemy, hashMap, consApp.getTexture(FileNames.ZULU_SP_TEX)));
		ePic.addFXGLEventHandler(Event.DEATH, (event) -> consApp.getSceneManager().removeEntity(ePic));
		enemy.aliveProperty().addListener(new ChangeListener<Boolean>(){
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
				if(!arg2){
					consApp.getTimerManager().runOnceAfter(() -> ePic.fireFXGLEvent(new FXGLEvent(Event.DEATH)), Duration.seconds(0.01));
				}
			}});
		
		//consApp.getSceneManager().addEntities(ePic);

		return new Pair<Entity, Entity>(enemy, ePic);
	}
	
	public Pair<Entity, Entity> spawnScorpion(Point2D spawnPoint){
		Entity enemy = new Entity(Type.ENEMY);
		Rectangle rect = new Rectangle(25, 20);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setCollidable(true);
		enemy.setProperty(Property.DATA, new Enemy(consApp.getAssetManager().loadText("enemies/enemy_FireElemental.txt")));
		enemy.setProperty("physics", consApp.physics);
		enemy.setProperty("facingRight", true);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		enemy.addControl(new PhysicsControl(consApp.physics));
		enemy.addControl(new ScorpionControl(consApp.player));
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
		enemy.addFXGLEventHandler(Event.ENEMY_FIRED, event -> {
			enemy.setProperty("attacking", true);
			consApp.getTimerManager().runOnceAfter(() -> {
				if(enemy != null && enemy.getControl(ScorpionControl.class) != null){
					consApp.consController.enemyShootProjectile(Element.METAL, enemy);
				}			
			}, Config.ENEMY_SCORPION_DELAY);
			consApp.getTimerManager().runOnceAfter(() -> {
				if(enemy != null && enemy.getControl(ScorpionControl.class) != null){
					enemy.setProperty("attacking", false);
					enemy.getControl(ScorpionControl.class).setSpearThrown(false);
				}			
			}, Config.ENEMY_SCORPION_DECAY);
		});
		
		enemy.setVisible(false);
		enemy.setProperty("jumping", false);
		enemy.setProperty("attacking", false);
		
		Entity ePic = new Entity(Type.ENEMY);
		ePic.setVisible(true);
		ePic.setCollidable(false);
		ePic.setPosition(spawnPoint.getX(), spawnPoint.getY());
		ePic.translateXProperty().bind(enemy.translateXProperty().add(5));
		ePic.translateYProperty().bind(enemy.translateYProperty().add(-10));
		int fS = 300;
		HashMap<Types.AnimationActions, AnimationDetails> hashMap = new HashMap<>();
		for(Types.AnimationActions aa : Types.AnimationActions.values()){
			switch(aa){
				case ATK: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   3*fS, 1*fS, fS), 1, 1, true)); break;
				case IDLE: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   0*fS, 1*fS, fS), 1, 1, true)); break;
				case JATK: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   3*fS, 1*fS, fS), 1, 1, true)); break;
				case JUMP: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   0*fS, 1*fS, fS), 1, 1, true)); break;
				case MATK: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   1*fS, 4*fS, fS), 4, 1, true)); break;
				case MOVE: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   1*fS, 4*fS, fS), 4, 1, true)); break;
				default: break;
			}
		}
		ePic.addControl(new AnimatedEnemyControl(enemy, hashMap, consApp.getTexture(FileNames.SCORPION_TEX)));
		ePic.addFXGLEventHandler(Event.DEATH, (event) -> consApp.getSceneManager().removeEntity(ePic));
		enemy.aliveProperty().addListener(new ChangeListener<Boolean>(){
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
				if(!arg2){
					consApp.getTimerManager().runOnceAfter(() -> ePic.fireFXGLEvent(new FXGLEvent(Event.DEATH)), Duration.seconds(0.01));
				}
			}});
		
		//consApp.getSceneManager().addEntities(ePic);

		return new Pair<Entity, Entity>(enemy, ePic);
	}
	
	//TODO: Not currently used
	public Entity spawnShooter(Point2D spawnPoint){
		Entity enemy = new Entity(Type.ENEMY);
		Rectangle rect = new Rectangle(30, 30);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setCollidable(false);
		enemy.setProperty(Property.DATA, new Enemy(consApp.getAssetManager().loadText("enemies/enemy_FireElemental.txt")));
		enemy.setProperty("physics", consApp.physics);
		enemy.setProperty("facingRight", false);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		enemy.addControl(new PhysicsControl(consApp.physics));
		enemy.addControl(new ShooterControl(consApp.player));
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
		enemy.addFXGLEventHandler(Event.ENEMY_FIRED, event -> {
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
	
	public Pair<Entity, Entity> spawnBurner(Point2D spawnPoint){
		Entity enemy = new Entity(Type.ENEMY);
		Rectangle rect = new Rectangle(40, 40);
		rect.setFill(Color.RED);

		enemy.setCollidable(false);
		enemy.setProperty(Property.DATA, new Enemy(consApp.getAssetManager().loadText("enemies/enemy_FireElemental.txt")));
		enemy.setProperty("physics", consApp.physics);
		enemy.setProperty("facingRight", false);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		enemy.addControl(new BurnerControl(consApp.player));
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
		enemy.addFXGLEventHandler(Event.ENEMY_FIRED, event -> {
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
		
		Entity ePic = new Entity(Type.ENEMY);
		ePic.setVisible(true);
		ePic.setCollidable(false);
		Texture t = consApp.getTexture(FileNames.COFFIN_BLOCK);
		t.setPreserveRatio(true);
		t.setFitHeight(80);
		ePic.setGraphics(t);
		ePic.setPosition(spawnPoint.getX(), spawnPoint.getY());
		ePic.translateXProperty().bind(enemy.translateXProperty());
		ePic.translateYProperty().bind(enemy.translateYProperty());
		ePic.addFXGLEventHandler(Event.DEATH, (event) -> consApp.getSceneManager().removeEntity(ePic));
		enemy.aliveProperty().addListener(new ChangeListener<Boolean>(){
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
				if(!arg2){
					consApp.getTimerManager().runOnceAfter(() -> ePic.fireFXGLEvent(new FXGLEvent(Event.DEATH)), Duration.seconds(0.01));
				}
			}});

		return new Pair<Entity, Entity>(enemy, ePic);
	}
	
	public Pair<Entity, Entity> spawnStoneEnemy(Point2D spawnPoint){
		Entity enemy = new Entity(Type.ENEMY);
		Rectangle rect = new Rectangle(20, 40);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setCollidable(true);
		enemy.setProperty(Property.DATA, new Enemy(consApp.getAssetManager().loadText("enemies/enemy_FireElemental.txt")));
		enemy.setProperty("physics", consApp.physics);
		enemy.setProperty("facingRight", false);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		enemy.addControl(new PhysicsControl(consApp.physics));
		enemy.addControl(new StoneThrowerControl(consApp.player));
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
		enemy.addFXGLEventHandler(Event.ENEMY_FIRED, event -> {
			consApp.consController.enemyShootStones(enemy);
			enemy.setProperty("attacking", true);
			consApp.getTimerManager().runOnceAfter(() -> {
				if(enemy != null && enemy.getControl(StoneThrowerControl.class) != null){
					enemy.getControl(StoneThrowerControl.class).setStonesThrown(false);
				}			
			}, Config.ENEMY_STONE_THROW_RECHARGE);
			consApp.getTimerManager().runOnceAfter(() -> {
				if(enemy != null){
					enemy.setProperty("attacking", false);
				}
			}, Config.ENEMY_STONE_THROW_DELAY.multiply(3.5));
		});
		
		enemy.setVisible(false);
		enemy.setProperty("jumping", false);
		enemy.setProperty("attacking", false);
		
		Entity ePic = new Entity(Type.ENEMY);
		ePic.setVisible(true);
		ePic.setCollidable(false);
		ePic.setPosition(spawnPoint.getX(), spawnPoint.getY());
		ePic.translateXProperty().bind(enemy.translateXProperty());
		ePic.translateYProperty().bind(enemy.translateYProperty().add(10));
		int fS = 300;
		HashMap<Types.AnimationActions, AnimationDetails> hashMap = new HashMap<>();
		for(Types.AnimationActions aa : Types.AnimationActions.values()){
			switch(aa){
				case ATK: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   3*fS, 2*fS, fS), 2, 1, false)); break;
				case IDLE: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   0*fS, 1*fS, fS), 1, 1, true)); break;
				case JATK: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(fS,  3*fS, 2*fS, fS), 2, 1, true)); break;
				case JUMP: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   1*fS, 1*fS, fS), 1, 1, true)); break;
				case MATK: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   3*fS, 2*fS, fS), 2, 1, false)); break;
				case MOVE: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   1*fS, 4*fS, fS), 4, 1, true)); break;
				default: break;
			}
		}
		ePic.addControl(new AnimatedEnemyControl(enemy, hashMap, consApp.getTexture(FileNames.GOLEM_TEX)));
		ePic.addFXGLEventHandler(Event.DEATH, (event) -> consApp.getSceneManager().removeEntity(ePic));
		enemy.aliveProperty().addListener(new ChangeListener<Boolean>(){
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
				if(!arg2){
					consApp.getTimerManager().runOnceAfter(() -> ePic.fireFXGLEvent(new FXGLEvent(Event.DEATH)), Duration.seconds(0.01));
				}
			}});
		
		//consApp.getSceneManager().addEntities(ePic);
		
		return new Pair<Entity, Entity>(enemy, ePic);
	}
	
	public Pair<Entity, Entity> spawnMusician(Point2D spawnPoint) {
		Entity enemy = new Entity(Type.ENEMY);
		Rectangle rect = new Rectangle(20, 30);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setCollidable(true);
		enemy.setProperty(Property.DATA, new Enemy(consApp.getAssetManager().loadText("enemies/enemy_FireElemental.txt")));
		enemy.setProperty("physics", consApp.physics);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		enemy.addControl(new PhysicsControl(consApp.physics));
		enemy.addControl(new MusicianControl(consApp, consApp.player, Speed.ENEMY_JUMP));
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
		enemy.addFXGLEventHandler(Event.ENEMY_FIRED, event -> {
			enemy.getControl(MusicianControl.class).setPlayed(true);
			consApp.consController.enemyPlayFlute(enemy);
		});
		
		enemy.setVisible(false);
		enemy.setProperty("jumping", false);
		enemy.setProperty("attacking", false);
		
		Entity ePic = new Entity(Type.ENEMY);
		ePic.setVisible(true);
		ePic.setCollidable(false);
		ePic.setPosition(spawnPoint.getX(), spawnPoint.getY());
		ePic.translateXProperty().bind(enemy.translateXProperty());
		ePic.translateYProperty().bind(enemy.translateYProperty());
		int fS = 300;
		HashMap<Types.AnimationActions, AnimationDetails> hashMap = new HashMap<>();
		for(Types.AnimationActions aa : Types.AnimationActions.values()){
			switch(aa){
				case ATK: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   3*fS, 1*fS, fS), 1, 1, true)); break;
				case IDLE: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   0*fS, 4*fS, fS), 4, 1, true)); break;
				case JATK: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(fS,  4*fS, 1*fS, fS), 1, 1, true)); break;
				case JUMP: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   4*fS, 1*fS, fS), 1, 1, true)); break;
				case MATK: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   0*fS, 4*fS, fS), 4, 1, true)); break;
				case MOVE: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   0*fS, 4*fS, fS), 4, 1, true)); break;
				default: break;
			}
		}
		ePic.addControl(new AnimatedEnemyControl(enemy, hashMap, consApp.getTexture(FileNames.MUSICIAN_TEX)));
		ePic.addFXGLEventHandler(Event.DEATH, (event) -> consApp.getSceneManager().removeEntity(ePic));
		enemy.aliveProperty().addListener(new ChangeListener<Boolean>(){
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
				if(!arg2){
					consApp.getTimerManager().runOnceAfter(() -> ePic.fireFXGLEvent(new FXGLEvent(Event.DEATH)), Duration.seconds(0.01));
				}
			}});
		
		//consApp.getSceneManager().addEntities(ePic);

		return new Pair<Entity, Entity>(enemy, ePic);
	}
	
	public Pair<Entity, Entity> spawnDancer(Point2D spawnPoint) {
		Entity enemy = new Entity(Type.ENEMY);
		Rectangle rect = new Rectangle(20, 30);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setCollidable(true);
		enemy.setProperty(Property.DATA, new Enemy(consApp.getAssetManager().loadText("enemies/enemy_FireElemental.txt")));
		enemy.setProperty("physics", consApp.physics);
		enemy.setProperty("shover", true);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		enemy.addControl(new PhysicsControl(consApp.physics));
		enemy.addControl(new ComplexJumpControl(consApp, consApp.player, Speed.ENEMY_JUMP));
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
		
		enemy.setVisible(false);
		enemy.setProperty("jumping", false);
		enemy.setProperty("attacking", false);
		
		Entity ePic = new Entity(Type.ENEMY);
		ePic.setVisible(true);
		ePic.setCollidable(false);
		ePic.setPosition(spawnPoint.getX(), spawnPoint.getY());
		ePic.translateXProperty().bind(enemy.translateXProperty());
		ePic.translateYProperty().bind(enemy.translateYProperty());
		int fS = 300;
		HashMap<Types.AnimationActions, AnimationDetails> hashMap = new HashMap<>();
		for(Types.AnimationActions aa : Types.AnimationActions.values()){
			switch(aa){
				case ATK: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   0*fS, 4*fS, fS), 4, 1, true)); break;
				case IDLE: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   0*fS, 4*fS, fS), 4, 1, true)); break;
				case JATK: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   4*fS, 1*fS, fS), 1, 1, true)); break;
				case JUMP: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   4*fS, 1*fS, fS), 1, 1, true)); break;
				case MATK: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   0*fS, 4*fS, fS), 4, 1, true)); break;
				case MOVE: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   0*fS, 4*fS, fS), 4, 1, true)); break;
				default: break;
			}
		}
		ePic.addControl(new AnimatedEnemyControl(enemy, hashMap, consApp.getTexture(FileNames.DANCERS_TEX)));
		ePic.addFXGLEventHandler(Event.DEATH, (event) -> consApp.getSceneManager().removeEntity(ePic));
		enemy.aliveProperty().addListener(new ChangeListener<Boolean>(){
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
				if(!arg2){
					consApp.getTimerManager().runOnceAfter(() -> ePic.fireFXGLEvent(new FXGLEvent(Event.DEATH)), Duration.seconds(0.01));
				}
			}});
		
		//consApp.getSceneManager().addEntities(ePic);

		return new Pair<Entity, Entity>(enemy, ePic);
	}
	
	public Pair<Entity, Entity> spawnDog(Point2D spawnPoint) {
		Entity enemy = new Entity(Type.ENEMY);
		Rectangle rect = new Rectangle(40, 20);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setCollidable(true);
		enemy.setProperty(Property.DATA, new Enemy(consApp.getAssetManager().loadText("enemies/enemy_FireElemental.txt")));
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
		
		enemy.setVisible(false);
		enemy.setProperty("jumping", false);
		enemy.setProperty("attacking", false);
		
		Entity ePic = new Entity(Type.ENEMY);
		ePic.setVisible(true);
		ePic.setCollidable(false);
		ePic.setPosition(spawnPoint.getX(), spawnPoint.getY());
		ePic.translateXProperty().bind(enemy.translateXProperty().add(10));
		ePic.translateYProperty().bind(enemy.translateYProperty().add(-10));
		int fS = 300;
		HashMap<Types.AnimationActions, AnimationDetails> hashMap = new HashMap<>();
		for(Types.AnimationActions aa : Types.AnimationActions.values()){
			switch(aa){
				case ATK: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   4*fS, 1*fS, fS), 1, 1, true)); break;
				case IDLE: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   0*fS, 1*fS, fS), 1, 1, true)); break;
				case JATK: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   4*fS, 1*fS, fS), 1, 1, true)); break;
				case JUMP: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   4*fS, 1*fS, fS), 1, 1, true)); break;
				case MATK: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   4*fS, 1*fS, fS), 1, 1, true)); break;
				case MOVE: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   1*fS, 4*fS, fS), 4, 1, true)); break;
				default: break;
			}
		}
		ePic.addControl(new AnimatedEnemyControl(enemy, hashMap, consApp.getTexture(FileNames.DOG_TEX)));
		ePic.addFXGLEventHandler(Event.DEATH, (event) -> consApp.getSceneManager().removeEntity(ePic));
		enemy.aliveProperty().addListener(new ChangeListener<Boolean>(){
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
				if(!arg2){
					consApp.getTimerManager().runOnceAfter(() -> ePic.fireFXGLEvent(new FXGLEvent(Event.DEATH)), Duration.seconds(0.01));
				}
			}});
		
		//consApp.getSceneManager().addEntities(ePic);

		return new Pair<Entity, Entity>(enemy, ePic);
	}
	
	public Pair<Entity, Entity> spawnIceSpirit(Point2D spawnPoint) {
		Entity enemy = new Entity(Type.ENEMY);
		Rectangle rect = new Rectangle(30, 30);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setCollidable(true);
		enemy.setProperty(Property.DATA, new Enemy(consApp.getAssetManager().loadText("enemies/enemy_FireElemental.txt")));
		enemy.setProperty("physics", consApp.physics);
		enemy.setProperty("shover", true);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		enemy.addControl(new PhysicsControl(consApp.physics));
		enemy.addControl(new DiveReturnControl(consApp.player, enemy.getPosition().getY()));
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
		enemy.setProperty(Property.ENABLE_GRAVITY, false);
		
		enemy.setVisible(false);
		enemy.setProperty("jumping", false);
		enemy.setProperty("attacking", false);
		
		Entity ePic = new Entity(Type.ENEMY);
		ePic.setVisible(true);
		ePic.setCollidable(false);
		ePic.setPosition(spawnPoint.getX(), spawnPoint.getY());
		ePic.translateXProperty().bind(enemy.translateXProperty().add(5));
		ePic.translateYProperty().bind(enemy.translateYProperty());
		int fS = 300;
		HashMap<Types.AnimationActions, AnimationDetails> hashMap = new HashMap<>();
		for(Types.AnimationActions aa : Types.AnimationActions.values()){
			switch(aa){
				case ATK: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   1*fS, 5*fS, fS), 5, 1, true)); break;
				case IDLE: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   1*fS, 5*fS, fS), 5, 1, true)); break;
				case JATK: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   1*fS, 5*fS, fS), 5, 1, true)); break;
				case JUMP: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   1*fS, 5*fS, fS), 5, 1, true)); break;
				case MATK: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   1*fS, 5*fS, fS), 5, 1, true)); break;
				case MOVE: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   1*fS, 5*fS, fS), 5, 1, true)); break;
				default: break;
			}
		}
		ePic.addControl(new AnimatedEnemyControl(enemy, hashMap, consApp.getTexture(FileNames.ICE_SPIRIT_TEX)));
		ePic.addFXGLEventHandler(Event.DEATH, (event) -> consApp.getSceneManager().removeEntity(ePic));
		enemy.aliveProperty().addListener(new ChangeListener<Boolean>(){
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
				if(!arg2){
					consApp.getTimerManager().runOnceAfter(() -> ePic.fireFXGLEvent(new FXGLEvent(Event.DEATH)), Duration.seconds(0.01));
				}
			}});
		
		//consApp.getSceneManager().addEntities(ePic);

		return new Pair<Entity, Entity>(enemy, ePic);
	}
	
	public Pair<Entity, Entity> spawnCannon(Point2D spawnPoint){
		Entity enemy = new Entity(Type.ENEMY);
		Rectangle rect = new Rectangle(40, 20);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setCollidable(true);
		enemy.setProperty(Property.DATA, new Enemy(consApp.getAssetManager().loadText("enemies/enemy_FireElemental.txt")));
		enemy.setProperty("physics", consApp.physics);
		enemy.setProperty("facingRight", false);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		enemy.addControl(new PhysicsControl(consApp.physics));
		enemy.addControl(new CannonControl(consApp.player));
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
		enemy.addFXGLEventHandler(Event.ENEMY_FIRED, event -> {
			consApp.getTimerManager().runOnceAfter(() -> {
				enemy.setProperty("attacking", false);
			}, Config.ENEMY_SCORPION_DELAY.multiply(2));
			consApp.getTimerManager().runOnceAfter(() -> {
				if(enemy != null && enemy.getControl(CannonControl.class) != null){
					consApp.consController.enemyShootCannon(enemy);
					enemy.setProperty("attacking", true);
				}			
			}, Config.ENEMY_SCORPION_DELAY.multiply(1.25));
			consApp.getTimerManager().runOnceAfter(() -> {
				if(enemy != null && enemy.getControl(CannonControl.class) != null){
					enemy.getControl(CannonControl.class).setSpearThrown(false);
				}			
			}, Config.ENEMY_SCORPION_DECAY.multiply(1.25));
		});
		
		enemy.setVisible(false);
		enemy.setProperty("jumping", false);
		enemy.setProperty("attacking", false);
		
		Entity ePic = new Entity(Type.ENEMY);
		ePic.setVisible(true);
		ePic.setCollidable(false);
		ePic.setPosition(spawnPoint.getX(), spawnPoint.getY());
		ePic.translateXProperty().bind(enemy.translateXProperty());
		ePic.translateYProperty().bind(enemy.translateYProperty().add(-10));
		HashMap<Types.AnimationActions, AnimationDetails> hashMap = new HashMap<>();
		for(Types.AnimationActions aa : Types.AnimationActions.values()){
			switch(aa){
				case ATK: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0,  900, 300, 300), 1, 1, true)); break;
				case IDLE: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 0, 600, 300), 2, 1, true)); break;
				case JATK: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 900, 300, 300), 1, 1, true)); break;
				case JUMP: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 0, 600, 300), 2, 1, true)); break;
				case MATK: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 900, 300, 300), 1, 1, true)); break;
				case MOVE: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 0, 600, 300), 2, 1, true)); break;
				default: break;
			}
		}
		ePic.addControl(new AnimatedEnemyControl(enemy, hashMap, consApp.getTexture(FileNames.CANNON_TEX)));
		ePic.addFXGLEventHandler(Event.DEATH, (event) -> consApp.getSceneManager().removeEntity(ePic));
		enemy.aliveProperty().addListener(new ChangeListener<Boolean>(){
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
				if(!arg2){
					consApp.getTimerManager().runOnceAfter(() -> ePic.fireFXGLEvent(new FXGLEvent(Event.DEATH)), Duration.seconds(0.01));
				}
			}});
		
		//consApp.getSceneManager().addEntities(ePic);

		return new Pair<Entity, Entity>(enemy, ePic);
	}
	
	public Pair<Entity, Entity> spawnRifler(Point2D spawnPoint){
		Entity enemy = new Entity(Type.ENEMY);
		Rectangle rect = new Rectangle(20, 30);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setCollidable(true);
		enemy.setProperty(Property.DATA, new Enemy(consApp.getAssetManager().loadText("enemies/enemy_FireElemental.txt")));
		enemy.setProperty("physics", consApp.physics);
		enemy.setProperty("facingRight", false);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		enemy.addControl(new PhysicsControl(consApp.physics));
		enemy.addControl(new RifleControl(consApp.player));
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
		enemy.addFXGLEventHandler(Event.ENEMY_FIRED, event -> {
			consApp.getTimerManager().runOnceAfter(() -> {
				enemy.setProperty("attacking", false);
			}, Config.ENEMY_SCORPION_DELAY.multiply(2));
			consApp.getTimerManager().runOnceAfter(() -> {
				if(enemy != null && enemy.getControl(RifleControl.class) != null){
					consApp.consController.enemyShootProjectile(Element.METAL, enemy);
					enemy.setProperty("attacking", true);
				}			
			}, Config.ENEMY_SCORPION_DELAY);
			consApp.getTimerManager().runOnceAfter(() -> {
				if(enemy != null && enemy.getControl(RifleControl.class) != null){
					enemy.getControl(RifleControl.class).setSpearThrown(false);
				}			
			}, Config.ENEMY_SCORPION_DECAY);
		});
		
		enemy.setVisible(false);
		enemy.setProperty("jumping", false);
		enemy.setProperty("attacking", false);
		
		Entity ePic = new Entity(Type.ENEMY);
		ePic.setVisible(true);
		ePic.setCollidable(false);
		ePic.setPosition(spawnPoint.getX(), spawnPoint.getY());
		ePic.translateXProperty().bind(enemy.translateXProperty());
		ePic.translateYProperty().bind(enemy.translateYProperty());
		HashMap<Types.AnimationActions, AnimationDetails> hashMap = new HashMap<>();
		for(Types.AnimationActions aa : Types.AnimationActions.values()){
			switch(aa){
				case ATK: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0,  900, 300, 300), 1, 1, true)); break;
				case IDLE: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 0, 300, 300), 1, 1, true)); break;
				case JATK: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 900, 300, 300), 1, 1, true)); break;
				case JUMP: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 300, 1200, 300), 4, 1, true)); break;
				case MATK: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 600, 1200, 300), 4, 1, true)); break;
				case MOVE: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 300, 1200, 300), 4, 1, true)); break;
				default: break;
			}
		}
		ePic.addControl(new AnimatedEnemyControl(enemy, hashMap, consApp.getTexture(FileNames.BRITISH_TEX)));
		ePic.addFXGLEventHandler(Event.DEATH, (event) -> consApp.getSceneManager().removeEntity(ePic));
		enemy.aliveProperty().addListener(new ChangeListener<Boolean>(){
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
				if(!arg2){
					consApp.getTimerManager().runOnceAfter(() -> ePic.fireFXGLEvent(new FXGLEvent(Event.DEATH)), Duration.seconds(0.01));
				}
			}});
		
		//consApp.getSceneManager().addEntities(ePic);

		return new Pair<Entity, Entity>(enemy, ePic);
	}
	
	public Pair<Entity, Entity> spawnBayoneter(Point2D spawnPoint){
		Entity enemy = new Entity(Type.ENEMY);
		Rectangle rect = new Rectangle(20, 30);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setCollidable(true);
		enemy.setProperty(Property.DATA, new Enemy(consApp.getAssetManager().loadText("enemies/enemy_FireElemental.txt")));
		enemy.setProperty("physics", consApp.physics);
		enemy.setProperty("facingRight", false);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		enemy.addControl(new PhysicsControl(consApp.physics));
		enemy.addControl(new BayonetControl(consApp.player));
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
		enemy.addFXGLEventHandler(Event.ENEMY_FIRED, event -> {
			consApp.consController.enemyStab(enemy);
		});
		
		enemy.setVisible(false);
		enemy.setProperty("jumping", false);
		enemy.setProperty("attacking", true);
		
		Entity ePic = new Entity(Type.ENEMY);
		ePic.setVisible(true);
		ePic.setCollidable(false);
		ePic.setPosition(spawnPoint.getX(), spawnPoint.getY());
		ePic.translateXProperty().bind(enemy.translateXProperty());
		ePic.translateYProperty().bind(enemy.translateYProperty());
		HashMap<Types.AnimationActions, AnimationDetails> hashMap = new HashMap<>();
		for(Types.AnimationActions aa : Types.AnimationActions.values()){
			switch(aa){
				case ATK: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0,  0, 300, 300), 1, 1, true)); break;
				case IDLE: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 0, 300, 300), 1, 1, true)); break;
				case JATK: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 900, 300, 300), 1, 1, true)); break;
				case JUMP: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 300, 1200, 300), 4, 1, true)); break;
				case MATK: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 600, 1200, 300), 4, 1, true)); break;
				case MOVE: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 300, 1200, 300), 4, 1, true)); break;
				default: break;
			}
		}
		ePic.addControl(new AnimatedEnemyControl(enemy, hashMap, consApp.getTexture(FileNames.BRITISH_TEX)));
		ePic.addFXGLEventHandler(Event.DEATH, (event) -> consApp.getSceneManager().removeEntity(ePic));
		enemy.aliveProperty().addListener(new ChangeListener<Boolean>(){
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
				if(!arg2){
					consApp.getTimerManager().runOnceAfter(() -> ePic.fireFXGLEvent(new FXGLEvent(Event.DEATH)), Duration.seconds(0.01));
				}
			}});
		
		//consApp.getSceneManager().addEntities(ePic);

		return new Pair<Entity, Entity>(enemy, ePic);
	}
	
	public Pair<Entity, Entity> spawnBKnifer(Point2D spawnPoint){
		Entity enemy = new Entity(Type.ENEMY);
		Rectangle rect = new Rectangle(20, 30);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setCollidable(true);
		enemy.setVisible(false);
		enemy.setProperty(Property.DATA, new Enemy(consApp.getAssetManager().loadText(FileNames.STATS_BANDIT_KNIFE)));
		enemy.setProperty("physics", consApp.physics);
		enemy.setProperty("facingRight", true);
		enemy.setProperty("jumping", false);
		enemy.setProperty("attacking", false);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		enemy.addControl(new PhysicsControl(consApp.physics));
		enemy.addControl(new KniferControl(consApp.player));
		
		Entity ePic = new Entity(Type.ENEMY);
		ePic.setVisible(true);
		ePic.setCollidable(false);
		ePic.setPosition(spawnPoint.getX(), spawnPoint.getY());
		ePic.translateXProperty().bind(enemy.translateXProperty());
		ePic.translateYProperty().bind(enemy.translateYProperty());
		HashMap<Types.AnimationActions, AnimationDetails> hashMap = new HashMap<>();
		for(Types.AnimationActions aa : Types.AnimationActions.values()){
			switch(aa){
				case ATK: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 900, 600, 300), 2, 2.5, false)); break;
				case IDLE: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 0, 300, 300), 1, 1, true)); break;
				case JATK: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 600, 600, 300), 2, 1, true)); break;
				case JUMP: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 300, 1200, 300), 4, 1, true)); break;
				case MATK: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 600, 600, 300), 2, 1, true)); break;
				case MOVE: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 300, 1200, 300), 4, 1, true)); break;
				default: break;
			}
		}
		ePic.addControl(new AnimatedEnemyControl(enemy, hashMap, consApp.getTexture(FileNames.BANDIT_K_TEX)));
		ePic.addFXGLEventHandler(Event.DEATH, (event) -> consApp.getSceneManager().removeEntity(ePic));
		enemy.aliveProperty().addListener(new ChangeListener<Boolean>(){
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
				if(!arg2){
					consApp.getTimerManager().runOnceAfter(() -> ePic.fireFXGLEvent(new FXGLEvent(Event.DEATH)), Duration.seconds(0.01));
				}
			}});
		
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
		enemy.addFXGLEventHandler(Event.ENEMY_FIRED, event -> {
			consApp.consController.enemyShootProjectile(Element.NEUTRAL2, enemy);
			enemy.setProperty("attacking", true);
			consApp.getTimerManager().runOnceAfter(() -> {
				enemy.setProperty("attacking", false);
			}, Config.CONSUME_DECAY);
		});
		//consApp.getSceneManager().addEntities(ePic);

		return new Pair<Entity, Entity>(enemy, ePic);
	}
	
	public Pair<Entity, Entity> spawnZKnifer(Point2D spawnPoint){
		Entity enemy = new Entity(Type.ENEMY);
		Rectangle rect = new Rectangle(20, 30);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setCollidable(true);
		enemy.setVisible(false);
		enemy.setProperty(Property.DATA, new Enemy(consApp.getAssetManager().loadText("enemies/enemy_FireElemental.txt")));
		enemy.setProperty("physics", consApp.physics);
		enemy.setProperty("facingRight", true);
		enemy.setProperty("jumping", false);
		enemy.setProperty("attacking", false);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		enemy.addControl(new PhysicsControl(consApp.physics));
		enemy.addControl(new KniferControl(consApp.player));
		
		Entity ePic = new Entity(Type.ENEMY);
		ePic.setVisible(true);
		ePic.setCollidable(false);
		ePic.setPosition(spawnPoint.getX(), spawnPoint.getY());
		ePic.translateXProperty().bind(enemy.translateXProperty());
		ePic.translateYProperty().bind(enemy.translateYProperty());
		HashMap<Types.AnimationActions, AnimationDetails> hashMap = new HashMap<>();
		for(Types.AnimationActions aa : Types.AnimationActions.values()){
			switch(aa){
				case ATK: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 900, 300, 300), 1, 1, true)); break;
				case IDLE: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 0, 300, 300), 1, 1, true)); break;
				case JATK: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 1200, 300, 300), 1, 1, true)); break;
				case JUMP: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 1200, 300, 300), 1, 1, true)); break;
				case MATK: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 600, 600, 300), 2, 1, true)); break;
				case MOVE: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 300, 1200, 300), 4, 1, true)); break;
				default: break;
			}
		}
		ePic.addControl(new AnimatedEnemyControl(enemy, hashMap, consApp.getTexture(FileNames.ZULU_K_TEX)));
		ePic.addFXGLEventHandler(Event.DEATH, (event) -> consApp.getSceneManager().removeEntity(ePic));
		enemy.aliveProperty().addListener(new ChangeListener<Boolean>(){
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
				if(!arg2){
					consApp.getTimerManager().runOnceAfter(() -> ePic.fireFXGLEvent(new FXGLEvent(Event.DEATH)), Duration.seconds(0.01));
				}
			}});
		
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
		enemy.addFXGLEventHandler(Event.ENEMY_FIRED, event -> {
			consApp.consController.enemyShootProjectile(Element.NEUTRAL2, enemy);
			enemy.setProperty("attacking", true);
			consApp.getTimerManager().runOnceAfter(() -> {
				enemy.setProperty("attacking", false);
			}, Config.CONSUME_DECAY);
		});
		//consApp.getSceneManager().addEntities(ePic);

		return new Pair<Entity, Entity>(enemy, ePic);
	}
	
	public Pair<Entity, Entity> spawnMagician(Point2D spawnPoint){
		Entity enemy = new Entity(Type.ENEMY);
		Rectangle rect = new Rectangle(20, 30);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setCollidable(true);
		enemy.setVisible(false);
		enemy.setProperty(Property.DATA, new Enemy(consApp.getAssetManager().loadText("enemies/enemy_FireElemental.txt")));
		enemy.setProperty("physics", consApp.physics);
		enemy.setProperty("facingRight", false);
		enemy.setProperty("jumping", false);
		enemy.setProperty("attacking", false);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		enemy.addControl(new PhysicsControl(consApp.physics));
		enemy.addControl(new MagicianControl(consApp.player));
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
		enemy.addFXGLEventHandler(Event.ENEMY_FIRED, event -> {
			consApp.getTimerManager().runOnceAfter(() -> {
				enemy.setProperty("attacking", false);
			}, Config.ENEMY_SCORPION_DELAY.multiply(2));
			enemy.setProperty("attacking", true);
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
		
		enemy.setProperty("jumping", false);
		enemy.setProperty("attacking", false);
		
		Entity ePic = new Entity(Type.ENEMY);
		ePic.setVisible(true);
		ePic.setCollidable(false);
		ePic.setPosition(spawnPoint.getX(), spawnPoint.getY());
		ePic.translateXProperty().bind(enemy.translateXProperty());
		ePic.translateYProperty().bind(enemy.translateYProperty());
		HashMap<Types.AnimationActions, AnimationDetails> hashMap = new HashMap<>();
		for(Types.AnimationActions aa : Types.AnimationActions.values()){
			switch(aa){
				case ATK: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 900, 600, 300), 2, 0.8, false)); break;
				case IDLE: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 0, 300, 300), 1, 1, true)); break;
				case JATK: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 600, 600, 300), 2, 0.8, false)); break;
				case JUMP: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 300, 300, 300), 1, 1, true)); break;
				case MATK: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 600, 600, 300), 2, 0.8, false)); break;
				case MOVE: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 300, 1200, 300), 4, 1, true)); break;
				default: break;
			}
		}
		ePic.addControl(new AnimatedEnemyControl(enemy, hashMap, consApp.getTexture(FileNames.BANDIT_M_TEX)));
		ePic.addFXGLEventHandler(Event.DEATH, (event) -> consApp.getSceneManager().removeEntity(ePic));
		enemy.aliveProperty().addListener(new ChangeListener<Boolean>(){
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
				if(!arg2){
					consApp.getTimerManager().runOnceAfter(() -> ePic.fireFXGLEvent(new FXGLEvent(Event.DEATH)), Duration.seconds(0.01));
				}
			}});
		
		//consApp.getSceneManager().addEntities(ePic);

		return new Pair<Entity, Entity>(enemy, ePic);
	}
	
	//TODO: Not currently used
	public Entity spawnBoss(Point2D spawnPoint) {
		Entity enemy = new Entity(Type.ENEMY);
		Rectangle rect = new Rectangle(30, 30);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setCollidable(true);
		enemy.setProperty(Property.DATA, new Enemy(consApp.getAssetManager().loadText("enemies/enemy_FireElemental.txt")));
		enemy.setProperty(Property.SUB_TYPE, Type.BOSS);
		enemy.setProperty("physics", consApp.physics);
		enemy.setProperty("shover", true);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		enemy.addControl(new PhysicsControl(consApp.physics));
		enemy.addControl(new SimpleJumpControl(consApp, consApp.player, Speed.ENEMY_JUMP));
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);

		return enemy;
	}
	
	public Pair<Entity, Entity> spawnSandBoss(Point2D spawnPoint) {
		Entity enemy = new Entity(Type.BOSS);
		Rectangle rect = new Rectangle(60, 20); //60 High, 20 start
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setVisible(false);
		enemy.setCollidable(false);
		enemy.setProperty(Property.DATA, new Enemy(consApp.getAssetManager().loadText("enemies/enemy_FireElemental.txt")));
		enemy.setProperty(Property.SUB_TYPE, Type.BOSS);
		enemy.setProperty("physics", consApp.physics);
		enemy.setProperty("shover", true);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		enemy.addControl(new PhysicsControl(consApp.physics));
		enemy.addControl(new SandBossControl(consApp.player));
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
		enemy.addFXGLEventHandler(Event.ENEMY_FIRED, event -> {
			consApp.getTimerManager().runOnceAfter(() -> {
				if(enemy != null && enemy.getControl(SandBossControl.class) != null){
					if(enemy.getControl(SandBossControl.class).isUnderground()){
						consApp.consController.enemyCreatePillar(enemy, consApp.player, spawnPoint.getY() + Config.BLOCK_SIZE/2);
					}
					else{
						enemy.setProperty("attacking", true);
						consApp.consController.enemyShootProjectile(Element.EARTH, enemy);
						consApp.getTimerManager().runOnceAfter(() -> {
							enemy.setProperty("attacking", false);
						}, Duration.seconds(1));
						
					}
				}			
			}, Config.ENEMY_SCORPION_DELAY);
			consApp.getTimerManager().runOnceAfter(() -> {
				if(enemy != null && enemy.getControl(SandBossControl.class) != null){
					enemy.getControl(SandBossControl.class).setAttackComplete(true);
				}			
			}, Config.ENEMY_SCORPION_DECAY);
		});
		
		//enemy.setVisible(false);
		enemy.setProperty("jumping", false);
		enemy.setProperty("attacking", false);
		
		Entity ePic = new Entity(Type.BOSS);
		ePic.setVisible(true);
		ePic.setCollidable(false);
		ePic.setPosition(spawnPoint.getX(), spawnPoint.getY());
		ePic.translateXProperty().bind(enemy.translateXProperty());
		ePic.translateYProperty().bind(enemy.translateYProperty().add(10));
		int fS = 300;
		HashMap<Types.AnimationActions, AnimationDetailsE> hashMap = new HashMap<>();
		for(Types.AnimationActions aa : Types.AnimationActions.values()){
			switch(aa){
				case ATK: hashMap.put(aa, new AnimationDetailsE(
						new Rectangle2D	(0,   4*fS, 2*fS, fS), 2, 1, false)); break;
				case IDLE: hashMap.put(aa, new AnimationDetailsE(
						new Rectangle2D	(0,   0*fS, 1*fS, fS), 1, 1, true)); break;
				case JATK: hashMap.put(aa, new AnimationDetailsE(
						new Rectangle2D	(0,   0*fS, 1*fS, fS), 1, 1, true)); break;
				case JUMP: hashMap.put(aa, new AnimationDetailsE(
						new Rectangle2D	(0,   1*fS, 5*fS, fS), 5, 1, true)); break;
				case MATK: hashMap.put(aa, new AnimationDetailsE(
						new Rectangle2D	(0,   4*fS, 2*fS, fS), 2, 1, true)); break;
				case MOVE: hashMap.put(aa, new AnimationDetailsE(
						new Rectangle2D	(0,   1*fS, 4*fS, fS), 4, 1, false)); break;
				default: break;
			}
		}
		ePic.addControl(new AnimatedElephantControl(enemy, hashMap, consApp.getTexture(FileNames.SAND_ELEPHANT_TEX)));
		ePic.addFXGLEventHandler(Event.DEATH, (event) -> consApp.getSceneManager().removeEntity(ePic));
		enemy.aliveProperty().addListener(new ChangeListener<Boolean>(){
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
				if(!arg2){
					consApp.getTimerManager().runOnceAfter(() -> ePic.fireFXGLEvent(new FXGLEvent(Event.DEATH)), Duration.seconds(0.01));
				}
			}});
		
		//consApp.getSceneManager().addEntities(ePic);

		return new Pair<Entity, Entity>(enemy, ePic);
	}
	
	public Pair<Entity, Entity> spawnAnubisBoss(Point2D spawnPoint) {
		Entity enemy = new Entity(Type.BOSS);
		Rectangle rect = new Rectangle(20, 30);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setVisible(false);
		enemy.setCollidable(true);
		enemy.setProperty(Property.DATA, new Enemy(consApp.getAssetManager().loadText("enemies/enemy_FireElemental.txt")));
		enemy.setProperty(Property.SUB_TYPE, Type.BOSS);
		enemy.setProperty("physics", consApp.physics);
		enemy.setProperty("shover", true);
		enemy.setProperty("facingRight", false);
		enemy.setProperty("beenHit", false);
		enemy.setProperty("attacking", false);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		enemy.addControl(new PhysicsControl(consApp.physics));
		enemy.addControl(new AnubisControl(consApp, consApp.player));
		Entity ePic = new Entity(Type.BOSS);
		ePic.setVisible(true);
		ePic.setCollidable(false);
		ePic.setPosition(spawnPoint.getX(), spawnPoint.getY());
		ePic.translateXProperty().bind(enemy.translateXProperty());
		ePic.translateYProperty().bind(enemy.translateYProperty());
		HashMap<Types.AnimationActions, AnimationDetails> hashMap = new HashMap<>();
		for(Types.AnimationActions aa : Types.AnimationActions.values()){
			switch(aa){
				case ATK: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 900, 600, 300), 2, 2.5, false)); break;
				case IDLE: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 0, 600, 300), 2, 1, true)); break;
				case JATK: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 1200, 300, 300), 1, 1, true)); break;
				case JUMP: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 1200, 300, 300), 1, 1, true)); break;
				case MATK: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 300, 1200, 300), 4, 1, true)); break;
				case MOVE: hashMap.put(aa, new AnimationDetails(new Rectangle2D(0, 300, 1200, 300), 4, 1, true)); break;
				default: break;
			}
		}
		ePic.addControl(new AnimatedEnemyControl(enemy, hashMap, consApp.getTexture(FileNames.ANUBIS_TEX)));
		ePic.addFXGLEventHandler(Event.DEATH, (event) -> consApp.getSceneManager().removeEntity(ePic));
		enemy.aliveProperty().addListener(new ChangeListener<Boolean>(){
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
				if(!arg2){
					consApp.getTimerManager().runOnceAfter(() -> ePic.fireFXGLEvent(new FXGLEvent(Event.DEATH)), Duration.seconds(0.01));
				}
			}});
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
		enemy.addFXGLEventHandler(Event.ENEMY_FIRED, event -> {
			if((boolean)enemy.getProperty("jumping")){
				enemy.setProperty("attacking", true);
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
						enemy.setProperty("attacking", false);
					}			
				}, Config.ANUBIS_JATTACK_DELAY.multiply(3));
			}
			else{
				consApp.consController.enemyShootProjectile(Element.NEUTRAL2, enemy);
				enemy.setProperty("attacking", true);
				consApp.getTimerManager().runOnceAfter(() -> {
					if(enemy != null && enemy.getControl(AnubisControl.class) != null){
						enemy.getControl(AnubisControl.class).setAttackComplete(true, true);
						enemy.setProperty("attacking", false);
					}			
					
				}, Config.ENEMY_SCORPION_DECAY);
			}
		});

		//consApp.getSceneManager().addEntities(ePic);
		return new Pair<Entity, Entity>(enemy, ePic);
	}
	
	public Pair<Entity, Entity> spawnShangoBoss(Point2D spawnPoint) {
		Entity enemy = new Entity(Type.BOSS);
		Rectangle rect = new Rectangle(20, 30);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setVisible(true);
		enemy.setCollidable(true);
		enemy.setProperty(Property.DATA, new Enemy(consApp.getAssetManager().loadText("enemies/enemy_FireElemental.txt")));
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
				enemy.setProperty("attacking", true);
				consApp.getTimerManager().runOnceAfter(() -> {
					if(enemy != null && enemy.getControl(ShangoControl.class) != null){
						if((boolean)enemy.getProperty("facingRight")){
							consApp.consController.aimedLightningBolt(enemy, new Point2D(enemy.getPosition().getX() - Config.BLOCK_SIZE*1.5, gPoint.getY()));	
						}
						else{
							consApp.consController.aimedLightningBolt(enemy, new Point2D(enemy.getPosition().getX() + Config.BLOCK_SIZE*1.5, gPoint.getY()));	
						}
						enemy.getControl(ShangoControl.class).setAttackComplete(true, false);
						enemy.setProperty("attacking", false);
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
					enemy.setProperty("attacking", true);
					consApp.getTimerManager().runOnceAfter(() -> {
						if(enemy != null && enemy.getControl(ShangoControl.class) != null){
							enemy.getControl(ShangoControl.class).setAttackComplete(true, true);
							enemy.setProperty("attacking", false);
						}				
					}, Config.ENEMY_SCORPION_DECAY);
					
				}
				else{
					enemy.setProperty("attacking", true);
					Point2D pPos = new Point2D(consApp.player.getPosition().getX(), gPoint.getY());
					consApp.getTimerManager().runOnceAfter(() -> {
						consApp.consController.aimedLightningBolt(enemy, pPos);
					}, Config.SHANGO_MATTACK_DELAY.multiply(3));
					consApp.getTimerManager().runOnceAfter(() -> {
						if(enemy != null && enemy.getControl(ShangoControl.class) != null){
							enemy.getControl(ShangoControl.class).setAttackComplete(true, true);
							enemy.setProperty("attacking", false);
						}			
					}, Config.ENEMY_SCORPION_DECAY);
				}			
			}
		});
		
		enemy.setVisible(false);
		enemy.setProperty("jumping", false);
		enemy.setProperty("attacking", false);
		
		Entity ePic = new Entity(Type.BOSS);
		ePic.setVisible(true);
		ePic.setCollidable(false);
		ePic.setPosition(spawnPoint.getX(), spawnPoint.getY());
		ePic.translateXProperty().bind(enemy.translateXProperty());
		ePic.translateYProperty().bind(enemy.translateYProperty());
		int fS = 300;
		HashMap<Types.AnimationActions, AnimationDetails> hashMap = new HashMap<>();
		for(Types.AnimationActions aa : Types.AnimationActions.values()){
			switch(aa){
				case ATK: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   3*fS, 2*fS, fS), 2, 1, true)); break;
				case IDLE: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   0*fS, 1*fS, fS), 1, 1, true)); break;
				case JATK: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(1*fS,4*fS, 1*fS, fS), 1, 1, true)); break;
				case JUMP: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   4*fS, 1*fS, fS), 1, 1, true)); break;
				case MATK: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   1*fS, 4*fS, fS), 4, 1, true)); break;
				case MOVE: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   1*fS, 4*fS, fS), 4, 1, true)); break;
				default: break;
			}
		}
		ePic.addControl(new AnimatedEnemyControl(enemy, hashMap, consApp.getTexture(FileNames.SHANGO_TEX)));
		ePic.addFXGLEventHandler(Event.DEATH, (event) -> consApp.getSceneManager().removeEntity(ePic));
		enemy.aliveProperty().addListener(new ChangeListener<Boolean>(){
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
				if(!arg2){
					consApp.getTimerManager().runOnceAfter(() -> ePic.fireFXGLEvent(new FXGLEvent(Event.DEATH)), Duration.seconds(0.01));
				}
			}});
		
		//consApp.getSceneManager().addEntities(ePic);

		return new Pair<Entity, Entity>(enemy, ePic);
	}
	
	public Pair<Entity, Entity> spawnKiboBoss(Point2D spawnPoint) {
		Entity enemy = new Entity(Type.BOSS);
		Rectangle rect = new Rectangle(20, 30);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setVisible(true);
		enemy.setCollidable(true);
		enemy.setProperty(Property.DATA, new Enemy(consApp.getAssetManager().loadText("enemies/enemy_FireElemental.txt")));
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
						enemy.setProperty("attacking", true);
						consApp.getTimerManager().runOnceAfter(() -> {
							if(enemy != null && enemy.getControl(KiboControl.class) != null){	
								consApp.consController.aimedFireball(enemy, consApp.player);
							}			
						}, Config.ENEMY_SCORPION_DELAY.multiply(0.5));
						consApp.getTimerManager().runOnceAfter(() -> {
							if(enemy != null && enemy.getControl(KiboControl.class) != null){	
								enemy.setProperty("attacking", false);
								enemy.getControl(KiboControl.class).setAttackComplete(true, true);
							}			
						}, Config.ENEMY_SCORPION_DELAY);				
						break;
					}
					case 1:{
						consApp.consController.enemyShootProjectile(Element.FIRE, enemy);
						enemy.setProperty("attacking", true);
						if(enemy != null & enemy.getControl(PhysicsControl.class) != null){
							enemy.getControl(PhysicsControl.class).jump();
						}
						consApp.getTimerManager().runOnceAfter(() -> {
							if(enemy != null && enemy.getControl(KiboControl.class) != null){			
								enemy.setProperty("attacking", false);
							}			
						}, Config.ENEMY_SCORPION_DELAY.multiply(0.5));	
						consApp.getTimerManager().runOnceAfter(() -> {
							if(enemy != null && enemy.getControl(KiboControl.class) != null){			
								consApp.consController.enemyShootProjectile(Element.FIRE, enemy);
								enemy.setProperty("attacking", true);
							}			
						}, Config.ENEMY_SCORPION_DELAY);	
						consApp.getTimerManager().runOnceAfter(() -> {
							if(enemy != null && enemy.getControl(KiboControl.class) != null){	
								enemy.setProperty("attacking", false);
							}			
						}, Config.ENEMY_SCORPION_DELAY.multiply(1.6));	
						consApp.getTimerManager().runOnceAfter(() -> {
							if(enemy != null && enemy.getControl(KiboControl.class) != null){	
								consApp.consController.enemyShootProjectile(Element.FIRE, enemy);
								enemy.setProperty("attacking", true);
								enemy.getControl(KiboControl.class).setAttackComplete(true, true);
							}			
						}, Config.ENEMY_SCORPION_DELAY.multiply(2.2));	
						consApp.getTimerManager().runOnceAfter(() -> {
							if(enemy != null && enemy.getControl(KiboControl.class) != null){	
								enemy.setProperty("attacking", false);
							}			
						}, Config.ENEMY_SCORPION_DELAY.multiply(2.5));	
						break;
					}
					case 2:{
						consApp.consController.enemyShootProjectile(Element.FIRE, enemy);
						enemy.setProperty("attacking", true);
						consApp.getTimerManager().runOnceAfter(() -> {
							if(enemy != null && enemy.getControl(KiboControl.class) != null){	
								enemy.setProperty("attacking", false);
							}			
						}, Config.ENEMY_SCORPION_DELAY.multiply(1.5));
						consApp.getTimerManager().runOnceAfter(() -> {
							if(enemy != null && enemy.getControl(KiboControl.class) != null){	
								enemy.getControl(KiboControl.class).setAttackComplete(true, false);
							}			
						}, Config.ENEMY_SCORPION_DELAY.multiply(3));
						break;
					}
				}
			}
			
		});
		
		
		enemy.setVisible(false);
		enemy.setProperty("jumping", false);
		enemy.setProperty("attacking", false);
		
		Entity ePic = new Entity(Type.BOSS);
		ePic.setVisible(true);
		ePic.setCollidable(false);
		ePic.setPosition(spawnPoint.getX(), spawnPoint.getY());
		ePic.translateXProperty().bind(enemy.translateXProperty());
		ePic.translateYProperty().bind(enemy.translateYProperty());
		int fS = 300;
		HashMap<Types.AnimationActions, AnimationDetails> hashMap = new HashMap<>();
		for(Types.AnimationActions aa : Types.AnimationActions.values()){
			switch(aa){
				case ATK: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   3*fS, 2*fS, fS), 2, 0.35, false)); break;
				case IDLE: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   0*fS, 1*fS, fS), 1, 1, true)); break;
				case JATK: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(fS,  4*fS, 1*fS, fS), 1, 1, true)); break;
				case JUMP: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   4*fS, 1*fS, fS), 1, 1, true)); break;
				case MATK: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   2*fS, 2*fS, fS), 2, 0.5, false)); break;
				case MOVE: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   1*fS, 4*fS, fS), 4, 1, true)); break;
				default: break;
			}
		}
		ePic.addControl(new AnimatedEnemyControl(enemy, hashMap, consApp.getTexture(FileNames.KIBO_TEX)));
		ePic.addFXGLEventHandler(Event.DEATH, (event) -> consApp.getSceneManager().removeEntity(ePic));
		enemy.aliveProperty().addListener(new ChangeListener<Boolean>(){
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
				if(!arg2){
					consApp.getTimerManager().runOnceAfter(() -> ePic.fireFXGLEvent(new FXGLEvent(Event.DEATH)), Duration.seconds(0.01));
				}
			}});
		
		//consApp.getSceneManager().addEntities(ePic);

		return new Pair<Entity, Entity>(enemy, ePic);
	}
	
	public Pair<Entity, Entity> spawnGentlemanBoss(Point2D spawnPoint) {
		Entity enemy = new Entity(Type.BOSS);
		Rectangle rect = new Rectangle(20, 30);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setVisible(true);
		enemy.setCollidable(true);
		enemy.setProperty(Property.DATA, new Enemy(consApp.getAssetManager().loadText("enemies/enemy_FireElemental.txt")));
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
				enemy.setProperty("attacking", true);
				consApp.getTimerManager().runOnceAfter(() -> {
					if(enemy != null){			
						enemy.setProperty("attacking", false);
					}			
				}, Config.ENEMY_SCORPION_DELAY.multiply(0.5));
				consApp.getTimerManager().runOnceAfter(() -> {
					if(enemy != null && enemy.getControl(GentlemanControl.class) != null){			
						consApp.consController.enemyShootProjectile(Element.METAL, enemy);
						consApp.soundManager.playSFX(FileNames.RIFLE_SHOT);
						enemy.setProperty("attacking", true);
					}			
				}, Config.ENEMY_SCORPION_DELAY);	
				consApp.getTimerManager().runOnceAfter(() -> {
					if(enemy != null){			
						enemy.setProperty("attacking", false);
					}			
				}, Config.ENEMY_SCORPION_DELAY.multiply(1.5));
				consApp.getTimerManager().runOnceAfter(() -> {
					if(enemy != null && enemy.getControl(GentlemanControl.class) != null){	
						consApp.consController.enemyShootProjectile(Element.METAL, enemy);
						consApp.soundManager.playSFX(FileNames.RIFLE_SHOT);
						enemy.setProperty("attacking", true);
					}			
				}, Config.ENEMY_SCORPION_DELAY.multiply(2));	
				consApp.getTimerManager().runOnceAfter(() -> {
					if(enemy != null){			
						enemy.setProperty("attacking", false);
					}			
				}, Config.ENEMY_SCORPION_DELAY.multiply(2.5));
			}
			else if((enemy != null && enemy.getControl(PhysicsControl.class) != null && enemy.getControl(PhysicsControl.class).getVelocity().getX() != 0)){
				consApp.consController.enemyShootProjectile(Element.METAL, enemy);
				consApp.soundManager.playSFX(FileNames.RIFLE_SHOT);
				enemy.setProperty("attacking", true);
				consApp.getTimerManager().runOnceAfter(() -> {
					if(enemy != null){			
						enemy.setProperty("attacking", false);
					}			
				}, Config.GENTLE_CATTACK_DELAY.multiply(0.5));
				consApp.getTimerManager().runOnceAfter(() -> {
					if(enemy != null && enemy.getControl(GentlemanControl.class) != null){			
						consApp.consController.enemyShootProjectile(Element.METAL, enemy);
						consApp.soundManager.playSFX(FileNames.RIFLE_SHOT);
						enemy.setProperty("attacking", true);
					}			
				}, Config.GENTLE_CATTACK_DELAY);
				consApp.getTimerManager().runOnceAfter(() -> {
					if(enemy != null){			
						enemy.setProperty("attacking", false);
					}			
				}, Config.GENTLE_CATTACK_DELAY.multiply(1.5));
				consApp.getTimerManager().runOnceAfter(() -> {
					if(enemy != null && enemy.getControl(GentlemanControl.class) != null){	
						consApp.consController.enemyShootProjectile(Element.METAL, enemy);
						consApp.soundManager.playSFX(FileNames.RIFLE_SHOT);
						enemy.setProperty("attacking", true);
					}			
				}, Config.GENTLE_CATTACK_DELAY.multiply(2));
				consApp.getTimerManager().runOnceAfter(() -> {
					if(enemy != null){			
						enemy.setProperty("attacking", false);
					}			
				}, Config.GENTLE_CATTACK_DELAY.multiply(2.5));
				consApp.getTimerManager().runOnceAfter(() -> {
					if(enemy != null && enemy.getControl(GentlemanControl.class) != null){	
						consApp.consController.enemyShootProjectile(Element.METAL, enemy);
						consApp.soundManager.playSFX(FileNames.RIFLE_SHOT);
						enemy.setProperty("attacking", true);
					}			
				}, Config.GENTLE_CATTACK_DELAY.multiply(3));
				consApp.getTimerManager().runOnceAfter(() -> {
					if(enemy != null){			
						enemy.setProperty("attacking", false);
					}			
				}, Config.GENTLE_CATTACK_DELAY.multiply(3.5));
				consApp.getTimerManager().runOnceAfter(() -> {
					if(enemy != null && enemy.getControl(GentlemanControl.class) != null){	
						consApp.consController.enemyShootProjectile(Element.METAL, enemy);
						consApp.soundManager.playSFX(FileNames.RIFLE_SHOT);
						enemy.setProperty("attacking", true);
					}			
				}, Config.GENTLE_CATTACK_DELAY.multiply(4));
				consApp.getTimerManager().runOnceAfter(() -> {
					if(enemy != null){			
						enemy.setProperty("attacking", false);
					}			
				}, Config.GENTLE_CATTACK_DELAY.multiply(4.5));
			}
			else{
				consApp.consController.enemyShootProjectile(Element.METAL, enemy);
				consApp.soundManager.playSFX(FileNames.RIFLE_SHOT);
				enemy.setProperty("attacking", true);
				consApp.getTimerManager().runOnceAfter(() -> {
					if(enemy != null && enemy.getControl(GentlemanControl.class) != null){
						enemy.getControl(GentlemanControl.class).setAttackComplete(true, true);
						enemy.setProperty("attacking", false);
					}			
				}, Config.GENTLE_CATTACK_DELAY.multiply(0.5));
							
			}
		});
		
		enemy.setVisible(false);
		enemy.setProperty("jumping", false);
		enemy.setProperty("attacking", false);
		
		Entity ePic = new Entity(Type.BOSS);
		ePic.setVisible(true);
		ePic.setCollidable(false);
		ePic.setPosition(spawnPoint.getX(), spawnPoint.getY());
		ePic.translateXProperty().bind(enemy.translateXProperty());
		ePic.translateYProperty().bind(enemy.translateYProperty());
		int fS = 300;
		HashMap<Types.AnimationActions, AnimationDetails> hashMap = new HashMap<>();
		for(Types.AnimationActions aa : Types.AnimationActions.values()){
			switch(aa){
				case ATK: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   3*fS, 2*fS, fS), 2, 1, false)); break;
				case IDLE: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   0*fS, 1*fS, fS), 1, 1, true)); break;
				case JATK: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(fS,  4*fS, 1*fS, fS), 1, 1, true)); break;
				case JUMP: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   4*fS, 1*fS, fS), 1, 1, true)); break;
				case MATK: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   2*fS, 2*fS, fS), 2, 1, false)); break;
				case MOVE: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   1*fS, 4*fS, fS), 4, 1, true)); break;
				default: break;
			}
		}
		ePic.addControl(new AnimatedEnemyControl(enemy, hashMap, consApp.getTexture(FileNames.GENTLEMAN_TEX)));
		ePic.addFXGLEventHandler(Event.DEATH, (event) -> consApp.getSceneManager().removeEntity(ePic));
		enemy.aliveProperty().addListener(new ChangeListener<Boolean>(){
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
				if(!arg2){
					consApp.getTimerManager().runOnceAfter(() -> ePic.fireFXGLEvent(new FXGLEvent(Event.DEATH)), Duration.seconds(0.01));
				}
			}});
		
		//consApp.getSceneManager().addEntities(ePic);

		return new Pair<Entity, Entity>(enemy, ePic);
	}
	

	public Pair<Entity, Entity> spawnShakaBoss(Point2D spawnPoint) {
		Entity enemy = new Entity(Type.BOSS);
		Rectangle rect = new Rectangle(20, 30);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setVisible(true);
		enemy.setCollidable(true);
		enemy.setProperty(Property.DATA, new Enemy(consApp.getAssetManager().loadText("enemies/enemy_FireElemental.txt")));
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
				enemy.setProperty("attacking", true);
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
				consApp.getTimerManager().runOnceAfter(() -> {
					if(enemy != null && enemy.getControl(ShakaControl.class) != null){	
						enemy.setProperty("attacking", false);
					}			
				}, Config.SHAKA_CATTACK_DELAY.multiply(6.25));	
			}
			else if((enemy != null && enemy.getControl(PhysicsControl.class) != null && enemy.getControl(PhysicsControl.class).getVelocity().getX() == 0)){
				consApp.consController.enemyShootProjectile(Element.NEUTRAL, enemy);
				enemy.setProperty("attacking", true);
				consApp.getTimerManager().runOnceAfter(() -> {
					if(enemy != null && enemy.getControl(ShakaControl.class) != null){			
						enemy.getControl(ShakaControl.class).setAttackComplete(true, true);
						enemy.setProperty("attacking", false);
					}			
				}, Config.SHAKA_SPEAR_DECAY);	
			}

		});
		
		enemy.setVisible(false);
		enemy.setProperty("jumping", false);
		enemy.setProperty("attacking", false);
		
		Entity ePic = new Entity(Type.BOSS);
		ePic.setVisible(true);
		ePic.setCollidable(false);
		ePic.setPosition(spawnPoint.getX(), spawnPoint.getY());
		ePic.translateXProperty().bind(enemy.translateXProperty());
		ePic.translateYProperty().bind(enemy.translateYProperty());
		int fS = 300;
		HashMap<Types.AnimationActions, AnimationDetails> hashMap = new HashMap<>();
		for(Types.AnimationActions aa : Types.AnimationActions.values()){
			switch(aa){
				case ATK: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   3*fS, 2*fS, fS), 2, 1.5, false)); break;
				case IDLE: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   0*fS, 1*fS, fS), 1, 1, true)); break;
				case JATK: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   4*fS, 1*fS, fS), 1, 1, true)); break;
				case JUMP: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   4*fS, 1*fS, fS), 1, 1, true)); break;
				case MATK: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   2*fS, 4*fS, fS), 4, 1, true)); break;
				case MOVE: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   1*fS, 4*fS, fS), 4, 1, true)); break;
				default: break;
			}
		}
		ePic.addControl(new AnimatedEnemyControl(enemy, hashMap, consApp.getTexture(FileNames.SHAKA_TEX)));
		ePic.addFXGLEventHandler(Event.DEATH, (event) -> consApp.getSceneManager().removeEntity(ePic));
		enemy.aliveProperty().addListener(new ChangeListener<Boolean>(){
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
				if(!arg2){
					consApp.getTimerManager().runOnceAfter(() -> ePic.fireFXGLEvent(new FXGLEvent(Event.DEATH)), Duration.seconds(0.01));
				}
			}});
		
		//consApp.getSceneManager().addEntities(ePic);

		return new Pair<Entity, Entity>(enemy, ePic);
	}
	
	public Pair<Entity, Entity> spawnEshuBoss(Point2D spawnPoint) {
		Entity enemy = new Entity(Type.BOSS);
		Rectangle rect = new Rectangle(20, 40);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setVisible(true);
		enemy.setCollidable(true);
		enemy.setProperty(Property.DATA, new Enemy(consApp.getAssetManager().loadText("enemies/enemy_FireElemental.txt")));
		enemy.setProperty(Property.SUB_TYPE, Type.BOSS);
		enemy.setProperty("physics", consApp.physics);
		enemy.setProperty("shover", true);
		enemy.setProperty("facingRight", false);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		enemy.addControl(new PhysicsControl(consApp.physics));
		enemy.addControl(new EshuControl(consApp, consApp.player));
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
		enemy.addFXGLEventHandler(Event.ENEMY_FIRED, event -> {
			if((enemy != null && (boolean)enemy.getProperty("jumping"))){
				consApp.consController.enemyStabDown(enemy);
			}
			else if((enemy != null && enemy.getControl(PhysicsControl.class) != null && enemy.getControl(PhysicsControl.class).getVelocity().getX() == 0)){
				consApp.consController.enemyShootProjectile(Element.NEUTRAL, enemy);
				consApp.getTimerManager().runOnceAfter(() -> {
					if(enemy != null && enemy.getControl(EshuControl.class) != null){			
						enemy.getControl(EshuControl.class).setAttackComplete(true, true);
					}			
				}, Config.ESHU_SPEAR_DECAY);	
			}
		});
		
		enemy.setVisible(false);
		enemy.setProperty("jumping", false);
		enemy.setProperty("attacking", false);
		
		Entity ePic = new Entity(Type.BOSS);
		ePic.setVisible(true);
		ePic.setCollidable(false);
		ePic.setPosition(spawnPoint.getX(), spawnPoint.getY());
		ePic.translateXProperty().bind(enemy.translateXProperty());
		ePic.translateYProperty().bind(enemy.translateYProperty().add(10));
		int fS = 300;
		HashMap<Types.AnimationActions, AnimationDetails> hashMap = new HashMap<>();
		for(Types.AnimationActions aa : Types.AnimationActions.values()){
			switch(aa){
				case ATK: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   3*fS, 2*fS, fS), 2, 1, true)); break;
				case IDLE: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   0*fS, 1*fS, fS), 1, 1, true)); break;
				case JATK: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(fS,  4*fS, 1*fS, fS), 1, 1, true)); break;
				case JUMP: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   4*fS, 1*fS, fS), 1, 1, true)); break;
				case MATK: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   2*fS, 4*fS, fS), 4, 1, true)); break;
				case MOVE: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   1*fS, 4*fS, fS), 4, 1, true)); break;
				default: break;
			}
		}
		ePic.addControl(new AnimatedEnemyControl(enemy, hashMap, consApp.getTexture(FileNames.ESHU_TEX)));
		ePic.addFXGLEventHandler(Event.DEATH, (event) -> consApp.getSceneManager().removeEntity(ePic));
		enemy.aliveProperty().addListener(new ChangeListener<Boolean>(){
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
				if(!arg2){
					consApp.getTimerManager().runOnceAfter(() -> ePic.fireFXGLEvent(new FXGLEvent(Event.DEATH)), Duration.seconds(0.01));
				}
			}});
		
		//consApp.getSceneManager().addEntities(ePic);

		return new Pair<Entity, Entity>(enemy, ePic);
	}
	
	public Pair<Entity, Entity> spawnEshuIBoss(Point2D spawnPoint) {
		Entity enemy = new Entity(Type.BOSS);
		Rectangle rect = new Rectangle(20, 40);
		rect.setFill(Color.RED);

		enemy.setGraphics(rect);
		enemy.setVisible(true);
		enemy.setCollidable(true);
		enemy.setProperty(Property.DATA, new Enemy(consApp.getAssetManager().loadText("enemies/enemy_FireElemental.txt")));
		enemy.setProperty(Property.SUB_TYPE, Type.BOSS);
		enemy.setProperty("physics", consApp.physics);
		enemy.setProperty("shover", true);
		enemy.setProperty("facingRight", false);
		enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
		enemy.addControl(new PhysicsControl(consApp.physics));
		enemy.addControl(new EshuIControl(consApp, consApp.player));
		enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
		enemy.addFXGLEventHandler(Event.ENEMY_FIRED, event -> {
			if((enemy != null && (boolean)enemy.getProperty("jumping"))){
				consApp.consController.enemyStabDown(enemy);
			}
			else if((enemy != null && enemy.getControl(PhysicsControl.class) != null && enemy.getControl(PhysicsControl.class).getVelocity().getX() == 0)){
				consApp.consController.enemyShootProjectile(Element.NEUTRAL, enemy);
				consApp.getTimerManager().runOnceAfter(() -> {
					if(enemy != null && enemy.getControl(EshuIControl.class) != null){			
						enemy.getControl(EshuIControl.class).setAttackComplete(true, true);
					}			
				}, Config.ESHU_SPEAR_DECAY);	
			}
		});
		
		enemy.setVisible(false);
		enemy.setProperty("jumping", false);
		enemy.setProperty("attacking", false);
		
		Entity ePic = new Entity(Type.BOSS);
		ePic.setVisible(true);
		ePic.setCollidable(false);
		ePic.setPosition(spawnPoint.getX(), spawnPoint.getY());
		ePic.translateXProperty().bind(enemy.translateXProperty());
		ePic.translateYProperty().bind(enemy.translateYProperty().add(10));
		int fS = 300;
		HashMap<Types.AnimationActions, AnimationDetails> hashMap = new HashMap<>();
		for(Types.AnimationActions aa : Types.AnimationActions.values()){
			switch(aa){
				case ATK: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   3*fS, 2*fS, fS), 2, 1, true)); break;
				case IDLE: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   0*fS, 1*fS, fS), 1, 1, true)); break;
				case JATK: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(fS,  4*fS, 1*fS, fS), 1, 1, true)); break;
				case JUMP: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   4*fS, 1*fS, fS), 1, 1, true)); break;
				case MATK: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   2*fS, 4*fS, fS), 4, 1, true)); break;
				case MOVE: hashMap.put(aa, new AnimationDetails(
						new Rectangle2D	(0,   1*fS, 4*fS, fS), 4, 1, true)); break;
				default: break;
			}
		}
		ePic.addControl(new AnimatedEnemyControl(enemy, hashMap, consApp.getTexture(FileNames.ESHU_TEX)));
		ePic.addFXGLEventHandler(Event.DEATH, (event) -> consApp.getSceneManager().removeEntity(ePic));
		enemy.aliveProperty().addListener(new ChangeListener<Boolean>(){
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
				if(!arg2){
					consApp.getTimerManager().runOnceAfter(() -> ePic.fireFXGLEvent(new FXGLEvent(Event.DEATH)), Duration.seconds(0.01));
				}
			}});
		
		//consApp.getSceneManager().addEntities(ePic);

		return new Pair<Entity, Entity>(enemy, ePic);
	}

	private void onEnemyDeath(FXGLEvent event) {
		Entity enemy = event.getTarget();
		consApp.getSceneManager().removeEntity(enemy);

		// chance based drop logic
		if (consApp.getRandom().nextInt(100) <= 33 || enemy.getProperty(Property.SUB_TYPE) == Type.BOSS) { // check if dropping
			ArrayList<Powerup> drops = new ArrayList<>();
			drops.add(Powerup.RESTORE_HEALTH_12);

			Player p = consApp.player.getProperty(Property.DATA);
			double hpPercent = p.getCurrentHealth() * 1.0 / p.getMaxHealth();

			if (hpPercent < 0.75)
				drops.add(Powerup.RESTORE_HEALTH_25);

			if (hpPercent < 0.5)
				drops.add(Powerup.RESTORE_HEALTH_50);


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
			
			Texture t;
			if(enemy.getProperty(Property.SUB_TYPE) == Type.BOSS){
				t = consApp.getTexture(FileNames.POWERUP_BLOCK);
				t.setPreserveRatio(true);
				t.setFitHeight(30);
			}
			else{
				t = consApp.getTexture(FileNames.FOOD_BLOCK);
				t.setPreserveRatio(true);
				t.setFitHeight(20);

				consApp.getTimerManager().runOnceAfter(() -> {
					consApp.getTimerManager().runAtIntervalWhile(() -> {
						e.setVisible(!e.isVisible());
					}, Duration.seconds(0.1), e.aliveProperty());
				}, Duration.seconds(5));
					
				consApp.getTimerManager().runOnceAfter(() -> e.fireFXGLEvent(new FXGLEvent(Event.DEATH)), Duration.seconds(10));
			}
			
			if(Config.RELEASE && t != null){
				e.setGraphics(t);
			}
			/*else{
				Rectangle r = new Rectangle(30, 30);
				r.setFill(Color.PINK);
				e.setGraphics(r);
			}*/
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