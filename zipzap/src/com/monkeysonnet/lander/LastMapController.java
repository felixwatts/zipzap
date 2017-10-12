package com.monkeysonnet.lander;

import com.monkeysonnet.engine.Game;
import com.monkeysonnet.zipzap.IConsoleEventHandler;
import com.monkeysonnet.zipzap.Z;

public class LastMapController extends LanderGameController
{
	private boolean _doneIntro;

	private IConsoleEventHandler outroConsoleHandler = new IConsoleEventHandler()
	{
		
		@Override
		public void tap(int row)
		{
		}
		
		@Override
		public void dismiss()
		{
			Game.ScreenManager.pop();
			L.sim.guy().lockControls = false;		
		}
		
		@Override
		public void callback(Object arg)
		{
		}
		
		@Override
		public void bufferEmpty()
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
	};

	public LastMapController()
	{
		super(1);
	}

	@Override
	public void onSimulationEvent(int eventType, Object argument)
	{	
		super.onSimulationEvent(eventType, argument);
		
		switch(eventType)
		{
			case LanderSim.EV_START:
			{					
				
			}
			break;
		}
	}
	
	@Override
	public void update(float dt)
	{
		if(!_doneIntro)
		{
			if(!Z.prefs.getBoolean("jetpak-outro", false))
			{
				Z.prefs.putBoolean("jetpak-outro", true);
				Z.prefs.flush();
				L.sim.guy().lockControls = true;				
				Z.console().clear()
				.setAvatar(Z.texture("miner"), Z.colorTutorial)
				.setColour(Z.colorUi, Z.colorTutorialBg)
				.write("Well, gee rookie.. it looks like youve completed Jet-Pak training...")
				.touch()
				.clear()
				.write("No one has ever done that before...")
				.touch()
				.clear()
				.write("You know, I'm not really sure what to do right now...")
				.touch()
				.clear()
				.write("Here, have a shiny thing...")
				.touch()
				.clear()
				.write("And here's our hardest obstacle course...")
				.touch()
				.clear()
				.write("Play safe!")
				.touch()
				.clear();
			
				Z.console().setHandler(outroConsoleHandler );
				Game.ScreenManager.push(Z.consoleScreen);
			}
			
			_doneIntro = true;
		}
	}
	
}
