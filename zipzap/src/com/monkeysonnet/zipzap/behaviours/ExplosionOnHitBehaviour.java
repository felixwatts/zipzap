package com.monkeysonnet.zipzap.behaviours;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.zipzap.Z;

public class ExplosionOnHitBehaviour extends BehaviourBase
{
	private static ExplosionOnHitBehaviour _magenta;
	private static ExplosionOnHitBehaviour _red;
	private static ExplosionOnHitBehaviour _green;
	private static ExplosionOnHitBehaviour _white;
	private static ExplosionOnHitBehaviour _yellow;
	private static ExplosionOnHitBehaviour _cyan;
	private final Color _color = new Color();
	private int _size;
	
	public ExplosionOnHitBehaviour(Color color, int size)
	{
		_color.set(color);
		_size = size;
	}
	
	@Override
	public void hit(IEntity subject, Fixture fixture, boolean mega,
			Vector2 loc, Vector2 norm)
	{
		if(loc != null)
			Z.sim().spawnExlosion(loc, _size, _color);
	}
	
	public static final ExplosionOnHitBehaviour magenta()
	{
		if(_magenta == null)
			_magenta = new ExplosionOnHitBehaviour(Color.MAGENTA, 5);
		return _magenta;
	}
	
	public static final ExplosionOnHitBehaviour red()
	{
		if(_red == null)
			_red = new ExplosionOnHitBehaviour(Color.RED, 5);
		return _red;
	}
	
	public static final ExplosionOnHitBehaviour green()
	{
		if(_green == null)
			_green = new ExplosionOnHitBehaviour(Color.GREEN, 5);
		return _green;
	}
	
	public static final ExplosionOnHitBehaviour cyan()
	{
		if(_cyan == null)
			_cyan = new ExplosionOnHitBehaviour(Color.CYAN, 5);
		return _cyan;
	}
	
	public static final ExplosionOnHitBehaviour white()
	{
		if(_white == null)
			_white = new ExplosionOnHitBehaviour(Color.WHITE, 5);
		return _white;
	}
	
	public static final ExplosionOnHitBehaviour yellow()
	{
		if(_yellow == null)
			_yellow = new ExplosionOnHitBehaviour(Color.YELLOW, 5);
		return _yellow;
	}
}
