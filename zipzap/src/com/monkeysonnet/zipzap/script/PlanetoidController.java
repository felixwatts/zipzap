package com.monkeysonnet.zipzap.script;

import java.util.Hashtable;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.engine.editor.Map;
import com.monkeysonnet.engine.editor.Point;
import com.monkeysonnet.engine.editor.Shape;
import com.monkeysonnet.zipzap.IOrigin;
import com.monkeysonnet.zipzap.ITriggerable;
import com.monkeysonnet.zipzap.Tools;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.achievements.BadgeTubulon;
import com.monkeysonnet.zipzap.entities.CloudSpawner;
import com.monkeysonnet.zipzap.entities.Decal;
import com.monkeysonnet.zipzap.entities.FallingRock;
import com.monkeysonnet.zipzap.entities.MinerDude;
import com.monkeysonnet.zipzap.entities.Particle;
import com.monkeysonnet.zipzap.entities.TriggerArea;
import com.monkeysonnet.zipzap.entities.Wall;
import com.monkeysonnet.zipzap.entities.WormEgg;
import com.monkeysonnet.zipzap.entities.WormSegment;

public class PlanetoidController extends EnvironmentController
{
	public static final float SCALE = 4;
	
	private Array<Array<FallingRock>> _rockfalls = new Array<Array<FallingRock>>();
	private boolean _wormSpawned;
	private final Color _mistColor = new Color(0, 1, 0, 0.1f);
	private Array<Wall> _bangBoxes = new Array<Wall>();
	private Array<Decal> _decals = new Array<Decal>();

	public PlanetoidController()
	{
		super(new Map("planetoid.v", SCALE, 0));
	}
	
	@Override
	public void init()
	{
		super.init();
		
		Z.screen.sim().specks(false);
		
		Particle.primePool();

		Z.sim().world().setGravity(Vector2.tmp.set(0, -5));
		
		TriggerArea.spawn(_map.shape("trg-worm-spawn").shape, new ITriggerable()
		{			
			@Override
			public boolean trigger()
			{
				Vector2.tmp.set(_map.point("loc-worm-spawn").point);
				WormSegment.spawnTubulon(Vector2.tmp.x, Vector2.tmp.y);// Tubulon.spawn(Vector2.tmp.x, Vector2.tmp.y);
				
				Tween.call(changeTargetCallback).delay(6000).start(Z.sim().tweens());
				
				_wormSpawned = true;
				return true;
			}
		});
		
		initRockfalls();		
		initEggs();		
		initClouds();
		initDecals();
		initMiners();
		
		for(int n = 0; n < _map.numShapes(); n++)
		{
			if(_map.shape(n).properties != null && _map.shape(n).properties.equals("bang-box"))
			{
				_bangBoxes.add(Wall.spawn(_map.shape(n).shape, Color.RED));
			}
			
			if(_map.shape(n).properties != null && _map.shape(n).properties.equals("decking"))
			{
				Wall.spawn(_map.shape(n).shape, Color.CYAN);
			}
		}
		
		TriggerArea.spawn(_map.shape("trg-worm-explode").shape, triggerWormExplode);
		
		Z.ship().setPosition(_map.point("start").point, 90);
		
		Z.screen.sim().target(new IOrigin()
		{	
			Vector2 _v = _map.point("target-1").point;
			
			@Override
			public Vector2 origin()
			{
				return _v;
			}
			
			@Override
			public float angle()
			{
				return 0;
			}
		});
	}
	
	private void initMiners()
	{
		MinerDude.spawn(_map.point("man-1").point, Color.CYAN);
		MinerDude.spawn(_map.point("man-2").point, Color.YELLOW);
	}

	private final ITriggerable triggerWormExplode = new ITriggerable()
	{			
		@Override
		public boolean trigger()
		{
			if(_wormSpawned)
			{
				Tween.call(new TweenCallback()
				{
					@Override
					public void onEvent(int type, BaseTween<?> source)
					{
						for(int n = 0; n < _map.numShapes(); n++)
						{
							if(_map.shape(n).properties != null && _map.shape(n).properties.equals("bang-box"))
							{
								Vector2 v = Tools.centre(_map.shape(n).shape);
								Z.sim().spawnExlosion(v, 1, Color.GRAY, 32);
								Z.sim().spawnExlosion(v, 2, Color.ORANGE, 16);
								Z.sim().spawnExlosion(v, 2, Color.YELLOW, 8);
								Z.sim().spawnExlosion(v, 2, Color.WHITE, 4);							
							}
						}
						
						for(Decal d : _decals)
							d.free();
						for(Wall w : _bangBoxes)
							w.free();
						
						float minDst2 = Float.POSITIVE_INFINITY;
						Vector2 explosionPoint = _map.point("loc-explosion").point;
						WormSegment nearest = null;
						
						for(int n = Z.sim().entities().size-1; n >= 0; n--)
						{
							IEntity e = Z.sim().entities().get(n);
							
							if(e instanceof WormSegment)
							{
								WormSegment s = (WormSegment)e;
								
								if(s.behaviour() == WormSegment.BH_BABYTUB)
									s.free();								
								else
								{
									float dst2 = s.origin().dst2(explosionPoint);
									if(dst2 < minDst2)
									{
										minDst2 = dst2;
										nearest = s;
									}
								}
							}
						}
						
						nearest.free();
					}
				})
				.delay(1500f)
				.start(Z.sim().tweens());
				
				Z.sim().setController(new DefeatBossController(_map.point("loc-watch-explosion").point));
				
				Z.achievments.earn(new BadgeTubulon());
				
				return true;
			}
			else return false;
		}
	};
	
