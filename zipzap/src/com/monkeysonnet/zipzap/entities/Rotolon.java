package com.monkeysonnet.zipzap.entities;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.equations.Quad;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.engine.BodyTweener;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.zipzap.ElectricBeamWidth;
import com.monkeysonnet.zipzap.IBossBody;
import com.monkeysonnet.zipzap.IRenderableMultiPolygon;
import com.monkeysonnet.zipzap.Sim;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;
import com.monkeysonnet.zipzap.achievements.BadgePrismolon;
import com.monkeysonnet.zipzap.behaviours.FlyingSoundBehaviour;
import com.monkeysonnet.zipzap.behaviours.KillOnContactBehaviour;
import com.monkeysonnet.zipzap.behaviours.ShieldBehaviour;
import com.monkeysonnet.zipzap.script.DefeatBossController;

public class Rotolon extends Entity implements IRenderableMultiPolygon, IBossBody
{	
	private static final int SHOOT_SEQ_2_REPEAT = 2;
	private static final float SHOOT_SEQ_2_ARC_TIME = 1000f;// 1200f;
	private static final float LASER_COOL_TIME_MS = 800f;// 1000;
	private static final float ROTATE_TIME_MS = 4000f;// 6000;
	private static final float LASER_WARM_UP_TIME_MS = 800f;//1000;
	private static final float SHOOT_SEQ_1_REPEAT_DELAY_MS = 1000f;// 1200;
	private static final int NUM_LAYERS = 4;
	private static final float SHIELD_STRENGTH = 12f;
	private static final float SHIELD_RECHARGE_RATE = 0.0f;
	private static final Color GLOW_COLOR = new Color(0.5f, 0, 0f, 0.2f);
	private static final Color COLD_COLOR = new Color(0.5f, 0.5f, 0.5f, 1f);
	private static final int START_RADIUS = 6;
	private static final int DELTA_RADIUS = 3;
	protected static final float PROJECTILE_SPEED = 30;
	protected static final int SFX_FIRE = -1030;
	protected static final int SFX_LASER_CHARGE = -1032;
	protected static final int SFX_LASER_ON = -1033;
	protected static final int SFX_FLY = 31;
	private static final float SHOOT_SEQ_1_RELOAD_TIME_MS = 400;
	private static final int SHOOT_SEQ_1_NUM_REPEATS = 2;
	//private static final int SFX_DIE = -1003;
	private static final int SFX_DIE2 = -1004;
	
	private Prismoid _prismoid;
	private boolean _shieldOn;
	private final ShieldBehaviour _shieldBehaviour = new ShieldBehaviour(NUM_LAYERS, SHIELD_STRENGTH, SHIELD_RECHARGE_RATE, GLOW_COLOR, COLD_COLOR, null);
	private Vector2[][] _verts;
	private ElectricBeamWidth _beamWidth;
	private final Vector2 _startLoc = new Vector2();
	private final LaserBeam[] _beams = new LaserBeam[3];
	private Timeline _timeline;
	private Color _projectileColor = Color.RED;
	
	public Rotolon()
	{
		BossEye.instance().init(Color.CYAN, 1f, this);
		_verts = new Vector2[NUM_LAYERS][];
		for(int n = 0; n < NUM_LAYERS; n++)
		{
			_verts[n] = new Vector2[3];
			for(int v = 0; v < 3; v++)
			{
				_verts[n][v] = Z.sim().vector().obtain();
				_verts[n][v].set(START_RADIUS + (DELTA_RADIUS * n), 0).rotate(90 + (v * 120));
			}
		}
		
		_beamWidth = new ElectricBeamWidth();
		
		_behaviours.add(_shieldBehaviour);
		_behaviours.add(KillOnContactBehaviour.basic());
		_behaviours.add(new FlyingSoundBehaviour(SFX_FLY, 5f, 40f));
	}
	
