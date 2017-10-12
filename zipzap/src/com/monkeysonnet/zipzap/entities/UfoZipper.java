package com.monkeysonnet.zipzap.entities;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.equations.Quad;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.engine.BodyTweener;
import com.monkeysonnet.engine.FixtureTag;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.zipzap.IRenderableMultiPolygon;
import com.monkeysonnet.zipzap.Tools;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;
import com.monkeysonnet.zipzap.behaviours.DieOnHitBehaviour;
import com.monkeysonnet.zipzap.behaviours.DieOnRangeBehaviour;
import com.monkeysonnet.zipzap.behaviours.KillOnContactBehaviour;
import com.monkeysonnet.zipzap.script.IActiveCount;

public class UfoZipper extends Enemy implements IRenderableMultiPolygon
{
	private static final UfoZipperPool pool = new UfoZipperPool(); 
	private static final Vector2[][] verts = new Vector2[2][];
	private static final float RADIUS = 2;
	private static final float RELOAD_TIME_MS = 300f;
	private static final float PROJECTILE_SPEED = 25;
	private static final int PROJECTILE_LENGTH = 2;
	private static final float ZIP_TIME_MS = 200;
	private static final int MIN_DST_TO_SHIP = 25;
	private static final float DST_TO_SHIP_RANGE = 10;
	private static final float ORBIT_RANGE = 90;
	private static final int KILL_SCORE = 25;
	private static final float PRELOAD_TIME_MS = 750;	
	private static final int SFX_ZIP = -1029;
	
	private static class UfoZipperPool extends Pool<UfoZipper>
	{
		@Override
		protected UfoZipper newObject()
		{
			return new UfoZipper();
		}
	}

	static
	{
		verts[0] = new Vector2[10];
		verts[1] = new Vector2[10];
		for(int n = 0; n < 10; n++)
		{
			verts[0][n] = new Vector2(RADIUS, 0).rotate(n * (360f/10f));
			verts[1][n] = new Vector2(RADIUS/2f, 0).rotate(n * (360f/10f));
		}
	}
	
	public static int _activeCount;	
	public static final IActiveCount activeCount = new IActiveCount()
	{
		
		@Override
		public int activeCount()
		{
			return _activeCount;
		}
	};
	
	private final FixtureTag _fixtureTag = new FixtureTag(this, this);
	
	public static UfoZipper spawn()
	{
		UfoZipper j = pool.obtain();
		
		j._body = B2d
				.kinematicBody()
				.at(Tools.randomSpawnLoc())
				.withFixture(B2d
						.circle()
						.sensor(true)
						.radius(RADIUS)
						.category(ZipZapSim.COL_CAT_METEORITE)
						.mask(ZipZapSim.COL_CAT_SHIP)
						.userData(j._fixtureTag))
				.create(Z.sim().world());
		
		j.onSpawn();
		Z.sim().entities().add(j);
		
		j.beginZip();
		
		_activeCount++;
		
		return j;
	}
	
	private UfoZipper()
	{
		_behaviours.add(new DieOnHitBehaviour(Color.YELLOW, 12, true, Color.YELLOW, 1));
		_behaviours.add(KillOnContactBehaviour.alsoDie());
		_behaviours.add(DieOnRangeBehaviour.instance());
		
		_killScore = KILL_SCORE;
	}
	
	@Override
	public void onFree()
	{	
		Z.sim().tweens().killTarget(_body);
		pool.free(this);
		_activeCount--;
	}

	@Override
	public int getNumPolys()
	{
		return 2;
	}

	@Override
	public float angle(int poly)
	{
		return 0;
	}

	@Override
	public Vector2 origin(int poly)
	{
		return _body.getWorldCenter();
	}

	@Override
	public Vector2[] verts(int poly)
	{
		return verts[poly];
	}

	@Override
	public Color color(int poly)
	{
		return Color.CYAN;
	}

	@Override
	public float lineWidth(int poly)
	{
		return 1f;
	}

	private void fire() 
	{
		if(Z.ship().ghostMode())
			return;
			
		Projectile.spawn(
				origin(), 
				Z.v1().set(Z.ship().origin()).sub(origin()).nor().mul(PROJECTILE_SPEED), 
				PROJECTILE_LENGTH, 
				Color.RED, 
				true);
	}
	
	private void beginZip()
	{
		Z.sim().tweens().killTarget(_body);
		
		Z.v1()
			.set(origin())
			.sub(Z.ship().origin())
			.rotate((Game.Dice.nextFloat() - 0.5f) * ORBIT_RANGE)
			.nor()
			.mul(MIN_DST_TO_SHIP + (Game.Dice.nextFloat() * DST_TO_SHIP_RANGE))
			.add(Z.ship().origin());
		
		Timeline
			.createSequence()
			.push(Tween
				.to(_body, BodyTweener.VAL_POS_XY, ZIP_TIME_MS)
				.target(Z.v1().x, Z.v1().y)
				.ease(Quad.INOUT))
			.pushPause(PRELOAD_TIME_MS)
			.push(Tween.call(fireCallback))
			.pushPause(RELOAD_TIME_MS)
			.push(Tween.call(fireCallback))
			.pushPause(RELOAD_TIME_MS)
//			.push(Tween.call(fireCallback))
//			.pushPause(RELOAD_TIME_MS)
			.setCallbackTriggers(TweenCallback.COMPLETE)
			.setCallback(zipCompleteCallback)
			.start(Z.sim().tweens());
		
		Z.sim.fireEvent(SFX_ZIP, null);
		
	}
	
	private final TweenCallback zipCompleteCallback = new TweenCallback() 
	{		
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			if(type == TweenCallback.COMPLETE)
			{
				beginZip();
			}
		}
	};
	
	private final TweenCallback fireCallback = new TweenCallback() 
	{
		@Override
		public void onEvent(int type, BaseTween<?> source) 
		{
			fire();
		}
	};
	
	@Override
	public boolean isLoop(int poly)
	{
		return true; // todo
	}
		
	@Override
	public float clipRadius()
	{
		return 4f;
	}
	
	@Override
	public void onKill()
	{
		super.onKill();
		Z.renderer().shakeCamera(1, 0.5f);
	}
}
