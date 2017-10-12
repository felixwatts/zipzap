package com.monkeysonnet.zipzap;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.monkeysonnet.engine.ButtonActor;
import com.monkeysonnet.engine.ColorTools;

public class TargetArrowActor extends ButtonActor
{
	private static final float ARROW_VISIBILITY_THRESHOLD_LOW = 60 * 60;
	private static final float ARROW_VISIBILITY_THRESHOLD_HIGH = 65 * 65;
	
	private final Color _baseColor = new Color();
	
	public TargetArrowActor(Group g, Color color)
	{
		super(Z.texture("hud-arrow"), null);
		touchable = visible = false;
		
		float radius = g.width / 6f;
		width = height = radius * 2;
		originX = originY = radius;
		x = (g.width - width) / 2f;
		y = (g.height - height) / 2f;
		_baseColor.set(color);
		
		g.addActor(this);
	}
	
	public TargetArrowActor(Stage s, Color color)
	{
		super(Z.texture("hud-arrow"), null);
		touchable = visible = false;
		
		float radius = s.width() / 6f;
		width = height = radius * 2;
		originX = originY = radius;
		x = (s.width() - width) / 2f;
		y = (s.height() - height) / 2f;
		_baseColor.set(color);
		
		s.addActor(this);
	}

	@Override
	public void draw(SpriteBatch batch, float parentAlpha)
	{
		if(Z.sim.target() != null && Z.sim.focalPoint() != null)
		{
			float dst2 = Z.sim.focalPoint().dst2(Z.sim().target().origin());
			
			float a;
			
			if(dst2 <= ARROW_VISIBILITY_THRESHOLD_LOW)
				a = 0;
			else
			{
				rotation = Z.v1().set(Z.sim().target().origin()).sub(Z.sim.focalPoint()).angle();
				
				if(dst2 >=  ARROW_VISIBILITY_THRESHOLD_HIGH)
					a = 0.5f;
				else
				{
					float rel = (dst2 - ARROW_VISIBILITY_THRESHOLD_LOW) / (ARROW_VISIBILITY_THRESHOLD_HIGH - ARROW_VISIBILITY_THRESHOLD_LOW);
					
					a = rel * 0.5f;					
				}
			}
			
			color.set(ColorTools.combineAlpha(_baseColor, a));
			
			super.draw(batch, parentAlpha);
		}
	}

	@Override
	public Actor hit(float x, float y)
	{
		return null;
	}

}
