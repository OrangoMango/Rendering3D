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

	private List<MeshTriangle> clip(Camera camera, Point3D planeN, Point3D planeA){
		// TODO: Color needs to be interpolated

		MeshVertex[] inside = new MeshVertex[3];
		MeshVertex[] outside = new MeshVertex[3];
		Point3D[] insideView = new Point3D[3];
		Point3D[] outsideView = new Point3D[3];
		int insideN = 0;
		int outsideN = 0;
		List<MeshTriangle> output = new ArrayList<>();

		Point3D view1 = this.vertex1.getViewPosition(camera);
		Point3D view2 = this.vertex2.getViewPosition(camera);
		Point3D view3 = this.vertex3.getViewPosition(camera);

		double d1 = Engine3D.distanceToPlane(planeN, planeA, view1, planeN.multiply(-1));
		double d2 = Engine3D.distanceToPlane(planeN, planeA, view2, planeN.multiply(-1));
		double d3 = Engine3D.distanceToPlane(planeN, planeA, view3, planeN.multiply(-1));

		if (d1 >= 0){
			inside[insideN] = this.vertex1;
			insideView[insideN++] = view1;
		} else {
			outside[outsideN] = this.vertex1;
			outsideView[outsideN++] = view1;
		}
		if (d2 >= 0){
			inside[insideN] = this.vertex2;
			insideView[insideN++] = view2;
		} else {
			outside[outsideN] = this.vertex2;
			outsideView[outsideN++] = view2;
		}
		if (d3 >= 0){
			inside[insideN] = this.vertex3;
			insideView[insideN++] = view3;
		} else {
			outside[outsideN] = this.vertex3;
			outsideView[outsideN++] = view3;
		}

		if (insideN == 3){
			output.add(this);
		} else if (insideN == 0){
			// No output (All vertices are outside)
		} else if (insideN == 1){
			// 1 triangle is produced
			Point3D dp1 = outsideView[0].subtract(insideView[0]).normalize();
			Point3D dp2 = outsideView[1].subtract(insideView[0]).normalize();
			double factor1 = Engine3D.distanceToPlane(planeN, planeA, insideView[0], dp1);
			double factor2 = Engine3D.distanceToPlane(planeN, planeA, insideView[0], dp2);
			MeshVertex vex1, vex2, vex3;
			if (this.vertex1.isImageVertex()){
				Point2D tex1 = inside[0].getTextureCoords().add(outside[0].getTextureCoords().subtract(inside[0].getTextureCoords()).normalize().multiply(factor1));
				Point2D tex2 = inside[0].getTextureCoords().add(outside[1].getTextureCoords().subtract(inside[0].getTextureCoords()).normalize().multiply(factor2));
				vex1 = inside[0];
				vex2 = new MeshVertex(insideView[0].add(dp1.multiply(factor1)), outside[0].getNormal(), tex1, outside[0].getImage());
				vex3 = new MeshVertex(insideView[0].add(dp2.multiply(factor2)), outside[1].getNormal(), tex2, outside[1].getImage());
			} else {
				vex1 = inside[0];
				vex2 = new MeshVertex(insideView[0].add(dp1.multiply(factor1)), outside[0].getNormal(), outside[0].getColor());
				vex3 = new MeshVertex(insideView[0].add(dp2.multiply(factor2)), outside[1].getNormal(), outside[1].getColor());
			}

			// Build the view
			vex2.setInView();
			vex3.setInView();

			MeshTriangle triangle = new MeshTriangle(vex1, vex2, vex3);
			output.add(triangle);
		} else if (insideN == 2){
			// 2 triangles are produced
			Point3D dp1 = outsideView[0].subtract(insideView[0]).normalize();
			Point3D dp2 = outsideView[0].subtract(insideView[1]).normalize();
			double factor1 = Engine3D.distanceToPlane(planeN, planeA, insideView[0], dp1);
			double factor2 = Engine3D.distanceToPlane(planeN, planeA, insideView[1], dp2);
			Point3D tempP = insideView[0].add(dp1.multiply(factor1));
			MeshVertex vex1, vex2, vex3; // Triangle 1
			MeshVertex vex4, vex5, vex6; // Triangle 2
			if (this.vertex1.isImageVertex()){
				Point2D textureP = inside[0].getTextureCoords().add(outside[0].getTextureCoords().subtract(inside[0].getTextureCoords()).normalize().multiply(factor1));
				vex1 = inside[0];
				vex2 = inside[1];
				vex3 = new MeshVertex(tempP, outside[0].getNormal(), textureP, outside[0].getImage());

				vex4 = vex3;
				vex5 = inside[1];
				vex6 = new MeshVertex(insideView[1].add(dp2.multiply(factor2)), outside[0].getNormal(), inside[1].getTextureCoords().add(outside[0].getTextureCoords().subtract(inside[1].getTextureCoords()).normalize().multiply(factor2)), outside[0].getImage());
			} else {
				vex1 = inside[0];
				vex2 = inside[1];
				vex3 = new MeshVertex(tempP, outside[0].getNormal(), outside[0].getColor());

				vex4 = vex3;
				vex5 = inside[1];
				vex6 = new MeshVertex(insideView[1].add(dp2.multiply(factor2)), outside[0].getNormal(), outside[0].getColor());
			}

			// Build the view
			vex3.setInView();
			vex6.setInView();

			MeshTriangle triangle1 = new MeshTriangle(vex1, vex2, vex3);
			MeshTriangle triangle2 = new MeshTriangle(vex4, vex5, vex6);
			output.add(triangle1);
			output.add(triangle2);
		}

		return output;
	}

	// Called once every frame
	public void update(Camera camera, List<Light> lights){
		this.projected.clear(); // Reset

		// Check the normal of the triangle before clipping and calculate the dot product
		double firstDot = getDotProduct(this, camera);

		if (firstDot < 0 || this.showAllFaces){
			List<MeshTriangle> triangles = clip(camera, new Point3D(0, 0, 1), new Point3D(0, 0, camera.getZnear()));
			for (MeshTriangle triangle : triangles){
				double[] p1 = triangle.vertex1.getProjection(camera);
				double[] p2 = triangle.vertex2.getProjection(camera);
				double[] p3 = triangle.vertex3.getProjection(camera);

				if (!Engine3D.isInScene((int)p1[0], (int)p1[1], camera) && !Engine3D.isInScene((int)p2[0], (int)p2[1], camera) && !Engine3D.isInScene((int)p3[0], (int)p3[1], camera)){
					continue;
				}

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

	private static double getDotProduct(MeshTriangle triangle, Camera camera){
		Point3D normal = triangle.vertex1.getNormal();
		if (normal == null){
			normal = triangle.vertex2.getPosition().subtract(triangle.vertex1.getPosition()).crossProduct(triangle.vertex3.getPosition().subtract(triangle.vertex1.getPosition()));
			normal = normal.normalize();
			triangle.vertex1.setNormal(normal);
			triangle.vertex2.setNormal(normal);
			triangle.vertex3.setNormal(normal);
		}
		double dot = normal.dotProduct(triangle.vertex1.getPosition().subtract(camera.getPosition()));
		return dot;
	}

	public List<ProjectedTriangle> getProjectedTriangles(){
		return this.projected;
	}
}