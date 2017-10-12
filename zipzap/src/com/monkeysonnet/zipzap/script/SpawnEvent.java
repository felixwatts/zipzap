package com.monkeysonnet.zipzap.script;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.entities.Ace;
import com.monkeysonnet.zipzap.entities.Amoeba;
import com.monkeysonnet.zipzap.entities.Centipede;
import com.monkeysonnet.zipzap.entities.Clam;
import com.monkeysonnet.zipzap.entities.Enterprise;
import com.monkeysonnet.zipzap.entities.Ghoster;
import com.monkeysonnet.zipzap.entities.Glider;
import com.monkeysonnet.zipzap.entities.Gnat;
import com.monkeysonnet.zipzap.entities.Kobo;
import com.monkeysonnet.zipzap.entities.MaxiJelly;
import com.monkeysonnet.zipzap.entities.Meteor2;
import com.monkeysonnet.zipzap.entities.Millipede;
import com.monkeysonnet.zipzap.entities.MiniJelly;
import com.monkeysonnet.zipzap.entities.Mutha;
import com.monkeysonnet.zipzap.entities.NebulonCell;
import com.monkeysonnet.zipzap.entities.Jet;
import com.monkeysonnet.zipzap.entities.RedArrow;
import com.monkeysonnet.zipzap.entities.Rotolon;
import com.monkeysonnet.zipzap.entities.ShootingSeeker;
import com.monkeysonnet.zipzap.entities.SnowFlake;
import com.monkeysonnet.zipzap.entities.SpinningSeeker;
import com.monkeysonnet.zipzap.entities.Square;
import com.monkeysonnet.zipzap.entities.Stalker;
import com.monkeysonnet.zipzap.entities.StingRay;
import com.monkeysonnet.zipzap.entities.Triangle;
import com.monkeysonnet.zipzap.entities.UfoZipper;
import com.monkeysonnet.zipzap.entities.Warpey;

public class SpawnEvent implements IScriptEvent
{
//	public static final int TYPE_JELLY_FISH = 0;
	public static final int TYPE_KOBO = 1;
	public static final int TYPE_NEBULON = 2;
	//public static final int TYPE_POWER_UP = 3;
	public static final int TYPE_RED_ARROW = 4;
	public static final int TYPE_SHOOTING_SEEKER = 5;
	public static final int TYPE_SPINNING_SEEKER = 6;
	public static final int TYPE_TIE_FIGHTER = 7;
	public static final int TYPE_SLIME_BALL = 8;
	public static final int TYPE_GNAT = 9;
	public static final int TYPE_FIGHTER = 10;
	public static final int TYPE_MINI_JELLY = 11;
	public static final int TYPE_MAXI_JELLY = 12;
	public static final int TYPE_TUBULON = 13;
	public static final int TYPE_SNAKEY = 14;
	public static final int TYPE_UFO = 15;
	public static final int TYPE_UFO_ZIPPER = 16;
	public static final int TYPE_BUG = 17;
	public static final int TYPE_RANGE_MINE = 18;
	public static final int TYPE_CLAM = 19;
	public static final int TYPE_AMOEBA = 20;
	public static final int TYPE_ROTOLON = 21;
	public static final int TYPE_MUTHA = 22;
	public static final int TYPE_DUMBOT = 23;
	public static final int TYPE_METEOR = 24;
	public static final int TYPE_ONIONITE = 25;
	public static final int TYPE_METEOR2 = 26;
	public static final int TYPE_GHOSTER = 27;
	public static final int TYPE_MINE_LAYER = 28;
	public static final int TYPE_HEAD = 29;
	public static final int TYPE_SQUARE = 30;
	public static final int TYPE_BACTERIUM = 31;
	public static final int TYPE_TANK = 32;
	public static final int TYPE_JELLY_SHOAL = 33;
	public static final int TYPE_GNAT_SQUADRON = 34;
	public static final int TYPE_STALKER = 35;
	public static final int TYPE_ENTERPRISE = 36;
	public static final int TYPE_WARPEY = 37;
	public static final int TYPE_CENTIPEDE = 38;
	public static final int TYPE_GLIDERS = 39;
	public static final int TYPE_MILLIPEDE = 40;
	public static final int TYPE_STING_RAY = 41;
	public static final int TYPE_SNOWFLAKE = 42;
	public static final int TYPE_ACE = 43;
	public static final int TYPE_TRIANGLE = 44;
	public static final int TYPE_FLYPAST = 45;
	
	private static class SpawnEventPool extends Pool<SpawnEvent>
	{
		@Override
		protected SpawnEvent newObject()
		{
			return new SpawnEvent();
		}
	}
	
	private static final SpawnEventPool pool = new SpawnEventPool();
	
	private int _type, _int1, _int2;
	private float _time;
	private float _x, _y, _angle, _speed;
	private boolean _bool1;
	private Color _color;
	
	private SpawnEvent(){}
	
	public static SpawnEvent obtain(int type)
	{
		SpawnEvent e = pool.obtain();
		e._type = type;
		return e;
	}
	
	public SpawnEvent after(float time)
	{
		_time = time;
		return this;
	}
	
