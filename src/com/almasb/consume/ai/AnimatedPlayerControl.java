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


	private HashMap<Element, Integer> elementLocation;
	private HashMap<AnimationChannel, Double> animationLength;
	private Texture playerSS;
	private Entity player;
	private Player playerData;
	
	private long lastMemory = 0;
	public void printMemoryUsage(String label){
		Runtime run = Runtime.getRuntime();
		
		long aMem = run.totalMemory();
		long fMem = run.freeMemory();
		long uMem = aMem - fMem;
		int  cMem = (int)uMem - (int)lastMemory;
		lastMemory = uMem;
		
		System.out.println(label + " {");
		System.out.println("Free memory: " + fMem);
		System.out.println("Allocated memory: " + aMem);
		System.out.println("Using memory: " + uMem);
		//System.out.println("Max memory: " + run.maxMemory());
		System.out.println("Used memory changed: " + cMem);
		System.out.println("}");
		System.out.println();
	}

	public AnimatedPlayerControl(Entity p, Player pD, HashMap<Element, Integer> eL, Texture pSS) {
		printMemoryUsage("Start APC");
		player = p;
		playerData = pD;
		playerSS = pSS;
		elementLocation = eL;
		animationLength = new HashMap<>();
		
		printMemoryUsage("Assigned APC");
		/*ani.put(ac, 
				hashMap.get(el).subTexture(ac.area).
				toStaticAnimatedTexture(ac.frames, Duration.seconds(1)));*/
		
		for(AnimationChannel ac : AnimationChannel.values()){
			switch(ac){
				case IDLE:
				case MOVE:
				case HIT:
				case JUMP:
				case JATK:
				
				case EAT:
				case HOLD:{
					animationLength.put(ac, 1.0);
					break;
				}
				case ATK:
				case EARTH_ATK:
				case EARTH_JATK:
				case MATK:
				case CLIMB:{
					animationLength.put(ac, 0.5);
					break;
				}
			}
		}
		
		storeAnimations();
		
		printMemoryUsage("End APC");
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
				t = getTexture(AnimationChannel.HIT, playerData.getElement());
				t.setPreserveRatio(true);
				t.setTranslateX(-30);
				t.setTranslateY(-40);
				t.setFitHeight(75);	
			}
			else if(player.<Boolean> getProperty("eating")){
				t = getTexture(AnimationChannel.EAT, playerData.getElement());
				t.setPreserveRatio(true);
				t.setTranslateX(-30);
				t.setTranslateY(-40);
				t.setFitHeight(75);	
			}
			else if(player.<Boolean> getProperty("climb")){
				if(player.<Boolean> getProperty("climbing")){
					t = getTexture(AnimationChannel.CLIMB, playerData.getElement());
				}
				else{
					t = getTexture(AnimationChannel.HOLD, playerData.getElement());
				}
				t.setPreserveRatio(true);
				t.setTranslateX(-30);
				t.setTranslateY(-40);
				t.setFitHeight(75);	
			}
			else if(player.<Boolean> getProperty("jumping")){
				if(player.<Boolean> getProperty("attacking")){
					if(playerData.getElement() == Element.EARTH){
						t = getTexture(AnimationChannel.EARTH_JATK, playerData.getElement());
					}
					else{
						t = getTexture(AnimationChannel.JATK, playerData.getElement());
					}
				}
				else{
					if(playerData.getElement() == Element.NEUTRAL && playerData.getWeaponThrown()){
						t = getTexture(AnimationChannel.JUMP, Element.CONSUME);
					}
					else{
						t = getTexture(AnimationChannel.JUMP, playerData.getElement());
					}
				}
				t.setPreserveRatio(true);
				t.setTranslateX(-31);
				t.setTranslateY(-43);
				t.setFitHeight(78);
			}
			else if (pc.getVelocity().getX() != 0) {
				if(player.<Boolean> getProperty("attacking")){
					t = getTexture(AnimationChannel.MATK, playerData.getElement());
				}
				else{
					if(playerData.getElement() == Element.NEUTRAL && playerData.getWeaponThrown()){
						t = getTexture(AnimationChannel.MOVE, Element.CONSUME);
					}
					else{
						t = getTexture(AnimationChannel.MOVE, playerData.getElement());
					}
				}
				t.setPreserveRatio(true);
				t.setTranslateX(-30);
				t.setTranslateY(-40);
				t.setFitHeight(75);	
			} 
			else if(player.<Boolean> getProperty("attacking")){
				if(playerData.getElement() == Element.EARTH){
					t = getTexture(AnimationChannel.EARTH_ATK, playerData.getElement());
				}
				else{
					t = getTexture(AnimationChannel.ATK, playerData.getElement());
				}
				t.setPreserveRatio(true);
				t.setTranslateX(-30);
				t.setTranslateY(-40);
				t.setFitHeight(75);	
			}
			else {
				if(playerData.getElement() == Element.NEUTRAL && playerData.getWeaponThrown()){
					t = getTexture(AnimationChannel.IDLE, Element.CONSUME);
				}
				else{
					t = getTexture(AnimationChannel.IDLE, playerData.getElement());
				}
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
	
	private HashMap<AnimationChannel, HashMap<Element, Texture>> storedAnim = new HashMap<>();
	
	private Texture getTexture(AnimationChannel ac, Element el){
		if(curTexVal != null && curTexVal.getKey() == el && curTexVal.getValue() == ac && curTex != null){
			return curTex;
		}
		
		curTexVal = new Pair<Element, AnimationChannel>(el, ac);
		
		Rectangle2D area;
		
		switch(ac){
			case ATK:
			case EARTH_ATK:
			case HIT:
			case JATK:
			case EARTH_JATK:
			case MATK:

			case IDLE:
			case JUMP:
			case MOVE:
				if(!storedAnim.containsKey(ac) || !storedAnim.get(ac).containsKey(el)){
					area = new Rectangle2D(ac.area.getMinX(), ac.area.getMinY() + elementLocation.get(el), 
													ac.area.getWidth(), ac.area.getHeight());
				
					curTex = playerSS.subTexture(area).toStaticAnimatedTexture(ac.frames, Duration.seconds(animationLength.get(ac)));	
				}
				else{
					curTex = storedAnim.get(ac).get(playerData.getElement());
				}
				break;
			case CLIMB:
			case HOLD:
			case EAT:
				if(!storedAnim.containsKey(ac)){
					area = new Rectangle2D(ac.area.getMinX(), (ac.area.getMinY() + 12000), 
					ac.area.getWidth(), ac.area.getHeight());

					curTex = playerSS.subTexture(area).toStaticAnimatedTexture(ac.frames, Duration.seconds(animationLength.get(ac)));	
				}
				else{
					curTex = storedAnim.get(ac).get(storedAnim.get(ac).keySet().toArray()[0]);
				}
				break;
		}
		
		
		
		if(isValidToStore(ac, playerData.getElement())){
			if(!storedAnim.containsKey(ac)){
				storedAnim.put(ac, new HashMap<Element, Texture>());
			}
			storedAnim.get(ac).put(playerData.getElement(), curTex);
		}
		
		return curTex;
	}
	
	private void storeAnimations(){
		for(AnimationChannel ac : AnimationChannel.values()){
			for(Element e : Element.values()){
				if(isValidToStore(ac, e)){
					getTexture(ac, e);
				}
			}
		}
	}

	private boolean isValidToStore(AnimationChannel ac, Element el) {
		if((ac.frames == 1 || ac == AnimationChannel.IDLE || 
				ac == AnimationChannel.CLIMB || ac == AnimationChannel.MOVE)){
			if(ac == AnimationChannel.HOLD || ac == AnimationChannel.CLIMB){
				return !storedAnim.containsKey(ac);
			}
			else{
				return (!storedAnim.containsKey(ac) || !storedAnim.get(ac).containsKey(el));
			}
		}
		return false;
	}

	public void setPlayer(Entity play) {
		player = play;
	}
}
