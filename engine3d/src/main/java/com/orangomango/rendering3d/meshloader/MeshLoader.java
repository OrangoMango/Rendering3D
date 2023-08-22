package com.orangomango.rendering3d.meshloader;

import javafx.geometry.Point3D;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;

import java.io.*;
import java.util.*;

import com.orangomango.rendering3d.model.Mesh;

public class MeshLoader{
	private File file;
	private Point3D position = Point3D.ZERO;
	private double scale;

	public MeshLoader(File file){
		this.file = file;
	}

	public void setPosition(Point3D pos){
		this.position = pos;
	}

	public void setScale(double scale){
		this.scale = scale;
	}

	public Mesh load(boolean meshImage){
		try {
			BufferedReader reader = new BufferedReader(new FileReader(this.file));

			Mtllib mtllib = null;
			List<Point3D> vertices = new ArrayList<>();
			List<Color> vertexColors = new ArrayList<>();
			List<Point3D> normals = new ArrayList<>();
			List<Point2D> vertexCoords = new ArrayList<>();
			List<Point3D[]> facesNormals = new ArrayList<>();
			List<int[]> faces = new ArrayList<>();
			List<int[]> facesTextureVertices = new ArrayList<>();
			List<Color> facesColors = new ArrayList<>();
			List<Integer> facesImages = new ArrayList<>();

			String currentMaterial = null;

			String line;
			while ((line = reader.readLine()) != null){
				if (line.startsWith("mtllib ")){
					mtllib = new Mtllib(new File(this.file.getParent(), line.split(" ")[1]));
					boolean loaded = mtllib.load();
					if (!loaded) mtllib = null;
				} else if (line.startsWith("v ")){
					String[] pieces = line.split(" ");
					double[] parray = new double[pieces.length-1];
					for (int i = 0; i < parray.length; i++){
						parray[i] = Double.parseDouble(pieces[i+1]);
					}
					vertices.add(new Point3D(parray[0]*this.scale+this.position.getX(), parray[1]*this.scale+this.position.getY(), parray[2]*this.scale+this.position.getZ()));
					// This line may contain also some color information (x y z r g b)
					if (parray.length == 6){
						vertexColors.add(Color.color(parray[3], parray[4], parray[5]));
					}		
				} else if (line.startsWith("vn ")){
					double nx = Double.parseDouble(line.split(" ")[1]);
					double ny = Double.parseDouble(line.split(" ")[2]);
					double nz = Double.parseDouble(line.split(" ")[3]);
					normals.add(new Point3D(nx, ny, nz));
				} else if (line.startsWith("vt ")){
					double vtx = Double.parseDouble(line.split(" ")[1]);
					double vty = Double.parseDouble(line.split(" ")[2]);
					vertexCoords.add(new Point2D(vtx, 1-vty));
				} else if (line.startsWith("f ")){
					String[] pieces = line.split(" ");
					int[] farray = new int[pieces.length-1];
					int[] tarray = new int[pieces.length-1];
					int[] narray = new int[pieces.length-1];
					boolean hasTextures = false, hasNormals = false;
					for (int i = 0; i < farray.length; i++){
						// <vertex id>/<texture vertex id>/<normal id>
						String[] lineArray = pieces[i+1].split("/");
						farray[i] = Integer.parseInt(lineArray[0])-1; // IDs start at 1 in .obj files
						// The line could be <vID>//<normalId> or <vId>/<texID>/<normalID>
						if (lineArray.length == 3){
							if (!lineArray[1].equals("")){
								tarray[i] = Integer.parseInt(lineArray[1])-1;
								hasTextures = true;
							}
							narray[i] = Integer.parseInt(lineArray[2])-1;
							hasNormals = true;
						}
					}

					if (hasNormals){
						// There is a separate list ('facesNormals') because the Mesh constructor requires an array of normals instead of an array of their IDs
						for (int i = 1 ; i <= farray.length-2; i++){
							facesNormals.add(new Point3D[]{normals.get(narray[0]), normals.get(narray[i]), normals.get(narray[i+1])});
						}
					}

					// Build all the triangles
					for (int i = 1; i <= farray.length-2; i++){
						faces.add(new int[]{farray[0], farray[i], farray[i+1]});
						if (hasTextures){
							facesTextureVertices.add(new int[]{tarray[0], tarray[i], tarray[i+1]});
						}
					}

					if (mtllib != null){
						for (int i = 0; i < farray.length-2; i++){
							facesColors.add(mtllib.getColor(currentMaterial));
							facesImages.add(mtllib.getImageIndex(currentMaterial));
						}
					}
				} else if (line.startsWith("usemtl ")){
					currentMaterial = line.split(" ")[1];
				}
			}
			reader.close();

			// Convert everything to an array and build the mesh
			Point3D[] vt = vertices.toArray(new Point3D[vertices.size()]);
			int[][] fc = faces.toArray(new int[faces.size()][3]);
			Point3D[][] fcn = facesNormals.size() == 0 ? null : facesNormals.toArray(new Point3D[facesNormals.size()][3]);
			
			// Images
			Image[] images = mtllib == null ? null : mtllib.getImages();
			int[] fci = facesImages.stream().mapToInt(i -> i.intValue()).toArray();
			Point2D[] vexCoords = vertexCoords.toArray(new Point2D[vertexCoords.size()]);
			int[][] fcv = facesTextureVertices.toArray(new int[facesTextureVertices.size()][3]);

			// Colors
			Color[] vc = vertexColors.size() == 0 ? null : vertexColors.toArray(new Color[vertexColors.size()]);
			Color[] fcc = facesColors.size() == 0 ? null : facesColors.toArray(new Color[facesColors.size()]);

			if (meshImage){
				Mesh mesh = new Mesh(vt, fc, fcn, images, fci, vexCoords, fcv);
				return mesh;
			} else {
				Mesh mesh = new Mesh(vt, fc, fcn, vc, fcc);
				return mesh;
			}
		} catch (IOException ex){
			ex.printStackTrace();
			return null;
		}
	}
}