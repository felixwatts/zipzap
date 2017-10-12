package com.monkeysonnet.zipzap.behaviours;

import com.monkeysonnet.engine.IEntity;

public class MaxAgeBehaviour extends BehaviourBase
{
	private float _maxAge;
	private float _age;
	
	public MaxAgeBehaviour(float maxAge)
	{
		_maxAge = maxAge;
	}

	@Override
	public void update(float dt, IEntity subject)
	{
		_age += dt;
		if(_age > _maxAge)
			subject.free();
	}

	@Override
	public void spawn(IEntity subject)
	{
		_age = 0;
	}
}
