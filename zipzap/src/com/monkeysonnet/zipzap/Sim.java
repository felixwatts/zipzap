package com.monkeysonnet.zipzap;

import aurelienribon.tweenengine.TweenManager;
import aurelienribon.tweenengine.primitives.MutableFloat;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.monkeysonnet.engine.FixtureTag;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.engine.ISimulationEventHandler;
import com.monkeysonnet.engine.SpatialIndex;
import com.monkeysonnet.zipzap.entities.Meteor;
import com.monkeysonnet.zipzap.entities.Particle;

public class Sim
{
	public static final int EV_START = -1;
	public static final int EV_FLASH = -2;
	public static final int EV_TARGET_CHANGED = -3;
	public static final int EV_RUMBLE = -4;
	public static final int EV_SCRIPT_COMPLETE = -5;
	public static final int EV_ENQUEUE_NOTIFICATION = -6;
	public static final int EV_DEQUEUE_NOTIFICATION = -7;
	
	public static final float WORLD_STEP_TIME = 1f/60f;	
	private static final Vector2 defaultFocalPoint = new Vector2(0, 0);	
	public static final Filter nullFilter  = new Filter();	
	protected World _world;
	protected Array<IEntity> _entities;	
	private VectorPool _vectorPool;
	protected ISimulationEventHandler _handler;
	private IGameController _controller;
	private Vector2 _focalPoint;	
	public final MutableFloat timeMultiplier = new MutableFloat(1f);
	private final TweenManager _tweenManager = new TweenManager();	
	private SpatialIndex<IEntity> _environment = new SpatialIndex<IEntity>(32, true);
	private boolean _inPhysicalUpdate;	
	private final Vector2 _focalPointVel = new Vector2();
	private final Vector2 _focalPointCache = new Vector2();	
	private IOrigin _target;
	
	private boolean _paused;
	
	static
	{
		nullFilter.categoryBits = ZipZapSim.COL_CAT_SHIP;
		nullFilter.maskBits = ZipZapSim.COL_CAT_POWERUP;
	}
	
	public Sim(ISimulationEventHandler handler)
	{
		_handler = handler;
		_vectorPool = new VectorPool();
		_entities = new Array<IEntity>();
		_focalPoint = defaultFocalPoint;
	}
	
	public void pause()
	{
		_paused = true;
		if(_controller != null)
			_controller.pause();
	}
	
	public void resume()
	{
		_paused = false;
		if(_controller != null)
			_controller.resume();
	}
	
	public TweenManager tweens()
	{
		return _tweenManager;
	}
	
	public IOrigin target()
	{
		return _target;
	}
	
	public void target(IOrigin o)
	{
		_target = o;
		
		if(_handler != null)
			_handler.onSimulationEvent(ZipZapSim.EV_TARGET_CHANGED, null);
	}
	
	public Vector2 focalPoint()
	{
		return _focalPoint;
	}
	
	public void focalPoint(Vector2 v)
	{
		_focalPoint = v;
		_focalPointCache.set(v);
		_focalPointVel.set(0, 0);
	}
	
