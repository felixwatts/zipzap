package com.monkeysonnet.zipzap.script;

import com.badlogic.gdx.Gdx;
import com.monkeysonnet.zipzap.IGameController;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.entities.Ace;
import com.monkeysonnet.zipzap.entities.Glider;
import com.monkeysonnet.zipzap.entities.Gnat;
import com.monkeysonnet.zipzap.entities.Meteor2;
import com.monkeysonnet.zipzap.entities.PowerUp;
import com.monkeysonnet.zipzap.entities.Jet;
import com.monkeysonnet.zipzap.entities.SnowFlake;
import com.monkeysonnet.zipzap.entities.SpinningSeeker;
import com.monkeysonnet.zipzap.entities.StingRay;

public class Sector4Script extends Script implements ISectorScript
{	
	// boss: Mutha
	// theme: tech ships
	
	// glider
	// snowflake
	// red arrow
	// gnat
	// stingray
	// ace
	
	public Sector4Script(int wave)
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
					Z.hud().announceWave(1);
					
					Z.music.play(Gdx.files.internal("music/sector-4.ogg"));
					
					IAutoSpawner meteorSpawner = new AutoSpawner(Meteor2.activeCount, SpawnEvent.TYPE_METEOR2, 6, 12, 12, 20, 1, 1, PowerUp.TYPE_NONE, 0, false);
	
