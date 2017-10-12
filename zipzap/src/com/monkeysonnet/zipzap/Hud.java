package com.monkeysonnet.zipzap;

import java.util.Hashtable;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenAccessor;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.equations.Bounce;
import aurelienribon.tweenengine.equations.Elastic;
import aurelienribon.tweenengine.equations.Linear;
import aurelienribon.tweenengine.equations.Quad;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.monkeysonnet.engine.ActorTweener;
import com.monkeysonnet.engine.ButtonActor;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.ISimulationEventHandler;
import com.monkeysonnet.zipzap.achievements.Notification;
import com.monkeysonnet.zipzap.screens.PauseScreen;

public class Hud implements InputProcessor, ISimulationEventHandler, TweenAccessor<Hud>
{
	public static final float ICON_SIZE = 48f * Gdx.graphics.getDensity();
	//private static final int DISPLAY_SCORE_SPEED = 500;
	private static final float CAPTION_OPACITY = 0.5f;
	private static final int SFX_LEVEL_UP = 38;
	protected static final int SFX_BOSS_LETTER = 52;

	//private static final float ARROW_RADIUS = ;
	
	private Stage _stage;
	private ButtonActor 
		_captionMegaLaser, 
		_captionBomb, 
		_captionMeteorStorm, 
		_captionWave, 
		_captionWaveNumber1, 
		_captionWaveNumber2, 
		_captionShield,
		_captionBossB,
		_captionBossO,
		_captionBossS1,
		_captionBossS2,
		_captionVictory;
	
	private NotificationQueue _badgeIndicatorQueue;
	
	private ButtonActor[] _lifeButtons;
	
	private TargetArrowActor _arrow;
	
	private ButtonActor[] _comboCaptions;
	
	private TextureRegion _texCaptionMegaLaser = Z.texture("zipzap-caption-mega-laser"),
			_texCaptionBomb = Z.texture("zipzap-caption-bomb"),
			_texCaptionMeteorStorm = Z.texture("zipzap-caption-meteor-storm"),
			_textCaptionWave =  Z.texture("zipzap-caption-wave"),
			_texCaptionVictory = Z.texture("zipzap-caption-victory");
	
	private Hashtable<Character, TextureRegion> _captionNumberTextures;
	private int _displayScore;
	private MainScoreLabel _score;
	private Group _mainGroup;
	
