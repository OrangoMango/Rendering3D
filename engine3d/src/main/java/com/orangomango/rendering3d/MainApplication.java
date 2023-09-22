package com.orangomango.rendering3d;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.Font;
import javafx.geometry.Point3D;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;

import java.io.File;
import java.util.Random;

import com.orangomango.rendering3d.model.Mesh;
import com.orangomango.rendering3d.model.MeshVertex;
import com.orangomango.rendering3d.model.Camera;
import com.orangomango.rendering3d.model.Light;
import com.orangomango.rendering3d.meshloader.MeshLoader;

/**
 * Engine3D in Java/JavaFX
 * Refactored code, v1.0
 * Features: Camera, .obj files, shadows, image/color mesh, clipping and lights
 * @author Paul Kocian aka OrangoMango (https://orangomango.github.io)
 */
public class MainApplication extends Application{
	private static final int WIDTH = 360;
	private static final int HEIGHT = 180;
	private static boolean ROTATE_LIGHT = false;
	
	@Override
	public void start(Stage stage){
		stage.setTitle("3D Graphics");
		
		Engine3D engine = new Engine3D(stage, WIDTH, HEIGHT);
		Camera camera = new Camera(new Point3D(0, 0, -3), WIDTH, HEIGHT, Math.PI/4, 100, 0.3);
		engine.setCamera(camera);

		final double speed = 0.3;
		engine.setOnKey(KeyCode.W, () -> camera.move(new Point3D(speed*Math.cos(camera.getRy()+Math.PI/2), 0, speed*Math.sin(camera.getRy()+Math.PI/2))), false);
		engine.setOnKey(KeyCode.A, () -> camera.move(new Point3D(-speed*Math.cos(camera.getRy()), 0, -speed*Math.sin(camera.getRy()))), false);
		engine.setOnKey(KeyCode.S, () -> camera.move(new Point3D(-speed*Math.cos(camera.getRy()+Math.PI/2), 0, -speed*Math.sin(camera.getRy()+Math.PI/2))), false);
		engine.setOnKey(KeyCode.D, () -> camera.move(new Point3D(speed*Math.cos(camera.getRy()), 0, speed*Math.sin(camera.getRy()))), false);
		engine.setOnKey(KeyCode.R, () -> camera.reset(), true);
		engine.setOnKey(KeyCode.SPACE, () -> camera.move(new Point3D(0, -speed, 0)), false);
		engine.setOnKey(KeyCode.SHIFT, () -> camera.move(new Point3D(0, speed, 0)), false);

		buildScene5(engine);
		engine.setOnKey(KeyCode.DIGIT1, () -> {
			engine.clearObjects();
			engine.getLights().clear();
			buildScene1(engine);
		}, true);
		engine.setOnKey(KeyCode.DIGIT2, () -> {
			engine.clearObjects();
			engine.getLights().clear();
			buildScene2(engine);
		}, true);
		engine.setOnKey(KeyCode.DIGIT3, () -> {
			engine.clearObjects();
			engine.getLights().clear();
			buildScene3(engine);
		}, true);
		engine.setOnKey(KeyCode.DIGIT4, () -> {
			engine.clearObjects();
			engine.getLights().clear();
			buildScene4(engine);
		}, true);
		engine.setOnKey(KeyCode.DIGIT5, () -> {
			engine.clearObjects();
			engine.getLights().clear();
			buildScene5(engine);
		}, true);

		stage.setResizable(false);
		stage.setScene(engine.getScene());
		stage.show();
	}

