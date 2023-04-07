package com.orangomango.blockworld;

import javafx.application.Application;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.geometry.Point3D;
import javafx.stage.Stage;

import java.util.*;

import com.orangomango.rendering3d.model.*;
import com.orangomango.rendering3d.Engine3D;
import com.orangomango.blockworld.model.*;

/*
 * RaspberryPi settings: CHUNKS: 5, RENDER_DISTANCE: 4, CHUNK_SIZE: 4
 * Normal settings: CHUNKS: 5, RENDER_DISTANCE: 4/5, CHUNK_SIZE: 8
 * 
 * @author OrangoMango (https://orangomango.github.io)
 */
public class MainApplication extends Application{
	private static final int WIDTH = 320;
	private static final int HEIGHT = 180;
	private static final int CHUNKS = 3;

	private static final String[] inventoryBlocks = new String[]{"wood", "coal", "grass", "stone", "wood_log", "dirt", "cobblestone", "sand"};
	private int currentBlock = 0;
	private boolean loadChunks = true;
	
	@Override
	public void start(Stage stage){
		stage.setTitle("BlockWorld");
		
		Engine3D engine = new Engine3D(stage, WIDTH, HEIGHT);

		Player player = new Player(0, 15, 0);
		
		engine.setCamera(player.getCamera());
		Light light = new Light(0, 0, 0); // -5, 3, 5
		light.setFixed(true);
		engine.getLights().add(light);
		//Engine3D.LIGHT_AVAILABLE = false;
		
		World world = new World((int)System.currentTimeMillis());
		ChunkManager chunkManager = new ChunkManager(world, player);
		/*final int worldChunks = 8;
		for (int i = 0; i < worldChunks; i++){
			for (int j = 0; j < worldChunks; j++){
				for (int k = -1; k < 2; k++){
					chunkManager.loadChunk(i, Chunk.HEIGHT_LIMIT+k, j);
				}
			}
		}
		for (Chunk chunk : world.getChunks().values()){
			chunk.setupFaces();
		}*/

		engine.setOnMousePressed(e -> {
			//world.removeBlockAt((int)player.getX(), (int)(player.getY()+1), (int)player.getZ());
			Block block = null;

			double stepX = Math.cos(player.getRx())*Math.cos(player.getRy()+Math.PI/2);
			double stepY = -Math.sin(player.getRx());
			double stepZ = Math.cos(player.getRx())*Math.sin(player.getRy()+Math.PI/2);

			int lastX = 0;
			int lastY = 0;
			int lastZ = 0;

			for (double i = 0; i <= 10; i += 0.01){
				int lX = (int)(player.getX()+i*stepX);
				int lY = (int)(player.getY()+i*stepY);
				int lZ = (int)(player.getZ()+i*stepZ);
				block = world.getBlockAt(lX, lY, lZ);
				if (block == null || i == 0){
					lastX = lX;
					lastY = lY;
					lastZ = lZ;
				}
				if (block != null) break;
			}
			if (block != null){
				boolean chunkUpdate = false;
				if (e.getButton() == MouseButton.PRIMARY){
					world.removeBlockAt(block.getX(), block.getY(), block.getZ());
					chunkUpdate = true;
				} else if (e.getButton() == MouseButton.SECONDARY && lastX >= 0 && lastY >= 0 && lastZ >= 0){
					world.setBlockAt(lastX, lastY, lastZ, inventoryBlocks[this.currentBlock]);
					chunkUpdate = true;
				}
				if (chunkUpdate){
					for (int i = -1; i < 2; i++){
						for (int j = -1; j < 2; j++){
							for (int k = -1; k < 2; k++){
								Chunk chunk = world.getChunkAt(block.getX()/Chunk.CHUNK_SIZE+i, block.getY()/Chunk.CHUNK_SIZE+k, block.getZ()/Chunk.CHUNK_SIZE+j);
								if (chunk != null){
									Chunk.updateMesh(chunk);
								}
							}
						}
					}
					int chunkX = block.getX() / Chunk.CHUNK_SIZE;
					int chunkY = block.getY() / Chunk.CHUNK_SIZE;
					int chunkZ = block.getZ() / Chunk.CHUNK_SIZE;
					chunkManager.saveChunkToFile(world.getChunkAt(chunkX, chunkY, chunkZ));
				}
			}
		});

		engine.setOnKey(KeyCode.O, engine::toggleMouseMovement, true);
		engine.setOnKey(KeyCode.I, () -> this.loadChunks = !this.loadChunks, true);
		engine.setOnKey(KeyCode.R, () -> player.reset(), true);
		engine.setOnKey(KeyCode.DIGIT1, () -> this.currentBlock = 0, true);
		engine.setOnKey(KeyCode.DIGIT2, () -> this.currentBlock = 1, true);
		engine.setOnKey(KeyCode.DIGIT3, () -> this.currentBlock = 2, true);
		engine.setOnKey(KeyCode.DIGIT4, () -> this.currentBlock = 3, true);
		engine.setOnKey(KeyCode.DIGIT5, () -> this.currentBlock = 4, true);
		engine.setOnKey(KeyCode.DIGIT6, () -> this.currentBlock = 5, true);
		engine.setOnKey(KeyCode.DIGIT7, () -> this.currentBlock = 6, true);
		engine.setOnKey(KeyCode.DIGIT8, () -> this.currentBlock = 7, true);
		
		engine.setOnKey(KeyCode.B, () -> {
			int chunkX = player.getChunkX();
			int chunkY = player.getChunkY();
			int chunkZ = player.getChunkZ();
			Chunk chunk = world.getChunkAt(chunkX, chunkY, chunkZ);
			if (chunk != null){
				chunkManager.saveChunkToFile(chunk);
				System.out.println(chunk+" saved");
			}
		}, true);
		engine.setOnKey(KeyCode.V, () -> {
			chunkManager.loadChunkFromFile(0, 2, 0);
			for (Chunk chunk : world.getChunks().values()){
				chunk.setupFaces();
			}
		}, true);

		final double speed = 0.4;
		engine.setOnKey(KeyCode.W, () -> player.move(world, speed*Math.cos(player.getRy()+Math.PI/2), 0, speed*Math.sin(player.getRy()+Math.PI/2)), false);
		engine.setOnKey(KeyCode.A, () -> player.move(world, -speed*Math.cos(player.getRy()), 0, -speed*Math.sin(player.getRy())), false);
		engine.setOnKey(KeyCode.S, () -> player.move(world, -speed*Math.cos(player.getRy()+Math.PI/2), 0, -speed*Math.sin(player.getRy()+Math.PI/2)), false);
		engine.setOnKey(KeyCode.D, () -> player.move(world, speed*Math.cos(player.getRy()), 0, speed*Math.sin(player.getRy())), false);
		engine.setOnKey(KeyCode.SPACE, () -> player.move(world, 0, -speed, 0), false);
		engine.setOnKey(KeyCode.SHIFT, () -> player.move(world, 0, speed, 0), false);

		engine.setOnUpdate(gc -> {
			//StringBuilder builder = new StringBuilder();
			//for (Point3D[] plane : player.getCamera().getViewFrustum()){
			//	builder.append(Arrays.toString(plane).replace("], ", "],\n")+"\n\n");
			//}
			
			engine.extraText.set("Chunks generated: "+engine.getRenderedMeshes()+String.format(" Chunk: %d %d %d", player.getChunkX(), player.getChunkY(), player.getChunkZ()));
			
			if (loadChunks){
				int chunkX = player.getChunkX();
				int chunkY = player.getChunkY();
				int chunkZ = player.getChunkZ();
				boolean updated = false;
				for (int i = -CHUNKS/2; i < -CHUNKS/2+CHUNKS; i++){
					for (int j = -CHUNKS/2; j < -CHUNKS/2+CHUNKS; j++){
						for (int k = -1; k < 2; k++){ // y-chunks
							if (chunkX+i < 0 || chunkY+k < 0 || chunkZ+j < 0) continue;
							if (world.getChunkAt(chunkX+i, chunkY+k, chunkZ+j) == null){
								if ((new Point3D(chunkX, chunkY, chunkZ)).distance(new Point3D(chunkX+i, chunkY+k, chunkZ+j)) <= ChunkManager.RENDER_DISTANCE){
									chunkManager.loadChunk(chunkX+i, chunkY+k, chunkZ+j);
									updated = true;
								}
							}
						}
					}
				}
				if (updated){
					for (Chunk chunk : world.getChunks().values()){
						chunk.setupFaces();
					}
				}
			}
			
			chunkManager.manage();
			
			// Draw chunkMap
			// TODO Math.round(chunkPos.subtract(cpos).dotProduct(camDir)) < 0
			/*gc.save();
			gc.beginPath();
			gc.rect(20, 50, 75, 75);
			gc.clip();
			gc.closePath();
			gc.setStroke(Color.BLACK);
			gc.strokeRect(20, 50, 75, 75);
			int chunkSize = 15;
			gc.translate(20, 50);
			for (Chunk chunk : world.getChunks().values()){
				if (chunk.getY() != 2) continue;
				gc.setFill((chunk.getX() == player.getChunkX() && chunk.getZ() == player.getChunkZ()) ? Color.RED : Color.WHITE);
				for (MeshGroup mg : Engine3D.getInstance().getObjects()){
					if (mg.tag != null && mg.tag.equals(World.getChunkTag(chunk.getX(), chunk.getY(), chunk.getZ()))){
						//if (mg.skipCondition.test(player.getCamera())) gc.setFill(Color.BLUE);
						break;
					}
				}
				gc.fillRect(chunk.getX()*chunkSize, chunk.getZ()*chunkSize, chunkSize, chunkSize);
			}
			gc.restore();*/
		});
		
		stage.setResizable(false);
		stage.setScene(engine.getScene());
		stage.show();
	}
	
	public static void main(String[] args){		
		launch(args);
	}
}
