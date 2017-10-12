package com.monkeysonnet.zipzap.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.engine.IFactory;
import com.monkeysonnet.engine.editor.Map;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.script.IActiveCount;

public class Millipede extends Congoid
{
	private static final int KILL_SCORE = 10;	
	private static final int NUM_SEGMENTS = 12;
//	private static final float TURN_ANGLE_VARIANCE = 90;
//	private static final float MIN_TURN_ANGLE = 15f;
//	private static final float MIN_TURN_TIME = 1f;
//	private static final float TURN_TIME_VARIANCE = 2f;
//	private static final int NUM_TURNS = 12;	
	private static final float SPEED = 12f;
	private static final float PROJECTILE_SPEED = 20;
	private static final float NUM_PROJECTILES = 6;
	private static final Map mapHead = new Map("centipede-head.v", 3f, 180f);
	private static final Map mapSegment = new Map("centipede-head.v", 3f, 0f);
	
	private static int _activeCount;	
	public static final IActiveCount activeCount = new IActiveCount()
	{
		
		@Override
		public int activeCount()
		{
			return _activeCount;
		}
	};
	
	private static final float[] turns = new float[]
	{
		500,
		30,
		1000,
		-60,
		500,
		30,
	};

	private static final MillipedePool pool = new MillipedePool();
	private static class MillipedePool extends Pool<Millipede>
	{
		@Override
		protected Millipede newObject()
		{
			return new Millipede();
		}
	}
	
	private static final IFactory<Congoid> factory = new IFactory<Congoid>()
	{		
		@Override
		public Congoid get()
		{
			_activeCount++;
			return pool.obtain();
		}
	};
	
	private Millipede() 
	{
		_killScore = KILL_SCORE;
	}
	
	public static void spawn()
	{
		spawnConga(
				factory,
				NUM_SEGMENTS, 
				mapHead, 
				mapSegment, 
				SPEED, 
				6 / SPEED,
				turns);
	}
	
	@Override
	protected void onFree()
	{
		if(_killed)
		{
			if(_map == mapHead)
			{
				PowerUp.spawn(origin(), PowerUp.TYPE_MEGA_LASER);
			}
		}
		
		super.onFree();
		pool.free(this);
		_activeCount--;
	}
	
	@Override
	public Color color(int poly)
	{
		return Color.RED;
	}
	
	@Override
	public boolean hit(Fixture f, boolean mega, Vector2 loc, Vector2 norm)
	{
		if(!Z.sim.inPhysicalUpdate())
			for(int n = 0; n < NUM_PROJECTILES; n++)
			{
				Projectile.spawn(origin(), Vector2.tmp.set(PROJECTILE_SPEED, 0).rotate(n * (360f/NUM_PROJECTILES)), 0, Color.ORANGE, false);
			}
		
		return super.hit(f, mega, loc, norm);
	}
	
	@Override
	public void update(float dt)
	{
		super.update(dt);
	}
}