	public Hud()
	{
		_stage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
		
		_mainGroup = new Group();
		_mainGroup.x = 0;
		_mainGroup.y = 0;
		_mainGroup.width = _stage.width();
		_mainGroup.height = _stage.height();
		_stage.addActor(_mainGroup);
		
		_captionNumberTextures = new Hashtable<Character, TextureRegion>();
		CharSequence letters = "0123456789";
		for(int n = 0; n < letters.length(); n++)
			_captionNumberTextures.put(letters.charAt(n), Z.texture("zipzap-caption-" + letters.charAt(n)));
		
		_captionVictory = new ButtonActor(_texCaptionVictory, null);
		_captionVictory.height = _stage.height();
		_captionVictory.width = (((float)_texCaptionVictory.getRegionWidth()) / ((float)_texCaptionVictory.getRegionHeight())) * _captionVictory.height;
		_captionVictory.x = _stage.width();
		_captionVictory.y = 0f;
		_captionVictory.color.set(1f, 1f, 116f/255f, CAPTION_OPACITY);
		_captionVictory.visible = _captionVictory.touchable = false;
		_captionVictory.originX = _captionVictory.width/2f;
		_captionVictory.originY = _captionVictory.height/2f;
		_mainGroup.addActor(_captionVictory);
		
		_captionMegaLaser = new ButtonActor(_texCaptionMegaLaser, null);
		_captionMegaLaser.width = _stage.width() * 3f/4f;
		_captionMegaLaser.height = (((float)_texCaptionMegaLaser.getRegionHeight()) / ((float)_texCaptionMegaLaser.getRegionWidth())) * _captionMegaLaser.width;
		_captionMegaLaser.x = _stage.width() / 8f;
		_captionMegaLaser.y = _captionMegaLaser.height /2f;// (_stage.height() - _captionMegaLaser.height) /2f;
		_captionMegaLaser.color.set(Color.MAGENTA);
		_captionMegaLaser.visible = _captionMegaLaser.touchable = false;
		_captionMegaLaser.originX = _captionMegaLaser.width/2f;
		_captionMegaLaser.originY = _captionMegaLaser.height/2f;
		_mainGroup.addActor(_captionMegaLaser);
		
		_captionBomb = new ButtonActor(_texCaptionBomb, null);
		_captionBomb.width = (((float)_texCaptionBomb.getRegionWidth()) / ((float)_texCaptionBomb.getRegionHeight())) * _captionMegaLaser.height;
		_captionBomb.height = _captionMegaLaser.height;
		_captionBomb.x = (_stage.width() - _captionBomb.width) / 2f;
		_captionBomb.y = _captionBomb.height /2f;// (_stage.height() - _captionBomb.height) /2f;
		_captionBomb.color.set(Color.RED);
		_captionBomb.visible = _captionBomb.touchable = false;
		_captionBomb.originX = _captionBomb.width/2f;
		_captionBomb.originY = _captionBomb.height/2f;
		_mainGroup.addActor(_captionBomb);
		
		_captionMeteorStorm = new ButtonActor(_texCaptionMeteorStorm, null);
		_captionMeteorStorm.width = (((float)_texCaptionMeteorStorm.getRegionWidth()) / ((float)_texCaptionMeteorStorm.getRegionHeight())) * _captionMegaLaser.height;
		_captionMeteorStorm.height = _captionMegaLaser.height;
		_captionMeteorStorm.x = _stage.width();
		_captionMeteorStorm.y = _captionMeteorStorm.height /2f;// (_stage.height() - _captionBomb.height) /2f;
		_captionMeteorStorm.color.set(Color.GRAY);
		_captionMeteorStorm.visible = _captionMeteorStorm.touchable = false;
		_captionMeteorStorm.originX = _captionMeteorStorm.width/2f;
		_captionMeteorStorm.originY = _captionMeteorStorm.height/2f;
		_mainGroup.addActor(_captionMeteorStorm);
		
		_captionWave = new ButtonActor(_textCaptionWave, null);
		_captionWave.width = (((float)_textCaptionWave.getRegionWidth()) / ((float)_textCaptionWave.getRegionHeight())) * _captionMegaLaser.height;
		_captionWave.height = _captionMegaLaser.height;
		_captionWave.x = _stage.width();
		_captionWave.y = _captionWave.height /2f;// (_stage.height() - _captionWave.height) /2f;
		_captionWave.color.set(1f, 1f, 116f/255f, CAPTION_OPACITY);
		_captionWave.visible = _captionWave.touchable = false;
		_captionWave.originX = _captionWave.width/2f;
		_captionWave.originY = _captionWave.height/2f;
		_mainGroup.addActor(_captionWave);
		
		_captionWaveNumber1 = new ButtonActor(null, null);
		_captionWaveNumber1.width = _captionMegaLaser.height;
		_captionWaveNumber1.height = _captionMegaLaser.height;
		_captionWaveNumber1.color.set(1f, 1f, 116f/255f, CAPTION_OPACITY);
		_captionWaveNumber1.y =_captionWaveNumber1.height /2f;// (_stage.height() - _captionWave.height) /2f;
		_mainGroup.addActor(_captionWaveNumber1);
		
		_captionWaveNumber2 = new ButtonActor(null, null);
		_captionWaveNumber2.width = _captionMegaLaser.height;
		_captionWaveNumber2.height = _captionMegaLaser.height;
		_captionWaveNumber2.color.set(1f, 1f, 116f/255f, CAPTION_OPACITY);
		_captionWaveNumber2.y =_captionWaveNumber2.height /2f;// (_stage.height() - _captionWave.height) /2f;
		_mainGroup.addActor(_captionWaveNumber2);
	
		_captionShield = initCaption(Z.texture("zipzap-caption-shield"), Color.CYAN);

		_comboCaptions = new ButtonActor[10];
		
		_comboCaptions[2] = initCaption(Z.texture("zipzap-caption-combo-2x"), Color.RED);
		_comboCaptions[3] = initCaption(Z.texture("zipzap-caption-combo-3x"), Color.RED);
		_comboCaptions[4] = initCaption(Z.texture("zipzap-caption-combo-4x"), Color.RED);
		_comboCaptions[5] = initCaption(Z.texture("zipzap-caption-combo-5x"), Color.RED);
		_comboCaptions[6] = initCaption(Z.texture("zipzap-caption-combo-6x"), Color.RED);
		_comboCaptions[7] = initCaption(Z.texture("zipzap-caption-combo-7x"), Color.RED);
		_comboCaptions[8] = initCaption(Z.texture("zipzap-caption-combo-8x"), Color.RED);
		_comboCaptions[9] = initCaption(Z.texture("zipzap-caption-combo-9x"), Color.MAGENTA);	
		
		float bossLetterSize = _captionMegaLaser.height * 3f;
		float middle = _stage.width() / 2f;
		
		_captionBossB = new ButtonActor(Z.texture("zipzap-letter-B1"), null);
		_captionBossB.width = bossLetterSize;
		_captionBossB.height = bossLetterSize;
		_captionBossB.color.set(1f, 0f, 1f, CAPTION_OPACITY);
		_captionBossB.x = middle - (2 * bossLetterSize);
		_captionBossB.y = _stage.height();
		_captionBossB.visible = _captionBossB.touchable = false;
		_captionBossB.originY = bossLetterSize / 2f;
		_captionBossB.originX = middle - _captionBossB.x;
		_mainGroup.addActor(_captionBossB);
		
		_captionBossO = new ButtonActor(Z.texture("zipzap-letter-O1"), null);
		_captionBossO.width = bossLetterSize;
		_captionBossO.height = bossLetterSize;
		_captionBossO.color.set(1f, 0f, 1f, CAPTION_OPACITY);
		_captionBossO.x = middle - (1 * bossLetterSize);
		_captionBossO.y = _stage.height();
		_captionBossO.visible = _captionBossO.touchable = false;
		_captionBossO.originY = bossLetterSize / 2f;
		_captionBossO.originX = middle - _captionBossO.x;
		_mainGroup.addActor(_captionBossO);
		
		_captionBossS1 = new ButtonActor(Z.texture("zipzap-letter-S1"), null);
		_captionBossS1.width = bossLetterSize;
		_captionBossS1.height = bossLetterSize;
		_captionBossS1.color.set(1f, 0f, 1f, CAPTION_OPACITY);
		_captionBossS1.x = middle + (0 * bossLetterSize);
		_captionBossS1.y = _stage.height();
		_captionBossS1.visible = _captionBossS1.touchable = false;
		_captionBossS1.originY = bossLetterSize / 2f;
		_captionBossS1.originX = middle - _captionBossS1.x;
		_mainGroup.addActor(_captionBossS1);
		
		_captionBossS2 = new ButtonActor(Z.texture("zipzap-letter-S1"), null);
		_captionBossS2.width = bossLetterSize;
		_captionBossS2.height = bossLetterSize;
		_captionBossS2.color.set(1f, 0f, 1f, CAPTION_OPACITY);
		_captionBossS2.x = middle + (1 * bossLetterSize);
		_captionBossS2.y = _stage.height();
		_captionBossS2.visible = _captionBossS2.touchable = false;
		_captionBossS2.originY = bossLetterSize / 2f;
		_captionBossS2.originX = middle - _captionBossS2.x;
		_mainGroup.addActor(_captionBossS2);
		
		_arrow = new TargetArrowActor(_mainGroup, Color.RED);
		
		_score = new MainScoreLabel(_mainGroup);
		
		_lifeButtons = new ButtonActor[4];
		for(int n = 0; n < 4; n++)
		{
			ButtonActor lb = new ButtonActor(Z.texture("extra-life"), null);
			lb.height = ICON_SIZE;
			lb.width = (((float)lb.getTexture().getRegionWidth()) / ((float)lb.getTexture().getRegionHeight())) * lb.height;
			lb.x = (ICON_SIZE/4f) + (n * lb.width);
			lb.y = (_stage.height() - lb.height) - (ICON_SIZE/4f);
			lb.visible = lb.touchable = false;
			lb.color.set(Color.RED);
			
			_lifeButtons[n] = lb;
			_mainGroup.addActor(lb);
		}
		
		_badgeIndicatorQueue = new NotificationQueue(_stage);
	}
	
