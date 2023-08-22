package com.orangomango.rendering3d.meshloader;

import javafx.scene.paint.Color;
import javafx.scene.image.Image;

public class Material{
	public String name;
	public Color color;
	public Image image;

	public Material(String name, Color color, Image image){
		this.name = name;
		this.color = color;
		this.image = image;
	}
}