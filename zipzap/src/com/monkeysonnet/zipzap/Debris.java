package com.monkeysonnet.zipzap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.IEntity;

public class Debris implements IRenderablePolygon, IEntity
{
	private static final DebrisPool pool = new DebrisPool();
	private static final float MAX_AGE = 1f;
	private static final float MAX_ANGULAR_VEL = 60;
	
	private float _angle, _angularVel;
	private Vector2 _origin, _v1, _v2, _vel;
	private Vector2[] _verts;
	private Color _color;
	private float _age;
	
	private static class DebrisPool extends Pool<Debris>
	{
		@Override
		protected Debris newObject()
		{
			return new Debris();
		}
	}
	
	private Debris()
	{
		_origin = new Vector2();
		_v1 = new Vector2();
		_v2 = new Vector2();
		_verts = new Vector2[2];
		_verts[0] = _v1;
		_verts[1] = _v2;
		_vel = new Vector2();
		_color = new Color();
	}
	
	public static void spawn(Vector2 v1, Vector2 v2, Vector2 vel, Color color)
	{		
		if(color == null)
			return;
	
		if(Gdx.graphics.getFramesPerSecond() < 55)
			return;
		
		Debris d = pool.obtain();
		d.init(v1, v2, vel, color);
		Z.sim().entities().add(d);
	}
	
	private void init(Vector2 v1, Vector2 v2, Vector2 vel, Color color)
	{
		_origin.set(v1).add(v2).mul(0.5f);
		_v1.set(v1).sub(_origin);
		_v2.set(v2).sub(_origin);	
		_vel.set(vel);
		_color.set(color);
		_age = 0;
		_angularVel = (Game.Dice.nextFloat() - 0.5f) * 2f * MAX_ANGULAR_VEL;
	}

	@Override
	public float angle()
	{
		return _angle;
	}

	@Override
	public Vector2 origin()
	{
		return _origin;
	}

	@Override
	public Vector2[] verts()
	{
		return _verts;
	}

	@Override
	public Color color()
	{
		return _color;
	}

	@Override
	public float lineWidth()
	{
		return 1f;
	}

	@Override
	public void update(float dt)
	{
		_age += dt;
		if(_age > MAX_AGE)
			free();
		else
		{
			_angle += _angularVel * dt;
			_origin.add(Game.workingVector2a.set(_vel).mul(dt));
			_color.a = 1 - (_age / MAX_AGE);
		}
	}

	@Override
	public void free()
	{
		//ZipZapScreen.instance().sim().freeDebris(this);
		Z.sim().entities().removeValue(this, true);
		pool.free(this);
	}

	@Override
	public int layer()
	{
		return 0;
	}
	
	@Override
	public float clipRadius()
	{
		return 8f;
	}
}
