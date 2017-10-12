package com.monkeysonnet.lander;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.zipzap.Tools;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.behaviours.BehaviourBase;
import com.monkeysonnet.zipzap.entities.Enemy;

public class ShootableBehaviour extends BehaviourBase
{
	private static final int SFX_DAMAGE = -1009;
	
	private int _energy;
	private int _startEnergy;
	
	public ShootableBehaviour(int energy)
	{
		_startEnergy = energy;
	}
	
	@Override
	public void spawn(IEntity subject)
	{
		super.spawn(subject);
		_energy = _startEnergy;
	}
	
	@Override
	public void hit(IEntity subject, Fixture fixture, boolean mega,
			Vector2 loc, Vector2 norm)
	{
		super.hit(subject, fixture, mega, loc, norm);

		Enemy e = (Enemy)subject;
		
		L.sim.fireEvent(SFX_DAMAGE, e.origin());
		
		_energy--;
		if(_energy == 0)
		{			
			SmokePuff.spawnExplosion(e.origin());
			Z.sim().spawnFlash(e.origin(), Color.ORANGE);
			Z.sim.spawnCloud(e.origin(), 4, Color.ORANGE, 12f);
			Z.sim.spawnCloud(e.origin(), 4, Color.YELLOW, 6f);
			Z.sim.spawnCloud(e.origin(), 4, Color.WHITE, 3f);
			Z.renderer().shakeCamera(0.75f, 0.5f);
			L.sim.fireEvent(LanderSim.EV_EXPLOSION_MEDIUM, e.origin());
			e.onKill();
			e.free();
		}
		
		L.sim.spawnSparks(loc, Tools.zeroVector, Bullet.sparksColour);
	}
}
