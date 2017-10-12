package com.monkeysonnet.zipzap.achievements;

import com.badlogic.gdx.utils.Array;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.ISimulationEventHandler;
import com.monkeysonnet.zipzap.Sim;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.screens.ReceiveAchievementScreen;

public class Achievements implements ISimulationEventHandler
{
	private Array<IBadge> _badges = new Array<IBadge>();
	private Array<IBadge> _simEventBadges = new Array<IBadge>();
	private Array<Object> _pending = new Array<Object>();
	
	public Achievements()
	{
		_badges.add(BadgeKobolon.instance());
		_badges.add(BadgeNebulon.instance());
		_badges.add(BadgePrismolon.instance());
		_badges.add(BadgeTerralon.instance());		
//		_badges.add(BadgeCombo.instance(2));
//		_badges.add(BadgeCombo.instance(3));
//		_badges.add(BadgeCombo.instance(4));
//		_badges.add(BadgeCombo.instance(5));
//		_badges.add(BadgeCombo.instance(6));
//		_badges.add(BadgeCombo.instance(7));
//		_badges.add(BadgeCombo.instance(8));
//		_badges.add(BadgeCombo.instance(9));
//		_badges.add(BadgeKillJelly.instance());
		_badges.add(BadgeSquadron.instance(1));
		_badges.add(BadgeSquadron.instance(2));
		_badges.add(BadgeSquadron.instance(3));
		_badges.add(BadgeSquadron.instance(4));
		_badges.add(BadgeUltraCapacitor.instance());
		_badges.add(BadgeRearCannon.instance());
		_badges.add(BadgeShield.instance());
		_badges.add(BadgeOverkill.instance());		
		_badges.add(BadgeBurger.instance(0));
		_badges.add(BadgeBurger.instance(1));
		_badges.add(BadgeBurger.instance(2));
		_badges.add(BadgeBurger.instance(3));
		_badges.add(BadgeJetPakWings1.instance());
		_badges.add(BadgeJetPakWings2.instance());
		
		for(IBadge b : _badges)
			if(b.needsSimEvents())
				_simEventBadges.add(b);
	}
	
	public void earn(IBadge badge)
	{	
		if(badge.isEarned())
			return;
		
//		if(badge.isPending())
//			return;
		
		_pending.add(badge);
	}
	
	public void unlock(ITreat treat)
	{
		_pending.add(treat);
	}
	
	public Array<IBadge> badges()
	{
		return _badges;
	}
	
	public boolean pending()
	{
		return _pending.size > 0;
	}
	
	public boolean process()
	{
		while(true)
		{
			if(_pending.size == 0)
				return false;
			else
			{						
				Object o = _pending.removeIndex(0);
				if(o instanceof IBadge)
				{
					IBadge b = (IBadge)o;
					
					Z.sim.fireEvent(Sim.EV_DEQUEUE_NOTIFICATION, null);
					
					b.earn();
					
					if(b.treat() != null)
						unlock(b.treat());

					Game.ScreenManager.push(new ReceiveAchievementScreen(b));
					
					return true;
				}
				else if(o instanceof ITreat)
				{
					ITreat t = (ITreat)o;				
					t.unlock();		
					
					continue;
					//Game.ScreenManager.push(new ReceiveAchievementScreen(t));
				}
			}
		}
		
	}

	@Override
	public void onSimulationEvent(int eventType, Object argument)
	{
		for(IBadge b : _simEventBadges)
		{
			b.onSimulationEvent(eventType, argument);
		}
	}	
}
