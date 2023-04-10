package com.orangomango.blockworld.model;

import javafx.geometry.Point3D;

import java.util.*;
import java.io.*;

import com.orangomango.rendering3d.model.MeshGroup;
import com.orangomango.rendering3d.Engine3D;

public class ChunkManager{
	private Player player;
	private World world;
	private Engine3D engine;
	public static final double RENDER_DISTANCE = 3.5;
	
	public ChunkManager(World world, Player player){
		this.player = player;
		this.world = world;
		this.engine = Engine3D.getInstance();
	}
	
	public void manage(){
		Point3D playerPos = new Point3D(player.getChunkX(), player.getChunkY(), player.getChunkZ());
		List<MeshGroup> toUnload = new ArrayList<>();
		for (MeshGroup group : this.engine.getObjects()){
			Chunk chunk = this.world.getChunkAt(group.tag);
			if ((new Point3D(chunk.getX(), chunk.getY(), chunk.getZ())).distance(playerPos) > RENDER_DISTANCE){
				toUnload.add(group);
			}
		}
		boolean unloaded = false;
		for (MeshGroup group : toUnload){
			Chunk chunk = this.world.getChunkAt(group.tag);
			unloadChunk(chunk.getX(), chunk.getY(), chunk.getZ());
			unloaded = true;
		}
		if (unloaded){
			for (Chunk chunk : world.getChunks().values()){
				chunk.setupFaces();
			}
		}
	}
	
	public void unloadChunk(int x, int y, int z){
		Chunk chunk = this.world.getChunkAt(x, y, z);
		if (chunk != null){
			saveChunkToFile(chunk);
			for (MeshGroup group : this.engine.getObjects()){
				if (group.tag.equals(World.getChunkTag(x, y, z))){
					this.engine.getObjects().remove(group);
					this.world.getChunks().remove(group.tag);
					break;
				}
			}
		}
	}
	
	public void loadChunk(int x, int y, int z){
		boolean loaded = loadChunkFromFile(x, y, z);
		if (!loaded){
			Chunk chunk = this.world.addChunk(x, y, z);
			this.engine.getObjects().add(getMeshGroup(chunk));
			saveChunkToFile(chunk);
		}
		//System.out.println("Loaded chunk");
	}
	
	public boolean loadChunkFromFile(int x, int y, int z){
		try {
			File dir = new File(System.getProperty("user.home"), ".blockWorld/");
			if (!dir.exists()) dir.mkdir();
			File chunkFile = new File(dir, World.getChunkTag(x, y, z).replace(" ", "_")+".chunk");
			if (!chunkFile.exists()) return false;
			int[][][] chunkData = new int[Chunk.CHUNK_SIZE][Chunk.CHUNK_SIZE][Chunk.CHUNK_SIZE];
			BufferedReader reader = new BufferedReader(new FileReader(chunkFile));
			for (int i = 0; i < Chunk.CHUNK_SIZE; i++){ // z
				for (int j = 0; j < Chunk.CHUNK_SIZE; j++){ // y
					String line = reader.readLine();
					int k = 0;
					for (String piece : line.split(" ")){
						int id = Integer.parseInt(piece);
						chunkData[k++][j][i] = id;
					}
				}
				reader.readLine();
			}
			reader.close();
			Chunk chunk = new Chunk(this.world, x, y, z, chunkData);
			this.world.getChunks().put(World.getChunkTag(x, y, z), chunk);
			Chunk.updateMesh(chunk);
			//System.out.println("Loaded a new chunk from file: "+chunk);
			return true;
		} catch (IOException ex){
			ex.printStackTrace();
			return false;
		}
	}
	
	public void saveChunkToFile(Chunk chunk){
		try {
			File dir = new File(System.getProperty("user.home"), ".blockWorld/");
			if (!dir.exists()) dir.mkdir();
			File chunkFile = new File(dir, World.getChunkTag(chunk.getX(), chunk.getY(), chunk.getZ()).replace(" ", "_")+".chunk");
			if (!chunkFile.exists()) chunkFile.createNewFile();
			BufferedWriter writer = new BufferedWriter(new FileWriter(chunkFile));
			for (int i = 0; i < Chunk.CHUNK_SIZE; i++){ // z
				for (int j = 0; j < Chunk.CHUNK_SIZE; j++){ // y
					for (int k = 0; k < Chunk.CHUNK_SIZE; k++){ // x
						Block block = chunk.getBlockAt(k, j, i);
						writer.write(block == null ? "0 " : block.getId()+" ");
					}
					writer.newLine();
				}
				writer.newLine();
			}
			writer.close();
		} catch (IOException ex){
			ex.printStackTrace();
		}
	}
	
	public static MeshGroup getMeshGroup(Chunk chunk){
		MeshGroup mgroup = new MeshGroup(chunk.getMesh());
		mgroup.tag = World.getChunkTag(chunk.getX(), chunk.getY(), chunk.getZ());
		return mgroup;
	}
}
