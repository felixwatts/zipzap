package com.monkeysonnet.zipzap.screens;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.monkeysonnet.engine.ButtonActor;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.IButtonEventHandler;
import com.monkeysonnet.engine.ICallback;
import com.monkeysonnet.engine.IScreen;
import com.monkeysonnet.zipzap.BadgeDetailsActor;
import com.monkeysonnet.zipzap.Tools;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.achievements.IBadge;

public class ReceiveAchievementScreen implements IScreen, InputProcessor, IButtonEventHandler
{
	private Stage _stage;
	private BadgeDetailsActor _actor;
	private ButtonActor _buttonShare;
	private Object _achievment;
	
	private ICallback onDismissBadge = new ICallback()
	{		
		@Override
		public void callback(Object arg)
		{
			Tween.call(new TweenCallback()
			{
				@Override
				public void onEvent(int type, BaseTween<?> source)
				{
					Game.ScreenManager.pop();
				}
			}).delay(600).start(Game.TweenManager);
		}
	};
//	private ButtonActor _actorScanlines;
	
	public ReceiveAchievementScreen(Object achievement)
	{
		_achievment = achievement;
	}
	
	@Override
	public void pause()
	{
	}
	
	@Override
	public void show()
	{
		_stage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
		_actor = new BadgeDetailsActor(_stage, onDismissBadge );
		
		if(((IBadge)_achievment).canShare())
		{		
			float h = _actor.y / 2f;
			float y = h/2f;
			TextureRegion tex = Z.texture("button-share");
			float aspect = Tools.aspect(tex, false);
			float w = h * aspect;
			float x = (_stage.width() - w) / 2f;
			
			_buttonShare = new ButtonActor(x, y, w, h, false, false, -1, tex, this);
			_stage.addActor(_buttonShare);
		}
	}

	@Override
	public void focus()
	{
		Gdx.input.setInputProcessor(this);	
		if(_achievment instanceof IBadge)
			_actor.setBadge((IBadge)_achievment);
//		else
//			_actor.setTreat((ITreat)_achievment);
	}

	@Override
	public void render()
	{
		_stage.act(Gdx.graphics.getDeltaTime());
		_stage.draw();
	}

	@Override
	public void blur()
	{
	}

	@Override
	public void hide()
	{
		_stage.dispose();
		_stage = null;
		_actor = null;
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
	public boolean keyDown(int keycode)
	{
		return false;
	}

	@Override
	public boolean keyUp(int keycode)
	{
		return false;
	}

	@Override
	public boolean keyTyped(char character)
	{
		return false;
	}

	@Override
	public boolean touchDown(int x, int y, int pointer, int button)
	{
		return _stage.touchDown(x, y, pointer, button);
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button)
	{
		return _stage.touchUp(x, y, pointer, button);
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer)
	{
		return false;
	}

	@Override
	public boolean touchMoved(int x, int y)
	{
		return false;
	}

	@Override
	public boolean scrolled(int amount)
	{
		return false;
	}

	@Override
	public void deserialize(Preferences dict)
	{
	}

	@Override
	public boolean onButtonDown(Actor sender)
	{
		return true;
	}

	@Override
	public void onButtonUp(Actor sender)
	{
		if(sender == _buttonShare)
		{
			if(Z.android != null)
			{
				Z.android.share(((IBadge)_achievment).shareText(), null);
			}
		}
	}

	@Override
	public void onButtonDragged(Actor sender, Vector2 delta)
	{
	}
}
