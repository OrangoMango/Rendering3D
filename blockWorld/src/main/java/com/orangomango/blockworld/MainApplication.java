package com.orangomango.blockworld;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;

import java.io.File;
import java.util.Random;

import com.orangomango.rendering3d.model.Camera;
import com.orangomango.rendering3d.model.Light;
import com.orangomango.rendering3d.model.Mesh;
import com.orangomango.rendering3d.Engine3D;

public class MainApplication extends Application{
	private static final int WIDTH = 640; //320;
	private static final int HEIGHT = 360; //180;
	
	private static final Image COAL_IMAGE = new Image(MainApplication.class.getResourceAsStream("/coal.png"));
	private static final Image DIRT_IMAGE = new Image(MainApplication.class.getResourceAsStream("/dirt.png"));
	private static final Image STONE_IMAGE = new Image(MainApplication.class.getResourceAsStream("/stone.png"));
	
	@Override
	public void start(Stage stage){		
		stage.setTitle("BlockWorld");
		
		Engine3D engine = new Engine3D(stage, WIDTH, HEIGHT);
		Camera camera = new Camera(0, 0, -3);
		camera.lookAtCenter();
		
		engine.setCamera(camera);
		engine.getLights().add(new Light(-5, 3, 5));
		
		Random random = new Random();
		for (int i = 0; i < 2; i++){
			for (int j = 0; j < 1; j++){
				for (int k = 0; k < 1; k++){
					engine.getObjects().add(new Mesh(switch(random.nextInt(3)){
						case 0 -> COAL_IMAGE;
						case 1 -> DIRT_IMAGE;
						case 2 -> STONE_IMAGE;
						default -> null;
					}, new Point3D[]{
						new Point3D(i, k, j), new Point3D(i, 1+k, j), new Point3D(1+i, 1+k, j),
						new Point3D(1+i, k, j), new Point3D(i, k, 1+j), new Point3D(i, 1+k, 1+j),
						new Point3D(1+i, 1+k, 1+j), new Point3D(1+i, k, 1+j)}, new int[][]{
							{0, 1, 2}, {0, 2, 3}, {3, 2, 6},
							{3, 6, 7}, {7, 6, 5}, {7, 5, 4},
							{4, 5, 1}, {4, 1, 0}, {1, 5, 6},
							{1, 6, 2}, {4, 0, 3}, {4, 3, 7}
					}, new Point2D[]{
						new Point2D(0, 1), new Point2D(0, 0), new Point2D(1, 0), new Point2D(1, 1)
					}, new int[][]{
						{0, 1, 2}, {0, 2, 3}, {0, 1, 2}, {0, 2, 3},
						{0, 1, 2}, {0, 2, 3}, {0, 1, 2}, {0, 2, 3},
						{0, 1, 2}, {0, 2, 3}, {0, 1, 2}, {0, 2, 3},
						{0, 1, 2}, {0, 2, 3}, {0, 1, 2}, {0, 2, 3}
					}, null, null, null));
				}
			}
		}
		
		engine.getObjects().get(0).hiddenTriangles.add(Integer.valueOf(2));
		engine.getObjects().get(0).hiddenTriangles.add(Integer.valueOf(3));
		
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
