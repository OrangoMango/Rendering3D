package com.orangomango.rendering3d.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

import java.util.*;

import com.orangomango.rendering3d.Engine3D;

public class ProjectedTriangle{
	private double[] v1, v2, v3;
	private boolean imageTriangle;
	private Camera camera;
	private boolean transparent;
	private double lightIntensity;

	// Light data
	private MeshVertex vex1, vex2, vex3;
	private List<Light> lights = new ArrayList<>();

	// Image triangle
	private Point2D tex1, tex2, tex3;
	private Image image;

	// Coloed triangle
	private Color col1, col2, col3;

	private static final double SHADOW_EPSILON = 0.006; // Shadows
	private static final double W_EPSILON = 0.001; // Transparent meshes (-1 to disable)

	public ProjectedTriangle(Camera camera, double[] v1, double[] v2, double[] v3, Image image, Point2D tex1, Point2D tex2, Point2D tex3){
		this.imageTriangle = true;
		this.camera = camera;
		this.v1 = v1;
		this.v2 = v2;
		this.v3 = v3;
		this.image = image;
		this.tex1 = tex1.multiply(v1[2]);
		this.tex2 = tex2.multiply(v2[2]);
		this.tex3 = tex3.multiply(v3[2]);
	}

	public ProjectedTriangle(Camera camera, double[] v1, double[] v2, double[] v3, Color col1, Color col2, Color col3){
		this.imageTriangle = false;
		this.camera = camera;
		this.v1 = v1;
		this.v2 = v2;
		this.v3 = v3;
		this.col1 = col1;
		this.col2 = col2;
		this.col3 = col3;
	}

	public void setLightIntensity(double value){
		this.lightIntensity = value;
	}

	public void setTransparent(boolean value){
		this.transparent = value;
	}

	public boolean isTransparent(){
		return this.transparent;
	}

	public double getMeanDepth(){
		return (this.v1[2]+this.v2[2]+this.v3[2])/3;
	}

	public void setLightData(List<Light> lights, MeshVertex vex1, MeshVertex vex2, MeshVertex vex3){
		if (lights != null){
			this.lights = lights;
			this.vex1 = vex1;
			this.vex2 = vex2;
			this.vex3 = vex3;
		}
	}

	public void render(ColorCanvas canvas, GraphicsContext gc){
		if (canvas == null && gc == null){
			calculateDepthBuffer();
		} else {
			if (gc == null){
				if (this.imageTriangle){
					renderTriangle(canvas);
				} else {
					renderColoredTriangle(canvas);
				}
			} else {
				gc.setStroke(Color.RED);
				gc.setLineWidth(1.5);
				gc.strokeLine(this.v1[0], this.v1[1], this.v2[0], this.v2[1]);
				gc.strokeLine(this.v2[0], this.v2[1], this.v3[0], this.v3[1]);
				gc.strokeLine(this.v3[0], this.v3[1], this.v1[0], this.v1[1]);
			}
		}
	}

