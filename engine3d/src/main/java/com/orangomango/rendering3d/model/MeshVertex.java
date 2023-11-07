package com.orangomango.rendering3d.model;

import javafx.geometry.Point3D;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Objects;

import com.orangomango.rendering3d.Engine3D;

public class MeshVertex{
	private boolean imageVertex;
	private Point3D position, normal;

	// Image vertex
	private Point2D textureCoords;
	private Image image;

	// Color vertex
	private Color vertexColor;

	private static HashMap<MeshVertex, ProjectedVertex> VERTICES = new HashMap<>(300);

	private static class ProjectedVertex{
		private double[] view;
		private double[] projection;

		public ProjectedVertex(double[] view, double[] proj){
			this.view = view;
			this.projection = proj;
		}
	}

	public static void clearStoredVertices(){
		VERTICES.clear();
	}

	public static int getViewVerticesCount(){
		return VERTICES.size(); // If a vertex has a projection, then it also has a position in view space
	}

	public static long getProjectedVerticesCount(){
		return VERTICES.values().stream().filter(p -> p.projection != null).count();
	}

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

	@Override
	public int hashCode(){
		return Objects.hash(this.position);
	}

	@Override
	public boolean equals(Object other){
		if (other instanceof MeshVertex){
			MeshVertex v = (MeshVertex)other;
			return this.position.equals(v.position);
		} else return false;
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

	private double[] getViewPosition(Camera camera){
		ProjectedVertex projectedVertex = VERTICES.getOrDefault(this, null);
		if (projectedVertex == null || projectedVertex.view == null){
			double[] p = new double[]{this.position.getX(), this.position.getY(), this.position.getZ(), 1};
			double[] view = Engine3D.multiply(camera.getViewMatrix(), p);

			if (projectedVertex == null){
				projectedVertex = new ProjectedVertex(view, null);
				VERTICES.put(this, projectedVertex);
			} else {
				projectedVertex.view = view;
			}

			return view;
		} else {
			return projectedVertex.view;
		}
	}

	public double[] getProjection(Camera camera){
		ProjectedVertex projectedVertex = VERTICES.getOrDefault(this, null);

		// Calculate the position in the view space if needed
		double[] view = getViewPosition(camera);

		if (projectedVertex == null || projectedVertex.projection == null){
			double[] proj = Engine3D.multiply(camera.getProjectionMatrix(), view);
			double px = proj[0]/(proj[3] == 0 ? 1 : proj[3]);
			double py = proj[1]/(proj[3] == 0 ? 1 : proj[3]);
			double pz = proj[2];
			
			px += 1;
			py += 1;
			px *= 0.5*camera.getWidth();
			py *= 0.5*camera.getHeight();

			double[] projection = new double[]{px, py, 1/proj[3]};

			if (projectedVertex == null){
				projectedVertex = new ProjectedVertex(view, projection);
				VERTICES.put(this, projectedVertex);
			} else {
				projectedVertex.projection = projection;
			}

			return projection;
		} else {
			return projectedVertex.projection;
		}
	}
}