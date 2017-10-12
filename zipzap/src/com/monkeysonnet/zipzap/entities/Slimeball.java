package com.monkeysonnet.zipzap.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.zipzap.IRenderablePolygon;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;
import com.monkeysonnet.zipzap.behaviours.BlobTrailBehaviour;
import com.monkeysonnet.zipzap.behaviours.HomingBehaviour;
import com.monkeysonnet.zipzap.behaviours.IBehaviour;
import com.monkeysonnet.zipzap.behaviours.KillOnContactBehaviour;
import com.monkeysonnet.zipzap.behaviours.MaxAgeBehaviour;

public class Slimeball extends Enemy implements IRenderablePolygon
{
	private static final float RADIUS = 1f;	
	private static final SlimeballPool pool = new SlimeballPool();
	private static final IBehaviour homingBehaviour = new HomingBehaviour(90, 65, false);
	private static final IBehaviour blobTrailBehaviour = new BlobTrailBehaviour(Color.GREEN);	
	
	private final MaxAgeBehaviour maxAgeBehaviour = new MaxAgeBehaviour(5f);

	private static class SlimeballPool extends Pool<Slimeball>
	{
		private int _num = 0;
		
		@Override
		protected Slimeball newObject()
		{
			if(_num >= 24)
				return null;
			else
			{
				_num++;
				return new Slimeball();
			}
		}
	}
	
	private Slimeball()
	{
		_behaviours.add(homingBehaviour);
		_behaviours.add(maxAgeBehaviour);
		_behaviours.add(blobTrailBehaviour);
		_behaviours.add(KillOnContactBehaviour.alsoDie());
	}
	
	public static void spawn(Vector2 loc, Vector2 v)
	{
		Slimeball s = pool.obtain();
		
		if(s != null)
		{		
			s._body = B2d
					.kinematicBody()
					.at(loc)
					.linearVelocity(v)
					.linearDamping(0)
					.withFixture(B2d
							.circle()
							.radius(RADIUS)
							.category(ZipZapSim.COL_CAT_METEORITE)
							.mask(ZipZapSim.COL_CAT_SHIP)
							.userData(s._fixtureTag))
					.create(Z.sim().world());
			
			Z.sim().entities().add(s);
			
			s.onSpawn();
		}
	}
	
	@Override
	public boolean isHitable()
	{
		return false;
	}

	@Override
	public Vector2[] verts()
	{
		return null;
	}

	@Override
	public Color color()
	{
		return Color.GREEN;
	}

	@Override
	public float lineWidth()
	{
		return 2f;
	}

	@Override
	public void onFree()
	{
		Z.sim().spawnExlosion(origin(), 6, Color.GREEN);
		pool.free(this);
	}

	@Override
	public int layer()
	{
		return 0;
	}

	@Override
	public float angle()
	{
		return 0;
	}
	
	@Override
	public float clipRadius()
	{
		return 4f;
	}
}
