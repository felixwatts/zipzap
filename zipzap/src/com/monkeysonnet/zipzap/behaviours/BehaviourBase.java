package com.monkeysonnet.zipzap.behaviours;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.monkeysonnet.engine.IEntity;

public class BehaviourBase implements IBehaviour
{

	@Override
	public void update(float dt, IEntity subject)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void spawn(IEntity subject)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onBeginContact(IEntity subject, Contact c, Fixture me,
			Fixture other)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onEndContact(IEntity subject, Contact c, Fixture me,
			Fixture other)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postSolve(IEntity subject, Contact c, ContactImpulse impulse,
			Fixture me, Fixture other)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hit(IEntity subject, Fixture fixture, boolean mega, Vector2 loc, Vector2 norm)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFree(IEntity subject)
	{
		// TODO Auto-generated method stub
		
	}
}
