package com.orangomango.rendering3d.model;

import javafx.geometry.Point3D;
import javafx.scene.paint.Color;

import com.orangomango.rendering3d.Engine3D;

public class Light{
	private Camera camera;
	private double fixed = -1;

	private static final double AMBIENT_LIGHT = 0.08;

	public Light(Camera camera){
		this.camera = camera;
	}

	public Light(double intensity){
		this.fixed = intensity;
	}

	public void setFixedIntensity(double intensity){
		this.camera = null;
		this.fixed = intensity;
	}

	public Camera getCamera(){
		return this.camera;
	}

	public double getLightIntensity(Point3D normal, Point3D point){
		if (Engine3D.LIGHT_AVAILABLE){
			double intensity = 1;
			double factor = 0;
			
			if (this.fixed >= 0){
				double nx = Math.abs(normal.getX());
				double ny = Math.abs(normal.getY());
				double nz = Math.abs(normal.getZ());
				if (nx != 0) factor = 0.4*this.fixed;
				else if (ny != 0) factor = this.fixed;
				else if (nz != 0) factor = 0.7*this.fixed;
				else System.out.println("Normal: "+normal);
			} else {
				factor = normal.dotProduct(point.subtract(this.camera.getPosition()).normalize());
				if (factor < -1) factor = 1;
				else if (factor > 0) factor = 0;
				else factor = Math.abs(factor);
			}
			
			return factor*intensity+AMBIENT_LIGHT;
		} else {
			return 1;
		}
	}

	public static Color getLight(Color color, double factor){
		double red = color.getRed();
		double green = color.getGreen();
		double blue = color.getBlue();
		red = red * factor;
		green = green * factor;
		blue = blue * factor;

		// R G or B could be a little bit over 1 (1.00000000002)
		return Color.color(Math.min(1, red), Math.min(1, green), Math.min(1, blue), color.getOpacity());
	}
}
