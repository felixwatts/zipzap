package com.monkeysonnet.zipzap.behaviours;

import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.entities.Entity;

public class FlyingSoundBehaviour extends BehaviourBase
{
	private int _sound;
	private long _instance;
	private float _vol;
	private float _dropOffRange2;
	
	public FlyingSoundBehaviour(int sound)
	{
		this(sound, 1f, 10f);
	}
	
	public FlyingSoundBehaviour(int sound, float vol, float dropOff)
	{
		_sound = sound;
		_vol = vol;
		_dropOffRange2 = dropOff * dropOff;
	}
	
	@Override
	public void spawn(IEntity subject)
	{
		super.spawn(subject);		
		_instance = Z.sfx.play(_sound, _vol, true);
	}
	
	@Override
	public void onFree(IEntity subject)
	{
		super.onFree(subject);		
		Z.sfx.stop(_sound, _instance);
	}
	
	@Override
	public void update(float dt, IEntity subject)
	{
		super.update(dt, subject);
		
		Entity e = (Entity)subject;
		
		float vol = _dropOffRange2 / Z.ship().origin().dst2(e.origin());
		if(vol > 1f)
			vol = 1f;
		
		if(e.body().getLinearVelocity().len2() == 0)
			vol *= 0.5f;
		
		Z.sfx.setVolume(_sound, _instance, vol);
	}
}
