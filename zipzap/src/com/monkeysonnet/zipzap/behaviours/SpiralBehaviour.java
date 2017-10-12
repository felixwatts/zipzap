package com.monkeysonnet.zipzap.behaviours;

import com.badlogic.gdx.math.Vector2;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.zipzap.entities.Enemy;

public class SpiralBehaviour extends BehaviourBase
{
	private float _angularVelocity;
	private float _accel;
	private float _vel;
	private float _angle;
	
	public SpiralBehaviour(float angularVel, float acc)
	{
		_accel = acc;
		_angularVelocity = angularVel;
		if(Game.Dice.nextBoolean())
			_angularVelocity = -_angularVelocity;
	}
	
	@Override
	public void spawn(IEntity subject)
	{
		super.spawn(subject);
		
		Enemy e = (Enemy)subject;
		
		_angle = e.angle();
		_vel = e.body().getLinearVelocity().len();
	}
	
	@Override
	public void update(float dt, IEntity subject)
	{
		super.update(dt, subject);
		
		_angle += dt * _angularVelocity;
		_vel += dt * _accel;
		
		Enemy e = (Enemy)subject;
		
		//e.body().setLinearVelocity(Vector2.tmp.set(_vel, 0).rotate(_angle));
		
		e.targetDirection(Vector2.tmp.set(_vel, 0).rotate(_angle));
	}
}
