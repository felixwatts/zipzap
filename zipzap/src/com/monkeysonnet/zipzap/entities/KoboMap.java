package com.monkeysonnet.zipzap.entities;

import com.monkeysonnet.engine.Game;

public class KoboMap
{
	public static final int SITE_MAX = 1024;
	public static final int MAP_SIZEX = 17;
	public static final int MAP_SIZEY = 17;

	public static final int WALL = 1;

	// Tile state bits
	public static final int D_MASK = (1<<0);
	public static final int R_MASK = (1<<1);
	public static final int U_MASK = (1<<2);
	public static final int L_MASK = (1<<3);
	public static final int CORE = (1<<4);
	public static final int HIT_MASK = (CORE | U_MASK | R_MASK | D_MASK | L_MASK);
	public static final int HARD = (1<<5);
	public static final int SPACE = (1<<6);
	public static int MAP_BITS(int x) { return	((x) & 0xff); }

	public static boolean IS_SPACE(int x) { return	((x) & SPACE) != 0; }
	
	protected int sitex[];
	protected int sitey[];
	protected int site_max;
	protected int[][] data;
	
	public KoboMap()
	{
		sitex = new int[SITE_MAX];
		sitey = new int[SITE_MAX];
		
		data = new int[MAP_SIZEX][];
		for(int n = 0; n < MAP_SIZEX; n++)
			data[n] = new int[MAP_SIZEY];
	}
	
	void init()
	{
		int i, j;
		for(i = 0; i < MAP_SIZEX; i++)
			for(j = 0; j < MAP_SIZEY; j++)
				data[i][j] = SPACE;
	}

	public void make_maze(int x, int y, int difx, int dify)
	{
		int i, j;
		int vx, vy;

		/* clear */
		for(i = x - difx; i <= x + difx; i++)
			for(j = y - dify; j <= y + dify; j++)
				data[i][j] = SPACE;

		/* push initial sites */
		site_max = 0;
		if(Game.Dice.nextInt(1 << 8) < 128)
		{
			data[x][y] = CORE | R_MASK | L_MASK;
			maze_push(x - 1, y);
			maze_push(x + 1, y);
		}
		else
		{
			data[x][y] = CORE | U_MASK | D_MASK;
			maze_push(x, y - 1);
			maze_push(x, y + 1);
		}

		for(;;)
		{
			/* get one */
			if(maze_pop() != 0)
				break;
			vx = sitex[site_max];
			vy = sitey[site_max];

			int dirs[] = new int[4];
			for(i = 0; i < 4; i++)
				dirs[i] = 0;
			int dirs_max = 0;
			if(maze_judge(x, y, difx, dify, vx + 2, vy + 0) != 0)
				dirs[dirs_max++] = 1;
			if(maze_judge(x, y, difx, dify, vx + 0, vy + 2) != 0)
				dirs[dirs_max++] = 2;
			if(maze_judge(x, y, difx, dify, vx - 2, vy + 0) != 0)
				dirs[dirs_max++] = 3;
			if(maze_judge(x, y, difx, dify, vx + 0, vy - 2) != 0)
				dirs[dirs_max++] = 4;
			if(dirs_max == 0)
				continue;	/* there are no places to go */
			i = Game.Dice.nextInt(dirs_max);
			maze_move_and_push(vx, vy, dirs[i]);
			maze_push(vx, vy);
		}
	}

	private int maze_pop()
	{
		if(site_max == 0)
			return 1;
		int i = Game.Dice.nextInt(site_max);
		site_max--;
		if(i != site_max)
		{
			int tmpx = sitex[site_max];
			int tmpy = sitey[site_max];
			sitex[site_max] = sitex[i];
			sitey[site_max] = sitey[i];
			sitex[i] = tmpx;
			sitey[i] = tmpy;
		}
		return 0;
	}

	private void maze_push(int x, int y)
	{
		sitex[site_max] = x;
		sitey[site_max++] = y;
		data[x][y] = WALL;
	}

	private void maze_move_and_push(int x, int y, int d)
	{
		int x1 = x;
		int y1 = y;
		switch (d)
		{
		  case 1:
		  {
			x1 += 2;
			break;
		  }
		  case 2:
		  {
			y1 += 2;
			break;
		  }
		  case 3:
		  {
			x1 -= 2;
			break;
		  }
		  case 4:
		  {
			y1 -= 2;
			break;
		  }
		}
		maze_push(x1, y1);
		data[(x + x1) / 2][(y + y1) / 2] = WALL;
	}

	int maze_judge(int cx, int cy, int dx, int dy, int x, int y)
	{
		if((x < cx - dx) || (x > cx + dx) || (y < cy - dy)
				|| (y > cy + dy))
			return 0;
		if(data[x][y] == WALL)
			return 0;
		return 1;
	}

	// Translate map "tile state bits" into tile index
	//
	// The hard end nodes need some special treatment here. There are 16 direction
	// mask combinations, but only 4 are valid in this case. So, we use a 16 x 2
	// bit lookup "table" (packed into an 'int') to get the right tile index.
	//
//		    00  01  10  11 <-- mask bits 1 (right) and 0 (up)
//		-----------------------------------------------------
//		00  --  00  01  --
//		01  10  --  --  --      <-- Tile indices
//		10  11  --  --  --
//		11  --  --  --  --
//		 ^
//		 '- mask bits 3 (left) and 2 (down)
	//
	// Thus, our table is 00000000 00000011 00000010 00010000 == 0x00030210
	//
	private static int bits2tile(int n)
	{
		if((n & CORE) != 0)		// Core
			return ((n & (U_MASK | D_MASK)) != 0) ? 6 : 7;
//		else if((n & HARD) != 0)	// One of the 4 indestructible end nodes
//			return (0x00030210 >> ((n & 15) << 1)) & 3;
//		else if(n == 5)		// Vertical pipe
//			return Game.Dice.nextBoolean() ? 13 : 4;
//		else if(n == 10)	// Vertical pipe
//			return Game.Dice.nextBoolean() ? 18 : 5;
		else			// Other pipe parts or normal end nodes
			return n + 8;
	}

	public int[][] convert(int ratio)
	{
		int i, j;
		int p = 0;
		for(i = 0; i < MAP_SIZEX; i++)
			for(j = 0; j < MAP_SIZEY; j++)
			{
				p = data[i][j] & CORE;
				if(IS_SPACE(data[i][j]))
				{
					clearpos(i, j);
					continue;
				}
				if((j > 0) && !IS_SPACE(data[i][j-1]))
					p |= U_MASK;
				if((i < MAP_SIZEX - 1) && !IS_SPACE(data[i+1][j]))
					p |= R_MASK;
				if((j < MAP_SIZEY - 1) && !IS_SPACE(data[i][j+1]))
					p |= D_MASK;
				if((i > 0) && !IS_SPACE(data[i-1][j]))
					p |= L_MASK;
				if((p == U_MASK) || (p == R_MASK) || (p == D_MASK)
						|| (p == L_MASK))
				{
					if(Game.Dice.nextInt(1 << 8) < ratio)
						p |= HARD;
				}
				data[i][j] = (bits2tile(p) << 8) | p;
			}
		
		return data;
	}


	void clearpos(int x, int y)
	{
		data[x][y] = (8 << 8) | SPACE;
	}
}
