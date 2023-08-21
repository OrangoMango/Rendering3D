package com.orangomango.rendering3d.model;

import javafx.geometry.Point3D;

import com.orangomango.rendering3d.Engine3D;

public class Camera{
	private Point3D position;
	private double rx, ry;
	public double[][] depthBuffer;
	private double aspectRatio, fov, zFar, zNear;
	private double width, height;
	private boolean stateChanged = true;
	private double[][] savedMatrix = null;

	public Camera(Point3D pos, int w, int h, double fov, double zFar, double zNear){
		this.position = pos;
		this.width = w;
		this.height = h;
		this.depthBuffer = new double[w][h];
		this.aspectRatio = (double)h/w;
		this.fov = fov;
		this.zNear = zNear;
		this.zFar = zFar;
	}

	public double getWidth(){
		return this.width;
	}

	public double getHeight(){
		return this.height;
	}

	public void setPos(Point3D p){
		this.stateChanged = true;
		this.position = p;
	}

	public Point3D getPosition(){
		return this.position;
	}

	public void move(Point3D m){
		this.stateChanged = true;
		this.position = this.position.add(m);
	}

	public void reset(){
		this.stateChanged = true;
		this.position = new Point3D(0, 0, 0);
		this.rx = 0;
		this.ry = 0;
	}

	public void clearDepthBuffer(){
		for (int i = 0; i < this.depthBuffer.length; i++){
			for (int j = 0; j < this.depthBuffer[i].length; j++){
				this.depthBuffer[i][j] = 0;
			}
		}
	}

	public void lookAtCenter(){
		setRx(Math.atan(this.position.getY()/this.position.getZ()));
		setRy(Math.atan2(this.position.getZ(), this.position.getX())+Math.PI/2);
	}

	public double getRx(){
		return this.rx;
	}
	
	public void setRx(double rx){
		if (this.rx != rx) this.stateChanged = true;
		this.rx = Math.max(-Math.PI/2, Math.min(Math.PI/2, rx));
	}
	
	public double getRy(){
		return this.ry;
	}
	
	public void setRy(double ry){
		if (this.ry != ry) this.stateChanged = true;
		this.ry = ry;
	}

	public double[][] getViewMatrix(){
		if (this.stateChanged){
			this.savedMatrix = Engine3D.multiply(Engine3D.getTranslation(-this.position.getX(), -this.position.getY(), -this.position.getZ()),
				Engine3D.multiply(Engine3D.getRotateY(-getRy()), Engine3D.getRotateX(-getRx())));
			this.stateChanged = false;
		}
		return this.savedMatrix;
	}

	public double[][] getProjectionMatrix(){
		return new double[][]{
			{aspectRatio*1/Math.tan(fov/2), 0, 0, 0},
			{0, 1/Math.tan(fov/2), 0, 0},
			{0, 0, 2/(zFar-zNear), -2*zNear/(zFar-zNear)-1},
			{0, 0, 1, 0}
		};
	}
}
