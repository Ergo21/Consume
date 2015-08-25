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
import com.almasb.consume.Types.Powerup;
import com.almasb.consume.Types.Property;
import com.almasb.consume.Types.Type;
import com.almasb.consume.ai.ChargeControl;
import com.almasb.consume.ai.PhysicsControl;
import com.almasb.consume.ai.SimpleJumpControl;
import com.almasb.consume.ai.SimpleMoveControl;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;
import com.almasb.fxgl.entity.FXGLEventHandler;

public class EntitySpawner{
	private ConsumeApp consApp;
	
	public EntitySpawner(ConsumeApp a){
		consApp = a;
	}
	

	public Entity spawnEnemy(Point2D spawnPoint){
		Entity enemy = new Entity(Type.ENEMY);
        Rectangle rect = new Rectangle(30, 30);
        rect.setFill(Color.RED);

        enemy.setGraphics(rect);
        enemy.setCollidable(true);
        enemy.setProperty(Property.DATA, new Enemy(consApp.assets.getText("enemies/enemy_FireElemental.txt")));
        enemy.setProperty("physics", consApp.physics);
        enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
        //testEnemy2.addControl(new AimedProjectileControl(player));
        enemy.addControl(new SimpleMoveControl(consApp.player));
        enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
        enemy.addFXGLEventHandler(Event.ENEMY_SAW_PLAYER, event -> consApp.consController.aimedProjectile(enemy, consApp.player));
        
        return enemy;
	}

	
	public Entity spawnDog(Point2D spawnPoint){
		Entity enemy = new Entity(Type.ENEMY);

        Rectangle rect = new Rectangle(30, 30);
        rect.setFill(Color.RED);

        enemy.setGraphics(rect);
        enemy.setCollidable(true);
        enemy.setProperty(Property.DATA, new Enemy(consApp.assets.getText("enemies/enemy_FireElemental.txt")));
        enemy.setProperty("physics", consApp.physics);
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
            }, Config.ENEMY_CHARGE_DELAY);
        });
        
        return enemy;
	}
	
	public Entity spawnScarab(Point2D spawnPoint){
		Entity enemy = new Entity(Type.ENEMY);
        Rectangle rect = new Rectangle(30, 30);
        rect.setFill(Color.RED);
        enemy.setGraphics(rect);
        enemy.setCollidable(true);
        enemy.setProperty(Property.DATA, new Enemy(consApp.assets.getText("enemies/enemy_FireElemental.txt")));
        enemy.setProperty("physics", consApp.physics);
        enemy.setPosition(spawnPoint.getX(), spawnPoint.getY());
        //testEnemy2.addControl(new AimedProjectileControl(player));
        enemy.addControl(new PhysicsControl(consApp.physics));
        enemy.addControl(new SimpleJumpControl(consApp.player, Speed.ENEMY_JUMP));
        enemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);

        return enemy;
	}
	
	private void onEnemyDeath(FXGLEvent event) {
        Entity enemy = event.getTarget();
        consApp.getSceneManager().removeEntity(enemy);

        // chance based drop logic
        if (consApp.getRandom().nextInt(100) <= 33) {    // check if dropping
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
            }
            else {
                drops.remove(Powerup.RESTORE_MANA_12);
                drops.remove(Powerup.RESTORE_MANA_25);
                drops.remove(Powerup.RESTORE_MANA_50);
            }

            Collections.shuffle(drops);

            Entity e = new Entity(Type.POWERUP);
            e.setCollidable(true);
            e.setPosition(enemy.getTranslateX(), enemy.getTranslateY());
            e.setProperty(Property.SUB_TYPE, drops.get(0));
            Rectangle r = new Rectangle(30, 30);
            r.setFill(Color.PINK);
            e.setGraphics(r);
            e.addControl(new PhysicsControl(consApp.physics));
            
            e.addFXGLEventHandler(Event.DEATH, new FXGLEventHandler(){
				@Override
				public void handle(FXGLEvent event) {
					consApp.getSceneManager().removeEntity(e);
				}    	
            });

            consApp.getSceneManager().addEntities(e);
        }
    }
}