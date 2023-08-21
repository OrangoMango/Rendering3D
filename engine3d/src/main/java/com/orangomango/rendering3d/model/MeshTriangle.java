package com.orangomango.rendering3d.model;

import javafx.scene.paint.Color;

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
	public void update(Camera camera){
		double[] p1 = this.vertex1.getProjection(camera);
		double[] p2 = this.vertex2.getProjection(camera);
		double[] p3 = this.vertex3.getProjection(camera);

		if (this.vertex1.isImageVertex()){
			this.projected = new ProjectedTriangle(p1, p2, p3, this.vertex1.getImage(), this.vertex1.getTextureCoords(), this.vertex2.getTextureCoords(), this.vertex3.getTextureCoords());	
		} else {
			this.projected = new ProjectedTriangle(p1, p2, p3, this.vertex1.getColor(), this.vertex2.getColor(), this.vertex3.getColor());
		}
	}

	public ProjectedTriangle getProjectedTriangle(){
		return this.projected;
	}
}