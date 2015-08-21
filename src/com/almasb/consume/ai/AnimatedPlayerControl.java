package com.almasb.consume.ai;

import java.util.HashMap;
import java.util.Map;

import com.almasb.fxgl.asset.Texture;
import com.almasb.fxgl.entity.Control;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.time.TimerManager;

import javafx.geometry.Rectangle2D;

public class AnimatedPlayerControl implements Control {

    private enum AnimationChannel {
        IDLER(new Rectangle2D(150, 0, 90, 30), 3),
        IDLEL(new Rectangle2D(60, 0, 90, 30), 3),
        MOVER(new Rectangle2D(180, 30, 120, 30), 4),
        MOVEL(new Rectangle2D(0, 30, 120, 30), 4);

        final int frames;
        final Rectangle2D area;

        AnimationChannel(Rectangle2D area, int frames) {
            this.area = area;
            this.frames = frames;
        }
    }

    private AnimationChannel current;

    private Map<AnimationChannel, Texture> animationTextures = new HashMap<>();

    public AnimatedPlayerControl(Texture spritesheet) {
    	for (AnimationChannel channel : AnimationChannel.values()) {
    	    animationTextures.put(channel,
    	            spritesheet.subTexture(channel.area)
    	            .toStaticAnimatedTexture(channel.frames, TimerManager.SECOND));
    	}
    }

    @Override
    public void onUpdate(Entity entity, long now) {
        PhysicsControl pc = entity.getControl(PhysicsControl.class);
        if (pc != null){
        	if (pc.getVelocity().getX() == 0){
        		if (current != AnimationChannel.IDLER && entity.<Boolean>getProperty("facingRight")){
        			entity.setGraphics(animationTextures.get(AnimationChannel.IDLER));
        			current = AnimationChannel.IDLER;
        		}
        		else if (current != AnimationChannel.IDLEL && !entity.<Boolean>getProperty("facingRight")){
        		    entity.setGraphics(animationTextures.get(AnimationChannel.IDLEL));
        			current = AnimationChannel.IDLEL;
        		}
        	}
        	else {
        		if (current != AnimationChannel.MOVER && entity.<Boolean>getProperty("facingRight")){
        		    entity.setGraphics(animationTextures.get(AnimationChannel.MOVER));
        			current = AnimationChannel.MOVER;
        		}
        		else if (current != AnimationChannel.MOVEL && !entity.<Boolean>getProperty("facingRight")){
        		    entity.setGraphics(animationTextures.get(AnimationChannel.MOVEL));
        			current = AnimationChannel.MOVEL;
        		}
        	}
        }
    }
}
