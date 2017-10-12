package com.monkeysonnet.zipzap;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.monkeysonnet.engine.ColorTools;
import com.monkeysonnet.engine.IProjection;
import com.monkeysonnet.engine.WorldButton;

public class WorldLabel extends WorldButton
{
	private CharSequence _str;
	private float _charSize;
	private IOrigin _target;

	public WorldLabel(IProjection renderer, CharSequence str, float charSize, IOrigin target, int hAlign, int vAlign)
	{
		super(0, 0, 0, 0, false, false, -1, null, null, renderer, hAlign, vAlign);
		
		_str = str;
		_charSize = charSize;
		_target = target;
		
		width = str.length() * charSize;
		height = charSize;
	}

	@Override
	public void draw(SpriteBatch batch, float parentAlpha)
	{
		batch.setColor(ColorTools.combineAlpha(color, parentAlpha));
		for(int n = 0; n < _str.length(); n++)
		{
			TextureRegion c = Z.console().getCharTexture(_str.charAt(n));
			
			if(c != null)
			{			
				batch.draw(
						c, 
						x + n * _charSize, 
						y, 
						originX, 
						originY, 
						_charSize, 
						_charSize, 
						scaleX, 
						scaleY, 
						rotation);
			}
		}
	}
	
	@Override
	public Actor hit(float x, float y)
	{
		return null;
	}
	
	@Override
	public void act(float delta)
	{
		super.act(delta);
		
		setWorldLocation(_target.origin().x, _target.origin().y);
	}
}
