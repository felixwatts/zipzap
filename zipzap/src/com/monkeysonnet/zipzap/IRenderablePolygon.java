package com.monkeysonnet.zipzap;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

public interface IRenderablePolygon
{
	float angle();
	Vector2 origin();
	Vector2[] verts();
	Color color();
	float lineWidth();
	float clipRadius();
}
