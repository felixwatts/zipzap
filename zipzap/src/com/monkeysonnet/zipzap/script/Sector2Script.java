package com.monkeysonnet.zipzap.script;

import com.badlogic.gdx.Gdx;
import com.monkeysonnet.zipzap.IGameController;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.entities.Amoeba;
import com.monkeysonnet.zipzap.entities.Centipede;
import com.monkeysonnet.zipzap.entities.Clam;
import com.monkeysonnet.zipzap.entities.MaxiJelly;
import com.monkeysonnet.zipzap.entities.Meteor2;
import com.monkeysonnet.zipzap.entities.Millipede;
import com.monkeysonnet.zipzap.entities.MiniJelly;
import com.monkeysonnet.zipzap.entities.PowerUp;
import com.monkeysonnet.zipzap.entities.Jet;

public class Sector2Script extends Script implements ISectorScript
{
	// boss: nebulon
	// theme: organic
	
	// amoeba
	// mini jelly
	// maxi jelly
	// centipede
	// millipede
	// head ?
	
	public Sector2Script(int wave)
	{
		super(wave);
	}

	@Override
	protected IGameController get(int level)
	{
		if(Z.isDemo)
			return null;
		else
		{		
			switch(level)
			{
				case 0:
				{
					Z.music.play(Gdx.files.internal("music/sector-2.ogg"));
					
					Z.hud().announceWave(1);
					
					IAutoSpawner meteorSpawner = new AutoSpawner(Meteor2.activeCount, SpawnEvent.TYPE_METEOR2, 6, 6, 7, 7, 0, 0, PowerUp.TYPE_NONE, 0, false);
					IAutoSpawner amoebaSpawner = new AutoSpawner(Amoeba.activeCountTame, SpawnEvent.TYPE_AMOEBA, 1, 4, 0, 0, 0, 0, 0, 0, false);
					
					return new ScoreScript(0, new MultiAutoSpawner(meteorSpawner, amoebaSpawner))
						.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_METEOR2).int1(PowerUp.TYPE_SHIELD).after(50));	
				}	
				case 1:
				{
					Z.hud().announceWave(2);
					
					IAutoSpawner meteorSpawner = new AutoSpawner(Meteor2.activeCount, SpawnEvent.TYPE_METEOR2, 6, 6, 7, 7, 0, 0, PowerUp.TYPE_NONE, 0, false);
					IAutoSpawner clamSpawner = new AutoSpawner(Clam.activeCount, SpawnEvent.TYPE_CLAM, 1, 1, 0, 0, 0, 0, PowerUp.TYPE_NONE, 0, false);
									
					return new ScoreScript(1000, new MultiAutoSpawner(meteorSpawner, clamSpawner))
						.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_METEOR2).int1(PowerUp.TYPE_SHIELD).after(50));
				}
					
				case 2:
				{
					Z.hud().announceWave(3);
					
					IAutoSpawner redArrowSpawner = new AutoSpawner(Jet.activeCount, SpawnEvent.TYPE_RED_ARROW, 1, 1, 0, 0, 0, 0, PowerUp.TYPE_NONE, 0, false);
					
					return new ScoreScript(2000, redArrowSpawner)
						.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_METEOR2).int1(PowerUp.TYPE_SHIELD).after(50));
				}
				
				case 3:
					
					Z.screen.hud().announceWave(4);
					
					return new ScoreScript(3000, new AutoSpawner(MiniJelly.activeCount, SpawnEvent.TYPE_JELLY_SHOAL, 10, 10, 0, 0, 0, 0, 9, 0, false))
						.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_CENTIPEDE).after(0))
						.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_CENTIPEDE).after(50));
					
				case 4:
				{		
					Z.hud().announceWave(5);
					
					IAutoSpawner s1 = new AutoSpawner(Meteor2.activeCount, SpawnEvent.TYPE_METEOR2, 24, 32, 14, 14, 90, 90, PowerUp.TYPE_NONE, 0, true);
					
					return new TimedWave(20, s1)
						.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_METEOR2).int1(PowerUp.TYPE_SHIELD).speed(14).angle(90).after(10));
				}	
				case 5:
				{
					IAutoSpawner mini = new AutoSpawner(MiniJelly.activeCount, SpawnEvent.TYPE_MINI_JELLY, 3, 3, 0, 0, 0, 0, 0, 0, false);
					IAutoSpawner maxi = new AutoSpawner(MaxiJelly.activeCount, SpawnEvent.TYPE_MAXI_JELLY, 1, 1, 0, 0, 0, 0, 0, 0, false);
					
					return new ScoreScript(4000, new MultiAutoSpawner(mini, maxi))
							.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_CENTIPEDE).after(0));
				}	
				case 6:	
				{
					Z.hud().announceWave(6);	
					
					IAutoSpawner s1 = new AutoSpawner(Clam.activeCount, SpawnEvent.TYPE_CLAM, 1, 3, 0, 0, 0, 0, PowerUp.TYPE_NONE, 0, false);	
					IAutoSpawner spawner = new AutoSpawner(Centipede.activeCount, SpawnEvent.TYPE_CENTIPEDE, 27, 54, 0, 0, 0, 0, 0, 0, false);
					
					return new ScoreScript(5000, new MultiAutoSpawner(s1, spawner))
						.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_CLAM).int1(PowerUp.TYPE_SHIELD).after(0))
						.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_CLAM).int1(PowerUp.TYPE_BOMB).after(20));
				}			
				case 7:
				{
					Z.hud().announceWave(7);
					
					IAutoSpawner amoebaSpawner = new AutoSpawner(Amoeba.activeCountTame, SpawnEvent.TYPE_AMOEBA, 8, 8, 0, 0, 0, 0, 0, 0, false);
					//IAutoSpawner amoebaSpawner2 = new AutoSpawner(Amoeba.activeCountFucker, SpawnEvent.TYPE_AMOEBA, 4, 4, 0, 0, 0, 0, 0, 0, true);
					IAutoSpawner spawner3 = new AutoSpawner(Millipede.activeCount, SpawnEvent.TYPE_MILLIPEDE, 1, 1, 0, 0, 0, 0, 0, 0, false);
				
					return new ScoreScript(6000, new MultiAutoSpawner(amoebaSpawner, spawner3));	
				}
				case 8:
				{
					Z.hud().announceWave(8);
					
					IAutoSpawner meteorSpawner = new AutoSpawner(Meteor2.activeCount, SpawnEvent.TYPE_METEOR2, 6, 6, 14, 14, -45, -45, PowerUp.TYPE_NONE, 0, false);
					IAutoSpawner amoebaSpawner = new AutoSpawner(Amoeba.activeCountFucker, SpawnEvent.TYPE_AMOEBA, 1, 4, 0, 0, 0, 0, 0, 0, true);
					IAutoSpawner spawner2 = new AutoSpawner(Centipede.activeCount, SpawnEvent.TYPE_CENTIPEDE, 1, 1, 0, 0, 0, 0, 0, 0, false);
					
					return new ScoreScript(8000, new MultiAutoSpawner(meteorSpawner, amoebaSpawner, spawner2))
						.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_STALKER).int1(PowerUp.TYPE_SHIELD).after(0))
						.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_STALKER).int1(PowerUp.TYPE_REAR_CANNON).after(0));
				}
				case 9:
				{
					Z.hud().announceWave(9);
					
					IAutoSpawner meteorSpawner = new AutoSpawner(Meteor2.activeCount, SpawnEvent.TYPE_METEOR2, 6, 6, 14, 14, -45, -45, PowerUp.TYPE_NONE, 0, false);
					IAutoSpawner amoebaSpawner = new AutoSpawner(Amoeba.activeCountFucker, SpawnEvent.TYPE_AMOEBA, 1, 4, 0, 0, 0, 0, 0, 0, true);
					IAutoSpawner spawner2 = new AutoSpawner(Centipede.activeCount, SpawnEvent.TYPE_CENTIPEDE, 1, 1, 0, 0, 0, 0, 0, 0, false);
					IAutoSpawner spawner3 = new AutoSpawner(Millipede.activeCount, SpawnEvent.TYPE_MILLIPEDE, 1, 1, 0, 0, 0, 0, 0, 0, false);
					
					return new ScoreScript(8000, new MultiAutoSpawner(meteorSpawner, amoebaSpawner, spawner2, spawner3));
				}
				case 10:
				{
					return new AnnounceBossController();
				}
				case 11:
				{
					return new GameController().addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_NEBULON).int1(4).after(0));
				}					
				default:
					return null;
			}
		}
	}

	@Override
	public int sectorNum()
	{
		return 2;
	};
}
