package com.orangomango.blockworld.model;

import javafx.geometry.Point3D;

import com.orangomango.blockworld.MainApplication;
import com.orangomango.rendering3d.model.Camera;

public class Player{
	private Camera camera;

	public Player(double x, double y, double z, int w, int h){
		this.camera = new Camera(new Point3D(x, y, z), w, h, Math.PI/2, 75, 0.1);
	}

	public Camera getCamera(){
		return this.camera;
	}

	public Point3D getPosition(){
		return this.camera.getPosition();
	}

	public double getRx(){
		return this.camera.getRx();
	}

	public double getRy(){
		return this.camera.getRy();
	}

	public void move(double x, double y, double z){
		this.camera.move(new Point3D(x, y, z));
	}
}