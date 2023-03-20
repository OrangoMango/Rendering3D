package com.orangomango.rendering3d.model;

import javafx.geometry.Point3D;

import com.orangomango.rendering3d.Engine3D;

public class Camera{
	private double cx, cy, cz, rx, ry;
	public double[][] depthBuffer = new double[Engine3D.getInstance().getWidth()][Engine3D.getInstance().getHeight()];
	
	public double aspectRatio = (double)Engine3D.getInstance().getHeight()/Engine3D.getInstance().getWidth();
	public double fov = Math.toRadians(45);
	public double zFar = 100;
	public double zNear = 0.3;
	
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
		//this.depthBuffer = new double[Engine3D.getInstance().getWidth()][Engine3D.getInstance().getHeight()];
		for (int i = 0; i < Engine3D.getInstance().getWidth(); i++){
			for (int j = 0; j < Engine3D.getInstance().getHeight(); j++){
				this.depthBuffer[i][j] = 0;
			}
		}
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
	
	public double[][] getViewMatrix(){
		if (this.stateChanged){
			this.savedMatrix = Engine3D.multiply(Engine3D.getTranslation(-getX(), -getY(), -getZ()),
				Engine3D.multiply(Engine3D.getRotateY(-getRy()), Engine3D.getRotateX(-getRx())));

			/*double alpha = -getRx();
			double beta = -getRy();
			double tx = -getX(), ty = -getY(), tz = -getZ();

			this.savedMatrix = new double[][]{
				{aspectRatio*Math.cos(beta)/Math.tan(fov/2), 0, -aspectRatio*Math.sin(beta)/Math.tan(fov/2), aspectRatio*(Math.cos(beta)*tx-Math.sin(beta)*tz)/Math.tan(fov/2)},
					{-Math.sin(beta)*Math.sin(alpha)/Math.tan(fov/2), Math.cos(alpha)/Math.tan(fov/2), -Math.sin(alpha)*Math.cos(beta)/Math.tan(fov/2), (-Math.sin(beta)*Math.sin(alpha)*tx+Math.cos(alpha)*ty-Math.sin(alpha)*Math.cos(beta)*tz)/Math.tan(fov/2)},
					{2*Math.sin(beta)*Math.cos(alpha)/(zFar-zNear), 2*Math.sin(alpha)/(zFar-zNear), 2*Math.cos(alpha)*Math.cos(beta)/(zFar-zNear), 2*(Math.sin(beta)*Math.cos(alpha)*tx+Math.sin(alpha)*ty+Math.cos(alpha)*Math.cos(beta)*tz)/(zFar-zNear)-2*zNear/(zFar-zNear)-1},
					{Math.sin(beta)*Math.cos(alpha), Math.sin(alpha), Math.cos(alpha)*Math.cos(beta), Math.sin(beta)*Math.cos(alpha)*tx+Math.sin(alpha)*ty, Math.cos(alpha)*Math.cos(beta)*tz}
			};*/

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
