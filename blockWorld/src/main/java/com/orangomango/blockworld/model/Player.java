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
        this.camera.zNear = 1;
        this.camera.zFar = 100;
        this.camera.lookAtCenter();
    }

    public void move(double mx, double my, double mz){
        this.x += mx;
        this.y += my;
        this.z += mz;
        this.camera.move(mx, my, mz);
    }

    public double getRx(){
        return this.camera.getRx();
    }

    public double getRy(){
        return this.camera.getRy();
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
