package com.monkeysonnet.zipzap.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.engine.FixtureTag;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.editor.Map;
import com.monkeysonnet.zipzap.IRenderableMultiPolygon;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;
import com.monkeysonnet.zipzap.behaviours.DieOnHitBehaviour;
import com.monkeysonnet.zipzap.behaviours.FaceDirectionOfTravelBehaviour;
import com.monkeysonnet.zipzap.behaviours.HomingBehaviour;
import com.monkeysonnet.zipzap.behaviours.IBehaviour;
import com.monkeysonnet.zipzap.behaviours.KillOnContactBehaviour;
import com.monkeysonnet.zipzap.behaviours.DieOnRangeBehaviour;
import com.monkeysonnet.zipzap.script.IActiveCount;

public class MiniJelly extends Enemy implements IRenderableMultiPolygon
{
	private static final float SCALE = 2f;
	private static final MiniJellyPool pool = new MiniJellyPool(); 
	private static final Map verts = new Map("mini-jelly.v", SCALE, 0);
	private static final Vector2[] points = new Vector2[6];
	private static final float ATTACK_SPEED = 8;
	private static final float DRIFT_SPEED = 4;
	public static final Color color = new Color(210f/255f, 1f, 1f, 0.7f);
	private static final IBehaviour homingBehaviour = new HomingBehaviour(360, 180, false);
	private static final float GANG_RADIUS = 20f;
	private static final float SHOAL_RADIUS = 12;
	private static final int SFX_EXPLODE = -1016;
		
	private static int _activeCount = 0;
	
	public static final IActiveCount activeCount = new IActiveCount()
	{
		@Override
		public int activeCount()
		{
			return _activeCount;
		}
	};	
	
	private static class MiniJellyPool extends Pool<MiniJelly>
	{
		@Override
		protected MiniJelly newObject()
		{
			return new MiniJelly();
		}
	}
	
	public void attack()
	{
		if(!_behaviours.contains(homingBehaviour, true))
		{
			_body.setLinearVelocity(Z.v1().set(_body.getLinearVelocity()).nor().mul(ATTACK_SPEED));
			_behaviours.add(homingBehaviour);
		}
	}
	
	static
	{
		Map m = new Map("mini-jelly.v");
		points[0] = m.point("p1a").point.mul(SCALE);
		points[1] = m.point("p2a").point.mul(SCALE);
		points[2] = m.point("p3a").point.mul(SCALE);
		points[3] = m.point("p1b").point.mul(SCALE);
		points[4] = m.point("p2b").point.mul(SCALE);
		points[5] = m.point("p3b").point.mul(SCALE);
	}
	
	private final FixtureTag _fixtureTag = new FixtureTag(this, this);
	
	private final JellyfishTentacle[] _tentacles = new JellyfishTentacle[3];
	private Color _color;
	

	public static void spawnShoal(int num)
	{
		Color color = Color.MAGENTA;
		switch(Game.Dice.nextInt(3))
		{
			case 0:
				color = Color.CYAN;
				break;
			case 1:
				color = Color.MAGENTA;
				break;
			case 2:
				color = Color.YELLOW;
				break;
		}
		
		Vector2 o = Vector2.tmp.set(ZipZapSim.SPAWN_DISTANCE + SHOAL_RADIUS, 0).rotate(Game.Dice.nextFloat() * 360).add(Z.ship().origin());
		float x = o.x; 
		float y = o.y;
		float angle = Vector2.tmp.set(Z.ship().origin()).sub(x, y).angle();
		
		for(int n = 0; n < num; n++)
		{
			float dx = ((Game.Dice.nextFloat() * 2f) - 1f) * SHOAL_RADIUS;
			float dy = ((Game.Dice.nextFloat() * 2f) - 1f) * SHOAL_RADIUS;
			
			spawn(x + dx, y + dy, angle, color);
		}
	}
	
	public static MiniJelly spawn(float x, float y, float angle)
	{
		return spawn(x, y, angle, Color.MAGENTA);
	}
	
	public static MiniJelly spawn(float x, float y, float angle, Color color)
	{
		MiniJelly j = pool.obtain();
		
		j._behaviours.removeValue(homingBehaviour, true);
		
		j._color = color;
		
		j._killScore = 5;
		
		j._body = B2d
				.kinematicBody()
				.at(x, y)
				.rotated((float)Math.toRadians(angle))
				.linearVelocity(Z.v1().set(DRIFT_SPEED, 0).rotate(angle))
				.withFixture(B2d
						.loop(verts.shape(0).shape)
						.category(ZipZapSim.COL_CAT_METEORITE)
						.mask(ZipZapSim.COL_CAT_SHIP)
						.userData(j._fixtureTag))
				.create(Z.sim().world());
		
		for(int n = 0; n < 3; n++)
		{
			j._tentacles[n] = JellyfishTentacle.spawn(j._body, points[n], points[n+3], 3, color);
		}
		
		j.onSpawn();
		
		_activeCount++;
		
		Z.sim().entities().add(j);
		
		return j;
	}
	
	public static MiniJelly spawn(float angle)
	{
		Z.v1().set(ZipZapSim.SPAWN_DISTANCE, 0).rotate((float)(angle + 180 + (Game.Dice.nextGaussian() * 90))).add(Z.ship().origin());
		return spawn(Z.v1().x, Z.v1().y, angle);		
	}
	
	private MiniJelly()
	{
		_behaviours.add(new DieOnHitBehaviour(Color.MAGENTA, 12, false, null, 1));
		_behaviours.add(FaceDirectionOfTravelBehaviour.instance());
		_behaviours.add(KillOnContactBehaviour.alsoDie());
		_behaviours.add(DieOnRangeBehaviour.instance());
	}
	
	@Override
	public void onFree()
	{	
		Z.sim().spawnCloud(origin(), 3, _color, 6f);
		
		for(int n = 0; n < 3; n++)
		{
			_tentacles[n].free();
			_tentacles[n] = null;
		}
		_activeCount--;
		
		pool.free(this);
	}

	@Override
	public int getNumPolys()
	{
		return verts.numShapes();
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
		return verts.shape(poly).shape;
	}

	@Override
	public Color color(int poly)
	{
		return color;
	}

	@Override
	public float lineWidth(int poly)
	{
		return 1f;
	}
	
	@Override
	public boolean hit(Fixture f, boolean mega, Vector2 loc, Vector2 norm)
	{
		Z.sim().world().QueryAABB(
				attackCallback, 
				_body.getWorldCenter().x - GANG_RADIUS,
				_body.getWorldCenter().y - GANG_RADIUS, 
				_body.getWorldCenter().x + GANG_RADIUS, 
				_body.getWorldCenter().y + GANG_RADIUS);
		
		
		return super.hit(f, mega, loc, norm);

//		MiniJelly.spawn(Game.Dice.nextFloat() * 360f);
//		MiniJelly.spawn(Game.Dice.nextFloat() * 360f);
	}
	
	private static final QueryCallback attackCallback = new QueryCallback()
	{
		public boolean reportFixture(Fixture fixture) 
		{
			FixtureTag tag = (FixtureTag)fixture.getUserData();
			if(tag != null)
			{
				if(tag.owner instanceof MiniJelly)
				{
					((MiniJelly)tag.owner).attack();
				}
			}
			return true;
		}
	};	
	
	@Override
	public boolean isLoop(int poly)
	{
		return true;// verts.shape(poly).type == Shape.TYPE_LOOP;
	}
		
	@Override
	public float clipRadius()
	{
		return 4f;
	}
	
	@Override
	public void onKill()
	{
		Z.sim.fireEvent(SFX_EXPLODE, null);
	}
}
