package com.orangomango.blockworld.model;

import java.util.*;

import com.orangomango.rendering3d.model.Mesh;

public class World{
	private final List<Chunk> chunks = new ArrayList<>();
	public int seed = (int)System.currentTimeMillis();
	private int width, height, depth;
	private int currentDepth = -1;
	
	public World(int w, int h, int d){
		this.width = w;
		this.height = h;
		this.depth = d;
		for (int i = 0; i < w; i++){
			for (int j = 0; j < h; j++){
				for (int k = 0; k < d; k++){
					chunks.add(new Chunk(this, i, j, k));
				}
			}
		}
	}

	public Chunk addChunk(){
		if (this.currentDepth < 0 || this.currentDepth == this.depth){
			if (this.currentDepth >= 0) this.width++;
			this.currentDepth = 0;
		}
		Chunk c = new Chunk(this, this.width, this.height-1, this.currentDepth++);
		chunks.add(c);
		return c;
	}
	
	public Block getBlockAt(int x, int y, int z){
		int chunkX = x / Chunk.CHUNK_SIZE;
		int chunkY = y / Chunk.CHUNK_SIZE;
		int chunkZ = z / Chunk.CHUNK_SIZE;
		Chunk chunk = getChunkAt(chunkX, chunkY, chunkZ);
		if (chunk != null){
			return chunk.getBlockAt(x % Chunk.CHUNK_SIZE, y % Chunk.CHUNK_SIZE, z % Chunk.CHUNK_SIZE);
		} else {
			return null;
		}
	}
	
	public void removeBlockAt(int x, int y, int z){
		int chunkX = x / Chunk.CHUNK_SIZE;
		int chunkY = y / Chunk.CHUNK_SIZE;
		int chunkZ = z / Chunk.CHUNK_SIZE;
		Chunk chunk = getChunkAt(chunkX, chunkY, chunkZ);
		if (chunk != null){
			chunk.setBlock(null, x % Chunk.CHUNK_SIZE, y % Chunk.CHUNK_SIZE, z % Chunk.CHUNK_SIZE);
		}
	}
	
	private Chunk getChunkAt(int x, int y, int z){
		for (Chunk chunk : this.chunks){
			if (chunk.getX() == x && chunk.getY() == y && chunk.getZ() == z){
				return chunk;
			}
		}
		return null;
	}
	
	public List<Mesh> getMesh(){
		List<Mesh> output = new ArrayList<>();
		for (Chunk chunk : this.chunks){
			for (Mesh mesh : chunk.getMesh()){
				output.add(mesh);
			}
		}
		return output;
	}
}
