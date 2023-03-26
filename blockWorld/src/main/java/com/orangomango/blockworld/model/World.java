package com.orangomango.blockworld.model;

import java.util.*;

import com.orangomango.rendering3d.model.Mesh;

public class World{
	private final Map<String, Chunk> chunks = new HashMap<>();
	private int seed;
	private Random random;

	public World(int seed){
		this.seed = seed;
		this.random = new Random(seed);
	}

	public Random getRandom(){
		return this.random;
	}

	public int getSeed(){
		return this.seed;
	}

	public Chunk addChunk(int x, int y, int z){
		Chunk c = new Chunk(this, x, y, z);
		chunks.put(c.getTag(), c);
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

	public Map<String, Chunk> getChunks(){
		return this.chunks;
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

	public void setBlockAt(int x, int y, int z, String type){
		int chunkX = x / Chunk.CHUNK_SIZE;
		int chunkY = y / Chunk.CHUNK_SIZE;
		int chunkZ = z / Chunk.CHUNK_SIZE;
		Chunk chunk = getChunkAt(chunkX, chunkY, chunkZ);
		if (chunk != null){
			int blockX = x % Chunk.CHUNK_SIZE;
			int blockY = y % Chunk.CHUNK_SIZE;
			int blockZ = z % Chunk.CHUNK_SIZE;
			chunk.setBlock(new Block(chunk, blockX, blockY, blockZ, type), blockX, blockY, blockZ);
		}
	}

	public void clearChunks(){
		this.chunks.clear();
	}
	
	public Chunk getChunkAt(int x, int y, int z){
		return getChunkAt(String.format("%d %d %d", x, y, z));
	}
	
	public Chunk getChunkAt(String tag){
		return this.chunks.get(tag);
	}
	
	public List<List<Mesh>> getMesh(){
		List<List<Mesh>> output = new ArrayList<>();
		for (Chunk chunk : this.chunks.values()){
			output.add(chunk.getMesh());
		}
		return output;
	}
}