					return new ScoreScript(0, meteorSpawner)
						.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_ACE).after(20))
						.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_METEOR2).int1(PowerUp.TYPE_BOMB).after(50))
						.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_ACE).after(50))
						.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_ACE).after(70));	
				}	
				case 1:
				{
					Z.hud().announceWave(2);
					
					IAutoSpawner meteorSpawner = new AutoSpawner(Meteor2.activeCount, SpawnEvent.TYPE_METEOR2, 3, 3, 7, 7, 0, 0, PowerUp.TYPE_NONE, 0, false);
					IAutoSpawner spawner = new AutoSpawner(Glider.activeCount, SpawnEvent.TYPE_GLIDERS, 5, 5, 0, 0, 0, 0, 0, 0, false);
	
					return new ScoreScript(1000, new MultiAutoSpawner(spawner, meteorSpawner))
						.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_METEOR2).int1(PowerUp.TYPE_SHIELD).after(0))
						.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_METEOR2).int1(PowerUp.TYPE_SHIELD).after(50));
				}
				case 2:
				{
					Z.hud().announceWave(3);
					
					IAutoSpawner meteorSpawner = new AutoSpawner(Meteor2.activeCount, SpawnEvent.TYPE_METEOR2, 6, 6, 12, 12, 0, 0, PowerUp.TYPE_NONE, 0, false);
					IAutoSpawner spawner = new AutoSpawner(Gnat.activeCommanderCount, SpawnEvent.TYPE_GNAT_SQUADRON, 1, 1, 0, 0, 0, 0, 6, 0, false);
	
					return new ScoreScript(2000, new MultiAutoSpawner(meteorSpawner, spawner))
						.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_METEOR2).int1(PowerUp.TYPE_SHIELD).after(0))
						.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_METEOR2).int1(PowerUp.TYPE_SHIELD).after(50));
				}
				case 3:
				{
					Z.screen.sim().setScore(3000);
					
					Z.hud().announceWave(4);
					
					IAutoSpawner meteorSpawner = new AutoSpawner(SnowFlake.activeCount, SpawnEvent.TYPE_SNOWFLAKE, 18, 18, 0, 0, 0, 0, 0, 0, false);
					//IAutoSpawner spawner = new AutoSpawner(Jet.activeCount, SpawnEvent.TYPE_RED_ARROW, 6, 6, 0, 0, 0, 0, PowerUp.TYPE_NONE, 0, false);
	
					return new TimedWave(12, meteorSpawner)
						.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_METEOR2).int1(PowerUp.TYPE_SHIELD).after(50));
						//.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_RED_ARROW).int1(PowerUp.TYPE_ULTRA_CAPACITOR).after(50));
				}
				case 4:
				{
					IAutoSpawner spawner = new AutoSpawner(Jet.activeCount, SpawnEvent.TYPE_RED_ARROW, 6, 6, 0, 0, 0, 0, PowerUp.TYPE_NONE, 0, false);
					return new ScoreScript(3000, spawner)
						.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_RED_ARROW).int1(PowerUp.TYPE_DRAGON).after(50));
				}
				case 5:
				{
					Z.hud().announceWave(5);
					
					//IAutoSpawner meteorSpawner = new AutoSpawner(Meteor2.activeCount, SpawnEvent.TYPE_METEOR2, 6, 6, 12, 12, 0, 0, PowerUp.TYPE_NONE, 0, false);
					IAutoSpawner spawner = new AutoSpawner(StingRay.activeCount, SpawnEvent.TYPE_STING_RAY, 2, 2, 0, 0, 0, 0, 0, 0, false);
					//IAutoSpawner spawner2 = new AutoSpawner(SpinningSeeker.activeCount, SpawnEvent.TYPE_SPINNING_SEEKER, 2, 2, 0, 0, 0, 0, 0, 0, false);
					
					return new ScoreScript(4000, spawner)
						.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_METEOR2).int1(PowerUp.TYPE_BOMB).after(0));
				}
				case 6:
				{
					Z.hud().announceWave(6);
					
					IAutoSpawner meteorSpawner = new AutoSpawner(Glider.activeCount, SpawnEvent.TYPE_GLIDERS, 5, 5, 0, 0, 0, 0, 0, 0, false);
					IAutoSpawner spawner = new AutoSpawner(StingRay.activeCount, SpawnEvent.TYPE_STING_RAY, 2, 2, 0, 0, 0, 0, 0, 0, false);
	
					return new ScoreScript(5000, new MultiAutoSpawner(meteorSpawner, spawner))
						.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_METEOR2).int1(PowerUp.TYPE_BOMB).after(25));
				}
				case 7:
				{
					Z.hud().announceWave(7);
					IAutoSpawner spawner = new AutoSpawner(Ace.activeCount, SpawnEvent.TYPE_ACE, 2, 2, 0, 0, 0, 0, 0, 0, false);
					IAutoSpawner spawner2 = new AutoSpawner(SpinningSeeker.activeCount, SpawnEvent.TYPE_SPINNING_SEEKER, 2, 2, 0, 0, 0, 0, 0, 0, false);
					return new ScoreScript(6000, new MultiAutoSpawner(spawner, spawner2))
						.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_METEOR2).int1(PowerUp.TYPE_DRAGON).after(25));
				}
				case 8:
				{
					Z.hud().announceWave(8);
					IAutoSpawner meteorSpawner = new AutoSpawner(StingRay.activeCount, SpawnEvent.TYPE_STING_RAY, 3, 3, 0, 0, 0, 0, 0, 0, false);
					return new ScoreScript(8000, meteorSpawner)
						.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_METEOR2).int1(PowerUp.TYPE_BOMB).after(25));
				}
				case 9:
				{
					Z.hud().announceWave(9);
					IAutoSpawner meteorSpawner = new AutoSpawner(Meteor2.activeCount, SpawnEvent.TYPE_METEOR2, 6, 6, 12, 12, 0, 0, PowerUp.TYPE_NONE, 0, false);
					IAutoSpawner spawner2 = new AutoSpawner(Ace.activeCount, SpawnEvent.TYPE_ACE, 1, 1, 0, 0, 0, 0, 0, 0, false);
					IAutoSpawner spawner3 = new AutoSpawner(Gnat.activeCommanderCount, SpawnEvent.TYPE_GNAT_SQUADRON, 1, 1, 0, 0, 0, 0, 0, 0, false);
					return new ScoreScript(7000, new MultiAutoSpawner(meteorSpawner, spawner2, spawner3))
						.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_METEOR2).int1(PowerUp.TYPE_SHIELD).after(0))
						.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_METEOR2).int1(PowerUp.TYPE_BOMB).after(50));
				}
				case 10:
				{
					return new AnnounceBossController();
				}
				case 11:
				{
					return new GameController()
						.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_MUTHA).after(0));
				}
				
				default:
					return null;
			}
		}
	}
	
	@Override
	public int sectorNum()
	{
		return 3;
	}
}
