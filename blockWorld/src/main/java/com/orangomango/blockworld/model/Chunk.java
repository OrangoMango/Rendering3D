package com.orangomango.blockworld.model;

import java.util.*;

import com.orangomango.rendering3d.model.Mesh;

public class Chunk{
	public static final int CHUNK_SIZE = 16;
	
	private Block[][][] blocks = new Block[CHUNK_SIZE][CHUNK_SIZE][CHUNK_SIZE];
	private int x, y, z;
	private final World world;
	
	public Chunk(World world, int x, int y, int z){
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		PerlinNoise noise = new PerlinNoise(world.seed);
		float frequency = 0.125f;

		for (int i = 0; i < CHUNK_SIZE; i++){ // x
			for (int j = 0; j < CHUNK_SIZE; j++){ // z
				for (int k = 0; k < CHUNK_SIZE; k++){ // y
					float n = (noise.noise((i+this.x)*frequency, (j+this.z)*frequency, 0)+1)/2;
					int h = Math.round(n*(CHUNK_SIZE-1));
					if (k >= h){
						this.blocks[i][k][j] = new Block(this, i, k, j);
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
}
