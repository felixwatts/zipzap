package com.monkeysonnet.zipzap.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.editor.Map;
import com.monkeysonnet.zipzap.IRenderablePolygon;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;
import com.monkeysonnet.zipzap.behaviours.DieOnHitBehaviour;
import com.monkeysonnet.zipzap.behaviours.FlyingSoundBehaviour;
import com.monkeysonnet.zipzap.behaviours.HomingBehaviour;
import com.monkeysonnet.zipzap.behaviours.KillOnContactBehaviour;
import com.monkeysonnet.zipzap.script.IActiveCount;

public class SpinningSeeker extends Enemy implements IRenderablePolygon
{
	private static final SpinningSeekerPool pool = new SpinningSeekerPool();
	private static Vector2[] vertsArr;

	private static final float ANGULAR_VELOCITY = 3;
	private static final float SPEED = 10;
	private static final int SFX_FLY = 28;
	private static int _activeCount;
	
	private static final HomingBehaviour homingBehaviour = new HomingBehaviour(360, 360, false);
	
	static
	{
		Map m = new Map("spinning-seeker.v", 2, 0);
		vertsArr = m.shape("p1").shape;
	}
	
	public static final IActiveCount activeCount = new IActiveCount()
	{
		@Override
		public int activeCount()
		{
			return _activeCount;
		}
	};
		
	private static class SpinningSeekerPool extends Pool<SpinningSeeker>
	{
		@Override
		protected SpinningSeeker newObject()
		{
			return new SpinningSeeker();
		}
	}
	
	private SpinningSeeker()
	{
		_behaviours.add(DieOnHitBehaviour.basic());
		_behaviours.add(homingBehaviour);
		_behaviours.add(KillOnContactBehaviour.alsoDie());
		_behaviours.add(new FlyingSoundBehaviour(SFX_FLY, 1.5f, 20f));
		
		_killScore = 50;
	}
	
	public static SpinningSeeker spawn(float angle)
	{		
		if(angle == 0)
			angle = Game.Dice.nextFloat() * 360f;
		
		SpinningSeeker s = pool.obtain();
		
		Game.workingVector2a.set(ZipZapSim.SPAWN_DISTANCE, 0).rotate(angle).add(Z.ship().origin());
		
		s._body = B2d
				.kinematicBody()
				.angularVelocity(ANGULAR_VELOCITY)
				.linearVelocity(Vector2.tmp.set(SPEED, 0))
				.at(Game.workingVector2a.x, Game.workingVector2a.y)
				.withFixture(B2d
						.loop(vertsArr)
						.category(ZipZapSim.COL_CAT_METEORITE)
						.mask(ZipZapSim.COL_CAT_SHIP | ZipZapSim.COL_CAT_BUBBLE)
						.userData(s._fixtureTag))
				.create(Z.sim().world());
		
		s.onSpawn();
		
		Z.sim().entities().add(s);
		
		_activeCount++;
		
		return s;
	}

	@Override
	public float angle()
	{
		return (float)Math.toDegrees(_body.getAngle());
	}

	@Override
	public Vector2 origin()
	{
		return _body.getPosition();
	}

	@Override
	public Vector2[] verts()
	{
		return vertsArr;
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

//	@Override
//	public void update(float dt)
//	{
//		super.update(dt);
//		if(!_dead)
//		{
//			
//		}
//		
////		if(_dead)
////			free();
////		else 
////		{
////			if(!ZipZapScreen.instance().sim().ship().dead())
////			{
////				Game.workingVector2a.set(ZipZapScreen.instance().sim().ship().origin()).sub(origin()).nor().mul(SPEED);
////				_body.setLinearVelocity(Game.workingVector2a);
////			}				
////			
////			if(_justHit > 0)
////				_justHit -= dt;
////		}			
//	}

	@Override
	public void onFree()
	{
		pool.free(this);
		_activeCount--;
	}
	
	@Override
	public float clipRadius()
	{
		return 4f;
	}
	
	@Override
	public void onKill()
	{
		Z.sim.fireEvent(ZipZapSim.EV_EXPLOSION_MEDIUM, false);
	}
}
