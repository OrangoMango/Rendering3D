package com.orangomango.rendering3d;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.geometry.Point3D;

import com.orangomango.rendering3d.model.Mesh;
import com.orangomango.rendering3d.model.Camera;

/*
 * @author OrangoMango (https://orangomango.github.io)
 */
public class MainApplication extends Application{
	private static final int WIDTH = 320;
	private static final int HEIGHT = 180;
	
	@Override
	public void start(Stage stage){
		stage.setTitle("3D Graphics");
		
		Engine3D engine = new Engine3D(stage, WIDTH, HEIGHT);
		Camera camera = new Camera(new Point3D(0, 7.5, -5), WIDTH, HEIGHT, Math.PI/4, 100, 0.3);
		engine.setCamera(camera);

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
