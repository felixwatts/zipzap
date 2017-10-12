package com.monkeysonnet.zipzap.entities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.engine.FixtureTag;
import com.monkeysonnet.engine.IContactHandler;
import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.zipzap.ITriggerable;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;

public class TriggerArea implements IEntity, IContactHandler
{
	private Body _body;
	private ITriggerable _handler;
	private final FixtureTag _fixtureTag = new FixtureTag(this, this);
	private boolean _triggered, _triggerring;
	
	private TriggerArea(){}
	
	private static final TriggerAreaPool pool = new TriggerAreaPool();	
	private static class TriggerAreaPool extends Pool<TriggerArea>
	{
		@Override
		protected TriggerArea newObject()
		{
			return new TriggerArea();
		}
	}
	
	public static void spawn(Vector2[] verts, ITriggerable handler)
	{
		TriggerArea t = pool.obtain();
		
		t._handler = handler;
		t._triggered = false;
		t._triggerring = false;
		t._body = B2d
				.staticBody()
				.withFixture(B2d
						.polygon(verts)
						.mask(ZipZapSim.COL_CAT_SHIP)
						.category(ZipZapSim.COL_CAT_METEORITE)
						.sensor(true)
						.userData(t._fixtureTag))
				.create(Z.sim().world());
		
		Z.sim().addEnvironment(t, verts);
	}

	@Override
	public void update(float dt)
	{
		if(_triggerring)
		{
			_triggerring = false;
			if(_handler != null)
			{				
				_triggered = _handler.trigger();
			}
		}
	}

	@Override
	public void free()
	{
		if(_body != null)
			Z.sim().world().destroyBody(_body);
		
		pool.free(this);
	}

	@Override
	public int layer()
	{
		return 0;
	}

	@Override
	public void onBeginContact(Contact c, Fixture me, Fixture other)
	{
		if(!_triggered)
		{
			_triggerring = true;			
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
}
