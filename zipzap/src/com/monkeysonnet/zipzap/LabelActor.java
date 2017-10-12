package com.monkeysonnet.zipzap;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.monkeysonnet.engine.ColorTools;

public class LabelActor extends Actor
{
	private CharSequence _str;
	private float _charSize;
	
	public LabelActor(CharSequence str, Stage stage)
	{
		_str = str;
		_charSize = (stage.width() * 0.75f) / _str.length();
		
		width = _charSize * _str.length();
		height  = _charSize;
		x = (stage.width() - width) / 2f;
		y = _charSize/2f;
		color.set(Color.WHITE);
		
		stage.addActor(this);
	}
	
	public LabelActor(CharSequence str, float maxWidth, float maxHeight)
	{
		_str = str;
		
		_charSize = Math.min(maxWidth / _str.length(), maxHeight);
		width = _charSize * _str.length();
		height  = _charSize;		
	}

	@Override
	public void draw(SpriteBatch batch, float parentAlpha)
	{
		batch.setColor(ColorTools.combineAlpha(color, parentAlpha));
		for(int n = 0; n < _str.length(); n++)
		{
			if(_str.charAt(n) != ' ')
			{
				batch.draw(Z.console().getCharTexture(_str.charAt(n)), x + n * _charSize, y, 0, 0, _charSize, _charSize, 1f, 1f, 0);
			}
		}
	}

	@Override
	public Actor hit(float x, float y)
	{
		return null;
	}

}
