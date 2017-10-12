package com.monkeysonnet.zipzap.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.zipzap.IRenderableMultiPolygon;
import com.monkeysonnet.zipzap.Tools;
import com.monkeysonnet.zipzap.VariSfx;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;
import com.monkeysonnet.zipzap.behaviours.BlobTrailBehaviour;
import com.monkeysonnet.zipzap.behaviours.DieOnRangeBehaviour;
import com.monkeysonnet.zipzap.behaviours.HomingBehaviour;
import com.monkeysonnet.zipzap.behaviours.KillOnContactBehaviour;
import com.monkeysonnet.zipzap.behaviours.MaxAgeBehaviour;
import com.monkeysonnet.zipzap.script.IActiveCount;

public class Amoeba extends Enemy implements IRenderableMultiPolygon
{
	private static final AmoebaPool pool = new AmoebaPool();
	private static final float SPEED = 4;
	private static final float ANGULAR_VELOCITY_RAD = 3;
	private static final float CELL_RADIUS = 1f;
	private static final float COLOR_THRESHOLD = 0.5f;
	private static final float RECOVER_TIME = 1.5f;
	private static final Color color = new Color();
	private static final HomingBehaviour homingBehaviour = new HomingBehaviour(360, 360, false);
	private static final BlobTrailBehaviour blobTrailBehaviourGreen = new BlobTrailBehaviour(Color.GREEN);
	private static final BlobTrailBehaviour blobTrailBehaviourRed = new BlobTrailBehaviour(Color.RED);
	
	private static final VariSfx _sfxHit = new VariSfx(-1005, -1006);
	private static final int SFX_DIE = -1004;
	
	private boolean _isFucker;
	
	private static final Vector2[][] verts = new Vector2[3][];
	private static final float SPAWN_MEGA_LASER_CHANCE = 0.0f;	
	
	private static class AmoebaPool extends Pool<Amoeba>
	{
		@Override
		protected Amoeba newObject()
		{
			return new Amoeba();
		}
	}

	private static int numActiveTame;
	public static IActiveCount activeCountTame = new IActiveCount()
	{
		@Override
		public int activeCount()
		{
			return numActiveTame;
		}
	};
	
	private static int numActiveFucker;
	public static IActiveCount activeCountFucker = new IActiveCount()
	{
		@Override
		public int activeCount()
		{
			return numActiveFucker;
		}
	};
	
	private final float[] _energy = new float[3];
	private final Fixture[] _cellFixtures = new Fixture[3];	

	static
	{
		for(int n = 0; n < 3; n++)
		{
			verts[n] = new Vector2[10];
			
			Z.v1().set(CELL_RADIUS, 0).rotate(n * 120);
			
			for(int x = 0; x < 10; x++)
			{
				verts[n][x] = new Vector2(CELL_RADIUS, 0).rotate(x * 36).add(Z.v1());
			}
		}
	}	
	
	public static void spawn(boolean fucker)
	{
		Vector2 v = Tools.randomSpawnLoc();
		spawn(v.x, v.y, false, fucker);
	}
	
	public static void spawn(float x, float y, boolean fucker)
	{
		spawn(x, y, true, fucker);
	}

	public static void spawn(float x, float y, boolean relative, boolean fucker)
	{
		Amoeba j = pool.obtain();
		
		j._isFucker = fucker;
		
		if(relative)
		{
			x += Z.ship().origin().x;
			y += Z.ship().origin().y;
		}
		
		j._body = B2d
				.kinematicBody()
				.at(x, y)
				.linearVelocity(Z.v1().set(SPEED * (fucker ? 3f : 1f), 0))
				.angularVelocity(ANGULAR_VELOCITY_RAD)
				.create(Z.sim().world());
		
		j.addCell(0);
		j.addCell(1);
		j.addCell(2);
		
		j._behaviours.removeValue(blobTrailBehaviourGreen, true);
		j._behaviours.removeValue(blobTrailBehaviourRed, true);
		
		if(fucker)
			j._behaviours.add(blobTrailBehaviourRed);
		else j._behaviours.add(blobTrailBehaviourGreen);
		
		j.onSpawn();
		Z.sim().entities().add(j);
		
		if(fucker)
			numActiveFucker++;
		else
			numActiveTame++;
	}
	
	private void addCell(int c)
	{
		_cellFixtures[c] = B2d
				.circle()
				.radius(CELL_RADIUS)
				.at(Z.v1().set(CELL_RADIUS, 0).rotate(120f * c))
				.mask(ZipZapSim.COL_CAT_SHIP)
				.category(ZipZapSim.COL_CAT_METEORITE)
				.userData(_fixtureTag)
				.create(_body);
		
		_energy[c] = 1f;
		
		Z.sim().spawnCloud(origin(), 1, Color.GREEN, CELL_RADIUS);
	}
	
