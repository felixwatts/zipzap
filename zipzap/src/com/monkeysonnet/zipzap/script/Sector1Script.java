package com.monkeysonnet.zipzap.script;

import com.badlogic.gdx.Gdx;
import com.monkeysonnet.engine.ICallback;
import com.monkeysonnet.zipzap.IGameController;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.entities.Gnat;
import com.monkeysonnet.zipzap.entities.Meteor2;
import com.monkeysonnet.zipzap.entities.PowerUp;
import com.monkeysonnet.zipzap.entities.ShootingSeeker;
import com.monkeysonnet.zipzap.entities.SpinningSeeker;
import com.monkeysonnet.zipzap.screens.PurchaseScreen;

public class Sector1Script extends Script implements ISectorScript
{
	// boss: kobo
	// theme: red ships
	
	// spinning seeker
	// shooting seeker
	// gnat
	
	public Sector1Script(int wave)
	{
		super(wave);
	}
	
	@Override
	public void dispose()
	{
	}
	
	@Override 
	protected IGameController get(int level)
	{
		switch(level)
		{
			case 0:
				if(Z.prefs.getBoolean("first-show-zipzap", true))
				{			
					Z.prefs.putBoolean("first-show-zipzap", false);
					Z.prefs.flush();
					
					Z.console().clearNow()
						.setAvatar(Z.texture("miner"), Z.colorTutorial)
						.setColour(Z.colorTutorial, Z.colorTutorialBg)
						.write("Welcome to Space, Captain!")
						.touch()
						.clear()
						.write("The rules out here are pretty simple...")
						.touch()
						.clear()
						.write("Shoot stuff to earn points...")
						.touch()
						.clear()
						.write("Earn 1000 points to advance to the next wave...")
						.touch()
						.clear()
						.write("Defeat the boss in wave 10 to unlock the next sector.")
						.touch()
						.clear()
						.write("Over to you, Captain!");
				
					return new ConsoleCutsceneController();
				}
				else 
				{
					return new TimedWave(0);
				}
			
			case 1:
				
				Z.music.play(Gdx.files.internal("music/sector-1.ogg"));
				Z.hud().announceWave(1);				
				return new ScoreScript(0, new AutoSpawner(Meteor2.activeCount, SpawnEvent.TYPE_METEOR2, 8, 8, 5, 7, 0, 0, PowerUp.TYPE_NONE, 0, false));
				
			case 2:
			{
				
				Z.hud().announceWave(2);	
				
				IAutoSpawner ss = new AutoSpawner(SpinningSeeker.activeCount, SpawnEvent.TYPE_SPINNING_SEEKER, 1, 1, 0, 0, 0, 0, 0, 0, false);
				IAutoSpawner sm = new AutoSpawner(Meteor2.activeCount, SpawnEvent.TYPE_METEOR2, 8, 8, 7, 7, 0, 0, PowerUp.TYPE_NONE, 0, false);
				
				return new ScoreScript(1000, new MultiAutoSpawner(ss, sm))
					.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_METEOR2).int1(PowerUp.TYPE_SHIELD).after(50));
			}
			case 3:
			{
				Z.hud().announceWave(3);	
				
				IAutoSpawner ss = new AutoSpawner(SpinningSeeker.activeCount, SpawnEvent.TYPE_SPINNING_SEEKER, 2, 2, 0, 0, 0, 0, 0, 0, false);
				IAutoSpawner sm = new AutoSpawner(Meteor2.activeCount, SpawnEvent.TYPE_METEOR2, 8, 8, 7, 7, 0, 0, PowerUp.TYPE_NONE, 0, false);
				
				return new ScoreScript(2000, new MultiAutoSpawner(ss, sm))
					.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_METEOR2).int1(PowerUp.TYPE_SHIELD).after(50));
			}
			case 4:
				Z.hud().announceWave(4);				
				{
					IAutoSpawner ss1 = new AutoSpawner(ShootingSeeker.activeCount, SpawnEvent.TYPE_SHOOTING_SEEKER, 1, 1, 0, 0, 0, 0, 0, 0, false);
					IAutoSpawner ss2 = new AutoSpawner(SpinningSeeker.activeCount, SpawnEvent.TYPE_SPINNING_SEEKER, 2, 2, 0, 0, 0, 0, 0, 0, false);
					IAutoSpawner sm = new AutoSpawner(Meteor2.activeCount, SpawnEvent.TYPE_METEOR2, 8, 8, 7, 7, 0, 0, PowerUp.TYPE_NONE, 0, false);
					
					return new ScoreScript(3000, new MultiAutoSpawner(ss1, ss2, sm))
						.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_METEOR2).int1(PowerUp.TYPE_SHIELD).after(50));
				}
				
			case 5:
				
				Z.hud().announceWave(5);				
				return new ScoreScript(4000, new AutoSpawner(Meteor2.activeCount, SpawnEvent.TYPE_METEOR2, 16, 24, 7, 14, 45, 45, PowerUp.TYPE_NONE, 0, false))
					.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_METEOR2).int1(PowerUp.TYPE_SHIELD).after(0));
			
			case 6:
				Z.hud().announceWave(6);				
				return new ScoreScript(5000, new AutoSpawner(SpinningSeeker.activeCount, SpawnEvent.TYPE_SPINNING_SEEKER, 4, 16, 0, 0, 0, 0, PowerUp.TYPE_NONE, 0, false));
				
			case 7:
				Z.hud().announceWave(7);
				return new ScoreScript(6000, new AutoSpawner(Gnat.activeCommanderCount, SpawnEvent.TYPE_GNAT_SQUADRON, 1, 1, 0, 0, 0, 0, 3, 0, false));
				
			case 8:
			{
				IAutoSpawner s1 = new AutoSpawner(ShootingSeeker.activeCount, SpawnEvent.TYPE_SHOOTING_SEEKER, 2, 2, 0, 0, 0, 0, 0, 0, false);
				IAutoSpawner s2 = new AutoSpawner(Meteor2.activeCount, SpawnEvent.TYPE_METEOR2, 14, 22, 8, 10, 90, 90, PowerUp.TYPE_NONE, 0, false);
				
				Z.hud().announceWave(8);				
				return new ScoreScript(7000, new MultiAutoSpawner(s1, s2))
					.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_METEOR2).int1(PowerUp.TYPE_SHIELD).after(0))
