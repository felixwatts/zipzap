package com.monkeysonnet.zipzap.screens;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.monkeysonnet.engine.ActorTweener;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.ICallback;
import com.monkeysonnet.engine.IScreen;
import com.monkeysonnet.zipzap.Console;

public class ConsoleScreen implements IScreen
{
	protected Stage _stage;
	protected Console _console;

	public ConsoleScreen()
	{
		_stage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
		_console = new Console(32, _stage.width()/8f, _stage.height() / 2f, _stage.width()*0.75f, _stage.height()/2f, 2f, _stage, true, Color.CLEAR);
	}

	@Override
	public void show()
	{
	}	
	
	@Override
	public void pause()
	{
	}

	@Override
	public void focus()
	{
		Gdx.input.setInputProcessor(_stage);
		_stage.setKeyboardFocus(_console);
		_stage.getRoot().touchable = true;
	}
	
	public Console console()
	{
		return _console;
	}

	@Override
	public void render()
	{
		_stage.draw();
	}

	@Override
	public void blur()
	{
	}

	@Override
	public void hide()
	{
		//_console.clearNow();
	}

	@Override
	public boolean isFullScreen()
	{
		return false;
	}

	@Override
	public void serialize(Preferences dict)
	{
	}
	
	@Override
	public void deserialize(Preferences dict)
	{
	}
	
	public void close(ICallback onClosed)
	{
		_stage.getRoot().touchable = false;
		
		_console.clearAvatar();
		
		Tween
			.to(_console, ActorTweener.VAL_COLOR_RGBA, 500)
			.target(1, 1, 1, 0)
			.setCallbackTriggers(TweenCallback.COMPLETE)
			.setCallback(closeCallback)
			.setUserData(onClosed)
			.start(Game.TweenManager);
	}
	
	private final TweenCallback closeCallback = new TweenCallback()
	{		
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			Game.ScreenManager.pop();
			
			ICallback cb = (ICallback)source.getUserData();
			if(cb != null)
				cb.callback(null);
		}
	};
}
