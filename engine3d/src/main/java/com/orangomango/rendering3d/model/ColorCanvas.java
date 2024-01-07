package com.orangomango.rendering3d.model;

import javafx.scene.paint.Color;

public class ColorCanvas{
	private byte[][] red, green, blue, opacity;

	public ColorCanvas(int w, int h){
		this.red = new byte[w][h];
		this.green = new byte[w][h];
		this.blue = new byte[w][h];
		this.opacity = new byte[w][h];
	}

	public void setColor(Color color, int x, int y){
		double r = color.getRed();
		double g = color.getGreen();
		double b = color.getBlue();
		double o = color.getOpacity();
		this.red[x][y] = (byte)((r*255)+Byte.MIN_VALUE);
		this.green[x][y] = (byte)((g*255)+Byte.MIN_VALUE);
		this.blue[x][y] = (byte)((b*255)+Byte.MIN_VALUE);
		this.opacity[x][y] = (byte)((o*255)+Byte.MIN_VALUE);
	}

	public Color getColor(int x, int y){
		int red = this.red[x][y]-Byte.MIN_VALUE;
		int green = this.green[x][y]-Byte.MIN_VALUE;
		int blue = this.blue[x][y]-Byte.MIN_VALUE;
		double opacity = (this.opacity[x][y]-Byte.MIN_VALUE)/255.0;

		return Color.rgb(red, green, blue, opacity);
	}
}