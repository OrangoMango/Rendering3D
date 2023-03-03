package com.orangomango.blockworld;

import javafx.application.Application;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import com.orangomango.rendering3d.model.Camera;
import com.orangomango.rendering3d.model.Light;
import com.orangomango.rendering3d.model.Mesh;
import com.orangomango.rendering3d.Engine3D;
import com.orangomango.blockworld.model.World;
import com.orangomango.blockworld.model.Chunk;

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
		Camera camera = new Camera(0, 0, -1);
		camera.zNear = 1;
		camera.lookAtCenter();
		
		engine.setCamera(camera);
		engine.getLights().add(new Light(-5, 3, 5));
		Engine3D.LIGHT_AVAILABLE = false;
		
		World world = new World(3, 1, 3);
		world.removeBlockAt(0, 0, 0);
		for (Mesh mesh : world.getMesh()){
			engine.getObjects().add(mesh);
		}

		engine.setOnKey(KeyCode.P, () -> {
			Chunk chunk = world.addChunk();
			for (Mesh mesh : chunk.getMesh()){
				engine.getObjects().add(mesh);
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