	public static void spawn()
	{
		Rotolon r = new Rotolon();	
		
		r._projectileColor = Color.RED;

		r._prismoid = new Prismoid(r);	
		
		r._body = B2d
				.staticBody()
				.at(Z.ship().origin().x + ZipZapSim.SPAWN_DISTANCE, Z.ship().origin().y)
				.create(Z.sim().world());
		
		BossEye.instance().setLoc(r.origin(), 0);
		
		for(int n = 0; n < 3; n++)
		{
			r._beams[n] = LaserBeam.spawn(Color.RED);			
		}
		
		for(int n = 0; n < NUM_LAYERS; n++)
		{
			Fixture f = B2d
				.polygon(r._verts[n])
				.category(ZipZapSim.COL_CAT_METEORITE)
				.mask(ZipZapSim.COL_CAT_SHIP)
				.userData(r._fixtureTag)
				.create(r._body);
			
			r._shieldBehaviour.addFixture(n, f);
		}
		
		r._shieldOn = true;
		

		Z.sim().entities().add(r._prismoid);			
		Z.sim().entities().add(r);
		
//		Tween.to(r._body, BodyTweener.VAL_POS_XY, 6000)
//			.target(Z.ship().origin().x + ORBIT_DST, Z.ship().origin().y)
//			.ease(Quad.OUT)
//			.setCallbackTriggers(TweenCallback.COMPLETE)
//			.setCallback(r.arriveCallback)
//			.start(Z.sim().tweens());
		
		r.onSpawn();
		
		r.arriveCallback.onEvent(0, null);
		
	}
	
