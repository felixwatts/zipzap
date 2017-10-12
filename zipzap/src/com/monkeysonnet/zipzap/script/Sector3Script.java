package com.monkeysonnet.zipzap.script;

import com.badlogic.gdx.Gdx;
import com.monkeysonnet.zipzap.IGameController;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.entities.Ghoster;
import com.monkeysonnet.zipzap.entities.Meteor2;
import com.monkeysonnet.zipzap.entities.PowerUp;
import com.monkeysonnet.zipzap.entities.Square;
import com.monkeysonnet.zipzap.entities.Triangle;
import com.monkeysonnet.zipzap.entities.Warpey;

public class Sector3Script extends Script implements ISectorScript
{
	// boss: Prismoid
	// theme: lasers + shields
	
	// warpey
	// enterprise
	// clam
	// ghoster
	// square
	// ufo zipper
	
	public Sector3Script(int wave)
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
					
					Z.music.play(Gdx.files.internal("music/sector-3.ogg"));
					
					IAutoSpawner meteorSpawner = new AutoSpawner(Meteor2.activeCount, SpawnEvent.TYPE_METEOR2, 6, 12, 12, 20, 90, 90, PowerUp.TYPE_NONE, 0, false);
	
					return new ScoreScript(0, meteorSpawner)
						.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_UFO_ZIPPER).after(20))
						.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_METEOR2).int1(PowerUp.TYPE_SHIELD).after(50))
						.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_UFO_ZIPPER).after(70))
						.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_UFO_ZIPPER).after(70));	
				}	
				case 1:
				{
					Z.hud().announceWave(2);
					
					IAutoSpawner meteorSpawner = new AutoSpawner(Meteor2.activeCount, SpawnEvent.TYPE_METEOR2, 3, 3, 7, 7, 0, 0, PowerUp.TYPE_NONE, 0, false);
					IAutoSpawner spawner = new AutoSpawner(Square.activeCount, SpawnEvent.TYPE_SQUARE, 2, 2, 0, 0, 0, 0, 0, 0, false);
	
					return new ScoreScript(1000, new MultiAutoSpawner(spawner, meteorSpawner))
						.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_METEOR2).int1(PowerUp.TYPE_SHIELD).after(50));
				}
				case 2:
				{
					Z.hud().announceWave(3);
					
					IAutoSpawner spawner = new AutoSpawner(Square.activeCount, SpawnEvent.TYPE_SQUARE, 2, 2, 0, 0, 0, 0, PowerUp.TYPE_NONE, 0, false);
					IAutoSpawner spawner2 = new AutoSpawner(Triangle.activeCount, SpawnEvent.TYPE_TRIANGLE, 2, 2, 0, 0, 0, 0, PowerUp.TYPE_NONE, 0, false);
	
					return new ScoreScript(2000, new MultiAutoSpawner(spawner, spawner2))
						.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_TRIANGLE).int1(PowerUp.TYPE_SHIELD).after(0))
						.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_TRIANGLE).int1(PowerUp.TYPE_SHIELD).after(50))
						.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_TRIANGLE).int1(PowerUp.TYPE_ULTRA_CAPACITOR).after(0));
				}
				case 3:
				{
	
					Z.hud().announceWave(4);
					
					IAutoSpawner spawner = new AutoSpawner(Ghoster.activeCount, SpawnEvent.TYPE_GHOSTER, 4, 12, 0, 0, 0, 0, 0, 0, false);
	
					return new ScoreScript(4000, spawner)
						.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_METEOR2).int1(PowerUp.TYPE_SHIELD).after(0))
						.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_METEOR2).int1(PowerUp.TYPE_SHIELD).after(50));
				
				}
				case 4:
				{
					Z.hud().announceWave(5);
					
					IAutoSpawner spawner = new AutoSpawner(Square.activeCount, SpawnEvent.TYPE_SQUARE, 3, 3, 0, 0, 0, 0, PowerUp.TYPE_NONE, 0, false);
					IAutoSpawner spawner2 = new AutoSpawner(Triangle.activeCount, SpawnEvent.TYPE_TRIANGLE, 3, 3, 0, 0, 0, 0, PowerUp.TYPE_NONE, 0, false);
	
					return new ScoreScript(5000, new MultiAutoSpawner(spawner, spawner2))
						.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_METEOR2).int1(PowerUp.TYPE_SHIELD).after(0))
						.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_TRIANGLE).int1(PowerUp.TYPE_MEGA_LASER).after(0))
						.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_TRIANGLE).int1(PowerUp.TYPE_MEGA_LASER).after(50));
				}
				case 5:
				{
					Z.hud().announceWave(6);
					
					IAutoSpawner spawner = new AutoSpawner(Warpey.activeCount, SpawnEvent.TYPE_WARPEY, 4, 7, 0, 0, 0, 0, PowerUp.TYPE_NONE, 0, false);
					return new ScoreScript(6000, spawner)
						.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_METEOR2).int1(PowerUp.TYPE_SHIELD).after(0));
				}
				case 6:
				{
					Z.hud().announceWave(7);
					
					IAutoSpawner spawner = new AutoSpawner(Warpey.activeCount, SpawnEvent.TYPE_WARPEY, 7, 7, 0, 0, 0, 0, PowerUp.TYPE_NONE, 0, false);
					return new ScoreScript(7000, spawner)
						.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_ENTERPRISE).int1(3).after(0));
					
	
				}
				case 7:
				{
					Z.hud().announceWave(8);
					
					IAutoSpawner spawner = new AutoSpawner(Warpey.activeCount, SpawnEvent.TYPE_WARPEY, 7, 7, 0, 0, 0, 0, PowerUp.TYPE_NONE, 0, false);
					return new ScoreScript(8000, spawner)
						.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_ENTERPRISE).int1(5).after(0));
					
	
				}
				case 8:			
				{
					Z.hud().announceWave(9);
					
					IAutoSpawner meteor = new AutoSpawner(Meteor2.activeCount, SpawnEvent.TYPE_METEOR2, 6, 6, 12, 20, 0, 0, PowerUp.TYPE_NONE, 0, false);
					IAutoSpawner warpey = new AutoSpawner(Warpey.activeCount, SpawnEvent.TYPE_WARPEY, 3, 3, 0, 0, 0, 0, PowerUp.TYPE_NONE, 0, false);
					IAutoSpawner square = new AutoSpawner(Square.activeCount, SpawnEvent.TYPE_SQUARE, 1, 1, 0, 0, 0, 0, PowerUp.TYPE_NONE, 0, false);
					IAutoSpawner triangle = new AutoSpawner(Triangle.activeCount, SpawnEvent.TYPE_TRIANGLE, 1, 1, 0, 0, 0, 0, PowerUp.TYPE_DRAGON, 0, false);
					IAutoSpawner ghoster = new AutoSpawner(Ghoster.activeCount, SpawnEvent.TYPE_GHOSTER, 4, 12, 0, 0, 0, 0, 0, 0, false);
					
					return new ScoreScript(9000, new MultiAutoSpawner(meteor, warpey, square, triangle, ghoster));
	//					.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_METEOR2).int1(PowerUp.TYPE_DRAGON).after(0))
	//					.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_METEOR2).int1(PowerUp.TYPE_DRAGON).after(50));
				}
				case 9:
				{
					return new AnnounceBossController();
				}
				case 10:
				{
					return new GameController()
						.addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_ROTOLON).after(0));
				}
				default:
					return null;
			}
		}
	}

	@Override
	public int sectorNum()
	{
		return 4;
	}

}
