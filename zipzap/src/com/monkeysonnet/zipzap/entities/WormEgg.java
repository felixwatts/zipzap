package com.monkeysonnet.zipzap.entities;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.equations.Quad;
import aurelienribon.tweenengine.primitives.MutableFloat;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.editor.Map;
import com.monkeysonnet.engine.editor.Shape;
import com.monkeysonnet.zipzap.IRenderableMultiPolygon;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;
import com.monkeysonnet.zipzap.behaviours.DieOnHitBehaviour;
import com.monkeysonnet.zipzap.behaviours.ExplosionOnHitBehaviour;
import com.monkeysonnet.zipzap.behaviours.KillOnContactBehaviour;

public class WormEgg extends Enemy implements IRenderableMultiPolygon
{
	private static final WormEggPool pool = new WormEggPool();
	private static class WormEggPool extends Pool<WormEgg>
	{
		@Override
		protected WormEgg newObject()
		{
			return new WormEgg();
		}	
	}
	
	private static final Map map = new Map("worm-egg.v", 2f, 0);
	private static final float RADIUS = 3;
	private static final Color colorShell = new Color(1, 1, 1, 0.6f);
	private static final Color colorLarva = Color.GREEN;
	private MutableFloat _larvaLineWidth = new MutableFloat(0);
	private Tween _larvaLineWidthTween;
	private Vector2 _centre;
	
	private WormEgg()
	{
		_behaviours.add(KillOnContactBehaviour.basic());
		_behaviours.add(new DieOnHitBehaviour(Color.WHITE, 8, false, null, 0));
		_behaviours.add(ExplosionOnHitBehaviour.green());
	}
	
	public static WormEgg spawn(Vector2 loc)
	{
		WormEgg e = pool.obtain();
		
		e._body = B2d
				.staticBody()
				.at(loc)
				.withFixture(B2d
						.circle()
						.at(map.point("centre").point)
						.radius(RADIUS)
						.category(ZipZapSim.COL_CAT_METEORITE)
						.mask(ZipZapSim.COL_CAT_SHIP)
						.userData(e._fixtureTag))
				.create(Z.sim().world());
		
		e._centre = Z.sim().vector().obtain().set(loc).add(map.point("centre").point);
		
		e.onSpawn();
		
		Z.sim().entities().add(e);
		
		e._larvaLineWidth.setValue(RADIUS/2f);
		e._larvaLineWidthTween = Tween
				.to(e._larvaLineWidth, 0, 500f + (Game.Dice.nextFloat() * 1000f))
				.target(RADIUS * 4f/5f)
				.ease(Quad.INOUT)
				.repeatYoyo(Tween.INFINITY, 0)
				.start(Z.sim().tweens());
		
		return e;
	}
	
	@Override
	protected void onFree()
	{
		if(_larvaLineWidthTween != null)
		{
			_larvaLineWidthTween.kill();
			_larvaLineWidthTween = null;
		}
		
		Z.sim().spawnCloud(_centre, 1, Color.WHITE, RADIUS * 2);
		Z.sim().spawnExlosion(_centre, 4, Color.WHITE, 4f);
		Z.sim().spawnExlosion(_centre, 8, Color.GREEN);
		
		WormSegment.spawnBabytub(_centre);
		
		Z.sim().vector().free(_centre);
		_centre = null;
		
		pool.free(this);
	}

	@Override
	public int getNumPolys()
	{
		return 3;
	}

	@Override
	public float angle(int poly)
	{
		return 0;
	}

	@Override
	public Vector2 origin(int poly)
	{
		switch(poly)
		{
			case 0:
				return _body.getPosition();
			default:
				return _centre;
		}
	}

	@Override
	public Vector2[] verts(int poly)
	{
		switch(poly)
		{
			case 0:
				return map.shape(0).shape;
			default:
				return null;
		}
	}

	@Override
	public Color color(int poly)
	{
		switch(poly)
		{
			case 0:
			case 2:
				return colorShell;
			default:
				return colorLarva;
		}
	}

	@Override
	public float lineWidth(int poly)
	{
		switch(poly)
		{
			case 0:
			default:
				return 1f;
			case 1:
				return _larvaLineWidth.floatValue();
			case 2:
				return RADIUS * 1.5f;
		}
	}
	
	@Override
	public boolean hit(Fixture f, boolean mega, Vector2 loc, Vector2 norm)
	{
		super.hit(f, mega, loc, norm);
		
		if(!_dead)
			Z.screen.sim().score(_centre, 10, false);
		
		return false;
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