	private void buildScene5(Engine3D engine){
		final int width = 31;
		final int depth = 31;

		for (int i = 0; i < width; i++){
			for (int j = 0; j < depth; j++){
				Color top = Color.color(Math.random(), Math.random(), Math.random());
				Color bottom = Color.color(Math.random(), Math.random(), Math.random());
				Color right = Color.color(Math.random(), Math.random(), Math.random());
				Color left = Color.color(Math.random(), Math.random(), Math.random());
				Color front = Color.color(Math.random(), Math.random(), Math.random());
				Color back = Color.color(Math.random(), Math.random(), Math.random());

				Mesh object = new Mesh(new Point3D[]{
					new Point3D(i, 0, j), new Point3D(i, 1, j), new Point3D(i+1, 1, j), new Point3D(i+1, 0, j),
					new Point3D(i, 0, j+1), new Point3D(i, 1, j+1), new Point3D(i+1, 1, j+1), new Point3D(i+1, 0, j+1)
				}, new int[][]{
					{0, 1, 2}, {0, 2, 3}, {3, 2, 6},
					{3, 6, 7}, {7, 6, 5}, {7, 5, 4},
					{4, 5, 1}, {4, 1, 0}, {1, 5, 6},
					{1, 6, 2}, {4, 0, 3}, {4, 3, 7}
				}, null, null, new Color[]{
					front, front, right, right, back, back, left, left, bottom, bottom, top, top
				});

				setupHiddenFaces(object, i, 0, j, width, 1, depth);

				object.build();
				engine.addObject(object);
			}
		}

		engine.getLights().add(new Light(1));

		// New camera
		engine.getCamera().setPosition(new Point3D(0, 0, 0));
		engine.getCamera().setZnear(0.1);
		engine.getCamera().setZfar(10);
		engine.getCamera().setFov(Math.PI/2);
		engine.getCamera().setRx(-Math.PI/4);
		engine.getCamera().setRy(0);

		// -------------------- DEBUG --------------------
		/*{
			java.util.function.Function<Point3D, String> read = p -> String.format("[x=%.2f y=%.2f z=%.2f]", p.getX(), p.getY(), p.getZ());
			String[] texts = new String[]{"front", "back", "right", "left", "top", "bottom"};
			Point3D[][] frustum = engine.getCamera().getViewFrustum(false);
			for (int i = 0; i < frustum.length; i++){
				Point3D[] plane = frustum[i];
				System.out.println(texts[i]+"\t| planeN: "+read.apply(plane[0])+"\tplaneA: "+read.apply(plane[1]));
			}

			Point3D v1 = new Point3D(5, 1, 1);
			Point3D v2 = new Point3D(5, 1, 1);
			Point3D v3 = new Point3D(5, 1, 1);

			boolean test = true;
			for (Point3D[] plane : frustum){
				double d1 = Engine3D.distanceToPlane(plane[0], plane[1], v1, plane[0].multiply(-1));
				double d2 = Engine3D.distanceToPlane(plane[0], plane[1], v2, plane[0].multiply(-1));
				double d3 = Engine3D.distanceToPlane(plane[0], plane[1], v3, plane[0].multiply(-1));
				if (d1 > 0 && d2 > 0 && d3 > 0){
					test = false;
					break;
				}
			}
			System.out.println(test);

			double[] rot = Engine3D.multiply(Engine3D.getRotateY(-1.107148), new double[]{1, 0, 0});
			rot = Engine3D.multiply(Engine3D.getRotateX(-Math.PI/4), rot);
			System.out.format("Rotated: %.2f %.2f %.2f\n", rot[0], rot[1], rot[2]);

			System.exit(0);
		}*/
		// -----------------------------------------------

		int[] modifier = new int[]{0};

		engine.setOnKey(KeyCode.LEFT, () -> {
			switch (modifier[0]){
				case 0 -> engine.getCamera().setZnear(engine.getCamera().getZnear()-0.1);
				case 1 -> engine.getCamera().setZfar(engine.getCamera().getZfar()-0.1);
				case 2 -> engine.getCamera().setFov(engine.getCamera().getFov()-Math.toRadians(1));
			}
			System.out.format("zN: %.2f zF: %.2f Fov: %.2f\n", engine.getCamera().getZnear(), engine.getCamera().getZfar(), Math.toDegrees(engine.getCamera().getFov()));
		}, true);
		engine.setOnKey(KeyCode.RIGHT, () -> {
			switch (modifier[0]){
				case 0 -> engine.getCamera().setZnear(engine.getCamera().getZnear()+0.1);
				case 1 -> engine.getCamera().setZfar(engine.getCamera().getZfar()+0.1);
				case 2 -> engine.getCamera().setFov(engine.getCamera().getFov()+Math.toRadians(1));
			}
			System.out.format("zN: %.2f zF: %.2f Fov: %.2f\n", engine.getCamera().getZnear(), engine.getCamera().getZfar(), Math.toDegrees(engine.getCamera().getFov()));
		}, true);

		engine.setOnKey(KeyCode.B, () -> {modifier[0] = 0; System.out.println("Mod0");}, true);
		engine.setOnKey(KeyCode.N, () -> {modifier[0] = 1; System.out.println("Mod1");}, true);
		engine.setOnKey(KeyCode.M, () -> {modifier[0] = 2; System.out.println("Mod2");}, true);

		engine.setOnUpdate(gc -> {
			gc.setFill(Color.BLACK);
			gc.setTextAlign(TextAlignment.RIGHT);
			gc.setFont(new Font("sans-serif", 11));
			String text = "Projected: "+MeshVertex.getProjectedVerticesCount();
			text += "\nView: "+MeshVertex.getViewVerticesCount();
			gc.fillText(text, WIDTH*0.95, HEIGHT*0.1);
		});
	}

