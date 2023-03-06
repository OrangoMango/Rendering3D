package com.orangomango.blockworld.model;

import java.util.*;

import com.orangomango.rendering3d.model.Mesh;

public class World{
	private final List<Chunk> chunks = new ArrayList<>();
	public int seed = (int)System.currentTimeMillis();

	public Chunk addChunk(int x, int y, int z){
		Chunk c = new Chunk(this, x, y, z);
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

	public void clearChunks(){
		this.chunks.clear();
	}
	
	public Chunk getChunkAt(int x, int y, int z){
		for (Chunk chunk : this.chunks){
			if (chunk.getX() == x && chunk.getY() == y && chunk.getZ() == z){
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
