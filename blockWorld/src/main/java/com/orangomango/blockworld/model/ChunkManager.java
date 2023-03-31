package com.orangomango.blockworld.model;

import javafx.geometry.Point3D;

import java.util.*;

import com.orangomango.rendering3d.model.MeshGroup;
import com.orangomango.rendering3d.Engine3D;

public class ChunkManager{
	private Player player;
	private World world;
	private Engine3D engine;
	public static final double RENDER_DISTANCE = 3;
	
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
		for (MeshGroup group : toUnload){
			Chunk chunk = this.world.getChunkAt(group.tag);
			unloadChunk(chunk.getX(), chunk.getY(), chunk.getZ());
		}
	}
	
	public void unloadChunk(int x, int y, int z){
		for (MeshGroup group : this.engine.getObjects()){
			if (group.tag.equals(World.getChunkTag(x, y, z))){
				this.engine.getObjects().remove(group);
				this.world.getChunks().remove(group.tag);
				break;
			}
		}
	}
	
	public void loadChunk(int x, int y, int z){
		Chunk chunk = this.world.addChunk(x, y, z);
		System.out.println("Loading "+chunk+"...");
		this.engine.getObjects().add(getMeshGroup(chunk));
	}
	
	private static MeshGroup getMeshGroup(Chunk chunk){
		MeshGroup mgroup = new MeshGroup(chunk.getMesh());
		mgroup.tag = World.getChunkTag(chunk.getX(), chunk.getY(), chunk.getZ());
		mgroup.skipCondition = cam -> {
			Point3D cpos = new Point3D((int)(cam.getX()/Chunk.CHUNK_SIZE), (int)(cam.getY()/Chunk.CHUNK_SIZE), (int)(cam.getZ()/Chunk.CHUNK_SIZE));
			Point3D chunkPos = new Point3D(chunk.getX(), chunk.getY(), chunk.getZ());
			Point3D camDir = new Point3D(Math.cos(cam.getRy()+Math.PI/2), 0, Math.sin(cam.getRy()+Math.PI/2)).normalize();
			return cpos.distance(chunkPos) > RENDER_DISTANCE || chunkPos.subtract(cpos).normalize().dotProduct(camDir) < 0;
		};
		return mgroup;
	}
}
