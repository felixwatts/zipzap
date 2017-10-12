package com.monkeysonnet.zipzap.entities;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.equations.Quad;
import aurelienribon.tweenengine.primitives.MutableFloat;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.zipzap.IAttractor;
import com.monkeysonnet.zipzap.IRenderablePolygon;
import com.monkeysonnet.zipzap.Z;

public class Attracticle implements IEntity, IRenderablePolygon
{
	private static final AtracticlePool pool = new AtracticlePool();
	private static class AtracticlePool extends Pool<Attracticle>
	{
		@Override
		protected Attracticle newObject()
		{
			return new Attracticle();
		}
	}

	private TweenCallback tweenCompleteCallback = new TweenCallback()
	{		
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			_tween = null;
			free();
		}
	};
	
	private final Vector2 _origin = new Vector2(), _start = new Vector2();
	private MutableFloat _pos = new MutableFloat(0);
	private IAttractor _target;
	private final Color _color = new Color();
	private static final float MIN_RADIUS = 0.0f;
	private static final float MAX_RADIUS = 1f;	
	private static final float SPAWN_RADIUS = 12f;
	private Tween _tween;
	private boolean _type1;
	
	private Attracticle(){}
	
	public static void spawn(IAttractor target, Color color)
	{
		spawn(null, target, color);
	}
	
	public static Attracticle spawn(Vector2 start, IAttractor target, Color color)
	{
		return spawn(start, target, color, 1000f, SPAWN_RADIUS);
	}
	
	public static Attracticle spawn(Vector2 start, IAttractor target, Color color, float arriveTimeMs, float spawnRadius)
	{
		Attracticle a = pool.obtain();
		
		a._type1 = start != null;
		a._target = target;
		a._color.set(color);
		
		a._start.set(spawnRadius * Game.Dice.nextFloat(), 0).rotate(Game.Dice.nextFloat()*360f);//.add(target.origin());
		
		if(start != null)
			a._start.add(start);
		
		a._origin.set(a._start);
		a._pos.setValue(0);
		a._tween = Tween
				.to(a._pos, 0, arriveTimeMs)
				.target(1)
				.ease(Quad.IN)
				.setCallbackTriggers(TweenCallback.COMPLETE)
				.setCallback(a.tweenCompleteCallback ) 
				.start(Z.sim.tweens());
		
		Z.sim.entities().add(a);
		
		return a;
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
		return null;
	}

	@Override
	public Color color()
	{
		return _color;
	}

	@Override
	public float lineWidth()
	{
		if(_type1)
			return 0.5f;
		else return MIN_RADIUS + ((MAX_RADIUS - MIN_RADIUS) * _pos.floatValue());
	}

	@Override
	public void update(float dt)
	{
		if(_target.dead())
			free();
		else
		{		
			if(_type1)
				_origin.set(_target.origin()).sub(_start).mul(_pos.floatValue()).add(_start);
			else
				_origin.set(_start).mul(1-_pos.floatValue()).add(_target.origin());
		}
	}

	@Override
	public void free()
	{
		if(_tween != null)
		{
			_tween.kill();
			_tween = null;
		}
		
		Z.sim.entities().removeValue(this, true);
		pool.free(this);
	}

	@Override
	public int layer()
	{
		return 0;
	}
	
	@Override
	public float clipRadius()
	{
		return lineWidth();
	}
}
