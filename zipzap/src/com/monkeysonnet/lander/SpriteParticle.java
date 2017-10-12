package com.monkeysonnet.lander;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.zipzap.IRenderableTexture;
import com.monkeysonnet.zipzap.Z;

public class SpriteParticle implements IEntity, IRenderableTexture
{
	private static final SpriteParticlePool pool = new SpriteParticlePool();
	private static class SpriteParticlePool extends Pool<SpriteParticle>
	{
		@Override
		protected SpriteParticle newObject()
		{
			return new SpriteParticle();
		}
	}
	
	private TextureRegion _tex;
	private float _maxAge;
	private float _radius;
	private final Vector2 _origin = new Vector2();
	private final Color _color = new Color();
	private float _angle;
	private float _age;

	public static void spawn(Vector2 loc, TextureRegion tex, float maxAge, float radius, float angle)
	{
		SpriteParticle p = pool.obtain();
		
		p._maxAge = maxAge;
		p._radius = radius;
		p._origin.set(loc);
		p._color.set(Color.WHITE);
		p._age = 0;
		p._angle = angle;
		p._tex = tex;
		
		Z.sim.entities().add(p);
	}
	
	private SpriteParticle(){}
	
	@Override
	public float radius()
	{
		return _radius;
	}

	@Override
	public Vector2 origin()
	{
		return _origin;
	}

	@Override
	public TextureRegion texture()
	{
		return _tex;
	}

	@Override
	public Color color()
	{
		return _color ;
	}

	@Override
	public float angle()
	{
		return _angle;
	}

	@Override
	public void update(float dt)
	{
		_age += dt;
		if(_age > _maxAge)
			free();
		else
		{		
			float a = 1 - (_age / _maxAge);
			_color.set(1, 1, 1, a);
		}
	}

	@Override
	public void free()
	{
		Z.sim.entities().removeValue(this, true);
	}

	@Override
	public int layer()
	{
		return 0;
	}

}
