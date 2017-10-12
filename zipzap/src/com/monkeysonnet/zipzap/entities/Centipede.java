package com.monkeysonnet.zipzap.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.engine.IFactory;
import com.monkeysonnet.engine.editor.Map;
import com.monkeysonnet.zipzap.Tools;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.behaviours.BlobTrailBehaviour;
import com.monkeysonnet.zipzap.behaviours.IBehaviour;
import com.monkeysonnet.zipzap.behaviours.MaxAgeBehaviour;
import com.monkeysonnet.zipzap.behaviours.SpiralBehaviour;
import com.monkeysonnet.zipzap.script.IActiveCount;

public class Centipede extends Congoid
{
	private static final int KILL_SCORE = 8;	
	private static final int NUM_SEGMENTS = 9;
	private static final float TURN_ANGLE_VARIANCE = 90;
	private static final float MIN_TURN_ANGLE = 15f;
	private static final float MIN_TURN_TIME = 1f;
	private static final float TURN_TIME_VARIANCE = 2f;
	private static final int NUM_TURNS = 12;	
	private static final float SPEED = 12f;
	private static final int SFX_HIT = -1024;
	private static final int SFX_DIE = -1016;
	private static final Map mapHead = new Map("centipede-head.v", 2f, 180f);
	private static final Map mapSegment = new Map("centipede-segment.v", 2f, 180f);
	private final IBehaviour _sprialBehaviour = new SpiralBehaviour(120f, 8f);//  new HomingBehaviour(360f, 45f, false);
	private final IBehaviour _blobTrailBehaviour = new BlobTrailBehaviour(Color.RED, 0.1f, 0.5f, Tools.zeroVector, 1f, -0.8f);
	private final IBehaviour _maxAgeBehaviour = new MaxAgeBehaviour((3f * Game.Dice.nextFloat()) + 1);
	
	private static int squadNum;
	private int _squadNum;
	
	private static int _activeCount;	
	public static final IActiveCount activeCount = new IActiveCount()
	{
		
		@Override
		public int activeCount()
		{
			return _activeCount;
		}
	};

	private static final CentipedePool pool = new CentipedePool();
	private static class CentipedePool extends Pool<Centipede>
	{
		@Override
		protected Centipede newObject()
		{
			return new Centipede();
		}
	}
	
	private static final IFactory<Congoid> factory = new IFactory<Congoid>()
	{		
		@Override
		public Congoid get()
		{
			_activeCount++;
			
			Centipede c = pool.obtain();
			c._squadNum = squadNum;
			
			return c;
		}
	};
	
	private Centipede() 
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
				4 / SPEED, 
				MIN_TURN_TIME, 
				TURN_TIME_VARIANCE, 
				MIN_TURN_ANGLE,
				TURN_ANGLE_VARIANCE, 
				NUM_TURNS);
		
		squadNum++;
	}
	
	@Override
	protected void onSpawn()
	{
		_behaviours.removeValue(_sprialBehaviour, true);
		_behaviours.removeValue(_blobTrailBehaviour, true);
		_behaviours.removeValue(_maxAgeBehaviour, true);
		super.onSpawn();
	}
	
	@Override
	protected void onFree()
	{
		Z.sim.spawnCloud(origin(), 1, Color.ORANGE, 6);
		Z.sim.spawnExlosion(origin(), 3, Color.ORANGE, 1);
		super.onFree();
		pool.free(this);
		_activeCount--;
	}
	
	@Override
	public boolean hit(Fixture f, boolean mega, Vector2 loc, Vector2 norm)
	{
		if(!_dead)
		{
			for(int n = Z.sim.entities().size-1; n >= 0; n--)
			{
				IEntity e = Z.sim.entities().get(n);
				if(e instanceof Centipede)
				{
					Centipede c = (Centipede)e;
					if(c != this && c._squadNum == _squadNum)
					{
//						if(mega) // || _map == mapHead)
//							DieOnHitBehaviour.basic().hit(c, null, true, null, null);
//						else
							c.attack();
					}
				}
			}
		}
		
		return super.hit(f, mega, loc, norm);
	}

	private void attack()
	{
		if(!_behaviours.contains(_sprialBehaviour, true))
		{
			Z.sim.spawnExlosion(origin(), 3, Color.RED, 6f);
			Z.sim.tweens().killTarget(_angle);
			_body.setLinearVelocity(Vector2.tmp.set(SPEED * 1.5f, 0).rotate(_angle.floatValue()));
			_behaviours.add(_sprialBehaviour);
			_sprialBehaviour.spawn(this);
			_behaviours.add(_maxAgeBehaviour);
			_maxAgeBehaviour.spawn(this);
			_behaviours.add(_blobTrailBehaviour);
		}
	}
	
	@Override
	public void onKill()
	{
		Z.sim.fireEvent(SFX_HIT, null);
		Z.sim.fireEvent(SFX_DIE, null);
		_killed = true;
	}
}
