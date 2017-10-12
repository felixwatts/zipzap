package com.monkeysonnet.zipzap.entities;

import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.engine.editor.Map;
import com.monkeysonnet.engine.editor.Shape;
import com.monkeysonnet.zipzap.IRenderableMultiPolygon;
import com.monkeysonnet.zipzap.Sim;
import com.monkeysonnet.zipzap.Tools;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;
import com.monkeysonnet.zipzap.behaviours.DieOnRangeBehaviour;
import com.monkeysonnet.zipzap.behaviours.ExplosionOnHitBehaviour;
import com.monkeysonnet.zipzap.behaviours.KillOnContactBehaviour;
import com.monkeysonnet.zipzap.script.IActiveCount;

public class Square extends Enemy implements IRenderableMultiPolygon
{
	private static final Map mapClosed = new Map("square.v", 3f, 0f);
	private static final Map mapOpen = new Map("square-open.v", 3f, 0f);	
	private static final Color[] colorsOpen = Tools.mapColours(mapOpen);
	
	private Fixture _fixtureClosed, _fixtureCore;
	private Fixture[] _fixturesOpen = new Fixture[mapOpen.numShapes()];
	
	private boolean _isOpen;
	
	private Timeline _timeline;
	
	private static int _activeCount;	
	public static final IActiveCount activeCount = new IActiveCount()
	{
		@Override
		public int activeCount()
		{
			return _activeCount;
		}
	};
	
	private static final SquarePool pool = new SquarePool();
	private static final float SPEED = 12;
	private static final float ANGULAR_VEL_RADS = 1f;
	private static final float FIRE_DELAY_MS = 100;
	private static final int FIRE_REPEAT = 5;
	private static final float OPEN_TIME_MS = 2000;
	protected static final float PROJECTILE_VELOCITY = 30;
	private static final int KILL_SCORE = 40;
	
	private static final int SFX_IMPACT = -1013;
	private static final int SFX_EXPLODE = -1004;
	private static final int SFX_OPEN = -1029;
	private static final int SFX_CLOSE = -1037;
	protected static final int SFX_FIRE = -1020;
	
	private static class SquarePool extends Pool<Square>
	{
		@Override
		protected Square newObject()
		{
			return new Square();
		}
	}
	
	private Square()
	{
		_behaviours.add(KillOnContactBehaviour.alsoDie());
		_behaviours.add(ExplosionOnHitBehaviour.yellow());
		_behaviours.add(DieOnRangeBehaviour.instance());
		
		_killScore = KILL_SCORE;
	}
	
	public static void spawn()
	{
		Square s = pool.obtain();
		
		s._body = B2d.kinematicBody()
				.at(Tools.randomSpawnLoc())
				.angularVelocity(ANGULAR_VEL_RADS)
				.create(Z.sim().world());	
		
		s._body.setLinearVelocity(Vector2
						.tmp
						.set(Z.ship().velocity())
						.mul(2f)
						.add(Z.ship().origin())
						.sub(s.origin()).nor().mul(SPEED));
		
		s._fixtureClosed = B2d
				.polygon(mapClosed.shape(0).shape)
				.userData(s._fixtureTag)
				.category(ZipZapSim.COL_CAT_METEORITE)
				.mask(ZipZapSim.COL_CAT_SHIP)
				.create(s._body);
		
		for(int n = 0; n < mapOpen.numShapes(); n++)
		{
			Shape sh = mapOpen.shape(n);
			
			if(sh.type == Shape.TYPE_LOOP)
			{			
				s._fixturesOpen[n] = B2d
					.loop(sh.shape)
					.userData(s._fixtureTag)
					.category(ZipZapSim.COL_CAT_METEORITE)
					.mask(ZipZapSim.COL_CAT_SHIP)
					.create(s._body);
				
				if(sh.label != null && sh.label.equals("core"))
					s._fixtureCore = s._fixturesOpen[n];
			}
		}
		
		s.open(false);
		
		Z.sim().entities().add(s);
		
		s.onSpawn();
		
		_activeCount++;
	}
	
	@Override
	public void onBeginContact(Contact c, Fixture me, Fixture other)
	{
		if(!_isOpen && me != _fixtureClosed)
			return;

		super.onBeginContact(c, me, other);
	}
	