	private ButtonActor initCaption(TextureRegion tex, Color color)
	{
		ButtonActor r = new ButtonActor(tex, null);
		r.width = (((float)tex.getRegionWidth()) / ((float)tex.getRegionHeight())) * _captionMegaLaser.height;
		r.height = _captionMegaLaser.height;
		r.x = (_stage.width() - r.width) / 2f;
		r.y = r.height /2f;// (_stage.height() - r.height) /2f;
		r.color.set(color);
		r.visible = r.touchable = false;
		r.originX = r.width / 2f;
		r.originY = r.height / 2f;
		_mainGroup.addActor(r);	
		
		return r;
	}
	
	public Stage stage()
	{
		return _stage;
	}
	
	public void dispose()
	{		
		_stage.dispose();
	}

	@Override
	public boolean keyDown(int keycode)
	{
		switch(keycode)
		{
//			case Keys.Z:
//			case Keys.MENU:
//			case Keys.X:
			case Keys.BACK:
			case Keys.ESCAPE:		
				return true;
			default: return false;
		}
	}
	
	public void fadeOut()
	{
		//Timeline.createParallel()
		//	.push(
		
		_mainGroup.visible = false;
				
		//Tween.to(_score, ActorTweener.VAL_COLOR_RGBA, 500).target(1f, 1f, 116f/255f, 0f).start(Game.TweenManager);
	}
	
