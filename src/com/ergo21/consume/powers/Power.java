package com.ergo21.consume.powers;

import com.ergo21.consume.Player;

import javafx.scene.Group;

public class Power{
	protected Player player;
	protected Player.PowerElem powElement;
	
	public Power(Player p, Player.PowerElem pE){
		player = p;
		powElement = pE;
	}

	public Group makeAttack(){
		return new Group();
	}
	
	public Player.PowerElem getElement(){
		return powElement;
	}
}