package com.orangomango.rendering3d;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.geometry.Point3D;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;

import java.io.File;

import com.orangomango.rendering3d.model.Mesh;
import com.orangomango.rendering3d.model.Camera;
import com.orangomango.rendering3d.model.Light;
import com.orangomango.rendering3d.meshloader.MeshLoader;

/*
 * @author OrangoMango (https://orangomango.github.io)
 */
public class MainApplication extends Application{
	private static final int WIDTH = 320;
	private static final int HEIGHT = 180;
	private static boolean ROTATE_LIGHT = false;
	
	@Override
	public void start(Stage stage){
		stage.setTitle("3D Graphics");
		
		Engine3D engine = new Engine3D(stage, WIDTH, HEIGHT);
		Camera camera = new Camera(new Point3D(0, 0, -3), WIDTH, HEIGHT, Math.PI/4, 100, 0.3);
		engine.setCamera(camera);

		double speed = 0.3;
		engine.setOnKey(KeyCode.W, () -> camera.move(new Point3D(speed*Math.cos(camera.getRy()+Math.PI/2), 0, speed*Math.sin(camera.getRy()+Math.PI/2))), false);
		engine.setOnKey(KeyCode.A, () -> camera.move(new Point3D(-speed*Math.cos(camera.getRy()), 0, -speed*Math.sin(camera.getRy()))), false);
		engine.setOnKey(KeyCode.S, () -> camera.move(new Point3D(-speed*Math.cos(camera.getRy()+Math.PI/2), 0, -speed*Math.sin(camera.getRy()+Math.PI/2))), false);
		engine.setOnKey(KeyCode.D, () -> camera.move(new Point3D(speed*Math.cos(camera.getRy()), 0, speed*Math.sin(camera.getRy()))), false);
		engine.setOnKey(KeyCode.R, () -> camera.reset(), true);
		engine.setOnKey(KeyCode.SPACE, () -> camera.move(new Point3D(0, -speed, 0)), false);
		engine.setOnKey(KeyCode.SHIFT, () -> camera.move(new Point3D(0, speed, 0)), false);

		engine.setOnKey(KeyCode.F5, () -> ROTATE_LIGHT = !ROTATE_LIGHT, true);
		engine.setOnKey(KeyCode.LEFT, () -> com.orangomango.rendering3d.model.ProjectedTriangle.SHADOW_EPSILON -= 0.0005, true);
		engine.setOnKey(KeyCode.RIGHT, () -> com.orangomango.rendering3d.model.ProjectedTriangle.SHADOW_EPSILON += 0.0005, true);
		engine.setOnKey(KeyCode.DOWN, () -> System.out.println(com.orangomango.rendering3d.model.ProjectedTriangle.SHADOW_EPSILON), true);

		Mesh object = new Mesh(new Point3D[]{
			new Point3D(0, 0, 0), new Point3D(0, 1, 0), new Point3D(1, 1, 0), new Point3D(1, 0, 0),
			new Point3D(0, 0, 1), new Point3D(0, 1, 1), new Point3D(1, 1, 1), new Point3D(1, 0, 1)
		}, new int[][]{
			{0, 1, 2}, {0, 2, 3}, {3, 2, 6},
			{3, 6, 7}, {7, 6, 5}, {7, 5, 4},
			{4, 5, 1}, {4, 1, 0}, {1, 5, 6},
			{1, 6, 2}, {4, 0, 3}, {4, 3, 7}
		}, null, null, new Color[]{
			Color.color(Math.random(), Math.random(), Math.random()), Color.color(Math.random(), Math.random(), Math.random()), Color.color(Math.random(), Math.random(), Math.random()),
			Color.color(Math.random(), Math.random(), Math.random()), Color.color(Math.random(), Math.random(), Math.random()), Color.color(Math.random(), Math.random(), Math.random()),
			Color.color(Math.random(), Math.random(), Math.random()), Color.color(Math.random(), Math.random(), Math.random()), Color.color(Math.random(), Math.random(), Math.random()),
			Color.color(Math.random(), Math.random(), Math.random()), Color.color(Math.random(), Math.random(), Math.random()), Color.color(Math.random(), Math.random(), Math.random())
		});

		Mesh object2 = new Mesh(new Point3D[]{
			new Point3D(1.2, 0, 0), new Point3D(1.2, 1, 0), new Point3D(2.2, 1, 0), new Point3D(2.2, 0, 0),
			new Point3D(1.2, 0, 1), new Point3D(1.2, 1, 1), new Point3D(2.2, 1, 1), new Point3D(2.2, 0, 1)
		}, new int[][]{
			{0, 1, 2}, {0, 2, 3}, {3, 2, 6},
			{3, 6, 7}, {7, 6, 5}, {7, 5, 4},
			{4, 5, 1}, {4, 1, 0}, {1, 5, 6},
			{1, 6, 2}, {4, 0, 3}, {4, 3, 7}
		}, null, new Image[]{new Image(getClass().getResourceAsStream("/coal.png"))},
		new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, new Point2D[]{
			new Point2D(0, 1), new Point2D(0, 0), new Point2D(1, 0), new Point2D(1, 1)
		}, new int[][]{
			{0, 1, 2}, {0, 2, 3}, {0, 1, 2}, {0, 2, 3},
			{0, 1, 2}, {0, 2, 3}, {0, 1, 2}, {0, 2, 3},
			{0, 1, 2}, {0, 2, 3}, {0, 1, 2}, {0, 2, 3},
			{0, 1, 2}, {0, 2, 3}, {0, 1, 2}, {0, 2, 3}
		});

		MeshLoader loader = null;
		try {
			loader = new MeshLoader(new File(getClass().getResource("/model.obj").toURI()));
			loader.setPosition(new Point3D(4, 0, 0));
			loader.setScale(0.02);
		} catch (Exception ex){
			ex.printStackTrace();
		}
		Mesh loadedObject = loader.load(false);
		loadedObject.setRotation(Math.PI/2, 0, 0);

		loader = null;
		try {
			loader = new MeshLoader(new File(getClass().getResource("/shadows.obj").toURI()));
			loader.setPosition(new Point3D(0, 0, 4));
			loader.setScale(0.5);
		} catch (Exception ex){
			ex.printStackTrace();
		}
		Mesh shadowObject = loader.load(false);
		shadowObject.setRotation(0, 0, Math.PI);

		Light light = new Light(new Camera(new Point3D(7.5, -3.9, 12), WIDTH, HEIGHT, Math.PI/4, 100, 0.3));
		light.getCamera().setRx(-0.4);
		light.getCamera().setRy(2.2);
		Thread rotateLight = new Thread(() -> {
			while (true){
				try {
					if (ROTATE_LIGHT){
						double[] rotationV = Engine3D.multiply(Engine3D.getRotateY(0.06), new double[]{light.getCamera().getPosition().getX(), light.getCamera().getPosition().getY(), light.getCamera().getPosition().getZ()});
						light.getCamera().setPosition(new Point3D(rotationV[0], rotationV[1], rotationV[2]));
						light.getCamera().lookAtCenter();
					}
					Thread.sleep(50);
				} catch (InterruptedException ex){
					ex.printStackTrace();
				}
			}
		});
		rotateLight.setDaemon(true);
		rotateLight.start();

		engine.addObject(object);
		engine.addObject(object2);
		engine.addObject(loadedObject);
		engine.addObject(shadowObject);
		engine.addLight(light);

		stage.setResizable(false);
		stage.setScene(engine.getScene());
		stage.show();
	}
	
	public static void main(String[] args){		
		System.out.println("F1 -> SHOW_LINES");
		System.out.println("F2 -> FOLLOW_LIGHT");
		System.out.println("F3 -> LIGHT_AVAILABLE");
		System.out.println("F4 -> PLACE_LIGHT_AT_CAMERA");
		System.out.println("F5 -> ROTATE_LIGHT");
		System.out.println("F6 -> SHADOWS");
		launch(args);
	}
}
