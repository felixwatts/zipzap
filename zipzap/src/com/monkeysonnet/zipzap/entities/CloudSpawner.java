package com.monkeysonnet.zipzap.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.zipzap.Z;

public class CloudSpawner implements IEntity
{
	private static final CloudSpawnerPool pool = new CloudSpawnerPool();
	private static final float SPAWN_TIME = 6f;
	private static final float MAX_AGE = 6f;
	private static class CloudSpawnerPool extends Pool<CloudSpawner>
	{
		@Override
		protected CloudSpawner newObject()
		{
			return new CloudSpawner();
		}
	}
	
	private final Color _color = new Color();
	private float _size;
	private float _time;
	private final Vector2 _loc = new Vector2();
	
	private CloudSpawner()
	{		
	}
	
	public static void spawn(Vector2 loc, Color color, float size)
	{
		CloudSpawner c = pool.obtain();
		
		c._color.set(color);
		c._size = size;
		c._loc .set(loc);
		
		c._time = Game.Dice.nextFloat() * SPAWN_TIME;
		
		Z.sim().entities().add(c);
	}

	@Override
	public void update(float dt)
	{
		_time -= dt;
		if(_time < 0)
		{
			Particle.spawn(Z.sim(), _loc, Z.v1().set(0, 0), Z.v2().set(0, 0), _color, 0, _size / MAX_AGE, MAX_AGE);
			
			//Z.sim().spawnCloud(_loc, 1, _color, _size);
			_time = SPAWN_TIME;
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
		return 0;
	}

}
