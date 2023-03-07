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
	private static final int CHUNKS = 3;
	
	@Override
	public void start(Stage stage){
		stage.setTitle("BlockWorld");
		
		Engine3D engine = new Engine3D(stage, WIDTH, HEIGHT);
        Player player = new Player(Chunk.CHUNK_SIZE, 0, Chunk.CHUNK_SIZE);
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

		/*engine.setOnMousePressed(e -> {
			double w = camera.depthBuffer[(int)e.getX()][(int)e.getY()];
			double[] pos = Engine3D.revertPoint(new double[]{e.getX(), e.getY(), w}, camera);
			System.out.println(java.util.Arrays.toString(pos));
			int worldX = (int)Math.round(pos[0]);
			int worldY = (int)Math.round(pos[1]);
			int worldZ = (int)Math.round(pos[2]);
			world.removeBlockAt(worldX, worldY, worldZ);
		});*/

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
			for (int i = -CHUNKS/2; i < -CHUNKS/2+CHUNKS; i++){
				for (int j = -CHUNKS/2; j < -CHUNKS/2+CHUNKS; j++){
					for (int k = 0; k < 2; k++){
						if (chunkX+i < 0 || chunkY+k < 0 || chunkZ+j < 0) continue;
						if (world.getChunkAt(chunkX+i, chunkY+k, chunkZ+j) == null){
							Chunk chunk = world.addChunk(chunkX+i, chunkY+k, chunkZ+j);
							MeshGroup mgroup = new MeshGroup(chunk.getMesh());
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
