package com.orangomango.blockworld.model;

import java.util.*;

import com.orangomango.rendering3d.model.Mesh;

public class Chunk{
	public static final int CHUNK_SIZE = 10;
	
	private Block[][][] blocks = new Block[CHUNK_SIZE][CHUNK_SIZE][CHUNK_SIZE];
	private int x, y, z;
	private final World world;
	
	public Chunk(World world, int x, int y, int z){
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		PerlinNoise noise = new PerlinNoise(world.seed);
		float frequency = 0.1575f;

		for (int i = 0; i < CHUNK_SIZE; i++){ // x
			for (int j = 0; j < CHUNK_SIZE; j++){ // y
				for (int k = 0; k < CHUNK_SIZE; k++){ // z
					if (this.y < 0){
						this.blocks[i][j][k] = null;
					} else {
						float n = (noise.noise((i+this.x*CHUNK_SIZE)*frequency, 0, (k+this.z*CHUNK_SIZE)*frequency)+1)/2;
						int h = Math.round(n*(CHUNK_SIZE-1));
						if (this.y*Chunk.CHUNK_SIZE+j >= h){
							this.blocks[i][j][k] = new Block(this, i, j, k, "stone");
						}
					}
				}
			}
		}
	}
	
	public Block getBlockAt(int x, int y, int z){
		if (x < 0 || y < 0 || z < 0 || x >= Chunk.CHUNK_SIZE || y >= Chunk.CHUNK_SIZE || z >= Chunk.CHUNK_SIZE){
			return null;
		} else {
			return this.blocks[x][y][z];
		}
	}
	
	public void setBlock(Block block, int x, int y, int z){
		this.blocks[x][y][z] = block;
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
	
	public World getWorld(){
		return this.world;
	}
	
	public List<Mesh> getMesh(){
		List<Mesh> output = new ArrayList<>();
		for (int i = 0; i < CHUNK_SIZE; i++){
			for (int j = 0; j < CHUNK_SIZE; j++){
				for (int k = 0; k < CHUNK_SIZE; k++){
					if (this.blocks[i][j][k] != null) output.add(this.blocks[i][j][k].getMesh());
				}
			}
		}
		return output;
	}

	public void setupFaces(){
		for (int i = 0; i < CHUNK_SIZE; i++){ // x
			for (int j = 0; j < CHUNK_SIZE; j++){ // y
				for (int k = 0; k < CHUNK_SIZE; k++){ // z
					if (this.blocks[i][j][k] != null) this.blocks[i][j][k].setupFaces();
				}
			}
		}
	}
}
