package com.almasb.consume.ai;

import java.util.HashMap;

import com.almasb.consume.Types;
import com.almasb.consume.Types.AnimationActions;
import com.almasb.fxgl.asset.Texture;
import com.almasb.fxgl.entity.Control;
import com.almasb.fxgl.entity.Entity;

import javafx.geometry.Rectangle2D;
import javafx.util.Duration;

public class AnimatedEnemyControl implements Control {

	public class AnimationDetails{
		public final Rectangle2D area;
		public final int frames;
		public final Duration period;
		public final boolean startAnimation;
		
		public AnimationDetails(Rectangle2D are, int fra, double per, boolean staAnim){
			area = are;
			frames = fra;
			period = Duration.seconds(per);
			startAnimation = staAnim;
		}
	}
	
	private HashMap<Types.AnimationActions, Texture> animations = new HashMap<>();
	private HashMap<Types.AnimationActions, AnimationDetails> aniDetails;
	private Entity enemy;
	private Texture spriteSheet;

	public AnimatedEnemyControl(Entity e, HashMap<Types.AnimationActions, AnimationDetails> hashMap, Texture sS) {
		enemy = e;
		aniDetails = hashMap;
		spriteSheet = sS;
		
		for(Types.AnimationActions aA : aniDetails.keySet()){
			AnimationDetails val = aniDetails.get(aA);
			if(val != null){
				if(val.startAnimation){
					animations.put(aA, spriteSheet.subTexture(val.area).
							toStaticAnimatedTexture(val.frames, val.period));
				}
				else{
					animations.put(aA, spriteSheet.subTexture(val.area));
				}
			}
		}
	}

	@Override
	public void onUpdate(Entity entity, long now) {
		entity.setGraphics(getCurrentImage());
	}
	
	private Texture getCurrentImage() {
		Texture t = null;
		
		PhysicsControl ec = enemy.getControl(PhysicsControl.class);
		if (enemy != null && ec != null) {
			if(enemy.<Boolean> getProperty("jumping") && animations.get(AnimationActions.JUMP) != null){
				if(enemy.<Boolean> getProperty("attacking") && animations.get(AnimationActions.JATK) != null){
					t = animations.get(AnimationActions.JATK);
					if(!aniDetails.get(AnimationActions.JATK).startAnimation){
						t = t.toStaticAnimatedTexture(aniDetails.get(AnimationActions.JATK).frames, 
								aniDetails.get(AnimationActions.JATK).period);
					}
				}
				else{
					t = animations.get(AnimationActions.JUMP);
					if(!aniDetails.get(AnimationActions.JUMP).startAnimation){
						t = t.toStaticAnimatedTexture(aniDetails.get(AnimationActions.JUMP).frames, 
								aniDetails.get(AnimationActions.JUMP).period);
					}
				}
				
				t.setPreserveRatio(true);
				t.setTranslateX(-30);
				t.setTranslateY(-40);
				t.setFitHeight(75);
			}
			else if (ec.getVelocity().getX() != 0 && animations.get(AnimationActions.MOVE) != null) {
				if(enemy.<Boolean> getProperty("attacking") && animations.get(AnimationActions.MATK) != null){
					t = animations.get(AnimationActions.MATK);
					if(!aniDetails.get(AnimationActions.MATK).startAnimation){
						t = t.toStaticAnimatedTexture(aniDetails.get(AnimationActions.MATK).frames, 
								aniDetails.get(AnimationActions.MATK).period);
					}
				}
				else{
					t = animations.get(AnimationActions.MOVE);
					if(!aniDetails.get(AnimationActions.MOVE).startAnimation){
						t = t.toStaticAnimatedTexture(aniDetails.get(AnimationActions.MOVE).frames, 
								aniDetails.get(AnimationActions.MOVE).period);
					}
				}
				t.setPreserveRatio(true);
				t.setTranslateX(-30);
				t.setTranslateY(-40);
				t.setFitHeight(75);	
			} 
			else if(enemy.<Boolean> getProperty("attacking") && animations.get(AnimationActions.ATK) != null){
				t = animations.get(AnimationActions.ATK);
				if(!aniDetails.get(AnimationActions.ATK).startAnimation){
					t = t.toStaticAnimatedTexture(aniDetails.get(AnimationActions.ATK).frames, 
							aniDetails.get(AnimationActions.ATK).period);
				}
				t.setPreserveRatio(true);
				t.setTranslateX(-30);
				t.setTranslateY(-40);
				t.setFitHeight(75);	
			}
			else if(animations.get(AnimationActions.IDLE) != null) {
				t = animations.get(AnimationActions.IDLE);
				if(!aniDetails.get(AnimationActions.IDLE).startAnimation){
					t = t.toStaticAnimatedTexture(aniDetails.get(AnimationActions.IDLE).frames, 
							aniDetails.get(AnimationActions.IDLE).period);
				}
				t.setPreserveRatio(true);
				t.setTranslateX(-30);
				t.setTranslateY(-40);
				t.setFitHeight(75);
			}
		}
		
		if(t != null && ((enemy.<Boolean> getProperty("facingRight") && t.getScaleX() < 0) ||
				(!enemy.<Boolean> getProperty("facingRight") && t.getScaleX() > 0))){
			t.setScaleX(t.getScaleX()*-1);
		}
		
		return t;
	}
}