	public void fadeIn()
	{
		//Timeline.createParallel()
		//	.push(
		
		_mainGroup.visible = true;
		
		//Tween.to(_score, ActorTweener.VAL_COLOR_RGBA, 500).target(1f, 1f, 116f/255f, 1f).start(Game.TweenManager);
	}

	@Override
	public boolean keyUp(int keycode)
	{
		switch(keycode)
		{
//			case Keys.Z:
//				Z.sim().start(Z.script.prev());				
//				break;
//			case Keys.MENU:
//			case Keys.X:
//				Z.sim().advanceScript();
//				return true;
			case Keys.BACK:
			case Keys.ESCAPE:				
				Game.ScreenManager.push(new PauseScreen());				
				return true;
		}
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
		Z.renderer().screenToWorld(Game.workingVector2a.set(x, y));		
		Z.screen.sim().ship().tap(Game.workingVector2a.x, Game.workingVector2a.y);
		Z.sim.fireEvent(ZipZapSim.EV_TAP, null);
		return true;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button)
	{
		Z.screen.sim().ship().release();
		return true;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer)
	{
		Z.renderer().screenToWorld(Game.workingVector2a.set(x, y));		
		Z.screen.sim().ship().slide(Game.workingVector2a.x, Game.workingVector2a.y);
		return true;
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
	
	public void render()
	{
		_stage.draw();		
	}
	
	public TextureRegion getNumberTexture(char c)
	{
		return _captionNumberTextures.get(c);
	}

	public void announceWave(int n)
	{
		hideCaptions();
		
		_captionWave.x = - _captionWave.width;
		_captionWave.visible = true;
		
		Timeline
			.createSequence()
			.push(Tween
				.to(_captionWave, ActorTweener.VAL_POS_XY, 500)
				.target((_stage.width()/2f) - (_captionWave.height * 3.5f), _captionWave.y)
				.ease(Quad.OUT))			
			.pushPause(200)
			.push(Tween
				.to(_captionWave, ActorTweener.VAL_POS_XY, 500)
				.target(- _captionWave.width, _captionWave.y)
				.ease(Quad.IN))
			.start(Game.TweenManager);
		
		String str = String.format("%02d", n);
		
		_captionWaveNumber1.setTexture(_captionNumberTextures.get(str.charAt(0)));
		_captionWaveNumber1.x = _stage.width();
		_captionWaveNumber1.visible = true;
		if(str.charAt(0) == '1')
			_captionWaveNumber1.width = _captionWaveNumber1.height * (3f/5f);
		else _captionWaveNumber1.width = _captionWaveNumber1.height;
		
		Timeline
			.createSequence()
			.push(Tween
				.to(_captionWaveNumber1, ActorTweener.VAL_POS_XY, 500)
				.target((_stage.width()/2f) + (_captionWave.height * 1.5f), _captionWaveNumber1.y)
				.ease(Quad.OUT))			
			.pushPause(200)
			.push(Tween
				.to(_captionWaveNumber1, ActorTweener.VAL_POS_XY, 500)
				.target(_stage.width(), _captionWave.y)
				.ease(Quad.IN))
			.start(Game.TweenManager);
		
		_captionWaveNumber2.setTexture(_captionNumberTextures.get(str.charAt(1)));
		_captionWaveNumber2.x = _stage.width() + _captionWaveNumber1.width;
		_captionWaveNumber2.visible = true;
		if(str.charAt(1) == '1')
			_captionWaveNumber2.width = _captionWaveNumber2.height * (3f/5f);
		else _captionWaveNumber2.width = _captionWaveNumber2.height;
		
		Timeline
			.createSequence()
			.push(Tween
				.to(_captionWaveNumber2, ActorTweener.VAL_POS_XY, 500)
				.target((_stage.width()/2f) + (_captionWave.height * 1.5f) + _captionWaveNumber1.width, _captionWaveNumber2.y)
				.ease(Quad.OUT))			
			.pushPause(200)
			.push(Tween
				.to(_captionWaveNumber2, ActorTweener.VAL_POS_XY, 500)
				.target(_stage.width() + _captionWaveNumber1.width, _captionWave.y)
				.ease(Quad.IN))
			.start(Game.TweenManager);
		
		if(n > 1)
			Z.sfx.play(SFX_LEVEL_UP);
	}
	
	private final TweenCallback annouceCompleteCallback = new TweenCallback()
	{
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			if(type == TweenCallback.COMPLETE)
			{
				Actor a = (Actor)source.getUserData();
				a.visible = false;
			}
		}
	};
	
