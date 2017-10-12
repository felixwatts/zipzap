package com.monkeysonnet.zipzap.script;

import com.monkeysonnet.zipzap.Z;

public class TimedWave extends GameController
{
	private float _length;
	
	public TimedWave(float length)
	{
		_length = length;
	}
	
	public TimedWave(float length, IAutoSpawner spawner)
	{
		super(spawner);
		_length = length;
	}
	
	@Override
	public void update(float dt)
	{
		super.update(dt);
		
		if(_time > _length)
		{
			Z.screen.sim().advanceScript();
		}
	}
}
