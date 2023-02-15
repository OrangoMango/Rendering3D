package com.orangomango.rendering3d.model;

import javafx.geometry.Point3D;
import javafx.scene.paint.Color;

import static com.orangomango.rendering3d.MainApplication.LIGHT_AVAILABLE;

public class Light{
	private double x, y, z;
	private double rx, ry;
	private Camera camera;
	
	public Light(double x, double y, double z){
		this.x = x;
		this.y = y;
		this.z = z;
		this.camera = new Camera(this.x, this.y, this.z);
	}
	
	public Camera getCamera(){
		return this.camera;
	}
	
	public Point3D getPosition(){
		return new Point3D(this.x, this.y, this.z);
	}
	
	public void setRx(double rx){
		this.rx = rx;
		this.camera.setRx(this.rx);
	}
	
	public double getRx(){
		return this.rx;
	}
	
	public void setRy(double ry){
		this.ry = ry;
		this.camera.setRy(this.ry);
	}
	
	public double getRy(){
		return this.ry;
	}
	
	public void setPos(double x, double y, double z){
		this.x = x;
		this.y = y;
		this.z = z;
		this.camera.setPos(new Point3D(x, y, z));
	}
	
	public void lookAtCenter(){
		//setRx(Math.atan(getPosition().getY()/getPosition().getZ()));
		setRy(Math.atan2(getPosition().getZ(), getPosition().getX())+Math.PI/2);
		this.camera.lookAtCenter();
	}
	
	public double getLightIntensity(Point3D normal, Point3D point){
		double intensity = Math.max(0, 1-point.subtract(getPosition()).magnitude()*0.005);
		double factor = normal.dotProduct(point.subtract(getPosition()).normalize());	
		if (factor < -1) factor = 1;
		else if (factor > 0) factor = 0;
		else factor = Math.abs(factor);
		return factor*intensity;
	}
	
	public static Color getLight(Color color, double factor){
		double red = color.getRed();
		double green = color.getGreen();
		double blue = color.getBlue();
		if (LIGHT_AVAILABLE){
			red = red * factor;
			green = green * factor;
			blue = blue * factor;
		}
		return Color.color(red, green, blue);
	}
}
