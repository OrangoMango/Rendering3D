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

    public Camera getCamera(){
        return this.camera;
    }
}
