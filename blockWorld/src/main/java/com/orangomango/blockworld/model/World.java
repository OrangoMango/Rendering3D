package com.orangomango.blockworld.model;

import java.util.*;

public class World{
	private int seed;
	private boolean superFlat;
	private Random random;
	private HashMap<Chunk.ChunkPosition, Chunk> chunks = new HashMap<>();

	public World(int seed, boolean superFlat){
		this.seed = seed;
		System.out.println("Seed: "+seed);
		this.superFlat = superFlat;
		this.random = new Random(seed);
	}

	public Chunk addChunk(int x, int y, int z){
		Chunk.ChunkPosition pos = new Chunk.ChunkPosition(x, y, z);
		Chunk chunk = new Chunk(this, pos);
		this.chunks.put(pos, chunk);
		return chunk;
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

	public void setupFaces(){
		for (Chunk chunk : this.chunks.values()){
			chunk.setupFaces();
		}
	}

	public Chunk getChunkAt(int x, int y, int z){
		Chunk.ChunkPosition pos = new Chunk.ChunkPosition(x, y, z);
		return this.chunks.getOrDefault(pos, null);
	}

	public int getSeed(){
		return this.seed;
	}

	public Random getRandom(){
		return this.random;
	}

	public boolean isSuperFlat(){
		return this.superFlat;
	}
}
