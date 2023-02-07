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
	
	private double[][] cache;
	
	public Camera(double x, double y, double z){
		this.cx = x;
		this.cy = y;
		this.cz = z;
	}
	
	public void setPos(Point3D p){
		this.cx = p.getX();
		this.cy = p.getY();
		this.cz = p.getZ();
	}
	
	public void clearDepthBuffer(){
		this.depthBuffer = new double[MainApplication.WIDTH][MainApplication.HEIGHT];
	}
	
	public void clearCache(){
		this.cache = null;
	}
	
	public void move(double x, double y, double z){
		this.cx += x;
		this.cy += y;
		this.cz += z;
	}
	
	public void reset(){
		this.cx = 0;
		this.cy = 0;
		this.cz = 0;
		this.rx = 0;
		this.ry = 0;
	}
	
	public Point3D getPosition(){
		return new Point3D(this.cx, this.cy, this.cz);
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
		this.rx = rx;
	}
	
	public double getRy(){
		return this.ry;
	}
	
	public void setRy(double ry){
		this.ry = ry;
	}
	
	@Override
	public String toString(){
		return String.format("Cx: %.2f Cy: %.2f Cz: %.2f", this.cx, this.cy, this.cz);
	}
	
	/*public double[][] getInverseProjectionMatrix(){
		double aspectRatio = MainApplication.HEIGHT/MainApplication.WIDTH;
		double fov = Math.toRadians(45);
		double zFar = 100;
		double zNear = 1;
		return new double[][]{
			{Math.tan(fov/2)/aspectRatio, 0, 0, 0},
			{0, Math.tan(fov/2), 0, 0},
			{0, 0, 0, 1},
			{0, 0, (zNear-zFar)/(zFar+zNear), 2/(zFar+zNear)}
		};
	}*/
	
	public double[][] getProjectionMatrix(boolean s){
		if (this.cache != null && s) return this.cache;
		double[][] mat = new double[][]{
			{aspectRatio*1/Math.tan(fov/2), 0, 0, 0},
			{0, 1/Math.tan(fov/2), 0, 0},
			{0, 0, 2/(zFar-zNear), -2*zNear/(zFar-zNear)-1},
			{0, 0, 1, 0}
		};
		this.cache = MainApplication.multiply(MainApplication.multiply(MainApplication.getTranslation(-getX(), -getY(), -getZ()), MainApplication.multiply(MainApplication.getRotateX(getRx()), MainApplication.getRotateY(getRy()))), mat);
		return this.cache;
	}
}
