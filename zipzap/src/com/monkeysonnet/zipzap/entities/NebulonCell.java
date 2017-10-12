package com.monkeysonnet.zipzap.entities;

import java.util.ArrayList;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.JointEdge;
import com.badlogic.gdx.physics.box2d.joints.DistanceJoint;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.engine.DistanceJointTweener;
import com.monkeysonnet.engine.FixtureTag;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.IContactHandler;
import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.zipzap.IBossBody;
import com.monkeysonnet.zipzap.IColourable;
import com.monkeysonnet.zipzap.IHitable;
import com.monkeysonnet.zipzap.IRenderableMultiPolygon;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;
import com.monkeysonnet.zipzap.achievements.BadgeNebulon;
import com.monkeysonnet.zipzap.script.DefeatBossController;

public class NebulonCell implements IRenderableMultiPolygon, IContactHandler, IEntity, IHitable, IColourable, IBossBody
{
	private static final float RADIUS = 2f;
	private static final int NUM_NEIGHBOURS = 6;
	private static final Vector2[] verts;
	private static final float[] splungeColourArr = new float[]{ 0f, 1f, 0f, 1f };
	
	private static final NebulonCellPool pool = new NebulonCellPool();
	
	private Body _body;
	private final NebulonCell[] _neighbours = new NebulonCell[NUM_NEIGHBOURS];
	private final FixtureTag _fixtureTag = new FixtureTag(this, this);
	private int _level;
	private final Color _colour = new Color(1f, 0f, 1f, 0.2f);
	private static final float[] _colourArr = new float[4];
	protected static final int SFX_LAY_SLIMEBALL = -1022;
	protected static final int SFX_HIT = -1016;
	protected static final int SFX_SPLUNGE = -1026;
	protected static final int SFX_DIE = -1016;
	protected static final int SFX_EYE_DIE2 = -1027;
	private static final int SFX_EYE_DIE = -1003;
	protected static final float SPEED_SLIMEBALL = 14f;// 18f;
	private float _disconnectedTime = -1;	
	private int _health;	
	private float _bleedTime;
	private boolean _dead;
	
