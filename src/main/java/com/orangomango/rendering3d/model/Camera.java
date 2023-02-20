package com.orangomango.rendering3d.model;

import javafx.geometry.Point3D;

import com.orangomango.rendering3d.Engine3D;

public class Camera{
	private double cx, cy, cz, rx, ry;
	public double[][] depthBuffer = new double[Engine3D.getInstance().getWidth()][Engine3D.getInstance().getHeight()];
	
	public double aspectRatio = (double)Engine3D.getInstance().getHeight()/Engine3D.getInstance().getWidth();
	public double fov = Math.toRadians(45);
	public double zFar = 100;
	public double zNear = 0.1;
	
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
		this.depthBuffer = new double[Engine3D.getInstance().getWidth()][Engine3D.getInstance().getHeight()];
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
		//setRx(Math.atan(getY()/getZ()));
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
		this.rx = Math.max(-Math.PI/2, Math.min(Math.PI/2, rx));
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
			this.savedMatrix = Engine3D.multiply(Engine3D.multiply(Engine3D.getTranslation(-getX(), -getY(), -getZ()), 
				Engine3D.multiply(Engine3D.getRotateY(getRy()), Engine3D.getRotateX(getRx()))), getProjectionMatrix());
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
