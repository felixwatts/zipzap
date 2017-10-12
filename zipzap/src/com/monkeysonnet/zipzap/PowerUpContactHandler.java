package com.monkeysonnet.zipzap;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.monkeysonnet.engine.FixtureTag;
import com.monkeysonnet.engine.IContactHandler;
import com.monkeysonnet.zipzap.entities.PowerUp;
import com.monkeysonnet.zipzap.entities.Ship;

public class PowerUpContactHandler implements IContactHandler
{
	private static PowerUpContactHandler _instance;

	@Override
	public void onBeginContact(Contact c, Fixture me, Fixture other)
	{
		if(other.getBody().getUserData() instanceof Ship)
		{
			PowerUp p = (PowerUp)((FixtureTag)me.getUserData()).owner;
			Z.ship().givePowerUp(p.type());
			p.pickup();
		}
	}

	@Override
	public void onEndContact(Contact c, Fixture me, Fixture other)
	{
	}

	@Override
	public void postSolve(Contact c, ContactImpulse impulse, Fixture me,
			Fixture other)
	{
	}
	
	public static PowerUpContactHandler instance()
	{
		if(_instance == null)
			_instance = new PowerUpContactHandler();
		return _instance;
	}
}