	private Timeline _bossTween;
	private final TweenCallback playBossLetterSoundCallback = new TweenCallback()
	{		
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			Z.sfx.play(SFX_BOSS_LETTER, 2f);
		}
	};
	
	private final TweenCallback playBossLettersExpandSoundCallback = new TweenCallback()
	{
		
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			Z.sfx.play(51, 0.5f);
		}
	};	

	@Override
	public void onSimulationEvent(int eventType, Object argument)
	{
		switch(eventType)
		{
			case ZipZapSim.EV_NUM_LIVES_CHANGED:
				
				refreshLivesDisplay();
				
				break;
			
			case ZipZapSim.EV_POWERUP_MEGALASER:
				
				announce(_captionMegaLaser, false);
				
				break;
				
			case ZipZapSim.EV_POWERUP_BOMB:
				
				announce(_captionBomb, false);
				
				break;
				
			case ZipZapSim.EV_POWERUP_SHIELD:
				
				announce(_captionShield, false);
				
				//announceShield();
				break;
				
			case ZipZapSim.EV_SET_SCORE:
				
				Game.TweenManager.killTarget(this);
				_displayScore = Z.screen.sim().score();
				_score.setScore(_displayScore);
				
				break;
				
			case ZipZapSim.EV_SCORE:
				
				Vector3 v = (Vector3)argument;
				
				int score = (int)v.z;
				ScoreMarker.spawn(Z.v1().set(v.x, v.y), score, _mainGroup);
				
				Game.TweenManager.killTarget(this);
				Tween
					.to(this, 0, 1000)
					.target(Z.screen.sim().score())
					.ease(Quad.IN)
					.start(Game.TweenManager);
				
				_score.setScore(Z.screen.sim().score());
				
				if(Z.screen.sim().comboLevel() > 1)
				{
					Game.TweenManager.killTarget(_score);
					_score.scaleX = _score.scaleY = (Z.screen.sim().comboLevel() == 9 ? 2f : 1.25f);
					Tween.to(_score, ActorTweener.VAL_SCALE, 1500).target(1f).ease(Quad.INOUT).start(Game.TweenManager);
				}
				
				break;
				
			case Sim.EV_START:
				
				Game.TweenManager.killTarget(this);
				_displayScore = Z.screen.sim().score();
				_score.setScore(Z.screen.sim().score());
				refreshLivesDisplay();
				
				break;
				
			case ZipZapSim.EV_COMBO:
				
				if(Z.screen.sim().comboLevel() > 1)
				{
					announce(_comboCaptions[Z.screen.sim().comboLevel()], Z.screen.sim().comboLevel() == 9, 1 + 0.1f * (Z.screen.sim().comboLevel()-2));
				}

				break;
				
			case ZipZapSim.EV_TARGET_CHANGED:
			
				_arrow.visible = Z.screen.sim().target() != null;
			
				break;
				
			case Sim.EV_ENQUEUE_NOTIFICATION:		
				
				Notification n = (Notification)argument;				
				_badgeIndicatorQueue.enqueue(n.icon, n.worldLoc, n.color);
				
				break;
				
			case Sim.EV_DEQUEUE_NOTIFICATION:
				
				_badgeIndicatorQueue.dequeue();
				
				break;
		}
	}
	
	private void refreshLivesDisplay()
	{
		for(int n = 0; n < 4; n++)
		{
			_lifeButtons[n].visible = Z.ship().lives() > n;
		}
	}
	private void announce(Actor caption, boolean mega)
	{
		announce(caption, mega, 1f);
	}

	private void announce(Actor caption, boolean mega, float scale)
	{
		hideCaptions();
				
		if(mega)
		{
			caption.color.set(1, 0, 1, 0);
			caption.scaleX = caption.scaleY = 1.25f;
			caption.visible = true;
			
			Timeline.createParallel()
				.push(Tween
						.to(caption, ActorTweener.VAL_SCALE, 50)
						.target(2f)
						.ease(Bounce.INOUT)
						.repeatYoyo(12, 0))
				.push(Timeline
						.createSequence()
						.push(Tween
							.to(caption, ActorTweener.VAL_COLOR_RGBA, 250)
							.target(1, 0, 1, CAPTION_OPACITY)
							.ease(Bounce.OUT))
						.push(Tween
							.to(caption, ActorTweener.VAL_COLOR_RGBA, 1125)
							.target(1, 0, 1, 0)
							.ease(Quad.INOUT)))
				.setCallback(annouceCompleteCallback)
				.setUserData(caption)
				.start(Game.TweenManager);
		}
		else
		{
			caption.color.set(caption.color.r,caption.color.g, caption.color.b, 0);
			caption.y = (caption.height/2f)  /* ((_stage.height() - _captionBomb.height) /2f) */ - 2 * caption.height;
			caption.visible = true;
			caption.scaleX = caption.scaleY = scale;
			
			Timeline
				.createSequence()
				.push(Timeline
						.createParallel()
						.push(Tween
								.to(caption, ActorTweener.VAL_COLOR_RGBA, 500)
								.target(caption.color.r,caption.color.g, caption.color.b, CAPTION_OPACITY)
								.ease(Quad.OUT))
						.push(Tween
								.to(caption, ActorTweener.VAL_POS_XY, 500)
								.target(caption.x, caption.height/2f /* (_stage.height() - caption.height) /2f */)
								.ease(Elastic.OUT)))
				.pushPause(250)
				.push(Timeline
						.createParallel()
						.push(Tween
								.to(caption, ActorTweener.VAL_COLOR_RGBA, 500)
								.target(caption.color.r,caption.color.g, caption.color.b, 0)
								.ease(Quad.IN))
						.push(Tween
								.to(caption, ActorTweener.VAL_POS_XY, 500)
								.target(caption.x, (caption.height/2f) - 2 * caption.height /* ((_stage.height() - caption.height) /2f) - 2 * caption.height */)
								.ease(Quad.IN)))
				.setCallback(annouceCompleteCallback)
				.setUserData(caption)
				.start(Game.TweenManager);
		}
	}
	
	private void hideCaptions()
	{
		Game.TweenManager.killTarget(_captionWave);
		_captionWave.visible = false;
		
		Game.TweenManager.killTarget(_captionWaveNumber1);
		_captionWaveNumber1.visible = false;
		
		Game.TweenManager.killTarget(_captionWaveNumber2);
		_captionWaveNumber2.visible = false;
		
		Game.TweenManager.killTarget(_captionBomb);
		_captionBomb.visible = false;
		
		Game.TweenManager.killTarget(_captionMegaLaser);
		_captionMegaLaser.visible = false;
		
		Game.TweenManager.killTarget(_captionMeteorStorm);// TODO Auto-generated method stub
		
		_captionMeteorStorm.visible = false;
		
		Game.TweenManager.killTarget(_captionShield);		
		_captionShield.visible = false;
		
		Game.TweenManager.killTarget(_captionVictory);
		_captionVictory.visible = false;
		
		for(int n = 2; n < 10; n++)
		{
			Game.TweenManager.killTarget(_comboCaptions[n]);
			_comboCaptions[n].visible = false;
		}
		
		if(_bossTween != null)
		{
			_bossTween.kill();
			_bossTween = null;
			
			_captionBossB.visible = false;
			_captionBossO.visible = false;
			_captionBossS1.visible = false;
			_captionBossS2.visible = false;
		}
	}

	@Override
	public int getValues(Hud target, int tweenType, float[] returnValues)
	{
		returnValues[0] = target._displayScore;
		return 1;
	}

	@Override
	public void setValues(Hud target, int tweenType, float[] newValues)
	{
		target._displayScore = (int)newValues[0];
		_score.setScore(_displayScore);
	}
	
	public void announceVictory()
	{
		hideCaptions();
		
		_captionVictory.x = _stage.width();
		_captionVictory.visible = true;
		
		Tween.to(_captionVictory, ActorTweener.VAL_POS_XY, 9000).target(-_captionVictory.width, 0).ease(Linear.INOUT).start(Game.TweenManager);
	}

	public void announceBoss()
	{
		hideCaptions();
		
		_captionBossB.color.set(1f, 1f, 116f/255f, 1);
		_captionBossO.color.set(1f, 1f, 116f/255f, 1);
		_captionBossS1.color.set(1f, 1f, 116f/255f, 1);
		_captionBossS2.color.set(1f, 1f, 116f/255f, 1);
		
		_captionBossB.scaleX = _captionBossB.scaleY = 1f;
		_captionBossO.scaleX = _captionBossO.scaleY = 1f;
		_captionBossS1.scaleX = _captionBossS1.scaleY = 1f;
		_captionBossS2.scaleX = _captionBossS2.scaleY = 1f;
		
		_captionBossB.y = _stage.height();
		_captionBossB.visible = true;		
		_captionBossO.y = _stage.height();
		_captionBossO.visible = true;
		_captionBossS1.y = _stage.height();
		_captionBossS1.visible = true;
		_captionBossS2.y = _stage.height();
		_captionBossS2.visible = true;
		
		float targetY = (_stage.height() - _captionBossB.height) / 2f;
		
		_bossTween = Timeline.createSequence()
			.push(Tween.call(playBossLetterSoundCallback))
			.push(Tween.to(_captionBossB, ActorTweener.VAL_POS_XY, 400).target(_captionBossB.x, targetY).ease(Bounce.OUT))
			.push(Tween.call(rumbleCallback))
			.pushPause(150)
			.push(Tween.call(playBossLetterSoundCallback))
			.push(Tween.to(_captionBossO, ActorTweener.VAL_POS_XY, 400).target(_captionBossO.x, targetY).ease(Bounce.OUT))
			.push(Tween.call(rumbleCallback))
			.pushPause(150)
			.push(Tween.call(playBossLetterSoundCallback))
			.push(Tween.to(_captionBossS1, ActorTweener.VAL_POS_XY, 400).target(_captionBossS1.x, targetY).ease(Bounce.OUT))
			.push(Tween.call(rumbleCallback))
			.pushPause(150)
			.push(Tween.call(playBossLetterSoundCallback))
			.push(Tween.to(_captionBossS2, ActorTweener.VAL_POS_XY, 400).target(_captionBossS2.x, targetY).ease(Bounce.OUT))
			.push(Tween.call(rumbleCallback))
			.pushPause(150)		
			.push(Tween.call(playBossLettersExpandSoundCallback ))
			.push(Timeline.createParallel()
					.push(Tween.to(_captionBossB, ActorTweener.VAL_SCALE, 500).target(6))
					.push(Tween.to(_captionBossB, ActorTweener.VAL_COLOR_RGBA, 500).target(1f, 1f, 116f/255f, 0))
					.push(Tween.to(_captionBossO, ActorTweener.VAL_SCALE, 500).target(6))
					.push(Tween.to(_captionBossO, ActorTweener.VAL_COLOR_RGBA, 500).target(1f, 1f, 116f/255f, 0))
					.push(Tween.to(_captionBossS1, ActorTweener.VAL_SCALE, 500).target(6))
					.push(Tween.to(_captionBossS1, ActorTweener.VAL_COLOR_RGBA, 500).target(1f, 1f, 116f/255f, 0))
					.push(Tween.to(_captionBossS2, ActorTweener.VAL_SCALE, 500).target(6))
					.push(Tween.to(_captionBossS2, ActorTweener.VAL_COLOR_RGBA, 500).target(1f, 1f, 116f/255f, 0)))
			.setCallbackTriggers(TweenCallback.COMPLETE)
			.setCallback(bossTweenCompleteCallback)
			.start(Game.TweenManager);
	}
	
	private final TweenCallback rumbleCallback = new TweenCallback()
	{		
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			Z.renderer().shakeCamera(0.75f, 0.75f);			
		}
	};
	
	private final TweenCallback bossTweenCompleteCallback = new TweenCallback()
	{		
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			_bossTween = null;
		}
	};
}
