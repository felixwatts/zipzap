package com.monkeysonnet.zipzap.screens;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.equations.Quad;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.monkeysonnet.engine.ActorTweener;
import com.monkeysonnet.engine.ButtonActor;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.IButtonEventHandler;
import com.monkeysonnet.engine.ICallback;
import com.monkeysonnet.engine.IScreen;
import com.monkeysonnet.zipzap.TitleActor;
import com.monkeysonnet.zipzap.Z;

public class PauseScreen implements IScreen, InputProcessor, IButtonEventHandler
	{
		private Stage _stage;
		private ButtonActor _btnContinue, _btnQuit;
		private TitleActor _titleActor;
	
		@Override
		public void pause()
		{
		}
		
		public PauseScreen()
		{
			_stage = new Stage(Gdx.graphics.getWidth() * Gdx.graphics.getDensity(), Gdx.graphics.getHeight() * Gdx.graphics.getDensity(), true);

			_btnContinue = TitleScreen.createButton(Z.texture("button-continue"), Z.texture("button-continue-glow"), 0, _stage, this);
			_btnQuit = TitleScreen.createButton(Z.texture("button-quit"), Z.texture("button-quit-glow"), 1, _stage, this);
			
			float unit = _stage.width() / 8f;
			
			_btnContinue.x = 2 * unit;
			_btnContinue.y = (_stage.height() / 2f) - (2 * unit);
			_btnContinue.width = _btnContinue.height = 2 * unit;
			_btnContinue.originX = _btnContinue.width/2f;
			_btnContinue.originY = _btnContinue.height/2f;
			
			_btnQuit.x = 4 * unit;
			_btnQuit.y = (_stage.height() / 2f) - (2 * unit);
			_btnQuit.width = _btnQuit.height = 2 * unit;
			_btnQuit.originX = _btnQuit.width/2f;
			_btnQuit.originY = _btnQuit.height/2f;
						
			_titleActor = new TitleActor(_stage);
		}

		@Override
		public void show()
		{	
			Z.sim.pause();
			
			_titleActor.scaleX = _titleActor.scaleY = 0;
			_titleActor.color.set(Color.WHITE);
			
			Game.TweenManager.killTarget(_titleActor);
			Tween.to(_titleActor, ActorTweener.VAL_SCALE, 500).target(1f).ease(Quad.OUT).start(Game.TweenManager);
			
			TitleScreen.appearButton(_btnContinue, 0);
			TitleScreen.appearButton(_btnQuit, 1);
			
			Z.music.pause();
			Z.sfx.pause();
		}

		@Override
		public void focus()
		{
			Gdx.input.setInputProcessor(this);
			_stage.getRoot().touchable = true;
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
			Game.TweenManager.killTarget(_btnContinue.color);
			Game.TweenManager.killTarget(_btnQuit.color);			
			Game.TweenManager.killTarget(_btnContinue);
			Game.TweenManager.killTarget(_btnQuit);
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

		@Override
		public boolean keyDown(int keycode)
		{
			switch(keycode)
			{
				case Keys.BACK:
					return true;
				default:
					return _stage.keyDown(keycode);
			}
		}

		@Override
		public boolean keyUp(int keycode)
		{
			switch(keycode)
			{
				case Keys.BACK:
				case Keys.ESCAPE:
					close(quitCallback);
					return true;
				default:
					return _stage.keyUp(keycode);
			}
		}

		@Override
		public boolean keyTyped(char character)
		{
			return _stage.keyTyped(character);
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
			return _stage.touchDragged(x, y, pointer);
		}

		@Override
		public boolean touchMoved(int x, int y)
		{
			return _stage.touchMoved(x, y);
		}

		@Override
		public boolean scrolled(int amount)
		{
			return _stage.scrolled(amount);
		}

		@Override
		public boolean onButtonDown(Actor sender)
		{
			Tween.to(sender, ActorTweener.VAL_SCALE, 100).target(0.8f).ease(Quad.OUT).start(Game.TweenManager);			
			return true;
		}

		@Override
		public void onButtonUp(Actor sender)
		{
			if(sender == _btnContinue)
			{
				close(continueCallback);
			}
			else if(sender == _btnQuit)
			{
				close(quitCallback);
			}
		}

		@Override
		public void onButtonDragged(Actor sender, Vector2 delta)
		{
		}
		
		private void close(TweenCallback next)
		{
			_stage.getRoot().touchable = false;
			
			TitleScreen.disappearActor(_btnContinue, null);
			TitleScreen.disappearActor(_btnQuit, next);
		}
		
		private final TweenCallback continueCallback = new TweenCallback()
		{		
			@Override
			public void onEvent(int type, BaseTween<?> source)
			{
				Game.ScreenManager.pop();
				Z.sim.resume();
				Z.music.resume();
				Z.sfx.resume();
			}
		};

		private final ICallback achievementsCompleteCallback = new ICallback()
		{			
			@Override
			public void callback(Object arg)
			{
//				Game.ScreenManager.clear();
//				Game.ScreenManager.push(Z.screen);
				Z.screen.initBackgroundDemo();
				Game.ScreenManager.push(Z.titleScreen);
			}
		};
		
		private final TweenCallback quitCallback = new TweenCallback()
		{		
			@Override
			public void onEvent(int type, BaseTween<?> source)
			{
				Game.ScreenManager.clear();
				Game.ScreenManager.push(Z.screen);
				Z.achievementsScreen.doAchievements(achievementsCompleteCallback);
			}
		};
}
