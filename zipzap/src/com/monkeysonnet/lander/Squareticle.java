package com.monkeysonnet.lander;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.engine.ColorTools;
import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.zipzap.IRenderableTexture;
import com.monkeysonnet.zipzap.Tools;
import com.monkeysonnet.zipzap.Z;

public class Squareticle implements IEntity, IRenderableTexture
{
	private static final float MEAN_LONGEVITY = 0.25f;
	private static final float SD_LONGEVITY = 0.1f;
	private static final float MEAN_RADIUS = 0.5f;
	private static final float SD_RADIUS = 0.1f;
	private static final float MEAN_Y_VEL = 8f;
	private static final float SD_Y_VEL = 1f;
	
	private float _radius;
	private float _age, _maxAge, _yVel;
	private final Vector2 _origin = new Vector2();	
	
	private static final TextureRegion texture = Z.texture("solid");	
	private static final SquareticlePool pool = new SquareticlePool();
	private static class SquareticlePool extends Pool<Squareticle>
	{
		@Override
		protected Squareticle newObject()
		{
			return new Squareticle();
		}
	}
	
	public static void spawn(Vector2 loc)
	{
		Squareticle s = pool.obtain();
		
		s._origin.set(loc);
		s._age = 0;
		s._maxAge = Tools.random(MEAN_LONGEVITY, SD_LONGEVITY);
		s._radius = Tools.random(MEAN_RADIUS, SD_RADIUS);
		s._yVel = Tools.random(MEAN_Y_VEL, SD_Y_VEL);
		
		Z.sim.entities().add(s);
	}

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
		return texture;
	}

	@Override
	public Color color()
	{
		return ColorTools.combineAlpha(Z.colorTutorial, 1 - (_age / _maxAge));
	}

	@Override
	public float angle()
	{
		return 0;
	}

	@Override
	public void update(float dt)
	{
		_age += dt;
		if(_age > _maxAge)
			free();
		else _origin.add(0, _yVel * dt);
	}

	@Override
	public void free()
	{
		Z.sim.entities().removeValue(this, true);
		pool.free(this);
	}

	@Override
	public int layer()
	{
		return 3;
	}

}
