package com.orangomango.rendering3d.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;

import java.util.*;
import java.io.*;

import static com.orangomango.rendering3d.Engine3D.*;

public class Mesh{
	private Point3D[] points;
	private double[][][] projected;
	private Map<Integer, double[][]> extraProjected = new HashMap<>();
	private Color[][] vertexColors;
	private Color[][] facesColors;
	private int[][] faces;
	private Color[] colors;
	private Color color;
	private Point2D[] textureVertex;
	private int[][] textureFaces;
	private Point3D[][] normals;
	private Image image;
	private Point3D[][] trianglePoints;
	private Color[][] vertexCol;
	private double crx, cry, crz;
	public boolean showLines;
	private List<Integer> hiddenTriangles = new ArrayList<>();
	
	public Map<Camera, double[][][]> cache = new HashMap<>();
	private double[][][] rotationCache;
	
	public static double SHADOW_FACTOR = 0.0005;
	
	public Mesh(Image image, Point3D[] points, int[][] faces, Point2D[] textureCoords, int[][] vertexFaces, Color[] colors, Point3D[][] ns, Color[][] fcs){
		this.color = Color.RED; //Color.color(Math.random(), Math.random(), Math.random());
		this.image = image;
		this.points = points;
		this.projected = new double[faces.length][3][3];
		this.faces = faces;
		this.colors = colors;
		this.facesColors = fcs;
		this.textureVertex = textureCoords;
		this.textureFaces = vertexFaces;
		this.normals = ns == null ? new Point3D[faces.length][3] : ns;
		
		this.rotationCache = new double[faces.length][3][];
	}

	public void addHiddenFace(int n){
		if (!this.hiddenTriangles.contains(n)) this.hiddenTriangles.add(n);
	}

	public void clearHiddenFaces(){
		this.hiddenTriangles.clear();
	}
	
	public void setRotation(double crx, double cry, double crz){
		this.rotationCache = new double[faces.length][3][];
		this.crx = crx;
		this.cry = cry;
		this.crz = crz;
		for (int i = 0; i < this.normals.length; i++){
			// Rotate normals
			Point3D n1 = this.normals[i][0];
			Point3D n2 = this.normals[i][1];
			Point3D n3 = this.normals[i][2];
			
			if (n1 == null || n2 == null || n3 == null) continue;
			
			double[] rn1 = multiply(getRotateX(this.crx), new double[]{n1.getX(), n1.getY(), n1.getZ()});
			double[] rn2 = multiply(getRotateX(this.crx), new double[]{n2.getX(), n2.getY(), n2.getZ()});
			double[] rn3 = multiply(getRotateX(this.crx), new double[]{n3.getX(), n3.getY(), n3.getZ()});
			rn1 = multiply(getRotateY(this.cry), rn1);
			rn2 = multiply(getRotateY(this.cry), rn2);
			rn3 = multiply(getRotateY(this.cry), rn3);
			rn1 = multiply(getRotateZ(this.crz), rn1);
			rn2 = multiply(getRotateZ(this.crz), rn2);
			rn3 = multiply(getRotateZ(this.crz), rn3);
			this.normals[i][0] = new Point3D(rn1[0], rn1[1], rn1[2]);
			this.normals[i][1] = new Point3D(rn2[0], rn2[1], rn2[2]);
			this.normals[i][2] = new Point3D(rn3[0], rn3[1], rn3[2]);
		}
	}
	
	private Point3D[] getPoints(){
		return this.points;
	}
	
	private Point3D[][] getTrianglePoints(Color[][] vertexColors){
		if (this.trianglePoints != null){
			if (this.colors != null){
				for (int i = 0; i < this.vertexCol.length; i++){
					System.arraycopy(this.vertexCol[i], 0, vertexColors[i], 0, vertexColors[i].length);
				}
			}
			return this.trianglePoints;
		}
		Point3D[][] output = new Point3D[this.faces.length][3];
		for (int i = 0; i < output.length; i++){
			Point3D[] tr = new Point3D[3];
			tr[0] = getPoints()[this.faces[i][0]];
			tr[1] = getPoints()[this.faces[i][1]];
			tr[2] = getPoints()[this.faces[i][2]];
			output[i] = tr;
			if (this.colors != null){
				Color[] cr = new Color[3];
				cr[0] = this.colors[this.faces[i][0]];
				cr[1] = this.colors[this.faces[i][1]];
				cr[2] = this.colors[this.faces[i][2]];
				vertexColors[i] = cr;
			}
		}
		this.vertexCol = vertexColors;
		this.trianglePoints = output;
		return output;
	}
	
	private void setProjectedPoint(int f, int tr, double[] point){
		this.projected[f][tr] = point;
	}

	private double distanceToPlane(Point3D normal, Point3D planePoint, Point3D point, Point3D direction){
		if (normal.dotProduct(direction) == 0) throw new IllegalStateException("Debug: dp is 0");
		return (normal.dotProduct(planePoint)-normal.dotProduct(point))/normal.dotProduct(direction);
	}

	private Point3D[][] clipTriangle(Point3D planeN, Point3D planeA, Point3D t1, Point3D t2, Point3D t3){
		Point3D[] inside = new Point3D[3];
		Point3D[] outside = new Point3D[3];
		int insideN = 0;
		int outsideN = 0;

		double d1 = distanceToPlane(planeN, planeA, t1, planeN.multiply(-1));
		double d2 = distanceToPlane(planeN, planeA, t2, planeN.multiply(-1));
		double d3 = distanceToPlane(planeN, planeA, t3, planeN.multiply(-1));

		if (d1 >= 0) inside[insideN++] = t1;
		else outside[outsideN++] = t1;
		if (d2 >= 0) inside[insideN++] = t2;
		else outside[outsideN++] = t2;
		if (d3 >= 0) inside[insideN++] = t3;
		else outside[outsideN++] = t3;

		if (insideN == 3){
			return new Point3D[][]{{t1, t2, t3}};
		} else if (insideN == 0){
			return null;
		} else if (insideN == 1){
			Point3D dp1 = outside[0].subtract(inside[0]).normalize();
			Point3D dp2 = outside[1].subtract(inside[0]).normalize();
			return new Point3D[][]{{inside[0], inside[0].add(dp1.multiply(distanceToPlane(planeN, planeA, inside[0], dp1))), inside[0].add(dp2.multiply(distanceToPlane(planeN, planeA, inside[0], dp2)))}};
		} else if (insideN == 2){
			Point3D dp1 = outside[0].subtract(inside[0]).normalize();
			Point3D tempP = inside[0].add(dp1.multiply(distanceToPlane(planeN, planeA, inside[0], dp1)));
			Point3D dp2 = outside[0].subtract(inside[1]).normalize();
			return new Point3D[][]{{inside[0], inside[1], tempP}, {tempP, inside[1], inside[1].add(dp2.multiply(distanceToPlane(planeN, planeA, inside[1], dp2)))}};
		} else {
			return null;
		}
	}
	
