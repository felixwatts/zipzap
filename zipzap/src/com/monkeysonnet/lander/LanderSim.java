package com.monkeysonnet.lander;

import java.util.Hashtable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.ISimulationEventHandler;
import com.monkeysonnet.zipzap.ITriggerable;
import com.monkeysonnet.zipzap.Sim;
import com.monkeysonnet.zipzap.entities.Particle;

public class LanderSim extends Sim
{
	public static final int COL_CAT_GUY = 1;
	public static final int COL_CAT_WALL = 2;
	public static final int COL_CAT_PAD = 4;
	public static final int COL_CAT_GUY_BULLET = 32;
	public static final int COL_CAT_ENEMY = 64;
	public static final int COL_CAT_ENEMY_BULLET = 128;
	
	public static final int EV_PLAYER_DIED = 0;
	public static final int EV_OUCH = 1;
	public static final int EV_LEVEL_COMPLETE = 2;
	public static final int EV_EXPLOSION_SMALL = 3;
	public static final int EV_EXPLOSION_MEDIUM = 4;
	public static final int EV_LASER_SMALL = 5;
	public static final int EV_BEGIN_THRUST = 6;
	public static final int EV_END_THRUST = 7;	
	public static final int EV_SET_NEXT_LEVEL = 8;	
	
	private static final float SPARKS_VEL = 8;
	private static final float SPARKS_MAX_AGE = 0.2f;
	private static final int SFX_WARP_CURTAIN = -1029;	

	private final Hashtable<String, ITriggerable> _triggerables = new Hashtable<String, ITriggerable>();
	private Guy _guy;

	public LanderSim(ISimulationEventHandler handler)
	{
		super(handler);		
	}
	
	public void initShip(Vector2 loc)
	{
		_guy = new Guy(loc);
	}
		
	public Guy guy()
	{
		return _guy;
	}
	
	@Override
	public void clear()
	{
		super.clear();
		_triggerables.clear();
		_world.setGravity(Vector2.tmp.set(0, -10f));
	}
	
	public void register(ITriggerable triggerable, String name)
	{
		if(name == null)
			return;
		_triggerables.put(name, triggerable);
	}
	
	public void unregister(String name)
	{
		if(name == null)
			return;
		_triggerables.remove(name);
	}
	
	public void trigger(String names)
	{
		for(String str : names.split(","))
		{
			ITriggerable t = _triggerables.get(str);
			if(t != null)
				t.trigger();
		}
	}
	
	@Override
	public void advanceScript()
	{
		super.advanceScript();
		fireEvent(Sim.EV_START, null);
	}
	
	public void spawnSparks(Vector2 loc, Vector2 sourceVel, Color color)
	{
		for(int n = 0; n < 4; n++)
			Particle.spawn(loc, Vector2.tmp.set(SPARKS_VEL, 0).rotate(Game.Dice.nextFloat()*360f).add(sourceVel), color, 0.5f, 0.8f, SPARKS_MAX_AGE);
	}
	
	public void spawnWarpCurtain()
	{
		for(float y = L.sim.guy().origin().y - Guy.HEIGHT/2f; y < L.sim.guy().origin().y + Guy.HEIGHT/2f; y += Guy.HEIGHT/8f)
		{
			for(float x = L.sim.guy().origin().x - Guy.WIDTH/2f; x < L.sim.guy().origin().x + Guy.WIDTH/2f; x += Guy.WIDTH/8f)
			{
				Squareticle.spawn(Vector2.tmp.set(x, y));
			}
		}
		
		fireEvent(SFX_WARP_CURTAIN, null);
	}
}
