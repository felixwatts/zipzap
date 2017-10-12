package com.monkeysonnet.zipzap.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Array;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.editor.Map;
import com.monkeysonnet.engine.editor.Point;
import com.monkeysonnet.engine.editor.Shape;
import com.monkeysonnet.zipzap.IBossBody;
import com.monkeysonnet.zipzap.IFixtureEventHandler;
import com.monkeysonnet.zipzap.IRenderableMultiPolygon;
import com.monkeysonnet.zipzap.Tools;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;
import com.monkeysonnet.zipzap.achievements.BadgeTerralon;
import com.monkeysonnet.zipzap.behaviours.BlobTrailBehaviour;
import com.monkeysonnet.zipzap.behaviours.FaceDirectionOfTravelBehaviour;
import com.monkeysonnet.zipzap.behaviours.HomingBehaviour;
import com.monkeysonnet.zipzap.behaviours.KillOnContactBehaviour;
import com.monkeysonnet.zipzap.behaviours.ShieldBehaviour;
import com.monkeysonnet.zipzap.script.DefeatBossController;

public class Mutha extends Entity implements IRenderableMultiPolygon, IBossBody
{
	private static final float SHIELD_STRENGTH = 6f;
	private static final float SHIELD_RECHARGE_RATE = 0.0f;
	private static final Color GLOW_COLOR = new Color(Color.ORANGE);
	private static final Color COLD_COLOR = new Color(0.7f, 0.7f, 0.8f, 1f);
	private static final float SPEED = 4;
	private static final int SFX_FIXTURE_DESTROYED = -1014;
	private static final int SFX_DIE2 = -1004;
	
	private final Map _map = new Map("mutha.v", 4f, 180);	
	private final Array<Fixture> _hardpointFixtures = new Array<Fixture>();
	private final Fixture[] _boosterFixtures = new Fixture[3];
	private final BlobTrailBehaviour[] _boosterBehaviours = new BlobTrailBehaviour[3];
	private final Array<MuthaHardpoint> _hardpoints = new Array<MuthaHardpoint>();
	private final ShieldBehaviour _shieldBehaviour = new ShieldBehaviour(
			_map.numShapes(), 
			SHIELD_STRENGTH, 
			SHIELD_RECHARGE_RATE, 
			GLOW_COLOR, 
			COLD_COLOR,
			new IFixtureEventHandler()
			{					
				@Override
				public void onEvent(Fixture f)
				{
					onFixtureDestroyed(f);
				}
			});
	private final Vector2[] _fixtureCentres = new Vector2[_map.numShapes()];
	private final Color[] _fixtureColors = new Color[_map.numShapes()];
	private Fixture _cockpitFixture;
	
	public Mutha()
	{
		BossEye.instance().init(Color.YELLOW, 1f, this);
		
		_behaviours.add(_shieldBehaviour);
		_behaviours.add(FaceDirectionOfTravelBehaviour.instance());
		_behaviours.add(new HomingBehaviour(360, 5, false));
		_behaviours.add(KillOnContactBehaviour.basic());
		
		_body = B2d
				.kinematicBody()
				.at(Z.ship().origin().x, Z.ship().origin().y - ZipZapSim.SPAWN_DISTANCE * 2)
				.rotated((float)Math.toRadians(90))
				.linearVelocity(Z.v1().set(SPEED, 0).rotate(90))
				.create(Z.sim().world());
		
		BossEye.instance().setLoc(origin(), angle());
		
		_boosterBehaviours[0] = new BlobTrailBehaviour(Color.CYAN, 0.1f, 0.5f, _map.point("booster-1").point);
		_boosterBehaviours[1] = new BlobTrailBehaviour(Color.CYAN, 0.1f, 0.5f, _map.point("booster-2").point);
		_boosterBehaviours[2] = new BlobTrailBehaviour(Color.CYAN, 0.1f, 0.5f, _map.point("booster-3").point);
		
		_behaviours.add(_boosterBehaviours[0]);
		_behaviours.add(_boosterBehaviours[1]);
		_behaviours.add(_boosterBehaviours[2]);
		
		Z.sim().entities().add(this);
		
		for(int n = 0; n < _map.numShapes(); n++)
		{
			Shape s = _map.shape(n);			
			
			Fixture f = B2d.loop(s.shape).category(ZipZapSim.COL_CAT_METEORITE).mask(ZipZapSim.COL_CAT_SHIP).userData(_fixtureTag).create(_body);				
			_shieldBehaviour.addFixture(n, f);
			
			if(s.properties != null && s.properties.equals("yellow"))
				_fixtureColors[n] = Color.YELLOW;
			else if(s.properties != null && s.properties.equals("cyan"))
				_fixtureColors[n] = Color.CYAN;
			else _fixtureColors[n] = Color.BLUE;
			
			if(s.label != null)
			{
				Point p = _map.point(s.label);		
				if(p != null)
					createHardpoint(p, p.properties, f);	
				
				if(s.label.equals("hp-centre"))
					_cockpitFixture = f;
				
				if(s.label.equals(_map.point("booster-1").properties))
					_boosterFixtures[0] = f;
				else if(s.label.equals(_map.point("booster-2").properties))
					_boosterFixtures[1] = f;
				else if(s.label.equals(_map.point("booster-3").properties))
					_boosterFixtures[2] = f;
			}
			
			_fixtureCentres[n] = Z.sim().vector().obtain().set(Tools.centre(s.shape));							
		}
		
		onSpawn();		
	}