	public void evaluate(Camera camera){
		int i = 0;
		this.vertexColors = new Color[this.faces.length][3];
		this.extraProjected.clear();
		for (Point3D[] points : getTrianglePoints(vertexColors)){
			double[][] cam = camera.getViewMatrix();
			
			// Apply transforms
			double[] p1 = new double[]{points[0].getX(), points[0].getY(), points[0].getZ(), 1};
			double[] p2 = new double[]{points[1].getX(), points[1].getY(), points[1].getZ(), 1};
			double[] p3 = new double[]{points[2].getX(), points[2].getY(), points[2].getZ(), 1};
			
			double[][][] proj = this.cache.getOrDefault(camera, null);
			
			// Scale
			/*double factor = 0.1; //0.1;
			p1 = multiply(getScale(factor, factor, factor), p1);
			p2 = multiply(getScale(factor, factor, factor), p2);
			p3 = multiply(getScale(factor, factor, factor), p3);*/
			
			// Rotate
			double[][] rCache = this.rotationCache[i];
			if (rCache[0] == null){
				rCache[0] = multiply(getRotateX(this.crx), p1);
				rCache[0] = multiply(getRotateY(this.cry), rCache[0]);
				rCache[0] = multiply(getRotateZ(this.crz), rCache[0]);
			}
			if (rCache[1] == null){
				rCache[1] = multiply(getRotateX(this.crx), p2);
				rCache[1] = multiply(getRotateY(this.cry), rCache[1]);
				rCache[1] = multiply(getRotateZ(this.crz), rCache[1]);
			}
			if (rCache[2] == null){
				rCache[2] = multiply(getRotateX(this.crx), p3);
				rCache[2] = multiply(getRotateY(this.cry), rCache[2]);
				rCache[2] = multiply(getRotateZ(this.crz), rCache[2]);
			}
			
			p1 = rCache[0];
			p2 = rCache[1];
			p3 = rCache[2];

			// Translate
			//p1 = multiply(getTranslation(0, 0, 8), p1);
			//p2 = multiply(getTranslation(0, 0, 8), p2);
			//p3 = multiply(getTranslation(0, 0, 8), p3);
			
			Point3D point1 = new Point3D(p1[0], p1[1], p1[2]);
			Point3D point2 = new Point3D(p2[0], p2[1], p2[2]);
			Point3D point3 = new Point3D(p3[0], p3[1], p3[2]);
		
			Point3D normal = this.normals[i][0]; 

			if (normal == null){
				normal = point2.subtract(point1).crossProduct(point3.subtract(point1));
				normal.normalize();
				this.normals[i][0] = normal;
				this.normals[i][1] = normal;
				this.normals[i][2] = normal;
			}
			
			double dot = normal.dotProduct(point1.subtract(camera.getX(), camera.getY(), camera.getZ()));
			if (proj == null){
				proj = new double[this.faces.length][3][];
				this.cache.put(camera, proj);
			}
			
			if (dot < 0){
				// Project 3D -> View space
				boolean cacheMissing = false;
				if (proj[i][0] == null){
					proj[i][0] = multiply(cam, p1);
					cacheMissing = true;
				}
				if (proj[i][1] == null){
					proj[i][1] = multiply(cam, p2);
					cacheMissing = true;
				}
				if (proj[i][2] == null){
					proj[i][2] = multiply(cam, p3);
					cacheMissing = true;
				}
				if (cacheMissing){
					Point3D[][] clippedTriangles = clipTriangle(new Point3D(0, 0, 1), new Point3D(0, 0, 5.5), new Point3D(proj[i][0][0], proj[i][0][1], proj[i][0][2]), new Point3D(proj[i][1][0], proj[i][1][1], proj[i][1][2]), new Point3D(proj[i][2][0], proj[i][2][1], proj[i][2][2]));
					if (clippedTriangles == null){
						setProjectedPoint(i, 0, null);
						setProjectedPoint(i, 1, null);
						setProjectedPoint(i, 2, null);
						i++;
						continue;
					} else {
						Point3D[] tr1 = clippedTriangles[0];
						// Project View space -> 2D
						proj[i][0] = multiply(camera.getProjectionMatrix(), new double[]{tr1[0].getX(), tr1[0].getY(), tr1[0].getZ(), 1});
						proj[i][1] = multiply(camera.getProjectionMatrix(), new double[]{tr1[1].getX(), tr1[1].getY(), tr1[1].getZ(), 1});
						proj[i][2] = multiply(camera.getProjectionMatrix(), new double[]{tr1[2].getX(), tr1[2].getY(), tr1[2].getZ(), 1});
						setupTriangle(i, proj[i][0], proj[i][1], proj[i][2], false);
						if (clippedTriangles.length == 2){
							Point3D[] tr2 = clippedTriangles[1];
							double[] a = multiply(camera.getProjectionMatrix(), new double[]{tr2[0].getX(), tr2[0].getY(), tr2[0].getZ(), 1});
							double[] b = multiply(camera.getProjectionMatrix(), new double[]{tr2[1].getX(), tr2[1].getY(), tr2[1].getZ(), 1});
							double[] c = multiply(camera.getProjectionMatrix(), new double[]{tr2[2].getX(), tr2[2].getY(), tr2[2].getZ(), 1});
							setupTriangle(i, a, b, c, true);
						}
					}
				}
			} else {
				setProjectedPoint(i, 0, null);
				setProjectedPoint(i, 1, null);
				setProjectedPoint(i, 2, null);
			}
			i++;
		}
	}