	private final TweenCallback _callbackHit = new TweenCallback()
	{		
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			if(type == TweenCallback.COMPLETE)
			{
				disconnect();
			}
		}
	};
	
	private void updateEye()
	{
		if(!BossEye.instance().dead())
			BossEye.instance().setLoc(origin(0), angle(0) - 90);
	}
	
	private final TweenCallback _callbackSplunge = new TweenCallback()
	{		
		private final int[] _nNums = new int[3];
		private final TweenCallback callbackPlaySound = new TweenCallback()
		{
			
			@Override
			public void onEvent(int type, BaseTween<?> source)
			{
				int sfx = (Integer)source.getUserData();
				Z.sim.fireEvent(sfx, null);
			}
		};
		
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			int dir = (Integer)source.getUserData();
			
			Tween.call(callbackPlaySound).setUserData(SFX_SPLUNGE).delay(Game.Dice.nextFloat()*100f).start(Z.sim.tweens());

			if(_level > 1 || !isExposed())
			{
				if(_neighbours[dir] == null)
				{
					Tween.call(callbackPlaySound).setUserData(SFX_LAY_SLIMEBALL).delay(Game.Dice.nextFloat()*100f).start(Z.sim.tweens());
					Slimeball.spawn(origin(0), Z.v1().set(SPEED_SLIMEBALL, 0).rotate((float)Math.toDegrees(_body.getAngle())).rotate(dir * (360/NUM_NEIGHBOURS)));
				}
				else
				{
					_neighbours[dir].splunge(dir);
				}
			}
			else
			{
				_nNums[0] = neighbour(oppositeNeighbour(dir), true);
				_nNums[1] = dir;
				_nNums[2] = neighbour(oppositeNeighbour(dir), false);
				
				for(int n = 0; n < 3; n++)
				{
					if(_neighbours[_nNums[n]] == null)
					{
						Tween.call(callbackPlaySound).setUserData(SFX_LAY_SLIMEBALL).delay(Game.Dice.nextFloat()*100f).start(Z.sim.tweens());
						Slimeball.spawn(origin(0), Z.v1().set(8, 0).rotate((float)Math.toDegrees(_body.getAngle())).rotate(_nNums[n] * (360/NUM_NEIGHBOURS)));
					}
					else
					{
						_neighbours[_nNums[n]].splunge(dir);
					}
				}
			}
//			else if(_level > -1)
//			{
//				if(_neighbours[dir] != null)
//					_neighbours[dir].splunge(dir);
//			}
//			else
//			{
//				if(_neighbours[dir] != null)
//					_neighbours[dir].splunge(dir);
//			}
		}
	};	
	
	static
	{
		verts = new Vector2[6];
		for(int n = 0; n < 6; n++)
		{
			verts[n] = new Vector2(RADIUS, 0).rotate(n * (360/6));
		}
	}
	
	private NebulonCell(){}
	
	private static class NebulonCellPool extends Pool<NebulonCell>
	{
		@Override
		protected NebulonCell newObject()
		{
			return new NebulonCell();
		}		
	}

	public static void spawn(int size)
	{	
		NebulonCell c = pool.obtain();
	
		c._level = -1;
		c._disconnectedTime = -1;
		c._health = 3;
		c._dead = false;
		
		c._body = B2d
				.dynamicBody()
				.at(Vector2.tmp.set(Z.ship().origin()).add(0, 80))
				.linearDamping(0)
				.withFixture(B2d
						.circle()
						.radius(RADIUS)
						.category(ZipZapSim.COL_CAT_METEORITE)
						.mask(ZipZapSim.COL_CAT_SHIP | ZipZapSim.COL_CAT_LASER)
						.userData(c._fixtureTag))
				.create(Z.sim().world());
		
		Z.sim().entities().add(c);
		
		BossEye.instance().init(Color.GREEN, 1f, c);
		c.updateEye();
		
		for(int s = 0; s < size; s++)
		{
			c.grow(s, s);
		}
	}
	
	private void injure()
	{
		if(_health > 0)
		{
			_health--;			
			_colour.set(1f, 0f, 1f - ((3f-_health)/3f), 0.2f);		
		}
	}
	
	public static void spawn(NebulonCell parent, int pos, int t)
	{
		NebulonCell c = pool.obtain();
		
		c._level = t;
		c._colour.set(1f, 0f, 1f, 0.2f);
		c._disconnectedTime = -1;
		c._splungeTimer = 0;
		c._health = 3;
		c._dead = false;
		
		Z.v1()
			.set(2*RADIUS, 0)
			.rotate(pos * (360f/NUM_NEIGHBOURS))
			.add(parent.origin(0));
		
		c._body = B2d
				.dynamicBody()
				.at(Z.v1())
				.withFixture(B2d
						.circle()
						.radius(RADIUS)
						.category(ZipZapSim.COL_CAT_METEORITE)
						.mask(ZipZapSim.COL_CAT_SHIP | ZipZapSim.COL_CAT_LASER)
						.userData(c._fixtureTag))
				.create(Z.sim().world());
		
		B2d
			.distanceJoint()
			.between(c._body, parent._body)
			.collideConnected(false)
			.frequencyHz(1f)
			.length(RADIUS*2)
			.create(Z.sim().world());
		
		c._neighbours[oppositeNeighbour(pos)] = parent;
		parent._neighbours[pos] = c;
		
		int parentNeighbourPos = (pos-1)%NUM_NEIGHBOURS;
		if(parentNeighbourPos < 0)
			parentNeighbourPos += NUM_NEIGHBOURS;
		NebulonCell n1 = parent._neighbours[parentNeighbourPos];
		if(n1 != null)
		{
			B2d
				.distanceJoint()
				.between(c._body, n1._body)
				.collideConnected(false)
				.frequencyHz(1f)
				.length(RADIUS*2)
				.create(Z.sim().world());
			
			c._neighbours[neighbour(pos, true)] = n1;
			n1._neighbours[neighbour(parentNeighbourPos, false)] = c;
		}
		
		parentNeighbourPos = (pos+1)%NUM_NEIGHBOURS;
		NebulonCell n2 = parent._neighbours[parentNeighbourPos];
		if(n2 != null)
		{
			B2d
				.distanceJoint()
				.between(c._body, n2._body)
				.collideConnected(false)
				.frequencyHz(1f)
				.length(RADIUS*2)
				.create(Z.sim().world());
			
			c._neighbours[neighbour(pos, false)] = n2;
			n2._neighbours[neighbour(parentNeighbourPos, true)] = c;
		}
		
		Z.sim().entities().add(c);
	}
	
	private void grow(int level, int t)
	{	
		if(level == 0)
		{
			for(int n = 0; n < NUM_NEIGHBOURS; n++)
			{
				if(_neighbours[n] == null)
				{
					spawn(this, n, t);
				}
			}
		}
		else
		{
			for(int n = 0; n < NUM_NEIGHBOURS; n++)
			{
				if(_neighbours[n] != null)
				{
					_neighbours[n].grow(level-1, t);
				}
			}
		}
	}
	
	private static int neighbour(int parentPos, boolean clockwise)
	{
		return (parentPos + (clockwise ? 4 : 8)) % 6; //; TODO use NUM_NEIGHTBOURS
	}
	
	private static int oppositeNeighbour(int n)
	{
		return ((NUM_NEIGHBOURS/2)+n)%NUM_NEIGHBOURS;
	}
	
	private void disconnect()
	{
//		boolean cancel = false;
//		for(int n = 0; n < NUM_NEIGHBOURS; n++)
//		{
//			if(_neighbours[n] != null)
//			{
//				cancel = false;
//				break;
//			}
//		}
//		if(cancel)
//			return;
		
		ArrayList<JointEdge> joints = _body.getJointList();
		for(int n = joints.size()-1; n >= 0; n--)
		{
			Z.sim().world().destroyJoint(joints.get(n).joint);
		}
		
		for(int n = 0; n < NUM_NEIGHBOURS; n++)
		{
			if(_neighbours[n] != null)
			{
				_neighbours[n]._neighbours[oppositeNeighbour(n)] = null;
				_neighbours[n] = null;				
			}
		}		
		
		_disconnectedTime = 4;
		
//		if(_level > 1)
//		{
//			Tween.call(new TweenCallback()
//			{
//				@Override
//				public void onEvent(int type, BaseTween source)
//				{
//					free();
//				}
//			})
//			.delay(4000)
//			.start(Game.TweenManager);
//		}
	}
	
	public boolean isDisconnected()
	{
		return _disconnectedTime > 0;
	}

	@Override
	public int getNumPolys()
	{
		return 1;
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
		return verts;
	}	
	
	@Override
	public Color color(int poly)
	{
		return poly == 0 ? _colour : Color.GREEN;// Color.MAGENTA;
	}

	@Override
	public float lineWidth(int poly)
	{
		return poly == 0 ? 1f : 0.75f;
	}

	@Override
	public void onBeginContact(Contact c, Fixture me, Fixture other)
	{
		_dead = true;	
		
		if(other.getBody().getUserData() == Z.ship())
		{
			Z.ship().strike();
		}
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

	float _splungeTimer = 0;
	private float _felpTimer;	
	
	@Override
	public void update(float dt)
	{
		if(_dead)
			free();
		else
		{
			if(_level == -1)			
			{
				updateEye();
				
				_splungeTimer -= dt;
				if(_splungeTimer < 0)
				{
					_splungeTimer = 4f;
					//splunge(0);
					for(int n = 0; n < NUM_NEIGHBOURS; n++)
						splunge(n);
				}
				
				_felpTimer -= dt;
				if(_felpTimer < 0)
				{
					felp();
					_felpTimer = 8f;
				}
			}
			
			if(_disconnectedTime > 0)
			{
				_disconnectedTime -= dt;
				if(_disconnectedTime < 0)
				{					
					free();
				}
			}
			
			if(_health < 3)
			{
				_bleedTime -= dt;
				if(_bleedTime < 0)
				{
					_bleedTime = 0.2f + _health;
					Particle.spawn(origin(0), Z.v1().set(Game.Dice.nextFloat(), Game.Dice.nextFloat()), Color.RED, 0.5f, 0);
				}
			}
		}
	}
	
	@Override
	public void free()
	{
		if(!isDisconnected())
			disconnect();
		Z.sim().tweens().killTarget(this);
		Z.sim().spawnExlosion(origin(0), 4, Color.MAGENTA);
		Z.sim().spawnCloud(origin(0), 1, Color.GREEN, 6f);
		Z.sim().fireEvent(SFX_DIE, null);
		Z.sim().entities().removeValue(this, true);
		Z.sim().world().destroyBody(_body);
		pool.free(this);	
		
		if(_level == -1)
		{
			BossEye.instance().free();
			Z.sim().fireEvent(SFX_EYE_DIE, null);
			Z.sim().fireEvent(SFX_EYE_DIE2, null);
//			Array<IEntity> entities = Z.sim().entities(); 
//			for(int n = entities.size-1; n >= 0; n--)
//			{
//				if(entities.get(n) instanceof NebulonCell || entities.get(n) instanceof Slimeball)
//					entities.get(n).free();
//			}
		}
	}

	@Override
	public int layer()
	{
		return 0;
	}

	@Override
	public boolean hit(Fixture f, boolean mega, Vector2 loc, Vector2 norm)
	{
		if(_level == -1)
		{
			for(IEntity e : Z.sim().entities())
			{
				if(e instanceof NebulonCell && e != this)
				{
					NebulonCell c = (NebulonCell)e;
					
					float t = 1 + (float) Game.Dice.nextGaussian();
					if(t < 0.01f)
						t = 0.01f;
					if(t > 1.5f)
						t = 2f;
					
					c._disconnectedTime = t/2f;
				}
//				else if(e instanceof Particle)
//					e.free();
			}
			
			disconnect();
			_disconnectedTime = 0.1f;
			
			Z.sim().spawnExlosion(origin(0), 8, Color.GREEN, 4f);
				
			Z.sim().setController(new DefeatBossController(origin(0)));	
			
			if(BadgeNebulon.instance().queue())
				BossEye.instance().doNotification();
		}		
		else if(isDisconnected())
			free();
		else
		{
			Z.sim.fireEvent(SFX_HIT, null);
			Z.sim().spawnExlosion(origin(0), 6, Color.MAGENTA);
			
			injure();
			//injure();
			
			if(_health == 0)
			{			
				for(JointEdge j : _body.getJointList())
				{
					DistanceJoint d = (DistanceJoint)j.joint;
					
					Z.sim().tweens().killTarget(d);
					
					Timeline
						.createSequence()
						.push(Tween
								.set(d, DistanceJointTweener.VAL_LENGTH)
								.target(RADIUS*1.5f))
						.pushPause(1000)
						.push(Tween
								.to(d, DistanceJointTweener.VAL_LENGTH, 500)
								.target(2*RADIUS))
						.setCallback(_callbackHit)
						.start(Z.sim().tweens());
				}
				
				for(int n = 0; n < NUM_NEIGHBOURS; n++)
				{
					if(_neighbours[n] != null)
					{
						_neighbours[n].injure();
					}
				}
			}
		}
		
		Z.sim.fireEvent(ZipZapSim.EV_LASER_SMALL, null);
		
		return false;
	}
	
	private boolean isExposed()
	{
		for(int n = 0; n < NUM_NEIGHBOURS; n++)
			if(_neighbours[n] == null)
				return true;
		return false;
	}
	
	private void splunge(int direction)
	{
		Z.sim().tweens().killTarget((IColourable)this, 0);
		
		_colourArr[0] = 1f;
		_colourArr[1] = 0f;
		_colourArr[2] = 1f - ((3f-_health)/3f);
		_colourArr[3] = 0.2f;
		
		Timeline
			.createSequence()
			.push(Tween.to(this, 0, 400).target(splungeColourArr).cast(IColourable.class))
			.push(Tween.to(this, 0, 400).target(_colourArr).cast(IColourable.class))
			.start(Z.sim().tweens());
		
		Tween.call(_callbackSplunge)
			.setUserData(direction)
			.delay(200)
			.start(Z.sim().tweens());
	}
	
	private void felp()
	{
		Amoeba.spawn(origin(0).x, origin(0).y, false);
		
		for(JointEdge j : _body.getJointList())
		{
			DistanceJoint d = (DistanceJoint)j.joint;
			
			Z.sim().tweens().killTarget(d);
			
			Timeline
				.createSequence()
				.push(Tween
						.set(d, DistanceJointTweener.VAL_LENGTH)
						.target(RADIUS*4f))
				.pushPause(1000)
				.push(Tween
						.to(d, DistanceJointTweener.VAL_LENGTH, 500)
						.target(2*RADIUS))
				.start(Z.sim().tweens());
		}
	}

	@Override
	public Color color()
	{
		return _colour;
	}

	@Override
	public void setColor(float r, float g, float b, float a)
	{
		_colour.set(r, g, b, a);
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
		return RADIUS;
	}
}
