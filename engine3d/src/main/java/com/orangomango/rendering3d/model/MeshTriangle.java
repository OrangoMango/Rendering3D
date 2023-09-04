package com.orangomango.rendering3d.model;

import javafx.scene.paint.Color;
import javafx.geometry.Point3D;
import javafx.geometry.Point2D;

import java.util.*;

import com.orangomango.rendering3d.Engine3D;

public class MeshTriangle{
	private MeshVertex vertex1, vertex2, vertex3;
	private List<ProjectedTriangle> projected = new ArrayList<>();
	private boolean showAllFaces, imageTransparent;

	public MeshTriangle(MeshVertex vertex1, MeshVertex vertex2, MeshVertex vertex3){
		this.vertex1 = vertex1;
		this.vertex2 = vertex2;
		this.vertex3 = vertex3;
		if (!(this.vertex1.isImageVertex() && this.vertex2.isImageVertex() && this.vertex3.isImageVertex()) && !(!this.vertex1.isImageVertex() && !this.vertex2.isImageVertex() && !this.vertex3.isImageVertex())){
			throw new IllegalStateException("All vertices must be either imageVertices or colorVertices");
		}
	}

	public void setImageTransparent(boolean value){
		this.imageTransparent = value;
	}

	public void setShowAllFaces(boolean value){
		this.showAllFaces = value;
	}

	// Called once every frame
	public void update(Camera camera, List<Light> lights){
		this.projected.clear(); // Reset

		// Get the normal of the triangle before clipping and calculate the dot product
		Point3D normal = this.vertex1.getNormal();
		if (normal == null){
			normal = this.vertex2.getPosition().subtract(this.vertex1.getPosition()).crossProduct(this.vertex3.getPosition().subtract(this.vertex1.getPosition()));
			normal = normal.normalize();
			this.vertex1.setNormal(normal);
			this.vertex2.setNormal(normal);
			this.vertex3.setNormal(normal);
		}
		double dot = normal.dotProduct(this.vertex1.getPosition().subtract(camera.getPosition()));

		// Frustum culling
		boolean isVisible = camera.isVisible(this);

		if (isVisible && (dot < 0 || this.showAllFaces)){
			// Frustum clipping
			Point3D[][] frustum = camera.getViewFrustum(true);
			List<MeshTriangle> triangles = new ArrayList<>();
			triangles.add(this);
			for (Point3D[] plane : frustum){
				List<MeshTriangle> generated = new ArrayList<>();
				for (MeshTriangle bigTriangle : triangles){
					generated.addAll(Engine3D.clip(bigTriangle, camera, plane[0].multiply(-1), plane[1]));
				}
				triangles = generated;
			}

			for (MeshTriangle triangle : triangles){
				double[] p1 = triangle.vertex1.getProjection(camera);
				double[] p2 = triangle.vertex2.getProjection(camera);
				double[] p3 = triangle.vertex3.getProjection(camera);

				ProjectedTriangle projectedTriangle;
				if (triangle.vertex1.isImageVertex()){
					projectedTriangle = new ProjectedTriangle(camera, p1, p2, p3, triangle.vertex1.getImage(), triangle.vertex1.getTextureCoords(), triangle.vertex2.getTextureCoords(), triangle.vertex3.getTextureCoords());
					projectedTriangle.setTransparent(this.imageTransparent);
				} else {
					projectedTriangle = new ProjectedTriangle(camera, p1, p2, p3, triangle.vertex1.getColor(), triangle.vertex2.getColor(), triangle.vertex3.getColor());
					projectedTriangle.setTransparent(triangle.vertex1.getColor().getOpacity() < 1 || triangle.vertex2.getColor().getOpacity() < 1 || triangle.vertex3.getColor().getOpacity() < 1);
				}
				projectedTriangle.setLightData(lights, triangle.vertex1, triangle.vertex2, triangle.vertex3);
				this.projected.add(projectedTriangle);
			}
		}
	}

	public MeshVertex getVertex1(){
		return this.vertex1;
	}

	public MeshVertex getVertex2(){
		return this.vertex2;
	}

	public MeshVertex getVertex3(){
		return this.vertex3;
	}

	public List<ProjectedTriangle> getProjectedTriangles(){
		return this.projected;
	}
}