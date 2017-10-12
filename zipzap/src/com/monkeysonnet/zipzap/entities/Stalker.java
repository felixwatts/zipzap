package com.monkeysonnet.zipzap.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.engine.editor.Map;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;
import com.monkeysonnet.zipzap.behaviours.DieOnRangeBehaviour;
import com.monkeysonnet.zipzap.behaviours.FaceDirectionOfTravelBehaviour;
import com.monkeysonnet.zipzap.behaviours.FlyingSoundBehaviour;
import com.monkeysonnet.zipzap.behaviours.HomingBehaviour;
import com.monkeysonnet.zipzap.behaviours.KillOnContactBehaviour;
import com.monkeysonnet.zipzap.behaviours.OrbitBehaviour;
import com.monkeysonnet.zipzap.script.IActiveCount;

public class Stalker extends EnemyBasic
{
	private static final StalkerPool pool = new StalkerPool();
	private static class StalkerPool extends Pool<Stalker>
	{
		@Override
		protected Stalker newObject()
		{
			return new Stalker();
		}
	}

	private static final Map map = new Map("stalker.v", 2f, 0);
	private static final float SPEED = 12;
	private static final int SFX_FLY = 31;
	private static final int SFX_DEFLECT = -1023;
	private static final HomingBehaviour homingBehaviour = new HomingBehaviour(360, 360, false);
	private static final OrbitBehaviour orbitBehaviour = new OrbitBehaviour(20, 30, 360);
	private static int numActive;
	private int _powerup;
	private final boolean[] _armor = new boolean[4];
	private static final int[] _polyLookup = new int[map.numShapes()];
	private final Fixture[] _armorFixtures = new Fixture[4];
	
	public static final IActiveCount activeCount = new IActiveCount()
	{		
		@Override
		public int activeCount()
		{
			return numActive;
		}
	};
	
	static
	{
		for(int n = 0; n < map.numShapes(); n++)
		{
			if(map.shape(n).label == null)
				_polyLookup[n] = 0;
			else if(map.shape(n).label.startsWith("armor-"))
				_polyLookup[n] = Integer.parseInt(map.shape(n).label.substring(6));
			else if(map.shape(n).label.startsWith("inner-"))
				_polyLookup[n] = -Integer.parseInt(map.shape(n).label.substring(6));
		}
	}
	
	private Stalker()
	{
		_behaviours.removeValue(FaceDirectionOfTravelBehaviour.instance(), true);
		_behaviours.add(KillOnContactBehaviour.alsoDie());
		_behaviours.add(DieOnRangeBehaviour.instance());
		_behaviours.add(orbitBehaviour);
		_behaviours.add(new FlyingSoundBehaviour(SFX_FLY));
		
		_killScore = 25;
	}
	
	public static void spawn(int powerup)
	{
		if(!PowerUp.canSpawn(powerup))
			return;
		
		Stalker s = pool.obtain();
		s.setup(map, SPEED, false);

		s._powerup = powerup;
		
		s._behaviours.removeValue(homingBehaviour, true);
		s._behaviours.add(orbitBehaviour);
		
		s._armor[0] = true;
		s._armor[1] = false;
		s._armor[2] = true;
		s._armor[3] = false;
		
		for(int n = 0; n < map.numShapes(); n++)
		{
			if(map.shape(n).label != null && map.shape(n).label.startsWith("armor-"))
			{
				int a = Integer.parseInt(map.shape(n).label.substring(6)) - 1;
				s._armorFixtures[a] = s.addFixture(map.shape(n));
			}
		}
		
		numActive++;
	}
	
	@Override
	public Color color(int poly)
	{
		Color c = super.color(poly);
		
		int n = _polyLookup[poly];
		if(n < 0 && _armor[(-n)-1])
			c = null;
		else if(n > 0 && !_armor[n-1])
			c = null;
		
		if(c == Color.GREEN)
			c = PowerUp.colorForType(_powerup);

		return c;
	}
	
	@Override
	public boolean hit(Fixture f, boolean mega, Vector2 loc, Vector2 norm)
	{
		if(mega || !hasArmor())
		{
			Z.sim.spawnFlash(origin(), PowerUp.colorForType(_powerup));
			Z.sim.spawnDebris(this, _body.getLinearVelocity());
			Z.screen.sim().score(origin(), _killScore, true);
			_killed = true;
			free();
			return false;
		}
		else
		{
			for(int n = 0; n < 4; n++)
			{
				if(f == _armorFixtures[n])
				{
					if(_armor[n])
					{
						_armor[n] = false;
						Z.sim.spawnFlash(loc, Color.YELLOW);
						Z.sim.spawnExlosion(loc);
						Z.sim.spawnDebris(origin(), angle(), map.shape(armorToPoly(n+1)).shape, Color.GRAY, _body.getLinearVelocity());
						Z.sim.fireEvent(ZipZapSim.EV_EXPLOSION_SMALL, null);
						
						if(!hasArmor())
						{
							_behaviours.removeValue(orbitBehaviour, true);
							_body.setLinearVelocity(_body.getLinearVelocity().mul(2f));
							_behaviours.add(homingBehaviour);
						}
					}
					else
					{
						Z.sim.fireEvent(SFX_DEFLECT, null);
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	private int armorToPoly(int a)
	{
		for(int n = 0; n < _polyLookup.length; n++)
			if(_polyLookup[n] == a)
				return n;
		return -1;
	}
	
	private boolean hasArmor()
	{
		for(int n = 0; n < 4; n++)
			if(_armor[n])
				return true;
		return false;
	}
	
	@Override
	protected void onFree()
	{
		if(_killed)
		{
			Z.sim.fireEvent(ZipZapSim.EV_RUMBLE, 0.5f);
			PowerUp.spawn(origin(), _powerup);
		}
		
		pool.free(this);
		numActive--;
	}
	
	@Override
	public float lineWidth(int poly)
	{
		return 1f;
	}
}
