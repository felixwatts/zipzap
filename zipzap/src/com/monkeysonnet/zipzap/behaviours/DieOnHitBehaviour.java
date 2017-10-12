package com.monkeysonnet.zipzap.behaviours;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.zipzap.IOrigin;
import com.monkeysonnet.zipzap.IRenderableMultiPolygon;
import com.monkeysonnet.zipzap.IRenderablePolygon;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.entities.Enemy;

public class DieOnHitBehaviour extends BehaviourBase
{
	private static final float DEBRIS_SPEED = 30f;
	private Color _explosionColour, _flashColor;
	private int _explosionSize;
	private boolean _createDebris;
	private int _energy, _startEnergy;
	
	private static DieOnHitBehaviour _basic;
	
	public DieOnHitBehaviour(Color explosionColour,
			int explosionSize,
			boolean createDebris,
			Color flashColor,
			int energy)
	{
		_explosionColour = explosionColour;
		_explosionSize = explosionSize;
		_createDebris = createDebris;
		_startEnergy = energy;
		_flashColor = flashColor;
	}
	
	public static DieOnHitBehaviour basic()
	{
		if(_basic == null)
			_basic = new DieOnHitBehaviour(Color.CLEAR, 8, true, Color.CLEAR, 0);
		return _basic;
	}
	
	@Override
	public void spawn(IEntity subject)
	{
		_energy = _startEnergy;
	}
	
	@Override
	public void hit(IEntity subject, Fixture fixture, boolean mega, Vector2 loc, Vector2 norm)
	{
		if(_energy > 0)
			_energy--;
		if(mega || _energy == 0)
		{
			Enemy e = (Enemy)subject;
			
			if(e.killScore() > 0)
				Z.screen.sim().score(e.origin(), e.killScore(), true);
			
			Color itemColor = null;
			if(_explosionColour == Color.CLEAR || _flashColor == Color.CLEAR)
			{
				if(subject instanceof IRenderableMultiPolygon)
				{
					itemColor = ((IRenderableMultiPolygon)subject).color(0);
				}
				else if(subject instanceof IRenderablePolygon)
				{
					itemColor = ((IRenderablePolygon)subject).color();
				}
			}
			
			Color c = _explosionColour == Color.CLEAR ? itemColor : _explosionColour;
			if(c != null)
				Z.sim().spawnExlosion(((IOrigin)subject).origin(), _explosionSize, c);
			
			c = _flashColor == Color.CLEAR ? itemColor : _flashColor;
			if(c != null)
				Z.sim().spawnFlash(((IOrigin)subject).origin(), c);
			
			if(_createDebris)
			{
				Vector2 v = Z.sim().vector().obtain();
				v.set(Z.ship().origin()).sub(((IOrigin)subject).origin()).nor().mul(-DEBRIS_SPEED);
				
				if(subject instanceof IRenderableMultiPolygon)
				{
					Z.sim().spawnDebris((IRenderableMultiPolygon)subject, v);
				}
				else if(subject instanceof IRenderablePolygon)
				{
					Z.sim().spawnDebris((IRenderablePolygon)subject, v);
				}
				
				Z.sim().vector().free(v);
			}
			
			e.onKill();
			
			subject.free();
		}
	}
	
	public int energy()
	{
		return _energy;
	}
}
