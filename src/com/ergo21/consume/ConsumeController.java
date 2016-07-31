package com.ergo21.consume;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.almasb.consume.Config;
import com.almasb.consume.Config.Speed;
import com.almasb.consume.ConsumeApp;
import com.almasb.consume.Event;
import com.almasb.consume.Types.Actions;
import com.almasb.consume.Types.Element;
import com.almasb.consume.Types.Platform;
import com.almasb.consume.Types.Property;
import com.almasb.consume.Types.Type;
import com.almasb.consume.ai.AimedFireballControl;
import com.almasb.consume.ai.AimedProjectileControl;
import com.almasb.consume.ai.BulletProjectileControl;
import com.almasb.consume.ai.EshuControl;
import com.almasb.consume.ai.FireballProjectileControl;
import com.almasb.consume.ai.IngestControl;
import com.almasb.consume.ai.KnifeControl;
import com.almasb.consume.ai.LightningControl;
import com.almasb.consume.ai.PhysicsControl;
import com.almasb.consume.ai.PillarControl;
import com.almasb.consume.ai.SandEnemyProjectileControl;
import com.almasb.consume.ai.SandProjectileControl;
import com.almasb.consume.ai.ScorpionControl;
import com.almasb.consume.ai.ShakaControl;
import com.almasb.consume.ai.SpearProjectileControl;
import com.almasb.consume.ai.SpearThrowerControl;
import com.almasb.consume.ai.StabControl;
import com.almasb.consume.ai.StabDownControl;
import com.almasb.fxgl.asset.Texture;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;
import com.almasb.fxgl.event.UserAction;