	private void setupTriangle(int i, double[] p1, double[] p2, double[] p3, boolean extraList){
		// Scale
		double px1 = p1[0]/(p1[3] == 0 ? 1 : p1[3]);
		double py1 = p1[1]/(p1[3] == 0 ? 1 : p1[3]);
		double px2 = p2[0]/(p2[3] == 0 ? 1 : p2[3]);
		double py2 = p2[1]/(p2[3] == 0 ? 1 : p2[3]);
		double px3 = p3[0]/(p3[3] == 0 ? 1 : p3[3]);
		double py3 = p3[1]/(p3[3] == 0 ? 1 : p3[3]);
		double pz1 = p1[2];
		double pz2 = p2[2];
		double pz3 = p3[2];

		double bound = 1;
		if ((isOutside(px1, bound) && isOutside(px2, bound) && isOutside(px3, bound))
				|| (isOutside(py1, bound) && isOutside(py2, bound) && isOutside(py3, bound))){
			if (!extraList){
				setProjectedPoint(i, 0, null);
				setProjectedPoint(i, 1, null);
				setProjectedPoint(i, 2, null);
			}
			return;
		}

		px1 += 1;
		py1 += 1;
		px1 *= 0.5*getInstance().getWidth(); // getInstance() is referred to the Engine3D class
		py1 *= 0.5*getInstance().getHeight();
		px2 += 1;
		py2 += 1;
		px2 *= 0.5*getInstance().getWidth();
		py2 *= 0.5*getInstance().getHeight();
		px3 += 1;
		py3 += 1;
		px3 *= 0.5*getInstance().getWidth();
		py3 *= 0.5*getInstance().getHeight();

		if (extraList){
			this.extraProjected.put(i, new double[][]{{px1, py1, 1/p1[3]}, {px2, py2, 1/p2[3]}, {px3, py3, 1/p3[3]}});
		} else {
			setProjectedPoint(i, 0, new double[]{px1, py1, 1/p1[3]});
			setProjectedPoint(i, 1, new double[]{px2, py2, 1/p2[3]});
			setProjectedPoint(i, 2, new double[]{px3, py3, 1/p3[3]});
		}
	}
	
	public void render(Camera camera, List<Light> lights, GraphicsContext gc){
		if (this.showLines){
			gc.setStroke(this.color);
			gc.setLineWidth(1);
		}
		for (int i = 0; i < projected.length; i++){
			makeRendering(gc, i, camera, lights, projected[i][0], projected[i][1], projected[i][2]);
		}
		for (Map.Entry<Integer, double[][]> entry : this.extraProjected.entrySet()){
			makeRendering(gc, entry.getKey(), camera, lights, entry.getValue()[0], entry.getValue()[1], entry.getValue()[2]);
		}
	}

	private void makeRendering(GraphicsContext gc, int i, Camera camera, List<Light> lights, double[] proj1, double[] proj2, double[] proj3){
		if (proj1 == null || proj2 == null || proj3 == null) return;

		if (this.hiddenTriangles.contains(i)) return;

		Point2D p1 = new Point2D(proj1[0], proj1[1]);
		Point2D p2 = new Point2D(proj2[0], proj2[1]);
		Point2D p3 = new Point2D(proj3[0], proj3[1]);

		if (gc == null){
			calculateDepthBuffer((int)p1.getX(), (int)p1.getY(), (int)p2.getX(), (int)p2.getY(), (int)p3.getX(), (int)p3.getY(),
					proj1[2], proj2[2], proj3[2], camera);
		} else {
			if (this.showLines){
				gc.strokeLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
				gc.strokeLine(p2.getX(), p2.getY(), p3.getX(), p3.getY());
				gc.strokeLine(p1.getX(), p1.getY(), p3.getX(), p3.getY());
			} else {
				Point2D t1 = this.textureFaces[i] == null ? null : this.textureVertex[this.textureFaces[i][0]];
				Point2D t2 = this.textureFaces[i] == null ? null : this.textureVertex[this.textureFaces[i][1]];
				Point2D t3 = this.textureFaces[i] == null ? null : this.textureVertex[this.textureFaces[i][2]];

				if (t1 != null && t2 != null && t3 != null){
					t1 = t1.multiply(proj1[2]);
					t2 = t2.multiply(proj2[2]);
					t3 = t3.multiply(proj3[2]);

					renderTriangle((int)p1.getX(), (int)p1.getY(), (int)p2.getX(), (int)p2.getY(), (int)p3.getX(), (int)p3.getY(),
							t1.getX(), t1.getY(), t2.getX(), t2.getY(), t3.getX(), t3.getY(),
							proj1[2], proj2[2], proj3[2], i, gc, camera, lights, this.image);
				} else {
					Color c1 = this.facesColors != null ? this.facesColors[i][0] : this.vertexColors[i][0];
					Color c2 = this.facesColors != null ? this.facesColors[i][1] : this.vertexColors[i][1];
					Color c3 = this.facesColors != null ? this.facesColors[i][2] : this.vertexColors[i][2];

					renderColoredTriangle((int)p1.getX(), (int)p1.getY(), (int)p2.getX(), (int)p2.getY(), (int)p3.getX(), (int)p3.getY(),
							c1, c2, c3, proj1[2], proj2[2], proj3[2], i, gc, camera, lights);
				}
			}
		}
	}
	
