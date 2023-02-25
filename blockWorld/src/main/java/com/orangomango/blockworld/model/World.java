package com.orangomango.blockworld.model;

import java.util.*;

import com.orangomango.rendering3d.model.Mesh;

public class World{
	private List<Chunk> chunks = new ArrayList<Chunk>();
	
	public World(){
		chunks.add(new Chunk(this, 0, 0));
	}
	
	public Block getBlockAt(int x, int y, int z){
		int chunkX = x / Chunk.CHUNK_SIZE;
		int chunkY = y / Chunk.CHUNK_SIZE;
		Chunk chunk = getChunkAt(chunkX, chunkY);
		if (chunk != null){
			return chunk.getBlockAt(x % Chunk.CHUNK_SIZE, y % Chunk.CHUNK_SIZE, z % Chunk.CHUNK_SIZE);
		} else {
			return null;
		}
	}
	
	/*public void setBlockAt(int x, int y, int z){
		int chunkX = x / Chunk.CHUNK_SIZE;
		int chunkY = y / Chunk.CHUNK_SIZE;
		Chunk chunk = getChunkAt(chunkX, chunkY);
		if (chunk != null){
			Block block = new Block(chunk, x % Chunk.CHUNK_SIZE, y % Chunk.CHUNK_SIZE, z % Chunk.CHUNK_SIZE);
			chunk.setBlock(block, x % Chunk.CHUNK_SIZE, y % Chunk.CHUNK_SIZE, z % Chunk.CHUNK_SIZE);
		} else {
			return null;
		}
	}*/
	
	private Chunk getChunkAt(int x, int y){
		for (Chunk chunk : this.chunks){
			if (chunk.getX() == x && chunk.getY() == y){
				return chunk;
			}
		}
		return null;
	}
	
	public List<List<Mesh>> getMesh(){
		List<List<Mesh>> output = new ArrayList<>();
		for (Chunk chunk : this.chunks){
			output.add(chunk.getMesh());
		}
		return output;
	}
}
