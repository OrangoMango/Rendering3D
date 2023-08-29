package com.orangomango.blockworld.model;

import java.util.*;

public class World{
	private int seed;
	private boolean superFlat;
	private Random random;
	

	public World(int seed, boolean superFlat){
		this.seed = seed;
		this.superFlat = superFlat;
		this.random = new Random(seed);
	}

	public int getSeed(){
		return this.seed;
	}

	public Random getRandom(){
		return this.random;
	}
}
