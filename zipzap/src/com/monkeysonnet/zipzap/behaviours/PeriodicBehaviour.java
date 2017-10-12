package com.monkeysonnet.zipzap.behaviours;

import com.monkeysonnet.engine.ICallback;
import com.monkeysonnet.engine.IEntity;

public class PeriodicBehaviour extends BehaviourBase
{
	private ICallback _callback;
	private float _period, _time;
	private Object _arg;
	
	public PeriodicBehaviour(ICallback callback, float period, Object arg)
	{
		_period = period;
		_callback = callback;
		_arg = arg;
	}
	
	@Override
	public void spawn(IEntity subject)
	{
		_time = 0;
	}
	
	@Override
	public void update(float dt, IEntity subject)
	{
		_time += dt;
		if(_time > _period)
		{
			_time = 0;
			_callback.callback(_arg);
		}
	}
}
