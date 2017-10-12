package com.monkeysonnet.zipzap.entities;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.primitives.MutableFloat;

import com.badlogic.gdx.math.Vector2;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.IFactory;
import com.monkeysonnet.engine.editor.Map;
import com.monkeysonnet.zipzap.Tools;
import com.monkeysonnet.zipzap.Z;


public abstract class Congoid extends EnemyBasic
{
	private static final int MAX_CONGA_SIZE = 16;
	private static final Timeline[] _tmpArr = new Timeline[MAX_CONGA_SIZE];
	private static final Timeline[] _tmpArr3 = new Timeline[MAX_CONGA_SIZE];
	private static final Congoid[] _tmpArr2 = new Congoid[MAX_CONGA_SIZE];
	
	private TweenCallback callbackGo = new TweenCallback()
	{
		
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			if(!_dead)
			{
				_speed = (Float)source.getUserData();
				//_body.setLinearVelocity(Vector2.tmp.set(speed, 0).rotate(Tools.angleToShip(origin())));
			}
		}
	};
	
	protected final MutableFloat _angle = new MutableFloat(0);
	private float _speed;
	
	protected Congoid(){}
	
	public static void spawnConga(IFactory<Congoid> factory, int num, Map head, Map node, float speed, float delay, float minTurnTime, float tunrTimeVariance, float minTurnAngle, float angleVariance, int numTurns)
	{
		Vector2 loc = Tools.randomSpawnLoc();
		
		for(int n = 0; n < num; n++)
		{
			Congoid c = spawn(loc.x, loc.y, n == 0 ? head : node, factory);			

			_tmpArr[n] = Timeline.createSequence()
					.pushPause(n * delay * 1000)
					.push(Tween.call(c.callbackGo).setUserData(speed));
			
			_tmpArr2[n] = c;
		}
		
		float angle = _tmpArr2[0].angle();
		
		for(int t = 0; t < numTurns; t++)
		{
			float time = Math.max(minTurnTime * 1000, (minTurnTime * 1000) + (float)Game.Dice.nextGaussian() * tunrTimeVariance * 1000f);
			float dAngle = (float)Game.Dice.nextGaussian() * angleVariance;
			if(dAngle < 0)
				dAngle -= minTurnAngle;
			else dAngle += minTurnAngle;
			
			angle += dAngle;
			
			for(int n = 0; n < num; n++)
			{
				_tmpArr[n].push(Tween.to(_tmpArr2[n]._angle, 0, time).target(angle));
			}
		}
		
		for(int n = 0; n < num; n++)
		{
			_tmpArr[n].start(Z.sim.tweens());
		}
	}
	
	public static void spawnConga(IFactory<Congoid> factory, int num, Map head, Map node, float speed, float delay, float... turns)
	{
		Vector2 loc = Tools.randomSpawnLoc();
		
		for(int n = 0; n < num; n++)
		{
			Congoid c = spawn(loc.x, loc.y, n == 0 ? head : node, factory);			

			_tmpArr[n] = Timeline.createSequence()
					.pushPause(n * delay * 1000)
					.push(Tween.call(c.callbackGo).setUserData(speed));
			
			_tmpArr3[n] = Timeline.createSequence();
			_tmpArr[n].push(_tmpArr3[n]);
			
			_tmpArr2[n] = c;
		}
		
		float angle = _tmpArr2[0].angle();
		
		for(int t = 0; t < turns.length/2f; t++)
		{
			float time = turns[t*2];
			float dAngle = turns[(t*2)+1];
			
			angle += dAngle;
			
			for(int n = 0; n < num; n++)
			{
				_tmpArr3[n].push(Tween.to(_tmpArr2[n]._angle, 0, time).target(angle));
			}
		}
		
		for(int n = 0; n < num; n++)
		{
			_tmpArr3[n].repeat(1000000, 0);
			_tmpArr[n].start(Z.sim.tweens());
		}
	}
	
	private static Congoid spawn(float x, float y, Map map, IFactory<Congoid> factory)
	{
		Congoid c = factory.get();
		c.setup(map, 0);
		float angleToShip = Tools.angleToShip(Vector2.tmp.set(x, y));
		c._body.setTransform(x, y, (float)Math.toRadians(angleToShip));
		c._angle.setValue(angleToShip);
		c._speed = 0;
		return c;
	}
	
	@Override
	protected void onFree()
	{
		Z.sim.tweens().killTarget(_angle);
	}
	
	@Override
	public void update(float dt)
	{
		super.update(dt);
		
		if(!_dead && _speed != 0)
		{
			_body.setLinearVelocity(Vector2.tmp.set(_speed, 0).rotate(_angle.floatValue()));
		}
	}
	
	@Override
	public void targetDirection(Vector2 v)
	{
		_angle.setValue(v.angle());
		_speed = v.len();
	}
}
