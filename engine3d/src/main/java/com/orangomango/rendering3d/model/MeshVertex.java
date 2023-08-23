package com.orangomango.rendering3d.model;

import javafx.geometry.Point3D;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import com.orangomango.rendering3d.Engine3D;

public class MeshVertex{
	private boolean imageVertex;
	private Point3D position, normal;
	private double[] view;

	// Image vertex
	private Point2D textureCoords;
	private Image image;

	// Color vertex
	private Color vertexColor;

	public MeshVertex(Point3D position, Point3D normal, Point2D tex, Image image){
		this.imageVertex = true;
		this.position = position;
		this.normal = normal;
		this.textureCoords = tex;
		this.image = image;
	}

	public MeshVertex(Point3D position, Point3D normal, Color color){
		this.imageVertex = false;
		this.position = position;
		this.normal = normal;
		this.vertexColor = color;
	}

	public boolean isImageVertex(){
		return this.imageVertex;
	}

	public Image getImage(){
		return this.image;
	}

	public Point3D getPosition(){
		return this.position;
	}

	public Point2D getTextureCoords(){
		return this.textureCoords;
	}

	public Color getColor(){
		return this.vertexColor;
	}

	public Point3D getNormal(){
		return this.normal;
	}

	public void setNormal(Point3D n){
		this.normal = n;
	}

	public Point3D getViewPosition(Camera camera){
		double[] p = new double[]{this.position.getX(), this.position.getY(), this.position.getZ(), 1};
		this.view = Engine3D.multiply(camera.getViewMatrix(), p);
		return new Point3D(this.view[0], this.view[1], this.view[2]);
	}

	public void setInView(){
		this.view = new double[]{this.position.getX(), this.position.getY(), this.position.getZ(), 1};
	}

	public double[] getProjection(Camera camera){
		double[] proj = Engine3D.multiply(camera.getProjectionMatrix(), this.view);
		double px = proj[0]/proj[3];
		double py = proj[1]/proj[3];
		double pz = proj[2];
		
		px += 1;
		py += 1;
		px *= 0.5*camera.getWidth();
		py *= 0.5*camera.getHeight();

		return new double[]{px, py, 1/proj[3]};
	}
}