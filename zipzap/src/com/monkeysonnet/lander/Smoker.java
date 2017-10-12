package com.monkeysonnet.lander;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.engine.IEntity;

public class Smoker implements IEntity
{
	public static final float RELOAD_TIME = 0.25f;
	
	private static final Vector2 SMOKE_VEL = new Vector2(3, 6);
	
	private static final SmokerPool pool = new SmokerPool();
	private static class SmokerPool extends Pool<Smoker>
	{
		@Override
		protected Smoker newObject()
		{
			return new Smoker();
		}
	}
	
	private final Vector2 _origin = new Vector2();
	private float _reloadTime;
	
	public static void spawn(Vector2 loc)
	{
		Smoker s = pool.obtain();		
		s._origin.set(loc);		
		L.sim.environment().put(s, s._origin.x, s._origin.y, 0, 0);
	}

	@Override
	public void update(float dt)
	{
		_reloadTime -= dt;
		if(_reloadTime < 0)
		{
			_reloadTime = RELOAD_TIME;
			SmokePuff.spawn(_origin, SMOKE_VEL, -10, Color.GRAY, Color.GRAY, 0);
		}
	}

	@Override
	public void free()
	{
		L.sim.environment().remove(this);
		pool.free(this);
	}

	@Override
	public int layer()
	{
		return 0;
	}
}
