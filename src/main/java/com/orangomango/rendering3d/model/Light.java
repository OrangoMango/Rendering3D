package com.orangomango.rendering3d.model;

import javafx.geometry.Point3D;
import javafx.scene.paint.Color;

import static com.orangomango.rendering3d.MainApplication.LIGHT_AVAILABLE;

public class Light{
	private double x, y, z;
	
	public Light(double x, double y, double z){
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Light(Point3D pos){
		this.x = pos.getX();
		this.y = pos.getY();
		this.z = pos.getZ();
	}
	
	public Point3D getPosition(){
		return new Point3D(this.x, this.y, this.z);
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