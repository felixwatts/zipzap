package com.monkeysonnet.zipzap.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.zipzap.IRenderablePolygon;
import com.monkeysonnet.zipzap.Z;

public class Speck implements IEntity, IRenderablePolygon
{	
	protected static final float MAX_DST_TO_SHIP_2 = 3025;// 2500;
	protected static final float SPAWN_DST = 55;// 50;
	
	protected final Vector2 _loc = new Vector2();
	protected float _velMult;
	private final Color _color = new Color();
	private float _lineWidth;
	
//	public Speck init(Vector2 a1, Vector2 a2, float offset)
//	{
//		_loc.set(a1).mul((float)Game.Dice.nextGaussian() * (ZipZapSim.SPAWN_DISTANCE / 2f)).add(offset, 0);
//		Vector2.tmp.set(a2).mul((float)Game.Dice.nextGaussian() * (ZipZapSim.SPAWN_DISTANCE / 30f)).add(offset, 0);
//		_loc.add(Vector2.tmp);
//		
//		
//		float b = Game.Dice.nextFloat() / 4f;
//		_color.set(1, 1, 1, b);
//		_velMult = -1f;
//		
//		return this;
//	}
	
	public Speck init(float minVelMult, float maxVelMult)
	{
		_loc.set(0, Game.Dice.nextFloat() * SPAWN_DST).rotate(Game.Dice.nextFloat() * 360).add(Z.sim.focalPoint());
		float m = Game.Dice.nextFloat();
		_velMult = (m * (maxVelMult - minVelMult)) + minVelMult;
		_color.set(1f, 1f, 116f/255f, m);
		_lineWidth = -(m/6f);// *2f;
		return this;
	}

	@Override
	public void update(float dt)
	{
		if(Z.sim.focalPointVel().x == 0 && Z.sim.focalPointVel().y == 0)
			return;
		
		_loc.add(Game.
				workingVector2a
				.set(Z.sim.focalPointVel())
				.mul(-_velMult).mul(dt));
		
		if(_loc.dst2(Z.sim().focalPoint()) > MAX_DST_TO_SHIP_2)
		{
			Game
				.workingVector2a
				.set(Z.sim.focalPointVel())
				.nor()
				.mul(SPAWN_DST)
				.rotate((Game.Dice.nextFloat() * 180) - 90)
				.add(Z.sim().focalPoint());
			_loc.set(Game.workingVector2a);
		}
	}

	@Override
	public void free()
	{
		Z.sim().entities().removeValue(this, true);
	}

	@Override
	public float angle()
	{
		return 0;
	}

	@Override
	public Vector2 origin()
	{
		return _loc;
	}

	@Override
	public Vector2[] verts()
	{
		return null;
	}

	@Override
	public Color color()
	{
		return _color;
	}

	@Override
	public int layer()
	{
		return 0;
	}

	@Override
	public float lineWidth()
	{
		return _lineWidth;
	}
		
	@Override
	public float clipRadius()
	{
		return 1f;
	}
}