	private void calculateDepthBuffer(int x1, int y1, int x2, int y2, int x3, int y3, double w1, double w2, double w3, Camera camera){
		if (y2 < y1){
			y1 = swap(y2, y2 = y1);
			x1 = swap(x2, x2 = x1);
			w1 = swap(w2, w2 = w1);
		}
		if (y3 < y1){
			y1 = swap(y3, y3 = y1);
			x1 = swap(x3, x3 = x1);
			w1 = swap(w3, w3 = w1);
		}
		if (y3 < y2){
			y2 = swap(y3, y3 = y2);
			x2 = swap(x3, x3 = x2);
			w2 = swap(w3, w3 = w2);
		}
		
		int dx1 = x2-x1;
		int dy1 = y2-y1;
		double dw1 = w2-w1;
		
		int dx2 = x3-x1;
		int dy2 = y3-y1;
		double dw2 = w3-w1;
		
		double col_w;
		
		double dax_step = 0, dbx_step = 0, dw1_step = 0, dw2_step = 0;
		
		if (dy1 != 0) dax_step = dx1/(double)Math.abs(dy1);
		if (dy2 != 0) dbx_step = dx2/(double)Math.abs(dy2);
		
		if (dy1 != 0) dw1_step = dw1/Math.abs(dy1);
		if (dy2 != 0) dw2_step = dw2/Math.abs(dy2);
		
		if (dy1 != 0){
			for (int i = y1; i <= y2; i++){
				int ax = x1+(int)((i-y1)*dax_step);
				int bx = x1+(int)((i-y1)*dbx_step);
				
				double col_sw = w1+(i-y1)*dw1_step;
				double col_ew = w1+(i-y1)*dw2_step;
				
				if (ax > bx){
					ax = swap(bx, bx = ax);
					col_sw = swap(col_ew, col_ew = col_sw);
				}
				
				double tstep = 1.0/(bx-ax);
				double t = 0.0;
				
				for (int j = ax; j < bx; j++){
					col_w = (1-t)*col_sw+t*col_ew;
					
					if (isInScene(j, i) && camera.depthBuffer[j][i] <= col_w){
						camera.depthBuffer[j][i] = col_w;
					}

					t += tstep;
				}
			}
		}
		
		dx1 = x3-x2;
		dy1 = y3-y2;
		dw1 = w3-w2;
		
		if (dy1 != 0) dax_step = dx1/(double)Math.abs(dy1);
		if (dy2 != 0) dbx_step = dx2/(double)Math.abs(dy2);
		
		dw1_step = 0;
		if (dy1 != 0) dw1_step = dw1/Math.abs(dy1);
		
		if (dy1 != 0){
			for (int i = y2; i <= y3; i++){
				int ax = x2+(int)((i-y2)*dax_step);
				int bx = x1+(int)((i-y1)*dbx_step);
				
				double col_sw = w2+(i-y2)*dw1_step;
				double col_ew = w1+(i-y1)*dw2_step;
				
				if (ax > bx){
					ax = swap(bx, bx = ax);
					col_sw = swap(col_ew, col_ew = col_sw);
				}
				
				double tstep = 1.0/(bx-ax);
				double t = 0.0;
				
				for (int j = ax; j < bx; j++){
					col_w = (1-t)*col_sw+t*col_ew;
					
					if (isInScene(j, i) && camera.depthBuffer[j][i] <= col_w){
						camera.depthBuffer[j][i] = col_w;
					}

					t += tstep;
				}
			}
		}
	}
	
