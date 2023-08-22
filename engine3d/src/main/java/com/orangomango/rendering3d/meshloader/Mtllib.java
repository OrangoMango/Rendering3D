package com.orangomango.rendering3d.meshloader;

import javafx.scene.paint.Color;
import javafx.scene.image.Image;

import java.io.*;
import java.util.*;

public class Mtllib{
	private File file;
	private Image[] images;
	private List<Material> materials = new ArrayList<>();

	public Mtllib(File file){
		this.file = file;
	}

	public boolean load(){
		try {
			if (!this.file.exists()){
				return false;
			}

			Material currentMaterial = null;

			BufferedReader reader = new BufferedReader(new FileReader(this.file));
			String line;
			while ((line = reader.readLine()) != null){
				if (line.toLowerCase().startsWith("newmtl")){
					if (currentMaterial != null){
						this.materials.add(currentMaterial);
					}
					currentMaterial = new Material(line.split(" ")[1], null, null);
				} else if (line.toLowerCase().startsWith("kd") && currentMaterial != null){
					double red = Double.parseDouble(line.split(" ")[1]);
					double green = Double.parseDouble(line.split(" ")[2]);
					double blue = Double.parseDouble(line.split(" ")[3]);
					currentMaterial.color = Color.color(red, green, blue);
				} else if (line.toLowerCase().startsWith("d") && currentMaterial != null){
					double alpha = Double.parseDouble(line.split(" ")[1]);
					currentMaterial.color = Color.color(currentMaterial.color.getRed(), currentMaterial.color.getGreen(), currentMaterial.color.getBlue(), alpha);
				} else if (line.toLowerCase().startsWith("map_kd")){
					File location = new File(this.file.getParent(), line.split(" ")[1]);
					currentMaterial.image = new Image(location.toURI().toURL().toString());
				}
			}
			if (currentMaterial != null){
				this.materials.add(currentMaterial);
			}

			reader.close();

			List<Image> images = new ArrayList<>();
			for (Material m : this.materials){
				if (m.image != null){
					images.add(m.image);
				}
			}
			this.images = images.toArray(new Image[images.size()]);

			return true;
		} catch (IOException ex){
			ex.printStackTrace();
			return false;
		}
	}

	public Color getColor(String materialName){
		for (Material material : this.materials){
			if (material.name.equals(materialName)){
				return material.color;
			}
		}
		return null;
	}

	public Image[] getImages(){
		return this.images;
	}

	public int getImageIndex(String materialName){
		int index = 0;
		for (Material material : this.materials){
			if (material.name.equals(materialName)){
				return index;
			}

			if (material.image != null){
				index++;
			}
		}
		return -1;
	}
}