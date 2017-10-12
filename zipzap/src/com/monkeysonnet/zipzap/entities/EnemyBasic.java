package com.monkeysonnet.zipzap.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.engine.editor.Map;
import com.monkeysonnet.engine.editor.Shape;
import com.monkeysonnet.zipzap.IRenderableMultiPolygon;
import com.monkeysonnet.zipzap.Tools;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;
import com.monkeysonnet.zipzap.behaviours.DieOnHitBehaviour;
import com.monkeysonnet.zipzap.behaviours.DieOnRangeBehaviour;
import com.monkeysonnet.zipzap.behaviours.ExplosionOnHitBehaviour;
import com.monkeysonnet.zipzap.behaviours.FaceDirectionOfTravelBehaviour;
import com.monkeysonnet.zipzap.behaviours.KillOnContactBehaviour;

public class EnemyBasic extends Enemy implements IRenderableMultiPolygon
{
	protected Map _map;
	private Color[] _colors;

	protected EnemyBasic()
	{
		_behaviours.add(FaceDirectionOfTravelBehaviour.instance());
		_behaviours.add(KillOnContactBehaviour.alsoDie());
		_behaviours.add(ExplosionOnHitBehaviour.white());
		_behaviours.add(DieOnHitBehaviour.basic());
		_behaviours.add(DieOnRangeBehaviour.instance());				
	}
	
	protected void setup(Map map, float speed)
	{
		setup(map, speed, true);
	}
	
	protected void setup(Map map, float speed, boolean createFixtures)
	{
		_map = map;
		
		_body = B2d.kinematicBody()
				.at(Tools.randomSpawnLoc())
				.create(Z.sim().world());	
		_body.setLinearVelocity(Z.v1().set(speed, 0).rotate(Tools.angleToShip(_body.getPosition())));
		
		if(createFixtures)
		{
			for(int n = 0; n < _map.numShapes(); n++)
			{
				Shape s = _map.shape(n);
				addFixture(s);
			}
		}
		
		Z.sim().entities().add(this);
		
		_colors = Tools.mapColours(_map); // todo array allocation
		
		onSpawn();
	}
	
	protected Fixture addFixture(Shape s)
	{
		if(s.type == Shape.TYPE_LOOP)
		{
			return B2d
				.loop(s.shape)
				.sensor(true)
				.category(ZipZapSim.COL_CAT_METEORITE)
				.mask(ZipZapSim.COL_CAT_SHIP)
				.userData(_fixtureTag)
				.create(_body);			
		}
		else return null;
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
		return _body.getPosition();
	}

	@Override
	public Vector2[] verts(int poly)
	{
		return _map.shape(poly).shape;
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
	public int layer()
	{
		return 1;
	}
	
	@Override
	public boolean isLoop(int poly)
	{
		return _map.shape(poly).type == Shape.TYPE_LOOP;
	}
	
	@Override
	public float clipRadius()
	{
		return 4f;
	}
}
