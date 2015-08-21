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
import com.almasb.fxgl.time.TimerManager;
import com.ergo21.consume.Enemy;

import javafx.animation.FadeTransition;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class ProjectilePlayerHandler extends CollisionHandler {

    private GameApplication app;

    public ProjectilePlayerHandler(GameApplication app) {
        super(Type.ENEMY_PROJECTILE, Type.PLAYER);
        this.app = app;
    }

    @Override
    public void onCollision(Entity projectile, Entity player) {
        Element element = projectile.getProperty(Property.SUB_TYPE);
        Enemy playerData = player.getProperty(Property.DATA);
        if(playerData == null){
        	System.out.println("playerData == null");
        }

        List<Element> resists = playerData.getResistances();
        List<Element> weaknesses = playerData.getWeaknesses();

        int damage = Config.SPEAR_DAMAGE;
        switch(element){
        	case NEUTRAL2:{
    			damage = Config.KNIFE_DAMAGE;
    			break;
    		}
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
        String modifier = "x1";

        if (resists.contains(element)) {
            damage = (int)(damage * 0.5);
            modifier = "x0.5";
        }
        else if (weaknesses.contains(element)) {
            damage *= 2;
            modifier = "x2";
        }

        Entity e = Entity.noType()
                    .setPosition(player.getTranslateX(), player.getTranslateY())
                    .setGraphics(new Text(damage + "!  " + modifier));

        app.getSceneManager().addEntities(e);

        FadeTransition ft = new FadeTransition(Duration.seconds(1.5), e);
        ft.setToValue(0);
        ft.setOnFinished(event -> {
            app.getSceneManager().removeEntity(e);
        });
        ft.play();

        playerData.takeDamage(damage);

        projectile.fireFXGLEvent(new FXGLEvent(Event.ENEMY_HIT_PLAYER));

        player.setCollidable(false);
        Entity e2 = Entity.noType().setGraphics(new Text("INVINCIBLE"));
        e2.translateXProperty().bind(player.translateXProperty());
        e2.translateYProperty().bind(player.translateYProperty().subtract(20));

        app.getSceneManager().addEntities(e2);

        app.getTimerManager().runOnceAfter(() -> {
            app.getSceneManager().removeEntity(e2);
            player.setCollidable(true);
        }, 2 * TimerManager.SECOND);

        app.getSceneManager().removeEntity(projectile);

        if (playerData.getCurrentHealth() <= 0) {
            //app.removeEntity(player);
        }
    }
}
