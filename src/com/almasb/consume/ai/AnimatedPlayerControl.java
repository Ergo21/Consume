package com.almasb.consume.ai;

import java.util.HashMap;

import com.almasb.consume.Types.Element;
import com.almasb.fxgl.asset.Texture;
import com.almasb.fxgl.entity.Control;
import com.almasb.fxgl.entity.Entity;
import com.ergo21.consume.Player;

import javafx.geometry.Rectangle2D;
import javafx.util.Duration;
import javafx.util.Pair;

public class AnimatedPlayerControl implements Control {

	private enum AnimationChannel {
		IDLE(new Rectangle2D	(  0,   0,  600, 300), 2),
		HIT (new Rectangle2D	(600,   0,  300, 300), 1),
		MOVE(new Rectangle2D	(  0, 300, 1200, 300), 4),
		MATK(new Rectangle2D	(  0, 600,  600, 300), 2),
		ATK( new Rectangle2D	(  0, 900,  600, 300), 2),
		JUMP(new Rectangle2D	(  0,1200,  300, 300), 1),
		JATK(new Rectangle2D	(300,1200,  300, 300), 1),
		
		EARTH_ATK(new Rectangle2D	(  0, 900,  900, 300), 3),
		EARTH_JATK(new Rectangle2D	(300,1200,  600, 300), 2),
		
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
								hashMap.get(el).subTexture(ac.area));
						break;
					}
					case HIT:{
						ani.put(ac, 
								hashMap.get(el).subTexture(ac.area).
									toStaticAnimatedTexture(ac.frames, Duration.seconds(1)));
						break;
					}
					case ATK:{
						if(el == Element.EARTH){
							ani.put(ac, 
									hashMap.get(el).subTexture(AnimationChannel.EARTH_ATK.area));
						}
						else{
							ani.put(ac, 
									hashMap.get(el).subTexture(ac.area));
						}
						break;
					}
					case JUMP:{
						ani.put(ac, 
								hashMap.get(el).subTexture(ac.area).
									toStaticAnimatedTexture(ac.frames, Duration.seconds(1)));
						break;
					}
					case JATK:{
						if(el == Element.EARTH){
							ani.put(ac, 
									hashMap.get(el).subTexture(AnimationChannel.EARTH_JATK.area));
						}
						else{
							ani.put(ac, 
									hashMap.get(el).subTexture(ac.area).
										toStaticAnimatedTexture(ac.frames, Duration.seconds(1)));
						}
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
		if(player != null && !player.isCollidable()){
			entity.setVisible(!entity.isVisible());
		}
		else if(player != null && player.isCollidable() && !entity.isVisible()){
			entity.setVisible(true);
		}
		
		entity.setGraphics(getCurrentImage());
	}

	private Pair<Element, AnimationChannel> curTexVal = null;
	private Texture curTex;
	
	private Texture getCurrentImage() {
		Texture t = null;
		
		PhysicsControl pc = player.getControl(PhysicsControl.class);
		if (pc != null) {
			if(player.<Boolean> getProperty("stunned")){
				t = getTexture(AnimationChannel.HIT);
				t.setPreserveRatio(true);
				t.setTranslateX(-30);
				t.setTranslateY(-40);
				t.setFitHeight(75);	
			}
			else if(player.<Boolean> getProperty("eating")){
				t = getTexture(AnimationChannel.EAT);
				t.setPreserveRatio(true);
				t.setTranslateX(-30);
				t.setTranslateY(-40);
				t.setFitHeight(75);	
			}
			else if(player.<Boolean> getProperty("climb")){
				if(player.<Boolean> getProperty("climbing")){
					t = getTexture(AnimationChannel.CLIMB);
				}
				else{
					t = getTexture(AnimationChannel.HOLD);
				}
				t.setPreserveRatio(true);
				t.setTranslateX(-30);
				t.setTranslateY(-40);
				t.setFitHeight(75);	
			}
			else if(player.<Boolean> getProperty("jumping")){
				if(player.<Boolean> getProperty("attacking")){
					t = getTexture(AnimationChannel.JATK);
				}
				else{
					t = getTexture(AnimationChannel.JUMP);
				}
				t.setPreserveRatio(true);
				t.setTranslateX(-31);
				t.setTranslateY(-43);
				t.setFitHeight(78);
			}
			else if (pc.getVelocity().getX() != 0) {
				if(player.<Boolean> getProperty("attacking")){
					t = getTexture(AnimationChannel.MATK);
				}
				else{
					t = getTexture(AnimationChannel.MOVE);
				}
				t.setPreserveRatio(true);
				t.setTranslateX(-30);
				t.setTranslateY(-40);
				t.setFitHeight(75);	
			} 
			else if(player.<Boolean> getProperty("attacking")){
				t = getTexture(AnimationChannel.ATK);
				t.setPreserveRatio(true);
				t.setTranslateX(-30);
				t.setTranslateY(-40);
				t.setFitHeight(75);	
			}
			else {
				t = getTexture(AnimationChannel.IDLE);
				t.setPreserveRatio(true);
				t.setTranslateX(-30);
				t.setTranslateY(-40);
				t.setFitHeight(75);
			}
		}
		
		if((player.<Boolean> getProperty("facingRight") && t.getScaleX() < 0) ||
				(!player.<Boolean> getProperty("facingRight") && t.getScaleX() > 0)){
			t.setScaleX(t.getScaleX()*-1);
		}
		
		return t;
	}
	
	private Texture getTexture(AnimationChannel ac){
		if(curTexVal != null && curTexVal.getKey() == playerData.getElement() && curTexVal.getValue() == ac && curTex != null){
			return curTex;
		}
		
		switch(ac){
			case ATK:
				if(playerData.getElement() == Element.EARTH){
					curTex = elementAnimations.get(playerData.getElement()).get(AnimationChannel.ATK)
							.toStaticAnimatedTexture(AnimationChannel.EARTH_ATK.frames, Duration.seconds(0.5));
				}
				else{
					curTex = elementAnimations.get(playerData.getElement()).get(AnimationChannel.ATK)
							.toStaticAnimatedTexture(AnimationChannel.ATK.frames, Duration.seconds(0.5));
				}	
				break;
			case CLIMB:
			case HOLD:
				curTex = sharedAnimations.get(ac);
				break;
			case EAT:
				curTex = sharedAnimations.get(ac).toStaticAnimatedTexture(ac.frames, Duration.seconds(1));
				break;
			case HIT:
				curTex = elementAnimations.get(playerData.getElement()).get(AnimationChannel.HIT);
				break;
			case IDLE:
			case JUMP:
			case MOVE:
				curTex = elementAnimations.get(playerData.getElement()).get(ac);
				break;
			case JATK:
				if(playerData.getElement() == Element.EARTH){
					curTex = elementAnimations.get(playerData.getElement()).get(ac).
								toStaticAnimatedTexture(AnimationChannel.EARTH_JATK.frames, Duration.seconds(0.5));
				}
				else{
					curTex = elementAnimations.get(playerData.getElement()).get(ac);
				}
				break;
			case MATK:
				curTex = elementAnimations.get(playerData.getElement()).get(ac)
							.toStaticAnimatedTexture(ac.frames, Duration.seconds(0.5));
				break;
			default:
				break;
		}
		
		curTexVal = new Pair<Element, AnimationChannel>(playerData.getElement(), ac);
		
		return curTex;
	}

	public void setPlayer(Entity play) {
		player = play;
	}
}
