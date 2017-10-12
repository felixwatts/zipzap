package com.monkeysonnet.zipzap.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.ICallback;
import com.monkeysonnet.engine.editor.Map;
import com.monkeysonnet.zipzap.Tools;
import com.monkeysonnet.zipzap.behaviours.FaceDirectionOfTravelBehaviour;
import com.monkeysonnet.zipzap.behaviours.PeriodicBehaviour;
import com.monkeysonnet.zipzap.script.IActiveCount;

public class Triangle extends EnemyBasic
{
	private static final float SPEED = 15;
	private static final float ANGLE_VARIANCE = 30f;
	private static final float ANGULAR_VELOCITY = 90f;
	private static final float RELOAD_TIME = 1f;
	protected static final float PROJECTILE_SPEED = 25;
	private static final int KILL_SCORE = 100;
	
	private static final Map map = new Map("triangle.v", 0.75f, 0f);
	
	private static int _activeCount;	
	public static final IActiveCount activeCount = new IActiveCount()
	{
		@Override
		public int activeCount()
		{
			return _activeCount;
		}
	};
	
	private static final TrianglePool pool = new TrianglePool();	

	private static class TrianglePool extends Pool<Triangle>
	{
		@Override
		protected Triangle newObject()
		{
			return new Triangle();
		}
	}

	private Fixture _coreFixture;
	private int _powerUp;
	
	private final ICallback fireCallback = new ICallback()
	{
		@Override
		public void callback(Object arg)
		{
			for(float a = 30; a < 360; a += 120)
				Projectile.spawn(origin(), Vector2.tmp.set(PROJECTILE_SPEED, 0).rotate(a + angle()), 1, color(0), true);
		}
	};
	
	private Triangle()
	{
		_behaviours.add(new PeriodicBehaviour(fireCallback, RELOAD_TIME, null));
		_behaviours.removeValue(FaceDirectionOfTravelBehaviour.instance(), true);
		
		_killScore = KILL_SCORE;
	}
	
	public static void spawn(int powerup)
	{
		Triangle t = pool.obtain();
		t._powerUp = powerup;
		t.setup(map, SPEED, false);
		t._body.setLinearVelocity(Vector2.tmp.set(SPEED, 0).rotate(Tools.angleToShip(t.origin())).rotate((float) (Game.Dice.nextGaussian() * ANGLE_VARIANCE)));
		t._body.setAngularVelocity((float)Math.toRadians(ANGULAR_VELOCITY) * (Game.Dice.nextBoolean() ? 1f : -1f));
		
		for(int n = 0; n < map.numShapes(); n++)
		{
			Fixture f = t.addFixture(map.shape(n));
			if(map.shape(n).label != null && map.shape(n).label.equals("core"))
				t._coreFixture = f;
		}
		
		_activeCount++;
	}
	
	@Override
	public boolean hit(Fixture f, boolean mega, Vector2 loc, Vector2 norm)
	{
		if(!mega && f != _coreFixture)
			return true;
		else return super.hit(f, mega, loc, norm);
	}
	
	@Override
	protected void onFree()
	{
		pool.free(this);
		_activeCount--;
		
		if(_killed)
			PowerUp.spawn(origin(), _powerUp);
	}
	
	@Override
	public Color color(int poly)
	{
		Color c = super.color(poly);
		if(c == Color.RED)
		{
			if(_powerUp != PowerUp.TYPE_NONE)
				c = PowerUp.colorForType(_powerUp);
		}
		return c;
	}
}
