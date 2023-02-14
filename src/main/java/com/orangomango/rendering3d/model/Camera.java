package com.orangomango.rendering3d.model;

import javafx.geometry.Point3D;

import com.orangomango.rendering3d.MainApplication;

public class Camera{
	private double cx, cy, cz, rx, ry;
	public double[][] depthBuffer = new double[MainApplication.WIDTH][MainApplication.HEIGHT];
	
	public double aspectRatio = MainApplication.HEIGHT/MainApplication.WIDTH;
	public double fov = Math.toRadians(45);
	public double zFar = 100;
	public double zNear = 1;
	
	private double[][] savedMatrix = null;
	public boolean stateChanged = true;
	
	public Camera(double x, double y, double z){
		this.cx = x;
		this.cy = y;
		this.cz = z;
	}
	
	public void setPos(Point3D p){
		this.stateChanged = true;
		this.cx = p.getX();
		this.cy = p.getY();
		this.cz = p.getZ();
	}
	
	public void clearDepthBuffer(){
		this.depthBuffer = new double[MainApplication.WIDTH][MainApplication.HEIGHT];
	}
	
	public void move(double x, double y, double z){
		this.stateChanged = true;
		this.cx += x;
		this.cy += y;
		this.cz += z;
	}
	
	public void reset(){
		this.stateChanged = true;
		this.cx = 0;
		this.cy = 0;
		this.cz = 0;
		this.rx = 0;
		this.ry = 0;
	}
	
	public void lookAtCenter(){
		setRx(Math.atan(getY()/getZ()));
		setRy(Math.atan2(getZ(), getX())+Math.PI/2);
	}
	
	public double getX(){
		return this.cx;
	}
	
	public double getY(){
		return this.cy;
	}
	
	public double getZ(){
		return this.cz;
	}
	
	public double getRx(){
		return this.rx;
	}
	
	public void setRx(double rx){
		if (this.rx != rx) this.stateChanged = true;
		this.rx = rx;
	}
	
	public double getRy(){
		return this.ry;
	}
	
	public void setRy(double ry){
		if (this.ry != ry) this.stateChanged = true;
		this.ry = ry;
	}
	
	@Override
	public String toString(){
		return String.format("Cx: %.2f Cy: %.2f Cz: %.2f | Rx: %.2f Ry: %.2f", this.cx, this.cy, this.cz, this.rx, this.ry);
	}
	
	public double[][] getCompleteMatrix(){
		if (this.stateChanged){
			this.savedMatrix = MainApplication.multiply(MainApplication.multiply(MainApplication.getTranslation(-getX(), -getY(), -getZ()), 
				MainApplication.multiply(MainApplication.getRotateX(getRx()), MainApplication.getRotateY(getRy()))), getProjectionMatrix());
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
