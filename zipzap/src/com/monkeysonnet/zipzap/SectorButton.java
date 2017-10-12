package com.monkeysonnet.zipzap;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.monkeysonnet.engine.IButtonEventHandler;
import com.monkeysonnet.zipzap.achievements.TreatUnlockSector;

public class SectorButton extends GlowButton
{
	public final static Color notEarnedColor = new Color(1f, 1f, 1f, 0.2f);
	
	private int _sector;
	private boolean _isUnlocked;

	public SectorButton(int sector, float size, IButtonEventHandler h)
	{
		this(sector, size, h, "button-sector-" + sector, new TreatUnlockSector(sector).isUnlocked());
	}
	
	public SectorButton(int sector, float size, IButtonEventHandler h, String texName, boolean enabled)
	{
		super(Z.texture(texName), Z.texture(texName + "-glow"), h);
		
		_sector = sector;
	
		_isUnlocked = enabled;		
		color.set(_isUnlocked ? Color.WHITE : notEarnedColor);
		touchable = _isUnlocked;
		
		scaleX = scaleY = 0;
		width = height = size;
		originX = width/2f;
		originY = height/2f;
		//Tween.to(this, ActorTweener.VAL_SCALE, 1000).target(1f).ease(Elastic.OUT).delay(Game.Dice.nextFloat()*250).start(Game.TweenManager);
	}
	
	public int sector()
	{
		return _sector;
	}
	
	@Override
	public void draw(SpriteBatch batch, float parentAlpha)
	{
		if(_isUnlocked)
			super.draw(batch, parentAlpha);
		else
			super.draw(batch, parentAlpha * 0.2f);
	}
}
