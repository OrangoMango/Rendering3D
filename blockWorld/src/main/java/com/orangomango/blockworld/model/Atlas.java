package com.orangomango.blockworld.model;

import javafx.scene.image.Image;
import javafx.geometry.Point3D;

import java.io.*;
import java.util.*;
import org.json.*;

public class Atlas{
	private JSONObject json;
	private Map<String, Image[]> images = new HashMap<>();
	private Map<String, int[]> imageFaces = new HashMap<>();
	private Map<Integer, String> blockIds = new HashMap<>();

	// Mesh
	private Map<String, Point3D[]> vertices = new HashMap<>();
	private Map<String, int[][]> faces = new HashMap<>();

	public static Atlas MAIN_ATLAS;

	static {
		MAIN_ATLAS = new Atlas("/atlas.json");
	}

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
			buildMesh(blockType, this.json.getJSONObject("blocks").getJSONObject(blockType).getString("mesh"));
		}
		
		for (Object block : this.json.getJSONObject("blocks").keySet()){
			String blockType = (String)block;
			blockIds.put(getBlockId(blockType), blockType);
		}
	}

	private void buildMesh(String type, String fileName){
		try {
			File file = new File(Atlas.class.getResource(fileName).toURI());
			BufferedReader reader = new BufferedReader(new FileReader(file));
			boolean vertices = true;
			List<Point3D> vx = new ArrayList<>();
			List<int[]> fc = new ArrayList<>();
			String line;
			while ((line = reader.readLine()) != null){
				if (!line.equals("")){
					if (line.startsWith("vertices:")){
						vertices = true;
					} else if (line.startsWith("faces:")){
						vertices = false;
					} else {
						if (vertices){
							double x = Double.parseDouble(line.split(" ")[0]);
							double y = Double.parseDouble(line.split(" ")[1]);
							double z = Double.parseDouble(line.split(" ")[2]);
							vx.add(new Point3D(x, y, z));
						} else {
							int i = Integer.parseInt(line.split(" ")[0]);
							int j = Integer.parseInt(line.split(" ")[1]);
							int k = Integer.parseInt(line.split(" ")[2]);
							fc.add(new int[]{i, j, k});
						}
					}
				}
			}
			reader.close();

			this.vertices.put(type, vx.toArray(new Point3D[vx.size()]));
			this.faces.put(type, fc.toArray(new int[fc.size()][3]));
		} catch (Exception ex){
			ex.printStackTrace();
		}
	}
	
	public int getBlockId(String blockType){
		return this.json.getJSONObject("blocks").getJSONObject(blockType).getInt("id");
	}
	
	public boolean isTransparent(String blockType){
		return this.json.getJSONObject("blocks").getJSONObject(blockType).getBoolean("transparent");
	}
	
	public boolean isSprite(String blockType){
		return this.json.getJSONObject("blocks").getJSONObject(blockType).optBoolean("sprite");
	}
	
	public boolean isLiquid(String blockType){
		return this.json.getJSONObject("blocks").getJSONObject(blockType).optBoolean("liquid");
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

	public Map<String, Point3D[]> getVertices(){
		return this.vertices;
	}

	public Map<String, int[][]> getFaces(){
		return this.faces;
	}

	public JSONObject getJSON(){
		return this.json;
	}
}