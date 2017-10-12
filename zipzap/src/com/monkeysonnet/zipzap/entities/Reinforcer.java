package com.monkeysonnet.zipzap.entities;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.equations.Quad;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.zipzap.IRenderablePolygon;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;

public class Reinforcer implements IEntity, IRenderablePolygon
{
	private Vector2 _origin;	
	
	public Reinforcer()
	{
		_origin = new Vector2().set(ZipZapSim.SPAWN_DISTANCE, 0).rotate(Z.ship().angle() + 180).add(Z.ship().origin());
		
		Tween
			.to(_origin, 0, 1000)
			.target(Z.ship().origin().x, Z.ship().origin().y)
			.ease(Quad.OUT)
			.setCallbackTriggers(TweenCallback.COMPLETE)
			.setCallback(callbackComplete)
			.start(Game.TweenManager);
		
		Z.sim.entities().add(this);
	}
	
	private final TweenCallback callbackComplete = new TweenCallback()
	{
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			free();
			Z.ship().comeAlive();
		}		
	};

	@Override
	public float angle()
	{
		return Z.ship().angle();
	}

	@Override
	public Vector2 origin()
	{
		return _origin;
	}

	@Override
	public Vector2[] verts()
	{
		return Ship.verts;
	}

	@Override
	public Color color()
	{
		return Color.RED;
	}

	@Override
	public float lineWidth()
	{
		return 1f;
	}

	@Override
	public void update(float dt)
	{
		Ghost.spawn(this, Color.RED, Color.RED, 0.5f, 0.9f);
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
	
	@Override
	public float clipRadius()
	{
		return 4f;
	}
}
