package com.orangomango.blockworld.model;

import com.orangomango.rendering3d.model.Camera;

public class Player{
    private double x, y, z;
    private Camera camera;

    public Player(double x, double y, double z){
        this.x = x;
        this.y = y;
        this.z = z;
        this.camera = new Camera(this.x, this.y, this.z);
    }

    public double getX(){
        return this.camera.getX();
    }

    public double getY(){
        return this.camera.getY();
    }

    public double getZ(){
        return this.camera.getZ();
    }

    public int getChunkX(){
        return (int)Math.floor(getX()/Chunk.CHUNK_SIZE);
    }

    public int getChunkY(){
        return (int)Math.floor(getY()/Chunk.CHUNK_SIZE);
    }

    public int getChunkZ(){
        return (int)Math.floor(getZ()/Chunk.CHUNK_SIZE);
    }

    public Camera getCamera(){
        return this.camera;
    }
}