	private void open(boolean open)
	{
		if(open == _isOpen)
			return;
		
		_isOpen = open;
		
		Z.sim.fireEvent(_isOpen ? SFX_OPEN : SFX_CLOSE, null);
		
		_fixtureClosed.setFilterData(open ? Sim.nullFilter : basicFilter);
		for(int n = 0; n < _fixturesOpen.length; n++)
		{
			if(_fixturesOpen[n] != null)
				_fixturesOpen[n].setFilterData(!open ? Sim.nullFilter : basicFilter);
		}
		
		_fixtureClosed.setUserData(open ? null : _fixtureTag);
		
		if(open)
		{
			if(_timeline != null)
				_timeline.kill();
			
			_timeline = Timeline.createSequence()
					.push(Tween.call(fireCallback).delay(FIRE_DELAY_MS).repeat(FIRE_REPEAT, FIRE_DELAY_MS))
					.pushPause(OPEN_TIME_MS)
					.push(Tween.call(closeCallback))
					.start(Z.sim().tweens());
			
			Z.sim().spawnFlash(origin(), Color.CYAN);
		}
	}
	
	private final TweenCallback fireCallback = new TweenCallback()
	{
		public void onEvent(int type, aurelienribon.tweenengine.BaseTween<?> source) 
		{
			Projectile.spawn(origin(), Vector2.tmp.set(PROJECTILE_VELOCITY, 0).rotate(angle()), 2, Color.GREEN, true);
			Projectile.spawn(origin(), Vector2.tmp.set(PROJECTILE_VELOCITY, 0).rotate(angle()+90), 2, Color.GREEN, true);
			Projectile.spawn(origin(), Vector2.tmp.set(PROJECTILE_VELOCITY, 0).rotate(angle()+180), 2, Color.GREEN, true);
			Projectile.spawn(origin(), Vector2.tmp.set(PROJECTILE_VELOCITY, 0).rotate(angle()+270), 2, Color.GREEN, true);
			
			Z.sim.fireEvent(SFX_FIRE, null);
		};
	};
	
	private final TweenCallback closeCallback = new TweenCallback()
	{
		public void onEvent(int type, aurelienribon.tweenengine.BaseTween<?> source) 
		{
			open(false);			
			_timeline = null;
		};
	};

	@Override
	public int getNumPolys()
	{
		return _isOpen ? mapOpen.numShapes() : mapClosed.numShapes();
	}

	@Override
	public float angle(int poly)
	{
		return (float)Math.toDegrees(_body.getAngle());
	}

	@Override
	public Vector2 origin(int poly)
	{
		return _body.getPosition();
	}

	@Override
	public Vector2[] verts(int poly)
	{
		return _isOpen ? mapOpen.shape(poly).shape : mapClosed.shape(poly).shape;
	}

	@Override
	public Color color(int poly)
	{
		return _isOpen ? colorsOpen[poly] : Color.GRAY;
	}

	@Override
	public float lineWidth(int poly)
	{
		return 1f;
	}
	
	@Override
	public boolean hit(Fixture f, boolean mega, Vector2 loc, Vector2 norm)
	{
		super.hit(f, mega, loc, norm);
		
		if(!_dead)
		{
			if(!_isOpen)
			{
				Z.sim.fireEvent(SFX_IMPACT, null);
				open(true);
				return true;
			}
			else
			{
				if(f == _fixtureCore)
				{
					Z.sim().spawnExlosion(origin(), 12, Color.GREEN);
					Z.sim().spawnFlash(origin(), Color.YELLOW);
					Z.sim().spawnDebris(this, _body.getLinearVelocity());
					Z.screen.sim().score(origin(), _killScore, true);
					free();	
					
					Z.renderer().shakeCamera(1, 0.5f);
					
					Z.sim.fireEvent(SFX_EXPLODE, null);
					
					return false;
				}
				else
				{
					Z.sim.fireEvent(SFX_IMPACT, null);
					return true;
				}
			}
		}
		
		return false;
	}
	
	@Override
	protected void onFree()
	{
		if(_timeline != null)
		{
			_timeline.kill();
			_timeline = null;
		}
		
		pool.free(this);
		
		_activeCount--;
	}
	
	@Override
	public boolean isLoop(int poly)
	{
		return _isOpen ? mapOpen.shape(poly).type == Shape.TYPE_LOOP : mapClosed.shape(poly).type == Shape.TYPE_LOOP;
	}	
	
	@Override
	public float clipRadius()
	{
		return 4f;
	}
}
