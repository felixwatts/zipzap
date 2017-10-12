package com.monkeysonnet.zipzap.script;

import com.badlogic.gdx.Gdx;
import com.monkeysonnet.zipzap.Z;

public class AnnounceBossController extends GameController
{
	private static final float TIME = 4;

	@Override
	public void init()
	{
		super.init();
		Z.screen.sim().clearEnemies();
		Z.screen.sim().clearPowerUps();
		Z.ship().loseBubble();
		Z.hud().announceBoss();
		Z.music.play(null);
	}
	
	@Override
	public void update(float dt)
	{
		super.update(dt);
		
		if(_time > TIME)
		{
			Z.sim.advanceScript();
		}
	}
	
	@Override
	public void cleanup()
	{
		super.cleanup();
		Z.music.play(Gdx.files.internal("music/boss.ogg"));
	}
}
