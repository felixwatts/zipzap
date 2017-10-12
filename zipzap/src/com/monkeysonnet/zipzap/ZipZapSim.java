package com.monkeysonnet.zipzap;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.utils.Array;
import com.monkeysonnet.engine.FixtureTag;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.engine.ISimulationEventHandler;
import com.monkeysonnet.zipzap.achievements.BadgeOverkill;
import com.monkeysonnet.zipzap.achievements.Notification;
import com.monkeysonnet.zipzap.entities.Enemy;
import com.monkeysonnet.zipzap.entities.PowerUp;
import com.monkeysonnet.zipzap.entities.Ship;
import com.monkeysonnet.zipzap.entities.Speck;

public class ZipZapSim extends Sim
{
	public static final float SPAWN_DISTANCE = 60f;
	
	public static final int COL_CAT_BUBBLE = 16;
	public static final int COL_CAT_POWERUP = 8;
	public static final int COL_CAT_LASER = 4;
	public static final int COL_CAT_METEORITE = 2;
	public static final int COL_CAT_SHIP = 1;
		
	public static final int EV_END_UC_CHARGE = 19;	
	public static final int EV_BEGIN_UC_CHARGE = 18;
	public static final int EV_TAP = 17;
	public static final int EV_EXPLOSION_MEDIUM = 14;
	public static final int EV_LASER_SMALL = 13;
	public static final int EV_EXPLOSION_SMALL = 12;		
	public static final int EV_COMBO_END = 11;
	public static final int EV_SET_SCORE = 10;	
	public static final int EV_NUM_LIVES_CHANGED = 9;	
	public static final int EV_WAVE_COMPLETE = 8;	
	public static final int EV_COMBO = 6;
	public static final int EV_SCORE = 4;
	public static final int EV_POWERUP_SHIELD = 3;
	public static final int EV_GAME_OVER = 2;
	public static final int EV_POWERUP_BOMB = 1;
	public static final int EV_POWERUP_MEGALASER = 0;	
	
	private static final float COMBO_TIME = 1f;

	private static final Notification notification = new Notification();
	
	private Ship _ship;
	private final Speck[] _specks = new Speck[60];
	private boolean _specksOn = true;
	private int _score;
	private int _comboLevel;
	private float _comboTime;	
	private final Array<IEntity> _workingEntityArr = new Array<IEntity>();
	
	public ZipZapSim(ISimulationEventHandler handler)
	{
		super(handler);
				
		for(int n = 0; n < _specks.length; n++)	
			_specks[n] = new Speck();		
	}
	
	@Override
	public void start()
	{
		Z.script.reset();
		start(Z.script.next());
	}
	
	public void specks(boolean on)
	{
		if(on == _specksOn)
			return;
		
		for(int s = 0; s < _specks.length; s++)
		{
			if(on)
				_entities.add(_specks[s]);
			else
				_entities.removeValue(_specks[s], true);
		}
	}	
	
	public int comboLevel()
	{
		return _comboLevel;
	}
	
	@Override
	public void start(IGameController controller)
	{
		clear();		
		
		_score = 0;

		setController(controller);		
		fireEvent(Sim.EV_START, null);
	}

	public void clearEnemies()
	{
		boolean enemyFound;
		
		do
		{		
			enemyFound = false;
			for(int n = _entities.size-1; n >= 0; n--)
			{
				if(n > _entities.size-1)
					continue;
				
				IEntity e = _entities.get(n);
				if(e instanceof Enemy)
				{
					enemyFound = true;
					
					if(e instanceof IRenderableMultiPolygon)
					{
						spawnDebris((IRenderableMultiPolygon)e, ((Enemy) e).body().getLinearVelocity());
					}
					else if(e instanceof IRenderablePolygon)
					{
						spawnDebris((IRenderablePolygon)e, ((Enemy) e).body().getLinearVelocity());
					}
					
					e.free();
				}
			}
		
		}while(enemyFound);
	}
		
	public int score()
	{
		return _score;
	}
	