	private void calculateDepthBuffer(){
		int x1 = (int)this.v1[0];
		int y1 = (int)this.v1[1];
		double w1 = this.v1[2];
		int x2 = (int)this.v2[0];
		int y2 = (int)this.v2[1];
		double w2 = this.v2[2];
		int x3 = (int)this.v3[0];
		int y3 = (int)this.v3[1];
		double w3 = this.v3[2];

		if (y2 < y1){
			y1 = Engine3D.swap(y2, y2 = y1);
			x1 = Engine3D.swap(x2, x2 = x1);
			w1 = Engine3D.swap(w2, w2 = w1);
		}
		if (y3 < y1){
			y1 = Engine3D.swap(y3, y3 = y1);
			x1 = Engine3D.swap(x3, x3 = x1);
			w1 = Engine3D.swap(w3, w3 = w1);
		}
		if (y3 < y2){
			y2 = Engine3D.swap(y3, y3 = y2);
			x2 = Engine3D.swap(x3, x3 = x2);
			w2 = Engine3D.swap(w3, w3 = w2);
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
					ax = Engine3D.swap(bx, bx = ax);
					col_sw = Engine3D.swap(col_ew, col_ew = col_sw);
				}
				
				double tstep = 1.0/(bx-ax);
				double t = 0.0;
				
				for (int j = ax; j < bx; j++){
					col_w = (1-t)*col_sw+t*col_ew;
					
					if (Engine3D.isInScene(j, i, this.camera) && this.camera.depthBuffer[j][i] <= col_w){
						this.camera.depthBuffer[j][i] = col_w;
					}

					t += tstep;
				}
			}
		}
		
		dx1 = x3-x2;
		dy1 = y3-y2;
		dw1 = w3-w2;
		
		if (dy1 != 0) dax_step = dx1/(double)Math.abs(dy1);
		
		dw1_step = 0;
		if (dy1 != 0) dw1_step = dw1/Math.abs(dy1);
		
		if (dy1 != 0){
			for (int i = y2; i <= y3; i++){
				int ax = x2+(int)((i-y2)*dax_step);
				int bx = x1+(int)((i-y1)*dbx_step);
				
				double col_sw = w2+(i-y2)*dw1_step;
				double col_ew = w1+(i-y1)*dw2_step;
				
				if (ax > bx){
					ax = Engine3D.swap(bx, bx = ax);
					col_sw = Engine3D.swap(col_ew, col_ew = col_sw);
				}
				
				double tstep = 1.0/(bx-ax);
				double t = 0.0;
				
				for (int j = ax; j < bx; j++){
					col_w = (1-t)*col_sw+t*col_ew;
					
					if (Engine3D.isInScene(j, i, this.camera) && this.camera.depthBuffer[j][i] <= col_w){
						this.camera.depthBuffer[j][i] = col_w;
					}

					t += tstep;
				}
			}
		}
	}

	private void renderTriangle(ColorCanvas canvas){
		int x1 = (int)this.v1[0];
		int y1 = (int)this.v1[1];
		double w1 = this.v1[2];
		int x2 = (int)this.v2[0];
		int y2 = (int)this.v2[1];
		double w2 = this.v2[2];
		int x3 = (int)this.v3[0];
		int y3 = (int)this.v3[1];
		double w3 = this.v3[2];

		Image image = this.image;
		// Texture coordinates are multiplied by w in the constructor
		Point2D t1 = this.tex1;
		Point2D t2 = this.tex2;
		Point2D t3 = this.tex3;
		double u1 = t1.getX();
		double v1 = t1.getY();
		double u2 = t2.getX();
		double v2 = t2.getY();
		double u3 = t3.getX();
		double v3 = t3.getY();

		double l1 = 0, l2 = 0, l3 = 0;
		for (Light light : this.lights){
			l1 += light.getLightIntensity(this.vex1.getNormal(), this.vex1.getPosition());
			l2 += light.getLightIntensity(this.vex2.getNormal(), this.vex2.getPosition());
			l3 += light.getLightIntensity(this.vex3.getNormal(), this.vex3.getPosition());
		}
		l1 = Math.min(1, l1);
		l2 = Math.min(1, l2);
		l3 = Math.min(1, l3);

		double width = image.getWidth();
		double height = image.getHeight();
		PixelReader reader = image.getPixelReader();

		if (y2 < y1){
			y1 = Engine3D.swap(y2, y2 = y1);
			x1 = Engine3D.swap(x2, x2 = x1);
			u1 = Engine3D.swap(u2, u2 = u1);
			v1 = Engine3D.swap(v2, v2 = v1);
			w1 = Engine3D.swap(w2, w2 = w1);
			l1 = Engine3D.swap(l2, l2 = l1);
		}
		if (y3 < y1){
			y1 = Engine3D.swap(y3, y3 = y1);
			x1 = Engine3D.swap(x3, x3 = x1);
			u1 = Engine3D.swap(u3, u3 = u1);
			v1 = Engine3D.swap(v3, v3 = v1);
			w1 = Engine3D.swap(w3, w3 = w1);
			l1 = Engine3D.swap(l3, l3 = l1);
		}
		if (y3 < y2){
			y2 = Engine3D.swap(y3, y3 = y2);
			x2 = Engine3D.swap(x3, x3 = x2);
			u2 = Engine3D.swap(u3, u3 = u2);
			v2 = Engine3D.swap(v3, v3 = v2);
			w2 = Engine3D.swap(w3, w3 = w2);
			l2 = Engine3D.swap(l3, l3 = l2);
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
		
		if (dy1 != 0){
			du1_step = du1/Math.abs(dy1);
			dv1_step = dv1/Math.abs(dy1);
			dw1_step = dw1/Math.abs(dy1);
			dl1_step = dl1/Math.abs(dy1);
		}
		if (dy2 != 0){
			du2_step = du2/Math.abs(dy2);
			dv2_step = dv2/Math.abs(dy2);
			dw2_step = dw2/Math.abs(dy2);
			dl2_step = dl2/Math.abs(dy2);
		}

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
					ax = Engine3D.swap(bx, bx = ax);
					tex_su = Engine3D.swap(tex_eu, tex_eu = tex_su);
					tex_sv = Engine3D.swap(tex_ev, tex_ev = tex_sv);
					tex_sw = Engine3D.swap(tex_ew, tex_ew = tex_sw);
					tex_sl = Engine3D.swap(tex_el, tex_el = tex_sl);
				}
				
				double tstep = 1.0/(bx-ax);
				double t = 0.0;
				
				for (int j = ax; j <= bx; j++){
					tex_u = (1-t)*tex_su+t*tex_eu;
					tex_v = (1-t)*tex_sv+t*tex_ev;
					tex_w = (1-t)*tex_sw+t*tex_ew;
					tex_l = (1-t)*tex_sl+t*tex_el;
					
					int pix_x = (int)Math.max(0, tex_u/tex_w*width);
					int pix_y = (int)Math.max(0, tex_v/tex_w*height);

					if (Engine3D.isInScene(j, i, this.camera) && this.camera.depthBuffer[j][i] <= tex_w){
						Color color = reader.getColor(Math.min((int)width-1, pix_x), Math.min((int)height-1, pix_y));
						Color backColor = canvas.getColor(j, i);

						// Transparency
						if (color.getOpacity() == 0 || (backColor.getOpacity() < 1 && color.getOpacity() < 1 && Math.abs(this.camera.depthBuffer[j][i]-tex_w) < W_EPSILON)){
							t += tstep;
							continue;
						}
						color = Engine3D.mixColors(color, backColor);
						
						// Light
						color = Light.getLight(color, tex_l);

						// Shadows
						if (Engine3D.SHADOWS){
							for (Light light : this.lights){
								Camera cam2 = light.getCamera();
								double[] shadow = Engine3D.convertPoint(new double[]{j, i, tex_w}, this.camera, cam2);
								int index_x = (int)shadow[0];
								int index_y = (int)shadow[1];
								if (index_x >= 0 && index_y >= 0 && index_x < cam2.getWidth() && index_y < cam2.getHeight()){
									double depth = cam2.depthBuffer[index_x][index_y];
									if (Math.abs(shadow[2]-depth) > SHADOW_EPSILON*this.camera.getAspectRatio()){
										color = color.darker();
									}
								}
							}
						}

						this.camera.depthBuffer[j][i] = tex_w;
						canvas.setColor(Light.getLight(color, this.lightIntensity), j, i);
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
		
		du1_step = 0; dv1_step = 0; dw1_step = 0; dl1_step = 0;
		if (dy1 != 0){
			du1_step = du1/Math.abs(dy1);
			dv1_step = dv1/Math.abs(dy1);
			dw1_step = dw1/Math.abs(dy1);
			dl1_step = dl1/Math.abs(dy1);
		}

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
					ax = Engine3D.swap(bx, bx = ax);
					tex_su = Engine3D.swap(tex_eu, tex_eu = tex_su);
					tex_sv = Engine3D.swap(tex_ev, tex_ev = tex_sv);
					tex_sw = Engine3D.swap(tex_ew, tex_ew = tex_sw);
					tex_sl = Engine3D.swap(tex_el, tex_el = tex_sl);
				}
				
				double tstep = 1.0/(bx-ax);
				double t = 0.0;
				
				for (int j = ax; j <= bx; j++){
					tex_u = (1-t)*tex_su+t*tex_eu;
					tex_v = (1-t)*tex_sv+t*tex_ev;
					tex_w = (1-t)*tex_sw+t*tex_ew;
					tex_l = (1-t)*tex_sl+t*tex_el;

					int pix_x = (int)Math.max(0, tex_u/tex_w*width);
					int pix_y = (int)Math.max(0, tex_v/tex_w*height);

					if (Engine3D.isInScene(j, i, this.camera) && this.camera.depthBuffer[j][i] <= tex_w){
						Color color = reader.getColor(Math.min((int)width-1, pix_x), Math.min((int)height-1, pix_y));
						Color backColor = canvas.getColor(j, i);

						// Transparency
						if (color.getOpacity() == 0 || (backColor.getOpacity() < 1 && color.getOpacity() < 1 && Math.abs(this.camera.depthBuffer[j][i]-tex_w) < W_EPSILON)){
							t += tstep;
							continue;
						}
						color = Engine3D.mixColors(color, backColor);
						
						// Light
						color = Light.getLight(color, tex_l);

						// Shadows
						if (Engine3D.SHADOWS){
							for (Light light : this.lights){
								Camera cam2 = light.getCamera();
								double[] shadow = Engine3D.convertPoint(new double[]{j, i, tex_w}, this.camera, cam2);
								int index_x = (int)shadow[0];
								int index_y = (int)shadow[1];
								if (index_x >= 0 && index_y >= 0 && index_x < cam2.getWidth() && index_y < cam2.getHeight()){
									double depth = cam2.depthBuffer[index_x][index_y];
									if (Math.abs(shadow[2]-depth) > SHADOW_EPSILON*this.camera.getAspectRatio()){
										color = color.darker();
									}
								}
							}
						}

						this.camera.depthBuffer[j][i] = tex_w;
						canvas.setColor(Light.getLight(color, this.lightIntensity), j, i);
					}
					
					t += tstep;
				}
			}
		}
	}

	private void renderColoredTriangle(ColorCanvas canvas){
		int x1 = (int)this.v1[0];
		int y1 = (int)this.v1[1];
		double w1 = this.v1[2];
		int x2 = (int)this.v2[0];
		int y2 = (int)this.v2[1];
		double w2 = this.v2[2];
		int x3 = (int)this.v3[0];
		int y3 = (int)this.v3[1];
		double w3 = this.v3[2];

		Color c1 = this.col1;
		Color c2 = this.col2;
		Color c3 = this.col3;

		// Setup the lights
		double l1 = 0, l2 = 0, l3 = 0;
		for (Light light : this.lights){
			l1 += light.getLightIntensity(this.vex1.getNormal(), this.vex1.getPosition());
			l2 += light.getLightIntensity(this.vex2.getNormal(), this.vex2.getPosition());
			l3 += light.getLightIntensity(this.vex3.getNormal(), this.vex3.getPosition());
		}
		l1 = Math.min(1, l1);
		l2 = Math.min(1, l2);
		l3 = Math.min(1, l3);

		if (y2 < y1){
			y1 = Engine3D.swap(y2, y2 = y1);
			x1 = Engine3D.swap(x2, x2 = x1);
			c1 = Engine3D.swap(c2, c2 = c1);
			w1 = Engine3D.swap(w2, w2 = w1);
			l1 = Engine3D.swap(l2, l2 = l1);
		}
		if (y3 < y1){
			y1 = Engine3D.swap(y3, y3 = y1);
			x1 = Engine3D.swap(x3, x3 = x1);
			c1 = Engine3D.swap(c3, c3 = c1);
			w1 = Engine3D.swap(w3, w3 = w1);
			l1 = Engine3D.swap(l3, l3 = l1);
		}
		if (y3 < y2){
			y2 = Engine3D.swap(y3, y3 = y2);
			x2 = Engine3D.swap(x3, x3 = x2);
			c2 = Engine3D.swap(c3, c3 = c2);
			w2 = Engine3D.swap(w3, w3 = w2);
			l2 = Engine3D.swap(l3, l3 = l2);
		}

		// Calculate the values of the first line
		int dx1 = x2-x1;
		int dy1 = y2-y1;
		double dr1 = c2.getRed()-c1.getRed();
		double dg1 = c2.getGreen()-c1.getGreen();
		double db1 = c2.getBlue()-c1.getBlue();
		double da1 = c2.getOpacity()-c1.getOpacity();
		double dw1 = w2-w1;
		double dl1 = l2-l1;
		
		// Calculate the values of the second line
		int dx2 = x3-x1;
		int dy2 = y3-y1;
		double dr2 = c3.getRed()-c1.getRed();
		double dg2 = c3.getGreen()-c1.getGreen();
		double db2 = c3.getBlue()-c1.getBlue();
		double da2 = c3.getOpacity()-c1.getOpacity();
		double dw2 = w3-w1;
		double dl2 = l3-l1;

		double col_r, col_g, col_b, col_o, col_w, col_l;
		double dax_step = 0, dbx_step = 0, dr1_step = 0, dg1_step = 0, db1_step = 0, da1_step = 0, dr2_step = 0, dg2_step = 0, db2_step = 0, da2_step = 0, dw1_step = 0, dw2_step = 0, dl1_step = 0, dl2_step = 0;

		if (dy1 != 0) dax_step = dx1/(double)Math.abs(dy1);
		if (dy2 != 0) dbx_step = dx2/(double)Math.abs(dy2);
		
		if (dy1 != 0){
			dr1_step = dr1/Math.abs(dy1);
			dg1_step = dg1/Math.abs(dy1);
			db1_step = db1/Math.abs(dy1);
			da1_step = da1/Math.abs(dy1);
			dw1_step = dw1/Math.abs(dy1);
			dl1_step = dl1/Math.abs(dy1);
		}
		if (dy2 != 0){
			dr2_step = dr2/Math.abs(dy2);
			dg2_step = dg2/Math.abs(dy2);
			db2_step = db2/Math.abs(dy2);
			da2_step = da2/Math.abs(dy2);
			dw2_step = dw2/Math.abs(dy2);
			dl2_step = dl2/Math.abs(dy2);
		}

		if (dy1 != 0){
			for (int i = y1; i <= y2; i++){
				int ax = x1+(int)((i-y1)*dax_step);
				int bx = x1+(int)((i-y1)*dbx_step);
				
				double col_sr = c1.getRed()+(i-y1)*dr1_step;
				double col_sg = c1.getGreen()+(i-y1)*dg1_step;
				double col_sb = c1.getBlue()+(i-y1)*db1_step;
				double col_so = c1.getOpacity()+(i-y1)*da1_step;
				double col_sw = w1+(i-y1)*dw1_step;
				double col_sl = l1+(i-y1)*dl1_step;
				
				double col_er = c1.getRed()+(i-y1)*dr2_step;
				double col_eg = c1.getGreen()+(i-y1)*dg2_step;
				double col_eb = c1.getBlue()+(i-y1)*db2_step;
				double col_eo = c1.getOpacity()+(i-y1)*da2_step;
				double col_ew = w1+(i-y1)*dw2_step;
				double col_el = l1+(i-y1)*dl2_step;
				
				if (ax > bx){
					ax = Engine3D.swap(bx, bx = ax);
					col_sr = Engine3D.swap(col_er, col_er = col_sr);
					col_sg = Engine3D.swap(col_eg, col_eg = col_sg);
					col_sb = Engine3D.swap(col_eb, col_eb = col_sb);
					col_so = Engine3D.swap(col_eo, col_eo = col_so);
					col_sw = Engine3D.swap(col_ew, col_ew = col_sw);
					col_sl = Engine3D.swap(col_el, col_el = col_sl);
				}
				
				double tstep = 1.0/(bx-ax);
				double t = 0.0;
				
				for (int j = ax; j <= bx; j++){
					col_r = (1-t)*col_sr+t*col_er;
					col_g = (1-t)*col_sg+t*col_eg;
					col_b = (1-t)*col_sb+t*col_eb;
					col_o = (1-t)*col_so+t*col_eo;
					col_w = (1-t)*col_sw+t*col_ew;
					col_l = (1-t)*col_sl+t*col_el;
					if (Engine3D.isInScene(j, i, this.camera) && this.camera.depthBuffer[j][i] <= col_w){
						Color color = Color.color(col_r, col_g, col_b, col_o);
						Color backColor = canvas.getColor(j, i);

						// Transparency
						if (color.getOpacity() == 0 || (backColor.getOpacity() < 1 && color.getOpacity() < 1 && Math.abs(this.camera.depthBuffer[j][i]-col_w) < W_EPSILON)){
							t += tstep;
							continue;
						}
						color = Engine3D.mixColors(color, backColor);

						// Light
						color = Light.getLight(color, col_l);

						// Shadows
						if (Engine3D.SHADOWS){
							for (Light light : this.lights){
								Camera cam2 = light.getCamera();
								double[] shadow = Engine3D.convertPoint(new double[]{j, i, col_w}, this.camera, cam2);
								int index_x = (int)shadow[0];
								int index_y = (int)shadow[1];
								if (index_x >= 0 && index_y >= 0 && index_x < cam2.getWidth() && index_y < cam2.getHeight()){
									double depth = cam2.depthBuffer[index_x][index_y];
									if (Math.abs(shadow[2]-depth) > SHADOW_EPSILON*this.camera.getAspectRatio()){
										color = color.darker();
									}
								}
							}
						}
						
						this.camera.depthBuffer[j][i] = col_w;
						canvas.setColor(Light.getLight(color, this.lightIntensity), j, i);
					}

					t += tstep;
				}
			}
		}

		// Calculate the values of the 'new' first line
		dx1 = x3-x2;
		dy1 = y3-y2;
		dr1 = c3.getRed()-c2.getRed();
		dg1 = c3.getGreen()-c2.getGreen();
		db1 = c3.getBlue()-c2.getBlue();
		da1 = c3.getOpacity()-c2.getOpacity();
		dw1 = w3-w2;
		dl1 = l3-l2;

		if (dy1 != 0) dax_step = dx1/(double)Math.abs(dy1);

		dr1_step = 0; dg1_step = 0; db1_step = 0; dw1_step = 0; dl1_step = 0;
		if (dy1 != 0){
			dr1_step = dr1/Math.abs(dy1);
			dg1_step = dg1/Math.abs(dy1);
			db1_step = db1/Math.abs(dy1);
			da1_step = da1/Math.abs(dy1);
			dw1_step = dw1/Math.abs(dy1);
			dl1_step = dl1/Math.abs(dy1);
		}

		if (dy1 != 0){
			for (int i = y2; i <= y3; i++){
				int ax = x2+(int)((i-y2)*dax_step);
				int bx = x1+(int)((i-y1)*dbx_step);
				
				double col_sr = c2.getRed()+(i-y2)*dr1_step;
				double col_sg = c2.getGreen()+(i-y2)*dg1_step;
				double col_sb = c2.getBlue()+(i-y2)*db1_step;
				double col_so = c2.getOpacity()+(i-y2)*da1_step;
				double col_sw = w2+(i-y2)*dw1_step;
				double col_sl = l2+(i-y2)*dl1_step;
				
				double col_er = c1.getRed()+(i-y1)*dr2_step;
				double col_eg = c1.getGreen()+(i-y1)*dg2_step;
				double col_eb = c1.getBlue()+(i-y1)*db2_step;
				double col_eo = c1.getOpacity()+(i-y1)*da2_step;
				double col_ew = w1+(i-y1)*dw2_step;
				double col_el = l1+(i-y1)*dl2_step;
				
				if (ax > bx){
					ax = Engine3D.swap(bx, bx = ax);
					col_sr = Engine3D.swap(col_er, col_er = col_sr);
					col_sg = Engine3D.swap(col_eg, col_eg = col_sg);
					col_sb = Engine3D.swap(col_eb, col_eb = col_sb);
					col_so = Engine3D.swap(col_eo, col_eo = col_so);
					col_sw = Engine3D.swap(col_ew, col_ew = col_sw);
					col_sl = Engine3D.swap(col_el, col_el = col_sl);
				}
				
				double tstep = 1.0/(bx-ax);
				double t = 0.0;
				
				for (int j = ax; j <= bx; j++){
					col_r = (1-t)*col_sr+t*col_er;
					col_g = (1-t)*col_sg+t*col_eg;
					col_b = (1-t)*col_sb+t*col_eb;
					col_o = (1-t)*col_so+t*col_eo;
					col_w = (1-t)*col_sw+t*col_ew;
					col_l = (1-t)*col_sl+t*col_el;
					
					if (Engine3D.isInScene(j, i, this.camera) && this.camera.depthBuffer[j][i] <= col_w){
						Color color = Color.color(col_r, col_g, col_b, col_o);
						Color backColor = canvas.getColor(j, i);

						// Transparency
						if (color.getOpacity() == 0 || (backColor.getOpacity() < 1 && color.getOpacity() < 1 && Math.abs(this.camera.depthBuffer[j][i]-col_w) < W_EPSILON)){
							t += tstep;
							continue;
						}
						color = Engine3D.mixColors(color, backColor);

						// Light
						color = Light.getLight(color, col_l);

						// Shadows
						if (Engine3D.SHADOWS){
							for (Light light : this.lights){
								Camera cam2 = light.getCamera();
								double[] shadow = Engine3D.convertPoint(new double[]{j, i, col_w}, this.camera, cam2);
								int index_x = (int)shadow[0];
								int index_y = (int)shadow[1];
								if (index_x >= 0 && index_y >= 0 && index_x < cam2.getWidth() && index_y < cam2.getHeight()){
									double depth = cam2.depthBuffer[index_x][index_y];
									if (Math.abs(shadow[2]-depth) > SHADOW_EPSILON*this.camera.getAspectRatio()){
										color = color.darker();
									}
								}
							}
						}
						
						this.camera.depthBuffer[j][i] = col_w;
						canvas.setColor(Light.getLight(color, this.lightIntensity), j, i);
					}

					t += tstep;
				}
			}
		}
	}
}