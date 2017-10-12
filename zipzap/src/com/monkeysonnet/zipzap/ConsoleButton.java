package com.monkeysonnet.zipzap;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.monkeysonnet.engine.ButtonActor;
import com.monkeysonnet.engine.IButtonEventHandler;

public class ConsoleButton extends ButtonActor
{
	private float _texScale;
	
	public ConsoleButton(TextureRegion tex, float w, IButtonEventHandler handler)
	{
		super(tex, handler);
		_texScale = (float)tex.getRegionWidth() / w;
	}
	
	@Override
	public void draw(SpriteBatch batch, float parentAlpha)
	{
		if(_tex != null)
		{
			batch.setColor(color);
			
			//batch.draw(Textures.solid, x, y, width, height);
			
			batch.draw(
					_tex.getTexture(), 
					x, 
					y, 
					originX, 
					originY, 
					width, 
					height, 
					scaleX, 
					scaleY, 
					rotation, 
					_tex.getRegionX(), 
					_tex.getRegionY(), 
					(int)(_texScale * width), 
					(int)(_texScale * height), 
					false, 
					false);
		}
	}
}
