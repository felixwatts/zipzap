package com.monkeysonnet.zipzap.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.ICallback;
import com.monkeysonnet.engine.editor.Map;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.behaviours.BlobTrailBehaviour;
import com.monkeysonnet.zipzap.behaviours.HomingBehaviour;
import com.monkeysonnet.zipzap.behaviours.IBehaviour;
import com.monkeysonnet.zipzap.behaviours.OrbitBehaviour;
import com.monkeysonnet.zipzap.behaviours.ProximityBehaviour;
import com.monkeysonnet.zipzap.behaviours.RandomSwitchingBehaviour;
import com.monkeysonnet.zipzap.script.IActiveCount;

public class Ace extends EnemyBasic
{
	private static final Map map = new Map("ace.v", 1f, -90f);
	
	private static final Vector2 gunLeft = map.point("gun-left").point;
	private static final Vector2 gunRight = map.point("gun-right").point;

	private static final float ANGULAR_VELOCITY = 15f;
	private static final float RELOAD_TIME_HEAT_SEEKER = 5f;
	private static final float SPEED = 22f;
	protected static final float PROJECTILE_SPEED = 32;
	private static final float RELOAD_TIME_PROECTILE = 0.6f;
	private static final int KILL_SCORE = 25;
	private static final float GIVE_SHIELD_CHANCE = 0.1f;	
	private static final int SFX_FIRE_HEAT_SEEKER = -1034;
	private static final int SFX_FIRE_LASER = -1001;
	
	private final RandomSwitchingBehaviour behaviour = new RandomSwitchingBehaviour(0.5f, 
			new HomingBehaviour(180f, ANGULAR_VELOCITY, false),
			new HomingBehaviour(360f, ANGULAR_VELOCITY, true),
			new OrbitBehaviour(20f, 30f, ANGULAR_VELOCITY));

	private Color _color;
	private IBehaviour _fireBehaviour;

	private final ICallback callbackFireHeatSeeker = new ICallback()
	{		
		@Override
		public void callback(Object arg)
		{
			Z.sim.fireEvent(SFX_FIRE_HEAT_SEEKER, null);
			HeatSeeker.spawn(Vector2.tmp.set(gunLeft).rotate(angle()).add(origin()), angle());
			HeatSeeker.spawn(Vector2.tmp.set(gunRight).rotate(angle()).add(origin()), angle());
		}
	};
	
	private final ICallback callbackFireProjectile = new ICallback()
	{		
		@Override
		public void callback(Object arg)
		{
			Z.sim.fireEvent(SFX_FIRE_LASER, null);
			Projectile.spawn(Vector2.tmp.set(gunLeft).rotate(angle()).add(origin()), Z.v1().set(PROJECTILE_SPEED, 0).rotate(angle()), 0, Color.CYAN, false);
			Projectile.spawn(Vector2.tmp.set(gunRight).rotate(angle()).add(origin()), Z.v1().set(PROJECTILE_SPEED, 0).rotate(angle()), 0, Color.CYAN, false);
		}
	};

	private final ICallback callbackFlee = new ICallback()
	{		
		@Override
		public void callback(Object arg)
		{
			behaviour.force(1);
		}
	};
	
	private Ace()
	{
		_behaviours.add(behaviour);
		_behaviours.add(new ProximityBehaviour(-360f, 360f, 20f, 0.5f, callbackFlee));
		_behaviours.add(new BlobTrailBehaviour(Color.YELLOW, 0.1f, 0.5f, map.point("jet").point, 0.5f, 0));
		
		_killScore = KILL_SCORE;
	}
	
	@Override
	protected void onFree()
	{
		if(_killed && Game.Dice.nextFloat() < GIVE_SHIELD_CHANCE)
		{
			if(_color == Color.CYAN)
				PowerUp.spawn(origin(), PowerUp.TYPE_SHIELD);
			else 
				PowerUp.spawn(origin(), PowerUp.TYPE_BOMB);
		}
		
		_behaviours.removeValue(_fireBehaviour, true);
		pool.free(this);
		_activeCount--;
	}
	
	private static int _activeCount;	
	public static final IActiveCount activeCount = new IActiveCount()
	{
		
		@Override
		public int activeCount()
		{
			return _activeCount;
		}
	};
	
	private static final AcePool pool = new AcePool();
	private static class AcePool extends Pool<Ace>
	{
		@Override
		protected Ace newObject()
		{
			return new Ace();
		}
	}
	
	public static void spawn()
	{
		Ace a = pool.obtain();
		a.setup(map, SPEED);
		
		if(Game.Dice.nextBoolean())
		{
			a._fireBehaviour = new ProximityBehaviour(-30f, 30f, 50f, RELOAD_TIME_HEAT_SEEKER, a.callbackFireHeatSeeker);
			a._behaviours.add(a._fireBehaviour);
			a._color = Color.RED;
		}
		else
		{
			a._fireBehaviour = new ProximityBehaviour(-30f, 30f, 50f, RELOAD_TIME_PROECTILE, a.callbackFireProjectile);
			a._behaviours.add(a._fireBehaviour);
			a._color = Color.CYAN;
		}
		
		_activeCount++;
	}
	
	@Override
	public Color color(int poly)
	{
		if(super.color(poly) == Color.GRAY)
			return Color.GRAY;
		else return _color;
	}
}