	private void buildScene4(Engine3D engine){
		for (int i = 0; i < 4; i++){
			Mesh wood = new Mesh(new Point3D[]{
				new Point3D(i, 0, 0), new Point3D(i, 1, 0), new Point3D(i+1, 1, 0), new Point3D(i+1, 0, 0),
				new Point3D(i, 0, 1), new Point3D(i, 1, 1), new Point3D(i+1, 1, 1), new Point3D(i+1, 0, 1)
			}, new int[][]{
				{0, 1, 2}, {0, 2, 3}, {3, 2, 6},
				{3, 6, 7}, {7, 6, 5}, {7, 5, 4},
				{4, 5, 1}, {4, 1, 0}, {1, 5, 6},
				{1, 6, 2}, {4, 0, 3}, {4, 3, 7}
			}, null, new Image[]{new Image(getClass().getResourceAsStream("/wood.png"))},
			new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, new Point2D[]{
				new Point2D(0, 0), new Point2D(0, 1), new Point2D(1, 1), new Point2D(1, 0)
			}, new int[][]{
				{0, 1, 2}, {0, 2, 3}, {0, 1, 2}, {0, 2, 3},
				{0, 1, 2}, {0, 2, 3}, {0, 1, 2}, {0, 2, 3},
				{0, 1, 2}, {0, 2, 3}, {0, 1, 2}, {0, 2, 3},
				{0, 1, 2}, {0, 2, 3}, {0, 1, 2}, {0, 2, 3}
			});
			setupHiddenFaces(wood, i, 0, 0, 4, 1, 1);
			wood.build();
			engine.addObject(wood);

			Mesh glass = new Mesh(new Point3D[]{
				new Point3D(i, -1, 0), new Point3D(i, 0, 0), new Point3D(i+1, 0, 0), new Point3D(i+1, -1, 0),
				new Point3D(i, -1, 1), new Point3D(i, 0, 1), new Point3D(i+1, 0, 1), new Point3D(i+1, -1, 1)
			}, new int[][]{
				{0, 1, 2}, {0, 2, 3}, {3, 2, 6},
				{3, 6, 7}, {7, 6, 5}, {7, 5, 4},
				{4, 5, 1}, {4, 1, 0}, {1, 5, 6},
				{1, 6, 2}, {4, 0, 3}, {4, 3, 7}
			}, null, new Image[]{new Image(getClass().getResourceAsStream(i % 2 == 0 && i > 0 ? "/glass.png" : "/glass_red.png"))},
			new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, new Point2D[]{
				new Point2D(0, 0), new Point2D(0, 1), new Point2D(1, 1), new Point2D(1, 0)
			}, new int[][]{
				{0, 1, 2}, {0, 2, 3}, {0, 1, 2}, {0, 2, 3},
				{0, 1, 2}, {0, 2, 3}, {0, 1, 2}, {0, 2, 3},
				{0, 1, 2}, {0, 2, 3}, {0, 1, 2}, {0, 2, 3},
				{0, 1, 2}, {0, 2, 3}, {0, 1, 2}, {0, 2, 3}
			});
			for (int j = 0; j < 12; j++) glass.getTriangles()[j].setImageTransparent(true);
			setupHiddenFaces(glass, i, 0, 0, 4, 1, 1);
			glass.build();
			engine.addObject(glass);

			for (int j = 0; j < 3; j++){
				Mesh block = new Mesh(new Point3D[]{
					new Point3D(i, -j, 2), new Point3D(i, -j+1, 2), new Point3D(i+1, -j+1, 2), new Point3D(i+1, -j, 2),
					new Point3D(i, -j, 3), new Point3D(i, -j+1, 3), new Point3D(i+1, -j+1, 3), new Point3D(i+1, -j, 3)
				}, new int[][]{
					{0, 1, 2}, {0, 2, 3}, {3, 2, 6},
					{3, 6, 7}, {7, 6, 5}, {7, 5, 4},
					{4, 5, 1}, {4, 1, 0}, {1, 5, 6},
					{1, 6, 2}, {4, 0, 3}, {4, 3, 7}
				}, null, new Image[]{new Image(getClass().getResourceAsStream("/stone.png"))},
				new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, new Point2D[]{
					new Point2D(0, 0), new Point2D(0, 1), new Point2D(1, 1), new Point2D(1, 0)
				}, new int[][]{
					{0, 1, 2}, {0, 2, 3}, {0, 1, 2}, {0, 2, 3},
					{0, 1, 2}, {0, 2, 3}, {0, 1, 2}, {0, 2, 3},
					{0, 1, 2}, {0, 2, 3}, {0, 1, 2}, {0, 2, 3},
					{0, 1, 2}, {0, 2, 3}, {0, 1, 2}, {0, 2, 3}
				});
				setupHiddenFaces(block, i, 2-j, 0, 4, 3, 1);
				block.build();
				engine.addObject(block);
			}
		}

		Mesh extra = new Mesh(new Point3D[]{
			new Point3D(1, -1, 1), new Point3D(1, 0, 1), new Point3D(2, 0, 1), new Point3D(2, -1, 1),
			new Point3D(1, -1, 2), new Point3D(1, 0, 2), new Point3D(2, 0, 2), new Point3D(2, -1, 2)
		}, new int[][]{
			{0, 1, 2}, {0, 2, 3}, {3, 2, 6},
			{3, 6, 7}, {7, 6, 5}, {7, 5, 4},
			{4, 5, 1}, {4, 1, 0}, {1, 5, 6},
			{1, 6, 2}, {4, 0, 3}, {4, 3, 7}
		}, null, new Image[]{new Image(getClass().getResourceAsStream("/glass_red.png"))},
		new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, new Point2D[]{
			new Point2D(0, 0), new Point2D(0, 1), new Point2D(1, 1), new Point2D(1, 0)
		}, new int[][]{
			{0, 1, 2}, {0, 2, 3}, {0, 1, 2}, {0, 2, 3},
			{0, 1, 2}, {0, 2, 3}, {0, 1, 2}, {0, 2, 3},
			{0, 1, 2}, {0, 2, 3}, {0, 1, 2}, {0, 2, 3},
			{0, 1, 2}, {0, 2, 3}, {0, 1, 2}, {0, 2, 3}
		});
		extra.addHiddenFace(0);
		extra.addHiddenFace(1);
		extra.build();
		for (int j = 0; j < 12; j++) extra.getTriangles()[j].setImageTransparent(true);
		engine.addObject(extra);

		engine.getLights().add(new Light(1));

		engine.setOnKey(KeyCode.O, engine::toggleMouseMovement, true);
		engine.setOnUpdate(gc -> {
			gc.setFill(Color.BLACK);
			gc.setTextAlign(TextAlignment.RIGHT);
			gc.fillText("Vertices: "+MeshVertex.getViewVerticesCount(), WIDTH*0.95, HEIGHT*0.1);
		});
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
		loadedObject.setRotation(Math.PI/2, 0, 0, Point3D.ZERO);
		loadedObject.build();

		Light light = new Light(new Camera(new Point3D(-6, -6, -3), WIDTH, HEIGHT, Math.PI/4, 100, 0.3));

		engine.addObject(loadedObject);
		engine.getLights().add(light);

		engine.setOnUpdate(gc -> {
			gc.setFill(Color.BLACK);
			gc.setTextAlign(TextAlignment.RIGHT);
			gc.fillText("Vertices: "+MeshVertex.getViewVerticesCount(), WIDTH*0.95, HEIGHT*0.1);
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

					setupHiddenFaces(block, i, j, k, width, height, depth);

					//block.setShowAllFaces(true);
					block.build();
					engine.addObject(block);
				}
			}
		}

