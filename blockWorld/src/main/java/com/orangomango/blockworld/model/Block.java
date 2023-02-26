package com.orangomango.blockworld.model;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;

import java.util.Random;

import com.orangomango.rendering3d.model.Mesh;
import com.orangomango.blockworld.MainApplication;

public class Block{
	private int x, y, z;
	private Chunk chunk;
	
	public Block(Chunk chunk, int x, int y, int z){
		this.chunk = chunk;
		this.x = x+chunk.getX()*Chunk.CHUNK_SIZE;
		this.y = y+chunk.getY()*Chunk.CHUNK_SIZE;
		this.z = z+chunk.getZ()*Chunk.CHUNK_SIZE;
	}
	
	/**
	 * Block is declared like this:
	 * F F R R B B L L D D U  U
	 * 0 1 2 3 4 5 6 7 8 9 10 11
	 */
	public Mesh getMesh(){
		Random random = new Random();
		Mesh mesh = new Mesh(switch(random.nextInt(3)){
			case 0 -> MainApplication.COAL_IMAGE;
			case 1 -> MainApplication.DIRT_IMAGE;
			case 2 -> MainApplication.STONE_IMAGE;
			default -> null;
		}, new Point3D[]{
			new Point3D(this.x, this.y, this.z), new Point3D(this.x, 1+this.y, this.z), new Point3D(1+this.x, 1+this.y, this.z),
			new Point3D(1+this.x, this.y, this.z), new Point3D(this.x, this.y, 1+this.z), new Point3D(this.x, 1+this.y, 1+this.z),
			new Point3D(1+this.x, 1+this.y, 1+this.z), new Point3D(1+this.x, this.y, 1+this.z)}, new int[][]{
				{0, 1, 2}, {0, 2, 3}, {3, 2, 6},
				{3, 6, 7}, {7, 6, 5}, {7, 5, 4},
				{4, 5, 1}, {4, 1, 0}, {1, 5, 6},
				{1, 6, 2}, {4, 0, 3}, {4, 3, 7}
		}, new Point2D[]{
			new Point2D(0, 1), new Point2D(0, 0), new Point2D(1, 0), new Point2D(1, 1)
		}, new int[][]{
			{0, 1, 2}, {0, 2, 3}, {0, 1, 2}, {0, 2, 3},
			{0, 1, 2}, {0, 2, 3}, {0, 1, 2}, {0, 2, 3},
			{0, 1, 2}, {0, 2, 3}, {0, 1, 2}, {0, 2, 3},
			{0, 1, 2}, {0, 2, 3}, {0, 1, 2}, {0, 2, 3}
		}, null, null, null);
		
		if (this.chunk.getWorld().getBlockAt(this.x+1, this.y, this.z) != null){
			mesh.hiddenTriangles.add(Integer.valueOf(2));
			mesh.hiddenTriangles.add(Integer.valueOf(3));
		}
		if (this.chunk.getWorld().getBlockAt(this.x, this.y+1, this.z) != null){
			mesh.hiddenTriangles.add(Integer.valueOf(8));
			mesh.hiddenTriangles.add(Integer.valueOf(9));
		}
		if (this.chunk.getWorld().getBlockAt(this.x, this.y, this.z+1) != null){
			mesh.hiddenTriangles.add(Integer.valueOf(4));
			mesh.hiddenTriangles.add(Integer.valueOf(5));
		}
		if (this.chunk.getWorld().getBlockAt(this.x-1, this.y, this.z) != null){
			mesh.hiddenTriangles.add(Integer.valueOf(6));
			mesh.hiddenTriangles.add(Integer.valueOf(7));
		}
		if (this.chunk.getWorld().getBlockAt(this.x, this.y-1, this.z) != null){
			mesh.hiddenTriangles.add(Integer.valueOf(10));
			mesh.hiddenTriangles.add(Integer.valueOf(11));
		}
		if (this.chunk.getWorld().getBlockAt(this.x, this.y, this.z-1) != null){
			mesh.hiddenTriangles.add(Integer.valueOf(0));
			mesh.hiddenTriangles.add(Integer.valueOf(1));
		}
		return mesh;
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
}
