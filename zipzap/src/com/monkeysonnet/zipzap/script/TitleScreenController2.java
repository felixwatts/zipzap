package com.monkeysonnet.zipzap.script;

import com.badlogic.gdx.math.Vector2;
import com.monkeysonnet.zipzap.IGameController;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.entities.Meteor2;
import com.monkeysonnet.zipzap.entities.PowerUp;
import com.monkeysonnet.zipzap.entities.Ship;

public class TitleScreenController2 extends GameController
{
	public static IGameController create()
	{
		return new TitleScreenController2(new AutoSpawner(Meteor2.activeCount, SpawnEvent.TYPE_METEOR2, 4, 4, 7, 14, 90, 90, PowerUp.TYPE_NONE, 0, false));
		
//		if(BadgePrismolon.instance().isEarned())
//		{
//			return new TitleScreenController2().addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_ROTOLON).after(0));
//		}
//		else if(BadgeTerralon.instance().isEarned())
//		{
//			return new TitleScreenController2().addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_MUTHA).after(0));
//		}
//		else if(BadgeNebulon.instance().isEarned())
//		{
//			return new TitleScreenController2().addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_NEBULON).int1(4).after(0));
//		}
//		else if(BadgeKobolon.instance().isEarned())
//		{
//			//return new TitleScreenController2(new AutoSpawner(Meteor2.activeCount, SpawnEvent.TYPE_METEOR2, 4, 4, 7, 14, 90, 90, PowerUp.TYPE_NONE, 0, false));
//			return new TitleScreenController2().addEvent(SpawnEvent.obtain(SpawnEvent.TYPE_KOBO).pos(50, 20).after(0));
//		}
//		else
//		{
//			return new TitleScreenController2(new AutoSpawner(Meteor2.activeCount, SpawnEvent.TYPE_METEOR2, 4, 4, 7, 14, 90, 90, PowerUp.TYPE_NONE, 0, false));
//		}
	}
	
	public TitleScreenController2()
	{
		super();
	}
	
	public TitleScreenController2(IAutoSpawner spawner)
	{
		super(spawner);
	}
	
	@Override
	public void init()
	{
		super.init();
		
		//Z.ship().beginCruise();
		Z.ship().beginCruise(Vector2.tmp.set(0, Ship.SPEED_NORMAL));
		Z.ship().ghostMode(true);
		Z.sim().entities().removeValue(Z.ship(), true);
	}
}
