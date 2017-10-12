package com.monkeysonnet.zipzap.screens;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.equations.Quad;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.FlickScrollPane;
import com.monkeysonnet.engine.ActorTweener;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.IButtonEventHandler;
import com.monkeysonnet.engine.ICallback;
import com.monkeysonnet.engine.IScreen;
import com.monkeysonnet.lander.LanderScreen;
import com.monkeysonnet.zipzap.SectorButton;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.achievements.TreatJetpak;
import com.monkeysonnet.zipzap.script.Script;

public class SectorsScreen implements IScreen, IButtonEventHandler, InputProcessor
{
	public static final int NUM_SECTORS = 4;
	
	private Stage _stage;
	private Actor[] _buttons = new Actor[NUM_SECTORS + 1];

	private boolean _isFocussed;

	@Override
	public void pause()
	{
	}

	@Override
	public void show()
	{
		_stage = new Stage(Gdx.graphics.getWidth() * Gdx.graphics.getDensity(),  Gdx.graphics.getHeight() * Gdx.graphics.getDensity(), true);

		_stage.getRoot().originX = _stage.width() / 2f;
		_stage.getRoot().originY = _stage.height() / 2f;
		
		float unit = _stage.height() / 10f;
		float buttonSize = unit * 4f;
		float numCols = NUM_SECTORS / 2;
		
		Group g = new Group();
		g.x = 0;
		g.y = 0;
		g.width = (numCols + 2) * buttonSize;
		g.height = unit * 10;
		
		for(int n = 0; n < NUM_SECTORS; n++)
		{
			Actor a = new SectorButton(n+1, buttonSize, this);
			
			a.y = g.height - ((((n % 2) + 1) * buttonSize) + unit);
			a.x = ((n / 2) * buttonSize) + unit;			
			g.addActor(a);			
			_buttons[n] = a;
		}
		
		Actor a = new SectorButton(-1, buttonSize, this, "button-jetpak", new TreatJetpak().isUnlocked());		
		a.y = g.height - ((((NUM_SECTORS % 2) + 1) * buttonSize) + unit);
		a.x = ((NUM_SECTORS / 2) * buttonSize) + unit;			
		g.addActor(a);			
		_buttons[NUM_SECTORS] = a;
		
		FlickScrollPane p = new FlickScrollPane(g);
		p.x = 0;
		p.y = 0;
		p.height = _stage.height();
		p.width = _stage.width();
		
		_stage.addActor(p);
	}

	@Override
	public void focus()
	{
		_isFocussed = true;
		
		Game.TweenManager.killTarget(_stage.getRoot());
		_stage.getRoot().color.set(1f, 1f, 1f, 1f);
		_stage.getRoot().scaleX = 1f;
		_stage.getRoot().scaleY = 1f;
		
		Gdx.input.setInputProcessor(this);
		
		for(int n = 0; n < NUM_SECTORS+1; n++)			
			TitleScreen.appearButton(_buttons[n], n);
	}

	@Override
	public void render()
	{		
		if(_isFocussed)
		{
			_stage.act(Gdx.graphics.getDeltaTime());
			_stage.draw();
		}
	}

	@Override
	public void blur()
	{
		_isFocussed = false;
	}

	@Override
	public void hide()
	{
		for(int n = 0; n < NUM_SECTORS+1; n++)
		{
			Game.TweenManager.killTarget(_buttons[n].color);
			Game.TweenManager.killTarget(_buttons[n]);
			_buttons[n] = null;
		}
		
		_stage.dispose();
		_stage = null;
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
	public boolean onButtonDown(Actor sender)
	{
		Tween.to(sender, ActorTweener.VAL_SCALE, 100).target(0.8f).ease(Quad.OUT).start(Game.TweenManager);
		return true;		
	}

	@Override
	public void onButtonUp(Actor sender)
	{
		SectorButton b = (SectorButton)sender;	
		
		switch(b.sector())
		{
			case -1:
				
				if(Z.isDemo)
					close(buyCallback);
				else 
					close(landerCallback);
				
				break;
			default:
				
				if(Z.script != null)
					Z.script.dispose();
				
				if(Z.isDemo && b.sector() != 1)
				{
					close(buyCallback);
				}
				else
				{
					Z.script = Script.create(b.sector());
					close(playCallback);
				}
				break;
		}

		Z.sfx.play(10);
	}
	
	private void close(TweenCallback next)
	{
		disappearActor(_stage.getRoot(), next);
	}
	
	private void disappearActor(Actor a, TweenCallback callback)
	{
		Game.TweenManager.killTarget(a);
		Game.TweenManager.killTarget(a.color);
		
		Timeline.createParallel()
			.push(Tween.to(a, ActorTweener.VAL_SCALE, 200).target(2f).ease(Quad.IN))
			.push(Tween.to(a, ActorTweener.VAL_COLOR_RGBA, 200).target(a.color.r, a.color.g, a.color.b, 0))
			.setCallbackTriggers(TweenCallback.COMPLETE)
			.setCallback(callback)
			.start(Game.TweenManager);
	}
	
	private final TweenCallback playCallback = new TweenCallback()
	{		
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			Game.ScreenManager.pop();
			Game.ScreenManager.pop();
//			Game.ScreenManager.clear();
//			Game.ScreenManager.push(Z.screen);
			Z.sim.start();
		}
	};
	
	private final TweenCallback landerCallback = new TweenCallback()
	{		
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			Game.ScreenManager.clear();		
			Game.ScreenManager.push(new LanderScreen());
		}
	};
	
	private final TweenCallback buyCallback = new TweenCallback()
	{		
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			//Game.ScreenManager.pop();	
			Game.ScreenManager.push(new PurchaseScreen(onPurchaseComplete));
		}
	};
	
	private final ICallback onPurchaseComplete = new ICallback()
	{
		@Override
		public void callback(Object arg)
		{
			//Game.ScreenManager.push(SectorsScreen.this);
		}
	};

	@Override
	public void onButtonDragged(Actor sender, Vector2 delta)
	{
	}

	@Override
	public boolean keyDown(int keycode)
	{
		switch(keycode)
		{
			case Keys.BACK:
			case Keys.ESCAPE:
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
				Game.ScreenManager.pop();
				//Game.ScreenManager.push(Z.titleScreen);
				return true;
			default:
				return _stage.keyUp(keycode);
		}
	}

	@Override
	public boolean keyTyped(char character)
	{
		if(_stage != null)
			return _stage.keyTyped(character);
		else return false;
	}

	@Override
	public boolean touchDown(int x, int y, int pointer, int button)
	{
		if(_stage != null)
			return _stage.touchDown(x, y, pointer, button);
		else return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button)
	{
		if(_stage != null)
			return _stage.touchUp(x, y, pointer, button);
		else return false;		
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer)
	{
		if(_stage != null)
			return _stage.touchDragged(x, y, pointer);
		else return false;		
	}

	@Override
	public boolean touchMoved(int x, int y)
	{
		if(_stage != null)
			return _stage.touchMoved(x, y);
		else return false;			
	}

	@Override
	public boolean scrolled(int amount)
	{
		if(_stage != null)
			return _stage.scrolled(amount);
		else return false;			
	}
	
	@Override
	public void deserialize(Preferences dict)
	{
	}
}
