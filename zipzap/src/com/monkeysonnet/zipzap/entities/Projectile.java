package com.monkeysonnet.zipzap.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.zipzap.IRenderableMultiPolygon;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;
import com.monkeysonnet.zipzap.behaviours.KillOnContactBehaviour;
import com.monkeysonnet.zipzap.behaviours.MaxAgeBehaviour;

public class Projectile extends Enemy implements IRenderableMultiPolygon
{
	private static final ProjectilePool pool = new ProjectilePool();
	private static final float MAX_AGE = 3f;
	private static Vector2[][] vertsArr;
	
	private int _length;
	private Color _color;
	private boolean _glow;
	
	static
	{
		vertsArr = new Vector2[3][];
		
		for(int l = 0; l < 3; l++)
		{
			float length = (l+1) * 0.5f;
			vertsArr[l] = new Vector2[2];
			vertsArr[l][0] =  new Vector2(-length, 0);
			vertsArr[l][1] =  new Vector2(length, 0);
		}
	}
	
	private Projectile()
	{
		_behaviours.add(KillOnContactBehaviour.alsoDie());
		_behaviours.add(new MaxAgeBehaviour(MAX_AGE));
	}
	
	private static class ProjectilePool extends Pool<Projectile>
	{
		@Override
		protected Projectile newObject()
		{
			return new Projectile();
		}
	}
	public static void spawn(Vector2 v, Vector2 vel)
	{
		spawn(v, vel, 0, Color.YELLOW, false);
	}
	
	public static void spawn(Vector2 v, Vector2 vel, int length, Color color, boolean glow)
	{
		Projectile p = pool.obtain();
		
		p._glow = glow;
		p._dead = false;
		p._color = color;
		p._length = length;
		
		p._body = B2d
				.kinematicBody()
				.at(v.x,v.y)
				.linearVelocity(vel)
				.rotated((float)Math.toRadians(vel.angle()))
				.withFixture(B2d
						.chain(vertsArr[length])
						.category(ZipZapSim.COL_CAT_METEORITE)
						.mask(ZipZapSim.COL_CAT_SHIP | ZipZapSim.COL_CAT_BUBBLE)
						.userData(p._fixtureTag))
				.create(Z.sim().world());
		
		Z.sim().entities().add(p);
		
		p.onSpawn();
	}

	@Override
	public int getNumPolys()
	{
		return _glow ? 2 : 1;
	}

	@Override
	public Vector2[] verts(int poly)
	{
		return vertsArr[_length];
	}

	@Override
	public Color color(int poly)
	{
		if(_glow)
		{
			switch(poly)
			{
				case 0:
					return _color;
				default:
					return Color.WHITE;
			}			
		}
		else return _color;
	}

	@Override
	public float lineWidth(int poly)
	{
		if(_glow)
		{
			switch(poly)
			{
				case 0:
					return 2f;
				default:
					return 1f;
			}			
		}
		else return 1f;
	}
	
	@Override
	protected void onFree()
	{
		pool.free(this);
	}

	@Override
	public int layer()
	{
		return 0;
	}

	@Override
	public boolean isLoop(int poly)
	{
		return false;
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
	public boolean hit(Fixture f, boolean mega, Vector2 loc, Vector2 norm)
	{
		if(mega)
			free();
		return false;
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
