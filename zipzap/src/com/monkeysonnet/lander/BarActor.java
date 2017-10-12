package com.monkeysonnet.lander;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.monkeysonnet.zipzap.LabelActor;
import com.monkeysonnet.zipzap.Z;

public class BarActor extends Group
{
	private IBarData _data;
	private float _borderThickness;
	private TextureRegion _texSolid = Z.texture("solid");
	
	public BarActor(IBarData data, String label, float width, float height)
	{
		_data = data;
		
		this.width = width;
		this.height = height;
	
		_borderThickness = Math.min(width/16f, height/16f);
		
		LabelActor l = new LabelActor(label, width - (8*_borderThickness), height - (8*_borderThickness));
		l.x = (width - l.width) / 2f;
		l.y = (height - l.height) / 2f;
		addActor(l);
	}
	
	public void data(IBarData data)
	{
		_data = data;
	}
	

	@Override
	public void draw(SpriteBatch batch, float parentAlpha)
	{
		batch.setColor(1, 1, 1, parentAlpha * 0.25f);
		batch.draw(_texSolid, x, y, width/2f, height/2f, _borderThickness, height, scaleX, scaleY, rotation);
		batch.draw(_texSolid, x, y, width/2f, height/2f, width, _borderThickness, scaleX, scaleY, rotation);
		batch.draw(_texSolid, x, (y+height)-_borderThickness, width/2f, height/2f, width, _borderThickness, scaleX, scaleY, rotation);
		batch.draw(_texSolid, (x+width)-_borderThickness, y, width/2f, height/2f, _borderThickness, height, scaleX, scaleY, rotation);
		
		batch.setColor(color.r, color.g, color.b, parentAlpha * 0.25f);
		
		batch.draw(
				_texSolid, 
				x+(2*_borderThickness), 
				y+(2*_borderThickness), 
				width/2f, 
				height/2f, 
				(width-(4*_borderThickness)) * (_data == null ? 1 : (_data.val() / _data.maxVal())), 
				height-(4*_borderThickness), 
				scaleX, 
				scaleY, 
				0);
		
		super.draw(batch, parentAlpha);
	}

	@Override
	public Actor hit(float x, float y)
	{
		return null;
	}
}
