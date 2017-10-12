package com.monkeysonnet.zipzap.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.editor.Map;
import com.monkeysonnet.engine.editor.Point;
import com.monkeysonnet.engine.editor.Shape;
import com.monkeysonnet.zipzap.IRenderableMultiPolygon;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;
import com.monkeysonnet.zipzap.behaviours.DieOnHitBehaviour;
import com.monkeysonnet.zipzap.behaviours.FaceDirectionOfTravelBehaviour;
import com.monkeysonnet.zipzap.behaviours.HomingBehaviour;
import com.monkeysonnet.zipzap.behaviours.KillOnContactBehaviour;

public class Dumbot extends Enemy implements IRenderableMultiPolygon
{
	private static final Map map = new Map("dumb-bot.v", 5f, 90);
	private static final float SPEED = 40f;
	private static final HomingBehaviour homingBehaviour = new HomingBehaviour(360, 20, false);
	
	private static final DumbotPool pool = new DumbotPool();
	private static class DumbotPool extends Pool<Dumbot>
	{
		@Override
		protected Dumbot newObject()
		{
			return new Dumbot();
		}
	}
	
	private Dumbot()
	{
		_behaviours.add(FaceDirectionOfTravelBehaviour.instance());
		_behaviours.add(homingBehaviour);
		_behaviours.add(KillOnContactBehaviour.alsoDie());
		_behaviours.add(new DieOnHitBehaviour(Color.WHITE, 12, true, Color.ORANGE, 6));
	}
	
	public static void spawn()
	{
		Dumbot d = pool.obtain();
		
		d._body = B2d
				.kinematicBody()
				.at(Z.v1().set(ZipZapSim.SPAWN_DISTANCE, 0).rotate(Game.Dice.nextFloat()*360f).add(Z.ship().origin()))
				.linearVelocity(Z.v1().set(SPEED, 0))
				.create(Z.sim().world());
		
		for(int s = 0; s < map.numShapes(); s++)
		{
			Shape shape = map.shape(s);
			if(shape.type == Shape.TYPE_LOOP)
			{
				B2d.loop(shape.shape).category(ZipZapSim.COL_CAT_METEORITE).mask(ZipZapSim.COL_CAT_SHIP).userData(d._fixtureTag).create(d._body);
			}
		}
		
		d.onSpawn();
		
		Z.sim().entities().add(d);
		
		for(int n = 0; n < map.numPoints(); n++)
		{
			Point p = map.point(n);
			if(p.label != null && p.label.startsWith("gun"))
			{
				new MuthaHardpoint(d, p.point, 0, MuthaHardpoint.TYPE_HEAT_SEEKER);
			}
		}
	}

	@Override
	public int getNumPolys()
	{
		return map.numShapes();
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
		return map.shape(poly).shape;
	}

	@Override
	public Color color(int poly)
	{
		return Color.GRAY;
	}

	@Override
	public float lineWidth(int poly)
	{
		return 1f;
	}
	
	@Override
	protected void onFree()
	{
		pool.free(this);
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
