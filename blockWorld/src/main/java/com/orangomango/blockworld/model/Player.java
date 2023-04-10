package com.orangomango.blockworld.model;

import javafx.geometry.Point3D;

import com.orangomango.rendering3d.model.Camera;

public class Player{
	private double x, y, z;
	private Camera camera;
	private Point3D lastChunkPosition;

	public Player(double x, double y, double z){
		this.x = x;
		this.y = y;
		this.z = z;
		this.camera = new Camera(this.x, this.y, this.z);
		this.camera.zNear = 0.1;
		this.camera.fov = Math.PI/2;
		this.camera.zFar = 75;
		this.lastChunkPosition = new Point3D(getChunkX(), getChunkY(), getChunkZ());
		
		// TODO Make distanceToPlain private
		int i = 0;
		for (Point3D[] ps : this.camera.getViewFrustum()){
			System.out.println(java.util.Arrays.toString(ps));
			//if (i == 0){
				//System.out.println(com.orangomango.rendering3d.model.Mesh.distanceToPlane(ps[1], ps[0], new Point3D(6, 8, 18), ps[1].multiply(-1)));
			//}
			i++;
		}
		//System.exit(0);
		
		this.camera.lookAtCenter();
	}

	public void move(World world, double mx, double my, double mz){
		//if (checkCollision(world, this.x, this.y, this.z, mx, my, mz)) return;
		this.lastChunkPosition = new Point3D(getChunkX(), getChunkY(), getChunkZ());
		this.x += mx;
		this.y += my;
		this.z += mz;
		this.camera.move(mx, my, mz);
	}
	
	public void runOnChunkChanged(Runnable r){
		if (getChunkX() != lastChunkPosition.getX() || getChunkY() != lastChunkPosition.getY() || getChunkZ() != lastChunkPosition.getZ()){
			r.run();
		}
	}
	
	// AABB
	private boolean checkCollision(World world, double px, double py, double pz, double mx, double my, double mz){
		// z+ z- x+ x- y- y+
		final int[][] blocks = new int[][]{{0, 0, 1}, {0, 1, 1}, {0, 0, -1}, {0, 1, -1}, {1, 0, 0}, {1, 1, 0}, {-1, 0, 0}, {-1, 1, 0}, {0, -1, 0}, {0, 2, 0}};
		final boolean[] conditions = new boolean[]{mz <= 0, mz <= 0, mz >= 0, mz >= 0, mx <= 0, mx <= 0, mx >= 0, mx >= 0, my >= 0, my <= 0};
		for (int i = 0; i < blocks.length; i++){
			if (conditions[i]) continue; // skip conditions
			int[] bl = blocks[i];
			boolean col = checkBlockCollision(world, (int)(px+mx), (int)(py+my), (int)(pz+mz), bl[0], bl[1], bl[2]);
			if (col) return true;
		}
		return false;
	}
	
	private boolean checkBlockCollision(World world, int px, int py, int pz, int bx, int by, int bz){
		Block block = world.getBlockAt((int)(px+bx), (int)(py+by), (int)(pz+bz));
		if (block == null){
			return false;
		}
		Point3D blockMin = new Point3D(block.getX(), block.getY(), block.getZ());
		Point3D blockMax = new Point3D(block.getX()+1, block.getY()+1, block.getZ()+1);
		Point3D playerMin = new Point3D(px, py, pz);
		Point3D playerMax = new Point3D(px+1, py+2, pz+1);
		if (playerMax.getX() < blockMin.getX() || playerMin.getX() > blockMax.getX()) return false;
		if (playerMax.getY() < blockMin.getY() || playerMin.getY() > blockMax.getY()) return false;
		if (playerMax.getZ() < blockMin.getZ() || playerMin.getZ() > blockMax.getZ()) return false;
		return true;
	}
	
	public void reset(){
		this.x = 0;
		this.y = 0;
		this.z = 0;
		this.camera.reset();
	}

	public double getRx(){
		return this.camera.getRx();
	}

	public double getRy(){
		return this.camera.getRy();
	}

	public double getX(){
		return this.x;
	}

	public double getY(){
		return this.y;
	}

	public double getZ(){
		return this.z;
	}

	public int getChunkX(){
		return (int)(getX()/Chunk.CHUNK_SIZE);
	}

	public int getChunkY(){
		return (int)(getY()/Chunk.CHUNK_SIZE);
	}

	public int getChunkZ(){
		return (int)(getZ()/Chunk.CHUNK_SIZE);
	}

	public Camera getCamera(){
		return this.camera;
	}

}
