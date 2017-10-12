package com.monkeysonnet.zipzap;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class TitleActor extends Actor
{
	private TextureRegion _tex = Z.texture("astronomo");

	public TitleActor(Stage stage)
	{
		float g = stage.width() / 80f;		
		
		width = 72 * g;
		height = ((float)_tex.getRegionHeight() / (float)_tex.getRegionWidth()) * width;
		originX = width / 2f;
		originY = height / 4f;
		x = 4 * g;
		y = stage.height() - (height + 1.5f*g);
		color.set(Color.WHITE);
		
		stage.addActor(this);
	}

	@Override
	public void draw(SpriteBatch batch, float parentAlpha)
	{
		batch.setColor(color);
		batch.draw(_tex, x, y, originX, originY, width, height, scaleX, scaleY, rotation);
	}

	@Override
	public Actor hit(float x, float y)
	{
		return null;
	}
}
