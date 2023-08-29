package com.orangomango.blockworld.model;

import java.util.Random;
import java.util.Objects;

public class Chunk{
	public static final int CHUNK_SIZE = 4;
	private static final int HEIGHT_LIMIT = 2;

	private World world;
	private int x, y, z;
	private Block[][][] blocks = new Block[CHUNK_SIZE][CHUNK_SIZE][CHUNK_SIZE];

	public Chunk(World world, int x, int y, int z){
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;

		PerlinNoise noise = new PerlinNoise(world.getSeed());
		Random random = world.getRandom();
		float frequency = 0.1575f;
		float biomeFreq = 0.05f;

		// World generation
		for (int i = 0; i < CHUNK_SIZE; i++){ // x
			for (int j = 0; j < CHUNK_SIZE; j++){ // y
				for (int k = 0; k < CHUNK_SIZE; k++){ // z
					if (this.y < HEIGHT_LIMIT){
						this.blocks[i][j][k] = null;
					} else {
						float n = (noise.noise((i+this.x*CHUNK_SIZE)*frequency, 0, (k+this.z*CHUNK_SIZE)*frequency)+1)/2;
						float b = (noise.noise((i+this.x*CHUNK_SIZE)*biomeFreq, 0, (k+this.z*CHUNK_SIZE)*biomeFreq)+1)/2;
						// TODO Replace 16 with CHUNK_SIZE
						int h = Math.round(n*(16-1))+CHUNK_SIZE*HEIGHT_LIMIT; // air column
						if (world.superFlat) h = CHUNK_SIZE*HEIGHT_LIMIT+1;
						int pos = this.y*CHUNK_SIZE+j;
						if (pos >= h){
							String biome = b <= 0.4 || (pos > WATER_HEIGHT && random.nextInt(100) < 35) ? "sand" : (pos == h && pos <= WATER_HEIGHT ? "grass" : "dirt");
							this.blocks[i][j][k] = new Block(this, i, j, k, pos > h+3 ? "stone" : biome);
						}
					}
				}
			}
		}
	}

	public World getWorld(){
		return this.world;
	}

	public int getX(){
		return this.x;
	}

	public int getY(){
		return this.y;
	}

	public int getZ(){
		return this.z;
	}

	/*@Override
	public int hashCode(){
		return Objects.hash(Integer.valueOf(this.x), Integer.valueOf(this.y), Integer.valueOf(this.z))
	}

	@Override
	public boolean equals(Object other){
		if (other instanceof Chunk){
			Chunk chunk = (Chunk)other;
			return this.x == chunk.x && this.y == chunk.y && this.z == chunk.z;
		} else return false;
	}*/
}
