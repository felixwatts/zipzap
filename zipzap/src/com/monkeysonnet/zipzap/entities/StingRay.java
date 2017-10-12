package com.monkeysonnet.zipzap.entities;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.primitives.MutableFloat;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.ICallback;
import com.monkeysonnet.engine.editor.Map;
import com.monkeysonnet.zipzap.Tools;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.behaviours.BlobTrailBehaviour;
import com.monkeysonnet.zipzap.behaviours.IBehaviour;
import com.monkeysonnet.zipzap.behaviours.ProximityBehaviour;
import com.monkeysonnet.zipzap.script.IActiveCount;

public class StingRay extends EnemyBasic
{
	private static final float SPEED = 35;
	private static final float TURN_CHANCE = 1f/60f;
	private static final float MIN_TURN_ANGLE = 45;
	private static final float TURN_ANGLE_VARIANCE = 90f;
	private static final float MIN_TURN_TIME = 0.5f;
	private static final float TURN_TIME_VARIANCE = 1f;
	private static final float SCALE = 2f;
	private static final int KILL_SCORE = 75;
	private static final float SPAWN_SHIELD_CHANCE = 0.2f;
	
	private static Color color = new Color(0f, 1f, 102f/255f, 1f);
	
	private final  ICallback fireCallback = new ICallback()
	{		
		@Override
		public void callback(Object arg)
		{
			Stinger.spawn(origin());
		}
	};
	
	private final IBehaviour proximityBehaviour = new ProximityBehaviour(-360f, 360f, 25, 3f, fireCallback); //135f, 225f, 15f, 0.1f, fireCallback );
	
	private static final Map map = new Map("sting-ray.v", SCALE, 180f);
	
	private static int _activeCount;	
	public static final IActiveCount activeCount = new IActiveCount()
	{
		
		@Override
		public int activeCount()
		{
			return _activeCount;
		}
	};
	
	private static final StingRayPool pool = new StingRayPool();	
	private static class StingRayPool extends Pool<StingRay>
	{
		@Override
		protected StingRay newObject()
		{
			return new StingRay();
		}
	}
	
	private StingRay()
	{
		_behaviours.add(proximityBehaviour);
		_behaviours.add(new BlobTrailBehaviour(Color.CYAN, 0.1f, 0.5f, map.point("jet-left").point, 0.5f, 0f));
		_behaviours.add(new BlobTrailBehaviour(Color.CYAN, 0.1f, 0.5f, map.point("jet-right").point, 0.5f, 0f));
		
		_killScore = KILL_SCORE;
	}
	
	protected final MutableFloat _angle = new MutableFloat(0);
	
	public static void spawn()
	{
		StingRay s = pool.obtain();
		s.setup(map, SPEED);
		_activeCount++;
		
		
//		float turnAngle = (float)Game.Dice.nextGaussian() * TURN_ANGLE_VARIANCE;
//		if(turnAngle < 0)
//			turnAngle -= MIN_TURN_ANGLE;
//		else turnAngle += MIN_TURN_ANGLE;
//		
//		Tween.to(s._angle, 0, 5000).target(s._angle.floatValue() + turnAngle).start(Z.sim.tweens());
	}
	
	@Override
	public void update(float dt)
	{
		super.update(dt);
		
		if(!_dead)
		{
			_body.setLinearVelocity(Vector2.tmp.set(SPEED, 0).rotate(_angle.floatValue()));
		}
		
		if(Game.Dice.nextFloat() < TURN_CHANCE)
		{
			Z.sim.tweens().killTarget(_angle);
			
			float turnAngle = (float)Game.Dice.nextGaussian() * TURN_ANGLE_VARIANCE;
			if(turnAngle < 0)
				turnAngle -= MIN_TURN_ANGLE;
			else turnAngle += MIN_TURN_ANGLE;
			float turnTime = MIN_TURN_TIME + Game.Dice.nextFloat() * TURN_TIME_VARIANCE;
			
			Tween.to(_angle, 0, turnTime * 1000f).target(_angle.floatValue() + turnAngle).start(Z.sim.tweens());
		}
	}
	
	@Override
	protected void onFree()
	{
		Z.sim.tweens().killTarget(_angle);
		pool.free(this);
		_activeCount--;
		
		if(_killed && Tools.prob(SPAWN_SHIELD_CHANCE))
			PowerUp.spawn(origin(), PowerUp.TYPE_SHIELD);
	}
	
	@Override
	public Color color(int poly)
	{
		return color;
	}
}
