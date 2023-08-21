package com.orangomango.rendering3d.model;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import static com.orangomango.rendering3d.Engine3D.swap;
import static com.orangomango.rendering3d.Engine3D.isInScene;

public class ProjectedTriangle{
	private double[] v1, v2, v3;
	private boolean imageTriangle;
	private Camera camera;

	// Image triangle
	private Point2D tex1, tex2, tex3;
	private Image image;

	// Coloed triangle
	private Color col1, col2, col3;

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

	public void render(Color[][] canvas){
		if (this.imageTriangle){

		} else {
			renderColoredTriangle(canvas);
		}
	}

	private void renderColoredTriangle(Color[][] canvas){
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

		double col_r, col_g, col_b, col_a, col_w, col_l;
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
				double col_sa = c1.getOpacity()+(i-y1)*da1_step;
				double col_sw = w1+(i-y1)*dw1_step;
				double col_sl = l1+(i-y1)*dl1_step;
				
				double col_er = c1.getRed()+(i-y1)*dr2_step;
				double col_eg = c1.getGreen()+(i-y1)*dg2_step;
				double col_eb = c1.getBlue()+(i-y1)*db2_step;
				double col_ea = c1.getOpacity()+(i-y1)*da2_step;
				double col_ew = w1+(i-y1)*dw2_step;
				double col_el = l1+(i-y1)*dl2_step;
				
				if (ax > bx){
					ax = swap(bx, bx = ax);
					col_sr = swap(col_er, col_er = col_sr);
					col_sg = swap(col_eg, col_eg = col_sg);
					col_sb = swap(col_eb, col_eb = col_sb);
					col_sa = swap(col_ea, col_ea = col_sa);
					col_sw = swap(col_ew, col_ew = col_sw);
					col_sl = swap(col_el, col_el = col_sl);
				}
				
				double tstep = 1.0/(bx-ax);
				double t = 0.0;
				
				for (int j = ax; j <= bx; j++){
					col_r = (1-t)*col_sr+t*col_er;
					col_g = (1-t)*col_sg+t*col_eg;
					col_b = (1-t)*col_sb+t*col_eb;
					col_a = (1-t)*col_sa+t*col_ea;
					col_w = (1-t)*col_sw+t*col_ew;
					col_l = (1-t)*col_sl+t*col_el;
					if (isInScene(j, i, this.camera) && this.camera.depthBuffer[j][i] <= col_w){
						Color color = Color.color(col_r, col_g, col_b);
						
						this.camera.depthBuffer[j][i] = col_w;
						canvas[j][i] = color;
						System.out.println("Rendering1");
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
		if (dy2 != 0) dbx_step = dx2/(double)Math.abs(dy2);

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
				double col_sa = c2.getOpacity()+(i-y2)*da1_step;
				double col_sw = w2+(i-y2)*dw1_step;
				double col_sl = l2+(i-y2)*dl1_step;
				
				double col_er = c1.getRed()+(i-y1)*dr2_step;
				double col_eg = c1.getGreen()+(i-y1)*dg2_step;
				double col_eb = c1.getBlue()+(i-y1)*db2_step;
				double col_ea = c1.getOpacity()+(i-y1)*da2_step;
				double col_ew = w1+(i-y1)*dw2_step;
				double col_el = l1+(i-y1)*dl2_step;
				
				if (ax > bx){
					ax = swap(bx, bx = ax);
					col_sr = swap(col_er, col_er = col_sr);
					col_sg = swap(col_eg, col_eg = col_sg);
					col_sb = swap(col_eb, col_eb = col_sb);
					col_sa = swap(col_ea, col_ea = col_sa);
					col_sw = swap(col_ew, col_ew = col_sw);
					col_sl = swap(col_el, col_el = col_sl);
				}
				
				double tstep = 1.0/(bx-ax);
				double t = 0.0;
				
				for (int j = ax; j <= bx; j++){
					col_r = (1-t)*col_sr+t*col_er;
					col_g = (1-t)*col_sg+t*col_eg;
					col_b = (1-t)*col_sb+t*col_eb;
					col_a = (1-t)*col_sa+t*col_ea;
					col_w = (1-t)*col_sw+t*col_ew;
					col_l = (1-t)*col_sl+t*col_el;
					
					if (isInScene(j, i, this.camera) && this.camera.depthBuffer[j][i] <= col_w){
						Color color = Color.color(col_r, col_g, col_b);
						
						this.camera.depthBuffer[j][i] = col_w;
						canvas[j][i] = color;
						System.out.println("Rendering2");
					}

					t += tstep;
				}
			}
		}
	}
}