	private final TweenCallback arriveCallback = new TweenCallback()
	{		
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			_startLoc.set(_body.getWorldCenter());
			beginSequence();
		}
	};
	
	private TweenCallback sequenceCompleteCallback = new TweenCallback()
	{		
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{		
			_shieldOn = false;
			_prismoid.beginSequence(getSequenceRadius(level()));
			
			killTweens();
		}
	};
	
	private TweenCallback fireCallback = new TweenCallback()
	{		
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{		
			Float angleOffset = (Float)source.getUserData();
			for(int n = 0; n < 3; n++)
			{
				Projectile.spawn(origin(0), Z.v1().set(PROJECTILE_SPEED, 0).rotate(30 + (120 * n) + angleOffset), 2, _projectileColor, true);
			}
			
			Z.sim.fireEvent(SFX_FIRE, null);
		}
	};
	
	private TweenCallback randomFireCallback = new TweenCallback()
	{		
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{		
			Projectile.spawn(origin(0), Z.v1().set(PROJECTILE_SPEED, 0).rotate(Game.Dice.nextFloat()*360), 1, _projectileColor, true);
		}
	};
	
	private TweenCallback setLasersCallback = new TweenCallback()
	{		
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			Integer mode = (Integer)source.getUserData();
			for(int n = 0; n < 3; n++)
			{
				_beams[n].mode(mode);
			}
			
			switch(mode)
			{
				case LaserBeam.MODE_ON:
					Z.sim.fireEvent(SFX_LASER_ON, null);
					break;
			}
		}
	};
	private final TweenCallback playSoundCallback = new TweenCallback()
	{
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			int sfx = (Integer)source.getUserData();
			Z.sim.fireEvent(sfx, null);
		}
	};
	
	private void beginSequence()
	{
		//killTweens();
		
		_shieldOn = true;
		
		_body.setTransform(_body.getWorldCenter(), 0);
		
		Timeline shootSequence1 = Timeline.createSequence();
		for(int c = 0; c < 3; c++)
		{
			shootSequence1.push(Tween.call(fireCallback).setUserData(0f));
			
			for(int n = 1; n < level() + 1; n++)
			{
				float offset = (60f / (level()+1)) * n;
				shootSequence1.push(Tween.call(fireCallback).setUserData(offset));
				shootSequence1.push(Tween.call(fireCallback).setUserData(-offset));
			}
			
			shootSequence1.pushPause(SHOOT_SEQ_1_RELOAD_TIME_MS);
		}
		shootSequence1.repeat(SHOOT_SEQ_1_NUM_REPEATS, SHOOT_SEQ_1_REPEAT_DELAY_MS);
		
		Timeline turnSequence = Timeline
				.createSequence()
				.push(Tween.set(_body, BodyTweener.VAL_ANGLE).target(0))
				.push(Tween.call(setLasersCallback).setUserData(LaserBeam.MODE_WARM_UP))
				.push(Tween.call(playSoundCallback ).setUserData(SFX_LASER_CHARGE))
				.pushPause(LASER_WARM_UP_TIME_MS)
				.push(Tween.call(setLasersCallback).setUserData(LaserBeam.MODE_ON))
				.push(Tween.to(_body, BodyTweener.VAL_ANGLE, ROTATE_TIME_MS)
						.target((float)(Math.PI * 2f/3f))
						.ease(Quad.INOUT))
				.push(Tween.call(setLasersCallback).setUserData(LaserBeam.MODE_WARM_UP))
				.pushPause(LASER_COOL_TIME_MS)
				.push(Tween.call(setLasersCallback).setUserData(LaserBeam.MODE_OFF));
		
		Timeline shootSequence2 = Timeline.createSequence();
		
		int l = (level() * 2) + 4;
		float step = 120f / l;
		float pause = SHOOT_SEQ_2_ARC_TIME / l;
		for(int n = 0; n < l; n++)
		{
			float offset = -60f + (n * step);
			shootSequence2.push(Tween.call(fireCallback).setUserData(offset));
			shootSequence2.pushPause(pause);
		}
		
		shootSequence2.repeatYoyo(SHOOT_SEQ_2_REPEAT, 0);
		
		Timeline turnSequence2 = Timeline
				.createSequence()
				.push(Tween.set(_body, BodyTweener.VAL_ANGLE).target(0))
				.push(Tween.call(setLasersCallback).setUserData(LaserBeam.MODE_WARM_UP))
				.pushPause(LASER_WARM_UP_TIME_MS)
				.push(Tween.call(setLasersCallback).setUserData(LaserBeam.MODE_ON))
				.push(Tween.to(_body, BodyTweener.VAL_ANGLE, ROTATE_TIME_MS)
						.target((float)(Math.PI * 2f/3f))
						.ease(Quad.INOUT))
				.push(Tween.call(setLasersCallback).setUserData(LaserBeam.MODE_WARM_UP))
				.pushPause(LASER_COOL_TIME_MS)
				.push(Tween.call(setLasersCallback).setUserData(LaserBeam.MODE_OFF));
		
		_timeline = Timeline
				.createSequence()
				.push(shootSequence1)
				.push(turnSequence)
				.push(shootSequence2)
				.push(turnSequence2)
				.setCallbackTriggers(TweenCallback.COMPLETE)
				.setCallback(sequenceCompleteCallback)
				.start(Z.sim().tweens());	
	}
	
	private void killTweens()
	{
		if(_timeline != null)
		{
			_timeline.kill();
			_timeline = null;
		}
		Z.sim().tweens().killTarget(_body);
	}
	
	@Override
	protected void onFree()
	{
		killTweens();
		
		_beamWidth.free();
		
		for(int n = 0; n < NUM_LAYERS; n++)
		{
			for(int v = 0; v < 3; v++)
			{
				Z.sim().vector().free(_verts[n][v]);
				_verts[n][v] = null;
			}
		}
		
		for(int v = 0; v < 3; v++)
		{
			_beams[v].free();
			_beams[v] = null;
		}
	}
	
	public void onPrismoidSequenceComplete()
	{
		beginSequence();
	}
	
	private static float getSequenceRadius(int level)
	{
		return 30 - (5 * level);
	}
	
	@Override
	public boolean hit(Fixture f, boolean mega, Vector2 loc, Vector2 norm)
	{
		if(_shieldOn)
			return true;
		else 
		{
			Z.sim.fireEvent(Sim.EV_RUMBLE, 0.25f);
			Z.sim.spawnExlosion(loc, 5, Color.YELLOW);
			return super.hit(f, mega, loc, norm);
		}
	}

	@Override
	public int getNumPolys()
	{
		if(_shieldOn)
			return NUM_LAYERS + 2;
		else return NUM_LAYERS;
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
		if(poly >= NUM_LAYERS)
			return _verts[outerLayer()];
		else return _verts[poly];
	}

	@Override
	public Color color(int poly)
	{
		if(poly >= NUM_LAYERS)
		{
			// shield
			if(_shieldOn)
			{
				switch(poly)
				{
					case NUM_LAYERS:
					default:
						return Color.CYAN;
					case NUM_LAYERS + 1:
						return Color.WHITE;
				}
			}
			else return null;
		}
		else return _shieldBehaviour.getColor(poly);
	}

	@Override
	public float lineWidth(int poly)
	{
		if(poly >= NUM_LAYERS)
		{
			switch(poly)
			{
				case NUM_LAYERS:
					return _beamWidth.beamWidth();
				default:
					return 1f;
			}
		}
		else return 1f;
	}
	
	@Override
	public void update(float dt)
	{
		super.update(dt);
		
		if(!_dead)
		{
			BossEye.instance().setLoc(origin(), -90);
			
			for(int n = 0; n < 3; n++)
			{			
				_beams[n].set(origin(0), (-30 + (n*120)) + angle(0));
			}
		}
	}
	
	private int level()
	{
		return NUM_LAYERS - outerLayer();
	}
	
	private int outerLayer()
	{
		int l = 0;
		while(l < NUM_LAYERS - 1 && _shieldBehaviour.energy(l+1) >= 0)
			l++;
		return l;
	}

	@Override
	public boolean isEyeVulnerable()
	{
		return outerLayer() == 0;
	}

	@Override
	public void onEyeHit()
	{
		_prismoid.free();
		
		Z.sim().setController(new DefeatBossController(origin()));
		
		_projectileColor = Color.CYAN;
		
		Timeline t = Timeline.createSequence()
			.push(Tween.call(randomFireCallback))
			.pushPause(20)
			.push(Tween.call(randomFireCallback))
			.pushPause(20)
			.push(Tween.call(randomFireCallback))
			.pushPause(20)
			.push(Tween.call(randomFireCallback))
			.pushPause(200);
			
		for(int n = 0; n < 12; n++)
		{
			t.push(Tween.call(fireCallback).setUserData(-60f + 10 * n));
		}
		
		t
			.setCallbackTriggers(TweenCallback.COMPLETE)
			.setCallback(dieCallback)
			.start(Z.sim().tweens());	
		
		Z.sim().spawnFlash(origin(), Color.CYAN);
		Z.sim.spawnCloud(origin(), 4, Color.BLUE, 12f);
		Z.sim.spawnCloud(origin(), 4, Color.CYAN, 6f);
		Z.sim.spawnCloud(origin(), 4, Color.WHITE, 3f);
		
		if(BadgePrismolon.instance().queue())
			BossEye.instance().doNotification();
	}
	
	public float radius()
	{
		return START_RADIUS + (DELTA_RADIUS * outerLayer());
	}
	
	private final TweenCallback dieCallback = new TweenCallback()
	{
		
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			//Z.sim().spawnCloud(origin(), 1, Color.RED, 6f);
			//Z.sim().spawnExlosion(origin(), 4, Color.RED, 4);
			//Z.sim().spawnExlosion(origin(), 4, new Color(1f, 0.5f, 0.5f, 1f), 2);
			Z.sim().spawnExlosion(origin(), 12, Color.WHITE);
			
			Z.sim.fireEvent(SFX_DIE2, null);
			
			BossEye.instance().free();
			free();
		}
	};
	
	@Override
	public boolean isLoop(int poly)
	{
		return true;
	}
	
	@Override
	public float clipRadius()
	{
		return 40f;
	}
}
