package com.ergo21.consume;

import java.sql.Time;
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
import com.almasb.consume.ai.AimedProjectileControl;
import com.almasb.consume.ai.BulletProjectileControl;
import com.almasb.consume.ai.FireballProjectileControl;
import com.almasb.consume.ai.IngestControl;
import com.almasb.consume.ai.LightningControl;
import com.almasb.consume.ai.PhysicsControl;
import com.almasb.consume.ai.SandProjectileControl;
import com.almasb.consume.ai.SpearProjectileControl;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.event.UserAction;

import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
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
				if (consApp.gScene.isVisible()) {
					consApp.gScene.updateScript();
				}
				else if(consApp.player != null && consApp.player.getProperty("inDoor") != null && (boolean) consApp.player.getProperty("inDoor")){
					consApp.playerData.setCurrentLevel(consApp.playerData.getCurrentLevel() + 1);
					consApp.changeLevel();
				}
				else{	
					consApp.player.setProperty("eating", true);
					consApp.player.getControl(PhysicsControl.class).moveX(0);
					consApp.getTimerManager().runOnceAfter(() ->{
						consApp.player.setProperty("eating", false);
						consApp.player.setProperty("eaten", true);
					}, Duration.seconds(1));
					consApp.getTimerManager().runOnceAfter(() ->{
						consApp.player.setProperty("eaten", false);
					}, Duration.seconds(1.1));
				}
			}
		});
		allActions.put(Actions.LEFT, new UserAction("Left") {
			@Override
			protected void onAction() {
				if (consApp.player != null && consApp.player.getProperty("stunned") != null && !(boolean) consApp.player.getProperty("stunned") &&
												consApp.player.getProperty("eating") != null && !(boolean) consApp.player.getProperty("eating")) {
					consApp.player.getControl(PhysicsControl.class).moveX(-Speed.PLAYER_MOVE);
					consApp.player.setProperty("facingRight", false);
				}
			}

			@Override
			protected void onActionEnd() {
				if (consApp.player != null && consApp.player.getProperty("stunned") != null && !(boolean) consApp.player.getProperty("stunned") &&
						consApp.player.getProperty("eating") != null && !(boolean) consApp.player.getProperty("eating")) {
					consApp.player.getControl(PhysicsControl.class).moveX(0);
				}
			}
		});
		allActions.put(Actions.RIGHT, new UserAction("Move Right") {
			@Override
			protected void onAction() {
				if (consApp.player != null && consApp.player.getProperty("stunned") != null &&  !(boolean) consApp.player.getProperty("stunned") &&
						consApp.player.getProperty("eating") != null && !(boolean) consApp.player.getProperty("eating")) {
					consApp.player.getControl(PhysicsControl.class).moveX(Speed.PLAYER_MOVE);
					consApp.player.setProperty("facingRight", true);
				}
			}

			@Override
			protected void onActionEnd() {
				if (consApp.player != null && consApp.player.getProperty("stunned") != null && !(boolean) consApp.player.getProperty("stunned") &&
						consApp.player.getProperty("eating") != null && !(boolean) consApp.player.getProperty("eating")) {
					consApp.player.getControl(PhysicsControl.class).moveX(0);
				}
			}
		});
		allActions.put(Actions.UP, new UserAction("Up") {
			@Override
			protected void onAction() {
				if (consApp.player != null && consApp.player.getProperty("stunned") != null && !(boolean) consApp.player.getProperty("stunned") &&
						consApp.player.getProperty("eating") != null && !(boolean) consApp.player.getProperty("eating")) {
					if (consApp.player.<Boolean> getProperty("climbing"))
						consApp.player.getControl(PhysicsControl.class).climb(-5);
				}
			}
		});
		allActions.put(Actions.JUMP, new UserAction("Jump / Climb Up") {
			@Override
			protected void onAction() {
				if (consApp.player != null && consApp.player.getProperty("stunned") != null && !(boolean) consApp.player.getProperty("stunned") &&
						consApp.player.getProperty("eating") != null && !(boolean) consApp.player.getProperty("eating")) {
					if (consApp.player.<Boolean> getProperty("climbing"))
						consApp.player.getControl(PhysicsControl.class).climb(-5);
					else
						consApp.player.getControl(PhysicsControl.class).jump();
				}
			}
		});
		allActions.put(Actions.DOWN, new UserAction("Down") {
			@Override
			protected void onAction() {
				if (consApp.player != null && consApp.player.getProperty("stunned") != null && !(boolean) consApp.player.getProperty("stunned") &&
						consApp.player.getProperty("eating") != null && !(boolean) consApp.player.getProperty("eating")) {
					if (consApp.player.<Boolean> getProperty("climbing")) {
						consApp.player.getControl(PhysicsControl.class).climb(5);
					}
				}
			}
		});
		allActions.put(Actions.SHOOT, new UserAction("Shoot") {
			@Override
			protected void onActionBegin() {
				if (consApp.player != null && consApp.player.getProperty("stunned") != null && !(boolean) consApp.player.getProperty("stunned") &&
						consApp.player.getProperty("eating") != null && !(boolean) consApp.player.getProperty("eating")) {
					shootProjectile();
				}
			}
		});
		allActions.put(Actions.CHPOWP, new UserAction("Change Power +") {
			@Override
			protected void onActionBegin() {
				if (consApp.player != null && consApp.player.getProperty("stunned") != null && !(boolean) consApp.player.getProperty("stunned") &&
						consApp.player.getProperty("eating") != null && !(boolean) consApp.player.getProperty("eating")) {
					changePower(1);
				}
			}
		});
		allActions.put(Actions.CHPOWN, new UserAction("Change Power -") {
			@Override
			protected void onActionBegin() {
				if (consApp.player != null && consApp.player.getProperty("stunned") != null && !(boolean) consApp.player.getProperty("stunned") &&
						consApp.player.getProperty("eating") != null && !(boolean) consApp.player.getProperty("eating")) {
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

	private Entity spear;
	private boolean fired;

	public void shootProjectile() {
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

			if (platform.getProperty(Property.SUB_TYPE) == Platform.DESTRUCTIBLE) {
				consApp.destroyBlock(platform);
			}
		});
		e.addFXGLEventHandler(Event.DEATH, event -> {
			consApp.getSceneManager().removeEntity(event.getTarget());
		});

		switch (element) {
		case NEUTRAL: {
			e.addControl(new SpearProjectileControl(consApp.player));
			if (spear == null || !consApp.getSceneManager().getEntities().contains(spear)) {
				spear = e;
			} else {
				return;
			}
			break;
		}
		case NEUTRAL2: {
			e.setVisible(true);
			e.setCollidable(true);
			e.setGraphics(new Rectangle(0, 0, consApp.player.getWidth() / 2, consApp.player.getHeight()));
			e.setProperty(Property.ENABLE_GRAVITY, false);
			e.addControl(new IngestControl(consApp.player));
			break;
		}
		case FIRE: {
			e.addControl(new FireballProjectileControl(consApp.player));
			e.setProperty(Property.ENABLE_GRAVITY, false);
			if (consApp.playerData.getCurrentMana() >= Config.FIREBALL_COST) {
				consApp.playerData.setCurrentMana(consApp.playerData.getCurrentMana() - Config.FIREBALL_COST);
			} else {
				return;
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

				if (platform.getProperty(Property.SUB_TYPE) == Platform.DESTRUCTIBLE) {
					consApp.destroyBlock(platform);
				}
			});
			e2.addFXGLEventHandler(Event.DEATH, event -> {
				consApp.getSceneManager().removeEntity(event.getTarget());
			});
			e2.addControl(new SandProjectileControl(consApp.player, true));
			e2.setProperty(Property.ENABLE_GRAVITY, false);

			consApp.getSceneManager().addEntities(e2);
			break;
		}
		case METAL: {
			if (consApp.playerData.getCurrentMana() >= Config.BULLET_COST) {
				consApp.playerData.setCurrentMana(consApp.playerData.getCurrentMana() - Config.BULLET_COST);
			} else {
				return;
			}
			if (fired) {
				return;
			}
			consApp.getTimerManager().runOnceAfter(() -> fired = false, Duration.seconds(3));
			fired = true;
			e.addControl(new BulletProjectileControl(consApp.player));
			e.setProperty(Property.ENABLE_GRAVITY, false);
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
			e.addControl(new PhysicsControl(consApp.physics));
			LightningControl lc = new LightningControl(g);
			e.addControl(lc);
			Point2D p = consApp.player.getPosition();
			if ((boolean) consApp.player.getProperty("facingRight")) {
				p = p.add(consApp.player.getWidth() + 30, 0);
			} else {
				p = p.add(-e.getWidth() - 30, 0);
			}
			p = p.add(0, consApp.player.getHeight());
			e.setPosition(0, 0);

			g.getChildren().addAll(createBolt(new Point2D(p.getX(), -200), p, 5));
			e.setGraphics(g);

			DropShadow shadow = new DropShadow(20, Color.PURPLE);
			shadow.setInput(new Glow(0.7));
			g.setEffect(shadow);
			e.setProperty(Property.ENABLE_GRAVITY, false);
			break;
		}
		case DEATH: {
			break;
		}
		case CONSUME: {
			// e.setVisible(true);
			e.setCollidable(true);
			e.setGraphics(new Rectangle(0, 0, consApp.player.getWidth() / 2, consApp.player.getHeight()));
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

		float sway = 80;
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