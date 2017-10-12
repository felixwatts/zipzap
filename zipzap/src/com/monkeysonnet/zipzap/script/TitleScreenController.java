package com.monkeysonnet.zipzap.script;

import com.monkeysonnet.engine.Game;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;
import com.monkeysonnet.zipzap.entities.Amoeba;
import com.monkeysonnet.zipzap.entities.Gnat;
import com.monkeysonnet.zipzap.entities.Meteor;
import com.monkeysonnet.zipzap.entities.MiniJelly;
import com.monkeysonnet.zipzap.entities.PowerUp;
import com.monkeysonnet.zipzap.entities.ShootingSeeker;
import com.monkeysonnet.zipzap.entities.SpinningSeeker;
import com.monkeysonnet.zipzap.entities.UfoZipper;
import com.monkeysonnet.zipzap.entities.WormSegment;

public class TitleScreenController extends GameController
{
	private static final float MAX_METEOR_SPEED = 10;
	
	@Override
	public void init()
	{
		super.init();
		
		Z.ship().ghostMode(true);
		Z.sim().entities().removeValue(Z.ship(), true);
		
		for(int n = 0; n < 3; n++)
		{
			switch(Game.Dice.nextInt(7))
			{
				case 0:
					Gnat.spawn(Game.Dice.nextFloat() * 360, Gnat.SQUAD_MAGENTA, true);
					Gnat.spawn(Game.Dice.nextFloat() * 360, Gnat.SQUAD_MAGENTA, false);
					Gnat.spawn(Game.Dice.nextFloat() * 360, Gnat.SQUAD_MAGENTA, false);
					break;
				case 1:
					MiniJelly.spawn(Game.Dice.nextFloat() * 360);
					break;
				case 2:
					UfoZipper.spawn();
					break;
				case 3:
					SpinningSeeker.spawn(Game.Dice.nextFloat() * 360);
					break;
				case 4:
					ShootingSeeker.spawn();
					break;
				case 5:
					Z.v1().set(ZipZapSim.SPAWN_DISTANCE, 0).rotate(Game.Dice.nextFloat() * 360);
					Amoeba.spawn(Z.v1().x, Z.v1().y, false);
					break;
				case 6:
					WormSegment.spawnSnakey(Game.Dice.nextFloat() * 360);
					break;
				
			}
		}
	}
	
	@Override
	public void update(float dt)
	{
		super.update(dt);
		
		if(Meteor.activeCount.activeCount() < 12)
		{
			Meteor.spawn(Game.Dice.nextFloat() * MAX_METEOR_SPEED, PowerUp.TYPE_NONE);
		}
	}
	
	@Override
	public void cleanup()
	{
		super.cleanup();
		Z.sim().clear();
	}
}
