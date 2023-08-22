package com.orangomango.rendering3d.model;

import javafx.scene.paint.Color;
import javafx.geometry.Point3D;

import java.util.List;

public class MeshTriangle{
	private MeshVertex vertex1, vertex2, vertex3;
	private ProjectedTriangle projected;

	public MeshTriangle(MeshVertex vertex1, MeshVertex vertex2, MeshVertex vertex3){
		this.vertex1 = vertex1;
		this.vertex2 = vertex2;
		this.vertex3 = vertex3;
		if (!(this.vertex1.isImageVertex() && this.vertex2.isImageVertex() && this.vertex3.isImageVertex()) && !(!this.vertex1.isImageVertex() && !this.vertex2.isImageVertex() && !this.vertex3.isImageVertex())){
			throw new IllegalStateException("All vertices must be either imageVertices or colorVertices");
		}
	}

	// Called once every frame
	public void update(Camera camera, List<Light> lights){
		this.projected = null; // Reset

		double[] p1 = this.vertex1.getProjection(camera);
		double[] p2 = this.vertex2.getProjection(camera);
		double[] p3 = this.vertex3.getProjection(camera);

		Point3D normal = this.vertex1.getNormal();
		if (normal == null){
			normal = this.vertex2.getPosition().subtract(this.vertex1.getPosition()).crossProduct(this.vertex3.getPosition().subtract(this.vertex1.getPosition()));
			normal = normal.normalize();
			this.vertex1.setNormal(normal);
			this.vertex2.setNormal(normal);
			this.vertex3.setNormal(normal);
		}
		double dot = normal.dotProduct(this.vertex1.getPosition().subtract(camera.getPosition()));

		if (dot < 0){
			if (this.vertex1.isImageVertex()){
				this.projected = new ProjectedTriangle(camera, p1, p2, p3, this.vertex1.getImage(), this.vertex1.getTextureCoords(), this.vertex2.getTextureCoords(), this.vertex3.getTextureCoords());
			} else {
				this.projected = new ProjectedTriangle(camera, p1, p2, p3, this.vertex1.getColor(), this.vertex2.getColor(), this.vertex3.getColor());
			}
			this.projected.setLightData(lights, this.vertex1, this.vertex2, this.vertex3);
		}
	}

	public ProjectedTriangle getProjectedTriangle(){
		return this.projected;
	}
}