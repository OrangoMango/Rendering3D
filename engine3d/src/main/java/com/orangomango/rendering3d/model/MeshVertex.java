package com.orangomango.rendering3d.model;

import javafx.geometry.Point3D;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import com.orangomango.rendering3d.Engine3D;

public class MeshVertex{
	private boolean imageVertex;
	private Point3D position;

	// Image vertex
	private Point2D textureCoords;
	private Image image;

	// Color vertex
	private Color vertexColor;

	public MeshVertex(Point3D position, Point2D tex, Image image){
		this.imageVertex = true;
		this.position = position;
		this.textureCoords = tex;
		this.image = image;
	}

	public MeshVertex(Point3D position, Color color){
		this.imageVertex = false;
		this.position = position;
		this.vertexColor = color;
	}

	public boolean isImageVertex(){
		return this.imageVertex;
	}

	public Image getImage(){
		return this.image;
	}

	public Point2D getTextureCoords(){
		return this.textureCoords;
	}

	public Color getColor(){
		return this.vertexColor;
	}

	public double[] getProjection(Camera camera){
		double[] p = new double[]{this.position.getX(), this.position.getY(), this.position.getZ(), 1};
		double[] view = Engine3D.multiply(camera.getViewMatrix(), p);
		double[] proj = Engine3D.multiply(camera.getProjectionMatrix(), view);
		double px = proj[0]/(proj[3] == 0 ? 1 : proj[3]);
		double py = proj[1]/(proj[3] == 0 ? 1 : proj[3]);
		double pz = proj[2];

		px += 1;
		py += 1;
		px *= 0.5*camera.getWidth();
		py *= 0.5*camera.getHeight();

		return new double[]{px, py, 1/proj[3]};
	}
}