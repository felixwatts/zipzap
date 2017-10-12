package com.monkeysonnet.zipzap.behaviours;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.monkeysonnet.engine.IEntity;

public interface IBehaviour
{
	void update(float dt, IEntity subject);
	void spawn(IEntity subject);
	void onBeginContact(IEntity subject, Contact c, Fixture me, Fixture other);
	void onEndContact(IEntity subject, Contact c, Fixture me, Fixture other);
	void postSolve(IEntity subject, Contact c, ContactImpulse impulse, Fixture me,
			Fixture other);
	void hit(IEntity subject, Fixture fixture, boolean mega, Vector2 loc, Vector2 norm);
	void onFree(IEntity subject);
}
