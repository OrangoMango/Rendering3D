package com.orangomango.blockworld.model;

import javafx.geometry.Point3D;

import com.orangomango.rendering3d.model.Camera;

public class Player{
    private double x, y, z;
    private Camera camera;

    public Player(double x, double y, double z){
        this.x = x;
        this.y = y;
        this.z = z;
        this.camera = new Camera(this.x, this.y, this.z);
        this.camera.zNear = 0.1;
        this.camera.zFar = 100;
        this.camera.lookAtCenter();
    }

    public void move(World world, double mx, double my, double mz){
		if (checkCollision(world, (int)(this.x+mx), (int)(this.y+my), (int)(this.z+mz))) return;
        this.x += mx;
        this.y += my;
        this.z += mz;
        this.camera.move(mx, my, mz);
    }
    
    public boolean checkCollision(World world, int px, int py, int pz){
		Block block = world.getBlockAt((int)px, (int)(py+2), (int)pz);
		if (block == null){
			return false;
		}
		Point3D blockMin = new Point3D(block.getX(), block.getY(), block.getZ());
		Point3D blockMax = new Point3D(block.getX(), block.getY(), block.getZ()+1);
		Point3D playerMin = new Point3D(px, py, pz);
		Point3D playerMax = new Point3D(px, py+2, pz+0.5);
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