	private void renderColoredTriangle(int x1, int y1, int x2, int y2, int x3, int y3, Color c1, Color c2, Color c3, 
											double w1, double w2, double w3, int index, GraphicsContext gc, Camera camera, List<Light> lights){
	
		double l1 = 0, l2 = 0, l3 = 0;
		for (Light light : lights){
			l1 += light.getLightIntensity(this.normals[index][0], this.trianglePoints[index][0]);
			l2 += light.getLightIntensity(this.normals[index][1], this.trianglePoints[index][1]);
			l3 += light.getLightIntensity(this.normals[index][2], this.trianglePoints[index][2]);
		}
		l1 = Math.min(1, l1);
		l2 = Math.min(1, l2);
		l3 = Math.min(1, l3);
										
		if (y2 < y1){
			y1 = swap(y2, y2 = y1);
			x1 = swap(x2, x2 = x1);
			c1 = swap(c2, c2 = c1);
			w1 = swap(w2, w2 = w1);
			l1 = swap(l2, l2 = l1);
		}
		if (y3 < y1){
			y1 = swap(y3, y3 = y1);
			x1 = swap(x3, x3 = x1);
			c1 = swap(c3, c3 = c1);
			w1 = swap(w3, w3 = w1);
			l1 = swap(l3, l3 = l1);
		}
		if (y3 < y2){
			y2 = swap(y3, y3 = y2);
			x2 = swap(x3, x3 = x2);
			c2 = swap(c3, c3 = c2);
			w2 = swap(w3, w3 = w2);
			l2 = swap(l3, l3 = l2);
		}
		
		int dx1 = x2-x1;
		int dy1 = y2-y1;
		double dr1 = c2.getRed()-c1.getRed();
		double dg1 = c2.getGreen()-c1.getGreen();
		double db1 = c2.getBlue()-c1.getBlue();
		double dw1 = w2-w1;
		double dl1 = l2-l1;
		
		int dx2 = x3-x1;
		int dy2 = y3-y1;
		double dr2 = c3.getRed()-c1.getRed();
		double dg2 = c3.getGreen()-c1.getGreen();
		double db2 = c3.getBlue()-c1.getBlue();
		double dw2 = w3-w1;
		double dl2 = l3-l1;
		
		double col_r, col_g, col_b, col_w, col_l;
		
		double dax_step = 0, dbx_step = 0, dr1_step = 0, dg1_step = 0, db1_step = 0, dr2_step = 0, dg2_step = 0, db2_step = 0, dw1_step = 0, dw2_step = 0, dl1_step = 0, dl2_step = 0;
		
		if (dy1 != 0) dax_step = dx1/(double)Math.abs(dy1);
		if (dy2 != 0) dbx_step = dx2/(double)Math.abs(dy2);
		
		if (dy1 != 0) dr1_step = dr1/Math.abs(dy1);
		if (dy1 != 0) dg1_step = dg1/Math.abs(dy1);
		if (dy1 != 0) db1_step = db1/Math.abs(dy1);
		if (dy1 != 0) dw1_step = dw1/Math.abs(dy1);
		if (dy1 != 0) dl1_step = dl1/Math.abs(dy1);
		
		if (dy2 != 0) dr2_step = dr2/Math.abs(dy2);
		if (dy2 != 0) dg2_step = dg2/Math.abs(dy2);
		if (dy2 != 0) db2_step = db2/Math.abs(dy2);
		if (dy2 != 0) dw2_step = dw2/Math.abs(dy2);
		if (dy2 != 0) dl2_step = dl2/Math.abs(dy2);

		if (dy1 != 0){
			for (int i = y1; i <= y2; i++){
				int ax = x1+(int)((i-y1)*dax_step);
				int bx = x1+(int)((i-y1)*dbx_step);
				
				double col_sr = c1.getRed()+(i-y1)*dr1_step;
				double col_sg = c1.getGreen()+(i-y1)*dg1_step;
				double col_sb = c1.getBlue()+(i-y1)*db1_step;
				double col_sw = w1+(i-y1)*dw1_step;
				double col_sl = l1+(i-y1)*dl1_step;
				
				double col_er = c1.getRed()+(i-y1)*dr2_step;
				double col_eg = c1.getGreen()+(i-y1)*dg2_step;
				double col_eb = c1.getBlue()+(i-y1)*db2_step;
				double col_ew = w1+(i-y1)*dw2_step;
				double col_el = l1+(i-y1)*dl2_step;
				
				if (ax > bx){
					ax = swap(bx, bx = ax);
					col_sr = swap(col_er, col_er = col_sr);
					col_sg = swap(col_eg, col_eg = col_sg);
					col_sb = swap(col_eb, col_eb = col_sb);
					col_sw = swap(col_ew, col_ew = col_sw);
					col_sl = swap(col_el, col_el = col_sl);
				}
				
				double tstep = 1.0/(bx-ax);
				double t = 0.0;
				
				for (int j = ax; j < bx; j++){
					col_r = (1-t)*col_sr+t*col_er;
					col_g = (1-t)*col_sg+t*col_eg;
					col_b = (1-t)*col_sb+t*col_eb;
					col_w = (1-t)*col_sw+t*col_ew;
					col_l = (1-t)*col_sl+t*col_el;
					
					if (isInScene(j, i) && camera.depthBuffer[j][i] <= col_w){
						camera.depthBuffer[j][i] = col_w;
						if (gc != null){
							Color color = Color.color(col_r, col_g, col_b);
							if (SHADOWS){
								for (Light light : lights){
									Camera cam2 = light.getCamera();
									double[] shadow = convertPoint(new double[]{j, i, col_w}, camera, cam2);
									int index_x = (int)Math.round(shadow[0]);
									int index_y = (int)Math.round(shadow[1]);
									if (index_x >= 0 && index_y >= 0 && index_x < cam2.depthBuffer.length && index_y < cam2.depthBuffer[0].length){
										double depth = cam2.depthBuffer[index_x][index_y];
										if (Math.abs(shadow[2]-depth) > SHADOW_FACTOR){
											color = color.darker();
										}
									}
								}
							}
							gc.getPixelWriter().setColor(j, i, Light.getLight(color, Math.max(col_l, 0)));
						}
					}

					t += tstep;
				}
			}
		}
		
		dx1 = x3-x2;
		dy1 = y3-y2;
		dr1 = c3.getRed()-c2.getRed();
		dg1 = c3.getGreen()-c2.getGreen();
		db1 = c3.getBlue()-c2.getBlue();
		dw1 = w3-w2;
		dl1 = l3-l2;
		
		if (dy1 != 0) dax_step = dx1/(double)Math.abs(dy1);
		if (dy2 != 0) dbx_step = dx2/(double)Math.abs(dy2);
		
		dr1_step = 0; dg1_step = 0; db1_step = 0; dw1_step = 0; dl1_step = 0;
		if (dy1 != 0) dr1_step = dr1/Math.abs(dy1);
		if (dy1 != 0) dg1_step = dg1/Math.abs(dy1);
		if (dy1 != 0) db1_step = db1/Math.abs(dy1);
		if (dy1 != 0) dw1_step = dw1/Math.abs(dy1);
		if (dy1 != 0) dl1_step = dl1/Math.abs(dy1);
		
		if (dy1 != 0){
			for (int i = y2; i <= y3; i++){
				int ax = x2+(int)((i-y2)*dax_step);
				int bx = x1+(int)((i-y1)*dbx_step);
				
				double col_sr = c2.getRed()+(i-y2)*dr1_step;
				double col_sg = c2.getGreen()+(i-y2)*dg1_step;
				double col_sb = c2.getBlue()+(i-y2)*db1_step;
				double col_sw = w2+(i-y2)*dw1_step;
				double col_sl = l2+(i-y2)*dl1_step;
				
				double col_er = c1.getRed()+(i-y1)*dr2_step;
				double col_eg = c1.getGreen()+(i-y1)*dg2_step;
				double col_eb = c1.getBlue()+(i-y1)*db2_step;
				double col_ew = w1+(i-y1)*dw2_step;
				double col_el = l1+(i-y1)*dl2_step;
				
				if (ax > bx){
					ax = swap(bx, bx = ax);
					col_sr = swap(col_er, col_er = col_sr);
					col_sg = swap(col_eg, col_eg = col_sg);
					col_sb = swap(col_eb, col_eb = col_sb);
					col_sw = swap(col_ew, col_ew = col_sw);
					col_sl = swap(col_el, col_el = col_sl);
				}
				
				double tstep = 1.0/(bx-ax);
				double t = 0.0;
				
				for (int j = ax; j < bx; j++){
					col_r = (1-t)*col_sr+t*col_er;
					col_g = (1-t)*col_sg+t*col_eg;
					col_b = (1-t)*col_sb+t*col_eb;
					col_w = (1-t)*col_sw+t*col_ew;
					col_l = (1-t)*col_sl+t*col_el;
					
					if (isInScene(j, i) && camera.depthBuffer[j][i] <= col_w){
						camera.depthBuffer[j][i] = col_w;
						if (gc != null){
							Color color = Color.color(col_r, col_g, col_b);
							if (SHADOWS){
								for (Light light : lights){
									Camera cam2 = light.getCamera();
									double[] shadow = convertPoint(new double[]{j, i, col_w}, camera, cam2);
									int index_x = (int)Math.round(shadow[0]);
									int index_y = (int)Math.round(shadow[1]);
									if (index_x >= 0 && index_y >= 0 && index_x < cam2.depthBuffer.length && index_y < cam2.depthBuffer[0].length){
										double depth = cam2.depthBuffer[index_x][index_y];
										if (Math.abs(shadow[2]-depth) > SHADOW_FACTOR){
											color = color.darker();
										}
									}
								}
							}
							gc.getPixelWriter().setColor(j, i, Light.getLight(color, Math.max(col_l, 0)));
						}
					}

					t += tstep;
				}
			}
		}
	}
	
