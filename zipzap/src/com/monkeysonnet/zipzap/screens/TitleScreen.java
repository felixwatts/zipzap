package com.monkeysonnet.zipzap.screens;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.equations.Elastic;
import aurelienribon.tweenengine.equations.Quad;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.monkeysonnet.engine.ActorTweener;
import com.monkeysonnet.engine.ButtonActor;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.IButtonEventHandler;
import com.monkeysonnet.engine.IScreen;
import com.monkeysonnet.zipzap.GlowButton;
import com.monkeysonnet.zipzap.TitleActor;
import com.monkeysonnet.zipzap.Z;

public class TitleScreen implements IScreen, InputProcessor, IButtonEventHandler
{
	private static TweenCallback callbackPlayButtonAppearSound = new TweenCallback()
	{
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			Z.sfx.play(11);
		}
	};
	
	private Stage _stage;
	private Actor _titleActor;	
	private ButtonActor _btnPlay, _btnBadges, _btnSquadron, _btnMusic, _btnSfx;
	private boolean _isFocussed;
	
	@Override
	public void pause()
	{
	}
	
	public TitleScreen()
	{
		_stage = new Stage(Gdx.graphics.getWidth() * Gdx.graphics.getDensity(), Gdx.graphics.getHeight() * Gdx.graphics.getDensity(), true);

//		_actorScanlines = new ButtonActor(Z.texture("scanlines"), null);
//		_actorScanlines.x = 0;
//		_actorScanlines.y = 0;
//		_actorScanlines.width = _stage.width();
//		_actorScanlines.height = _actorScanlines.width * (((float)_actorScanlines.getTexture().getRegionHeight()) / ((float)_actorScanlines.getTexture().getRegionWidth()));
//		_actorScanlines.color.set(ColorTools.darken(SimRenderer.blackAndWhiteEntityColor));
//		_stage.addActor(_actorScanlines);
		
		_btnPlay = createButton(Z.texture("button-play"), Z.texture("button-play-glow"), 0, _stage, this);
		_btnBadges = createButton(Z.texture("button-badges"), Z.texture("button-badges-glow"), 1, _stage, this);
		_btnSquadron = createButton(Z.texture("button-squadron"), Z.texture("button-squadron-glow"), 2, _stage, this);		
		
		float unit = _stage.width() / 8f;
		
		_btnPlay.x = 2 * unit;
		_btnPlay.y = (_stage.height() / 2f) - (2 * unit);
		_btnPlay.width = _btnPlay.height = 2 * unit;
		_btnPlay.originX = _btnPlay.width/2f;
		_btnPlay.originY = _btnPlay.height/2f;
		
		_btnBadges.x = 4 * unit;
		_btnBadges.y = (_stage.height() / 2f) - (1 * unit);
		_btnBadges.width = 2 * unit;
		_btnBadges.height = unit;
		_btnBadges.originX = _btnBadges.width/2f;
		_btnBadges.originY = _btnBadges.height/2f;
		
		_btnSquadron.x = 4 * unit;
		_btnSquadron.y = (_stage.height() / 2f) - (2 * unit);
		_btnSquadron.width = 2 * unit;
		_btnSquadron.height = unit;
		_btnSquadron.originX = _btnSquadron.width/2f;
		_btnSquadron.originY = _btnSquadron.height/2f;

		_titleActor = new TitleActor(_stage);
		
		unit *= 0.6f;
		
		_btnMusic = new ButtonActor(unit * 0.5f, unit * 0.5f, unit, unit, false, false, -1, Z.music.enabled() ?  Z.texture("button-music-on") : Z.texture("button-music-off"), this);
		_btnSfx = new ButtonActor(_stage.width() - (unit * 1.5f), unit * 0.5f, unit, unit, false, false, -1, Z.sfx.enabled() ?  Z.texture("button-sfx-on") : Z.texture("button-sfx-off"), this);
		_btnMusic.color.set(Z.colorUi);
		_btnSfx.color.set(Z.colorUi);
		_stage.addActor(_btnMusic);
		_stage.addActor(_btnSfx);
	}
	
	public static ButtonActor createButton(TextureRegion fg, TextureRegion bg, int position, Stage stage, IButtonEventHandler h)
	{
		float buttonHeight = stage.height() * ((4f/8f)/4f);	
		TextureRegion tex = fg;
		float aspect = ((float)tex.getRegionWidth()) / ((float)tex.getRegionHeight());
		float buttonWidth = buttonHeight * aspect;
		
		GlowButton btn = new GlowButton(fg, bg, h);
		btn.width = buttonWidth;
		btn.height = buttonHeight;
		btn.x = (stage.width() - buttonWidth) / 2f;
		btn.y = (stage.height() / 2f) - (buttonHeight * (position + 0.5f));
		btn.originX = btn.width / 2f;
		btn.originY = btn.height / 2f;
		
		stage.addActor(btn);

		return btn;
	}
	
	public static void appearButton(Actor btn, int position)
	{
		Game.TweenManager.killTarget(btn.color);
		Game.TweenManager.killTarget(btn);
		
		btn.scaleX = btn.scaleY = 0f;
		
		btn.color.set(Z.colorUi);
		//btn.color.set(0f, 1f, 102f/255f, 1f);// ColorTools.hslToColor(51f/255f, 88f/255f, 100f/255f, 1f));
		
//		Timeline.createSequence()
//			.push(Tween.set(btn.color, ColorTweener.VAL_RGBA).target(1f, 1f, 0f, 1f)) //.target(1f/2f, 1, 0.5f, 1f))
//			.push(Tween.to(btn.color, ColorTweener.VAL_RGBA, 1000).target(1f, 0f, 0f, 1f)) //.target(2f/3f, 1f, 0.5f, 1f))
//			.pushPause(2000)
//			.repeatYoyo(Tween.INFINITY, 0)
//			.delay(position * 250)
//			.start(Game.TweenManager);	
		
		Tween
			.to(btn, ActorTweener.VAL_SCALE, 1000)
			.target(1f)
			.ease(Elastic.OUT)
			.delay(100f * (float)position)
			.setCallback(callbackPlayButtonAppearSound )
			.setCallbackTriggers(TweenCallback.BEGIN)			
			.start(Game.TweenManager);
		
		
	}

	@Override
	public void show()
	{		
		if(!Z.isDemo)
			Z.music.play(Gdx.files.internal("music/sector-4.ogg"));
		else Z.music.play(null);
	}

	@Override
	public void focus()
	{
		_isFocussed = true;
		Gdx.input.setInputProcessor(this);
		
		_titleActor.scaleX = _titleActor.scaleY = 0;
		_titleActor.color.set(Color.WHITE);
		
		Game.TweenManager.killTarget(_titleActor);
		Tween.to(_titleActor, ActorTweener.VAL_SCALE, 500).target(1f).ease(Quad.OUT).start(Game.TweenManager);
		
		appearButton(_btnPlay, 0);
		appearButton(_btnBadges, 1);
		appearButton(_btnSquadron, 2);
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
		Game.TweenManager.killTarget(_btnPlay.color);
		Game.TweenManager.killTarget(_btnBadges.color);
		Game.TweenManager.killTarget(_btnSquadron.color);
		
		Game.TweenManager.killTarget(_btnPlay);
		Game.TweenManager.killTarget(_btnBadges);
		Game.TweenManager.killTarget(_btnSquadron);
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
				Game.ScreenManager.clear();
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
		if(sender == _btnMusic)
		{
			Z.music.enabled(!Z.music.enabled());
			_btnMusic.setTexture(Z.music.enabled() ?  Z.texture("button-music-on") : Z.texture("button-music-off"));
		}
		else if(sender == _btnSfx)
		{
			Z.sfx.enabled(!Z.sfx.enabled());
			_btnSfx.setTexture(Z.sfx.enabled() ?  Z.texture("button-sfx-on") : Z.texture("button-sfx-off"));
		}
		else Tween.to(sender, ActorTweener.VAL_SCALE, 100).target(0.8f).ease(Quad.OUT).start(Game.TweenManager);
		
		return true;
	}

	@Override
	public void onButtonUp(Actor sender)
	{
		if(sender == _btnPlay)
		{
			close(playCallback);
		}
		else if(sender == _btnBadges)
		{
			close(badgesCallback);
		}
		else if(sender == _btnSquadron)
		{
			close(squadronCallback);
		}
		
		Z.sfx.play(10);
	}

	@Override
	public void onButtonDragged(Actor sender, Vector2 delta)
	{
		// TODO Auto-generated method stub
		
	}
	
	private void close(TweenCallback next)
	{
		disappearActor(_titleActor, null);
		disappearActor(_btnBadges, null);
		disappearActor(_btnPlay, null);
		disappearActor(_btnSquadron, next);
	}
	
	public static void disappearActor(Actor a, TweenCallback callback)
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
			//Game.ScreenManager.pop();
			Game.ScreenManager.push(new SectorsScreen());
			//Z.screen.resume();
		}
	};
	
	private final TweenCallback badgesCallback = new TweenCallback()
	{		
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			//Game.ScreenManager.pop();
			Game.ScreenManager.push(new BadgesScreen());
		}
	};
	
	private final TweenCallback squadronCallback = new TweenCallback()
	{		
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			//Game.ScreenManager.pop();		
			Game.ScreenManager.push(new SquadronScreen());
		}
	};
	
	public void dispose()
	{
	}
	
	@Override
	public void deserialize(Preferences dict)
	{
	}
}
