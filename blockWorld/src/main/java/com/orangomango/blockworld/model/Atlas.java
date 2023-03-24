package com.orangomango.blockworld.model;

import javafx.scene.image.Image;

import java.io.*;
import java.util.*;
import org.json.*;

public class Atlas{
	private JSONObject json;
	private Map<String, Image[]> images = new HashMap<>();
	private Map<String, int[]> imageFaces = new HashMap<>();

	public Atlas(String name){
		try {
			File file = new File(Atlas.class.getResource(name).toURI());
			BufferedReader reader = new BufferedReader(new FileReader(file));
			StringBuilder builder = new StringBuilder();
			reader.lines().forEach(line -> builder.append(line).append("\n"));
			reader.close();
			this.json = new JSONObject(builder.toString());
		} catch (Exception ex){
			ex.printStackTrace();
		}

		for (String blockType : this.json.getJSONObject("blocks").keySet()){
			List<Image> imageObjects = new ArrayList<>();
			for (Object imageName : this.json.getJSONObject("blocks").getJSONObject(blockType).getJSONArray("images")){
				imageObjects.add(new Image(Atlas.class.getResourceAsStream((String)imageName)));
			}
			this.images.put(blockType, imageObjects.toArray(new Image[imageObjects.size()]));
			
			int[] imageF = new int[12];
			imageF[0] = this.json.getJSONObject("blocks").getJSONObject(blockType).getJSONObject("config").getInt("front");
			imageF[1] = this.json.getJSONObject("blocks").getJSONObject(blockType).getJSONObject("config").getInt("front");
			imageF[2] = this.json.getJSONObject("blocks").getJSONObject(blockType).getJSONObject("config").getInt("right");
			imageF[3] = this.json.getJSONObject("blocks").getJSONObject(blockType).getJSONObject("config").getInt("right");
			imageF[4] = this.json.getJSONObject("blocks").getJSONObject(blockType).getJSONObject("config").getInt("back");
			imageF[5] = this.json.getJSONObject("blocks").getJSONObject(blockType).getJSONObject("config").getInt("back");
			imageF[6] = this.json.getJSONObject("blocks").getJSONObject(blockType).getJSONObject("config").getInt("left");
			imageF[7] = this.json.getJSONObject("blocks").getJSONObject(blockType).getJSONObject("config").getInt("left");
			imageF[8] = this.json.getJSONObject("blocks").getJSONObject(blockType).getJSONObject("config").getInt("down");
			imageF[9] = this.json.getJSONObject("blocks").getJSONObject(blockType).getJSONObject("config").getInt("down");
			imageF[10] = this.json.getJSONObject("blocks").getJSONObject(blockType).getJSONObject("config").getInt("top");
			imageF[11] = this.json.getJSONObject("blocks").getJSONObject(blockType).getJSONObject("config").getInt("top");
			this.imageFaces.put(blockType, imageF);
		}
	}

	public Map<String, Image[]> getImages(){
		return this.images;
	}
	
	public Map<String, int[]> getBlockFaces(){
		return this.imageFaces;
	}

	public JSONObject getJSON(){
		return this.json;
	}
}