	private void renderTriangle(int x1, int y1, int x2, int y2, int x3, int y3, double u1, double v1, double u2, double v2, double u3, double v3, 
								double w1, double w2, double w3, int index, GraphicsContext gc, Camera camera, List<Light> lights, Image image){
		
		double l1 = 0, l2 = 0, l3 = 0;
		for (Light light : lights){
			l1 += light.getLightIntensity(this.normals[index][0], this.trianglePoints[index][0]);
			l2 += light.getLightIntensity(this.normals[index][1], this.trianglePoints[index][1]);
			l3 += light.getLightIntensity(this.normals[index][2], this.trianglePoints[index][2]);
		}
		l1 = Math.min(1, l1);
		l2 = Math.min(1, l2);
		l3 = Math.min(1, l3);

		if (y2 < y1){
			y1 = swap(y2, y2 = y1);
			x1 = swap(x2, x2 = x1);
			u1 = swap(u2, u2 = u1);
			v1 = swap(v2, v2 = v1);
			w1 = swap(w2, w2 = w1);
			l1 = swap(l2, l2 = l1);
		}
		if (y3 < y1){
			y1 = swap(y3, y3 = y1);
			x1 = swap(x3, x3 = x1);
			u1 = swap(u3, u3 = u1);
			v1 = swap(v3, v3 = v1);
			w1 = swap(w3, w3 = w1);
			l1 = swap(l3, l3 = l1);
		}
		if (y3 < y2){
			y2 = swap(y3, y3 = y2);
			x2 = swap(x3, x3 = x2);
			u2 = swap(u3, u3 = u2);
			v2 = swap(v3, v3 = v2);
			w2 = swap(w3, w3 = w2);
			l2 = swap(l3, l3 = l2);
		}
		
		int dx1 = x2-x1;
		int dy1 = y2-y1;
		double du1 = u2-u1;
		double dv1 = v2-v1;
		double dw1 = w2-w1;
		double dl1 = l2-l1;
		
		int dx2 = x3-x1;
		int dy2 = y3-y1;
		double du2 = u3-u1;
		double dv2 = v3-v1;
		double dw2 = w3-w1;
		double dl2 = l3-l1;
		
		double tex_u, tex_v, tex_w, tex_l;
		
		double dax_step = 0, dbx_step = 0, du1_step = 0, dv1_step = 0, du2_step = 0, dv2_step = 0, dw1_step = 0, dw2_step = 0, dl1_step = 0, dl2_step = 0;
		
		if (dy1 != 0) dax_step = dx1/(double)Math.abs(dy1);
		if (dy2 != 0) dbx_step = dx2/(double)Math.abs(dy2);
		
		if (dy1 != 0) du1_step = du1/Math.abs(dy1);
		if (dy1 != 0) dv1_step = dv1/Math.abs(dy1);
		if (dy1 != 0) dw1_step = dw1/Math.abs(dy1);
		if (dy1 != 0) dl1_step = dl1/Math.abs(dy1);
		
		if (dy2 != 0) du2_step = du2/Math.abs(dy2);
		if (dy2 != 0) dv2_step = dv2/Math.abs(dy2);
		if (dy2 != 0) dw2_step = dw2/Math.abs(dy2);
		if (dy2 != 0) dl2_step = dl2/Math.abs(dy2);
		
		if (dy1 != 0){
			for (int i = y1; i <= y2; i++){
				int ax = x1+(int)((i-y1)*dax_step);
				int bx = x1+(int)((i-y1)*dbx_step);
				
				double tex_su = u1+(i-y1)*du1_step;
				double tex_sv = v1+(i-y1)*dv1_step;
				double tex_sw = w1+(i-y1)*dw1_step;
				double tex_sl = l1+(i-y1)*dl1_step;
				
				double tex_eu = u1+(i-y1)*du2_step;
				double tex_ev = v1+(i-y1)*dv2_step;
				double tex_ew = w1+(i-y1)*dw2_step;
				double tex_el = l1+(i-y1)*dl2_step;
				
				if (ax > bx){
					ax = swap(bx, bx = ax);
					tex_su = swap(tex_eu, tex_eu = tex_su);
					tex_sv = swap(tex_ev, tex_ev = tex_sv);
					tex_sw = swap(tex_ew, tex_ew = tex_sw);
					tex_sl = swap(tex_el, tex_el = tex_sl);
				}
				
				double tstep = 1.0/(bx-ax);
				double t = 0.0;
				
				for (int j = ax; j < bx; j++){
					tex_u = (1-t)*tex_su+t*tex_eu;
					tex_v = (1-t)*tex_sv+t*tex_ev;
					tex_w = (1-t)*tex_sw+t*tex_ew;
					tex_l = (1-t)*tex_sl+t*tex_el;
					
					int pix_x = (int)Math.round(tex_u/tex_w*(image.getWidth()-1)) % (int)(image.getWidth()-1);
					int pix_y = (int)Math.round(tex_v/tex_w*(image.getHeight()-1)) % (int)(image.getHeight()-1);
					if (pix_x < 0) pix_x = (int)(image.getWidth()-1)+pix_x;
					if (pix_y < 0) pix_y = (int)(image.getHeight()-1)+pix_y;

					//if (pix_x < 0 || pix_y < 0 || pix_x >= image.getWidth() || pix_y >= image.getHeight()) continue;

					if (isInScene(j, i) && camera.depthBuffer[j][i] <= tex_w){
						camera.depthBuffer[j][i] = tex_w;
						if (gc != null){
							Color color = image.getPixelReader().getColor(pix_x, pix_y);
							if (SHADOWS){
								for (Light light : lights){
									Camera cam2 = light.getCamera();
									double[] shadow = convertPoint(new double[]{j, i, tex_w}, camera, cam2);
									int index_x = (int)Math.round(shadow[0]);
									int index_y = (int)Math.round(shadow[1]);
									if (index_x >= 0 && index_y >= 0 && index_x < cam2.depthBuffer.length && index_y < cam2.depthBuffer[0].length){
										double depth = cam2.depthBuffer[index_x][index_y];
										if (Math.abs(shadow[2]-depth) > SHADOW_FACTOR){
											color = color.darker();
										}
									}
								}
							}
							gc.getPixelWriter().setColor(j, i, Light.getLight(color, Math.max(tex_l, 0)));
						}
					}
					
					t += tstep;
				}
			}
		}
		
		dx1 = x3-x2;
		dy1 = y3-y2;
		du1 = u3-u2;
		dv1 = v3-v2;
		dw1 = w3-w2;
		dl1 = l3-l2;
		
		if (dy1 != 0) dax_step = dx1/(double)Math.abs(dy1);
		if (dy2 != 0) dbx_step = dx2/(double)Math.abs(dy2);
		
		du1_step = 0; dv1_step = 0; dw1_step = 0; dl1_step = 0;
		if (dy1 != 0) du1_step = du1/Math.abs(dy1);
		if (dy1 != 0) dv1_step = dv1/Math.abs(dy1);
		if (dy1 != 0) dw1_step = dw1/Math.abs(dy1);
		if (dy1 != 0) dl1_step = dl1/Math.abs(dy1);
		
		if (dy1 != 0){
			for (int i = y2; i <= y3; i++){
				int ax = x2+(int)((i-y2)*dax_step);
				int bx = x1+(int)((i-y1)*dbx_step);
				
				double tex_su = u2+(i-y2)*du1_step;
				double tex_sv = v2+(i-y2)*dv1_step;
				double tex_sw = w2+(i-y2)*dw1_step;
				double tex_sl = l2+(i-y2)*dl1_step;
				
				double tex_eu = u1+(i-y1)*du2_step;
				double tex_ev = v1+(i-y1)*dv2_step;
				double tex_ew = w1+(i-y1)*dw2_step;
				double tex_el = l1+(i-y1)*dl2_step;
				
				if (ax > bx){
					ax = swap(bx, bx = ax);
					tex_su = swap(tex_eu, tex_eu = tex_su);
					tex_sv = swap(tex_ev, tex_ev = tex_sv);
					tex_sw = swap(tex_ew, tex_ew = tex_sw);
					tex_sl = swap(tex_el, tex_el = tex_sl);
				}
				
				double tstep = 1.0/(bx-ax);
				double t = 0.0;
				
				for (int j = ax; j < bx; j++){
					tex_u = (1-t)*tex_su+t*tex_eu;
					tex_v = (1-t)*tex_sv+t*tex_ev;
					tex_w = (1-t)*tex_sw+t*tex_ew;
					tex_l = (1-t)*tex_sl+t*tex_el;

					int pix_x = (int)Math.round(tex_u/tex_w*(image.getWidth()-1)) % (int)(image.getWidth()-1);
					int pix_y = (int)Math.round(tex_v/tex_w*(image.getHeight()-1)) % (int)(image.getHeight()-1);
					if (pix_x < 0) pix_x = (int)(image.getWidth()-1)+pix_x;
					if (pix_y < 0) pix_y = (int)(image.getHeight()-1)+pix_y;

					//if (pix_x < 0 || pix_y < 0 || pix_x >= image.getWidth() || pix_y >= image.getHeight()) continue;

					if (isInScene(j, i) && camera.depthBuffer[j][i] <= tex_w){
						camera.depthBuffer[j][i] = tex_w;
						if (gc != null){
							Color color = image.getPixelReader().getColor(pix_x, pix_y);
							if (SHADOWS){
								for (Light light : lights){
									Camera cam2 = light.getCamera();
									double[] shadow = convertPoint(new double[]{j, i, tex_w}, camera, cam2);
									int index_x = (int)Math.round(shadow[0]);
									int index_y = (int)Math.round(shadow[1]);
									if (index_x >= 0 && index_y >= 0 && index_x < cam2.depthBuffer.length && index_y < cam2.depthBuffer[0].length){
										double depth = cam2.depthBuffer[index_x][index_y];
										if (Math.abs(shadow[2]-depth) > SHADOW_FACTOR){
											color = color.darker();
										}
									}
								}
							}
							gc.getPixelWriter().setColor(j, i, Light.getLight(color, Math.max(tex_l, 0)));
						}
					}
					
					t += tstep;
				}
			}
		}
	}
	
