import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.canvas.*;
import javafx.scene.paint.Color;
import javafx.scene.image.*;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.geometry.Point3D;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;

import java.util.*;
import java.io.*;

public class Rendering3D extends Application{
	private static final int WIDTH = 600;
	private static final int HEIGHT = 600;
	private Map<KeyCode, Boolean> keys = new HashMap<>();
	private volatile int frames, fps;
	private static final int FPS = 10;
	
	private static double aspectRatio = HEIGHT/WIDTH;
	private static double fov = Math.toRadians(45);
	private static double zFar = 100;
	private static double zNear = 1;
	private static final double[][] PROJECTION_MATRIX = {
		{aspectRatio*1/Math.tan(fov/2), 0, 0, 0},
		{0, 1/Math.tan(fov/2), 0, 0},
		{0, 0, 2/(zFar-zNear), -2*zNear/(zFar-zNear)-1},
		{0, 0, 1, 0}
	};
	private static Point3D LIGHT = new Point3D(0, -5, -10);
	
	private List<Cube> cubes = new ArrayList<>();
	private static double cx, cy, cz, rx, ry;
	private static boolean SHOW_LINES = false;
	private double mouseX, mouseY, mouseOldX, mouseOldY;
	private static double[][] depthBuffer = new double[WIDTH][HEIGHT];
	private static final Image COAL_IMAGE = new Image(Rendering3D.class.getResourceAsStream("coal.png"));
	private static final Image DIRT_IMAGE = new Image(Rendering3D.class.getResourceAsStream("dirt.png"));
	private static final Image STONE_IMAGE = new Image(Rendering3D.class.getResourceAsStream("stone.png"));
	
	private static class Cube {
		private Point3D[] points;
		private double[][][] projected;
		private Color[][] vertexColors;
		private int[][] faces;
		private Color[] colors;
		private Color color;
		private double angle;
		private Point2D[] textureVertex;
		private int[][] textureFaces;
		private Point3D[] normals;
		private Image image;
		private Point3D[][] trianglePoints;
		private Color[][] vertexCol;
		
		public Cube(Image image, Point3D[] points, int[][] faces, Point2D[] textureCoords, int[][] vertexFaces, Color[] colors){
			this.color = Color.WHITE; //Color.color(Math.random(), Math.random(), Math.random());
			this.image = image;
			this.points = points;
			this.projected = new double[faces.length][3][3];
			this.faces = faces;
			this.colors = colors;
			this.textureVertex = textureCoords;
			this.textureFaces = vertexFaces;
			this.normals = new Point3D[faces.length];
		}
		
