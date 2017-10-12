package com.monkeysonnet.zipzap;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.equations.Elastic;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.monkeysonnet.engine.ActorTweener;
import com.monkeysonnet.engine.ButtonActor;
import com.monkeysonnet.engine.ColorTools;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.IButtonEventHandler;
import com.monkeysonnet.zipzap.achievements.IBadge;

public class BadgeButton extends ButtonActor
{
	public final static Color notEarnedColor = new Color(1f, 1f, 1f, 0.2f);
	private static TextureRegion texHasTreat = Z.texture("badge-decoration-locked");	
	
	private IBadge _badge;
	private final TweenCallback callbackAppearSound = new TweenCallback()
	{		
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			Z.sfx.play(11);
		}
	};

	public BadgeButton(IBadge badge, IButtonEventHandler handler, float size)
	{
		super(badge.icon(), handler);
		_badge = badge;
		
		color.set(badge.isEarned() ? badge.color() : notEarnedColor);
		
		scaleX = scaleY = 0;
		width = height = size;
		originX = width/2f;
		originY = height/2f;
		
		Tween
			.to(this, ActorTweener.VAL_SCALE, 1000)
			.target(1f)
			.ease(Elastic.OUT)
			.delay(Game.Dice.nextFloat()*250)
			.setCallback(callbackAppearSound)
			.setCallbackTriggers(TweenCallback.BEGIN)			
			.start(Game.TweenManager);
	}
	
	@Override
	public void draw(SpriteBatch batch, float parentAlpha)
	{
		super.draw(batch, parentAlpha);
		
		if(_badge.treat() != null)
		{
			batch.setColor(ColorTools.combineAlpha(Color.YELLOW, color.a));
			batch.draw(texHasTreat, x, y, originX, originY, width, height, scaleX, scaleY, 0);
		}
	}
	
	public void toBackground()
	{
		color.set(notEarnedColor);
	}
	
	public void toForeground()
	{
		color.set(_badge.isEarned() ? _badge.color() : notEarnedColor);
	}

	public IBadge badge()
	{
		return _badge;
	}
}