	private static Map<String, double[]> loadMaterialLib(File file){
		if (!file.exists()) return null;
		Map<String, double[]> output = new HashMap<>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			String name = null;
			double[] current = null;
			while ((line = reader.readLine()) != null){
				if (line.toLowerCase().startsWith("newmtl")){
					if (name != null){
						output.put(name, current);
					}
					name = line.split(" ")[1];
					current = new double[3];
				} else if (line.toLowerCase().startsWith("kd") && current != null){
					current[0] = Double.parseDouble(line.split(" ")[1]);
					current[1] = Double.parseDouble(line.split(" ")[2]);
					current[2] = Double.parseDouble(line.split(" ")[3]);
				} else if (line.toLowerCase().startsWith("d") && current != null){
					// nothing
				} else if (line.toLowerCase().startsWith("map_kd")){
					
				}
			}
			if (name != null){
				output.put(name, current);
			}
			reader.close();
			return output;
		} catch (IOException ex){
			ex.printStackTrace();
			return null;
		}
	}
	
	public static Mesh loadFromFile(File file, double x, double y, double z, double scale, String singleObject, boolean invertVaxis){
		Map<String, double[]> mtllib = null;
		Image image = null; //new Image(Mesh.class.getResourceAsStream("/truck_red.jpg"));
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			List<Point3D> points = new ArrayList<>();
			List<Point3D> normals = new ArrayList<>();
			List<Point2D> vertexCoords = new ArrayList<>(); // Texture vertices
			Map<Integer, Color> colors = new HashMap<>();
			List<Color[]> facesColors = new ArrayList<>();
			List<int[]> faces = new ArrayList<>();
			List<int[]> textureFaces = new ArrayList<>();
			List<Point3D[]> normalsList = new ArrayList<>();
			String line;
			String currentMaterial = null;
			String currentObject = null;
			while ((line = reader.readLine()) != null){
				if (line.startsWith("mtllib ")){
					mtllib = loadMaterialLib(new File(file.getParent(), line.split(" ")[1]));
				} if (line.startsWith("v ")){
					String[] pieces = line.split(" ");
					double[] parray = new double[pieces.length-1];
					for (int i = 0; i < parray.length; i++){
						parray[i] = Double.parseDouble(line.split(" ")[i+1]);
					}
					points.add(new Point3D(parray[0]*scale+x, parray[1]*scale+y, parray[2]*scale+z));
					if (parray.length == 6){
						colors.put(colors.size() == 0 ? 0 : Collections.max(colors.keySet())+1, Color.color(parray[3], parray[4], parray[5]));
					} 
				} else if (line.startsWith("vn ")){
					String[] pieces = line.split(" ");
					double[] narray = new double[pieces.length-1];
					for (int i = 0; i < narray.length; i++){
						narray[i] = Double.parseDouble(line.split(" ")[i+1]);
					}
					normals.add(new Point3D(narray[0], narray[1], narray[2]));
				} else if (line.startsWith("vt ")){
					vertexCoords.add(new Point2D(Double.parseDouble(line.split(" ")[1]), invertVaxis ? 1-Double.parseDouble(line.split(" ")[2]) : Double.parseDouble(line.split(" ")[2])));
				} else if (line.startsWith("f ")){
					String[] pieces = line.split(" ");
					int[] farray = new int[pieces.length-1];
					int[] narray = null;
					int[] tarray = null;
					for (int i = 0; i < farray.length; i++){
						String[] lineArray = line.split(" ")[i+1].split("/");
						farray[i] = Integer.parseInt(lineArray[0])-1;
						if (lineArray.length == 3){
							if (!lineArray[1].equals("")){
								if (tarray == null) tarray = new int[pieces.length-1];
								tarray[i] = Integer.parseInt(lineArray[1])-1;
							}
							if (narray == null) narray = new int[pieces.length-1];
							narray[i] = Integer.parseInt(lineArray[2])-1;
						}
					}
					if (narray != null){
						for (int i = 1 ; i <= narray.length-2; i++){
							normalsList.add(new Point3D[]{normals.get(narray[0]), normals.get(narray[i]), normals.get(narray[i+1])});
						}
					}
					
					for (int i = 1; i <= farray.length-2; i++){
						faces.add(new int[]{farray[0], farray[i], farray[i+1]});
						textureFaces.add(tarray == null ? null : new int[]{tarray[0], tarray[i], tarray[i+1]});
					}
					
					if (mtllib != null){
						for (int i = 0; i < farray.length-2; i++){
							facesColors.add(new Color[]{Color.color(mtllib.get(currentMaterial)[0], mtllib.get(currentMaterial)[1], mtllib.get(currentMaterial)[2]), 
								Color.color(mtllib.get(currentMaterial)[0], mtllib.get(currentMaterial)[1], mtllib.get(currentMaterial)[2]), 
								Color.color(mtllib.get(currentMaterial)[0], mtllib.get(currentMaterial)[1], mtllib.get(currentMaterial)[2])});
						}
					}
				} else if (line.startsWith("usemtl ")){
					currentMaterial = line.split(" ")[1];
				} else if (line.startsWith("o ")){
					String now = currentObject;
					currentObject = line.split(" ")[1];
					if (now != null && singleObject != null && now.equals(singleObject)){
						break;
					}
					if (singleObject != null){
						faces.clear();
						facesColors.clear();
						normalsList.clear();
					}
				}
			}
			reader.close();
			
			Point3D[] ps = new Point3D[points.size()];
			Color[] cs = new Color[colors.size()];
			Color[][] fcs = new Color[facesColors.size()][3];
			for (int i = 0; i < ps.length; i++){
				ps[i] = points.get(i);
				if (colors.size() > 0){
					cs[i] = colors.get(i);
				}
			}
			
			int[][] fs = new int[faces.size()][3];
			Point3D[][] ns = new Point3D[normalsList.size()][3];
			for (int i = 0; i < fs.length; i++){
				fs[i] = faces.get(i);
			}
			for (int i = 0; i < ns.length; i++){
				ns[i] = normalsList.get(i);
			}
			
			for (int i = 0; i < fcs.length; i++){
				fcs[i] = facesColors.get(i);
			}
			
			Point2D[] vc = new Point2D[vertexCoords.size()];
			int[][] vf = new int[textureFaces.size()][3];
			
			for (int i = 0; i < vc.length; i++){
				vc[i] = vertexCoords.get(i);
			}
			for (int i = 0; i < vf.length; i++){
				vf[i] = textureFaces.get(i);
			}

			return new Mesh(image, ps, fs, vc.length == 0 ? null : vc, vf.length == 0 ? null : vf, cs.length == 0 ? null : cs, ns.length == 0 ? null : ns, fcs.length == 0 ? null : fcs);
			
		} catch (IOException ex){
			ex.printStackTrace();
			return null;
		}
	}
}