	public SpawnEvent pos(float x, float y)
	{
		_x = x;
		_y = y;
		return this;
	}
	
	public SpawnEvent angle(float a)
	{
		_angle = a;
		return this;
	}
	
	public SpawnEvent color(Color c)
	{
		_color = c;
		return this;
	}
	
	public SpawnEvent int1(int i1)
	{
		_int1 = i1;;
		return this;
	}
	
	public SpawnEvent bool1(boolean b1)
	{
		_bool1 = b1;
		return this;
	}
	
	public SpawnEvent speed(float speed)
	{
		_speed = speed;
		return this;
	}

	@Override
	public IScriptEvent fire()
	{
		fire(_type, _x, _y, _int1, _int2, _angle, _speed, _bool1, _color);
		return this;
	}
	
	public static void fire(int type, float x, float y, int int1, int int2, float angle, float speed, boolean bool1, Color color)
	{
		switch(type)
		{
//			case TYPE_JELLY_FISH:
//				Jellyfish.spawn(x, y, angle);
//				break;
			case TYPE_KOBO: 
				Kobo.spawn(Z.ship().origin().x + x, Z.ship().origin().y + y);
				break;
			case TYPE_NEBULON:
				NebulonCell.spawn(int1);
				break;
//			case TYPE_POWER_UP:
//				Meteor.spawn(speed, int1);
//				break;
			case TYPE_RED_ARROW:
				Jet.spawnSquadron(int1);
				break;
			case TYPE_SHOOTING_SEEKER:
				ShootingSeeker.spawn();
				break;
			case TYPE_SPINNING_SEEKER:
				SpinningSeeker.spawn(angle);
				break;
//			case TYPE_TIE_FIGHTER:
//				TieFighter.spawn();
//				break;		
			case TYPE_GNAT:
				Gnat.spawn(angle, int1, bool1);
				break;
//			case TYPE_FIGHTER:
//				Fighter.spawn();
//				break;
			case TYPE_MINI_JELLY:
				if(bool1)
					MiniJelly.spawn(angle).attack();
				else MiniJelly.spawn(angle);
				break;
			case TYPE_MAXI_JELLY:
				MaxiJelly.spawn();
				break;
//			case TYPE_TUBULON:
//				WormSegment.spawnTubulon(x, y);
//				break;
//			case TYPE_SNAKEY:
//				WormSegment.spawnSnakey();
//				break;
//			case TYPE_UFO:
//				Ufo.spawn(x, y, angle);
//				break;
			case TYPE_UFO_ZIPPER:
				UfoZipper.spawn();
				break;
//			case TYPE_BUG:
//				Bug.spawn();
//				break;
//			case TYPE_RANGE_MINE:
//				RangeMine.spawn(Vector2.tmp.set(x, y));
//				break;
			case TYPE_CLAM:
				Clam.spawn(int1);
				break;
			case TYPE_AMOEBA:
				Amoeba.spawn(bool1);
				break;
			case TYPE_ROTOLON:
				Rotolon.spawn();
				break;
			case TYPE_MUTHA:
				new Mutha();
				break;
//			case TYPE_DUMBOT:
//				Dumbot.spawn();
//				break;
//			case TYPE_METEOR:
//				Meteor.spawn(speed, int1);
//				break;
//			case TYPE_ONIONITE:
//				Onionite.spawn(int1, speed);
//				break;
			case TYPE_METEOR2:
				Meteor2.spawn(int1, speed, angle, bool1);
				break;
			case TYPE_GHOSTER:
				Ghoster.spawn();
				break;
//			case TYPE_MINE_LAYER:
//				MineLayer.spawn();
//				break;
//			case TYPE_HEAD:
//				Head.spawn();
//				break;
			case TYPE_SQUARE:
				Square.spawn();
				break;
//			case TYPE_BACTERIUM:
//				Bacterium.spawn();
//				break;
//			case TYPE_TANK:
//				Tank.spawn(Z.ship().origin().x + x, Z.ship().origin().y + y);
//				break;
			case TYPE_JELLY_SHOAL:
				MiniJelly.spawnShoal(int1);
				break;
			case TYPE_GNAT_SQUADRON:
				Gnat.spawnSquadron(int1);
				break;
			case TYPE_STALKER:
				Stalker.spawn(int1);
				break;
			case TYPE_ENTERPRISE:
				Enterprise.spawnSquadron(int1);
				break;
			case TYPE_WARPEY:
				Warpey.spawn();
				break;
			case TYPE_CENTIPEDE:
				Centipede.spawn();
				break;
			case TYPE_GLIDERS:
				Glider.spawn();
				break;
			case TYPE_MILLIPEDE:
				Millipede.spawn();
				break;
			case TYPE_STING_RAY:
				StingRay.spawn();
				break;
			case TYPE_SNOWFLAKE:
				SnowFlake.spawn();
				break;
			case TYPE_ACE:
				Ace.spawn();
				break;
			case TYPE_TRIANGLE:
				Triangle.spawn(int1);
				break;
			case TYPE_FLYPAST:
				RedArrow.spawnFlypast();
				break;
		}
	}

	@Override
	public float time()
	{
		return _time;
	}

	@Override
	public void free()
	{
		pool.free(this);
	}
}
