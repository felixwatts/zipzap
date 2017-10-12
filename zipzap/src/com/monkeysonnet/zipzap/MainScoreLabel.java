package com.monkeysonnet.zipzap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.monkeysonnet.engine.ButtonActor;

public class MainScoreLabel extends ButtonActor 
{
	private static final float DIGIT_SIZE = 48 * Gdx.graphics.getDensity();
	private CharSequence _str;
	
	public MainScoreLabel(Group parent)
	{
		super(0, 0, 0, 0, false, false, -1, null, null);
		
		color.set(1f, 1f, 116f/255f, 1f);
		x = 0;
		y = parent.height - DIGIT_SIZE - (DIGIT_SIZE / 4f);
		height = DIGIT_SIZE;
		parent.addActor(this);
		setScore(0);
		
		originY = height/2f;
	}
	
	public void setScore(int score)
	{
		_str = Integer.toString(score);
		width = DIGIT_SIZE * _str.length();
		x = (stage.width() - width) / 2f;
		
		originX = width / 2f;
	}
	
	@Override
	public void draw(SpriteBatch batch, float parentAlpha)
	{	
		float x = this.x;
		
		for(int d = 0; d < _str.length(); d++)
		{
			float middle = ((_str.length() * DIGIT_SIZE) / 2f) + this.x;
			float ox = middle - x;
			
			float w = DIGIT_SIZE;
			TextureRegion tex = Z.console().getCharTexture(_str.charAt(d));
			
			Color c;
			if(Z.screen.sim().comboLevel() == 9)
				c = Color.MAGENTA;
			else if(Z.screen.sim().comboLevel() > 1)
				c = Color.RED;
			else 
				c = color;
			
			batch.setColor(c);
			batch.draw(tex, x, this.y, ox, this.originY, w, DIGIT_SIZE, this.scaleX, this.scaleY, 0);
			
			x += w;
		}
	}
}
