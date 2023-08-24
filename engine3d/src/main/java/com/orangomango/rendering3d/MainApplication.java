package com.orangomango.rendering3d;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.text.TextAlignment;
import javafx.geometry.Point3D;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;

import java.io.File;
import java.util.Random;

import com.orangomango.rendering3d.model.Mesh;
import static com.orangomango.rendering3d.model.MeshVertex.VERTICES;
import com.orangomango.rendering3d.model.Camera;
import com.orangomango.rendering3d.model.Light;
import com.orangomango.rendering3d.meshloader.MeshLoader;

/*
 * Engine3D in Java/JavaFX
 * Refactored code, v1.0
 * Features: Camera, .obj files, shadows, image/color mesh, clipping and lights
 * @author Paul Kocian aka OrangoMango (https://orangomango.github.io)
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

		//buildScene1(engine);
		//buildScene2(engine);
		buildScene3(engine);

		stage.setResizable(false);
		stage.setScene(engine.getScene());
		stage.show();
	}

	private void buildScene3(Engine3D engine){
		MeshLoader loader = null;
		try {
			loader = new MeshLoader(new File(getClass().getResource("/rendering3d.obj").toURI()));
			loader.setScale(0.02);
		} catch (Exception ex){
			ex.printStackTrace();
		}
		Mesh loadedObject = loader.load(false);
		loadedObject.setRotation(Math.PI/2, 0, 0);

		Light light = new Light(new Camera(new Point3D(-6, -6, -3), WIDTH, HEIGHT, Math.PI/4, 100, 0.3));

		engine.getObjects().add(loadedObject);
		engine.addLight(light);

		engine.setOnUpdate(gc -> {
			gc.setFill(Color.BLACK);
			gc.setTextAlign(TextAlignment.RIGHT);
			gc.fillText("Vertices: "+VERTICES.size(), WIDTH*0.95, HEIGHT*0.1);
		});
	}

	private void buildScene2(Engine3D engine){
		final int width = 15;
		final int height = 15;
		final int depth = 15;

		Random random = new Random();
		Image[] images = new Image[]{new Image(getClass().getResourceAsStream("/coal.png")),
									new Image(getClass().getResourceAsStream("/stone.png")),
									new Image(getClass().getResourceAsStream("/wood.png")),
									new Image(getClass().getResourceAsStream("/texture-block.png"))};

		for (int i = 0; i < width; i++){
			for (int j = 0; j < height; j++){
				for (int k = 0; k < depth; k++){
					Mesh block = new Mesh(new Point3D[]{
						new Point3D(i, j, k), new Point3D(i, j+1, k), new Point3D(i+1, j+1, k), new Point3D(i+1, j, k),
						new Point3D(i, j, k+1), new Point3D(i, j+1, k+1), new Point3D(i+1, j+1, k+1), new Point3D(i+1, j, k+1)
					}, new int[][]{
						{0, 1, 2}, {0, 2, 3}, {3, 2, 6},
						{3, 6, 7}, {7, 6, 5}, {7, 5, 4},
						{4, 5, 1}, {4, 1, 0}, {1, 5, 6},
						{1, 6, 2}, {4, 0, 3}, {4, 3, 7}
					}, null, new Image[]{images[random.nextInt(images.length)]},
					new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, new Point2D[]{
						new Point2D(0, 0), new Point2D(0, 1), new Point2D(1, 1), new Point2D(1, 0)
					}, new int[][]{
						{0, 1, 2}, {0, 2, 3}, {0, 1, 2}, {0, 2, 3},
						{0, 1, 2}, {0, 2, 3}, {0, 1, 2}, {0, 2, 3},
						{0, 1, 2}, {0, 2, 3}, {0, 1, 2}, {0, 2, 3},
						{0, 1, 2}, {0, 2, 3}, {0, 1, 2}, {0, 2, 3}
					});

					if (i > 0){ // Left
						block.addHiddenFace(6);
						block.addHiddenFace(7);
					}
					if (i < width-1){ // Right
						block.addHiddenFace(2);
						block.addHiddenFace(3);
					}
					if (j > 0){ // Top
						block.addHiddenFace(10);
						block.addHiddenFace(11);
					}
					if (j < height-1){ // Bottom
						block.addHiddenFace(8);
						block.addHiddenFace(9);
					}
					if (k > 0){ // Front
						block.addHiddenFace(0);
						block.addHiddenFace(1);
					}
					if (k < depth-1){ // Back
						block.addHiddenFace(4);
						block.addHiddenFace(5);
					}

					//block.setShowAllFaces(true);
					engine.getObjects().add(block);
				}
			}
		}

		Light light = new Light();
		engine.addLight(light);

		engine.setOnUpdate(gc -> {
			gc.setFill(Color.BLACK);
			gc.setTextAlign(TextAlignment.RIGHT);
			gc.fillText("Vertices: "+VERTICES.size(), WIDTH*0.95, HEIGHT*0.1);
		});
	}

	private void buildScene1(Engine3D engine){
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
			new Point2D(0, 0), new Point2D(0, 1), new Point2D(1, 1), new Point2D(1, 0)
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

		engine.setOnKey(KeyCode.F6, () -> ROTATE_LIGHT = !ROTATE_LIGHT, true);

		engine.getObjects().add(object);
		engine.getObjects().add(object2);
		engine.getObjects().add(loadedObject);
		engine.getObjects().add(shadowObject);

		engine.addLight(light);
	}
	
	public static void main(String[] args){		
		System.out.println("F1 -> SHOW_LINES");
		System.out.println("F2 -> FOLLOW_LIGHT");
		System.out.println("F3 -> LIGHT_AVAILABLE");
		System.out.println("F4 -> PLACE_LIGHT_AT_CAMERA");
		System.out.println("F5 -> SHADOWS");
		System.out.println("F6 -> ROTATE_LIGHT");
		launch(args);
	}
}
