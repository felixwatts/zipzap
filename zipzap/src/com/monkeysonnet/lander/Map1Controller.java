package com.monkeysonnet.lander;

import com.monkeysonnet.engine.Game;
import com.monkeysonnet.zipzap.IConsoleEventHandler;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.achievements.BadgeJetPakWings1;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;

public class Map1Controller extends LanderGameController
{
	private final TweenCallback playerDiedCallback = new TweenCallback()
	{		
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			_tweenPlayerDied = null;
			
			Z.console().clearNow()
				.setAvatar(Z.texture("miner"), Z.colorTutorial)
				.setColour(Z.colorTutorial, Z.colorTutorialBg)
				.write("dag-NAMMIT!")
				.touch()
				.clear()
				.write("Jim! ")
				.pause(750)
				.write("Send in the next recruit!");
			
			Z.console().setHandler(playerDiedConsoleHandler);
			Game.ScreenManager.push(Z.consoleScreen);
		}
	};

	private boolean _haveDoneIntro;
	
	public void update(float dt) 
	{
		if(!_haveDoneIntro)
		{
			if(!Z.prefs.getBoolean("jetpak-map1-intro", false))
			{
				Z.prefs.putBoolean("jetpak-map1-intro", true);
				Z.prefs.flush();
				L.sim.guy().lockControls = true;				
				doIntro();
			}
			
			_haveDoneIntro = true;
		}
	};
	
	private final IConsoleEventHandler playerDiedConsoleHandler = new IConsoleEventHandler()
	{
		
		@Override
		public void tap(int row)
		{
		}
		
		@Override
		public void dismiss()
		{
			Game.ScreenManager.pop();
			
			if(_tweenRestart != null)
				_tweenRestart.kill();
			
			_tweenRestart = Tween.call(restartCallback).delay(1000).start(L.sim.tweens());
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
	
	private final IConsoleEventHandler outroConsoleHandler = new IConsoleEventHandler()
	{
		
		@Override
		public void tap(int row)
		{
		}
		
		@Override
		public void dismiss()
		{
			Game.ScreenManager.pop();
			
			if(_tweenNextLevel != null)
				_tweenNextLevel.kill();
			
			_tweenNextLevel = Tween.call(nextLevelCallback).delay(1000).start(L.sim.tweens());
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
			// TODO Auto-generated method stub
			
		}

		@Override
		public void cancelInput()
		{
			// TODO Auto-generated method stub
			
		}
	};
	
	private final IConsoleEventHandler introConsoleHandler = new IConsoleEventHandler()
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
			// TODO Auto-generated method stub
			
		}

		@Override
		public void cancelInput()
		{
			// TODO Auto-generated method stub
			
		}
	};

	private Tween _tweenPlayerDied;
	
	public Map1Controller()
	{
		super(0);
	}
	
	private void doIntro()
	{
		Z.console().clearNow()
			.setAvatar(Z.texture("miner"), Z.colorTutorial)
			.setColour(Z.colorTutorial, Z.colorTutorialBg)
			.write("Stand to attention rookie!")
			.touch()
			.clear()				
			.write("Ready your Jet Pak...")
			.touch()
			.clear()
			.write("On my mark, I want you to ascend slowly and manouvre VERY CAREFULLY towards the yellow landing pad...")
			.touch()
			.clear()
			.write("Land gently on the pad...")
			.touch()
			.clear()
			.write("Try to forget any rumours you may have heard regarding the quality of engineering on the United States Army Mk1 Jet Pak.")
			.touch()
			.clear()
			.write("Go!");
		
		Z.console().setHandler(introConsoleHandler);
		Game.ScreenManager.push(Z.consoleScreen);
	}

	@Override
	public void onSimulationEvent(int eventType, Object argument)
	{
		if(!BadgeJetPakWings1.instance().isEarned())
		{	
			switch(eventType)
			{
				case LanderSim.EV_START:
	
					if(!Z.prefs.getBoolean("jetpak-map1-intro", false))
					{
						Z.prefs.putBoolean("jetpak-map1-intro", true);
						Z.prefs.flush();
						L.sim.guy().lockControls = true;				
						doIntro();
					}
					
					super.onSimulationEvent(eventType, argument);
					break;
				case LanderSim.EV_LEVEL_COMPLETE:
				{		
					if(!BadgeJetPakWings1.instance().isEarned())	
					{
						Z.console().clearNow()
						.setAvatar(Z.texture("miner"), Z.colorTutorial)
						.setColour(Z.colorTutorial, Z.colorTutorialBg)
						.write("You're still alive!")
						.touch()
						.clear()
						.write("I must admit - I never saw that coming...")
						.touch()
						.clear()
						.write("Well done rookie!")
						.touch()
						.clear()
						.write("Now, let's see if you can do the same thing in a series of increasingly hostile environments.");
					
						Z.console().setHandler(outroConsoleHandler);
						Game.ScreenManager.push(Z.consoleScreen);
					}
					else super.onSimulationEvent(eventType, argument);
				}
					break;
				
				case LanderSim.EV_PLAYER_DIED:
					
					if(_tweenPlayerDied != null)
						_tweenPlayerDied.kill();
					
					if(!BadgeJetPakWings1.instance().isEarned())					
						_tweenPlayerDied = Tween.call(playerDiedCallback ).delay(3000).start(L.sim.tweens());
					else super.onSimulationEvent(eventType, argument);
					
					break;
					
				default:
					super.onSimulationEvent(eventType, argument);
					break;
			}
		}
		else super.onSimulationEvent(eventType, argument);
	}
	
	@Override
	public void cleanup()
	{
		super.cleanup();
		
		if(_tweenPlayerDied != null)
		{
			_tweenPlayerDied.kill();
			_tweenPlayerDied = null;
		}
	}
}