import javafx.animation.FadeTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class ConsumeController {
	private ConsumeApp consApp;
	private HashMap<Actions, KeyCode> defaultKeys;
	private HashMap<Actions, KeyCode> currentKeys;
	private HashMap<Actions, UserAction> allActions;

	public ConsumeController(ConsumeApp a) {
		consApp = a;
		defaultKeys = new HashMap<Actions, KeyCode>();
		currentKeys = new HashMap<Actions, KeyCode>();
		allActions = new HashMap<Actions, UserAction>();
		defaultKeys.put(Actions.INTERACT, KeyCode.ENTER);
		defaultKeys.put(Actions.LEFT, KeyCode.A);
		defaultKeys.put(Actions.RIGHT, KeyCode.D);
		defaultKeys.put(Actions.UP, KeyCode.W);
		defaultKeys.put(Actions.JUMP, KeyCode.SPACE);
		defaultKeys.put(Actions.DOWN, KeyCode.S);
		defaultKeys.put(Actions.SHOOT, KeyCode.Q);
		defaultKeys.put(Actions.CHPOWP, KeyCode.E);
		defaultKeys.put(Actions.CHPOWN, KeyCode.R);

		if(consApp.sSettings.getControls().isEmpty()){
			currentKeys.put(Actions.INTERACT, KeyCode.ENTER);
			currentKeys.put(Actions.LEFT, KeyCode.A);
			currentKeys.put(Actions.RIGHT, KeyCode.D);
			currentKeys.put(Actions.UP, KeyCode.W);
			currentKeys.put(Actions.JUMP, KeyCode.SPACE);
			currentKeys.put(Actions.DOWN, KeyCode.S);
			currentKeys.put(Actions.SHOOT, KeyCode.Q);
			currentKeys.put(Actions.CHPOWP, KeyCode.E);
			currentKeys.put(Actions.CHPOWN, KeyCode.R);
			consApp.sSettings.setControls(currentKeys);
		}
		else{
			currentKeys = consApp.sSettings.getControls();
		}

		fired = false;
		allActions.put(Actions.INTERACT, new UserAction("Interact") {
			@Override
			protected void onActionBegin() {
				if(consApp.introPlaying){
					return;
				}
				
				if (consApp.gScene.isVisible()) {
					if(consApp.player.getProperty("consumed") == null || !(boolean) consApp.player.getProperty("consumed")){
						consApp.gScene.updateScript();
					}
				}
				else if(consApp.player != null && consApp.player.getProperty("inDoor") != null && (boolean) consApp.player.getProperty("inDoor")){
					consApp.player.getControl(PhysicsControl.class).moveX(0);
					consApp.playerData.setCurrentLevel(consApp.playerData.getCurrentLevel() + 1);
					consApp.changeLevel();
				}
				else if(consApp.player.getProperty("jumping") != null && !(boolean) consApp.player.getProperty("jumping") &&
						consApp.player.getProperty("climb") != null && !(boolean) consApp.player.getProperty("climb") &&
						consApp.player.getProperty("eating")!= null && !(boolean) consApp.player.getProperty("eating") && 
						consApp.player.getProperty("eaten") != null && !(boolean) consApp.player.getProperty("eaten")){	
					consApp.player.setProperty("eating", true);
					consApp.player.getControl(PhysicsControl.class).moveX(0);
					consApp.soundManager.playSFX(FileNames.EATING);
					consApp.getTimerManager().runOnceAfter(() ->{
						consApp.player.setProperty("eating", false);
						consApp.player.setProperty("eaten", true);
					}, Duration.seconds(1));
					consApp.getTimerManager().runOnceAfter(() ->{
						consApp.player.setProperty("eaten", false);
					}, Duration.seconds(1.1));
				}
				else if (consApp.player.<Boolean> getProperty("climb") && !consApp.player.<Boolean> getProperty("climbing")){
					consApp.player.setProperty("climb", false);
					consApp.player.setProperty("climbing", false);
					consApp.player.setProperty("jumping", false);
					consApp.player.setProperty(Property.ENABLE_GRAVITY, true);
				}
			}
		});
		allActions.put(Actions.LEFT, new UserAction("Left") {
			@Override
			protected void onAction() {
				if(consApp.introPlaying){
					return;
				}
				
				if (consApp.player != null && consApp.player.getProperty("stunned") != null && !(boolean) consApp.player.getProperty("stunned") &&
												consApp.player.getProperty("eating") != null && !(boolean) consApp.player.getProperty("eating") &&
												consApp.player.getProperty("scenePlaying") != null && !(boolean) consApp.player.getProperty("scenePlaying")) {
					if (!consApp.player.<Boolean> getProperty("climb")){
						consApp.player.getControl(PhysicsControl.class).moveX(-Speed.PLAYER_MOVE);
						consApp.player.setProperty("facingRight", false);
					}
				}
			}

			@Override
			protected void onActionEnd() {
				if(consApp.introPlaying){
					return;
				}
				
				if (consApp.player != null && consApp.player.getProperty("stunned") != null && !(boolean) consApp.player.getProperty("stunned") &&
						consApp.player.getProperty("eating") != null && !(boolean) consApp.player.getProperty("eating") &&
						consApp.player.getProperty("scenePlaying") != null && !(boolean) consApp.player.getProperty("scenePlaying")) {
					consApp.player.getControl(PhysicsControl.class).moveX(0);
				}
			}
		});
		allActions.put(Actions.RIGHT, new UserAction("Move Right") {
			@Override
			protected void onAction() {
				if(consApp.introPlaying){
					return;
				}
				
				if (consApp.player != null && consApp.player.getProperty("stunned") != null &&  !(boolean) consApp.player.getProperty("stunned") &&
						consApp.player.getProperty("eating") != null && !(boolean) consApp.player.getProperty("eating") &&
						consApp.player.getProperty("scenePlaying") != null && !(boolean) consApp.player.getProperty("scenePlaying")) {
					if (!consApp.player.<Boolean> getProperty("climb")){
						consApp.player.getControl(PhysicsControl.class).moveX(Speed.PLAYER_MOVE);
						consApp.player.setProperty("facingRight", true);
					}
				}
			}

			@Override
			protected void onActionEnd() {
				if(consApp.introPlaying){
					return;
				}
				
				if (consApp.player != null && consApp.player.getProperty("stunned") != null && !(boolean) consApp.player.getProperty("stunned") &&
						consApp.player.getProperty("eating") != null && !(boolean) consApp.player.getProperty("eating") && 
								consApp.player.getProperty("scenePlaying") != null && !(boolean) consApp.player.getProperty("scenePlaying")){
					consApp.player.getControl(PhysicsControl.class).moveX(0);
				}
			}
		});
		allActions.put(Actions.UP, new UserAction("Up") {
			@Override
			protected void onAction() {
				if(consApp.introPlaying){
					return;
				}
				
				if (consApp.player != null && consApp.player.getProperty("stunned") != null && !(boolean) consApp.player.getProperty("stunned") &&
						consApp.player.getProperty("eating") != null && !(boolean) consApp.player.getProperty("eating") && 
								consApp.player.getProperty("scenePlaying") != null && !(boolean) consApp.player.getProperty("scenePlaying")){
					if (consApp.player.<Boolean> getProperty("climb")){
						consApp.player.getControl(PhysicsControl.class).climb(-3);
						consApp.player.setProperty("climbing", true);
					}
					else if(!consApp.plBlHandler.laddersOn.isEmpty()){
						double x = consApp.plBlHandler.laddersOn.get(0).getPosition().getX();
						x += ((consApp.plBlHandler.laddersOn.get(0).getWidth()/2) - (consApp.player.getWidth()/2));
						consApp.player.setProperty("climb", true);
						consApp.player.setProperty(Property.ENABLE_GRAVITY, false);
						consApp.player.getControl(PhysicsControl.class).moveX(0);
						consApp.player.getControl(PhysicsControl.class).moveY(0);
						consApp.player.setPosition(x, consApp.player.getPosition().getY());
					}
				}
			}
			
			@Override
			protected void onActionEnd() {
				if(consApp.introPlaying){
					return;
				}
				
				if (consApp.player != null && consApp.player.getProperty("stunned") != null && !(boolean) consApp.player.getProperty("stunned") &&
						consApp.player.getProperty("eating") != null && !(boolean) consApp.player.getProperty("eating") && 
								consApp.player.getProperty("scenePlaying") != null && !(boolean) consApp.player.getProperty("scenePlaying")){
					if (consApp.player.<Boolean> getProperty("climb")){
						consApp.player.getControl(PhysicsControl.class).climb(0);
						consApp.player.setProperty("climbing", false);
					}
				}
			}
		});
		allActions.put(Actions.JUMP, new UserAction("Jump / Climb Up") {
			@Override
			protected void onAction() {
				if(consApp.introPlaying){
					return;
				}
				
				if (consApp.player != null && consApp.player.getProperty("stunned") != null && !(boolean) consApp.player.getProperty("stunned") &&
						consApp.player.getProperty("eating") != null && !(boolean) consApp.player.getProperty("eating") && 
								consApp.player.getProperty("scenePlaying") != null && !(boolean) consApp.player.getProperty("scenePlaying")){
						if (consApp.player.<Boolean> getProperty("climb") && !consApp.player.<Boolean> getProperty("climbing")){
							consApp.player.setProperty("climb", false);
							consApp.player.setProperty("jumping", false);
							consApp.player.setProperty(Property.ENABLE_GRAVITY, true);
							consApp.player.getControl(PhysicsControl.class).jump();
						}
						else if(!consApp.player.<Boolean> getProperty("climb")){
							consApp.player.getControl(PhysicsControl.class).jump();
						}
				}
			}
		});
		allActions.put(Actions.DOWN, new UserAction("Down") {
			@Override
			protected void onAction() {
				if(consApp.introPlaying){
					return;
				}
				
				if (consApp.player != null && consApp.player.getProperty("stunned") != null && !(boolean) consApp.player.getProperty("stunned") &&
						consApp.player.getProperty("eating") != null && !(boolean) consApp.player.getProperty("eating") && 
								consApp.player.getProperty("scenePlaying") != null && !(boolean) consApp.player.getProperty("scenePlaying")) {
					if (consApp.player.<Boolean> getProperty("climb")){
						consApp.player.getControl(PhysicsControl.class).climb(3);
						consApp.player.setProperty("climbing", true);
					}
					else if(!consApp.plBlHandler.laddersOn.isEmpty()){
						double x = consApp.plBlHandler.laddersOn.get(0).getPosition().getX();
						x += ((consApp.plBlHandler.laddersOn.get(0).getWidth()/2) - (consApp.player.getWidth()/2));
						consApp.player.setProperty("climb", true);
						consApp.player.setProperty(Property.ENABLE_GRAVITY, false);
						consApp.player.getControl(PhysicsControl.class).moveX(0);
						consApp.player.getControl(PhysicsControl.class).moveY(0);
						
						consApp.player.setPosition(x, consApp.player.getPosition().getY());
					}
				}
			}
			
			@Override
			protected void onActionEnd() {
				if(consApp.introPlaying){
					return;
				}
				
				if (consApp.player != null && consApp.player.getProperty("stunned") != null && !(boolean) consApp.player.getProperty("stunned") &&
						consApp.player.getProperty("eating") != null && !(boolean) consApp.player.getProperty("eating") && 
								consApp.player.getProperty("scenePlaying") != null && !(boolean) consApp.player.getProperty("scenePlaying")){
					if (consApp.player.<Boolean> getProperty("climb")){
						consApp.player.getControl(PhysicsControl.class).climb(0);
						consApp.player.setProperty("climbing", false);
					}
				}
			}
		});
		allActions.put(Actions.SHOOT, new UserAction("Shoot") {
			private int held = 0;
			private boolean aiming = false;
			private boolean tempFired = false;
			@Override
			protected void onActionBegin() {
				if(consApp.introPlaying){
					return;
				}
				
				if (consApp.player != null && consApp.player.getProperty("stunned") != null && !(boolean) consApp.player.getProperty("stunned") &&
						consApp.player.getProperty("eating") != null && !(boolean) consApp.player.getProperty("eating") && 
						consApp.player.getProperty("climb") != null  && !(boolean) consApp.player.getProperty("climb") &&
						consApp.player.getProperty("scenePlaying") != null && !(boolean) consApp.player.getProperty("scenePlaying")) {
					
					if(consApp.playerData.getCurrentPower() == Element.NEUTRAL){
						held = 0;
						aiming = true;
						tempFired = false;
					}
					else{
						shootProjectile();
					}
				}
			}
			
			@Override
			protected void onAction(){
				if(consApp.introPlaying){
					return;
				}
				
				if(aiming && consApp.playerData.getCurrentPower() == Element.NEUTRAL){
					held++;
					if(held > 10 && !tempFired){
						tempFired = true;
						shootProjectile(true);
					}
				}
			}
			
			@Override
			protected void onActionEnd(){
				if(consApp.introPlaying){
					return;
				}
				
				if(aiming && consApp.playerData.getCurrentPower() == Element.NEUTRAL && !tempFired){
					shootProjectile(false);
				}
				
				aiming = false;
			}
		});
		allActions.put(Actions.CHPOWP, new UserAction("Change Power +") {
			@Override
			protected void onActionBegin() {
				if(consApp.introPlaying){
					return;
				}
				
				if (consApp.player != null && consApp.player.getProperty("stunned") != null && !(boolean) consApp.player.getProperty("stunned") &&
						consApp.player.getProperty("eating") != null && !(boolean) consApp.player.getProperty("eating") && 
								consApp.player.getProperty("scenePlaying") != null && !(boolean) consApp.player.getProperty("scenePlaying")) {
					changePower(1);
				}
			}
		});
		allActions.put(Actions.CHPOWN, new UserAction("Change Power -") {
			@Override
			protected void onActionBegin() {
				if(consApp.introPlaying){
					return;
				}
				
				if (consApp.player != null && consApp.player.getProperty("stunned") != null && !(boolean) consApp.player.getProperty("stunned") &&
						consApp.player.getProperty("eating") != null && !(boolean) consApp.player.getProperty("eating") && 
								consApp.player.getProperty("scenePlaying") != null && !(boolean) consApp.player.getProperty("scenePlaying")) {
					changePower(-1);
				}
			}
		});

	}

	public void initControls(){
		for(Actions action : Actions.values()){
			consApp.getInputManager().addAction(allActions.get(action), currentKeys.get(action));
		}
	}

	public void initControls(HashMap<Actions, KeyCode> newKeyMap) {

		for(Actions action : Actions.values()){
			if(newKeyMap.containsKey(action) && newKeyMap.get(action) != KeyCode.UNDEFINED){
				consApp.getInputManager().rebind(allActions.get(action), newKeyMap.get(action));
				currentKeys.put(action, newKeyMap.get(action));
			}
			else{
				consApp.getInputManager().addAction(allActions.get(action), defaultKeys.get(action));
			}
		}
		consApp.sSettings.setControls(currentKeys);
	}

	private boolean fired;
	private boolean slashed;
	
	public void shootProjectile(){
		shootProjectile(false);
	}

	public void shootProjectile(boolean spearLongThrow) {
		Element element = consApp.playerData.getCurrentPower();

		Entity e = new Entity(Type.PLAYER_PROJECTILE);
		e.setProperty(Property.SUB_TYPE, element);
		e.setPosition(consApp.player.getPosition().add((consApp.player.getWidth() / 2), 0));
		e.setCollidable(true);
		e.setGraphics(new Rectangle(10, 1));
		e.addControl(new PhysicsControl(consApp.physics));
		e.addFXGLEventHandler(Event.COLLIDED_PLATFORM, event -> {
			Entity platform = event.getSource();
			consApp.getSceneManager().removeEntity(e);

			if (platform.getProperty(Property.SUB_TYPE) == Platform.DESTRUCTIBLE && 
					platform.getProperty("desElement") != null && platform.<Element>getProperty("desElement") == element) {
					consApp.destroyBlock(platform);
			}
		});
		e.addFXGLEventHandler(Event.DEATH, event -> {
			consApp.getSceneManager().removeEntity(event.getTarget());
		});

		switch (element) {
		case NEUTRAL: {
			if (!consApp.playerData.getWeaponThrown()) {
				consApp.playerData.setWeaponThrown(e.aliveProperty());
				consApp.soundManager.playSFX(FileNames.SPEAR_THROW);
			} else {
				return;
			}
			e.setGraphics(new Rectangle(30,5));
			e.setVisible(false);
				
			Texture t = consApp.getTexture(FileNames.SPEAR_PROJECTILE);
			t.setPreserveRatio(true);
			t.setFitWidth(30);
			if(!consApp.player.<Boolean>getProperty("facingRight")){
				t.setScaleX(t.getScaleX()*-1);
			}
			Entity ePic = new Entity(Type.ENEMY_PROJECTILE);
			ePic.setProperty(Property.SUB_TYPE, element);
			ePic.setPosition(e.getPosition());
			ePic.translateXProperty().bind(e.translateXProperty());	
			ePic.translateYProperty().bind(e.translateYProperty());
			e.aliveProperty().addListener(new ChangeListener<Boolean>(){
				@Override
				public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
					if(!arg2){
						consApp.getTimerManager().runOnceAfter(() -> {consApp.getSceneManager().removeEntity(ePic);}, Duration.seconds(0.01));
					}
				}});
			ePic.setCollidable(false);
			ePic.setGraphics(t);
			consApp.getSceneManager().addEntities(ePic);
			
			SpearProjectileControl spc = new SpearProjectileControl(consApp.player, ePic, (spearLongThrow ? Speed.PROJECTILE : Speed.PROJECTILE/2), (spearLongThrow ? -Speed.PROJECTILE : -Speed.PROJECTILE+1));
			e.addControl(spc);
			break;
		}
		case NEUTRAL2: {
			if(slashed){
				return;
			}
			
			consApp.getTimerManager().runOnceAfter(() -> slashed = false, Config.CONSUME_DECAY.multiply(2));
			slashed = true;
			
			e.setVisible(false);
			e.setCollidable(true);
			//e.setPosition(e.getPosition().add(4, 0));
			e.setPosition(consApp.player.getPosition());
			e.setGraphics(new Rectangle(0, 0, consApp.player.getWidth(), consApp.player.getHeight()));
			e.setProperty(Property.ENABLE_GRAVITY, false);
			e.addControl(new KnifeControl(consApp.player, 5, 30));
			break;
		}
		case FIRE: {
			e.addControl(new FireballProjectileControl(consApp.player, consApp.player.<Boolean>getProperty("facingRight")));
			e.setProperty(Property.ENABLE_GRAVITY, false);
			if (consApp.playerData.getCurrentMana() >= Config.FIREBALL_COST) {
				consApp.playerData.setCurrentMana(consApp.playerData.getCurrentMana() - Config.FIREBALL_COST);
				consApp.soundManager.playSFX(FileNames.FIRE_COAL);
			} else {
				return;
			}
			if(Config.RELEASE){
				e.setGraphics(new Rectangle(10,10));
				e.setVisible(false);
				
				Texture t = consApp.getTexture(FileNames.COAL_PROJECTILE);
				t = t.toStaticAnimatedTexture(3, Duration.seconds(0.5));
				t.setPreserveRatio(true);
				t.setFitHeight(15);
				if(!consApp.player.<Boolean>getProperty("facingRight")){
					t.setScaleX(t.getScaleX()*-1);
				}
				Entity ePic = new Entity(Type.PLAYER_PROJECTILE);
				ePic.setProperty(Property.SUB_TYPE, element);
				ePic.setPosition(e.getPosition());
				if(consApp.player.<Boolean>getProperty("facingRight")){
					ePic.translateXProperty().bind(e.translateXProperty().add(-20));
				}
				else{
					ePic.translateXProperty().bind(e.translateXProperty());
				}			
				ePic.translateYProperty().bind(e.translateYProperty().add(-5));
				e.aliveProperty().addListener(new ChangeListener<Boolean>(){
					@Override
					public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
						if(!arg2){
							consApp.getTimerManager().runOnceAfter(() -> {consApp.getSceneManager().removeEntity(ePic);}, Duration.seconds(0.01));
						}
					}});
				ePic.setCollidable(false);
				ePic.setGraphics(t);
				consApp.getSceneManager().addEntities(ePic);
			}
			
			break;
		}
		case EARTH: {
			if (consApp.playerData.getCurrentMana() >= Config.SAND_COST) {
				consApp.playerData.setCurrentMana(consApp.playerData.getCurrentMana() - Config.SAND_COST);
			} else {
				return;
			}
			Point2D p = consApp.player.getPosition();
			if ((boolean) consApp.player.getProperty("facingRight")) {
				p = p.add(consApp.player.getWidth(), 0);
			} else {
				p = p.add(-e.getWidth(), 0);
			}
			e.setPosition(p);
			e.addControl(new SandProjectileControl(consApp.player, false));
			e.setProperty(Property.ENABLE_GRAVITY, false);
			
			Entity e2 = new Entity(Type.PLAYER_PROJECTILE);
			e2.setProperty(Property.SUB_TYPE, element);
			e2.setPosition(p);
			e2.setCollidable(true);
			e2.setGraphics(new Rectangle(10, 1));
			e2.addControl(new PhysicsControl(consApp.physics));
			e2.addFXGLEventHandler(Event.COLLIDED_PLATFORM, event -> {
				Entity platform = event.getSource();
				consApp.getSceneManager().removeEntity(e2);

				if (platform.getProperty(Property.SUB_TYPE) == Platform.DESTRUCTIBLE && 
						platform.getProperty("desElement") != null && platform.<Element>getProperty("desElement") == element) {
						consApp.destroyBlock(platform);
				}
			});
			e2.addFXGLEventHandler(Event.DEATH, event -> {
				consApp.getSceneManager().removeEntity(event.getTarget());
			});
			e2.addControl(new SandProjectileControl(consApp.player, true));
			e2.setProperty(Property.ENABLE_GRAVITY, false);
			
			if(Config.RELEASE){
				e.setGraphics(new Rectangle(10,10));
				e.setVisible(false);
				
				e2.setGraphics(new Rectangle(10,10));
				e2.setVisible(false);
				consApp.getTimerManager().runOnceAfter(() ->{
					Texture t = consApp.getTexture(FileNames.SAND_PROJECTILE);
					Texture t2 = consApp.getTexture(FileNames.SAND_PROJECTILE);
					t = t.toStaticAnimatedTexture(3, Config.SAND_DECAY);
					t.setPreserveRatio(true);
					t.setFitHeight(20);
					t2 = t2.toStaticAnimatedTexture(3, Config.SAND_DECAY);
					t2.setPreserveRatio(true);
					t2.setFitHeight(20);
					if(!consApp.player.<Boolean>getProperty("facingRight")){
						t.setScaleX(t.getScaleX()*-1);
						t2.setScaleX(t2.getScaleX()*-1);
					}
					Entity ePic = new Entity(Type.PLAYER_PROJECTILE);
					ePic.setProperty(Property.SUB_TYPE, element);
					ePic.setPosition(e.getPosition());
					if(consApp.player.<Boolean>getProperty("facingRight")){
						ePic.translateXProperty().bind(e.translateXProperty().add(-10));
					}
					else{
						ePic.translateXProperty().bind(e.translateXProperty());
					}			
					ePic.translateYProperty().bind(e.translateYProperty().add(-5));
					e.aliveProperty().addListener(new ChangeListener<Boolean>(){
						@Override
						public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
							if(!arg2){
								consApp.getTimerManager().runOnceAfter(() -> {consApp.getSceneManager().removeEntity(ePic);}, Duration.seconds(0.01));
							}
						}});
					ePic.setCollidable(false);
					ePic.setGraphics(t);
					consApp.getSceneManager().addEntities(ePic);
					
					Entity ePic2 = new Entity(Type.PLAYER_PROJECTILE);
					ePic2.setProperty(Property.SUB_TYPE, element);
					ePic2.setPosition(e.getPosition());
					if(consApp.player.<Boolean>getProperty("facingRight")){
						ePic2.translateXProperty().bind(e2.translateXProperty().add(-10));
					}
					else{
						ePic2.translateXProperty().bind(e2.translateXProperty());
					}			
					ePic2.translateYProperty().bind(e2.translateYProperty().add(-5));
					e2.aliveProperty().addListener(new ChangeListener<Boolean>(){
						@Override
						public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
							if(!arg2){
								consApp.getTimerManager().runOnceAfter(() -> {consApp.getSceneManager().removeEntity(ePic2);}, Duration.seconds(0.01));
							}
						}});
					ePic2.setCollidable(false);
					ePic2.setGraphics(t2);
					consApp.getSceneManager().addEntities(ePic2);
				}, Config.SAND_DELAY);
				
				//e2.setRotate(-30);
			}
			
			consApp.getSceneManager().addEntities(e2);
			break;
		}
		case METAL: {
			if (consApp.playerData.getCurrentMana() >= Config.BULLET_COST && !fired) {
				consApp.playerData.setCurrentMana(consApp.playerData.getCurrentMana() - Config.BULLET_COST);
				consApp.soundManager.playSFX(FileNames.RIFLE_SHOT);
			} else {
				return;
			}
			
			consApp.getTimerManager().runOnceAfter(() -> fired = false, Duration.seconds(3));
			fired = true;
			e.addControl(new BulletProjectileControl(consApp.player, consApp.player.getProperty("facingRight")));
			Polygon p = new Polygon();
			p.getPoints().addAll(new Double[]{
					0.0,0.0,
					0.0,5.0,
					10.0,2.5
			});
			p.setFill(Color.SILVER);
			if(!consApp.player.<Boolean>getProperty("facingRight")){
				p.setScaleX(p.getScaleX()*-1);
				e.setPosition(consApp.player.getPosition().add(-consApp.player.getWidth()/2 - 10, 10));
			}
			else{
				e.setPosition(consApp.player.getPosition().add(consApp.player.getWidth()*3/2, 10));
			}
			e.setGraphics(p);
			e.setProperty(Property.ENABLE_GRAVITY, false);
			

			Entity relBar = Entity.noType();
			relBar.translateXProperty().bind(consApp.player.translateXProperty().subtract(10));
			relBar.translateYProperty().bind(consApp.player.translateYProperty().subtract(30));
			ProgressBar rBar = new ProgressBar();
			rBar.setProgress(0.01);
			rBar.setPrefWidth(40);
			rBar.getStylesheets().add("assets/ui/css/reload.css");
			
			relBar.setGraphics(rBar);
			FadeTransition fT = new FadeTransition(Duration.millis(500), relBar);
			fT.setFromValue(1);
			fT.setToValue(0.25);
			fT.setCycleCount(9);
			fT.setAutoReverse(true);
			fT.play();
			
			consApp.getTimerManager().runAtIntervalWhile(() -> {
					if(rBar.getProgress() < 1){
						rBar.setProgress(rBar.getProgress() + 0.01);
					}
				}, Duration.seconds(0.03), relBar.aliveProperty());
			
			consApp.getTimerManager().runOnceAfter(() -> {
				consApp.getSceneManager().removeEntity(relBar);
			}, Duration.seconds(3));

			consApp.getSceneManager().addEntities(relBar);
			
			break;
		}
		case LIGHTNING: {
			if (consApp.playerData.getCurrentMana() >= Config.LIGHTNING_COST) {
				consApp.playerData.setCurrentMana(consApp.playerData.getCurrentMana() - Config.LIGHTNING_COST);			
			} else {
				return;
			}
			e.setVisible(false);
			e.setCollidable(false);
			Group g = new Group();
			LightningControl lc = new LightningControl(g);
			e.addControl(lc);
			Point2D p = consApp.player.getPosition();
			if ((boolean) consApp.player.getProperty("facingRight")) {
				p = p.add(consApp.player.getWidth() + 30, 0);
			} else {
				p = p.add(-e.getWidth() - 30, 0);
			}
			p = p.add(0, consApp.player.getHeight() - 250);
			e.setPosition(p);

			g.getChildren().addAll(createBolt(new Point2D(0, 0), new Point2D(0, 250), 5));
			
			e.setGraphics(g);

			DropShadow shadow = new DropShadow(20, Color.PURPLE);
			shadow.setInput(new Glow(0.7));
			g.setEffect(shadow);
			e.setProperty(Property.ENABLE_GRAVITY, false);
			
			Entity e2 = new Entity(Type.PLAYER_PROJECTILE);
			e2.setProperty(Property.SUB_TYPE, element);
			e2.setPosition(p.add(0, 220));
			e2.setCollidable(true);
			e2.setVisible(false);
			e2.setGraphics(new Rectangle(10, 10));
			e2.addControl(new PhysicsControl(consApp.physics));
			e2.addFXGLEventHandler(Event.COLLIDED_PLATFORM, event -> {
				Entity platform = event.getSource();
				consApp.getSceneManager().removeEntity(e2);

				if (platform.getProperty(Property.SUB_TYPE) == Platform.DESTRUCTIBLE && 
					platform.getProperty("desElement") != null && platform.<Element>getProperty("desElement") == element) {
					consApp.destroyBlock(platform);
				}
			});
			e2.addFXGLEventHandler(Event.DEATH, event -> {
				consApp.getSceneManager().removeEntity(event.getTarget());
			});
			e2.translateYProperty().addListener(new ChangeListener<Number>(){
				@Override
				public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
					if(newValue.doubleValue() + 30 > e.getPosition().getY() + e.getHeight()){
						e2.fireFXGLEvent(new FXGLEvent(Event.DEATH));
					}
				}});
			e.aliveProperty().addListener(new ChangeListener<Boolean>(){
				@Override
				public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
					consApp.getTimerManager().runOnceAfter(() -> {
						e2.fireFXGLEvent(new FXGLEvent(Event.DEATH));
					}, Duration.seconds(0.01));
				}});
			
			consApp.getTimerManager().runOnceAfter(() ->{
				consApp.getSceneManager().addEntities(e2);
			}, Config.LIGHTNING_DELAY);
			
			consApp.getTimerManager().runOnceAfter(() -> {
				consApp.soundManager.playSFX(FileNames.LIGHTNING_DRUM);
			}, Config.LIGHTNING_DELAY.divide(2));
			break;
		}
		case DEATH: {
			if (consApp.playerData.getCurrentMana() >= Config.DEATH_COST) {
				consApp.playerData.setCurrentMana(consApp.playerData.getCurrentMana() - Config.DEATH_COST);			
			} else {
				return;
			}
			if(Config.RELEASE){
				Texture t = consApp.getTexture(FileNames.ANKH_PROJECTILE);
				t.setPreserveRatio(true);
				t.setFitHeight(15);
				e.setGraphics(t);
			}
			break;
		}
		case CONSUME: {
			if(slashed){
				return;
			}
			
			consApp.getTimerManager().runOnceAfter(() -> slashed = false, Config.CONSUME_DECAY.multiply(2));
			slashed = true;
			e.setVisible(false);
			e.setCollidable(true);
			e.setGraphics(new Rectangle(0, 0, consApp.player.getWidth() / 2, consApp.player.getHeight()));
			e.setProperty(Property.ENABLE_GRAVITY, false);
			e.addControl(new IngestControl(consApp.player));
			
			break;
		}
		}
		
		consApp.player.setProperty("attacking", true);
		consApp.getTimerManager().runOnceAfter(() ->{
			consApp.player.setProperty("attacking", false);
		}, Duration.seconds(0.5));
		
		consApp.getSceneManager().addEntities(e);
	}
	
	public void enemyShootProjectile(Element element, Entity source) {
		Entity e = new Entity(Type.ENEMY_PROJECTILE);
		if(source.<Enemy>getProperty(Property.DATA) != null){
			e.setProperty(Property.SUB_TYPE, source.<Enemy>getProperty(Property.DATA).getElement());
		}
		else{
			e.setProperty(Property.SUB_TYPE, element);
		}
		
		e.setPosition(source.getPosition().add((source.getWidth() / 2), 0));
		e.setCollidable(true);
		e.setGraphics(new Rectangle(10, 1));
		e.addControl(new PhysicsControl(consApp.physics));
		e.addFXGLEventHandler(Event.COLLIDED_PLATFORM, event -> {
			consApp.getSceneManager().removeEntity(e);
		});
		e.addFXGLEventHandler(Event.DEATH, event -> {
			consApp.getSceneManager().removeEntity(event.getTarget());
		});

		switch (element) {
		case NEUTRAL: {
			if((source.getControl(SpearThrowerControl.class) != null && source.getControl(SpearThrowerControl.class).isShortThrow()) ||
					(source.getControl(ShakaControl.class) != null && source.getControl(ShakaControl.class).isShortThrow()) ||
					(source.getControl(EshuControl.class) != null && source.getControl(EshuControl.class).isShortThrow())){
				
				e.setGraphics(new Rectangle(30,5));
				e.setVisible(false);

				Texture t = consApp.getTexture(FileNames.SPEAR_PROJECTILE);
				t.setPreserveRatio(true);
				t.setFitWidth(30);
				if(!source.<Boolean>getProperty("facingRight")){
					t.setScaleX(t.getScaleX()*-1);
				}
				Entity ePic = new Entity(Type.ENEMY_PROJECTILE);
				ePic.setProperty(Property.SUB_TYPE, element);
				ePic.setPosition(e.getPosition());

				if(source.<Boolean>getProperty("facingRight")){
					ePic.translateXProperty().bind(e.translateXProperty().add(5));
				}
				else{
					ePic.translateXProperty().bind(e.translateXProperty().add(-5));
				}			
				ePic.translateYProperty().bind(e.translateYProperty());
				e.aliveProperty().addListener(new ChangeListener<Boolean>(){
					@Override
					public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
						if(!arg2){
							consApp.getTimerManager().runOnceAfter(() -> {consApp.getSceneManager().removeEntity(ePic);}, Duration.seconds(0.01));
						}
					}});
				ePic.setCollidable(false);
				ePic.setGraphics(t);
				consApp.getSceneManager().addEntities(ePic);
				
				
				SpearProjectileControl spc = new SpearProjectileControl(source, ePic, Speed.PROJECTILE, -Speed.PROJECTILE/2);
				e.addControl(spc);
			}
			else {
				e.setGraphics(new Rectangle(30,5));
				e.setVisible(false);

				Texture t = consApp.getTexture(FileNames.SPEAR_PROJECTILE);
				t.setPreserveRatio(true);
				t.setFitWidth(30);
				if(!source.<Boolean>getProperty("facingRight")){
					t.setScaleX(t.getScaleX()*-1);
				}
				Entity ePic = new Entity(Type.ENEMY_PROJECTILE);
				ePic.setProperty(Property.SUB_TYPE, element);
				ePic.setPosition(e.getPosition());
				if(source.<Boolean>getProperty("facingRight")){
					ePic.translateXProperty().bind(e.translateXProperty().add(5));
				}
				else{
					ePic.translateXProperty().bind(e.translateXProperty().add(-5));
				}			
				ePic.translateYProperty().bind(e.translateYProperty());
				e.aliveProperty().addListener(new ChangeListener<Boolean>(){
					@Override
					public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
						if(!arg2){
							consApp.getTimerManager().runOnceAfter(() -> {consApp.getSceneManager().removeEntity(ePic);}, Duration.seconds(0.01));
						}
					}});
				ePic.setCollidable(false);
				ePic.setGraphics(t);
				consApp.getSceneManager().addEntities(ePic);
				
				
				SpearProjectileControl spc = new SpearProjectileControl(source, ePic);
				e.addControl(spc);
			}
			consApp.soundManager.playSFX(FileNames.SPEAR_THROW);
			
			break;
		}
		case NEUTRAL2: {
			e.setVisible(false);
			e.setCollidable(true);
			e.setPosition(source.getPosition());
			e.setGraphics(new Rectangle(0, 0, source.getWidth(), source.getHeight()));
			e.setProperty(Property.ENABLE_GRAVITY, false);
			e.addControl(new KnifeControl(source, 5, 20));
			break;
		}
		case FIRE: {
			e.addControl(new FireballProjectileControl(consApp.player, source.<Boolean>getProperty("facingRight")));
			e.setProperty(Property.ENABLE_GRAVITY, false);
			consApp.soundManager.playSFX(FileNames.FIRE_COAL);
			
			if(Config.RELEASE){
				e.setGraphics(new Rectangle(10,10));
				e.setVisible(false);
				
				Texture t = consApp.getTexture(FileNames.COAL_PROJECTILE);
				t = t.toStaticAnimatedTexture(3, Duration.seconds(0.5));
				t.setPreserveRatio(true);
				t.setFitHeight(15);
				if(!source.<Boolean>getProperty("facingRight")){
					t.setScaleX(t.getScaleX()*-1);
				}
				Entity ePic = new Entity(Type.ENEMY_PROJECTILE);
				ePic.setProperty(Property.SUB_TYPE, element);
				ePic.setPosition(e.getPosition());
				if(source.<Boolean>getProperty("facingRight")){
					ePic.translateXProperty().bind(e.translateXProperty().add(-20));
				}
				else{
					ePic.translateXProperty().bind(e.translateXProperty());
				}			
				ePic.translateYProperty().bind(e.translateYProperty().add(-5));
				e.aliveProperty().addListener(new ChangeListener<Boolean>(){
					@Override
					public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
						if(!arg2){
							consApp.getTimerManager().runOnceAfter(() -> {consApp.getSceneManager().removeEntity(ePic);}, Duration.seconds(0.01));
						}
					}});
				ePic.setCollidable(false);
				ePic.setGraphics(t);
				consApp.getSceneManager().addEntities(ePic);
			}
			
			break;
		}
		case EARTH: {
			Point2D p = source.getPosition();
			if ((boolean) source.getProperty("facingRight")) {
				p = p.add(source.getWidth(), 0);
			} else {
				p = p.add(-e.getWidth(), 0);
			}
			e.setPosition(p);
			e.addControl(new SandEnemyProjectileControl(source, false));
			e.setProperty(Property.ENABLE_GRAVITY, false);

			Entity e2 = new Entity(Type.ENEMY_PROJECTILE);
			e2.setProperty(Property.SUB_TYPE, element);
			e2.setPosition(p);
			e2.setCollidable(true);
			e2.setGraphics(new Rectangle(10, 1));
			e2.addControl(new PhysicsControl(consApp.physics));
			e2.addFXGLEventHandler(Event.COLLIDED_PLATFORM, event -> {
				consApp.getSceneManager().removeEntity(e2);
			});
			e2.addFXGLEventHandler(Event.DEATH, event -> {
				consApp.getSceneManager().removeEntity(event.getTarget());
			});
			e2.addControl(new SandProjectileControl(source, true));
			e2.setProperty(Property.ENABLE_GRAVITY, false);

			consApp.getSceneManager().addEntities(e2);
			
			if(Config.RELEASE){
				e.setGraphics(new Rectangle(10,10));
				e.setVisible(false);
				
				e2.setGraphics(new Rectangle(10,10));
				e2.setVisible(false);
				consApp.getTimerManager().runOnceAfter(() ->{
					Texture t = consApp.getTexture(FileNames.SAND_PROJECTILE);
					Texture t2 = consApp.getTexture(FileNames.SAND_PROJECTILE);
					t = t.toStaticAnimatedTexture(3, Config.SAND_DECAY);
					t.setPreserveRatio(true);
					t.setFitHeight(20);
					t2 = t2.toStaticAnimatedTexture(3, Config.SAND_DECAY);
					t2.setPreserveRatio(true);
					t2.setFitHeight(20);
					if(consApp.player.<Boolean>getProperty("facingRight") != null
						&& !consApp.player.<Boolean>getProperty("facingRight")){
						t.setScaleX(t.getScaleX()*-1);
						t2.setScaleX(t2.getScaleX()*-1);
					}
					Entity ePic = new Entity(Type.ENEMY_PROJECTILE);
					ePic.setProperty(Property.SUB_TYPE, element);
					ePic.setPosition(e.getPosition());
					if(consApp.player.<Boolean>getProperty("facingRight")){
						ePic.translateXProperty().bind(e.translateXProperty().add(-10));
					}
					else{
						ePic.translateXProperty().bind(e.translateXProperty());
					}			
					ePic.translateYProperty().bind(e.translateYProperty().add(-5));
					e.aliveProperty().addListener(new ChangeListener<Boolean>(){
						@Override
						public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
							if(!arg2){
								consApp.getTimerManager().runOnceAfter(() -> {consApp.getSceneManager().removeEntity(ePic);}, Duration.seconds(0.01));
							}
						}});
					ePic.setCollidable(false);
					ePic.setGraphics(t);
					consApp.getSceneManager().addEntities(ePic);
					
					Entity ePic2 = new Entity(Type.ENEMY_PROJECTILE);
					ePic2.setProperty(Property.SUB_TYPE, element);
					ePic2.setPosition(e.getPosition());
					if(consApp.player.<Boolean>getProperty("facingRight")){
						ePic2.translateXProperty().bind(e2.translateXProperty().add(-10));
					}
					else{
						ePic2.translateXProperty().bind(e2.translateXProperty());
					}			
					ePic2.translateYProperty().bind(e2.translateYProperty().add(-5));
					e2.aliveProperty().addListener(new ChangeListener<Boolean>(){
						@Override
						public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
							if(!arg2){
								consApp.getTimerManager().runOnceAfter(() -> {consApp.getSceneManager().removeEntity(ePic2);}, Duration.seconds(0.01));
							}
						}});
					ePic2.setCollidable(false);
					ePic2.setGraphics(t2);
					consApp.getSceneManager().addEntities(ePic2);
				}, Config.SAND_DELAY);
				
				//e2.setRotate(-30);
			}
			
			break;
		}
		case METAL: {
			if(source != null && source.getControl(ScorpionControl.class) == null){
				consApp.soundManager.playSFX(FileNames.RIFLE_SHOT);
			}
			else{
				consApp.soundManager.playSFX(FileNames.SPEAR_THROW);
			}
			e.addControl(new BulletProjectileControl(consApp.player, source.getProperty("facingRight")));
			e.setProperty(Property.ENABLE_GRAVITY, false);
			
			Polygon p = new Polygon();
			p.getPoints().addAll(new Double[]{
					0.0,0.0,
					0.0,5.0,
					10.0,2.5
			});
			p.setFill(Color.SILVER);
			if(!source.<Boolean>getProperty("facingRight")){
				p.setScaleX(p.getScaleX()*-1);
				e.setPosition(source.getPosition().add(-Math.round(source.getWidth()/2 - 10), 10));
			}
			else{
				e.setPosition(source.getPosition().add(Math.round(source.getWidth()*3/2), 10));
			}
			e.setGraphics(p);
			
			break;
		}
		case LIGHTNING: {
			e.setVisible(false);
			e.setCollidable(false);
			Group g = new Group();
			e.addControl(new PhysicsControl(consApp.physics));
			LightningControl lc = new LightningControl(g);
			e.addControl(lc);
			Point2D p = source.getPosition();
			if ((boolean) source.getProperty("facingRight")) {
				p = p.add(source.getWidth() + 30, 0);
			} else {
				p = p.add(-e.getWidth() - 30, 0);
			}
			p = p.add(0, source.getHeight());
			e.setPosition(0, 0);

			g.getChildren().addAll(createBolt(new Point2D(p.getX(), -200), p, 5));
			e.setGraphics(g);

			DropShadow shadow = new DropShadow(20, Color.PURPLE);
			shadow.setInput(new Glow(0.7));
			g.setEffect(shadow);
			e.setProperty(Property.ENABLE_GRAVITY, false);
			
			consApp.getTimerManager().runOnceAfter(() -> {
				consApp.soundManager.playSFX(FileNames.LIGHTNING_DRUM);
			}, Config.LIGHTNING_DELAY.divide(2));
			break;
		}
		case DEATH: {
			if(Config.RELEASE){
				Texture t = consApp.getTexture(FileNames.ANKH_PROJECTILE);
				t.setPreserveRatio(true);
				t.setFitHeight(15);
				e.setGraphics(t);
			}
			break;
		}
		case CONSUME: {
			// e.setVisible(true);
			e.setCollidable(true);
			e.setGraphics(new Rectangle(0, 0, source.getWidth() / 2, source.getHeight()));
			e.setProperty(Property.ENABLE_GRAVITY, false);
			e.addControl(new IngestControl(consApp.player));
			
			break;
		}
		}

		consApp.getSceneManager().addEntities(e);
	}

	public void aimedProjectile(Entity source, Entity target) {
		Enemy sourceData = source.getProperty(Property.DATA);
		Type t = Type.ENEMY_PROJECTILE;
		if (source == consApp.player) {
			t = Type.PLAYER_PROJECTILE;
		}

		Entity e = new Entity(t);
		e.setProperty(Property.SUB_TYPE, sourceData.getElement());
		e.setPosition(source.getPosition().add(0, source.getHeight() / 2));
		e.setCollidable(true);
		e.setGraphics(new Rectangle(10, 1));
		e.addControl(new PhysicsControl(consApp.physics));
		e.addControl(new AimedProjectileControl(source, target));
		e.addFXGLEventHandler(Event.DEATH, event -> {
			consApp.getSceneManager().removeEntity(event.getTarget());
		});

		e.setProperty(Property.ENABLE_GRAVITY, false);

		consApp.getSceneManager().addEntities(e);
	}
	
	public void aimedFireball(Entity source, Entity target) {
		Enemy sourceData = source.getProperty(Property.DATA);
		Type ty = Type.ENEMY_PROJECTILE;
		if (source == consApp.player) {
			ty = Type.PLAYER_PROJECTILE;
		}

		Entity e = new Entity(ty);
		e.setProperty(Property.SUB_TYPE, sourceData.getElement());
		e.setPosition(source.getPosition().add(source.getWidth(), source.getHeight() / 2));
		e.setCollidable(true);
		e.setGraphics(new Rectangle(10, 1));
		e.addControl(new PhysicsControl(consApp.physics));
		e.addControl(new AimedFireballControl(source, target));
		e.addFXGLEventHandler(Event.COLLIDED_PLATFORM, event -> {
			consApp.getSceneManager().removeEntity(e);
		});
		e.addFXGLEventHandler(Event.DEATH, event -> {
			consApp.getSceneManager().removeEntity(event.getTarget());
		});

		e.setProperty(Property.ENABLE_GRAVITY, false);

		consApp.soundManager.playSFX(FileNames.FIRE_COAL);
		
		if(Config.RELEASE){
			e.setGraphics(new Rectangle(10,10));
			e.setVisible(false);
			
			Texture t = consApp.getTexture(FileNames.COAL_PROJECTILE);
			t = t.toStaticAnimatedTexture(3, Duration.seconds(0.5));
			t.setPreserveRatio(true);
			t.setFitHeight(15);
			if(!source.<Boolean>getProperty("facingRight")){
				t.setScaleX(t.getScaleX()*-1);
			}
			Entity ePic = new Entity(Type.ENEMY_PROJECTILE);
			ePic.setProperty(Property.SUB_TYPE, sourceData.getElement());
			ePic.setPosition(e.getPosition());
			if(source.<Boolean>getProperty("facingRight")){
				ePic.translateXProperty().bind(e.translateXProperty().add(-20));
			}
			else{
				ePic.translateXProperty().bind(e.translateXProperty());
			}			
			ePic.translateYProperty().bind(e.translateYProperty().add(-5));
			e.aliveProperty().addListener(new ChangeListener<Boolean>(){
				@Override
				public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
					if(!arg2){
						consApp.getTimerManager().runOnceAfter(() -> {consApp.getSceneManager().removeEntity(ePic);}, Duration.seconds(0.01));
					}
				}});
			ePic.setCollidable(false);
			ePic.setGraphics(t);
			consApp.getSceneManager().addEntities(ePic);
		}
		
		consApp.getSceneManager().addEntities(e);
	}
	
	public void aimedLightningBolt(Entity source, Point2D target) {
		Entity e = new Entity(Type.ENEMY_PROJECTILE);
		e.setProperty(Property.SUB_TYPE, Element.LIGHTNING);
		e.addControl(new PhysicsControl(consApp.physics));
		e.addFXGLEventHandler(Event.COLLIDED_PLATFORM, event -> {
			consApp.getSceneManager().removeEntity(e);
		});
		e.addFXGLEventHandler(Event.DEATH, event -> {
			consApp.getSceneManager().removeEntity(event.getTarget());
		});

		e.setVisible(false);
		e.setCollidable(false);
		Group g = new Group();
		e.addControl(new PhysicsControl(consApp.physics));
		LightningControl lc = new LightningControl(g);
		e.addControl(lc);
		e.setPosition(0, 0);

		g.getChildren().addAll(createBolt(new Point2D(target.getX(), -200), target, 5));
		e.setGraphics(g);

		DropShadow shadow = new DropShadow(20, Color.PURPLE);
		shadow.setInput(new Glow(0.7));
		g.setEffect(shadow);
		e.setProperty(Property.ENABLE_GRAVITY, false);
			
		consApp.getTimerManager().runOnceAfter(() -> {
			consApp.soundManager.playSFX(FileNames.LIGHTNING_DRUM);
		}, Config.LIGHTNING_DELAY.divide(2));

		consApp.getSceneManager().addEntities(e);
	}
	
	public void enemyShootStones(Entity source) {
		boolean lowFirst = consApp.getRandom().nextBoolean();
		for(int i = 0; i< 3; i++){
			Entity e = new Entity(Type.ENEMY_PROJECTILE);
			if(source.<Enemy>getProperty(Property.DATA) != null){
				e.setProperty(Property.SUB_TYPE, source.<Enemy>getProperty(Property.DATA).getElement());
			}
			else{
				e.setProperty(Property.SUB_TYPE, Element.EARTH);
			}
		
			e.setPosition(source.getPosition().add((source.getWidth() / 2), 0));
			e.setCollidable(true);
			Texture t = consApp.getTexture(FileNames.STONE_PROJ);
			t.setPreserveRatio(true);
			t.setFitHeight(10);
			e.setGraphics(t);
			e.addControl(new PhysicsControl(consApp.physics));
			e.addFXGLEventHandler(Event.COLLIDED_PLATFORM, event -> {
				consApp.getSceneManager().removeEntity(e);
			});
			e.addFXGLEventHandler(Event.DEATH, event -> {
				consApp.getSceneManager().removeEntity(event.getTarget());
			});
			
			switch(i){
				case 0:{
					e.addControl(new SpearProjectileControl(source, Speed.PROJECTILE, -Speed.PROJECTILE/2));
					if(lowFirst){
						consApp.getTimerManager().runOnceAfter(() -> {
							consApp.getSceneManager().addEntities(e);
							consApp.soundManager.playSFX(FileNames.STONE_THROW);
						}, Config.ENEMY_STONE_THROW_DELAY);
					}
					else{
						consApp.getTimerManager().runOnceAfter(() -> {
							consApp.getSceneManager().addEntities(e);
							consApp.soundManager.playSFX(FileNames.STONE_THROW);
						}, Config.ENEMY_STONE_THROW_DELAY.multiply(3));
					}
					break;
				}
				case 1:{
					e.addControl(new SpearProjectileControl(source, Speed.PROJECTILE*2/3, -Speed.PROJECTILE*2/3));
					consApp.getTimerManager().runOnceAfter(() -> {
						consApp.getSceneManager().addEntities(e);
						consApp.soundManager.playSFX(FileNames.STONE_THROW);
					}, Config.ENEMY_STONE_THROW_DELAY.multiply(2));
					break;
				}
				case 2:{
					e.addControl(new SpearProjectileControl(source, Speed.PROJECTILE/2, -Speed.PROJECTILE*3/2));
					if(lowFirst){
						consApp.getTimerManager().runOnceAfter(() -> {
							consApp.getSceneManager().addEntities(e);
							consApp.soundManager.playSFX(FileNames.STONE_THROW);
						}, Config.ENEMY_STONE_THROW_DELAY.multiply(3));
					}
					else{
						consApp.getTimerManager().runOnceAfter(() -> {
							consApp.getSceneManager().addEntities(e);
							consApp.soundManager.playSFX(FileNames.STONE_THROW);
						}, Config.ENEMY_STONE_THROW_DELAY);
					}
					break;
				}
				default:{
					e.addControl(new SpearProjectileControl(source, Speed.PROJECTILE, -Speed.PROJECTILE/2));
					break;
				}
			}
		}
	}
	
	public void enemyShootFireball(Entity source) {

		Entity e = new Entity(Type.ENEMY_PROJECTILE);
		if(source.<Enemy>getProperty(Property.DATA) != null){
			e.setProperty(Property.SUB_TYPE, source.<Enemy>getProperty(Property.DATA).getElement());
		}
		else{
			e.setProperty(Property.SUB_TYPE, Element.FIRE);
		}
		
		e.setPosition(source.getPosition().add(7, source.getHeight()/4));
		e.setCollidable(true);
		Texture t = consApp.getTexture(FileNames.FIREBALL_PROJ);
		t.setPreserveRatio(false);
		t.setFitWidth(10);
		t.setFitHeight(10);
		e.setGraphics(t);
		e.addFXGLEventHandler(Event.DEATH, event -> {
			consApp.getSceneManager().removeEntity(event.getTarget());
		});
		
		consApp.getTimerManager().runOnceAfter(() -> {
			t.setFitWidth(15);
			t.setFitHeight(30);
			e.setGraphics(t);
			e.setPosition(e.getPosition().subtract(2.5, 0));
		}, Config.ENEMY_FIRE_GROWTH_DELAY);
		consApp.getTimerManager().runOnceAfter(() -> {
			t.setFitWidth(20);
			t.setFitHeight(60);
			e.setGraphics(t);
			e.setPosition(e.getPosition().subtract(2.5, 0));
		}, Config.ENEMY_FIRE_GROWTH_DELAY.multiply(2));
		consApp.getTimerManager().runOnceAfter(() -> {
			e.fireFXGLEvent(new FXGLEvent(Event.DEATH));
		}, Config.ENEMY_FIRE_GROWTH_DELAY.multiply(3));
				
		consApp.soundManager.playSFX(FileNames.FIRE_TRAP);
		
		consApp.getSceneManager().addEntities(e);
	}
	
	public void enemyShootCannon(Entity source) {
		Entity e = new Entity(Type.ENEMY_PROJECTILE);
		e.setProperty(Property.SUB_TYPE, Element.METAL);
		e.setProperty("isCannonball", true);
		e.setCollidable(true);
		PhysicsControl pc = new PhysicsControl(consApp.physics);
		e.addControl(pc);
		pc.moveY(-5);
		e.addFXGLEventHandler(Event.COLLIDED_PLATFORM, event -> {
			consApp.getSceneManager().removeEntity(e);
		});
		e.addFXGLEventHandler(Event.DEATH, event -> {
			consApp.getSceneManager().removeEntity(event.getTarget());
		});

		consApp.soundManager.playSFX(FileNames.CANNON_SHOT);
		e.addControl(new BulletProjectileControl(consApp.player, source.getProperty("facingRight")));
		e.setProperty(Property.ENABLE_GRAVITY, true);
			
		Circle p = new Circle(8);
		p.setFill(Color.GRAY);
		p.setStroke(Color.BLACK);
		if(!source.<Boolean>getProperty("facingRight")){
			e.setPosition(source.getPosition().add(-source.getWidth()/2 - 10, 0));
		}
		else{
			e.setPosition(source.getPosition().add(source.getWidth()*3/2, 0));
		}
		e.setGraphics(p);

		consApp.getSceneManager().addEntities(e);
	}
	
	public void enemyPlayFlute(Entity source) {

		Entity e = new Entity(Type.ENEMY_PROJECTILE);
		if(source.<Enemy>getProperty(Property.DATA) != null){
			e.setProperty(Property.SUB_TYPE, source.<Enemy>getProperty(Property.DATA).getElement());
		}
		else{
			e.setProperty(Property.SUB_TYPE, Element.NEUTRAL);
		}
		
		e.setPosition(source.getPosition().add(source.getWidth()/2, source.getHeight()));
		e.setCollidable(true);
		
		Arc tA1 = new Arc(0, 0, 25, 35, 0, 180);
		tA1.setFill(null);
		tA1.setStroke(Color.BLACK);
		tA1.setStrokeWidth(2);
		tA1.setType(ArcType.OPEN);
		Arc tA2 = new Arc(0, 0, 35, 45, 0, 180);
		tA2.setFill(null);
		tA2.setStroke(Color.BLACK);
		tA2.setStrokeWidth(2);
		tA2.setType(ArcType.OPEN);
		Arc tA3 = new Arc(0, 0, 55, 65, 0, 180);
		tA3.setFill(null);
		tA3.setStroke(Color.BLACK);
		tA3.setStrokeWidth(2);
		tA3.setType(ArcType.OPEN);
		Group gr = new Group(tA1);
		e.setGraphics(gr);
		e.addFXGLEventHandler(Event.DEATH, event -> {
			consApp.getSceneManager().removeEntity(event.getTarget());
		});
		
		consApp.getTimerManager().runOnceAfter(() -> {
			gr.getChildren().add(tA2);
			//e.setPosition(e.getPosition().add(5, 0));
		}, Config.ENEMY_SOUND_GROWTH_DELAY);
		consApp.getTimerManager().runOnceAfter(() -> {
			gr.getChildren().add(tA3);
			gr.getChildren().remove(tA1);
			//e.setPosition(e.getPosition().add(5, 0));
		}, Config.ENEMY_SOUND_GROWTH_DELAY.multiply(2));
		consApp.getTimerManager().runOnceAfter(() -> {
			e.fireFXGLEvent(new FXGLEvent(Event.DEATH));
		}, Config.ENEMY_SOUND_GROWTH_DELAY.multiply(3));
				
		consApp.soundManager.playSFX(FileNames.FLUTE_TUNE);
		
		consApp.getSceneManager().addEntities(e);
	}
	
	public void enemyStab(Entity source){
		Entity e = new Entity(Type.ENEMY_PROJECTILE);
		if(source.<Enemy>getProperty(Property.DATA) != null){
			e.setProperty(Property.SUB_TYPE, source.<Enemy>getProperty(Property.DATA).getElement());
		}
		else{
			e.setProperty(Property.SUB_TYPE, Element.METAL);
		}
		
		e.setPosition(source.getPosition().add((source.getWidth() / 2), 0));
		e.setCollidable(true);
		e.addControl(new PhysicsControl(consApp.physics));
		e.addFXGLEventHandler(Event.COLLIDED_PLATFORM, event -> {
			consApp.getSceneManager().removeEntity(e);
		});
		e.addFXGLEventHandler(Event.DEATH, event -> {
			consApp.getSceneManager().removeEntity(event.getTarget());
		});

		
		e.setVisible(false);
		e.setGraphics(new Rectangle(0, 0, 20, 15));
		e.setProperty(Property.ENABLE_GRAVITY, false);
		e.addControl(new StabControl(source));

		consApp.getSceneManager().addEntities(e);
	}
	
	public void enemyStabDown(Entity source) {
		Entity e = new Entity(Type.ENEMY_PROJECTILE);
		if(source.<Enemy>getProperty(Property.DATA) != null){
			e.setProperty(Property.SUB_TYPE, source.<Enemy>getProperty(Property.DATA).getElement());
		}
		else{
			e.setProperty(Property.SUB_TYPE, Element.NEUTRAL);
		}
		
		e.setPosition(source.getPosition().add(source.getWidth() - e.getWidth()/2, source.getHeight()));
		e.setCollidable(true);
		e.addControl(new PhysicsControl(consApp.physics));
		e.addFXGLEventHandler(Event.COLLIDED_PLATFORM, event -> {
		});
		e.addFXGLEventHandler(Event.DEATH, event -> {
			consApp.getSceneManager().removeEntity(event.getTarget());
		});

		
		e.setVisible(false);
		e.setGraphics(new Rectangle(0, 0, 15, 30));
		e.setProperty(Property.ENABLE_GRAVITY, false);
		e.addControl(new StabDownControl(source));

		consApp.getSceneManager().addEntities(e);
	}
	
	public void enemyCreatePillar(Entity source, Entity target, double startY) {
		Entity e = new Entity(Type.ENEMY_PROJECTILE);
		if(source.<Enemy>getProperty(Property.DATA) != null){
			e.setProperty(Property.SUB_TYPE, source.<Enemy>getProperty(Property.DATA).getElement());
		}
		else{
			e.setProperty(Property.SUB_TYPE, Element.NEUTRAL);
		}
		
		e.setPosition(new Point2D(target.getPosition().getX(), startY - 5));
		e.setCollidable(false);
		e.addControl(new PhysicsControl(consApp.physics));
		e.addFXGLEventHandler(Event.COLLIDED_PLATFORM, event -> {
		});
		e.addFXGLEventHandler(Event.DEATH, event -> {
			consApp.getSceneManager().removeEntity(event.getTarget());
		});

		
		e.setVisible(true);
		Rectangle g = new Rectangle(0, 0, 20, 5);
		g.setFill(Color.GOLDENROD);
		e.setGraphics(g);
		e.setProperty(Property.ENABLE_GRAVITY, false);
		e.addControl(new PillarControl(consApp.getTexture(FileNames.S_N_SAND_BLOCK)));

		consApp.getSceneManager().addEntities(e);
	}

	public void changePower(int posi) {
		int ind = consApp.playerData.getPowers().indexOf(consApp.playerData.getCurrentPower());
		if(posi > 0){
			ind++;
		}
		else{
			ind--;
		}

		if (ind >= consApp.playerData.getPowers().size()) {
			ind = 0;
		}
		else if(ind < 0){
			ind = consApp.playerData.getPowers().size() - 1;
		}
		consApp.playerData.setCurrentPower(consApp.playerData.getPowers().get(ind));
	}

	public void changePower(Element e) {
		consApp.playerData.setCurrentPower(e);
	}

	public HashMap<Actions, KeyCode> getCurrentKeys(){
		return currentKeys;
	}

	public HashMap<Actions, KeyCode> getDefaultKeys(){
		return defaultKeys;
	}

	private List<Line> createBolt(Point2D src, Point2D dst, float thickness) {
		ArrayList<Line> results = new ArrayList<Line>();

		Point2D tangent = dst.subtract(src);
		Point2D normal = new Point2D(tangent.getY(), -tangent.getX()).normalize();

		double length = tangent.magnitude();

		ArrayList<Float> positions = new ArrayList<Float>();
		positions.add(0.0f);

		for (int i = 0; i < length / 4; i++)
			positions.add((float) Math.random());

		Collections.sort(positions);

		float sway = 20;
		float jaggedness = 1 / sway;

		Point2D prevPoint = src;
		float prevDisplacement = 0;
		Color c = Color.rgb(245, 230, 250);
		for (int i = 1; i < positions.size(); i++) {
			float pos = positions.get(i);

			// used to prevent sharp angles by ensuring very close positions
			// also have small perpendicular variation.
			double scale = (length * jaggedness) * (pos - positions.get(i - 1));

			// defines an envelope. Points near the middle of the bolt can be
			// further from the central line.
			float envelope = pos > 0.95f ? 20 * (1 - pos) : 1;

			float displacement = (float) (sway * (Math.random() * 2 - 1));
			displacement -= (displacement - prevDisplacement) * (1 - scale);
			displacement *= envelope;

			Point2D point = src.add(tangent.multiply(pos)).add(normal.multiply(displacement));

			Line line = new Line(prevPoint.getX(), prevPoint.getY(), point.getX(), point.getY());
			line.setStrokeWidth(thickness);
			line.setStroke(c);
			results.add(line);
			prevPoint = point;
			prevDisplacement = displacement;
		}

		Line line = new Line(prevPoint.getX(), prevPoint.getY(), dst.getX(), dst.getY());
		line.setStrokeWidth(thickness);
		line.setStroke(c);
		results.add(line);

		return results;
	}

}