package com.orangomango.rendering3d.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.geometry.Point3D;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.*;
import java.util.function.Predicate;

import com.orangomango.rendering3d.Engine3D;

public class Mesh{
	// Image mesh
	private Image[] images;
	private int[] facesImages;
	private Point2D[] textureVertices;
	private int[][] textureFaces;

	// Color mesh
	private Color[] colors;
	private Color[] facesColors; // .obj has colors in mtllib file
	private Color[][] vertexColors; // .obj has colors with vertices

	private Point3D[] vertices;
	private int[][] faces;
	private Point3D[][] normals;
	private boolean imageMesh;
	private MeshTriangle[] triangles;
	private List<Integer> hiddenFaces = new ArrayList<>();
	private boolean showAllFaces;
	private Predicate<Camera> skipCondition;

	public Mesh(Point3D[] vertices, int[][] faces, Point3D[][] normals, Image[] images, int[] facesImages, Point2D[] textureVertices, int[][] textureFaces){
		this.imageMesh = true;
		this.images = images;
		this.facesImages = facesImages;
		this.textureVertices = textureVertices;
		this.textureFaces = textureFaces;
		setupMesh(vertices, faces, normals);
	}

	public Mesh(Point3D[] vertices, int[][] faces, Point3D[][] normals, Color[] colors, Color[] facesColors){
		this.imageMesh = false;
		this.colors = colors;
		this.facesColors = facesColors;
		setupMesh(vertices, faces, normals);
	}

	public void addHiddenFace(int n){
		if (!this.hiddenFaces.contains(n)){
			this.hiddenFaces.add(n);
		}
	}

	public void clearHiddenFaces(){
		this.hiddenFaces.clear();
	}

	public void setShowAllFaces(boolean value){
		this.showAllFaces = value;
	}

	public void setSkipCondition(Predicate<Camera> condition){
		this.skipCondition = condition;
	}

	public void setTransparentProcessing(boolean value){
		for (int i = 0; i < this.triangles.length; i++){
			this.triangles[i].setImageTransparent(value);
		}
	}

	public MeshTriangle[] getTriangles(){
		return this.triangles;
	}

	// TODO: Change x,y,z to Point3D
	public void translate(double x, double y, double z){
		for (int i = 0; i < this.vertices.length; i++){
			Point3D vertex = this.vertices[i];
			this.vertices[i] = vertex.add(x, y, z);
		}
	}

	public void setRotation(Point3D axis, double rot, Point3D pivot){
		double[][] rotMat = Engine3D.getRotateAxis(axis, rot);

		for (int i = 0; i < this.vertices.length; i++){
			Point3D vertex = this.vertices[i].subtract(pivot);
			double[] rv = new double[]{vertex.getX(), vertex.getY(), vertex.getZ()};
			rv = Engine3D.multiply(rotMat, rv);
			this.vertices[i] = new Point3D(rv[0], rv[1], rv[2]);
			this.vertices[i] = this.vertices[i].add(pivot);
		}

		if (this.normals != null){
			for (int i = 0; i < this.normals.length; i++){
				Point3D n1 = this.normals[i][0];
				Point3D n2 = this.normals[i][1];
				Point3D n3 = this.normals[i][2];

				double[] rn1 = Engine3D.multiply(rotMat, new double[]{n1.getX(), n1.getY(), n1.getZ()});
				double[] rn2 = Engine3D.multiply(rotMat, new double[]{n2.getX(), n2.getY(), n2.getZ()});
				double[] rn3 = Engine3D.multiply(rotMat, new double[]{n3.getX(), n3.getY(), n3.getZ()});
				this.normals[i][0] = new Point3D(rn1[0], rn1[1], rn1[2]);
				this.normals[i][1] = new Point3D(rn2[0], rn2[1], rn2[2]);
				this.normals[i][2] = new Point3D(rn3[0], rn3[1], rn3[2]);
			}
		}
	}

