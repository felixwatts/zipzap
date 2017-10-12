package com.monkeysonnet.zipzap.behaviours;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.zipzap.IFixtureEventHandler;
import com.monkeysonnet.zipzap.IRenderableMultiPolygon;
import com.monkeysonnet.zipzap.Z;

public class ShieldBehaviour extends BehaviourBase
{
	private static final float HIT_DAMAGE = 1;
	private static final Color _color = new Color();
	private static final int SFX_HIT = -1002;
	
	private Fixture[] _fixtures;
	private float[] _energies;
	private IFixtureEventHandler _onFixtureDestroyed;
	
	private float _strength, _rechargeRate;
	private final Color _glowColor = new Color(), _coldColor = new Color();
	
	public ShieldBehaviour(
			int capacity, 
			float strength, 
			float rechargeRate, 
			Color glowColour, 
			Color coldColor, 
			IFixtureEventHandler onFixtureDestroyed)
	{
		_fixtures = new Fixture[capacity];
		_energies = new float[capacity];
		_onFixtureDestroyed = onFixtureDestroyed;
		
		_strength = strength;
		_rechargeRate = rechargeRate;
		_glowColor.set(glowColour);
		_coldColor.set(coldColor);
		
		for(int n = 0; n < capacity; n++)
			_energies[n] = -1;
	}
	
	@Override
	public void spawn(IEntity subject)
	{
		super.spawn(subject);
		
		for(int n = 0; n < _energies.length; n++)
		{
			if(_fixtures[n] != null)
				_energies[n] = _strength;
		}
	}
	
	public void addFixture(int num, Fixture f)
	{
		_fixtures[num] = f;
		_energies[num] = _strength;
	}
	
	@Override
	public void hit(IEntity subject, Fixture fixture, boolean mega, Vector2 loc, Vector2 norm)
	{
		for(int n = 0; n < _fixtures.length; n++)
		{
			if(_fixtures[n] == fixture)
			{
				Z.sim().spawnExlosion(loc, 3, _glowColor);
				
				Z.sim.fireEvent(SFX_HIT, null);
				
				if(_energies[n] < HIT_DAMAGE)
					Z.sim().spawnDebris((IRenderableMultiPolygon)subject, n, fixture.getBody().getLinearVelocity());
				
				_energies[n] -= HIT_DAMAGE;
				if(_energies[n] < 0)
				{
					if(_onFixtureDestroyed != null)
						_onFixtureDestroyed.onEvent(fixture);
					
					_fixtures[n] = null;						
					fixture.getBody().destroyFixture(fixture);								
				}
				
				break;
			}
		}
	}
	
	@Override
	public void update(float dt, IEntity subject)
	{
		for(int n = 0; n < _energies.length; n++)
		{
			if(_energies[n] >= 0 && _energies[n] < _strength)
			{
				_energies[n] = Math.min(_strength, _energies[n] + (_rechargeRate * dt));
			}
		}
	}
	
	@Override
	public void onFree(IEntity subject)
	{
		for(int n = 0; n < _fixtures.length; n++)
		{
			_fixtures[n] = null;
			_energies[n] = -1;
		}
	}
	
	public Color getColor(int n)
	{
		if(_energies[n] < 0)
			return null;
		
		float f = Math.max(_energies[n], 0) / _strength;
		_color.set(
				getVal(_glowColor.r, _coldColor.r, f), 
				getVal(_glowColor.g, _coldColor.g, f),
				getVal(_glowColor.b, _coldColor.b, f),
				getVal(_glowColor.a, _coldColor.a, f));
		return _color;
	}
	
	public float energy(int n)
	{
		return _energies[n];
	}
	
	private float getVal(float v1, float v2, float f)
	{
		return v1 + (f * (v2 - v1));
	}
}
