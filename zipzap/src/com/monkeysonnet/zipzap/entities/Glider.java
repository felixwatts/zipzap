package com.monkeysonnet.zipzap.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.engine.ICallback;
import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.engine.IFactory;
import com.monkeysonnet.engine.editor.Map;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.behaviours.BlobTrailBehaviour;
import com.monkeysonnet.zipzap.behaviours.FireWhenFacingBehaviour;
import com.monkeysonnet.zipzap.behaviours.HomingBehaviour;
import com.monkeysonnet.zipzap.behaviours.IBehaviour;
import com.monkeysonnet.zipzap.script.IActiveCount;

public class Glider extends Congoid
{
	private static final int KILL_SCORE = 10;
	protected static final float PROJECTILE_SPEED = 40f;
	private static final float SPEED = 20;
	protected static final int SFX_FIRE = -1015;
	
	private static final Map map = new Map("glider.v", 1f, 180f);
	private static final IBehaviour homingBehaviour = new HomingBehaviour(360f, 45f, false);
	
	private static final GliderPool pool = new GliderPool();
	private static class GliderPool extends Pool<Glider>
	{
		@Override
		protected Glider newObject()
		{
			return new Glider();
		}
	}
	
	private static final IFactory<Congoid> factory = new IFactory<Congoid>()
	{		
		@Override
		public Congoid get()
		{
			Glider g = pool.obtain();
			g._squadNum = squadNum;
			return g;
		}
	};
	
	private static int _activeCount;	
	public static final IActiveCount activeCount = new IActiveCount()
	{
		
		@Override
		public int activeCount()
		{
			return _activeCount;
		}
	};	
	
	private static int squadNum;
	private int _squadNum;

	private final ICallback fireCallback = new ICallback()
	{		
		@Override
		public void callback(Object arg)
		{
			Z.sim.fireEvent(SFX_FIRE, null);
			Projectile.spawn(origin(), Vector2.tmp.set(PROJECTILE_SPEED, 0).rotate(angle()), 0, Color.ORANGE, false);
		}
	};
	
	private final FireWhenFacingBehaviour fireBehaviour = new FireWhenFacingBehaviour(15f, 0.4f, fireCallback);
	
	private Glider() 
	{
		_killScore = KILL_SCORE;
		
		_behaviours.add(new BlobTrailBehaviour(Color.RED, 0.05f, 0.25f, map.point("jet-left").point, 2f, -8f));
		_behaviours.add(new BlobTrailBehaviour(Color.WHITE, 0.05f, 0.25f, map.point("jet-left").point, 1f, -4f));
		
		_behaviours.add(new BlobTrailBehaviour(Color.RED, 0.05f, 0.25f, map.point("jet-right").point, 2f, -8f));
		_behaviours.add(new BlobTrailBehaviour(Color.WHITE, 0.05f, 0.25f, map.point("jet-right").point, 1f, -4f));
	}
	
	public static void spawn()
	{
		spawnConga(
				factory,
				5, 
				map, 
				map, 
				SPEED, 
				8f / 20f, 
				0.5f, 
				1f,
				20f,
				45f, 
				6);
		
		squadNum++;
		_activeCount+=5;
	}
	
	@Override
	protected void onSpawn()
	{
		_behaviours.removeValue(fireBehaviour, true);
		_behaviours.removeValue(homingBehaviour, true);
		super.onSpawn();
	}
	
	@Override
	protected void onFree()
	{
		super.onFree();
		pool.free(this);
		_activeCount--;
	}
	
	@Override
	public boolean hit(Fixture f, boolean mega, Vector2 loc, Vector2 norm)
	{
		for(int n = Z.sim.entities().size-1; n >= 0; n--)
		{
			IEntity e = Z.sim.entities().get(n);
			if(e instanceof Glider)
			{
				Glider g = (Glider)e;
				if(g._squadNum == _squadNum)
				{
					g.attack();
				}
			}
		}
		
		return super.hit(f, mega, loc, norm);
	}

	private void attack()
	{
		if(!_behaviours.contains(homingBehaviour, true))
		{
			Z.sim.tweens().killTarget(_angle);
			_body.setLinearVelocity(Vector2.tmp.set(SPEED, 0).rotate(_angle.floatValue()));
			_behaviours.add(fireBehaviour);
			_behaviours.add(homingBehaviour);
		}
	}
}
