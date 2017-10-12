package com.monkeysonnet.zipzap.behaviours;

import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.zipzap.entities.Enemy;

public class RandomSwitchingBehaviour extends BehaviourBase
{
	private float _prob;
	private IBehaviour[] _behaviours;
	private float _time;
	private IBehaviour _current;
	private Enemy _subject;
	
	public RandomSwitchingBehaviour(float switchProbability, IBehaviour...behaviours)
	{
		_prob = switchProbability;
		_behaviours = behaviours;
	}
	
	@Override
	public void spawn(IEntity subject)
	{
		super.spawn(subject);
		_time = 0;		
		_subject = (Enemy)subject;
		_current = _behaviours[0];
		_subject.addBehaviour(_current);
	}
	
	@Override
	public void update(float dt, IEntity subject)
	{
		super.update(dt, subject);
		
		_time -= dt;
		if(_time < 0)
		{
			if(Game.Dice.nextFloat() < _prob)
			{
				_subject.removeBehaviour(_current);				
				_current = _behaviours[Game.Dice.nextInt(_behaviours.length)];				
				_subject.addBehaviour(_current);
			}
		}
	}
	
	public void force(int behaviour)
	{
		_subject.removeBehaviour(_current);				
		_current = _behaviours[behaviour];				
		_subject.addBehaviour(_current);
	}
}
