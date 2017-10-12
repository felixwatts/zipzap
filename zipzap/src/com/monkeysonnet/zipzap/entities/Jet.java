package com.monkeysonnet.zipzap.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.engine.editor.Map;
import com.monkeysonnet.zipzap.Tools;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;
import com.monkeysonnet.zipzap.behaviours.BlobTrailBehaviour;
import com.monkeysonnet.zipzap.behaviours.DieOnHitBehaviour;
import com.monkeysonnet.zipzap.behaviours.IBehaviour;
import com.monkeysonnet.zipzap.script.IActiveCount;

public class Jet extends EnemyBasic
{
	private static final int SFX_JET = 17;	
	private static final int KILL_SCORE = 20;
	private static final float SPEED = 25f;
	private static final float SPACING_X = 8f;
	private static final float SPACING_Y = 4f;
	private static final IBehaviour dieOnHitBehaviour = new DieOnHitBehaviour(Color.CYAN, 8, true, Color.CYAN, 0);
	private static final Map map = new Map("armadillo2.v", 0.5f, 0);
	private static int _activeCount;	
	
	private static final RedArrowPool pool = new RedArrowPool();	
	private static class RedArrowPool extends Pool<Jet>
	{
		@Override
		protected Jet newObject()
		{
			return new Jet();
		}
	}

	public static IActiveCount activeCount = new IActiveCount()
	{
		@Override
		public int activeCount()
		{
			return _activeCount;
		}
	};

	private int _powerup;
	private long _sfxJetIntance;
	
	private Jet() 
	{
		_behaviours.removeValue(DieOnHitBehaviour.basic(), true);
		_behaviours.add(dieOnHitBehaviour);
		_killScore = KILL_SCORE;
	}
	
	public static void spawnSquadron(int powerup)
	{
		Vector2 root = Z.sim.vector().obtain().set(Tools.randomSpawnLoc());
		float angle = Z.v1().set(Z.ship().origin()).sub(root).angle();
		float angleRad = (float)Math.toRadians(angle);
		Vector2 vel = Z.sim.vector().obtain().set(SPEED, 0).rotate(angle);
		Vector2 loc = Z.sim.vector().obtain();
		
		Jet a1 = pool.obtain();		
		a1.setup(map, SPEED);
		a1._body.setTransform(root, angleRad);
		a1._body.setLinearVelocity(vel);
		a1._powerup = powerup;
		a1._sfxJetIntance = Z.sfx.play(SFX_JET, 0f);
		
		loc.set(SPACING_X, SPACING_Y).rotate(angle + 90).add(root);
		Jet a2 = pool.obtain();		
		a2.setup(map, SPEED);
		a2._body.setTransform(loc, angleRad);
		a2._body.setLinearVelocity(vel);
		a2._powerup = PowerUp.TYPE_NONE;
		a2._sfxJetIntance = Z.sfx.play(SFX_JET, 0f);
		
		loc.set(-SPACING_X, SPACING_Y).rotate(angle + 90).add(root);
		Jet a3 = pool.obtain();		
		a3.setup(map, SPEED);
		a3._body.setTransform(loc, angleRad);
		a3._body.setLinearVelocity(vel);
		a3._powerup = PowerUp.TYPE_NONE;
		a3._sfxJetIntance = Z.sfx.play(SFX_JET, 0f);
		
		loc.set(2* SPACING_X, 2* SPACING_Y).rotate(angle + 90).add(root);
		Jet a4 = pool.obtain();		
		a4.setup(map, SPEED);
		a4._body.setTransform(loc, angleRad);
		a4._body.setLinearVelocity(vel);
		a4._powerup = PowerUp.TYPE_NONE;
		a4._sfxJetIntance = Z.sfx.play(SFX_JET, 0f);
		
		loc.set(-2 * SPACING_X, 2 * SPACING_Y).rotate(angle + 90).add(root);
		Jet a5 = pool.obtain();		
		a5.setup(map, SPEED);
		a5._body.setTransform(loc, angleRad);
		a5._body.setLinearVelocity(vel);
		a5._powerup = PowerUp.TYPE_NONE;
		a5._sfxJetIntance = Z.sfx.play(SFX_JET, 0f);
		
		a1.initJets();
		a2.initJets();
		a3.initJets();
		a4.initJets();
		a5.initJets();
		
		Z.sim.vector().free(root);
		Z.sim.vector().free(vel);
		Z.sim.vector().free(loc);
		
		_activeCount += 5;
	}
	
	private void initJets()
	{
		for(int n = _behaviours.size-1; n >= 0; n--)
		{
			if(_behaviours.get(n) instanceof BlobTrailBehaviour)
				_behaviours.removeIndex(n);
		}
		
		Color c = _powerup == PowerUp.TYPE_NONE ? Color.CYAN : PowerUp.colorForType(_powerup);
		
		_behaviours.add(new BlobTrailBehaviour(c, 0.05f, 0.25f, map.point("jet-left").point, 2f, -8f));
		_behaviours.add(new BlobTrailBehaviour(Color.WHITE, 0.05f, 0.25f, map.point("jet-left").point, 1f, -4f));
		
		_behaviours.add(new BlobTrailBehaviour(c, 0.05f, 0.25f, map.point("jet-right").point, 2f, -8f));
		_behaviours.add(new BlobTrailBehaviour(Color.WHITE, 0.05f, 0.25f, map.point("jet-right").point, 1f, -4f));
	}
	
	@Override
	protected void onFree()
	{	
		Z.sfx.stop(SFX_JET, _sfxJetIntance);
		pool.free(this);
		_activeCount--;
	}
	
	@Override
	public boolean hit(Fixture f, boolean mega, Vector2 loc, Vector2 norm)
	{
		PowerUp.spawn(origin(), _powerup);
		return super.hit(f, mega, loc, norm);
	}
	
	@Override
	public void update(float dt)
	{
		super.update(dt);
		
		if(!_dead)
		{
			float vol = 100f / Z.ship().origin().dst2(origin());
			if(vol > 1f)
				vol = 1f;
			Z.sfx.setVolume(SFX_JET, _sfxJetIntance, vol);
		}
	}
	
	@Override
	public void onKill()
	{
		Z.sim.fireEvent(ZipZapSim.EV_EXPLOSION_MEDIUM, null);
	}
}
