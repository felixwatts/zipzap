package com.monkeysonnet.zipzap;

import com.monkeysonnet.engine.Game;

public class VariSfx
{
	private int[] _sfx;
	
	public VariSfx(int... sfx)
	{
		_sfx = sfx;
	}
	
	public void play()
	{
		play(null);
	}
	
	public void play(Object arg)
	{
		Z.sim.fireEvent(_sfx[Game.Dice.nextInt(_sfx.length)], arg);
	}
}
