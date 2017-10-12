package com.monkeysonnet.zipzap.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.engine.editor.Map;
import com.monkeysonnet.zipzap.Tools;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.script.IActiveCount;

public class MineLayer extends EnemyBasic
{

	private static final float SPEED = 30f;
	
	private static int _activeCount;

//	private TweenCallback layMineCallback = new TweenCallback()
//	{		
//		@Override
//		public void onEvent(int type, BaseTween<?> source)
//		{
//			RangeMine.spawn(origin());
//		}
//	};
	
	public static final IActiveCount activeCount = new IActiveCount()
	{
		@Override
		public int activeCount()
		{
			return _activeCount;
		}
	};
	
	private static final MineLayerPool pool = new MineLayerPool();
	private static class MineLayerPool extends Pool<MineLayer>
	{
		@Override
		protected MineLayer newObject()
		{
			return new MineLayer();
		}
	}
	
	private static final Map map = new Map("mine-layer.v", 4f, -90);
	private static final Color[] colors = Tools.mapColours(map);

	//private static final float LAY_MINE_PERIOD_MS = 1000;
	
	//private Tween _layMineTween;
	
	public static void spawn()
	{
		MineLayer l = pool.obtain();		
		l.setup(map, SPEED);
		Vector2
			.tmp
			.set(Z.ship().velocity())
			.mul(2f)
			.add(Z.ship().origin())
			.sub(l.origin()).nor().mul(SPEED);
		l._body.setLinearVelocity(Vector2.tmp);
		
		//l._layMineTween = Tween.call(l.layMineCallback).delay(LAY_MINE_PERIOD_MS).repeat(Tween.INFINITY, LAY_MINE_PERIOD_MS).start(Z.sim().tweens());
	}
	
	@Override
	public Color color(int poly)
	{
		return colors[poly];
	}
	
	@Override
	protected void onFree()
	{
//		_layMineTween.kill();
//		_layMineTween = null;
		pool.free(this);
	}	
}
