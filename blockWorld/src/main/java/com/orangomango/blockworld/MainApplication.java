package com.orangomango.blockworld;

import javafx.application.Application;
import javafx.geometry.Point3D;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import com.orangomango.rendering3d.model.*;
import com.orangomango.rendering3d.Engine3D;
import com.orangomango.blockworld.model.*;

public class MainApplication extends Application{
	private static final int WIDTH = 320;
	private static final int HEIGHT = 180;
	private static final double RENDER_DISTANCE = 4;
	private static final int CHUNKS = 5;
	
	@Override
	public void start(Stage stage){
		stage.setTitle("BlockWorld");
		
		Engine3D engine = new Engine3D(stage, WIDTH, HEIGHT);

		Camera camera = new Camera(4, 3, -5);
		//camera.lookAtCenter();
		System.out.println(camera);
		double[][] m1 = Engine3D.multiply(Engine3D.multiply(Engine3D.getTranslation(-camera.getX(), -camera.getY(), -camera.getZ()),
				Engine3D.multiply(Engine3D.getRotateY(camera.getRy()), Engine3D.getRotateX(camera.getRx()))), camera.getProjectionMatrix());

		double[][] m2 = camera.getCompleteMatrix();
		System.out.println("---M2:");
		for (int i = 0; i < 4; i++) System.out.println(java.util.Arrays.toString(m2[i]));
		System.out.println("---M1:");
		for (int i = 0; i < 4; i++) System.out.println(java.util.Arrays.toString(m1[i]));
		System.out.println("---Output:");
		System.out.println(java.util.Arrays.toString(Engine3D.multiply(m1, new double[]{3, 4, 5, 1})));
		System.out.println(java.util.Arrays.toString(Engine3D.multiply(m2, new double[]{3, 4, 5, 1})));

		System.exit(0);

        /*Player player = new Player(Chunk.CHUNK_SIZE, 0, Chunk.CHUNK_SIZE);
		Camera camera = player.getCamera();
		camera.zNear = 1;
		camera.zFar = 100;
		camera.lookAtCenter();
		
		engine.setCamera(camera);
		Light light = new Light(-5, 3, 5);
		light.setFixed(true);
		engine.getLights().add(light);
		//Engine3D.LIGHT_AVAILABLE = false;
		
		World world = new World();

		engine.setOnMousePressed(e -> {
			world.removeBlockAt((int)player.getX(), (int)(player.getY()+1), (int)player.getZ());
			Chunk chunk = world.getChunkAt(player.getChunkX(), player.getChunkY(), player.getChunkZ());
			for (MeshGroup mg : engine.getObjects()){
				if (mg.tag != null && mg.tag.equals(String.format("%d %d %d", chunk.getX(), chunk.getY(), chunk.getZ()))){
					mg.updateMesh(chunk.getMesh());
				}
			}
			chunk.setupFaces();
		});

		engine.setOnKey(KeyCode.P, () -> {
			engine.getObjects().clear();
			world.clearChunks();
		});

		engine.setOnUpdate(gc -> {
			gc.setFill(Color.BLACK);
			int chunkX = player.getChunkX();
			int chunkY = player.getChunkY();
			int chunkZ = player.getChunkZ();
			gc.fillText(String.format("%d %d %d", chunkX, chunkY, chunkZ), 30, 50);
			for (int i = -CHUNKS/2; i < -CHUNKS/2+CHUNKS; i++){
				for (int j = -CHUNKS/2; j < -CHUNKS/2+CHUNKS; j++){
					for (int k = 0; k < 2; k++){
						if (chunkX+i < 0 || chunkY+k < 0 || chunkZ+j < 0) continue;
						if (world.getChunkAt(chunkX+i, chunkY+k, chunkZ+j) == null){
							Chunk chunk = world.addChunk(chunkX+i, chunkY+k, chunkZ+j);
							MeshGroup mgroup = new MeshGroup(chunk.getMesh());
							mgroup.tag = String.format("%d %d %d", chunk.getX(), chunk.getY(), chunk.getZ());
							mgroup.skipCondition = cam -> {
								Point3D cpos = new Point3D(cam.getX()/Chunk.CHUNK_SIZE, cam.getY()/Chunk.CHUNK_SIZE, cam.getZ()/Chunk.CHUNK_SIZE);
								Point3D chunkPos = new Point3D(chunk.getX(), chunk.getY(), chunk.getZ());
								return cpos.distance(chunkPos) > RENDER_DISTANCE;
							};
							engine.getObjects().add(mgroup);
						}
					}
				}
			}
			for (Chunk chunk : world.getChunks()){
				chunk.setupFaces();
			}
		});
		
		stage.setResizable(false);
		stage.setScene(engine.getScene());
		stage.show();*/
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
