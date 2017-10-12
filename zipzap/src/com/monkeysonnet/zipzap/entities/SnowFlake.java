package com.monkeysonnet.zipzap.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.editor.Map;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;
import com.monkeysonnet.zipzap.behaviours.BlobTrailBehaviour;
import com.monkeysonnet.zipzap.behaviours.DieOnHitBehaviour;
import com.monkeysonnet.zipzap.behaviours.FlyingSoundBehaviour;
import com.monkeysonnet.zipzap.script.IActiveCount;

public class SnowFlake extends EnemyBasic
{
	private static final Map map = new Map("snowflake.v", 1.5f, 0f);
	
	private static final SnowFlakePool pool = new SnowFlakePool();
	private static class SnowFlakePool extends Pool<SnowFlake>
	{
		@Override
		protected SnowFlake newObject()
		{
			return new SnowFlake();
		}
	}

	private static int _activeCount;
	
	public static final IActiveCount activeCount = new IActiveCount()
	{
		public int activeCount() { return _activeCount; }
	};

	private static final float SPEED = 32f;
	private static final int SFX_HIT = -1023;	
	
	private SnowFlake()
	{		
		_behaviours.removeValue(DieOnHitBehaviour.basic(), true);
		_behaviours.add(new BlobTrailBehaviour(Color.CYAN, 0.05f, 0.25f, 0.5f));
		_behaviours.add(new FlyingSoundBehaviour(31));
	}
	
	public static void spawn()
	{
		SnowFlake s = pool.obtain();
		s.setup(map, SPEED);
		//s._body.setLinearVelocity(Vector2.tmp.set(SPEED, 0).rotate(Tools.angleToShip(s.origin())).rotate((float) (Game.Dice.nextGaussian() * ANGLE_VARIANCE)));
		
		s._body.setTransform(Vector2.tmp.set(ZipZapSim.SPAWN_DISTANCE, (Game.Dice.nextFloat() * ZipZapSim.SPAWN_DISTANCE) - (ZipZapSim.SPAWN_DISTANCE/2f)).add(Z.ship().origin()), 0);
		s._body.setLinearVelocity(-SPEED, 0);
		
		_activeCount++;
	}
	
	@Override
	protected void onFree()
	{
		pool.free(this);
		_activeCount--;
	}
	
	public boolean hit(Fixture f, boolean mega, Vector2 loc, Vector2 norm) 
	{		
		Z.sim.fireEvent(SFX_HIT, null);
		return super.hit(f, mega, loc, norm);
	};
}