	private void initEggs()
	{
		Hashtable<String, WormEggTriggerHandler> triggers = new Hashtable<String, WormEggTriggerHandler>();
		
		for(int n = 0; n < _map.numShapes(); n++)
		{
			Shape p = _map.shape(n);
			if(p.hasProperty("worm-egg-trigger"))
			{
				WormEggTriggerHandler handler = new WormEggTriggerHandler();
				triggers.put(p.label, handler);
				TriggerArea.spawn(p.shape, handler);
			}
		}
		
		for(int n = 0; n < _map.numPoints(); n++)
		{
			Point p = _map.point(n);
			if(p.hasProperty("egg"))
			{
				WormEgg egg = WormEgg.spawn(p.point);
				if(p.propertyValue("trigger") != null)
					triggers.get(p.propertyValue("trigger")).add(egg);
			}
		}
	}
	
	private void initClouds()
	{
		for(int n = 0; n < _map.numPoints(); n++)
		{
			Point p = _map.point(n);
			if(p.hasProperty("cloud"))
			{
				CloudSpawner.spawn(p.point, _mistColor, 16);
			}
		}
	}
	
	public void initDecals()
	{
		for(int n = 0; n < _map.numPoints(); n++)
		{
			Point p = _map.point(n);
			if(p.hasProperty("decal-fire"))
			{
				_decals.add(Decal.spawn(Z.texture("zipzap-powerup-bomb"), 2, p.point, Color.RED));
			}
			if(p.hasProperty("decal-mist"))
			{
				Decal.spawn(Z.texture("zipzap-node"), 12, p.point, _mistColor);
			}
		}
	}

	public void initRockfalls()
	{
		int n = 0;
		while(true)
		{
			if(_map.shape("trg-rf" + (n+1)) == null)
				break;
			
			_rockfalls.add(new Array<FallingRock>());
			
			for(int s = 0; s < _map.numShapes(); s++)
			{
				if(_map.shape(s).properties != null && _map.shape(s).properties.equals("rf." + (n+1)))
				{
					_rockfalls.get(n).add(FallingRock.spawn(_map.shape(s).shape));
				}
			}
			
			TriggerArea.spawn(_map.shape("trg-rf" + (n+1)).shape, new RockfallTriggerHandler(n));
			
			n++;
		}
	}
	
	@Override
	public void cleanup()
	{
		super.cleanup();
		
		_rockfalls.clear();
		_wormSpawned = false;
		Z.sim().world().setGravity(Vector2.tmp.set(0, 0));
		Z.screen.sim().specks(true);
	}
	
	private class RockfallTriggerHandler implements ITriggerable
	{
		private int _n;
		
		public RockfallTriggerHandler(int n)
		{
			_n = n;
		}

		@Override
		public boolean trigger()
		{
			if(_wormSpawned)
			{
				for(FallingRock r : _rockfalls.get(_n))
					r.fall();					
				return true;
			}
			else return false;
		}
	}
	
	private class WormEggTriggerHandler implements ITriggerable
	{
		private Array<WormEgg> _eggs = new Array<WormEgg>();
		
		public void add(WormEgg egg)
		{
			_eggs.add(egg);
		}

		@Override
		public boolean trigger()
		{
			if(_wormSpawned)
			{
				for(WormEgg e : _eggs)
				{
					if(!e.dead())
					{
						e.free();
						break;
					}
				}
				
				return true;
			}
			else return false;
		}
	}
	
	private final TweenCallback changeTargetCallback = new TweenCallback()
	{		
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			Z.screen.sim().target(new IOrigin()
			{	
				Vector2 _v = _map.point("target-2").point;
				
				@Override
				public Vector2 origin()
				{
					return _v;
				}
				
				@Override
				public float angle()
				{
					return 0;
				}
			});
		}
	};
}
