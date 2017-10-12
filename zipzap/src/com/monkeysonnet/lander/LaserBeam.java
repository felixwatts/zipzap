package com.monkeysonnet.lander;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.equations.Sine;
import aurelienribon.tweenengine.primitives.MutableFloat;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.engine.ColorTools;
import com.monkeysonnet.zipzap.IRenderableMultiPolygon;
import com.monkeysonnet.zipzap.ITriggerable;
import com.monkeysonnet.zipzap.Tools;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.entities.Enemy;

public class LaserBeam extends Enemy implements ITriggerable, IRenderableMultiPolygon
{
	private static final int SFX_SWITCH = -1029;
	private static final LaserBeamPool pool = new LaserBeamPool();
	private static class LaserBeamPool extends Pool<LaserBeam>
	{
		@Override
		protected LaserBeam newObject()
		{
			return new LaserBeam();
		}
	}
	
	private final MutableFloat _alpha = new MutableFloat(0.3f);
	private Tween _alphaTween;
	private String _name;
	private boolean _isTripwire;
	
	private final Vector2[] _verts = new Vector2[] { new Vector2(), new Vector2() };
	private boolean _on;
	private String _target;
	private boolean _tripped;
	private int _numContacts;
	
	static
	{

	}
	
	public static void spawn(Vector2 from, Vector2 to, boolean on, String name, boolean tripWire, String target)
	{
		LaserBeam b = pool.obtain();
		
		b._isTripwire = tripWire;
		b._target = target;
		b._tripped = false;
		b._numContacts = 0;
		
		b._alpha.setValue(0.3f);
		b._alphaTween = Tween
				.to(b._alpha, 0, 50)
				.target(0.6f)
				.ease(Sine.INOUT)
				.repeatYoyo(Tween.INFINITY, 0)
				.start(Z.sim().tweens());
		
		int mask = LanderSim.COL_CAT_GUY;
		if(b._isTripwire)
			mask |= LanderSim.COL_CAT_WALL;
		
		b._body = B2d
				.staticBody()
				.active(false)
				.withFixture(B2d
						.edge()
						.between(from, to)
						.category(LanderSim.COL_CAT_ENEMY_BULLET)
						.mask(mask)
						.sensor(true)
						.userData(b._fixtureTag))
				.create(L.sim.world());
		
		b._verts[0].set(from);
		b._verts[1].set(to);
		
		b.setOn(on);
		
		b._name = name;
		L.sim.register(b, name);
		
		b.onSpawn();
		
		float x = Math.min(from.x, to.x);
		float y = Math.min(from.y, to.y);
		float w = Math.max(from.x, to.x) - x;
		float h = Math.max(from.y, to.y) - y;
		L.sim.environment().put(b, x, y, w, h);
	}

	@Override
	public int getNumPolys()
	{
		return 1;
	}

	@Override
	public float angle(int poly)
	{
		return 0;
	}

	@Override
	public Vector2 origin(int poly)
	{
		return Tools.zeroVector;
	}

	@Override
	public Vector2[] verts(int poly)
	{
		return _verts;
	}

	@Override
	public Color color(int poly)
	{
		if(!_on)
			return null;
		else return ColorTools.combineAlpha(_isTripwire ? Color.GREEN : Color.RED, _alpha.floatValue());
	}

	@Override
	public float lineWidth(int poly)
	{
		return 0.5f;
	}

	@Override
	public boolean isLoop(int poly)
	{
		return false;
	}

	@Override
	public float clipRadius()
	{
		return 2;
	}

	@Override
	public boolean trigger()
	{
		setOn(!_on);
		return true;
	}
	
	private void setOn(boolean on)
	{
		_body.setActive(on);
		_on = on;
	}
	
	@Override
	public void update(float dt)
	{
		super.update(dt);
		
		if(!_dead)
		{
			if(_tripped)
			{
				_tripped = false;
				L.sim.trigger(_target);
				L.sim.fireEvent(SFX_SWITCH, null);
			}
		}
	}
	
	@Override
	public void onBeginContact(Contact c, Fixture me, Fixture other)
	{
		super.onBeginContact(c, me, other);
		
		if(_isTripwire)
		{
			if(_target != null)
			{
				_numContacts++;
				if(_numContacts == 1)
					_tripped = true;				
			}
		}		
		else if((other.getFilterData().categoryBits & LanderSim.COL_CAT_GUY) != 0)
		{
			L.sim.guy().kill();
			L.sim.spawnSparks(origin(), _body.getLinearVelocity(), Color.RED);
		}
	}
	
	@Override
	public void onEndContact(Contact c, Fixture me, Fixture other)
	{
		super.onEndContact(c, me, other);
		
		if(_isTripwire)
		{
			if(_target != null)
			{
				_numContacts--;
				if(_numContacts == 0)
					_tripped = true;				
			}
		}
	}
	
	@Override
	protected void onFree()
	{
		_alphaTween.kill();
		L.sim.unregister(_name);
		L.sim.environment().remove(this);
		pool.free(this);
	}
}
