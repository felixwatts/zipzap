package com.monkeysonnet.zipzap.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.editor.Map;
import com.monkeysonnet.engine.editor.Shape;
import com.monkeysonnet.zipzap.IRenderableMultiPolygon;
import com.monkeysonnet.zipzap.Tools;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;
import com.monkeysonnet.zipzap.behaviours.DieOnHitBehaviour;
import com.monkeysonnet.zipzap.behaviours.DieOnRangeBehaviour;
import com.monkeysonnet.zipzap.behaviours.KillOnContactBehaviour;
import com.monkeysonnet.zipzap.script.IActiveCount;

public class Bug extends Enemy implements IRenderableMultiPolygon
{
	private static final float EXTEND_GIRDER_TIME = 0.5f/3f;
	private static final float SPEED = 32f;
	
	private static final BugPool pool = new BugPool();
	private static class BugPool extends Pool<Bug>
	{
		@Override
		protected Bug newObject()
		{
			return new Bug();
		}
	}
	
	private static Color[] _colors;
	private static Vector2[][] _verts;
	private static final Map map = new Map("bug.v", 1f, 90, true);
	private static final float TURN_CHANCE = 0.01f;	
	
	private float _spawnGirderTime;
	private Girder _girder;
	
	private static int _activeCount;
	
	public static final IActiveCount activeCount = new IActiveCount()
	{
		@Override
		public int activeCount()
		{
			return _activeCount;
		}
	};

	static
	{
		_colors = new Color[map.numShapes()-1];
		_verts = new Vector2[map.numShapes()-1][];
		
		int x = 0;
		for(int n = 0; n < map.numShapes(); n++)
		{
			Shape s = map.shape(n);
			if(s.label == null)
			{
				_verts[x] = s.shape;
				
				if(s.properties.equals("red"))
				{
					_colors[x] = Color.RED;
				}
				else if(s.properties.equals("gray"))
				{
					_colors[x] = Color.RED;
				}
				else if(s.properties.equals("cyan"))
				{
					_colors[x] = Color.CYAN;
				}
				
				x++;
			}
		}
	}
	
	public static Bug spawn()
	{
		Bug j = pool.obtain();
		
		j._body = B2d
				.kinematicBody()
				.at(Tools.randomSpawnLoc())
				.linearVelocity(Z.v1().set(SPEED, 0))
				.withFixture(B2d
						.polygon(map.shape("bb").shape)
						.sensor(true)
						.category(ZipZapSim.COL_CAT_METEORITE)
						.mask(ZipZapSim.COL_CAT_SHIP)
						.userData(j._fixtureTag))
				.create(Z.sim().world());
		
		j.onSpawn();
		Z.sim().entities().add(j);
		
		j._girder = Girder.spawn(j.origin(), true);
		
		_activeCount++;
		
		return j;
	}
	
	private Bug()
	{
		_behaviours.add(DieOnHitBehaviour.basic());
		_behaviours.add(KillOnContactBehaviour.alsoDie());
		_behaviours.add(DieOnRangeBehaviour.instance());
		
		_killScore = 100;
	}
	
	@Override
	public void onFree()
	{	
		_girder = null;
		Z.sim().tweens().killTarget(_body);
		pool.free(this);
		_activeCount--;
	}

	@Override
	public int getNumPolys()
	{
		return _colors.length;
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
		return _verts[poly];
	}

	@Override
	public Color color(int poly)
	{
		return _colors[poly];
	}

	@Override
	public float lineWidth(int poly)
	{
		return 1f;
	}
	
	@Override
	public void update(float dt)
	{
		super.update(dt);
		
		if(!_dead)
		{
			if(Game.Dice.nextFloat() < TURN_CHANCE)
			{
				boolean towards = true;//Game.Dice.nextFloat() < SNAKEY_TURN_TOWARDS_CHANCE;
				
				float angleToShip = Z.v1().set(Z.ship().origin()).sub(origin()).angle();
				float angleOfTravel =  _body.getLinearVelocity().angle();
				float angleToShipRel = angleToShip - angleOfTravel;
				if(angleToShipRel > 180)
					angleToShipRel -= 360;
				if(angleToShip < -180)
					angleToShipRel += 360;
				
				boolean clockwise = towards == (angleToShipRel < 0);
	
				float newAngle = angleOfTravel + (clockwise ? -90 : 90);
				
				_body.setLinearVelocity(Z.v1().set(SPEED, 0).rotate(newAngle));
				_body.setTransform(origin(), (float)Math.toRadians(newAngle));
				
				if(_girder != null)
					_girder.extend(origin());
				
				_girder = Girder.spawn(origin(), newAngle == 0 || newAngle == 180);
				_spawnGirderTime = EXTEND_GIRDER_TIME;
			}
			
			_spawnGirderTime -= dt;
			if(_spawnGirderTime < 0)
			{
				extendGirder();
				_spawnGirderTime = EXTEND_GIRDER_TIME;
			}
		}
	}

	private void extendGirder()
	{
		Z.sim().spawnExlosion(origin(), 2, Color.YELLOW);
		Z.sim().spawnExlosion(origin(), 1, Color.WHITE);
		_girder.extend(origin());
	}
	
	@Override
	public boolean isLoop(int poly)
	{
		return map.shape(poly).type == Shape.TYPE_LOOP;
	}
	
	@Override
	public float clipRadius()
	{
		return 4f;
	}
}