	private Amoeba()
	{
		_behaviours.add(KillOnContactBehaviour.alsoDie());
		_behaviours.add(DieOnRangeBehaviour.instance());
		_behaviours.add(homingBehaviour);
		_behaviours.add(blobTrailBehaviourGreen);
		_behaviours.add(new MaxAgeBehaviour(24f));
		
		_killScore = 25;
	}
	
	private Color energyToColor(float e, boolean fucker)
	{
		float x = Math.max(0, e - COLOR_THRESHOLD) / (1 - COLOR_THRESHOLD);		
		color.set(1f, 0f, fucker ? 0f : 1f, x);		
		return color;
	}
	
	@Override
	public void onFree()
	{	
		for(int n = 0; n < 3; n++)
			_cellFixtures[n] = null;
		pool.free(this);
		
		if(_isFucker)
		{
			Z.sim().spawnCloud(origin(), 4, Color.YELLOW, 8);
			Z.sim().spawnExlosion(origin(), 4, Color.YELLOW, 4);
			Z.sim().spawnExlosion(origin(), 4, Color.RED);
			
			if(_killed)
			{
				if(Game.Dice.nextFloat() < SPAWN_MEGA_LASER_CHANCE)
					PowerUp.spawn(origin(), PowerUp.TYPE_MEGA_LASER);
			}
		}
		else
		{
			Z.sim().spawnCloud(origin(), 4, Color.GREEN, 8);
			Z.sim().spawnExlosion(origin(), 4, Color.GREEN, 4);
			Z.sim().spawnExlosion(origin(), 4, Color.MAGENTA);
		}

		if(_isFucker)
			numActiveFucker--;
		else 
			numActiveTame--;
	}

	@Override
	public int getNumPolys()
	{
		return 4;
	}

	@Override
	public float angle(int poly)
	{
		return (float)Math.toDegrees(_body.getAngle());
	}

	@Override
	public Vector2 origin(int poly)
	{
		return _body.getWorldCenter();
	}

	@Override
	public Vector2[] verts(int poly)
	{
		if(poly == 0)
			return null;
		else return verts[poly-1];
	}

	@Override
	public Color color(int poly)
	{
		if(poly == 0)
			return _isFucker ? Color.YELLOW : Color.GREEN;
		return energyToColor(_energy[poly-1], _isFucker);
	}

	@Override
	public float lineWidth(int poly)
	{
		if(poly == 0)
			return 4f;
		else return 1f;
	}
	
	@Override
	public void update(float dt)
	{
		super.update(dt);
		
		if(!_dead)
		{
			for(int n = 0; n < 3; n++)
			{
				if(_energy[n] < 1f)
				{
					_energy[n] += dt / RECOVER_TIME;
					if(_energy[n] >= 1f)
					{
						addCell(n);
					}
				}
			}
		}
	}
	
	@Override
	public boolean hit(Fixture f, boolean mega, Vector2 loc, Vector2 norm)
	{		
		super.hit(f, mega, loc, norm);
		
		if(mega)
		{
			_killed = true;
			Z.sim().spawnExlosion(origin(), 6, _isFucker ? Color.RED : Color.MAGENTA);
			Z.screen.sim().score(origin(), _killScore, true);
			free();
			Z.sim.fireEvent(SFX_DIE, null);
		}
		else
		{
			for(int c = 0; c < 3; c++)
			{
				if(f == _cellFixtures[c])
				{
					killCell(c);
					break;
				}
			}
		}
		
		return false;
	}
	
	private void killCell(int c)
	{
		if(_cellFixtures[c] != null)
		{
			_body.destroyFixture(_cellFixtures[c]);
			_cellFixtures[c] = null;
			_energy[c] = 0;
			
			if(_isFucker)
			{
				Z.sim().spawnExlosion(origin(), 2, Color.YELLOW);
				Z.sim().spawnExlosion(origin(), 2, Color.RED);
			}
			else
			{
				Z.sim().spawnExlosion(origin(), 2, Color.GREEN);
				Z.sim().spawnExlosion(origin(), 2, Color.MAGENTA);
			}
			
			if(_cellFixtures[0] == null
					&& _cellFixtures[1] == null
					&& _cellFixtures[2] == null)
			{
				_killed = true;
				Z.screen.sim().score(origin(), _killScore, true);
				free();	
				Z.sim.fireEvent(SFX_DIE, null);
			}
			else _sfxHit.play();
		}
	}

	@Override
	public boolean isLoop(int poly)
	{
		return true;
	}

	@Override
	public float clipRadius()
	{
		return 4f;
	}
}
