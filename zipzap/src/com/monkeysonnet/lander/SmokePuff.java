package com.monkeysonnet.lander;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.zipzap.IRenderableSprite;
import com.monkeysonnet.zipzap.Z;

public class SmokePuff implements IEntity, IRenderableSprite
{
	private static final float MAX_AGE = 1.5f;
	private static final float ANGULAR_VEL_VARIANCE = 360f;
	private static final float SIZE = 3f;
	
	private static final Color colorStartDefault = new Color(1f, 1f, 1f, 1f/3f);
	private static final Color colorEndDefault = new Color(1f, 1f, 1f, 0f);
	
	private static final Color colorStartExplosion = new Color(255f/255f, 254f/255f, 154f/255f, 1f);
	private static final Color colorEndExplosion = new Color(79f/255f, 79f/255f, 79f/255f, 0f);
	
	private final Color _startColor = new Color(), _endColor = new Color();
	
	private static final TextureRegion _tex1 = Z.texture("jetpak-smoke-1"), _tex2  = Z.texture("jetpak-smoke-2");
	
	private static final SmokePuffPool pool = new SmokePuffPool();
		
		
	private static class SmokePuffPool extends Pool<SmokePuff>
	{
		@Override
		protected SmokePuff newObject()
		{
			return new SmokePuff();
		}
	}
	
	private final Sprite _sprite = new Sprite();
	private float _angularVel;	
	private float _age;
	private final Vector2 _vel = new Vector2();
	private float _startScale;
	
	private SmokePuff()
	{
		_sprite.setSize(SIZE, SIZE);
		_sprite.setOrigin(SIZE / 2f, SIZE / 2f);
	}
	
	public static void spawnExplosion(Vector2 loc)
	{
		for(float x = -2f; x <= 2f; x += 0.1f)
		{
			Vector2 l = Z.v1();
			l.set(loc).add(x, 0);
			
			Vector2 v = Z.v2();
			v.set(x, (7.5f + (Game.Dice.nextFloat() * 2.5f)) + (2.5f * (2f-Math.abs(x))));
			
			float av = -x * 90f;
			
			spawn(l, v, av, colorStartExplosion, colorEndExplosion, 1f);
		}
	}
	
	public static void spawn(Vector2 loc, Vector2 vel)
	{
		spawn(loc, vel, (float)Game.Dice.nextGaussian() * ANGULAR_VEL_VARIANCE, colorStartDefault, colorEndDefault, 1f);
	}
	
	public static void spawn(Vector2 loc, Vector2 vel, float angularVel, Color colStart, Color colEnd, float startScale)
	{
		SmokePuff p = pool.obtain();
		
		p._sprite.setRegion(Game.Dice.nextBoolean() ? _tex1 : _tex2);		
		p._sprite.setPosition(loc.x - (SIZE/2f), loc.y - (SIZE/2f));
		p._angularVel = angularVel;
		p._age = 0;	
		p._vel.set(vel);
		p._startColor.set(colStart);
		p._endColor.set(colEnd);
		p._startScale = startScale;
		
		Z.sim.entities().add(p);
	}

	@Override
	public Sprite sprite()
	{
		return _sprite;
	}

	@Override
	public void update(float dt)
	{
		_age += dt;
		if(_age > MAX_AGE)
			free();
		else
		{
			_sprite.rotate(dt * _angularVel);
			_sprite.setColor(1f, 1f, 1f, (1 - (_age / MAX_AGE)) / 3f);
			_sprite.setScale(_startScale + ((_age / MAX_AGE) * 2f));
			_sprite.translate(_vel.x * dt, _vel.y * dt);
		}
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
		return 2;
	}
}
