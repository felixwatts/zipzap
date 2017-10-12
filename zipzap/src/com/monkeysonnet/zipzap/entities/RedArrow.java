package com.monkeysonnet.zipzap.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.zipzap.IRenderablePolygon;
import com.monkeysonnet.zipzap.Tools;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.behaviours.BlobTrailBehaviour;
import com.monkeysonnet.zipzap.behaviours.DieOnRangeBehaviour;

public class RedArrow extends Enemy implements IRenderablePolygon
{
	private BlobTrailBehaviour _blobTrailBehaviour = new BlobTrailBehaviour(Color.WHITE, 0.1f, 5f, Ship.verts[1], 0.5f, 2f);

	private static final RedArrowPool pool = new RedArrowPool();

	private static final float FLYPAST_OFFSET_Y = 4 * Ship.SCALE;
	private static final float FLYPAST_OFFSET_X = 8 * Ship.SCALE;
	private static final float SPEED = Ship.SPEED_NORMAL;

	private static class RedArrowPool extends Pool<RedArrow>
	{
		@Override
		protected RedArrow newObject()
		{
			return new RedArrow();
		}
	}
	
	private RedArrow()
	{
		_behaviours.add(DieOnRangeBehaviour.instance());
		_behaviours.add(_blobTrailBehaviour);
	}
	
	public static void spawnFlypast()
	{
		Vector2 centre = Tools.randomSpawnLoc();
		
		Z.v1().set(centre).sub(Z.sim.focalPoint()).nor().mul(FLYPAST_OFFSET_Y);
		Z.v2().set(centre).sub(Z.sim.focalPoint()).nor().rotate(90).mul(FLYPAST_OFFSET_X);
		
		Vector2 vel = Z.sim.vector().obtain().set(Z.sim.focalPoint()).sub(centre).nor().mul(SPEED);
		Vector2 loc = Z.sim.vector().obtain();
		
		spawn(centre, vel, randomColor());
		
		Color c = randomColor();
		spawn(loc.set(centre).add(Z.v1()).add(Z.v2()), vel, c);
		spawn(loc.set(centre).add(Z.v1()).sub(Z.v2()), vel, c);
		
		c = randomColor();
		spawn(loc.set(centre).add(Z.v1()).add(Z.v1()).add(Z.v2()).add(Z.v2()), vel, c);
		spawn(loc.set(centre).add(Z.v1()).add(Z.v1()).sub(Z.v2()).sub(Z.v2()), vel, c);
		
		Z.sim.vector().free(loc);
		Z.sim.vector().free(vel);
	}
	
	private static  Color randomColor()
	{
		switch(Game.Dice.nextInt(7))
		{
			case 0:
				return Color.WHITE;
			case 1:
				return Color.RED;
			case 2:
				return Color.YELLOW;
			case 3:
				return Color.GREEN;
			case 4:
				return Color.CYAN;
			case 5:
				return Color.BLUE;
			case 6:
			default:
				return Color.MAGENTA;
		}
	}
	
	public static void spawn(Vector2 loc, Vector2 vel, Color color)
	{
		RedArrow r = pool.obtain();
		
		r._body = B2d
				.kinematicBody()
				.at(loc)
				.rotated((float)Math.toRadians(vel.angle()))
				.linearVelocity(vel)
				.create(Z.sim.world());
		
		r._blobTrailBehaviour.setColor(color);
		
		r.onSpawn();
		
		Z.sim.entities().add(r);
	}

	@Override
	public Vector2[] verts()
	{
		return Ship.verts;
	}

	@Override
	public Color color()
	{
		return Color.RED;
	}

	@Override
	public float lineWidth()
	{
		return 1f;
	}

	@Override
	public float clipRadius()
	{
		return 4f;
	}
	
	@Override
	protected void onFree()
	{
		pool.free(this);
	}
}
