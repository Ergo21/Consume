package com.almasb.consume.ai;

import java.util.HashMap;

import com.almasb.consume.Types.Element;
import com.almasb.fxgl.asset.Texture;
import com.almasb.fxgl.entity.Control;
import com.almasb.fxgl.entity.Entity;
import com.ergo21.consume.Player;

import javafx.geometry.Rectangle2D;
import javafx.util.Duration;

public class AnimatedPlayerControl implements Control {

	private enum AnimationChannel {
		IDLE(new Rectangle2D	(0,	    0,  600, 300), 2), 
		MOVE(new Rectangle2D	(0,   300, 1200, 300), 4),
		MATK(new Rectangle2D	(0,   600, 1200, 300), 4),
		HIT (new Rectangle2D	(0,   900,  300, 300), 1),	
		ATK( new Rectangle2D	(300, 900,  300, 300), 1),
		JUMP(new Rectangle2D	(600, 900,  300, 300), 1),
		JATK(new Rectangle2D	(900, 900,  300, 300), 1),
		
		HOLD(new Rectangle2D	(0, 	0,  300, 300), 1),
		CLIMB(new Rectangle2D	(0, 	0,  600, 300), 2),
		EAT (new Rectangle2D	(0,   300, 1500, 300), 5);
		
		/*IDLER(new Rectangle2D(150, 0, 90, 30), 2), IDLEL(new Rectangle2D(60, 0, 90, 30),
				3), MOVER(new Rectangle2D(180, 30, 120, 30), 4), MOVEL(new Rectangle2D(0, 30, 120, 30), 4);*/

		final int frames;
		final Rectangle2D area;

		AnimationChannel(Rectangle2D area, int frames) {
			this.area = area;
			this.frames = frames;
		}
	}


	private HashMap<Element, HashMap<AnimationChannel, Texture>> elementAnimations = new HashMap<>();
	private HashMap<AnimationChannel, Texture> sharedAnimations = new HashMap<>();
	private Entity player;
	private Player playerData;
	

	public AnimatedPlayerControl(Entity p, Player pD, HashMap<Element, Texture> hashMap, Texture sharedSS) {
		player = p;
		playerData = pD;
		
		for(Element el : hashMap.keySet()){
			HashMap<AnimationChannel, Texture> ani = new HashMap<>();
			for(AnimationChannel ac : AnimationChannel.values()){
				switch(ac){
					case IDLE:{
						ani.put(ac, 
								hashMap.get(el).subTexture(ac.area).
									toStaticAnimatedTexture(ac.frames, Duration.seconds(1)));
						break;
					}
					case MOVE:{
						ani.put(ac, 
								hashMap.get(el).subTexture(ac.area).
									toStaticAnimatedTexture(ac.frames, Duration.seconds(1)));
						break;
					}
					case MATK:{
						ani.put(ac, 
								hashMap.get(el).subTexture(ac.area).
									toStaticAnimatedTexture(ac.frames, Duration.seconds(1)));
						break;
					}
					case HIT:{
						ani.put(ac, 
								hashMap.get(el).subTexture(ac.area).
									toStaticAnimatedTexture(ac.frames, Duration.seconds(1)));
						break;
					}
					case ATK:{
						ani.put(ac, 
								hashMap.get(el).subTexture(ac.area).
									toStaticAnimatedTexture(ac.frames, Duration.seconds(1)));
						break;
					}
					case JUMP:{
						ani.put(ac, 
								hashMap.get(el).subTexture(ac.area).
									toStaticAnimatedTexture(ac.frames, Duration.seconds(1)));
						break;
					}
					case JATK:{
						ani.put(ac, 
								hashMap.get(el).subTexture(ac.area).
									toStaticAnimatedTexture(ac.frames, Duration.seconds(1)));
						break;
					}
					default:
					break;
				}
			}
			elementAnimations.put(el, ani);
		}
		
		sharedAnimations.put(AnimationChannel.HOLD, 
				sharedSS.subTexture(AnimationChannel.HOLD.area).
					toStaticAnimatedTexture(AnimationChannel.HOLD.frames, Duration.seconds(1)));
		sharedAnimations.put(AnimationChannel.CLIMB, 
				sharedSS.subTexture(AnimationChannel.CLIMB.area).
					toStaticAnimatedTexture(AnimationChannel.CLIMB.frames, Duration.seconds(0.5)));
		sharedAnimations.put(AnimationChannel.EAT, 
				sharedSS.subTexture(AnimationChannel.EAT.area));
	}

