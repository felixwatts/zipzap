package com.monkeysonnet.zipzap.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.zipzap.IRenderablePolygon;
import com.monkeysonnet.zipzap.Z;

public class Laser implements IRenderablePolygon, IEntity
{
	private static final LaserPool pool = new LaserPool();
	private static final Vector2 _origin = new Vector2();
	private static final float MAX_AGE = 0.25f;
	private static final float WIDTH_FACTOR = 2f;
	private Vector2[] _verts;
	private Color _color;
	private float _age;
	private float _width;
	
	private static class LaserPool extends Pool<Laser>
	{
		@Override
		protected Laser newObject()
		{
			return new Laser();
		}
	}
	
	public static void spawn(Vector2 v1, Vector2 v2, Color c, float width)
	{
		Laser l = pool.obtain();
		l.init(v1, v2, c, width);
		Z.sim().entities().add(l);
	}
	
	private Laser()
	{
		_verts = new Vector2[2];
		_verts[0] = new Vector2();
		_verts[1] = new Vector2();
		_color = new Color(Color.RED);
	}
	
	private void init(Vector2 v1, Vector2 v2, Color c, float width)
	{
		_verts[0].set(v1);
		_verts[1].set(v2);
		_color.set(c);
		_age = 0;
		_width = width;
	}

	@Override
	public float angle()
	{
		return 0;
	}

	@Override
	public Vector2 origin()
	{
		return _origin;
	}

	@Override
	public Vector2[] verts()
	{
		return _verts;
	}

	@Override
	public Color color()
	{
		return _color;
	}

	@Override
	public float lineWidth()
	{
		return _width + ((_age / MAX_AGE) * WIDTH_FACTOR);
	}

	@Override
	public void update(float dt)
	{
		_age += dt;
		if(_age > MAX_AGE)
			free();
		
		_color.a = 1 - (_age / MAX_AGE);
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
		return 2;
	}
		
	@Override
	public float clipRadius()
	{
		return Float.POSITIVE_INFINITY; // todo hack
	}
}
