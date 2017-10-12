package com.monkeysonnet.zipzap.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.zipzap.ElectricBeamWidth;
import com.monkeysonnet.zipzap.IRenderableMultiPolygon;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;
import com.monkeysonnet.zipzap.behaviours.KillOnContactBehaviour;

public class LaserBeam extends Entity implements IRenderableMultiPolygon
{
	private static final float LENGTH = ZipZapSim.SPAWN_DISTANCE * 3;
	
	public static final int MODE_OFF = 0;
	public static final int MODE_WARM_UP = 1;
	public static final int MODE_ON = 2;
	
	private static final Vector2 origin = new Vector2(0, 0);
	
	private static final LaserBeamPool pool = new LaserBeamPool();
	private static class LaserBeamPool extends Pool<LaserBeam>
	{
		@Override
		protected LaserBeam newObject()
		{
			return new LaserBeam();
		}
	}
	
	public static final ElectricBeamWidth beamWidth = new ElectricBeamWidth();

	private final Color _color = new Color();
	private final Vector2[] _verts = new Vector2[2];
	private int _mode;
	
	public LaserBeam()
	{
		_behaviours.add(KillOnContactBehaviour.basic());
	}
	
	public static LaserBeam spawn(Color color)
	{
		LaserBeam b = pool.obtain();
		
		b._mode = MODE_OFF;
		b._color.set(color);
		b._verts[0] = Z.sim().vector().obtain();
		b._verts[1] = Z.sim().vector().obtain();
		
		b._body = B2d
				.staticBody()
				.at(0, 0)
				.active(false)
				.withFixture(B2d
						.edge()
						.between(0, 0, LENGTH, 0)
						.category(ZipZapSim.COL_CAT_METEORITE)
						.mask(ZipZapSim.COL_CAT_SHIP)
						.userData(b._fixtureTag))
				.create(Z.sim().world());
		
		Z.sim().entities().add(b);
		
		b.onSpawn();
		
		return b;
	}
	
	public void set(Vector2 root, float angle)
	{
		_verts[0].set(root);
		_verts[1].set(LENGTH, 0).rotate(angle).add(root);
		
		_body.setTransform(root, (float)Math.toRadians(angle));
	}
	
	public void mode(int mode)
	{
		_mode = mode;
		_body.setActive(_mode == MODE_ON);
		_color.set(_color.r, _color.g, _color.b, _mode == MODE_WARM_UP ? 0.2f : 0.8f);
	}
	
	public int mode()
	{
		return _mode;
	}
	
	@Override
	protected void onFree()
	{
		Z.sim().vector().free(_verts[0]);
		Z.sim().vector().free(_verts[1]);
		_verts[0] = null;
		_verts[1] = null;
		pool.free(this);
	}

	@Override
	public int getNumPolys()
	{
		switch(_mode)
		{
			case MODE_OFF:
			default:
				return 0;
			case MODE_WARM_UP:
				return 1;
			case MODE_ON:
				return 2;
		}
	}

	@Override
	public float angle(int poly)
	{
		return 0;
	}

	@Override
	public Vector2 origin(int poly)
	{
		return origin;
	}

	@Override
	public Vector2[] verts(int poly)
	{
		return _verts;
	}

	@Override
	public Color color(int poly)
	{
		switch(poly)
		{
			case 0:
			default:
				return _color;
			case 1:
				return Color.WHITE;
		}
	}

	@Override
	public float lineWidth(int poly)
	{
		switch(poly)
		{
			case 0:
			default:
				switch(_mode)
				{
					case MODE_WARM_UP:
						return beamWidth.beamWidth() / 2f;
					case MODE_ON:
					default:
						return beamWidth.beamWidth();
				}				
			case 1:
				return 1f;
		}
	}
	
	@Override
	public boolean isHitable()
	{
		return false;
	}
	
	@Override
	public boolean isLoop(int poly)
	{
		return false;
	}
		
	@Override
	public float clipRadius()
	{
		return Float.POSITIVE_INFINITY;
	}
}
