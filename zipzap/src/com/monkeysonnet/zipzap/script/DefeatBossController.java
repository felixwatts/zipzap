package com.monkeysonnet.zipzap.script;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.equations.Quad;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.ICallback;
import com.monkeysonnet.zipzap.IGameController;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;
import com.monkeysonnet.zipzap.entities.RedArrow;

public class DefeatBossController implements IGameController
{
	private final Vector2 _focalPoint = new Vector2();
	private Vector2 _target;
	private Tween _tweenFlypast;
	private boolean _waitingForTouch;			
	
	private final TweenCallback callbackFlypast = new TweenCallback()
	{		
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			RedArrow.spawnFlypast();
		}
	};
	
	private final TweenCallback callbackResumeSpeed = new TweenCallback()
	{		
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			Z.sim.timeMultiplier.setValue(0.5f);
			Z.renderer.shakeCamera(1f, 3f);
			//Z.sfx.explosionLarge.play();
		}
	};
		
	public DefeatBossController(Vector2 target)
	{
		_target = target;
	}
	
	@Override
	public void init()
	{
		_waitingForTouch = false;
		
		Z.ship().ghostMode(true);
		Z.sim().timeMultiplier.setValue(0.1f);//_timeScale);
		Z.ship().lockControls(true);
		
		_focalPoint.set(Z.sim().focalPoint());
		Z.sim().focalPoint(_focalPoint);		
		
		Z.music.play(null);
		
		Timeline
			.createSequence()
			.push(Tween
				.to(_focalPoint, 0, 1500f)
				.target(_target.x, _target.y)
				.ease(Quad.INOUT))
			.push(Tween.call(callbackResumeSpeed))
			.pushPause(4000)// 4500 * (0.25f/_timeScale))
			.setCallbackTriggers(TweenCallback.COMPLETE)
			.setCallback(callbackComplete)
			.start(Game.TweenManager);		
	}
	
	private final ICallback callbackAchievmentsComplete = new ICallback()
	{		
		@Override
		public void callback(Object arg)
		{
			Z.sim.advanceScript();
		}
	};
	
//	protected final  TweenCallback callbackDoAchievements = new TweenCallback()
//	{		
//		@Override
//		public void onEvent(int type, BaseTween<?> source)
//		{
//			Z.achievementsScreen.doAchievements(callbackAchievmentsComplete);
//		}
//	};
	
	private final TweenCallback callbackComplete = new TweenCallback()
	{				
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			Z.hud().announceVictory();	
			
			Z.music.play(Gdx.files.internal("music/victory.ogg"));
			
			Tween.to(Z.sim.timeMultiplier, 0, 2000).target(1f).start(Game.TweenManager);
			Tween.call(callbackFlypast).repeat(Tween.INFINITY, 6000f).start(Z.sim.tweens());			
			//Tween.call(callbackDoAchievements).delay(100000).start(Game.TweenManager);		
			
			_waitingForTouch = true;
		}
	};

	@Override
	public void update(float dt)
	{	
	}

	@Override
	public void cleanup()
	{	
		Game.TweenManager.killTarget(Z.sim.timeMultiplier);
		Z.sim.timeMultiplier.setValue(1f);
		
		if(_tweenFlypast != null)
		{
			_tweenFlypast.kill();
			_tweenFlypast = null;
		}
		
		
		Z.sim().clear();
		Z.sim().focalPoint(Z.ship().origin());
		Z.ship().ghostMode(false);
		Z.ship().lockControls(false);
	}
	
	@Override
	public void onSimulationEvent(int eventType, Object argument)
	{
		switch(eventType)
		{
			case ZipZapSim.EV_TAP:
				if(_waitingForTouch)
				{
					Z.achievementsScreen.doAchievements(callbackAchievmentsComplete);
				}
		}
	}

	@Override
	public void pause()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume()
	{
		// TODO Auto-generated method stub
		
	}
}
