package com.monkeysonnet.zipzap.screens;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.monkeysonnet.engine.ColorTools;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.ICallback;
import com.monkeysonnet.engine.IDialogResultHandler;
import com.monkeysonnet.engine.IScreen;
import com.monkeysonnet.engine.ISimulationEventHandler;
import com.monkeysonnet.zipzap.Hud;
import com.monkeysonnet.zipzap.SimRenderer;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;
import com.monkeysonnet.zipzap.script.ISectorScript;
import com.monkeysonnet.zipzap.script.Script;
import com.monkeysonnet.zipzap.script.TitleScreenController2;

public class ZipZapScreen implements IScreen, ISimulationEventHandler, IDialogResultHandler
{
	private static final int SFX_COMBO = 10;
	private static final int SFX_UC_CHARGE = 41;
	private ZipZapSim _sim;
	private Hud _hud;
	private boolean _isFocussed;
	private SimRenderer _renderer;
	
	private boolean _restore;
	private int _restoreSector, _restoreScore, _restoreLives, _restoreWave, _restoreBubble;
	
	public ZipZapScreen()
	{
		Z.screen = this;		
		_renderer = new SimRenderer();			
	}

	public ZipZapSim sim()
	{
		return _sim;
	}

	@Override
	public void show()
	{	
		_hud = new Hud();
		_hud.fadeOut();
		
		Z.renderer = _renderer;
		
		if(_restore)
		{
			_restore = false;
			
			Z.script = Script.create(_restoreSector, _restoreWave);
			_sim = new ZipZapSim(this);	
			Z.sim = _sim;
			Z.sim.start();
			_sim.setScore(_restoreScore);
			Z.ship().restoreState(_restoreLives, _restoreBubble);
		}		
		else if(_sim == null)
		{
			initBackgroundDemo();
		}
		
		Z.sim = _sim;
	}
	
	public void initBackgroundDemo()
	{
		if(_sim != null)
			_sim.dispose();
		
		_sim = new ZipZapSim(this);	
		Z.sim = _sim;
		_sim.start(TitleScreenController2.create());
	}
	
	@Override
	public void pause()
	{
		Game.ScreenManager.push(new PauseScreen());
	}

	@Override
	public void focus()
	{
		_isFocussed = true;				
		Z.renderer.toColour();		
		Gdx.input.setInputProcessor(_hud);	
		_hud.fadeIn();		
	}

	@Override
	public void render()
	{
		_sim.update(
				Z.renderer.cam().position.x - (Z.renderer.cam().viewportWidth/2f), 
				Z.renderer.cam().position.y - (Z.renderer.cam().viewportHeight/2f), 
				Z.renderer.cam().viewportWidth, 
				Z.renderer.cam().viewportHeight);
		
		Z.renderer.renderBackground();
				
		// hack - somehow getting the ships origin changes it.
		// if we don;t do this then it changes when we set the camera position
		// meaning the ship is not always drawn centre screen. :S
		Z.ship().origin();
		
		Z.renderer.renderForeground(_sim);
		
		_hud.render();
	}

	@Override
	public void blur()
	{
		_isFocussed = false;
		Z.renderer.toBackAndWhite();
		_hud.fadeOut();
	}

	@Override
	public void hide()
	{
		dispose();
	}
	
	public void dispose()
	{
		if(Z.script != null)
		{
			Z.script.dispose();
			Z.script = null;
		}
		
		if(_sim != null)
			_sim.dispose();
		
		if(Z.sim == _sim)
			Z.sim = null;	
		
		_sim = null;
	
		if(_hud != null)
			_hud.dispose();
		_hud = null;
	}

	@Override
	public boolean isFullScreen()
	{
		return true;
	}

	@Override
	public void serialize(Preferences dict)
	{
		if(Z.script != null && Z.script instanceof ISectorScript)
		{			
			dict.putInteger("sector", ((ISectorScript)Z.script).sectorNum());
			dict.putInteger("wave", Z.script.level());
			dict.putInteger("score", _sim.score());
			dict.putInteger("lives", Z.ship().lives());	
			dict.putInteger("bubble", Z.ship().bubble().type());
		}
	}
	
	@Override
	public void deserialize(Preferences dict)
	{
		if(dict.contains("sector"))
		{
			_restore = true;
			_restoreLives = dict.getInteger("lives");
			_restoreScore = dict.getInteger("score");
			_restoreSector = dict.getInteger("sector");
			_restoreWave = dict.getInteger("wave");
			_restoreBubble = dict.getInteger("bubble");
		}
	}
	
	public Hud hud()
	{
		return _hud;
	}
	
	@Override
	public void onSimulationEvent(int eventType, Object argument)
	{
		_hud.onSimulationEvent(eventType, argument);
		Z.renderer.onSimulationEvent(eventType, argument);
		Z.achievments.onSimulationEvent(eventType, argument);
		
		switch(eventType)
		{
			case ZipZapSim.EV_GAME_OVER:
				
				Tween.call(new TweenCallback()
				{
					@Override
					public void onEvent(int type, BaseTween<?> source)
					{
						Z.achievementsScreen.doAchievements(achievementsCompleteCallback);
					}
				})
				.delay(2000)
				.start(Game.TweenManager);
				
				break;
				
			case ZipZapSim.EV_BEGIN_UC_CHARGE:
				
				Z.sfx.stop(SFX_UC_CHARGE);
				Z.sfx.play(SFX_UC_CHARGE);
				
				break;
				
			case ZipZapSim.EV_END_UC_CHARGE:
				
				Z.sfx.stop(SFX_UC_CHARGE);
				
				break;
				
//			case ZipZapSim.EV_WAVE_COMPLETE:
//
//				Z.sim().advanceScript();
//								
//				break;
				
			case ZipZapSim.EV_SCRIPT_COMPLETE:
				
				Game.ScreenManager.push(Z.titleScreen);
				Game.ScreenManager.push(new SectorsScreen());
				
				break;
				
			case ZipZapSim.EV_COMBO:
				
				if(Z.screen.sim().comboLevel() == 9)
					Z.renderer.backgroundColor(ColorTools.darken(Color.MAGENTA));
				
				if(Z.screen.sim().comboLevel() > 1)
				{
					long l = Z.sfx.play(SFX_COMBO, 1.5f, false);
					float p = (((float)Z.screen.sim().comboLevel() - 2f) / 10f) + 1f;
					Z.sfx.setPitch(SFX_COMBO, l, p);
				}
				
				
				break;
				
			case ZipZapSim.EV_COMBO_END:
				
				Z.renderer.backgroundColor(SimRenderer.defaultBackgroundColor);
				
				break;
				
			case ZipZapSim.EV_EXPLOSION_SMALL:
				
				if(_isFocussed)
					Z.sfx.explosionSmall.play();
				
				break;
				
			case ZipZapSim.EV_EXPLOSION_MEDIUM:
				
				if(_isFocussed)
					Z.sfx.explosionMedium.play();
				
				break;
				
			case ZipZapSim.EV_LASER_SMALL:
				
				if(_isFocussed)
					Z.sfx.laserSmall.play();
				
				break;
		}
		
		if(eventType <= -1000 && _isFocussed)
		{			
			Z.sfx.play((-eventType)-1000);
		}
	}

	@Override
	public void onDialogResult(Object sender)
	{
	}
	
	private final ICallback achievementsCompleteCallback = new ICallback()
	{
		@Override
		public void callback(Object arg)
		{
			Game.ScreenManager.push(Z.titleScreen);
			Game.ScreenManager.push(new SectorsScreen());
		}
	};
}
