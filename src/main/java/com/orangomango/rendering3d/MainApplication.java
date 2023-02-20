package com.orangomango.rendering3d;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.io.File;

import com.orangomango.rendering3d.model.Camera;
import com.orangomango.rendering3d.model.Light;
import com.orangomango.rendering3d.model.Mesh;

public class MainApplication extends Application{
	private static final int WIDTH = 640; //320;
	private static final int HEIGHT = 360; //180;

	public static final Image COAL_IMAGE = new Image(MainApplication.class.getResourceAsStream("/coal.png"));
	//private static final Image DIRT_IMAGE = new Image(MainApplication.class.getResourceAsStream("/dirt.png"));
	//private static final Image STONE_IMAGE = new Image(MainApplication.class.getResourceAsStream("/stone.png"));
	
	@Override
	public void start(Stage stage){		
		stage.setTitle("3D Graphics");
		
		Engine3D engine = new Engine3D(stage, WIDTH, HEIGHT);
		Camera camera = new Camera(0, 7.5, -5);
		camera.lookAtCenter();
		
		engine.setCamera(camera);
		
		//engine.getLights().add(new Light(-5, -1, -5));
		engine.getLights().add(new Light(-15, 0, 30));
		
		//Random random = new Random();
		/*for (int i = 0; i < 1; i++){
			for (int j = 0; j < 1; j++){
				for (int k = 0; k < 1; k++){
					objects.add(new Mesh(switch(random.nextInt(3)){
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
		}*/
		
		try {
			//Mesh model = Mesh.loadFromFile(new File(MainApplication.class.getResource("/model.obj").toURI()), 0, 0, 0, 0.05, null, null);
			//model.setRotation(Math.PI/2, 0, 0);
			//engine.getObjects().add(model);
			engine.getObjects().add(Mesh.loadFromFile(new File(MainApplication.class.getResource("/plane3.obj").toURI()), 0, 0.5, 0, 0.5, null, null));
			
			//Mesh model = Mesh.loadFromFile(new File(MainApplication.class.getResource("/chess.obj").toURI()), 0, 0, 0, 10, null, null);
			//model.setRotation(0, 0, Math.PI);
			//engine.getObjects().add(model);
		} catch (Exception ex){
			ex.printStackTrace();
		}
		
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
