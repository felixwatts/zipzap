package com.monkeysonnet.zipzap.entities;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.engine.IContactHandler;
import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.engine.editor.Map;
import com.monkeysonnet.zipzap.IBossBody;
import com.monkeysonnet.zipzap.IHitable;
import com.monkeysonnet.zipzap.IRenderableMultiPolygon;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;
import com.monkeysonnet.zipzap.achievements.BadgeKobolon;
import com.monkeysonnet.zipzap.script.DefeatBossController;

public class Kobo implements 
	IRenderableMultiPolygon, 
	IEntity, 
	IContactHandler,
	IHitable,
	IBossBody
{
	private static final KoboPool pool = new KoboPool();
	private static final Vector2[][] vertsArr = new Vector2[24][];
	private static final float CELL_SIZE = 4;
	private static final int SFX_BLOW_EYE = -1003;
	private static final int SFX_DEFLECT = -1023;

	private static class KoboPool extends Pool<Kobo>
	{
		@Override
		protected Kobo newObject()
		{
			return new Kobo();
		}
	}
	
	static
	{
		for(int n = 0; n < 24; n++)
		{
			if(n == 8)
			{
				vertsArr[n] = new Vector2[0];
			}
			else
			{
				Map m = new Map("kobo-tile-" + n + ".v");
				Array<Vector2> vs = new Array<Vector2>(m.shape(0).shape);
				
				vertsArr[n] = new Vector2[vs.size];
				for(int o = 0; o < vs.size; o++)
					vertsArr[n][o] = new Vector2(vs.get(o));
			}
		}
	}
	
	private int[][] _cells;
	private Fixture[][] _cellFixtures;
	private final Vector2[] _origins = new Vector2[KoboMap.MAP_SIZEX * KoboMap.MAP_SIZEY]; 
	private Body _body;
	private final KoboMap _map = new KoboMap();
	//private int _numCellsAlive;
	
	private final TweenCallback _callbackBlowTile = new TweenCallback()
	{				
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			if(_dead)
				return;
			
			Vector2 v = (Vector2)source.getUserData();
			
			boolean reverse = false;
			if(v.x < 0)
			{
				reverse = true;
				v.x = -v.x;
				v.x -= 1;
			}
			
			tryBlowTile((int)v.x-1, (int)v.y, reverse, false);
			tryBlowTile((int)v.x+1, (int)v.y, reverse, false);
			tryBlowTile((int)v.x, (int)v.y-1, reverse, false);
			tryBlowTile((int)v.x, (int)v.y+1, reverse, false);
			
			Z.sim().vector().free(v);
		}
	};
	private boolean _dead;

	private Kobo()
	{
		_cellFixtures = new Fixture[KoboMap.MAP_SIZEX][];
		for(int n = 0; n < KoboMap.MAP_SIZEX; n++)
			_cellFixtures[n] = new Fixture[KoboMap.MAP_SIZEY];
	}
	
	public static void spawn(float cx, float cy)
	{
		Kobo k = pool.obtain();
		
		k._dead = false;
		k._body = B2d
				.staticBody()
				.at(cx, cy)
				.fixedRotation(true)
				.create(Z.sim().world());
		
		k._map.make_maze(8, 8, 8, 8);
		k._cells = k._map.convert(0);	
		
		//k._numCellsAlive = 0;
		
		for(int x = 0; x < k._cells.length; x++)
			for(int y = 0; y < k._cells[0].length; y++)
			{		
				int cellVal = k._cells[x][y];
				
				if((cellVal & KoboMap.SPACE) != 0)
					continue;
				
				//k._numCellsAlive++;
				
//				int tile = cellVal >> 8;
//				Array<Vector2> tileVerts = verts.get(tile);
//				for(int v = 0; v < tileVerts.size; v++)
//				{
//					vertsArr[tile][v]
//							.set(tileVerts.get(v))
//							.add(k.origin(cellNum(x, y)))
//							.sub(k._body.getWorldCenter());
//				}
				
				int cellNum = cellNum(x, y);
				k._origins[cellNum] = Z
						.sim
						.vector()
						.obtain()
						.set(x, y)
						.sub(((float)KoboMap.MAP_SIZEX)/2f, ((float)KoboMap.MAP_SIZEY)/2f)
						.add(0.5f, 0.5f)
						.mul(CELL_SIZE)
						.add(cx, cy);
				Vector2 xx = k.origin(cellNum).sub(cx, cy);
				
				IEntity hardpoint = null;
				//cellVal &= (1 << 9) - 1;
				cellVal = cellVal >> 8;
				if(cellVal == 9 || cellVal == 10 || cellVal == 12 || cellVal == 16)
				{
					hardpoint = KoboHardpoint.spawn(xx, k._body.getWorldCenter(), false);
				}
				else if(cellVal == 6 || cellVal == 7)
				{
					hardpoint = KoboHardpoint.spawn(xx, k._body.getWorldCenter(), true);
				}
				
				k._cellFixtures[x][y] = B2d
					.box(2, 2)
					.at(xx.x, xx.y)				
					//.loop(vertsArr[tile])
					.category(ZipZapSim.COL_CAT_METEORITE)
					.mask(ZipZapSim.COL_CAT_SHIP | ZipZapSim.COL_CAT_LASER)
					.userData(KoboFixtureTag.obtain(k, k, x, y, hardpoint))
					.create(k._body);
			}
	
		Z.sim().entities().add(k);
		
		BossEye.instance().init(Color.RED, 1f, k);
		k.updateEyeLoc();
	}

	@Override
	public int getNumPolys()
	{
		return _cells.length * _cells[0].length;
	}

	@Override
	public float angle(int poly)
	{
		return 0;
	}

	@Override
	public Vector2 origin(int poly)
	{
		return Vector2.tmp.set( _origins[poly]);
	}

	@Override
	public Vector2[] verts(int poly)
	{
		return vertsArr[cellValue(poly) >> 8];
	}

	@Override
	public Color color(int poly)
	{
		int t = cellValue(poly) >> 8;

		switch(t)
		{
			case 8:
				return null;
			case 6:
			case 7:
			case 9:
			case 10:
			case 12:
			case 16:
				return Color.RED;
			default:
				return Color.GRAY;
		}
		
		//return (cellValue(poly) >> 8) == 8 ? null : Color.RED;
	}

	@Override
	public float lineWidth(int poly)
	{
		return 1f;
	}
	
	private int cellValue(int cellNum)
	{
		return _cells[cellNum/_cells.length][cellNum%_cells.length];
	}

	private void updateEyeLoc()
	{
		Vector2.tmp.set(_body.getWorldCenter());
		BossEye.instance().setLoc(Vector2.tmp, -90);
	}
	
	@Override
	public void update(float dt)
	{
//		if(!_dead)
//		{
//			updateEyeLoc();
//			
//			_body.setLinearVelocity(Z
//					.v1()
//					.set(Z.ship().origin())
//					.sub(_body.getWorldCenter())
//					.nor()
//					.mul(SPEED));
//		}
	}

	@Override
	public void free()
	{		
		for(int x = 0; x < KoboMap.MAP_SIZEX; x++)
			for(int y = 0; y < KoboMap.MAP_SIZEY; y++)
				if(_cellFixtures[x][y] != null)
				{
					Z.sim().spawnDebris(this, cellNum(x, y),  _body.getLinearVelocity());
					Z.sim().spawnExlosion(origin(cellNum(x, y)));
					
					KoboFixtureTag t = (KoboFixtureTag)_cellFixtures[x][y].getUserData();
					if(t.hardpoint != null)
						t.hardpoint.free();
					t.free();
					
					_cellFixtures[x][y] = null;
				}
		
		for(int n = 0; n < _origins.length; n++)
		{
			if(_origins[n] != null)
				Z.sim.vector().free(_origins[n]);
		}
		
		Z.sim().entities().removeValue(this, true);
		Z.sim().world().destroyBody(_body);
		pool.free(this);
		BossEye.instance().free();
		
		_dead = true;
	}

	@Override
	public int layer()
	{
		return 0;
	}

	@Override
	public void onBeginContact(Contact c, Fixture me, Fixture other)
	{
		if(other.getBody().getUserData() == Z.ship())
			Z.ship().strike();
	}

	@Override
	public void onEndContact(Contact c, Fixture me, Fixture other)
	{
	}

	@Override
	public void postSolve(Contact c, ContactImpulse impulse, Fixture me,
			Fixture other)
	{
	}

	@Override
	public boolean hit(Fixture f, boolean mega, Vector2 loc, Vector2 norm)
	{
		KoboFixtureTag t = (KoboFixtureTag)f.getUserData();		

		tryBlowTile(t.x, t.y, (_cells[t.x][t.y] & KoboMap.CORE) != 0, true);	
		
		return false;
	}
	
	private void tryBlowTile(int x, int y, boolean reverse, boolean directShot)
	{
		if(x < 0 
				|| x >= KoboMap.MAP_SIZEX 
				|| y < 0 
				|| y >= KoboMap.MAP_SIZEY)
			return;
		
		if((_cells[x][y] & KoboMap.SPACE) != 0)
			return;
		
		if(!reverse)
		{
			int s = 0;
			if(x > 0)
				s += (_cells[x-1][y] & KoboMap.SPACE) == 0 ? 1 : 0;
			if(x < _cells.length-1)
				s += (_cells[x+1][y] & KoboMap.SPACE) == 0 ? 1 : 0;
			if(y > 0)
				s += (_cells[x][y-1] & KoboMap.SPACE) == 0 ? 1 : 0;
			if(y < _cells[0].length-1)
				s += (_cells[x][y+1] & KoboMap.SPACE) == 0 ? 1 : 0;
			
			if(s > 1 || (((_cells[x][y] & KoboMap.CORE) != 0) && s > 0))
			{
				if(directShot)
					Z.sim.fireEvent(SFX_DEFLECT, null);
				return;
			}
		}	
		
		if((_cells[x][y] & KoboMap.CORE) != 0)
		{
			Z.sim().entities().removeValue(BossEye.instance(), true);
			
			if(!Z.ship().dead())
			{
				Vector2 target = origin(cellNum(x, y));			
				Z.sim().setController(new DefeatBossController(target));						
				if(BadgeKobolon.instance().queue())
					BossEye.instance().doNotification();
			}
			
			Z.sim.fireEvent(SFX_BLOW_EYE, null);
		}
		
		Z.sim().spawnDebris(this, cellNum(x, y),  _body.getLinearVelocity());
		Z.sim().spawnExlosion(origin(cellNum(x, y)));
		Z.sim.fireEvent(ZipZapSim.EV_EXPLOSION_SMALL, null);
		
		_body.destroyFixture(_cellFixtures[x][y]);
		
		KoboFixtureTag t = (KoboFixtureTag)_cellFixtures[x][y].getUserData();
		if(t.hardpoint != null)
			t.hardpoint.free();
		t.free();
		
		_cellFixtures[x][y] = null;
		_cells[x][y] = (8 << 8) | KoboMap.SPACE;	
	
		Tween
			.call(_callbackBlowTile )
			.setUserData(Z.sim().vector().obtain().set(reverse ? -(x+1) : x, y))
			.delay(200)
			.start(Z.sim().tweens());		
	}
	
	private static int cellNum(int x, int y)
	{
		return x * KoboMap.MAP_SIZEX + y;
	}
	
	@Override
	public boolean isHitable()
	{
		return true;
	}

	@Override
	public boolean isEyeVulnerable()
	{
		return false;
	}

	@Override
	public void onEyeHit()
	{		
	}
	
	@Override
	public boolean isLoop(int poly)
	{
		return true;
	}
		
	@Override
	public float clipRadius()
	{
		return CELL_SIZE * KoboMap.MAP_SIZEX/2;
	}
}
