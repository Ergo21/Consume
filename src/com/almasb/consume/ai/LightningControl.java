package com.almasb.consume.ai;

import javafx.animation.FadeTransition;
import javafx.scene.Group;
import javafx.util.Duration;

import com.almasb.consume.Config;
import com.almasb.consume.Event;
import com.almasb.fxgl.entity.AbstractControl;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;

public class LightningControl extends AbstractControl {

    private long created;
    private boolean nPlayed;
    private Group ligGraphics;

    public LightningControl(Group gr) {
    	created = 0;
    	nPlayed = true;
    	ligGraphics = gr;
    }

    @Override
    protected void initEntity(Entity entity) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onUpdate(Entity entity, long now) {
        //PhysicsControl control = entity.getControl(PhysicsControl.class);
        if(created == 0){
        	created = now;
        }
        
        if(now - created > Config.LIGHTNING_DECAY && nPlayed){
        	nPlayed = false;
        	FadeTransition ft = new FadeTransition(Duration.seconds(0.5), ligGraphics);
            ft.setFromValue(1);
            ft.setToValue(0);
            ft.setOnFinished(evt -> {
                entity.fireFXGLEvent(new FXGLEvent(Event.DEATH));
            });
            ft.play();
        }
        else if(now - created > Config.LIGHTNING_DELAY){
        	entity.setVisible(true);
        	entity.setCollidable(true);
        }
       
    }
    
}
