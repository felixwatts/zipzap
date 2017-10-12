package com.monkeysonnet.zipzap.script;

import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.IScreen;
import com.monkeysonnet.zipzap.IGameController;
import com.monkeysonnet.zipzap.Z;

public class ScreenController implements IGameController
{
	private IScreen _screen;

	public ScreenController(IScreen screen)
	{
		_screen = screen;
	}

	@Override
	public void init()
	{
		Game.ScreenManager.push(_screen);
	}

	@Override
	public void update(float dt)
	{
	}

	@Override
	public void cleanup()
	{
		//Z.sim().setFocalPoint(Z.ship().origin());
		Z.sim().timeMultiplier.setValue(1f);
		Z.ship().ghostMode(false);
		Z.ship().lockControls(false);
	}

	@Override
	public void onSimulationEvent(int eventType, Object argument)
	{
	}

	@Override
	public void pause()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume()
	{
		// TODO Auto-generated method stub
		
	}
}
