package com.orangomango.blockworld;

import javafx.application.Application;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import com.orangomango.rendering3d.model.Camera;
import com.orangomango.rendering3d.model.Light;
import com.orangomango.rendering3d.model.Mesh;
import com.orangomango.rendering3d.Engine3D;
import com.orangomango.blockworld.model.*;

public class MainApplication extends Application{
	private static final int WIDTH = 320;
	private static final int HEIGHT = 180;
	
	public static final Image COAL_IMAGE = new Image(MainApplication.class.getResourceAsStream("/coal.png"));
	public static final Image DIRT_IMAGE = new Image(MainApplication.class.getResourceAsStream("/dirt.png"));
	public static final Image STONE_IMAGE = new Image(MainApplication.class.getResourceAsStream("/stone.png"));
	
	@Override
	public void start(Stage stage){
		stage.setTitle("BlockWorld");
		
		Engine3D engine = new Engine3D(stage, WIDTH, HEIGHT);
        Player player = new Player(0, 0, 0);
		Camera camera = player.getCamera();
		camera.zNear = 1;
		camera.zFar = 100;
		camera.lookAtCenter();
		
		engine.setCamera(camera);
		engine.getLights().add(new Light(-5, 3, 5));
		Engine3D.LIGHT_AVAILABLE = false;
		
		World world = new World(3, 1, 4);
		world.removeBlockAt(0, 0, 0);
		for (Mesh mesh : world.getMesh()){
			engine.getObjects().add(mesh);
		}

		engine.setOnKey(KeyCode.P, () -> {
			engine.getObjects().clear();
			world.clearChunks();
		});

		engine.setOnUpdate(gc -> {
			gc.setFill(Color.BLACK);
			int chunkX = (int)Math.floor(player.getX()/Chunk.CHUNK_SIZE);
			int chunkY = (int)Math.floor(player.getY()/Chunk.CHUNK_SIZE);
			int chunkZ = (int)Math.floor(player.getZ()/Chunk.CHUNK_SIZE);
			gc.fillText(String.format("%d %d %d", chunkX, chunkY, chunkZ), 30, 50);
			for (int i = -1; i < 2; i++){
				for (int j = -1; j < 2; j++){
					if (world.getChunkAt(chunkX+i, chunkY+1, chunkZ+j) == null){
						Chunk chunk = world.addChunk(chunkX+i, chunkY+1, chunkZ+j);
						for (Mesh mesh : chunk.getMesh()){
							engine.getObjects().add(mesh);
						}
					}
				}
			}
		});
		
		stage.setResizable(false);
		stage.setScene(engine.getScene());
		stage.show();
	}
	
	public static void main(String[] args){		
		System.out.println("F1 -> SHOW_LINES");
		System.out.println("F2 -> FOLLOW_LIGHT");
		System.out.println("F3 -> LIGHT_AVAILABLE");
		System.out.println("F4 -> ROTATE_LIGHT");
		System.out.println("F5 -> PLACE_LIGHT_AT_CAMERA");
		System.out.println("F6 -> SHADOWS");
		launch(args);
	}
}
