package com.monkeysonnet.zipzap.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.zipzap.IRenderablePolygon;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;
import com.monkeysonnet.zipzap.behaviours.KillOnContactBehaviour;

public class Girder extends Enemy implements IRenderablePolygon
{
	private static final Girder2Pool pool = new Girder2Pool();
	private static final float WIDTH = 2;
	private static class Girder2Pool extends Pool<Girder>
	{
		@Override
		protected Girder newObject()
		{
			return new Girder();
		}
	}
	
	private final Vector2 _start = new Vector2();
	private final Vector2 _end = new Vector2();	
	private final Vector2[] _verts = new Vector2[4];
	private boolean _horz;
	private Fixture _fixture;
	
	private Girder()
	{		
		_behaviours.add(KillOnContactBehaviour.basic());
		
		for(int n = 0; n < 4; n++)
			_verts[n] = new Vector2();
	}
	
	public static Girder spawn(Vector2 loc, boolean horz)
	{
		Girder g = pool.obtain();
		
		g._body = B2d.staticBody().create(Z.sim().world());
		
		g._horz = horz;		
		g._start.set(loc);
		g._verts[0].set(loc).add(horz ? 0 : WIDTH/2f, horz ? WIDTH/2f : 0);
		g._verts[1].set(loc).add(horz ? 0 : -WIDTH/2f, horz ? -WIDTH/2f : 0);
		g._end.set(loc);		
		g._verts[2].set(loc).add(horz ? 0 : -WIDTH/2f, horz ? -WIDTH/2f : 0);
		g._verts[3].set(loc).add(horz ? 0 : WIDTH/2f, horz ? WIDTH/2f : 0);
		
		g.onSpawn();
		
		Z.sim().entities().add(g);
		
		return g;
	}
	
	public void extend(Vector2 end)
	{
		if(end.equals(_start))
			return;
		
		_end.set(end);
		
		_verts[2].set(_end).add(_horz ? 0 : -WIDTH/2f, _horz ? -WIDTH/2f : 0);
		_verts[3].set(_end).add(_horz ? 0 : WIDTH/2f, _horz ? WIDTH/2f : 0);
		
		if(_fixture != null)
			_body.destroyFixture(_fixture);
		
		_fixture = B2d
				.box(Math.abs(_end.x - _start.x)/2f, Math.abs(_end.y - _start.y)/2f)
				.at((_start.x + _end.x)/2f, (_start.y + _end.y)/2f)
				.category(ZipZapSim.COL_CAT_METEORITE)
				.mask(ZipZapSim.COL_CAT_SHIP)
				.userData(_fixtureTag)
				.sensor(true)
				.create(_body);
	}
	
	@Override
	protected void onFree()
	{
		_fixture = null;
		pool.free(this);
	}

	@Override
	public float angle()
	{
		// TODO Auto-generated method stub
		return 0;
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
	public float clipRadius()
	{
		return 120f;
	}
}
