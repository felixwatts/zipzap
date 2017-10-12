package com.monkeysonnet.zipzap.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.zipzap.IRenderablePolygon;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;
import com.monkeysonnet.zipzap.behaviours.BlobTrailBehaviour;
import com.monkeysonnet.zipzap.behaviours.IBehaviour;
import com.monkeysonnet.zipzap.behaviours.KillOnContactBehaviour;
import com.monkeysonnet.zipzap.behaviours.MaxAgeBehaviour;

public class Fireball extends Enemy implements IRenderablePolygon
{
	private static final float RADIUS = 1f;	
	private static final FireballPool pool = new FireballPool();
	private static final IBehaviour blobTrailBehaviour = new BlobTrailBehaviour(Color.ORANGE);	
	
	private final MaxAgeBehaviour maxAgeBehaviour = new MaxAgeBehaviour(5f);

	private static class FireballPool extends Pool<Fireball>
	{
		private int _num = 0;
		
		@Override
		protected Fireball newObject()
		{
			if(_num >= 24)
				return null;
			else
			{
				_num++;
				return new Fireball();
			}
		}
	}
	
	protected Fireball()
	{
		_behaviours.add(maxAgeBehaviour);
		_behaviours.add(blobTrailBehaviour);
		_behaviours.add(KillOnContactBehaviour.alsoDie());
	}
	
	@Override
	public boolean isHitable()
	{
		return false;
	}
	
	public static void spawn(Vector2 loc, Vector2 v)
	{
//		if(Gdx.graphics.getFramesPerSecond() < 50)
//			return;
		
		Fireball s = pool.obtain();
		
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
	public Vector2[] verts()
	{
		return null;
	}

	@Override
	public Color color()
	{
		return Color.YELLOW;
	}

	@Override
	public float lineWidth()
	{
		return 2f;
	}

	@Override
	public void onFree()
	{
		Z.sim().spawnExlosion(origin(), 6, Color.ORANGE);
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
		return RADIUS;
	}
}