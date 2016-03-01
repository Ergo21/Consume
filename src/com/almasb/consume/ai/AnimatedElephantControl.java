package com.almasb.consume.ai;

import java.util.HashMap;

import com.almasb.consume.Types;
import com.almasb.consume.Types.AnimationActions;
import com.almasb.fxgl.asset.Texture;
import com.almasb.fxgl.entity.Control;
import com.almasb.fxgl.entity.Entity;

import javafx.geometry.Rectangle2D;
import javafx.util.Duration;

public class AnimatedElephantControl implements Control {

	public static class AnimationDetailsE{
		public final Rectangle2D area;
		public final int frames;
		public final double period;
		public final boolean startAnimation;
		
		public AnimationDetailsE(Rectangle2D are, int fra, double per, boolean staAnim){
			area = are;
			frames = fra;
			period = per;
			startAnimation = staAnim;
		}
	}
	
	private HashMap<Types.AnimationActions, Texture> animations = new HashMap<>();
	private HashMap<Types.AnimationActions, AnimationDetailsE> aniDetails;
	private Entity enemy;
	private Texture spriteSheet;
	private boolean prevVisible;
	private AnimationActions curAnimation = null; 

	public AnimatedElephantControl(Entity e, HashMap<Types.AnimationActions, AnimationDetailsE> hashMap, Texture sS) {
		enemy = e;
		aniDetails = hashMap;
		spriteSheet = sS;
		
		for(Types.AnimationActions aA : aniDetails.keySet()){
			AnimationDetailsE val = aniDetails.get(aA);
			if(val != null){
				if(val.startAnimation){
					animations.put(aA, spriteSheet.subTexture(val.area).
							toStaticAnimatedTexture(val.frames, Duration.seconds(val.period)));
				}
				else{
					animations.put(aA, spriteSheet.subTexture(val.area));
				}
			}
		}
		
	}

	
	private int frames = 10;
	@Override
	public void onUpdate(Entity entity, long now){
		frames++;
		if(frames >= 2){
			actualUpdate(entity, now);
			frames = 0;
		}
	}
	
	public void actualUpdate(Entity entity, long now) {
		if(enemy != null && enemy.getProperty("beenHit") != null && enemy.<Boolean>getProperty("beenHit")){
			prevVisible = true;
			entity.setVisible(!entity.isVisible());
		}
		else if(enemy != null && enemy.getProperty("beenHit") != null && !enemy.<Boolean>getProperty("beenHit") &&
				!entity.isVisible() && prevVisible){
			entity.setVisible(true);
		}
		Texture t = getCurrentImage();
		if(t != null){
			t.translateYProperty().bind(enemy.heightProperty().add(-125));
			entity.setGraphics(t);
			
			if(entity != null && enemy != null && enemy.getProperty("facingRight") != null){
				t.setTranslateX(enemy.<Boolean>getProperty("facingRight") ? -10 : -50);
				if(((enemy.<Boolean> getProperty("facingRight") && entity.getScaleX() < 0) ||
					(!enemy.<Boolean> getProperty("facingRight") && entity.getScaleX() > 0))){
					entity.setScaleX(entity.getScaleX()*-1);
				}
			}
		}
		
		
		
		if(enemy != null && enemy.getControl(SandBossControl.class) != null){
			entity.setVisible(!enemy.getControl(SandBossControl.class).isUnderground());
		}
		
	}
	
	private int curForm = -1;
	
	private Texture getCurrentImage() {
		Texture t = null;
		
		PhysicsControl ec = enemy.getControl(PhysicsControl.class);
		SandBossControl sc = enemy.getControl(SandBossControl.class);
		if (enemy != null && ec != null) {
			if (sc.getForm() != -1 && animations.get(AnimationActions.MOVE) != null) {
				if(sc.getForm() != curForm){
					curForm = sc.getForm();
					t = animations.get(AnimationActions.MOVE).subTexture(new Rectangle2D(curForm*300,0,300,300));
					t.setPreserveRatio(true);
					t.setFitHeight(120);	
					curAnimation = AnimationActions.MOVE;
				}
			} 
			else if(sc.isUnderground()){
			}
			else if(enemy.<Boolean> getProperty("attacking") && animations.get(AnimationActions.ATK) != null){
				if(curAnimation != AnimationActions.ATK){
					t = animations.get(AnimationActions.ATK);
					if(!aniDetails.get(AnimationActions.ATK).startAnimation){
						t = t.toStaticAnimatedTexture(aniDetails.get(AnimationActions.ATK).frames, 
							Duration.seconds(aniDetails.get(AnimationActions.ATK).period));
					}
					curAnimation = AnimationActions.ATK;
					t.setPreserveRatio(true);
					t.setFitHeight(120);	
				}
			}
			else if(animations.get(AnimationActions.IDLE) != null) {
				if(curAnimation != AnimationActions.IDLE){
					t = animations.get(AnimationActions.IDLE);
					if(!aniDetails.get(AnimationActions.IDLE).startAnimation){
						t = t.toStaticAnimatedTexture(aniDetails.get(AnimationActions.IDLE).frames, 
							Duration.seconds(aniDetails.get(AnimationActions.IDLE).period));
					}
					curAnimation = AnimationActions.IDLE;
					t.setPreserveRatio(true);
					t.setFitHeight(120);
				}
			}
		}
		
		return t;
	}
}
