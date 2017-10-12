package com.monkeysonnet.zipzap.entities;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.equations.Quad;
import aurelienribon.tweenengine.primitives.MutableFloat;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.engine.editor.Map;
import com.monkeysonnet.zipzap.IAttractor;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.behaviours.DieOnRangeBehaviour;
import com.monkeysonnet.zipzap.behaviours.FaceDirectionOfTravelBehaviour;
import com.monkeysonnet.zipzap.script.IActiveCount;

public class Enterprise extends EnemyBasic implements IAttractor
{
	private static final Map map = new Map("enterprise.v", 1f, 180);
	
	private static final EnterprisePool pool = new EnterprisePool();

	private static final float START_ANGLE = 0;
	private static final float START_HEIGHT = 70;
	private static final float END_HEIGHT = 30;
	private static final float APPEAR_TIME = 4f;
	private static final float CHARGE_TIME = 1.5f;
	private static final int KILL_SCORE = 75;
	
	private static class EnterprisePool extends Pool<Enterprise>
	{
		@Override
		protected Enterprise newObject()
		{
			return new Enterprise();
		}
	}

	private static int _activeCount;

	public static final IActiveCount activeCount = new IActiveCount()
	{		
		@Override
		public int activeCount()
		{
			return _activeCount;
		}
	};

	private final TweenCallback beginChargeCallback = new TweenCallback()
	{		
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			_charging = true;
			_beam.mode(LaserBeam.MODE_WARM_UP);
		}
	};

	private TweenCallback timelineCompleteCallback = new TweenCallback()
	{		
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			_timeline = null;
		}
	};
	
	private Timeline _timeline;
	private boolean _charging;
	private LaserBeam _beam;
	private MutableFloat _orbitalAngle = new MutableFloat(0), _orbitalHeight = new MutableFloat(0), _faceAngle = new MutableFloat(0);

	private final TweenCallback fireCallback = new TweenCallback()
	{
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			_beam.mode(LaserBeam.MODE_ON);
			_charging = false;
		}
	};
	
	private Enterprise()
	{
		_behaviours.removeValue(FaceDirectionOfTravelBehaviour.instance(), true);
		_behaviours.removeValue(DieOnRangeBehaviour.instance(), true);
		
		_killScore = KILL_SCORE;
	}
	
	public static void spawnSquadron(int num)
	{
		for(int n = 0; n < num; n++)
			spawn(n, num);
	}
	
	public static void spawn(int position, int num)
	{
		Enterprise e = pool.obtain();
		
		e.setup(map, 0);
		
		e._charging = false;
		
		e._orbitalAngle.setValue(START_ANGLE);
		e._orbitalHeight.setValue(START_HEIGHT);
		e._faceAngle.setValue(START_ANGLE+90);
		
		e._beam = LaserBeam.spawn(e.color(0));
		
		float targetAngle = 360-(position * (360f/num));
		
		e._timeline = Timeline.createSequence()
				.pushPause(position * 500)
				.push(Timeline.createParallel()
						.push(Tween.to(e._orbitalHeight, 0, APPEAR_TIME*1000f).target(END_HEIGHT))
						.push(Tween.to(e._orbitalAngle, 0, APPEAR_TIME*1000f).target(targetAngle).ease(Quad.OUT))
						.push(Tween.to(e._faceAngle, 0, APPEAR_TIME*1000f).target(targetAngle+90).ease(Quad.OUT)))
				.push(Tween.call(e.beginChargeCallback))
				.push(Tween.to(e._faceAngle, 0, CHARGE_TIME*1000f).target(targetAngle+180))
				.push(Tween.call(e.fireCallback))
				.setCallbackTriggers(TweenCallback.COMPLETE)
				.setCallback(e.timelineCompleteCallback)
				.start(Z.sim.tweens());
		
		_activeCount++;
	}
	
	@Override
	public void update(float dt)
	{
		super.update(dt);
		
		if(!_dead)
		{
			_body.setTransform(origin(), (float)Math.toRadians(_faceAngle.floatValue()));
			
			if(_charging)
			{
				Attracticle.spawn(this, color(0));
			}
			
			if(_beam.mode() != LaserBeam.MODE_OFF)
			{
				_beam.set(origin(), angle());
			}
		}
	}
	
	@Override
	protected void onFree()
	{
		if(_timeline != null)
		{
			_timeline.kill();
			_timeline = null;
		}
		
		_beam.free();
		
		pool.free(this);
		
		_activeCount--;
	}
	
	@Override
	public Vector2 origin()
	{
		return Vector2.tmp.set(_orbitalHeight.floatValue(), 0).rotate(_orbitalAngle.floatValue()).add(Z.ship().origin());
	}
}
