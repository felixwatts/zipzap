package com.monkeysonnet.zipzap.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.zipzap.IRenderableTexture;
import com.monkeysonnet.zipzap.Z;

public class Decal implements IEntity, IRenderableTexture
{
	private static final DecalPool pool = new DecalPool();
	private static class DecalPool extends Pool<Decal>
	{
		@Override
		protected Decal newObject()
		{
			return new Decal();
		}	
	}
	
	private Decal(){}
	
	private final Vector2 _loc = new Vector2();
	private float _radius;
	private TextureRegion _tex;
	private final Color _color = new Color();
	
	public static Decal spawn(TextureRegion tex, float radius, Vector2 loc, Color color)
	{
		Decal d = pool.obtain();
		
		d._loc.set(loc);
		d._radius = radius;
		d._tex = tex;
		d._color.set(color);
		
		Z.sim().entities().add(d);
		
		return d;
	}

	@Override
	public float radius()
	{
		return _radius;
	}

	@Override
	public Vector2 origin()
	{
		return _loc;
	}

	@Override
	public TextureRegion texture()
	{
		return _tex;
	}

	@Override
	public Color color()
	{
		return _color;
	}

	@Override
	public float angle()
	{
		return 0;
	}

	@Override
	public void update(float dt)
	{
		// no op;
	}

	@Override
	public void free()
	{
		Z.sim().entities().removeValue(this, true);
		pool.free(this);
	}

	@Override
	public int layer()
	{
		return 0;
	}

}
