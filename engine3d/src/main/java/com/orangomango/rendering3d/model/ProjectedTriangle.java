package com.orangomango.rendering3d.model;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class ProjectedTriangle{
	private double[] v1, v2, v3;
	private boolean imageTriangle;

	// Image triangle
	private Point2D tex1, tex2, tex3;
	private Image image;

	// Coloed triangle
	private Color col1, col2, col3;

	public ProjectedTriangle(double[] v1, double[] v2, double[] v3, Image image, Point2D tex1, Point2D tex2, Point2D tex3){
		this.imageTriangle = true;
		this.v1 = v1;
		this.v2 = v2;
		this.v3 = v3;
		this.image = image;
		this.tex1 = tex1.multiply(v1[2]);
		this.tex2 = tex2.multiply(v2[2]);
		this.tex3 = tex3.multiply(v3[2]);
	}

	public ProjectedTriangle(double[] v1, double[] v2, double[] v3, Color col1, Color col2, Color col3){
		this.imageTriangle = false;
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

		}
	}
}