package com.monkeysonnet.zipzap;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

public interface IRenderableMultiPolygon
{
	int getNumPolys();
	float angle(int poly);
	Vector2 origin(int poly);
	Vector2[] verts(int poly);
	Color color(int poly);
	float lineWidth(int poly);
	boolean isLoop(int poly);
	float clipRadius();
}
