package com.monkeysonnet.zipzap.script;

import com.badlogic.gdx.math.Vector2;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.zipzap.IConsoleEventHandler;
import com.monkeysonnet.zipzap.IGameController;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.entities.Ship;

public class ConsoleCutsceneController implements IGameController
{
	public ConsoleCutsceneController()
	{
		
	}

	@Override
	public void init()
	{
		Z.renderer().setBlackAndWhite(1);		
		Z.screen.sim().ship().beginCruise(Vector2.tmp.set(0, Ship.SPEED_NORMAL/8f));		
		Z.sim().focalPoint(Z.ship().origin());
		Game.ScreenManager.push(Z.consoleScreen);		
		Z.console().setHandler(new IConsoleEventHandler()
		{
			@Override
			public void dismiss()
			{
				Z.screen.sim().advanceScript();
			}

			@Override
			public void bufferEmpty()
			{
			}

			@Override
			public void tap(int row)
			{
			}

			@Override
			public void callback(Object arg)
			{
			}

			@Override
			public void textEntered(String text)
			{
			}

			@Override
			public void cancelInput()
			{
				// TODO Auto-generated method stub
				
			}
		});
	}

	@Override
	public void update(float dt)
	{
	}

	@Override
	public void cleanup()
	{
		Z.screen.sim().ship().endCruise();
		Game.ScreenManager.pop();
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
