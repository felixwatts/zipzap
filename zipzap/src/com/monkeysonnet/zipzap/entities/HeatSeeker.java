package com.monkeysonnet.zipzap.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.engine.editor.Map;
import com.monkeysonnet.engine.editor.Shape;
import com.monkeysonnet.zipzap.IRenderableMultiPolygon;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;
import com.monkeysonnet.zipzap.behaviours.BlobTrailBehaviour;
import com.monkeysonnet.zipzap.behaviours.DieOnHitBehaviour;
import com.monkeysonnet.zipzap.behaviours.FaceDirectionOfTravelBehaviour;
import com.monkeysonnet.zipzap.behaviours.HomingBehaviour;
import com.monkeysonnet.zipzap.behaviours.KillOnContactBehaviour;
import com.monkeysonnet.zipzap.behaviours.MaxAgeBehaviour;

public class HeatSeeker extends Enemy implements IRenderableMultiPolygon
{
	private static final HeatSeekerPool pool = new HeatSeekerPool(); 
	private static class HeatSeekerPool extends Pool<HeatSeeker>
	{
		@Override
		protected HeatSeeker newObject()
		{
			return new HeatSeeker();
		}
	}
	
	private static final Map map = new Map("heat-seeker.v", 2f, 180);
	private static final Color[] colors = new Color[map.numShapes()];
	
	private static final HomingBehaviour homingBehaviour = new HomingBehaviour(90, 60, false);
	private static final BlobTrailBehaviour blobTrailBehaviour = new BlobTrailBehaviour(Color.ORANGE, 0.05f, 0.25f);
	private static final DieOnHitBehaviour dieOnHitBehaviour = new DieOnHitBehaviour(Color.ORANGE, 12, false, Color.ORANGE, 0);
	
	private static final float SPEED = 30;
	//private static final int SFX_FLY = 35;
	
	static
	{
		for(int n = 0; n < map.numShapes(); n++)
		{
			if(map.shape(n).properties.equals("white"))
				colors[n] = Color.WHITE;
			else colors[n] = Color.RED;
		}
	}
	
	private HeatSeeker()
	{
		_behaviours.add(homingBehaviour);
		_behaviours.add(blobTrailBehaviour);
		_behaviours.add(KillOnContactBehaviour.alsoDie());
		_behaviours.add(FaceDirectionOfTravelBehaviour.instance());
		_behaviours.add(new MaxAgeBehaviour(8f));
		_behaviours.add(dieOnHitBehaviour);
		//_behaviours.add(new FlyingSoundBehaviour(SFX_FLY));
	}
	
	public static void spawn(Vector2 loc, float angle)
	{
		HeatSeeker s = pool.obtain();
		
		s._body = B2d
				.kinematicBody()
				.at(loc)
				.rotated((float)Math.toRadians(angle))
				.linearVelocity(Z.v1().set(SPEED, 0).rotate(angle))
				.withFixture(B2d
						.edge()
						.between(-1.5f, 0, 1.5f, 0)
						.category(ZipZapSim.COL_CAT_METEORITE)
						.mask(ZipZapSim.COL_CAT_SHIP)
						.userData(s._fixtureTag))
				.create(Z.sim().world());
		
		s.onSpawn();
		
		Z.sim().entities().add(s);
	}
	
	@Override
	protected void onFree()
	{
		pool.free(this);
	}

	@Override
	public int getNumPolys()
	{
		return map.numShapes();
	}

	@Override
	public float angle(int poly)
	{
		return angle();
	}

	@Override
	public Vector2 origin(int poly)
	{
		return origin();
	}

	@Override
	public Vector2[] verts(int poly)
	{
		return map.shape(poly).shape;
	}

	@Override
	public Color color(int poly)
	{
		return colors[poly];
	}

	@Override
	public float lineWidth(int poly)
	{
		return 1f;
	}
	
	@Override
	public boolean isLoop(int poly)
	{
		return map.shape(poly).type == Shape.TYPE_LOOP;
	}
	
	
	@Override
	public float clipRadius()
	{
		return 4f;
	}
}
