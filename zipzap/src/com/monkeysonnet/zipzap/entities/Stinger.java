package com.monkeysonnet.zipzap.entities;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.zipzap.IRenderableMultiPolygon;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;
import com.monkeysonnet.zipzap.behaviours.KillOnContactBehaviour;

public class Stinger extends Enemy implements IRenderableMultiPolygon
{
	private static final StingerPool pool = new StingerPool();
	private static final float RADIUS = 1f;
//	private static final float MAX_AGE = 2f;
	private static final int NUM_FIREBALLS = 8;
	private static final float PROJECTILE_SPEED = 32f;
	private static final float FIRE_DELAY = 50f;
	private static final float FIRE_ANGLE_DELTA = 360f/(NUM_FIREBALLS + 1);// 20f;	
	private static final int SFX_LAY = -1029;
	private static final int SFX_SHOOT =  -1030;
	
	private Timeline _timeline;
	private float _fireAngle;
	
	private static class StingerPool extends Pool<Stinger>
	{
		@Override
		protected Stinger newObject()
		{
			return new Stinger();
		}
	}

	private final TweenCallback fireCallback = new TweenCallback()
	{		
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			Z.sim.fireEvent(SFX_SHOOT, null);
			Projectile.spawn(origin(), Vector2.tmp.set(PROJECTILE_SPEED, 0).rotate(_fireAngle), 0, Color.CYAN, true);
			_fireAngle += FIRE_ANGLE_DELTA;			
		}
	};
	
	private final TweenCallback timelineCompleteCallback = new TweenCallback()
	{		
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			_timeline = null;
			free();
		}
	};
	
	private Stinger()
	{
		_behaviours.add(KillOnContactBehaviour.alsoDie());
//		_behaviours.add(new MaxAgeBehaviour(MAX_AGE));
	}
	
	public static void spawn(Vector2 loc)
	{
		Stinger s = pool.obtain();
		
		s._fireAngle = 0;
		
		s._body = B2d
				.staticBody()
				.at(loc)
				.withFixture(B2d
						.circle()
						.radius(RADIUS)
						.category(ZipZapSim.COL_CAT_METEORITE)
						.mask(ZipZapSim.COL_CAT_SHIP)
						.userData(s._fixtureTag))
				.create(Z.sim().world());
		
		Z.sim().entities().add(s);
		
		s.onSpawn();
		
		s._timeline = Timeline.createSequence()
				.delay(1000)
				.push(Tween.call(s.fireCallback))
				.repeat(NUM_FIREBALLS, FIRE_DELAY)
				.setCallback(s.timelineCompleteCallback)
				.setCallbackTriggers(TweenCallback.COMPLETE)
				.start(Z.sim.tweens());
		
		Z.sim.fireEvent(SFX_LAY, null);
	}
	
	@Override
	protected void onFree()
	{
		pool.free(this);
		if(_timeline != null)
			_timeline.kill();
	}

	@Override
	public int getNumPolys()
	{
		return 2;
	}

	@Override
	public float angle(int poly)
	{
		return 0;
	}

	@Override
	public Vector2 origin(int poly)
	{
		return _body.getPosition();
	}

	@Override
	public Vector2[] verts(int poly)
	{
		return null;
	}

	@Override
	public Color color(int poly)
	{
		return poly == 0 ? Color.CYAN : Color.WHITE;
	}

	@Override
	public float lineWidth(int poly)
	{
		return poly == 0 ? LaserBeam.beamWidth.beamWidth() : 1f;
	}

	@Override
	public boolean isLoop(int poly)
	{
		return false;
	}

	@Override
	public float clipRadius()
	{
		return 3f;
	}
}
