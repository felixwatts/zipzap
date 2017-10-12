package com.monkeysonnet.zipzap;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.equations.Elastic;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.monkeysonnet.engine.ActorTweener;
import com.monkeysonnet.engine.ButtonActor;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.ICallback;
import com.monkeysonnet.zipzap.achievements.IBadge;

public class BadgeDetailsActor extends Group
{
	private ButtonActor _icon;
	private Console _console;
	private TextureRegion texSolid = Z.texture("solid");
	private ICallback _onDismiss;
	private Timeline _timeline;
	
	public BadgeDetailsActor(Stage stage, ICallback onDismiss)
	{
		width = stage.width() * 0.75f;
		
		_onDismiss = onDismiss;
		
		float unit = width/16f;
		
		height = unit * 6;
		x = (stage.width() - width) / 2f;
		y = (stage.height() - height) / 2f;
		originX = width/2f;
		originY = height/2f;		
		
		_icon = new ButtonActor(unit, height - (5*unit), 4*unit, 4*unit, null, null);
		_icon.touchable = false;
		addActor(_icon);
		
		_console = new Console(24, unit*6, unit, unit*9, height-(unit*2), 8f, null, false, Color.CLEAR);
		_console.touchable = false;
		addActor(_console);
		
		scaleX = scaleY = 0;
		visible = false;
		touchable = false;
		
		stage.addActor(this);
	}
	
	public void setBadge(IBadge badge)
	{
		scaleX = scaleY = 0.5f;
		visible = true;
		touchable = true;
		
		color.set(badge.color());
		
		_icon.setTexture(badge.icon());
		_icon.color.set(badge.color());
		
		if(_timeline != null)
			_timeline.kill();
		
		_console.clearNow();
		
		_console
			.setColour(badge.color(), Color.CLEAR)
			.write(badge.title())
			.write("\n\n")
			.write(badge.description());
	
		if(badge.isEarned())
		{
			_console
				.write("\n\n")
				.write("Well done!");
		}
		
		if(badge.treat() != null)
		{
			_console
				.write("\n\n")
				.setColour(Color.YELLOW, Color.CLEAR)
				.write(badge.treat().description());
		}	
		
		_timeline = Timeline.createSequence()
			.push(Timeline.createParallel()
				.push(Tween
						.to(this, ActorTweener.VAL_COLOR_RGBA, 800)
						.target(color.r, color.g, color.b, 1f))
				.push(Tween
						.to(this, ActorTweener.VAL_SCALE, 800)
						.target(1f)
						.ease(Elastic.OUT)))
			.push(Tween.call(new TweenCallback()
			{
				@Override
				public void onEvent(int type, BaseTween<?> source)
				{
					//touchable = true;
					_timeline = null;
				}
			}))
			.start(Game.TweenManager);
	}
	
//	public void setTreat(ITreat treat)
//	{
//		scaleX = scaleY = 0.5f;
//		visible = true;
//		touchable = false;
//		
//		_prevBgColor.set(Z.renderer().backgroundColor());		
//		Z.renderer.backgroundColor(ColorTools.darken(treat.color()));
//		
//		color.set(treat.color());
//		
//		_icon.setTexture(treat.icon());
//		_icon.color.set(treat.color());
//
//		
//		Timeline.createParallel()
//			.push(Tween
//					.to(this, ActorTweener.VAL_COLOR_RGBA, 800)
//					.target(color.r, color.g, color.b, 1f))
//			.push(Tween
//					.to(this, ActorTweener.VAL_SCALE, 800)
//					.target(1f)
//					.ease(Elastic.OUT))
//			.push(Tween.call(new TweenCallback(){
//
//				@Override
//				public void onEvent(int type, BaseTween<?> source)
//				{
//					touchable = true;
//					
//					ITreat badge = (ITreat)source.getUserData();
//					
//					_console
//						.setColour(badge.color(), Color.CLEAR)
//						.write(badge.title())
//						.write("\n\n")
//						.write(badge.description());
//								
//				}}).setUserData(treat))
//			.start(Game.TweenManager);
//	}
	
	@Override
	public Actor hit(float x, float y)
	{
		return this;
	}
	
	@Override
	public boolean touchDown(float x, float y, int pointer)
	{
		return true;
	}
	
	public void hide()
	{
		Game.TweenManager.killTarget(this);
		
		touchable = false;
		
		if(_timeline != null)
			_timeline.kill();
		
		_timeline = Timeline.createParallel()
		.push(Tween
				.to(this, ActorTweener.VAL_COLOR_RGBA, 200)
				.target(color.r, color.g, color.b, 0))
		.push(Tween.call(new TweenCallback(){

			@Override
			public void onEvent(int type, BaseTween<?> source)
			{				
				visible = false;
				//_console.clearNow();	
				
				_timeline = null;
				
			}}).delay(500))
		.start(Game.TweenManager);
		
		if(_onDismiss != null)
			_onDismiss.callback(null);
	}
	
	@Override
	public void touchUp(float x, float y, int pointer)
	{
		hide();
	}
	
	@Override
	public void draw(SpriteBatch batch, float parentAlpha)
	{
		batch.setColor(color.r, color.g, color.b, 0.2f * parentAlpha * color.a);
		batch.draw(texSolid, x, y, originX, originY, width, height, scaleX, scaleY, 0);		
		super.draw(batch, parentAlpha * color.a);
	}
}