//					.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_SHOOTING_SEEKER).after(20))
//					.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_SHOOTING_SEEKER).after(40))
					.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_METEOR2).after(50).int1(PowerUp.TYPE_SHIELD_UPGRADE));
//					.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_SHOOTING_SEEKER).after(60))
//					.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_SHOOTING_SEEKER).after(80));
			}	
			case 9:
				
				Z.hud().announceWave(9);				
				return new ScoreScript(8000, new AutoSpawner(Gnat.activeCommanderCount, SpawnEvent.TYPE_GNAT_SQUADRON, 2, 2, 0, 0, 0, 0, 2, 0, false));
			
			case 10:	
				
				return new AnnounceBossController();
				
			case 11:

				return new GameController()
					.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_KOBO).after(0).pos(0, 75));
				
			case 12:

				if(Z.isDemo)
				{
					PurchaseScreen s = new PurchaseScreen(new ICallback() 
					{						
						@Override
						public void callback(Object arg) 
						{
							Z.sim.advanceScript();
						}
					}, true);
					
					s.console()
						.clearNow()
						.setAvatar(Z.texture("miner"), Z.colorTutorial)
						.setColour(Z.colorTutorial, Z.colorTutorialBg)
						.write("Awsome job Captain! Why not continue your adventure in the full version? Tap below to visit Google Play.");
					
					return new ScreenController(s);
				}	
				else return null;
				
			default:
				return null;
		}
	}

	@Override
	public int sectorNum()
	{
		return 1;
	}
}
