package com.monkeysonnet.zipzap.entities;

import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.equations.Quad;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.zipzap.IRenderableTexture;
import com.monkeysonnet.zipzap.Z;

public class MinerDude extends Enemy implements IRenderableTexture
{
	private static final float RADIUS = 2f;
	private static final TextureRegion tex = Z.texture("man");
	
	private Vector2 _origin;	
	private Timeline _timeline;
	private Color _color;
	
	private MinerDude(){}
	
	public static void spawn(Vector2 loc, Color color)
	{
		MinerDude d = new MinerDude();
		
		d._color = color;
		
		d._origin = Z.sim().vector().obtain().set(loc).add(0, RADIUS);
		
		d._timeline = Timeline.createSequence()
				.push(Tween.to(d._origin, 0, 200)
						.target(d._origin.x, d._origin.y + (RADIUS/2f))
						.ease(Quad.IN))
				.push(Tween.to(d._origin, 0, 200)
					.target(d._origin.x, d._origin.y)
					.ease(Quad.IN))
				.repeat(Tween.INFINITY, 500)
				.delay(Game.Dice.nextFloat() * 1000f)
				.start(Z.sim().tweens());
		
		d.onSpawn();
		
		Z.sim().entities().add(d);
	}
	
	private void killTween()
	{
		if(_timeline != null)
		{
			_timeline.kill();
			_timeline = null;
		}
	}

	@Override
	public float radius()
	{
		return RADIUS;
	}

	@Override
	public TextureRegion texture()
	{
		return tex;
	}

	@Override
	public Color color()
	{
		return _color;
	}
	
	@Override
	protected void onFree()
	{
		killTween();
		Z.sim().vector().free(_origin);
	}
	
	@Override
	public Vector2 origin()
	{
		return _origin;
	}
	
	@Override
	public float angle()
	{
		return 0;
	}
}
