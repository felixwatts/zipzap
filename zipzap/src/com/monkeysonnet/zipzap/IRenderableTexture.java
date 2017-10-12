package com.monkeysonnet.zipzap;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public interface IRenderableTexture
{
	float radius();
	Vector2 origin();
	TextureRegion texture();
	Color color();
	float angle();
}
