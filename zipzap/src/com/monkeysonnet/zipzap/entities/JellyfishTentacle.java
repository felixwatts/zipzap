package com.monkeysonnet.zipzap.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.zipzap.IRenderableChain;
import com.monkeysonnet.zipzap.Z;

public class JellyfishTentacle implements IEntity, IRenderableChain
{
	//private static final int NUM_SECTIONS = 3;
	private static final float NODE_RADIUS = 0.0001f;
	private static final JellyfishTentaclePool pool = new JellyfishTentaclePool();
	private static final Vector2 origin = new Vector2(0, 0);
	private static final float LINEAR_DAMPING = 5f;
	private static final int MAX_NUM_SECTIONS = 16;
	
	private Array<Body> _bodies; 
	private Vector2[] _verts;
	private boolean _freed;
	private final Color _colour = new Color();
	
	private static class JellyfishTentaclePool extends Pool<JellyfishTentacle>
	{
		@Override
		protected JellyfishTentacle newObject()
		{
			return new JellyfishTentacle();
		}
	}
	
	private JellyfishTentacle()
	{
		_bodies = new Array<Body>();
		_verts = new Vector2[MAX_NUM_SECTIONS];
	}
	
	public static JellyfishTentacle spawn(Body parent, Vector2 root, Vector2 tip, int numSections, Color c)
	{	
		JellyfishTentacle t = pool.obtain();
	
		t._freed = false;
		t._bodies.clear();
		
		t._colour.set(c.r, c.g, c.b, 0.5f);// = c;
		
		float maxLength = root.dst(tip) / numSections;
		
		Body prevBody = parent;
		for(int n = 0; n < numSections; n++)
		{
			Z.v1().set(tip).sub(root).mul((1f/numSections) * n).add(root);
			Z.v1().set(parent.getWorldPoint(Z.v1()));
			
			Body b = B2d
					.dynamicBody()	
					.linearDamping(LINEAR_DAMPING)
					.at(Z.v1())
					.withFixture(B2d.circle().radius(NODE_RADIUS).category(0).mask(0).sensor(true))
					.create(Z.sim().world());
			
			t._bodies.add(b);
			t._verts[n] = b.getWorldCenter();
			
			B2d
				.ropeJoint()
				.between(prevBody, b)
				.localAnchorA(n == 0 ? root : Z.v1().set(0, 0))
				.localAnchorB(Z.v1().set(0, 0))
				.maxLength(n == 0 ? 0 : maxLength)
				.create(Z.sim().world());
			
			prevBody = b;
		}
		
		Z.sim().entities().add(t);
		
		return t;
	}
	
	@Override
	public void free()
	{
		if(!_freed)
		{
			for(Body b : _bodies)
				Z.sim().world().destroyBody(b);
			_bodies.clear();
					
			for(int n = 0; n < _verts.length; n++)
				_verts[n] = null;

			Z.sim().entities().removeValue(this, true);
			pool.free(this);
			_freed = true;
		}
	}

	@Override
	public void update(float dt)
	{
	}

	@Override
	public int layer()
	{
		return 0;
	}

	@Override
	public float angle()
	{
		return 0;
	}

	@Override
	public Vector2 origin()
	{
		return origin;
	}

	@Override
	public Vector2[] verts()
	{
		for(int n = 0; n < _bodies.size; n++)
			_verts[n] = _bodies.get(n).getWorldCenter();	
		return _verts;
	}

	@Override
	public Color color()
	{
		return _colour;
	}

	@Override
	public float lineWidth()
	{
		return 0.75f;
	}
		
	@Override
	public float clipRadius()
	{
		return Float.POSITIVE_INFINITY; // todo hack
	}
}
