package com.monkeysonnet.zipzap;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;

public interface IHitable
{
	boolean hit(Fixture f, boolean mega, Vector2 point, Vector2 normal);
	
	boolean isHitable();
}