	public void addEnvironment(IEntity item, Vector2[] verts)
	{
		Vector2 min = Z.sim().vector().obtain().set(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
		Vector2 max = Z.sim().vector().obtain().set(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
		for(int n = 0; n < verts.length; n++)
		{
			if(verts[n].x > max.x)
				max.x = verts[n].x;
			if(verts[n].x < min.x)
				min.x = verts[n].x;
			
			if(verts[n].y > max.y)
				max.y = verts[n].y;
			if(verts[n].y < min.y)
				min.y = verts[n].y;
		}
		
		float width = (max.x - min.x);
		float height = (max.y - min.y);
		
		environment().put(item, min.x, min.y, width, height);
		
		Z.sim().vector().free(min);
		Z.sim().vector().free(max);
	}
	
	public void removeEnvironment(IEntity item)
	{
		environment().remove(item);
	}
	
	public void start()
	{
		start(Z.script.current());
	}
	
	public void start(IGameController controller)
	{
		clear();		
		setController(controller);		
		fireEvent(EV_START, null);
	}
	
	public void rayCast(RayCastCallback callback, Vector2 p1, Vector2 p2)
	{
		_inPhysicalUpdate = true;
		_world.rayCast(callback, p1, p2);
		_inPhysicalUpdate = false;
	}
	
	public void advanceScript()
	{			
		IGameController controller = Z.script.next();
		if(controller != null)
			setController(controller);
		else fireEvent(EV_SCRIPT_COMPLETE, null);
	}
	
	public void fireEvent(int ev, Object arg)
	{
		if(_controller != null)
			_controller.onSimulationEvent(ev, arg);
		
		if(_handler != null)
			_handler.onSimulationEvent(ev, arg);
	}
	
	public void spawnDebris(Vector2 origin, float angle, Vector2[] v, Color c, Vector2 vel)
	{
		if(c == null)
			return;
		
		if(v == null)
			return;
		
		int numVerts = v.length;
		while(numVerts > 0 && v[numVerts-1] == null)
			numVerts--;
		
		for(int n = 1; n <= numVerts; n++)
		{
			Debris.spawn(
				Game.workingVector2a.set(v[n-1]).rotate(angle).add(origin), 
				Game.workingVector2b.set(v[n%numVerts]).rotate(angle).add(origin), 
				Game.workingVector2c
					.set(v[n-1])
					.add(v[n%numVerts])
					.mul(0.5f)
					.nor()
					.rotate(angle)
					.mul(Meteor.SPLIT_SPEED * 3f)
					.add(vel),
					c);
		}
	}
	
	public void spawnDebris(IRenderableMultiPolygon p, Vector2 vel)
	{
		for(int n = 0; n < p.getNumPolys(); n++)
		{
			spawnDebris(p.origin(n), p.angle(n), p.verts(n), p.color(n), vel);
		}
	}	
	
	public void spawnDebris(IRenderableMultiPolygon p, int n, Vector2 vel)
	{	
		spawnDebris(p.origin(n), p.angle(n), p.verts(n), p.color(n), vel);
	}	

	public void spawnDebris(IRenderablePolygon p, Vector2 vel)
	{
		spawnDebris(p.origin(), p.angle(), p.verts(), p.color(), vel);
	}
	
	public VectorPool vector()
	{
		return _vectorPool;
	}
	
	public World world()
	{
		return _world;
	}
	
	public Array<IEntity> entities()
	{
		return _entities;
	}
	
	public SpatialIndex<IEntity> environment()
	{
		return _environment;
	}
	
	public boolean inPhysicalUpdate()
	{
		return _inPhysicalUpdate;
	}
	
	public void clear()
	{
		while(_entities.size > 0)
			_entities.get(0).free();
		
//		// clear particles created by first freeing
//		while(_entities.size > 0)
//			_entities.get(0).free();
		
		for(IEntity e : _environment.getAll())
			e.free();
		_environment.clear();		
		
		assert _entities.size == 0;
		
		_tweenManager.killAll();
				
		if(_world != null)
			_world.dispose();
				
		_world = new World(new Vector2(), true);
		_world.setContactListener(new com.monkeysonnet.engine.ContactListener());	
		
		target(null);
	}
	
	public void dispose()
	{
		if(_controller != null)
			_controller.cleanup();
		clear();
		_world.dispose();
	}
	
	public Vector2 focalPointVel()
	{
		return _focalPointVel;
	}
	
	public void update(float x, float y, float w, float h)
	{
		if(_paused)
			return;
		
		_focalPointVel.set(_focalPoint).sub(_focalPointCache).mul(1f/WORLD_STEP_TIME);
		
		if(_focalPoint != null)
			_focalPointCache.set(_focalPoint);
		
		float dt = WORLD_STEP_TIME * timeMultiplier.floatValue();
		
		if(_controller != null)
			_controller.update(dt);
	
		_inPhysicalUpdate = true;
		_world.step(dt, 3, 3);
		_inPhysicalUpdate = false;
		
		for(IEntity e : _environment.get(x, y, w, h))
			e.update(dt);
		
		for(IEntity e : _entities)
			e.update(dt);
		
		_tweenManager.update(dt * 1000f);
	}
	
	public Iterable<IEntity> getEnvironment(float x, float y, float w, float h)
	{
		_getEnvironmentResult.clear();
		_world.QueryAABB(getEnvironmentCallback, x, y, x+w, y+h);
		return _getEnvironmentResult;
	}
	
	private final Array<IEntity> _getEnvironmentResult = new Array<IEntity>();
	
	private final QueryCallback getEnvironmentCallback = new QueryCallback()
	{		
		@Override
		public boolean reportFixture(Fixture fixture)
		{
			if(fixture.getUserData() != null)
			{
				FixtureTag t = (FixtureTag)fixture.getUserData();
				if(t.owner instanceof IEnvironment)
				{
					IEntity e = (IEntity)t.owner;
					
					if(!_getEnvironmentResult.contains(e, true))
						_getEnvironmentResult.add(e);
				}
			}	
			
			return true;
		}
	};
	
	public void setController(IGameController c)
	{
		if(_controller != null)
			_controller.cleanup();
		_controller = c;
		_controller.init();
	}
	
	public void spawnExlosion(Vector2 origin)
	{
		spawnExlosion(origin, 16, Color.YELLOW);
	}

	public void spawnExlosion(Vector2 origin, int num, Color c)
	{
		for(int n = 0; n < num; n++)
		{
			Game.workingVector2a.set(Game.Dice.nextFloat() + 0.5f, 0).mul(8f).rotate(Game.Dice.nextFloat() * 360f);
			Particle.spawn(origin, Game.workingVector2a, c, Game.Dice.nextFloat() + 0.25f, 0);
		}
	}
	
	public void spawnExlosion(Vector2 origin, int num, Color c, float radius)
	{
		for(int n = 0; n < num; n++)
		{
			Game.workingVector2a.set(Game.Dice.nextFloat() + 0.5f, 0).mul(8f).rotate(Game.Dice.nextFloat() * 360f);
			Particle.spawn(origin, Game.workingVector2a, c, (Game.Dice.nextFloat() + 0.25f) * radius, 0);
		}
	}
	
	public void spawnFlash(Vector2 loc, Color col)
	{
		Particle.spawn(loc, Tools.zeroVector, col, 20f, -10f, 0.15f);
	}
	
	public void spawnCloud(Vector2 origin, int num, Color c, float radius)
	{
		for(int n = 0; n < num; n++)
		{
			Game.workingVector2a.set(Game.Dice.nextFloat() + 0.5f, 0).mul(1f).rotate(Game.Dice.nextFloat() * 360f);
			Particle.spawn(origin, Game.workingVector2a, c, (Game.Dice.nextFloat() + 0.25f) * radius, radius/2f);
		}
	}
	
	public void flash(Color c)
	{
		fireEvent(EV_FLASH, c);
	}
}
