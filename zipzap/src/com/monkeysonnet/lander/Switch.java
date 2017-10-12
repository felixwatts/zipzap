package com.monkeysonnet.lander;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.engine.IContactHandler;
import com.monkeysonnet.zipzap.Z;

public class Switch extends Wall implements IContactHandler
{
	private static final SwitchPool pool = new SwitchPool();
	private static final int SFX_SWITCH = -1029;	
	private static class SwitchPool extends Pool<Switch>
	{
		@Override
		protected Switch newObject()
		{
			return new Switch();
		}
	}
	
	private String _target;
	private boolean _switched;
	private boolean _drawWhite;
	
	public static void spawnSwitch(Vector2[] verts, Color color, int colCat, boolean loop, String target)
	{
		Switch s = pool.obtain();
		s.fixtureTag.contactHandler = s;
		init(s, verts, color, colCat, loop, -1);
		s._target = target;
	}
	
	@Override
	public Color color(int poly)
	{
		if(_drawWhite)
		{
			_drawWhite = false;
			return Color.WHITE;
		}
		else return super.color(poly);
	}

	@Override
	public void onBeginContact(Contact c, Fixture me, Fixture other)
	{
		if((other.getFilterData().categoryBits & LanderSim.COL_CAT_GUY_BULLET) != 0)
		{
			_switched = true;
			_drawWhite = true;
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
	
	@Override
	public void free()
	{
		Z.sim.world().destroyBody(_body);
		pool.free(this);
	}
	
	@Override
	public void update(float dt)
	{
		super.update(dt);
		
		if(_switched)
		{
			_switched = false;
			L.sim.trigger(_target);
			L.sim.fireEvent(SFX_SWITCH, null);
		}
	}
}