		public Point3D[] getPoints(){
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
		
		private void evaluate(){
			int i = 0;
			this.vertexColors = new Color[this.faces.length][3];
			for (Point3D[] points : getTrianglePoints(vertexColors)){
				double[][] cam = multiply(multiply(getTranslation(-cx, -cy, -cz), multiply(getRotateX(rx), getRotateY(ry))), PROJECTION_MATRIX);
				
				// Apply transforms
				double[] p1 = new double[]{points[0].getX(), points[0].getY(), points[0].getZ(), 1};
				double[] p2 = new double[]{points[1].getX(), points[1].getY(), points[1].getZ(), 1};
				double[] p3 = new double[]{points[2].getX(), points[2].getY(), points[2].getZ(), 1};
				
				// Scale
				/*double factor = 0.1; //0.1;
				p1 = multiply(getScale(factor, factor, factor), p1);
				p2 = multiply(getScale(factor, factor, factor), p2);
				p3 = multiply(getScale(factor, factor, factor), p3);*/
				
				// Rotate
				p1 = multiply(getRotateX(this.angle+Math.PI/2), p1);
				p2 = multiply(getRotateX(this.angle+Math.PI/2), p2);
				p3 = multiply(getRotateX(this.angle+Math.PI/2), p3);
				p1 = multiply(getRotateY(this.angle), p1);
				p2 = multiply(getRotateY(this.angle), p2);
				p3 = multiply(getRotateY(this.angle), p3);
				p1 = multiply(getRotateZ(this.angle), p1);
				p2 = multiply(getRotateZ(this.angle), p2);
				p3 = multiply(getRotateZ(this.angle), p3);
				
				// Translate
				p1 = multiply(getTranslation(0, 0, 16), p1);
				p2 = multiply(getTranslation(0, 0, 16), p2);
				p3 = multiply(getTranslation(0, 0, 16), p3);
				
				Point3D point1 = new Point3D(p1[0], p1[1], p1[2]);
				Point3D point2 = new Point3D(p2[0], p2[1], p2[2]);
				Point3D point3 = new Point3D(p3[0], p3[1], p3[2]);
				
				Point3D normal = point2.subtract(point1).crossProduct(point3.subtract(point1));
				normal.normalize();
				this.normals[i] = normal;
				
				double dot = normal.dotProduct(point1.subtract(cx, cy, cz));
				
				if (dot < 0){					
					// Project 3D -> 2D
					p1 = multiply(cam, p1);
					p2 = multiply(cam, p2);
					p3 = multiply(cam, p3);
					
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
					if (px1 > 1 || px1 < -1 || py1 > 1 || py1 < -1 || pz1 > 1 || pz1 < -1
					 || px2 > 1 || px2 < -1 || py2 > 1 || py2 < -1 || pz2 > 1 || pz2 < -1
					 || px3 > 1 || px3 < -1 || py3 > 1 || py3 < -1 || pz3 > 1 || pz3 < -1){
						setProjectedPoint(i, 0, null);
						setProjectedPoint(i, 1, null);
						setProjectedPoint(i, 2, null);
						i++;
						continue;
					}
					
					px1 += 1;
					py1 += 1;
					px1 *= 0.5*WIDTH;
					py1 *= 0.5*HEIGHT;
					px2 += 1;
					py2 += 1;
					px2 *= 0.5*WIDTH;
					py2 *= 0.5*HEIGHT;
					px3 += 1;
					py3 += 1;
					px3 *= 0.5*WIDTH;
					py3 *= 0.5*HEIGHT;
					
					setProjectedPoint(i, 0, new double[]{px1, py1, p1[3]});
					setProjectedPoint(i, 1, new double[]{px2, py2, p2[3]});
					setProjectedPoint(i, 2, new double[]{px3, py3, p3[3]});
				} else {
					setProjectedPoint(i, 0, null);
					setProjectedPoint(i, 1, null);
					setProjectedPoint(i, 2, null);
				}
				
				i++;
			}
			//this.angle += 0.01*40/FPS;
		}
		
		public void render(GraphicsContext gc){
			if (SHOW_LINES){
				gc.setStroke(this.color);
				gc.setLineWidth(1);
			}

			for (int i = 0; i < projected.length; i++){
				if (projected[i][0] == null || projected[i][1] == null || projected[i][2] == null) continue;

				Point2D p1 = new Point2D(projected[i][0][0], projected[i][0][1]);
				Point2D p2 = new Point2D(projected[i][1][0], projected[i][1][1]);
				Point2D p3 = new Point2D(projected[i][2][0], projected[i][2][1]);
				
				if (SHOW_LINES){
					gc.strokeLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
					gc.strokeLine(p2.getX(), p2.getY(), p3.getX(), p3.getY());
					gc.strokeLine(p1.getX(), p1.getY(), p3.getX(), p3.getY());
				}

				if (!SHOW_LINES){
					if (this.colors == null){
						Point2D t1 = this.textureVertex[this.textureFaces[i][0]].multiply(1/projected[i][0][2]);
						Point2D t2 = this.textureVertex[this.textureFaces[i][1]].multiply(1/projected[i][1][2]);
						Point2D t3 = this.textureVertex[this.textureFaces[i][2]].multiply(1/projected[i][2][2]);
						
						projected[i][0][2] = 1/projected[i][0][2];
						projected[i][1][2] = 1/projected[i][1][2];
						projected[i][2][2] = 1/projected[i][2][2];
						
						renderTriangle((int)p1.getX(), (int)p1.getY(), (int)p2.getX(), (int)p2.getY(), (int)p3.getX(), (int)p3.getY(),
								t1.getX(), t1.getY(), t2.getX(), t2.getY(), t3.getX(), t3.getY(),
								projected[i][0][2], projected[i][1][2], projected[i][2][2], i, gc, this.image);
					} else {
						
						projected[i][0][2] = 1/projected[i][0][2];
						projected[i][1][2] = 1/projected[i][1][2];
						projected[i][2][2] = 1/projected[i][2][2];
						
						renderColoredTriangle((int)p1.getX(), (int)p1.getY(), (int)p2.getX(), (int)p2.getY(), (int)p3.getX(), (int)p3.getY(),
								this.vertexColors[i][0], this.vertexColors[i][1], this.vertexColors[i][2],
								projected[i][0][2], projected[i][1][2], projected[i][2][2], i, gc);
					}
				}
			}
		}
		
		private void renderColoredTriangle(int x1, int y1, int x2, int y2, int x3, int y3, Color c1, Color c2, Color c3, 
											double w1, double w2, double w3, int index, GraphicsContext gc){
			if (y2 < y1){
				y1 = swap(y2, y2 = y1);
				x1 = swap(x2, x2 = x1);
				c1 = swap(c2, c2 = c1);
				w1 = swap(w2, w2 = w1);
			}
			if (y3 < y1){
				y1 = swap(y3, y3 = y1);
				x1 = swap(x3, x3 = x1);
				c1 = swap(c3, c3 = c1);
				w1 = swap(w3, w3 = w1);
			}
			if (y3 < y2){
				y2 = swap(y3, y3 = y2);
				x2 = swap(x3, x3 = x2);
				c2 = swap(c3, c3 = c2);
				w2 = swap(w3, w3 = w2);
			}
			
			int dx1 = x2-x1;
			int dy1 = y2-y1;
			double dr1 = c2.getRed()-c1.getRed();
			double dg1 = c2.getGreen()-c1.getGreen();
			double db1 = c2.getBlue()-c1.getBlue();
			double dw1 = w2-w1;
			
			int dx2 = x3-x1;
			int dy2 = y3-y1;
			double dr2 = c3.getRed()-c1.getRed();
			double dg2 = c3.getGreen()-c1.getGreen();
			double db2 = c3.getBlue()-c1.getBlue();
			double dw2 = w3-w1;
			
			double col_r, col_g, col_b, col_w;
			
			double dax_step = 0, dbx_step = 0, dr1_step = 0, dg1_step = 0, db1_step = 0, dr2_step = 0, dg2_step = 0, db2_step = 0, dw1_step = 0, dw2_step = 0;
			
			if (dy1 != 0) dax_step = dx1/(double)Math.abs(dy1);
			if (dy2 != 0) dbx_step = dx2/(double)Math.abs(dy2);
			
			if (dy1 != 0) dr1_step = dr1/Math.abs(dy1);
			if (dy1 != 0) dg1_step = dg1/Math.abs(dy1);
			if (dy1 != 0) db1_step = db1/Math.abs(dy1);
			if (dy1 != 0) dw1_step = dw1/Math.abs(dy1);
			
			if (dy2 != 0) dr2_step = dr2/Math.abs(dy2);
			if (dy2 != 0) dg2_step = dg2/Math.abs(dy2);
			if (dy2 != 0) db2_step = db2/Math.abs(dy2);
			if (dy2 != 0) dw2_step = dw2/Math.abs(dy2);

			if (dy1 != 0){
				for (int i = y1; i <= y2; i++){
					int ax = x1+(int)((i-y1)*dax_step);
					int bx = x1+(int)((i-y1)*dbx_step);
					
					double col_sr = c1.getRed()+(i-y1)*dr1_step;
					double col_sg = c1.getGreen()+(i-y1)*dg1_step;
					double col_sb = c1.getBlue()+(i-y1)*db1_step;
					double col_sw = w1+(i-y1)*dw1_step;
					
					double col_er = c1.getRed()+(i-y1)*dr2_step;
					double col_eg = c1.getGreen()+(i-y1)*dg2_step;
					double col_eb = c1.getBlue()+(i-y1)*db2_step;
					double col_ew = w1+(i-y1)*dw2_step;
					
					if (ax > bx){
						ax = swap(bx, bx = ax);
						col_sr = swap(col_er, col_er = col_sr);
						col_sg = swap(col_eg, col_eg = col_sg);
						col_sb = swap(col_eb, col_eb = col_sb);
						col_sw = swap(col_ew, col_ew = col_sw);
					}
					
					col_r = col_sr;
					col_g = col_sg;
					col_b = col_sb;
					col_w = col_sw;
					
					double tstep = 1.0/(bx-ax);
					double t = 0.0;
					
					for (int j = ax; j < bx; j++){
						col_r = (1-t)*col_sr+t*col_er;
						col_g = (1-t)*col_sg+t*col_eg;
						col_b = (1-t)*col_sb+t*col_eb;
						col_w = (1-t)*col_sw+t*col_ew;
						
						if (depthBuffer[j][i] <= col_w){
							depthBuffer[j][i] = col_w;
							gc.getPixelWriter().setColor(j, i, getLight(Color.color(col_r, col_g, col_b), this.normals[index], this.trianglePoints[index][0]));
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
			
			if (dy1 != 0) dax_step = dx1/(double)Math.abs(dy1);
			if (dy2 != 0) dbx_step = dx2/(double)Math.abs(dy2);
			
			dr1_step = 0; dg1_step = 0; db1_step = 0;
			if (dy1 != 0) dr1_step = dr1/Math.abs(dy1);
			if (dy1 != 0) dg1_step = dg1/Math.abs(dy1);
			if (dy1 != 0) db1_step = db1/Math.abs(dy1);
			if (dy1 != 0) dw1_step = dw1/Math.abs(dy1);
			
			if (dy1 != 0){
				for (int i = y2; i <= y3; i++){
					int ax = x2+(int)((i-y2)*dax_step);
					int bx = x1+(int)((i-y1)*dbx_step);
					
					double col_sr = c2.getRed()+(i-y2)*dr1_step;
					double col_sg = c2.getGreen()+(i-y2)*dg1_step;
					double col_sb = c2.getBlue()+(i-y2)*db1_step;
					double col_sw = w2+(i-y2)*dw1_step;
					
					double col_er = c1.getRed()+(i-y1)*dr2_step;
					double col_eg = c1.getGreen()+(i-y1)*dg2_step;
					double col_eb = c1.getBlue()+(i-y1)*db2_step;
					double col_ew = w1+(i-y1)*dw2_step;
					
					if (ax > bx){
						ax = swap(bx, bx = ax);
						col_sr = swap(col_er, col_er = col_sr);
						col_sg = swap(col_eg, col_eg = col_sg);
						col_sb = swap(col_eb, col_eb = col_sb);
						col_sw = swap(col_ew, col_ew = col_sw);
					}
					
					col_r = col_sr;
					col_g = col_sg;
					col_b = col_sb;
					col_w = col_sw;
					
					double tstep = 1.0/(bx-ax);
					double t = 0.0;
					
					for (int j = ax; j < bx; j++){
						col_r = (1-t)*col_sr+t*col_er;
						col_g = (1-t)*col_sg+t*col_eg;
						col_b = (1-t)*col_sb+t*col_eb;
						col_w = (1-t)*col_sw+t*col_ew;
						
						if (depthBuffer[j][i] <= col_w){
							depthBuffer[j][i] = col_w;
							gc.getPixelWriter().setColor(j, i, getLight(Color.color(col_r, col_g, col_b), this.normals[index], this.trianglePoints[index][0]));
						}

						t += tstep;
					}
				}
			}
		}
		
		private void renderTriangle(int x1, int y1, int x2, int y2, int x3, int y3, double u1, double v1, double u2, double v2, double u3, double v3, 
									double w1, double w2, double w3, int index, GraphicsContext gc, Image image){
			if (y2 < y1){
				y1 = swap(y2, y2 = y1);
				x1 = swap(x2, x2 = x1);
				u1 = swap(u2, u2 = u1);
				v1 = swap(v2, v2 = v1);
				w1 = swap(w2, w2 = w1);
			}
			if (y3 < y1){
				y1 = swap(y3, y3 = y1);
				x1 = swap(x3, x3 = x1);
				u1 = swap(u3, u3 = u1);
				v1 = swap(v3, v3 = v1);
				w1 = swap(w3, w3 = w1);
			}
			if (y3 < y2){
				y2 = swap(y3, y3 = y2);
				x2 = swap(x3, x3 = x2);
				u2 = swap(u3, u3 = u2);
				v2 = swap(v3, v3 = v2);
				w2 = swap(w3, w3 = w2);
			}
			
			int dx1 = x2-x1;
			int dy1 = y2-y1;
			double du1 = u2-u1;
			double dv1 = v2-v1;
			double dw1 = w2-w1;
			
			int dx2 = x3-x1;
			int dy2 = y3-y1;
			double du2 = u3-u1;
			double dv2 = v3-v1;
			double dw2 = w3-w1;
			
			double tex_u, tex_v, tex_w;
			
			double dax_step = 0, dbx_step = 0, du1_step = 0, dv1_step = 0, du2_step = 0, dv2_step = 0, dw1_step = 0, dw2_step = 0;
			
			if (dy1 != 0) dax_step = dx1/(double)Math.abs(dy1);
			if (dy2 != 0) dbx_step = dx2/(double)Math.abs(dy2);
			
			if (dy1 != 0) du1_step = du1/Math.abs(dy1);
			if (dy1 != 0) dv1_step = dv1/Math.abs(dy1);
			if (dy1 != 0) dw1_step = dw1/Math.abs(dy1);
			
			if (dy2 != 0) du2_step = du2/Math.abs(dy2);
			if (dy2 != 0) dv2_step = dv2/Math.abs(dy2);
			if (dy2 != 0) dw2_step = dw2/Math.abs(dy2);
			
			if (dy1 != 0){
				for (int i = y1; i <= y2; i++){
					int ax = x1+(int)((i-y1)*dax_step);
					int bx = x1+(int)((i-y1)*dbx_step);
					
					double tex_su = u1+(i-y1)*du1_step;
					double tex_sv = v1+(i-y1)*dv1_step;
					double tex_sw = w1+(i-y1)*dw1_step;
					
					double tex_eu = u1+(i-y1)*du2_step;
					double tex_ev = v1+(i-y1)*dv2_step;
					double tex_ew = w1+(i-y1)*dw2_step;
					
					if (ax > bx){
						ax = swap(bx, bx = ax);
						tex_su = swap(tex_eu, tex_eu = tex_su);
						tex_sv = swap(tex_ev, tex_ev = tex_sv);
						tex_sw = swap(tex_ew, tex_ew = tex_sw);
					}
					
					tex_u = tex_su;
					tex_v = tex_sv;
					tex_w = tex_sw;
					
					double tstep = 1.0/(bx-ax);
					double t = 0.0;
					
					for (int j = ax; j < bx; j++){
						tex_u = (1-t)*tex_su+t*tex_eu;
						tex_v = (1-t)*tex_sv+t*tex_ev;
						tex_w = (1-t)*tex_sw+t*tex_ew;
						
						int pix_x = (int)(tex_u/tex_w*(image.getWidth())) % ((int)image.getWidth());
						int pix_y = (int)(tex_v/tex_w*(image.getHeight())) % ((int)image.getHeight());
						
						if (pix_x < 0) pix_x = (int)image.getWidth()-pix_x;
						if (pix_y < 0) pix_y = (int)image.getHeight()-pix_y;
						if (depthBuffer[j][i] <= tex_w){
							depthBuffer[j][i] = tex_w;
							gc.getPixelWriter().setColor(j, i, getLight(image.getPixelReader().getColor(pix_x, pix_y), this.normals[index], this.trianglePoints[index][0]));
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
			
			if (dy1 != 0) dax_step = dx1/(double)Math.abs(dy1);
			if (dy2 != 0) dbx_step = dx2/(double)Math.abs(dy2);
			
			du1_step = 0; dv1_step = 0; dw1_step = 0;
			if (dy1 != 0) du1_step = du1/Math.abs(dy1);
			if (dy1 != 0) dv1_step = dv1/Math.abs(dy1);
			if (dy1 != 0) dw1_step = dw1/Math.abs(dy1);
			
			if (dy1 != 0){
				for (int i = y2; i <= y3; i++){
					int ax = x2+(int)((i-y2)*dax_step);
					int bx = x1+(int)((i-y1)*dbx_step);
					
					double tex_su = u2+(i-y2)*du1_step;
					double tex_sv = v2+(i-y2)*dv1_step;
					double tex_sw = w2+(i-y2)*dw1_step;
					
					double tex_eu = u1+(i-y1)*du2_step;
					double tex_ev = v1+(i-y1)*dv2_step;
					double tex_ew = w1+(i-y1)*dw2_step;
					
					if (ax > bx){
						ax = swap(bx, bx = ax);
						tex_su = swap(tex_eu, tex_eu = tex_su);
						tex_sv = swap(tex_ev, tex_ev = tex_sv);
						tex_sw = swap(tex_ew, tex_ew = tex_sw);
					}
					
					tex_u = tex_su;
					tex_v = tex_sv;
					tex_w = tex_sw;
					
					double tstep = 1.0/(bx-ax);
					double t = 0.0;
					
					for (int j = ax; j < bx; j++){
						tex_u = (1-t)*tex_su+t*tex_eu;
						tex_v = (1-t)*tex_sv+t*tex_ev;
						tex_w = (1-t)*tex_sw+t*tex_ew;
						
						int pix_x = (int)(tex_u/tex_w*(image.getWidth())) % ((int)image.getWidth());
						int pix_y = (int)(tex_v/tex_w*(image.getHeight())) % ((int)image.getHeight());
						
						if (pix_x < 0) pix_x = (int)image.getWidth()-pix_x;
						if (pix_y < 0) pix_y = (int)image.getHeight()-pix_y;
						if (depthBuffer[j][i] <= tex_w){
							depthBuffer[j][i] = tex_w;
							gc.getPixelWriter().setColor(j, i, getLight(image.getPixelReader().getColor(pix_x, pix_y), this.normals[index], this.trianglePoints[index][0]));
						}
						
						t += tstep;
					}
				}
			}
		}
	}
	
	private static <T> T swap(T a, T b){
		return a;
	}
	
	@Override
	public void start(Stage stage){
		Thread counter = new Thread(() -> {
			while (true){
				try {
					this.fps = this.frames;
					this.frames = 0;
					Thread.sleep(1000);
				} catch (InterruptedException ex){
					ex.printStackTrace();
				}
			}
		});
		counter.setDaemon(true);
		counter.start();
		
		stage.setTitle("3D Graphics");
		StackPane pane = new StackPane();
		Canvas canvas = new Canvas(WIDTH, HEIGHT);
		canvas.setFocusTraversable(true);
		canvas.setOnKeyPressed(e -> this.keys.put(e.getCode(), true));
		canvas.setOnKeyReleased(e -> this.keys.put(e.getCode(), false));
		canvas.setOnMousePressed(e -> {
			this.mouseOldX = e.getX();
			this.mouseOldY = e.getY();
		});
		canvas.setOnMouseDragged(e -> {
			//rx += (mouseOldY-e.getY())/100;
			ry += Math.toRadians(e.getX()-mouseOldX);
			this.mouseOldX = e.getX();
			this.mouseOldY = e.getY();
		});
		GraphicsContext gc = canvas.getGraphicsContext2D();
		pane.getChildren().add(canvas);
		
		Random random = new Random();
		
		/*for (int i = 0; i < 1; i++){
			for (int j = 0; j < 1; j++){
				for (int k = 0; k < 1; k++){
					//if (!isPrime(i+j+k)) continue;
					cubes.add(new Cube(switch(random.nextInt(3)){
						case 0 -> COAL_IMAGE;
						case 1 -> DIRT_IMAGE;
						case 2 -> STONE_IMAGE;
						default -> null;
					}, new Point3D[]{
						new Point3D(i, k, j), new Point3D(i, 1+k, j), new Point3D(1+i, 1+k, j),
						new Point3D(1+i, k, j), new Point3D(i, k, 1+j), new Point3D(i, 1+k, 1+j), 
						new Point3D(1+i, 1+k, 1+j), new Point3D(1+i, k, 1+j)}, new int[][]{
							{0, 1, 2}, {0, 2, 3}, {3, 2, 6},
							{3, 6, 7}, {7, 6, 5}, {7, 5, 4},
							{4, 5, 1}, {4, 1, 0}, {1, 5, 6},
							{1, 6, 2}, {4, 0, 3}, {4, 3, 7}
					}, new Point2D[]{
						new Point2D(0, 1), new Point2D(0, 0), new Point2D(1, 0), new Point2D(1, 1)
					}, new int[][]{
						{0, 1, 2}, {0, 2, 3}, {0, 1, 2}, {0, 2, 3},
						{0, 1, 2}, {0, 2, 3}, {0, 1, 2}, {0, 2, 3},
						{0, 1, 2}, {0, 2, 3}, {0, 1, 2}, {0, 2, 3},
						{0, 1, 2}, {0, 2, 3}, {0, 1, 2}, {0, 2, 3}
					}, null));
				}
			}
		}*/
		
		cubes.add(loadCubeFromFile(new File("model.obj"), 0, 5, 0, 0.05));
		
		Timeline loop = new Timeline(new KeyFrame(Duration.millis(1000.0/FPS), e -> update(gc)));
		loop.setCycleCount(Animation.INDEFINITE);
		loop.play();
		
		AnimationTimer timer = new AnimationTimer(){
			@Override
			public void handle(long time){
				Rendering3D.this.frames++;
			}
		};
		timer.start();
		
		stage.setScene(new Scene(pane, WIDTH, HEIGHT));
		stage.show();
	}
	
	private static boolean isPrime(int n){
		if (n == 0 || n == 1 || n == 2) return true;
		for (int i = 2; i < Math.sqrt(n); i++){
			if (n % i == 0) return false;
		}
		return true;
	}
	
	private void update(GraphicsContext gc){
		gc.clearRect(0, 0, WIDTH, HEIGHT);
		depthBuffer = new double[WIDTH][HEIGHT];
		gc.setFill(Color.BLACK);
		gc.fillRect(0, 0, WIDTH, HEIGHT);
		
		double speed = 0.3;
		if (this.keys.getOrDefault(KeyCode.W, false)){
			cx += speed*Math.cos(ry+Math.PI/2);
			cz += speed*Math.sin(ry+Math.PI/2);
			this.keys.put(KeyCode.W, false);
		} else if (this.keys.getOrDefault(KeyCode.A, false)){
			cx += speed*Math.cos(ry+Math.PI);
			cz += speed*Math.sin(ry+Math.PI);
			this.keys.put(KeyCode.A, false);
		} else if (this.keys.getOrDefault(KeyCode.S, false)){
			cx -= speed*Math.cos(ry+Math.PI/2);
			cz -= speed*Math.sin(ry+Math.PI/2);
			this.keys.put(KeyCode.S, false);
		} else if (this.keys.getOrDefault(KeyCode.D, false)){
			cx -= speed*Math.cos(ry+Math.PI);
			cz -= speed*Math.sin(ry+Math.PI);
			this.keys.put(KeyCode.D, false);
		} else if (this.keys.getOrDefault(KeyCode.SPACE, false)){
			cy -= speed;
			this.keys.put(KeyCode.SPACE, false);
		} else if (this.keys.getOrDefault(KeyCode.Z, false)){
			cy += speed;
			this.keys.put(KeyCode.Z, false);
		} else if (this.keys.getOrDefault(KeyCode.F1, false)){
			SHOW_LINES = !SHOW_LINES;
			this.keys.put(KeyCode.F1, false);
		} else if (this.keys.getOrDefault(KeyCode.R, false)){
			cx = 0;
			cy = 0;
			cz = 0;
			rx = 0;
			ry = 0;
			this.keys.put(KeyCode.R, true);
		}
				
		for (Cube cube : cubes){
			cube.evaluate();
			cube.render(gc);
		}
		
		double lspeed = 5;
		//double[] rotationV = multiply(getRotateY(0.01*40/FPS), new double[]{LIGHT.getX(), LIGHT.getY(), LIGHT.getZ()});
		//LIGHT = new Point3D(rotationV[0], rotationV[1], rotationV[2]);
		//cx = LIGHT.getX();
		//cy = LIGHT.getY();
		//cz = LIGHT.getZ();
		//ry = Math.atan2(cz, cx)+Math.PI/2;
		LIGHT = new Point3D(cx, cy, cz);
		
		gc.setFill(Color.WHITE);
		gc.fillText(String.format("%.2f %.2f FPS:%d (%d)\nCx: %.2f Cy: %.2f Cz: %.2f\nLight: %s", Math.toDegrees(rx), Math.toDegrees(ry), fps, FPS, cx, cy, cz, LIGHT), 30, 30);
	}
	
	private static double[][] getRotateX(double angle){
		return new double[][]{
			{1, 0, 0, 0},
			{0, Math.cos(angle), -Math.sin(angle), 0},
			{0, Math.sin(angle), Math.cos(angle), 0},
			{0, 0, 0, 1}
		};
	}
	
	private static double[][] getRotateY(double angle){
		return new double[][]{
			{Math.cos(angle), 0, Math.sin(angle), 0},
			{0, 1, 0, 0},
			{-Math.sin(angle), 0, Math.cos(angle), 0},
			{0, 0, 0, 1}
		};
	}
	
	private static double[][] getRotateZ(double angle){
		return new double[][]{
			{Math.cos(angle), -Math.sin(angle), 0, 0},
			{Math.sin(angle), Math.cos(angle), 0, 0},
			{0, 0, 1, 0},
			{0, 0, 0, 1}
		};
	}
	
	private static double[][] getTranslation(double tx, double ty, double tz){
		return new double[][]{
			{1, 0, 0, tx},
			{0, 1, 0, ty},
			{0, 0, 1, tz},
			{0, 0, 0, 1}
		};
	}
	
	private static double[][] getScale(double sx, double sy, double sz){
		return new double[][]{
			{sx, 0, 0, 0},
			{0, sy, 0, 0},
			{0, 0, sz, 0},
			{0, 0, 0, 1}
		};
	}
	
	private static double[] multiply(double[][] mat, double[] vect){
		double[] out = new double[mat.length];
		
		for (int i = 0; i < out.length; i++){
			double sum = 0;
			for (int j = 0; j < vect.length; j++){
				sum += mat[i][j]*vect[j];
			}
			out[i] = sum;
		}

		return out;
	}
	
	private static double[][] multiply(double[][] mat1, double[][] mat2){
		double[][] out = new double[mat2.length][mat1[0].length];
		
		for (int i = 0; i < mat1[0].length; i++){
			for (int j = 0; j < mat2.length; j++){
				double sum = 0;
				for (int k = 0; k < mat1.length; k++){
					sum += mat1[k][i]*mat2[j][k];
				}
				out[j][i] = sum;
			}
		}

		return out;
	}
	
	private static Color getLight(Color color, Point3D normal, Point3D point){
		double red = color.getRed();
		double green = color.getGreen();
		double blue = color.getBlue();
		double intensity = Math.max(0, 1-point.subtract(LIGHT).magnitude()*0.005);
		
		double factor = normal.dotProduct(point.subtract(LIGHT).normalize());
		
		if (factor < -1) factor = 1;
		else if (factor > 0) factor = 0;
		else factor = Math.abs(factor);
		
		red = intensity * red * factor;
		green = intensity * green * factor;
		blue = intensity * blue * factor;
		return Color.color(red, green, blue);
	}
	
	private Map<String, double[]> loadMaterialLib(File file){
		if (!file.exists()) return null;
		Map<String, double[]> output = new HashMap<>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			String name = null;
			double[] current = null;
			while ((line = reader.readLine()) != null){
				if (line.startsWith("newmtl")){
					if (name != null){
						output.put(name, current);
					}
					name = line.split(" ")[1];
					current = new double[3];
				} else if (line.startsWith("Kd") && current != null){
					current[0] = Double.parseDouble(line.split(" ")[1]);
					current[1] = Double.parseDouble(line.split(" ")[2]);
					current[2] = Double.parseDouble(line.split(" ")[3]);
				} else if (line.startsWith("D") && current != null){
					// nothing
				}
			}
			reader.close();
			return output;
		} catch (IOException ex){
			ex.printStackTrace();
			return null;
		}
	}
	
	private Cube loadCubeFromFile(File file, double x, double y, double z, double scale){
		
		Map<String, double[]> mtllib = loadMaterialLib(new File("model.mtl"));
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			List<Point3D> points = new ArrayList<>();
			Map<Integer, Color> colors = new HashMap<>();
			List<int[]> faces = new ArrayList<>();
			String line;
			String currentMaterial = null;
			while ((line = reader.readLine()) != null){
				if (line.startsWith("v ")){
					String[] pieces = line.split(" ");
					double[] parray = new double[pieces.length-1];
					for (int i = 0; i < parray.length; i++){
						parray[i] = Double.parseDouble(line.split(" ")[i+1]);
					}
					points.add(new Point3D(parray[0]*scale+x, parray[1]*scale+y, parray[2]*scale+z));
					if (parray.length == 6){
						colors.put(points.size()-1, Color.color(parray[3], parray[4], parray[5]));
					} 
				} else if (line.startsWith("f ")){
					String[] pieces = line.split(" ");
					int[] farray = new int[pieces.length-1];
					for (int i = 0; i < farray.length; i++){
						farray[i] = Integer.parseInt(line.split(" ")[i+1].split("/")[0])-1;
					}
					faces.add(new int[]{farray[0], farray[1], farray[2]});
					if (farray.length == 4){ // Squares
						faces.add(new int[]{farray[0], farray[2], farray[3]});
					}
					if (mtllib != null){
						colors.put(farray[0], Color.color(mtllib.get(currentMaterial)[0], mtllib.get(currentMaterial)[1], mtllib.get(currentMaterial)[2]));
						colors.put(farray[1], Color.color(mtllib.get(currentMaterial)[0], mtllib.get(currentMaterial)[1], mtllib.get(currentMaterial)[2]));
						colors.put(farray[2], Color.color(mtllib.get(currentMaterial)[0], mtllib.get(currentMaterial)[1], mtllib.get(currentMaterial)[2]));
						if (farray.length == 4){
							colors.put(farray[3], Color.color(mtllib.get(currentMaterial)[0], mtllib.get(currentMaterial)[1], mtllib.get(currentMaterial)[2]));
						}
					}
				} else if (line.startsWith("usemtl")){
					currentMaterial = line.split(" ")[1];
				}
			}
			reader.close();
			
			Point3D[] ps = new Point3D[points.size()];
			Color[] cs = new Color[points.size()];
			for (int i = 0; i < ps.length; i++){
				ps[i] = points.get(i);
				if (colors.size() > 0) cs[i] = colors.get(i);
			}
			
			int[][] fs = new int[faces.size()][3];
			for (int i = 0; i < fs.length; i++){
				fs[i] = faces.get(i);
			}
			
			return new Cube(null, ps, fs, null, null, cs.length == 0 ? null : cs);
			
		} catch (IOException ex){
			ex.printStackTrace();
			return null;
		}
	}
	
	public static void main(String[] args){
		launch(args);
	}
}
