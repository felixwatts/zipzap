package com.monkeysonnet.zipzap.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.engine.editor.Map;
import com.monkeysonnet.zipzap.Tools;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.behaviours.DieOnHitBehaviour;
import com.monkeysonnet.zipzap.behaviours.GhostTrailBehaviour;
import com.monkeysonnet.zipzap.script.IActiveCount;

public class Head extends EnemyBasic
{
	private static final float SPEED = 30f;
	
	private static final Color trailColor = new Color(1f, 0f, 1f, 0.5f);
	
	private static int _activeCount;
	
	public static final IActiveCount activeCount = new IActiveCount()
	{
		@Override
		public int activeCount()
		{
			return _activeCount;
		}
	};
	
	private static final HeadPool pool = new HeadPool();
	private static class HeadPool extends Pool<Head>
	{
		@Override
		protected Head newObject()
		{
			return new Head();
		}
	}
	
	private static final Map map = new Map("head.v", 4f, 0);
	private static final Color[] colors = Tools.mapColours(map);
	
	private Head()
	{
		_behaviours.add(new GhostTrailBehaviour(this, 0.15f, 2f, trailColor, trailColor, 0.5f));
		_behaviours.add(new DieOnHitBehaviour(Color.MAGENTA, 6, false, null, 3));
		_killScore = 500;
	}

	public static void spawn()
	{
		Head l = pool.obtain();		
		l.setup(map, SPEED);
		l._behaviours.removeValue(DieOnHitBehaviour.basic(), true);
		Vector2
			.tmp
			.set(Z.ship().velocity())
			.mul(2f)
			.add(Z.ship().origin())
			.sub(l.origin()).nor().mul(SPEED);
		l._body.setLinearVelocity(Vector2.tmp);
		_activeCount++;
	}
	
	@Override
	public Color color(int poly)
	{
		return colors[poly];
	}
	
	@Override
	protected void onFree()
	{
		Z.sim().spawnCloud(origin(), 6, Color.MAGENTA, 10f);
		_activeCount--;
		pool.free(this);
	}	
	
	@Override
	public boolean hit(Fixture f, boolean mega, Vector2 loc, Vector2 norm)
	{
		super.hit(f, mega, loc, norm);
		
		if(!_dead)
		{
			Z.sim().spawnCloud(origin(), 4, Color.MAGENTA, 4f);
			_body.setLinearVelocity(Vector2.tmp.set(Z.ship().velocity()).nor().mul(SPEED));
		}
		
		return false;
	}
}
