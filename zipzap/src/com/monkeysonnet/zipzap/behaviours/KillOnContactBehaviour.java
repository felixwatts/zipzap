package com.monkeysonnet.zipzap.behaviours;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.zipzap.IHitable;
import com.monkeysonnet.zipzap.Z;

public class KillOnContactBehaviour extends BehaviourBase
{
	private static KillOnContactBehaviour _alsoDieInstance;
	private static KillOnContactBehaviour _basicInstance;
	
	private boolean _alsoDie;
	
	private KillOnContactBehaviour(boolean alsoDie)
	{
		_alsoDie = alsoDie;
	}

	@Override
	public void onBeginContact(IEntity subject, Contact c, Fixture me, Fixture other)
	{
		if(other.getBody().getUserData() == Z.ship())
		{
			Z.ship().strike();
			
			if(_alsoDie || Z.ship().dragonMode())
				if(subject != null)
				{
					if(subject instanceof IHitable)
					{
						((IHitable) subject).hit(me, true, c.getWorldManifold().getPoints()[0], c.getWorldManifold().getNormal());
					}
					else
					{
						subject.free();
					}
				}
		}
	}
	
	public static KillOnContactBehaviour alsoDie()
	{
		if(_alsoDieInstance == null)
			_alsoDieInstance = new KillOnContactBehaviour(true);
		return _alsoDieInstance;
	}
	
	public static KillOnContactBehaviour basic()
	{
		if(_basicInstance == null)
			_basicInstance = new KillOnContactBehaviour(false);
		return _basicInstance;
	}
}
