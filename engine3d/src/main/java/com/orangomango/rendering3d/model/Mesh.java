package com.orangomango.rendering3d.model;

import javafx.geometry.Point3D;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class Mesh{
	// Image mesh
	private Image[] images;
	private int[] facesImages;
	private Point2D[] textureVertices;
	private int[][] textureFaces;

	// Color mesh
	private Color[] colors;
	private Color[][] facesColors; // .obj has colors in mtllib file
	private Color[][] vertexColors; // .obj has colors with vertices

	private Point3D[] vertices;
	private int[][] faces;
	private Point3D[][] normals;
	private boolean imageMesh;
	private Point3D[][] trianglePoints;
	private MeshTriangle[] triangles;

	public Mesh(Point3D[] vertices, int[][] faces, Point3D[][] normals, Image[] images, int[] facesImages, Point2D[] textureVertices, int[][] textureFaces){
		this.imageMesh = true;
		setupMesh(vertices, faces, normals);
		this.images = images;
		this.facesImages = facesImages;
		this.textureVertices = textureVertices;
		this.textureFaces = textureFaces;
	}

	public Mesh(Point3D[] vertices, int[][] faces, Point3D[][] normals, Color[] colors, Color[][] facesColors){
		this.imageMesh = false;
		setupMesh(vertices, faces, normals);
		this.colors = colors;
		this.facesColors = facesColors;
	}

	public void update(Camera camera){
		for (int i = 0; i < this.triangles.length; i++){
			MeshTriangle mt = this.triangles[i];
			mt.update(camera);
		}
	}

	public void render(Color[][] canvas){
		for (int i = 0; i < this.triangles.length; i++){
			MeshTriangle mt = this.triangles[i];
			ProjectedTriangle pt = mt.getProjectedTriangle();
			pt.render(canvas);
		}
	}

	private void setupMesh(Point3D[] vertices, int[][] faces, Point3D[][] normals){
		this.vertices = vertices;
		this.faces = faces;
		this.normals = normals == null ? new Point3D[faces.length][3] : normals;
		
		// Build the triangles list
		this.trianglePoints = new Point3D[this.faces.length][3];
		this.vertexColors = new Color[this.faces.length][3];
		for (int i = 0; i < this.trianglePoints.length; i++){
			Point3D[] tr = new Point3D[3];
			tr[0] = this.vertices[this.faces[i][0]];
			tr[1] = this.vertices[this.faces[i][1]];
			tr[2] = this.vertices[this.faces[i][2]];
			this.trianglePoints[i] = tr;
			if (!this.imageMesh){
				Color[] cr = new Color[3];
				cr[0] = this.colors[this.faces[i][0]];
				cr[1] = this.colors[this.faces[i][1]];
				cr[2] = this.colors[this.faces[i][2]];
				this.vertexColors[i] = cr;
			}
		}

		// Create the triangles that form this mesh
		this.triangles = new MeshTriangle[this.trianglePoints.length];
		for (int i = 0; i < this.trianglePoints.length; i++){
			Point3D[] tr = this.trianglePoints[i];
			// TODO all vertices must have the same image
			if (this.imageMesh){
				MeshVertex v1 = new MeshVertex(tr[0], this.textureVertices[this.textureFaces[i][0]], this.images[this.facesImages[i]]);
				MeshVertex v2 = new MeshVertex(tr[1], this.textureVertices[this.textureFaces[i][1]], this.images[this.facesImages[i]]);
				MeshVertex v3 = new MeshVertex(tr[2], this.textureVertices[this.textureFaces[i][2]], this.images[this.facesImages[i]]);
				this.triangles[i] = new MeshTriangle(v1, v2, v3);
			} else {
				MeshVertex v1 = new MeshVertex(tr[0], this.facesColors != null ? this.facesColors[i][0] : this.vertexColors[i][0]);
				MeshVertex v2 = new MeshVertex(tr[0], this.facesColors != null ? this.facesColors[i][1] : this.vertexColors[i][1]);
				MeshVertex v3 = new MeshVertex(tr[0], this.facesColors != null ? this.facesColors[i][2] : this.vertexColors[i][2]);
				this.triangles[i] = new MeshTriangle(v1, v2, v3);
			}
		}
	}
}