	@Override
	public void onUpdate(Entity entity, long now) {
		entity.setGraphics(getCurrentImage());
	}

	private Texture eatTex = null;
	
	private Texture getCurrentImage() {
		Texture t = null;
		
		if(!player.<Boolean> getProperty("eating")){
			eatTex = null;
		}
		
		PhysicsControl pc = player.getControl(PhysicsControl.class);
		if (pc != null) {
			if(player.<Boolean> getProperty("stunned")){
				t = elementAnimations.get(playerData.getElement()).get(AnimationChannel.HIT);
				t.setPreserveRatio(true);
				t.setTranslateX(-27);
				t.setTranslateY(-45);
				t.setFitHeight(75);	
			}
			else if(player.<Boolean> getProperty("eating")){
				if(eatTex == null){
					eatTex = sharedAnimations.get(AnimationChannel.EAT).toStaticAnimatedTexture(AnimationChannel.EAT.frames, Duration.seconds(1));
				}
				t = eatTex;
				t.setPreserveRatio(true);
				t.setTranslateX(-27);
				t.setTranslateY(-45);
				t.setFitHeight(75);	
			}
			else if(player.<Boolean> getProperty("climb")){
				if(player.<Boolean> getProperty("climbing")){
					t = sharedAnimations.get(AnimationChannel.CLIMB);
				}
				else{
					t = sharedAnimations.get(AnimationChannel.HOLD);
				}
				t.setPreserveRatio(true);
				t.setTranslateX(-27);
				t.setTranslateY(-45);
				t.setFitHeight(75);	
			}
			else if(player.<Boolean> getProperty("jumping")){
				if(player.<Boolean> getProperty("attacking")){
					t = elementAnimations.get(playerData.getElement()).get(AnimationChannel.JATK);
				}
				else{
					t = elementAnimations.get(playerData.getElement()).get(AnimationChannel.JUMP);
				}
				t.setPreserveRatio(true);
				t.setTranslateX(-28);
				t.setTranslateY(-48);
				t.setFitHeight(78);
			}
			else if (pc.getVelocity().getX() != 0) {
				if(player.<Boolean> getProperty("attacking")){
					t = elementAnimations.get(playerData.getElement()).get(AnimationChannel.MATK);
				}
				else{
					t = elementAnimations.get(playerData.getElement()).get(AnimationChannel.MOVE);
				}
				t.setPreserveRatio(true);
				t.setTranslateX(-27);
				t.setTranslateY(-45);
				t.setFitHeight(75);	
			} 
			else if(player.<Boolean> getProperty("attacking")){
				t = elementAnimations.get(playerData.getElement()).get(AnimationChannel.ATK);
				t.setPreserveRatio(true);
				t.setTranslateX(-27);
				t.setTranslateY(-45);
				t.setFitHeight(75);	
			}
			else {
				t = elementAnimations.get(playerData.getElement()).get(AnimationChannel.IDLE);
				t.setPreserveRatio(true);
				t.setTranslateX(-27);
				t.setTranslateY(-45);
				t.setFitHeight(75);
			}
		}
		
		if((player.<Boolean> getProperty("facingRight") && t.getScaleX() < 0) ||
				(!player.<Boolean> getProperty("facingRight") && t.getScaleX() > 0)){
			t.setScaleX(t.getScaleX()*-1);
		}
		
		return t;
	}
}
