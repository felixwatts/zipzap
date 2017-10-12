package com.monkeysonnet.zipzap.entities;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.equations.Quad;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.zipzap.IOrigin;
import com.monkeysonnet.zipzap.IRenderablePolygon;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.achievements.BadgeSquadron;

public class Squadey implements IRenderablePolygon, IEntity, IOrigin
{
	private static final float START_OFFSET_Y = 50f;
	private static final float X_SPACING = 10f;
	private static final float Y_SPACING = 5f;
	
	private final Vector2 _focalPointOffset = new Vector2();
	private int _num;
	
	private Squadey(){}
	
	public static Squadey spawn(int num)
	{
		Squadey s = new Squadey();
	
		s._num = num;
		
		switch(num)
		{
			case 0:
				s.setup(0, 2);
				break;
			case 1:
				s.setup(-1, 1);
				break;
			case 2:
				s.setup(1, 1);
				break;
			case 3:
				s.setup(-2, 0);
				break;
			case 4:
				s.setup(2, 0);
				break;
		}
		
		return s;
	}
	
	public final IOrigin labelPos = new IOrigin()
	{
		
		@Override
		public Vector2 origin()
		{
			return Vector2.tmp.set(Squadey.this.origin()).add(0, 5f);
		}
		
		@Override
		public float angle()
		{
			return 0;
		}
	};
	
	private void setup(int xPos, int yPos)
	{
		_focalPointOffset.set(xPos * X_SPACING, (yPos*Y_SPACING) -START_OFFSET_Y);
		
		Tween
			.to(_focalPointOffset, 0, 3000)
			.target(_focalPointOffset.x, yPos*Y_SPACING)
			.ease(Quad.OUT)
			.setCallback(arriveCompleteCallback)
			.delay(Game.Dice.nextFloat()*500)
			.start(Z.sim().tweens());
	}
	
	private final TweenCallback arriveCompleteCallback = new TweenCallback()
	{
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			if(_num > 0)
			{
				if(BadgeSquadron.instance(_num).queue())
					Game.ScreenManager.push(Z.achievementsScreen);
			}
		}
	};

	@Override
	public float angle()
	{
		return 90;
	}

	@Override
	public Vector2 origin()
	{
		return Vector2.tmp.set(Z.sim.focalPoint()).add(_focalPointOffset);
	}

	@Override
	public Vector2[] verts()
	{
		return Ship.verts;
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

	@Override
	public void update(float dt)
	{
		Particle.spawn(Z.v2().set(Ship.verts[1]).rotate(angle()).add(origin()), Z.v1().set(0, 0), Color.ORANGE, 0.5f, 0, 0.2f);
	}

	@Override
	public void free()
	{
		Z.sim().tweens().killTarget(_focalPointOffset);
	}

	@Override
	public int layer()
	{
		return 1;
	}
		
	@Override
	public float clipRadius()
	{
		return 4f;
	}
}