	private void createHardpoint(Point p, String properties, Fixture f)
	{
		int type;		
		float aOff = 0;
		
		if(properties.equals("minigun"))
			type = MuthaHardpoint.TYPE_MINIGUN;
		else if(properties.equals("seeker"))
		{
			type = MuthaHardpoint.TYPE_HEAT_SEEKER;
			aOff = 180;
		}
		else if(properties.equals("gnat"))
			type = MuthaHardpoint.TYPE_GNAT;
		else 
			type = MuthaHardpoint.TYPE_LASER;
		
		MuthaHardpoint h = new MuthaHardpoint(this, p.point, aOff, type);
		
		_hardpointFixtures.add(f);
		_hardpoints.add(h);
	}

	protected void onFixtureDestroyed(Fixture f)
	{		
		Z.sim.fireEvent(SFX_FIXTURE_DESTROYED, null);
		
		int i = _hardpointFixtures.indexOf(f, true);
		if(i >= 0)
		{
			if(_hardpoints.get(i).type() != MuthaHardpoint.TYPE_GNAT)
			{
				_hardpoints.get(i).free();
				_hardpoints.removeIndex(i);
				_hardpointFixtures.removeIndex(i);
			}
		}	
		
		for(i = 0; i < _boosterFixtures.length; i++)
		{
			if(_boosterFixtures[i] == f)
			{
				_boosterFixtures[i] = null;
				_behaviours.removeValue(_boosterBehaviours[i], true);
			}
		}
		
		if(f == _cockpitFixture)
			_cockpitFixture = null;
	}

	@Override
	public int getNumPolys()
	{
		return _map.numShapes();
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
		return _map.shape(poly).shape;
	}

	@Override
	public Color color(int poly)
	{
		return _shieldBehaviour.energy(poly) < 0 ? null : _fixtureColors[poly];
	}

	@Override
	public float lineWidth(int poly)
	{
		return 1f;
	}
	
	@Override
	protected void onFree()
	{
		for(int n = 0; n < _fixtureCentres.length; n++)
		{
			if(_fixtureCentres[n] != null)
			{
				Z.sim().vector().free(_fixtureCentres[n]);
				_fixtureCentres[n] = null;
			}
		}
		
		for(MuthaHardpoint h : _hardpoints)
			h.free();
		
		_hardpoints.clear();
		_hardpointFixtures.clear();
	}
	
	@Override
	public void update(float dt)
	{
		super.update(dt);
		
		if(!_dead)
		{
			BossEye.instance().setLoc(origin(), angle());
			doFlames();
		}
	}
	
	private void doFlames()
	{
		for(int n = 0; n < _fixtureCentres.length; n++)
		{				
			if(_shieldBehaviour.energy(n) > 0)
			{
				float e = 0.1f - ((_shieldBehaviour.energy(n) / SHIELD_STRENGTH) * 0.1f);
				if(Game.Dice.nextFloat() < e)
				{
					Z.v2().set(_fixtureCentres[n]).rotate(angle(0)).add(origin());
					
					Z.sim().spawnExlosion(Z.v2(), 1, Color.ORANGE);
				}
			}
		}
	}

	@Override
	public boolean isEyeVulnerable()
	{
		return _cockpitFixture == null;
	}

	@Override
	public void onEyeHit()
	{
		if(!_dead)
		{		
			Z.sim().setController(new DefeatBossController(origin()));
			
			Z.sim().spawnExlosion(origin(), 3, Color.GRAY, 18);
			Z.sim().spawnExlosion(origin(), 4, Color.ORANGE, 9);
			Z.sim().spawnExlosion(origin(), 5, Color.YELLOW, 6);
			Z.sim().spawnExlosion(origin(), 8, Color.WHITE);
			
			//Z.sim.fireEvent(SFX_DIE, null);
			Z.sim.fireEvent(SFX_DIE2, null);
			
			Z.sim().spawnDebris(this, _body.getLinearVelocity());
			
			free();
			
			if(BadgeTerralon.instance().queue())
				BossEye.instance().doNotification();
			BossEye.instance().free();			
		}
	}
	
	@Override
	public boolean isLoop(int poly)
	{
		return _map.shape(poly).type == Shape.TYPE_LOOP;
	}
		
	@Override
	public float clipRadius()
	{
		return 40f;
	}
}