		Light light = new Light(1);
		engine.getLights().add(light);

		engine.setOnUpdate(gc -> {
			gc.setFill(Color.BLACK);
			gc.setTextAlign(TextAlignment.RIGHT);
			gc.fillText("Vertices: "+MeshVertex.getViewVerticesCount(), WIDTH*0.95, HEIGHT*0.1);
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
		loadedObject.setRotation(Math.PI/2, 0, 0, Point3D.ZERO);

		loader = null;
		try {
			loader = new MeshLoader(new File(getClass().getResource("/shadows.obj").toURI()));
			loader.setPosition(new Point3D(0, 0, 4));
			loader.setScale(0.5);
		} catch (Exception ex){
			ex.printStackTrace();
		}
		Mesh shadowObject = loader.load(false);
		shadowObject.setRotation(0, 0, Math.PI, Point3D.ZERO);

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

		object.build();
		object2.build();
		loadedObject.build();
		shadowObject.build();

		engine.addObject(object);
		engine.addObject(object2);
		engine.addObject(loadedObject);
		engine.addObject(shadowObject);

		engine.getLights().add(light);
	}

	private static void setupHiddenFaces(Mesh block, int x, int y, int z, int width, int height, int depth){
		if (x > 0){ // Left
			block.addHiddenFace(6);
			block.addHiddenFace(7);
		}
		if (x < width-1){ // Right
			block.addHiddenFace(2);
			block.addHiddenFace(3);
		}
		if (y > 0){ // Top
			block.addHiddenFace(10);
			block.addHiddenFace(11);
		}
		if (y < height-1){ // Bottom
			block.addHiddenFace(8);
			block.addHiddenFace(9);
		}
		if (z > 0){ // Front
			block.addHiddenFace(0);
			block.addHiddenFace(1);
		}
		if (z < depth-1){ // Back
			block.addHiddenFace(4);
			block.addHiddenFace(5);
		}
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