	public void setRotation(double rx, double ry, double rz, Point3D pivot){
		for (int i = 0; i < this.vertices.length; i++){
			Point3D vertex = this.vertices[i].subtract(pivot);
			double[] rv = new double[]{vertex.getX(), vertex.getY(), vertex.getZ()};
			if (rx != 0) rv = Engine3D.multiply(Engine3D.getRotateX(rx), rv);
			if (ry != 0) rv = Engine3D.multiply(Engine3D.getRotateY(ry), rv);
			if (rz != 0) rv = Engine3D.multiply(Engine3D.getRotateZ(rz), rv);
			this.vertices[i] = new Point3D(rv[0], rv[1], rv[2]);
			this.vertices[i] = this.vertices[i].add(pivot);
		}

		if (this.normals != null){
			for (int i = 0; i < this.normals.length; i++){
				Point3D n1 = this.normals[i][0];
				Point3D n2 = this.normals[i][1];
				Point3D n3 = this.normals[i][2];
				
				double[] rn1 = Engine3D.multiply(Engine3D.getRotateX(rx), new double[]{n1.getX(), n1.getY(), n1.getZ()});
				double[] rn2 = Engine3D.multiply(Engine3D.getRotateX(rx), new double[]{n2.getX(), n2.getY(), n2.getZ()});
				double[] rn3 = Engine3D.multiply(Engine3D.getRotateX(rx), new double[]{n3.getX(), n3.getY(), n3.getZ()});
				rn1 = Engine3D.multiply(Engine3D.getRotateY(ry), rn1);
				rn2 = Engine3D.multiply(Engine3D.getRotateY(ry), rn2);
				rn3 = Engine3D.multiply(Engine3D.getRotateY(ry), rn3);
				rn1 = Engine3D.multiply(Engine3D.getRotateZ(rz), rn1);
				rn2 = Engine3D.multiply(Engine3D.getRotateZ(rz), rn2);
				rn3 = Engine3D.multiply(Engine3D.getRotateZ(rz), rn3);
				this.normals[i][0] = new Point3D(rn1[0], rn1[1], rn1[2]);
				this.normals[i][1] = new Point3D(rn2[0], rn2[1], rn2[2]);
				this.normals[i][2] = new Point3D(rn3[0], rn3[1], rn3[2]);
			}
		}
	}

	public void update(Camera camera, List<Light> lights){
		for (int i = 0; i < this.triangles.length; i++){
			MeshTriangle mt = this.triangles[i];
			if ((this.skipCondition != null && this.skipCondition.test(camera)) || this.hiddenFaces.contains(i)){
				mt.getProjectedTriangles().clear(); // Reset
			} else {
				mt.setShowAllFaces(this.showAllFaces);
				mt.update(camera, lights);
			}
		}
	}

	public List<ProjectedTriangle> render(ColorCanvas canvas, GraphicsContext gc){
		List<ProjectedTriangle> transparentTriangles = new ArrayList<>();
		for (int i = 0; i < this.triangles.length; i++){
			MeshTriangle mt = this.triangles[i];
			List<ProjectedTriangle> pts = mt.getProjectedTriangles();
			for (ProjectedTriangle pt : pts){
				pt.setLightIntensity(mt.getLight());
				if (pt.isTransparent()){
					transparentTriangles.add(pt);
				} else {
					pt.render(canvas, gc);
				}
			}
		}
		return transparentTriangles;
	}

	private void setupMesh(Point3D[] vertices, int[][] faces, Point3D[][] normals){
		this.vertices = vertices;
		this.faces = faces;
		this.normals = normals == null ? new Point3D[faces.length][3] : normals;
	}

	public void build(){
		// Build the triangles list
		Point3D[][] trianglePoints = new Point3D[this.faces.length][3];
		this.vertexColors = new Color[this.faces.length][3];
		for (int i = 0; i < trianglePoints.length; i++){
			Point3D[] tr = new Point3D[3];
			tr[0] = this.vertices[this.faces[i][0]];
			tr[1] = this.vertices[this.faces[i][1]];
			tr[2] = this.vertices[this.faces[i][2]];
			trianglePoints[i] = tr;
			if (!this.imageMesh && this.colors != null){
				Color[] cr = new Color[3];
				cr[0] = this.colors[this.faces[i][0]];
				cr[1] = this.colors[this.faces[i][1]];
				cr[2] = this.colors[this.faces[i][2]];
				this.vertexColors[i] = cr;
			}
		}

		// Create the triangles that form this mesh
		MeshTriangle[] triangles = new MeshTriangle[trianglePoints.length];
		for (int i = 0; i < trianglePoints.length; i++){
			Point3D[] tr = trianglePoints[i];
			if (this.imageMesh){
				MeshVertex v1 = new MeshVertex(tr[0], this.normals == null ? null : this.normals[i][0], this.textureVertices[this.textureFaces[i][0]], this.images[this.facesImages[i]]);
				MeshVertex v2 = new MeshVertex(tr[1], this.normals == null ? null : this.normals[i][1], this.textureVertices[this.textureFaces[i][1]], this.images[this.facesImages[i]]);
				MeshVertex v3 = new MeshVertex(tr[2], this.normals == null ? null : this.normals[i][2], this.textureVertices[this.textureFaces[i][2]], this.images[this.facesImages[i]]);
				triangles[i] = new MeshTriangle(v1, v2, v3);
			} else {
				MeshVertex v1 = new MeshVertex(tr[0], this.normals == null ? null : this.normals[i][0], this.facesColors != null ? this.facesColors[i] : this.vertexColors[i][0]);
				MeshVertex v2 = new MeshVertex(tr[1], this.normals == null ? null : this.normals[i][1], this.facesColors != null ? this.facesColors[i] : this.vertexColors[i][1]);
				MeshVertex v3 = new MeshVertex(tr[2], this.normals == null ? null : this.normals[i][2], this.facesColors != null ? this.facesColors[i] : this.vertexColors[i][2]);
				triangles[i] = new MeshTriangle(v1, v2, v3);
			}
		}
		this.triangles = triangles;
	}
}