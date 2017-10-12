package com.monkeysonnet.zipzap.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.zipzap.IRenderablePolygon;
import com.monkeysonnet.zipzap.Sim;
import com.monkeysonnet.zipzap.Z;

public class Particle implements IRenderablePolygon, IEntity
{
	private static final ParticlePool pool = new ParticlePool();
	private static final float MAX_AGE = 2;
	private static final Vector2 _zeroVector = new Vector2();
	
	private Vector2 _origin;
	private Vector2 _vel, _acc;
	private Color _colour;
	private float _age, _size, _dSize, _maxAge;
	private float _dA;	
	
	private static class ParticlePool extends Pool<Particle>
	{
		private int _num = 0;
		
		@Override
		protected Particle newObject()
		{
			if(_num >= 512)
				return null;
			else
			{
				_num++;
				return new Particle();
			}
		}
		
		public void prime()
		{
			while(_num < 256)
				free(newObject());
		}
	}
	
	public static void primePool()
	{
		pool.prime();
	}
	
	public static void spawn(Vector2 origin, Vector2 vel, Color c, float size, float dSize) 
	{
		spawn(Z.sim(), origin, vel, _zeroVector, c, size, dSize, MAX_AGE);
	}
	
	public static void spawn(Vector2 origin, Vector2 vel, Color c, float size, float dSize, float maxAge)
	{
		spawn(Z.sim(), origin, vel, _zeroVector, c, size, dSize, maxAge);
	}
	
	public static void spawn(Sim sim, Vector2 origin, Vector2 vel, Color c, float size, float dSize, float maxAge)
	{
		spawn(sim, origin, vel, _zeroVector, c, size, dSize, maxAge);
	}
	
	public static void spawn(Sim sim, Vector2 origin, Vector2 vel, Vector2 acc, Color c, float size, float dSize, float maxAge) 
	{
//		if(Gdx.graphics.getFramesPerSecond() < 55)
//			return;
		
		Particle p = pool.obtain();
		
		if(p != null)
		{
			p.init(origin, vel, acc, c, size, dSize, maxAge);
			sim.entities().add(p);
		}
	}
	
	private Particle()
	{
		_origin = new Vector2();
		_vel = new Vector2();
		_acc = new Vector2();
		_colour = new Color();				
	}
	
	private void init(Vector2 origin, Vector2 vel, Vector2 acc, Color c, float size, float dSize, float maxAge)
	{
		_origin.set(origin);
		_vel.set(vel);
		_acc.set(acc);
		_colour.set(c);
		_dA = _colour.a / maxAge;
		_age = 0f;
		_size = size;
		_dSize = dSize;
		_maxAge = maxAge;
	}

	@Override
	public float angle()
	{
		return 0;
	}

	@Override
	public Vector2 origin()
	{
		return _origin;
	}

	@Override
	public Vector2[] verts()
	{
		return null;
	}

	@Override
	public Color color()
	{
		return _colour;
	}

	@Override
	public float lineWidth()
	{
		return _size;
	}

	@Override
	public void update(float dt)
	{
		_age += dt;
		if(_age > _maxAge)
			free();
		else
		{
			if(_vel.x != 0 || _vel.y != 0)
				_origin.add(Game.workingVector2a.set(_vel).mul(dt));
			
			if(_acc.x != 0 || _acc.y != 0)
				_vel.add(Game.workingVector2a.set(_acc).mul(dt));
			
			_colour.a -= dt * _dA;//  = 1 - (_age / _maxAge);
			if(_colour.a < 0)
				_colour.a = 0;
			
			_size += _dSize * dt;
			if(_size < 0)
				_size = 0;
		}
	}

	@Override
	public void free()
	{
		Z.sim().entities().removeValue(this, true);
		pool.free(this);
	}

	@Override
	public int layer()
	{
		return 2;
	}
	
	@Override
	public float clipRadius()
	{
		return lineWidth();
	}
}
