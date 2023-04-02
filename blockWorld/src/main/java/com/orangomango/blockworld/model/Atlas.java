package com.orangomango.blockworld.model;

import javafx.scene.image.Image;

import java.io.*;
import java.util.*;
import org.json.*;

public class Atlas{
	private JSONObject json;
	private Map<String, Image[]> images = new HashMap<>();
	private Map<String, int[]> imageFaces = new HashMap<>();
	private Map<Integer, String> blockIds = new HashMap<>();

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
			String[] directions = new String[]{"front", "right", "back", "left", "down", "top"};
			int i = 0;
			for (String dir : directions){
				imageF[i] = this.json.getJSONObject("blocks").getJSONObject(blockType).getJSONObject("config").getInt(dir);
				imageF[i+1] = imageF[i];
				i += 2;
			}

			this.imageFaces.put(blockType, imageF);
		}
		
		for (Object block : this.json.getJSONObject("blocks").keySet()){
			String blockType = (String)block;
			blockIds.put(getBlockId(blockType), blockType);
		}
	}
	
	public int getBlockId(String blockType){
		return this.json.getJSONObject("blocks").getJSONObject(blockType).getInt("id");
	}
	
	public String getBlockType(int id){
		return this.blockIds.get(id);
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
