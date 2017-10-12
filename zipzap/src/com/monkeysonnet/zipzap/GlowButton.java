package com.monkeysonnet.zipzap;

import aurelienribon.tweenengine.Tween;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.monkeysonnet.engine.ButtonActor;
import com.monkeysonnet.engine.ColorTools;
import com.monkeysonnet.engine.IButtonEventHandler;

public class GlowButton extends ButtonActor
{
	private TextureRegion _texGlow;
	private Tween _glowTeen;
	
	public GlowButton(TextureRegion tex, TextureRegion glow, IButtonEventHandler handler)
	{
		super(tex, handler);
		_texGlow = glow;
	}
	
	public void dispose()
	{
		if(_glowTeen != null)
		{
			_glowTeen.kill();
			_glowTeen = null;
		}
	}
	
	@Override
	public void draw(SpriteBatch batch, float parentAlpha)
	{
		batch.setColor(ColorTools.combineAlpha(color, parentAlpha));
		batch.draw(_texGlow, x, y, originX, originY, width, height, scaleX, scaleY, rotation);
		batch.setColor(ColorTools.combineAlpha(color, parentAlpha * color.a));
		batch.draw(_tex, x, y, originX, originY, width, height, scaleX, scaleY, rotation);
	}
}
