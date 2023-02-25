package com.orangomango.blockworld.model;

import java.util.*;

import com.orangomango.rendering3d.model.Mesh;

public class Chunk{
	public static final int CHUNK_SIZE = 4;
	
	private Block[][][] blocks = new Block[CHUNK_SIZE][CHUNK_SIZE][CHUNK_SIZE];
	private int x, y;
	private World world;
	
	public Chunk(World world, int x, int y){
		this.world = world;
		this.x = x;
		this.y = y;
		for (int i = 0; i < CHUNK_SIZE; i++){
			for (int j = 0; j < CHUNK_SIZE; j++){
				for (int k = 0; k < CHUNK_SIZE; k++){
					this.blocks[i][j][k] = new Block(this, i, j, k);
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
	
	/*public void setBlock(Block block, int x, int y, int z){
		this.blocks[x][y][z] = block;
	}*/
	
	public int getX(){
		return this.x;
	}
	
	public int getY(){
		return this.y;
	}
	
	public World getWorld(){
		return this.world;
	}
	
	public List<Mesh> getMesh(){
		List<Mesh> output = new ArrayList<>();
		for (int i = 0; i < CHUNK_SIZE; i++){
			for (int j = 0; j < CHUNK_SIZE; j++){
				for (int k = 0; k < CHUNK_SIZE; k++){
					output.add(this.blocks[i][j][k].getMesh());
				}
			}
		}
		return output;
	}
}
