package com.orangomango.blockworld.model;

import javafx.scene.image.Image;

import java.io.*;
import java.util.*;
import org.json.*;

public class Atlas{
	private JSONObject json;
	private Map<String, Image> images = new HashMap<>();

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
			String fileName = this.json.getJSONObject("blocks").getJSONObject(blockType).getString("fileName");
			this.images.put(blockType, new Image(Atlas.class.getResourceAsStream(fileName)));
		}
	}

	public Map<String, Image> getImages(){
		return this.images;
	}

	public JSONObject getJSON(){
		return this.json;
	}
}
