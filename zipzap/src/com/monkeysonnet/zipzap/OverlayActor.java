package com.monkeysonnet.zipzap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class OverlayActor extends Actor
{
	private static final TextureRegion tex = Z.texture("zipzap-scanlines"); // zipzap-background

	public OverlayActor(Stage stage)
	{
		x = 0; y = 0;
		width = stage.width();
		height = stage.height();
		touchable = false;
		color.set(Color.GRAY);
		
//		_u2 = width / tex.getRegionWidth();
//		_v2 = height / tex.getRegionHeight();
		
		stage.addActor(this);
	}

	@Override
	public void draw(SpriteBatch batch, float parentAlpha)
	{
		batch.setColor(Gdx.graphics.getFramesPerSecond() < 55 ? Color.RED : color);
		//batch.draw(tex, x, y, 0, 0, width, height, 1f, 1f, 0);
		batch.draw(tex.getTexture(), x, y, width, height, 0, 0, (int)width, (int)height, false, false);
	}

	@Override
	public Actor hit(float x, float y)
	{
		return null;
	}
}
