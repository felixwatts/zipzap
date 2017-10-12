package com.monkeysonnet.zipzap.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.zipzap.IRenderablePolygon;
import com.monkeysonnet.zipzap.Tools;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;

public class FallingRock extends Enemy implements IRenderablePolygon
{
	private static final FallingRockPool pool = new FallingRockPool(); 
	private static class FallingRockPool extends Pool<FallingRock>
	{
		@Override
		protected FallingRock newObject()
		{
			return new FallingRock();
		}
	}
	
	private Vector2[] _verts;
	//private Array<Vector2> _vertsArr;
	
	private FallingRock()
	{
	}
	
	public static FallingRock spawn(Vector2[] verts)
	{
		FallingRock r = pool.obtain();
		
		r._verts = verts;
		
		r._body = B2d
				.staticBody()
				.withFixture(B2d
						.polygon(verts)
						.category(ZipZapSim.COL_CAT_METEORITE)
						.mask(ZipZapSim.COL_CAT_METEORITE | ZipZapSim.COL_CAT_SHIP)
						.userData(r._fixtureTag))
				.create(Z.sim().world());
		
		r.onSpawn();
		
		Z.sim().entities().add(r);
		
		return r;
	}
	
	public void fall()
	{		
		Z.sim().spawnCloud(Tools.centre(_verts), 4, color(), 8f);
		
		Z.sim().world().destroyBody(_body);
		_body = B2d
				.dynamicBody()
				.fixedRotation(false)
				.withFixture(B2d
						.polygon(_verts)
						.category(ZipZapSim.COL_CAT_METEORITE)
						.mask(ZipZapSim.COL_CAT_METEORITE | ZipZapSim.COL_CAT_SHIP)
						.userData(_fixtureTag))
				.create(Z.sim().world());
	}

	@Override
	public float angle()
	{
		return (float)Math.toDegrees(_body.getAngle());
	}

	@Override
	public Vector2[] verts()
	{
		return _verts;
	}

	@Override
	public Color color()
	{
		return Color.GRAY;
	}

	@Override
	public float lineWidth()
	{
		return 1f;
	}
	
	@Override
	public void onBeginContact(Contact c, Fixture me, Fixture other)
	{
		if(other.getBody().getUserData() == Z.ship())
		{
			Z.ship().strike();
		}
	}
	
	@Override
	protected void onFree()
	{
		pool.free(this);
	}
	
	@Override
	public boolean isHitable()
	{
		return false;
	}
	
	@Override
	public float clipRadius()
	{
		return 4f;
	}
}