	public void score(Vector2 v, int score, boolean isKill)
	{
		if(_ship.dead())
			return;
		
		score = score * (_comboLevel + 1);
		
		_score += score;
		fireEvent(ZipZapSim.EV_SCORE, Game.workingVector3.set(v.x, v.y, score));
		
		if(isKill)
		{
			if(_comboLevel < 9)
			{
				_comboLevel++;
				
				if(_comboLevel == 9)
					if(BadgeOverkill.instance().queue())
					{
						notification.color.set(BadgeOverkill.instance().color());
						notification.icon = Z.texture("zipzap-notification-overkill");
						notification.worldLoc.set(v);
						
						fireEvent(Sim.EV_ENQUEUE_NOTIFICATION, notification);
					}
			}

			fireEvent(ZipZapSim.EV_COMBO, null);			
			
			_comboTime = COMBO_TIME;
		}
	}
	
	public void setScore(int score)
	{
		_score = score;
		fireEvent(ZipZapSim.EV_SET_SCORE, null);
	}
	
	@Override
	public void advanceScript()
	{
		fireEvent(ZipZapSim.EV_WAVE_COMPLETE, null);
		super.advanceScript();
	}
	
//	public void waveComplete()
//	{
//		Z.prefs.putInteger("current-wave", Z.script.level()+1);
//		Z.prefs.putInteger("score", score());
//		Z.prefs.flush();
//		
//		fireEvent(ZipZapSim.EV_WAVE_COMPLETE, null);
//	}
	
	public Ship ship()
	{
		return _ship;
	}
	
	@Override
	public void clear()
	{
		super.clear();
		
		_ship = new Ship(_world);
		_entities.add(_ship);	
		
		focalPoint(_ship.origin());
		
		if(_specksOn)
		{
			for(int n = 0; n < _specks.length; n++)
			{				
				_specks[n].init(-1f, 0f);
				_entities.add(_specks[n]);
			}
		}
	}
	
	public void dispose()
	{
		super.clear(); // necessary to zero active counts
		super.dispose();
	}
	
	@Override
	public void update(float x, float y, float w, float h)
	{
		super.update(x, y, w, h);
		
		float dt = WORLD_STEP_TIME * timeMultiplier.floatValue();
		if(_comboTime > 0)	
		{
			_comboTime-=dt;
			if(_comboTime < 0)
			{
				_comboLevel = 0;
				fireEvent(EV_COMBO_END, null);
			}
		}
	}
	
	public void applyRangeDamage(Vector2 loc, float radius, boolean hitEnemies, boolean strikeShip, boolean mega)
	{
		_workingEntityArr.clear();
		_world.QueryAABB(applyRangeDamageCallback, loc.x-radius, loc.y-radius, loc.x+radius, loc.y+radius);
		
		float dst2 = radius*radius;
		
		for(IEntity e : _workingEntityArr)
		{
			if(strikeShip && e == _ship)
			{
				if(loc.dst2(_ship.origin()) < dst2)
				{
					_ship.strike();
				}
			}
			else if(e instanceof IHitable && e instanceof IOrigin)
			{
				if(((IOrigin)e).origin().dst2(loc) < dst2)
				{
					((IHitable)e).hit(null, mega, null, null);
				}
			}
		}
	}
	
	private final QueryCallback applyRangeDamageCallback = new QueryCallback()
	{		
		@Override
		public boolean reportFixture(Fixture fixture)
		{
			if(fixture.getUserData() != null)
			{
				FixtureTag tag = (FixtureTag)fixture.getUserData();
				if(tag.owner instanceof IEntity)
				{
					IEntity e = (IEntity)tag.owner;
					if(!_workingEntityArr.contains(e, true))
						_workingEntityArr.add(e);
				}
			}
			
			return true;
		}
	};

	public void clearPowerUps()
	{
		for(int n = _entities.size-1; n >= 0; n--)
		{
			IEntity e = _entities.get(n);
			if(e instanceof PowerUp)
			{
				((PowerUp)e).clearForBoss();
			}
		}
	}
}
