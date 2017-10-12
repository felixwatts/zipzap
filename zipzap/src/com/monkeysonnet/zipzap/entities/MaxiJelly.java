package com.monkeysonnet.zipzap.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.engine.FixtureTag;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.editor.Map;
import com.monkeysonnet.zipzap.IRenderableMultiPolygon;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;
import com.monkeysonnet.zipzap.behaviours.DieOnHitBehaviour;
import com.monkeysonnet.zipzap.behaviours.DieOnRangeBehaviour;
import com.monkeysonnet.zipzap.behaviours.ExplosionOnHitBehaviour;
import com.monkeysonnet.zipzap.behaviours.FaceDirectionOfTravelBehaviour;
import com.monkeysonnet.zipzap.behaviours.HomingBehaviour;
import com.monkeysonnet.zipzap.behaviours.IBehaviour;
import com.monkeysonnet.zipzap.behaviours.KillOnContactBehaviour;
import com.monkeysonnet.zipzap.script.IActiveCount;

public class MaxiJelly extends Enemy implements IRenderableMultiPolygon
{
	private static final int SFX_LAY = -1022;
	private static final int SFX_HIT = -1016;
	private static final float SCALE = 8f;
	private static final MaxiJellyPool pool = new MaxiJellyPool(); 
	private static final Map verts = new Map("mini-jelly.v", SCALE, 0);
	private static final Vector2[] points = new Vector2[6];
	private static final float DRIFT_SPEED = 4;
	public static final Color color = new Color(210f/255f, 1f, 1f, 0.7f);
	private static final IBehaviour homingBehaviour = new HomingBehaviour(360, 12.5f, false);
	private static final int START_NUM_EGGS = 6;
	private static final float LAY_TIME = 1f;
	
	private int _numEggs;
	private int _layQueue;
	private float _layTime;
	private Color _tentacleColor;

	private static class MaxiJellyPool extends Pool<MaxiJelly>
	{
		@Override
		protected MaxiJelly newObject()
		{
			return new MaxiJelly();
		}
	}
	
	static
	{
		Map m = new Map("mini-jelly.v");
		points[0] = m.point("p1a").point.mul(SCALE);
		points[1] = m.point("p2a").point.mul(SCALE);
		points[2] = m.point("p3a").point.mul(SCALE);
		points[3] = m.point("p1b").point.mul(SCALE);
		points[4] = m.point("p2b").point.mul(SCALE);
		points[5] = m.point("p3b").point.mul(SCALE);
	}
	
	private final FixtureTag _fixtureTag = new FixtureTag(this, this);
	
	private final JellyfishTentacle[] _tentacles = new JellyfishTentacle[3];
	private static int numActive;
	
	public static void spawn()
	{
		switch(Game.Dice.nextInt(3))
		{
			case 0:
				spawn(Color.MAGENTA);
				break;
			case 1:
				spawn(Color.YELLOW);
				break;
			case 2:
				spawn(Color.CYAN);
				break;
		}
	}
	
	public static final IActiveCount activeCount = new IActiveCount()
	{		
		@Override
		public int activeCount()
		{
			return numActive;
		}
	};	
	
	public static void spawn(Color color)
	{
		MaxiJelly j = pool.obtain();
		
		j._killScore = 50;
		
		j._tentacleColor = color;
		
		float angle = Game.Dice.nextFloat() * 360f;
		
		Z.v1().set(ZipZapSim.SPAWN_DISTANCE, 0).rotate((float)(angle + 180)).add(Z.ship().origin());
		
		j._body = B2d
				.kinematicBody()
				.at(Z.v1())
				.rotated((float)Math.toRadians(angle))
				.linearVelocity(Z.v1().set(DRIFT_SPEED, 0).rotate(angle))
				.withFixture(B2d
						.loop(verts.shape(0).shape)
						.category(ZipZapSim.COL_CAT_METEORITE)
						.mask(ZipZapSim.COL_CAT_SHIP)
						.userData(j._fixtureTag))
				.create(Z.sim().world());
		
		for(int n = 0; n < 3; n++)
		{
			j._tentacles[n] = JellyfishTentacle.spawn(j._body, points[n], points[n+3], 8, color);
		}
		
		j.onSpawn();
		
		j._numEggs = START_NUM_EGGS;
		
		Z.sim().entities().add(j);
		
		numActive++;
	}
	
	private MaxiJelly()
	{
		_behaviours.add(homingBehaviour);
		_behaviours.add(new DieOnHitBehaviour(Color.MAGENTA, 12, false, null, 12));
		//_behaviours.add(homingBehaviour);
		_behaviours.add(FaceDirectionOfTravelBehaviour.instance());
		_behaviours.add(KillOnContactBehaviour.alsoDie());
		_behaviours.add(DieOnRangeBehaviour.instance());
		_behaviours.add(ExplosionOnHitBehaviour.magenta());
	}
	
	@Override
	public void onFree()
	{
		if(_killed)
		{
			Z.sim.fireEvent(ZipZapSim.EV_RUMBLE, 0.5f);
			Z.sim.fireEvent(ZipZapSim.EV_EXPLOSION_MEDIUM, 0.5f);
		}
			
		Z.sim().spawnExlosion(origin(), 16, _tentacleColor, 12f);
		Z.sim().spawnExlosion(origin(), 16, _tentacleColor);
	
		for(int n = 0; n < 3; n++)
		{
			_tentacles[n].free();
			_tentacles[n] = null;
		}
		
		pool.free(this);
		
		numActive--;	
	}

	@Override
	public int getNumPolys()
	{
		return verts.numShapes();
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
		return verts.shape(poly).shape;
	}

	@Override
	public Color color(int poly)
	{
		return color;
	}

	@Override
	public float lineWidth(int poly)
	{
		return 1f;
	}
	
	@Override
	public boolean hit(Fixture f, boolean mega, Vector2 loc, Vector2 norm)
	{
		Z.sim.fireEvent(SFX_HIT, null);
		
		if(!_dead)
		{
			Z.sim().spawnExlosion(origin(), 16, _tentacleColor, 6f);
			Z.sim().spawnExlosion(origin(), 16, _tentacleColor);
			
			super.hit(f, mega, loc, norm);
			
			if(!_dead && _numEggs > 0)
			{
				_layQueue++;				
				_numEggs--;
			}
		}
		
		return false;
	}
	
	@Override
	public boolean isLoop(int poly)
	{
		return true;
	}
	
	@Override
	public void update(float dt)
	{
		super.update(dt);
		
		if(!_dead)
		{
			if(_layTime > 0)
			{
				_layTime -= dt;
			}
			
			if(_layQueue > 0 && _layTime <= 0)
			{
				MiniJelly.spawn(origin().x, origin().y, (float) (Math.toDegrees(_body.getAngle()) + 160), _tentacleColor).attack();
				MiniJelly.spawn(origin().x, origin().y, (float) (Math.toDegrees(_body.getAngle()) + 200), _tentacleColor);	
				
				_layQueue--;
				
				_layTime = LAY_TIME;
				
				Z.sim.fireEvent(SFX_LAY, null);
			}
		}
	}
	
	@Override
	public float clipRadius()
	{
		return 20f;
	}
}
