package com.monkeysonnet.lander;

import com.badlogic.gdx.Preferences;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.IScreen;
import com.monkeysonnet.engine.ISimulationEventHandler;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.screens.PauseScreen;

public class LanderScreen implements IScreen, ISimulationEventHandler
{
	private Hud _hud;
	private Renderer _renderer = new Renderer();	
	
	private static final float UPDATE_BORDER_WIDTH = 60f;
	
	public LanderScreen()
	{
		Z.script = new Script();
		Z.sim = L.sim = new LanderSim(this);
		Z.renderer = _renderer;
		_hud = new Hud();
	}
	
	@Override
	public void show()
	{
		Z.sim.start();	
		Z.music.play(null);
	}

	@Override
	public void focus()
	{		
		Z.renderer.toColour();
		_hud.hide(false);	
	}
	
	@Override
	public void pause()
	{
		Game.ScreenManager.push(new PauseScreen());
	}

	@Override
	public void render()
	{
		//if(!_paused)
		L.sim.update(
				Z.renderer.cam().position.x - (Z.renderer.cam().viewportWidth/2f) - UPDATE_BORDER_WIDTH, 
				Z.renderer.cam().position.y - (Z.renderer.cam().viewportHeight/2f) - UPDATE_BORDER_WIDTH, 
				Z.renderer.cam().viewportWidth + 2 * UPDATE_BORDER_WIDTH, 
				Z.renderer.cam().viewportHeight + 2 * UPDATE_BORDER_WIDTH);
		
		_renderer.renderBackground();
		
		// hack - somehow getting the ships origin changes it.
		// if we don;t do this then it changes when we set the camera position
		// meaning the ship is not always drawn centre screen. :S
		L.sim.guy().origin();
		
		_renderer.renderForeground(Z.sim);
		
		_renderer.renderMinimap(Z.sim);
		
		_hud.render();				
	}

	@Override
	public void blur()
	{
		_hud.hide(true);
		Z.renderer.toBackAndWhite();
	}

	@Override
	public void hide()
	{
		L.sim.dispose();
	}

	@Override
	public boolean isFullScreen()
	{
		return true;
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
	public void onSimulationEvent(int eventType, Object argument)
	{
		_hud.onSimulationEvent(eventType, argument);
	}
}
