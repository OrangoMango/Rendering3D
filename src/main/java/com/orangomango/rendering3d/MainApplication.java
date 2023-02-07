package com.orangomango.rendering3d;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.canvas.*;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.scene.input.KeyCode;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;

import java.util.*;
import java.io.File;

import com.orangomango.rendering3d.model.Camera;
import com.orangomango.rendering3d.model.Mesh;
import com.orangomango.rendering3d.model.Light;

public class MainApplication extends Application{
	public static final int WIDTH = 600;
	public static final int HEIGHT = 600;
	private Map<KeyCode, Boolean> keys = new HashMap<>();
	private volatile int frames, fps;
	private static final int FPS = 5;
	//public static Light LIGHT = new Light(-5, -3, -8);
	public static Light LIGHT = new Light(0, 0, -35);
	
	public static boolean SHOW_LINES = false, LIGHT_AVAILABLE = true, FOLLOW_LIGHT = false, LIGHT_ROTATION = false;
	
	private static final Image COAL_IMAGE = new Image(MainApplication.class.getResourceAsStream("/coal.png"));
	private static final Image DIRT_IMAGE = new Image(MainApplication.class.getResourceAsStream("/dirt.png"));
	private static final Image STONE_IMAGE = new Image(MainApplication.class.getResourceAsStream("/stone.png"));
	
	private double mouseX, mouseY, mouseOldX, mouseOldY;
	private Camera camera;
	private List<Mesh> objects = new ArrayList<>();
	
	@Override
	public void start(Stage stage){
		Thread counter = new Thread(() -> {
			while (true){
				try {
					this.fps = this.frames;
					this.frames = 0;
					Thread.sleep(1000);
				} catch (InterruptedException ex){
					ex.printStackTrace();
				}
			}
		});
		counter.setDaemon(true);
		counter.start();
		
		stage.setTitle("3D Graphics");
		StackPane pane = new StackPane();
		Canvas canvas = new Canvas(WIDTH, HEIGHT);
		canvas.setFocusTraversable(true);
		canvas.setOnKeyPressed(e -> this.keys.put(e.getCode(), true));
		canvas.setOnKeyReleased(e -> this.keys.put(e.getCode(), false));
		canvas.setOnMousePressed(e -> {
			this.mouseOldX = e.getX();
			this.mouseOldY = e.getY();
		});
		canvas.setOnMouseDragged(e -> {
			switch (e.getButton()){
				case PRIMARY:
					this.camera.setRy(this.camera.getRy()+Math.toRadians(e.getX()-mouseOldX));
					break;
				case SECONDARY:
					this.camera.setRx(this.camera.getRx()+Math.toRadians(mouseOldY-e.getY()));
					break;
			}
			this.mouseOldX = e.getX();
			this.mouseOldY = e.getY();
		});
		GraphicsContext gc = canvas.getGraphicsContext2D();
		pane.getChildren().add(canvas);
		
		this.camera = new Camera(0, 0, 0);
		
		/*Random random = new Random();
		for (int i = 0; i < 1; i++){
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
			//Mesh model = Mesh.loadFromFile(this.camera, new File(MainApplication.class.getResource("/model.obj").toURI()), 0, 0, 0, 0.05);
			//model.setRotation(Math.PI/2, 0, 0);
			//objects.add(model);
			objects.add(Mesh.loadFromFile(new File(MainApplication.class.getResource("/plane2.obj").toURI()), 0, 0.5, 0, 0.5));
		} catch (Exception ex){
			ex.printStackTrace();
		}
		
		Timeline loop = new Timeline(new KeyFrame(Duration.millis(1000.0/FPS), e -> update(gc)));
		loop.setCycleCount(Animation.INDEFINITE);
		loop.play();
		
		AnimationTimer timer = new AnimationTimer(){
			@Override
			public void handle(long time){
				MainApplication.this.frames++;
			}
		};
		timer.start();
		
		stage.setScene(new Scene(pane, WIDTH, HEIGHT));
		stage.show();
	}
	
	private void update(GraphicsContext gc){
		gc.clearRect(0, 0, WIDTH, HEIGHT);
		gc.setFill(Color.BLACK);
		gc.fillRect(0, 0, WIDTH, HEIGHT);
		this.camera.clearDepthBuffer();
		
		double speed = 0.3;
		double ry = this.camera.getRy();
		if (this.keys.getOrDefault(KeyCode.W, false)){
			this.camera.move(speed*Math.cos(ry+Math.PI/2), 0, speed*Math.sin(ry+Math.PI/2));
			this.keys.put(KeyCode.W, false);
		} else if (this.keys.getOrDefault(KeyCode.A, false)){
			this.camera.move(speed*Math.cos(ry+Math.PI), 0, speed*Math.sin(ry+Math.PI));
			this.keys.put(KeyCode.A, false);
		} else if (this.keys.getOrDefault(KeyCode.S, false)){
			this.camera.move(-speed*Math.cos(ry+Math.PI/2), 0, -speed*Math.sin(ry+Math.PI/2));
			this.keys.put(KeyCode.S, false);
		} else if (this.keys.getOrDefault(KeyCode.D, false)){
			this.camera.move(-speed*Math.cos(ry+Math.PI), 0, -speed*Math.sin(ry+Math.PI));
			this.keys.put(KeyCode.D, false);
		} else if (this.keys.getOrDefault(KeyCode.SPACE, false)){
			this.camera.move(0, -speed, 0);
			this.keys.put(KeyCode.SPACE, false);
		} else if (this.keys.getOrDefault(KeyCode.Z, false)){
			this.camera.move(0, speed, 0);
			this.keys.put(KeyCode.Z, false);
		} else if (this.keys.getOrDefault(KeyCode.F1, false)){
			SHOW_LINES = !SHOW_LINES;
			this.keys.put(KeyCode.F1, false);
		} else if (this.keys.getOrDefault(KeyCode.F2, false)){
			FOLLOW_LIGHT = !FOLLOW_LIGHT;
			this.keys.put(KeyCode.F2, false);
		} else if (this.keys.getOrDefault(KeyCode.F3, false)){
			LIGHT_AVAILABLE = !LIGHT_AVAILABLE;
			this.keys.put(KeyCode.F3, false);
		} else if (this.keys.getOrDefault(KeyCode.F4, false)){
			LIGHT_ROTATION = !LIGHT_ROTATION;
			this.keys.put(KeyCode.F4, false);
		} else if (this.keys.getOrDefault(KeyCode.R, false)){
			this.camera.reset();
			this.keys.put(KeyCode.R, true);
		}
		
		Camera test = new Camera(LIGHT.getPosition().getX(), LIGHT.getPosition().getY(), LIGHT.getPosition().getZ());
		test.setRy(Math.atan2(test.getZ(), test.getX())+Math.PI/2);
		test.clearDepthBuffer();
		for (Mesh object : objects){
			object.showLines = false;
			object.evaluate(test);
			object.render(test, null, null);
			
			object.showLines = SHOW_LINES;
			object.evaluate(this.camera);
			object.render(this.camera, gc, test);
		}
		
		double lspeed = 5;
		if (LIGHT_ROTATION){
			double[] rotationV = multiply(getRotateY(0.01*40/FPS), new double[]{LIGHT.getPosition().getX(), LIGHT.getPosition().getY(), LIGHT.getPosition().getZ()});
			LIGHT = new Light(rotationV[0], rotationV[1], rotationV[2]);
		}
		if (FOLLOW_LIGHT){
			this.camera.setPos(LIGHT.getPosition());
			this.camera.setRy(Math.atan2(this.camera.getZ(), this.camera.getX())+Math.PI/2);
		}
		//LIGHT = new Light(this.camera.getPosition());
		
		gc.setFill(Color.WHITE);
		gc.fillText(this.camera.toString()+"\n"+String.format("FPS:%d (%d)\nLight: %s", this.fps, FPS, LIGHT.getPosition()), 30, 30);
	}
	
	public static <T> T swap(T a, T b){
		return a;
	}
	
	public static double[][] getRotateX(double angle){
		return new double[][]{
			{1, 0, 0, 0},
			{0, Math.cos(angle), -Math.sin(angle), 0},
			{0, Math.sin(angle), Math.cos(angle), 0},
			{0, 0, 0, 1}
		};
	}
	
	public static double[][] getRotateY(double angle){
		return new double[][]{
			{Math.cos(angle), 0, Math.sin(angle), 0},
			{0, 1, 0, 0},
			{-Math.sin(angle), 0, Math.cos(angle), 0},
			{0, 0, 0, 1}
		};
	}
	
	public static double[][] getRotateZ(double angle){
		return new double[][]{
			{Math.cos(angle), -Math.sin(angle), 0, 0},
			{Math.sin(angle), Math.cos(angle), 0, 0},
			{0, 0, 1, 0},
			{0, 0, 0, 1}
		};
	}
	
	public static double[][] getTranslation(double tx, double ty, double tz){
		return new double[][]{
			{1, 0, 0, tx},
			{0, 1, 0, ty},
			{0, 0, 1, tz},
			{0, 0, 0, 1}
		};
	}
	
	public static double[][] getScale(double sx, double sy, double sz){
		return new double[][]{
			{sx, 0, 0, 0},
			{0, sy, 0, 0},
			{0, 0, sz, 0},
			{0, 0, 0, 1}
		};
	}
	
	public static double[] multiply(double[][] mat, double[] vect){
		double[] out = new double[mat.length];
		
		for (int i = 0; i < out.length; i++){
			double sum = 0;
			for (int j = 0; j < vect.length; j++){
				sum += mat[i][j]*vect[j];
			}
			out[i] = sum;
		}

		return out;
	}
	
	public static double[][] multiply(double[][] mat1, double[][] mat2){
		double[][] out = new double[mat2.length][mat1[0].length];
		
		for (int i = 0; i < mat1[0].length; i++){
			for (int j = 0; j < mat2.length; j++){
				double sum = 0;
				for (int k = 0; k < mat1.length; k++){
					sum += mat1[k][i]*mat2[j][k];
				}
				out[j][i] = sum;
			}
		}

		return out;
	}
	
	/*private static List<Point3D> getPoints(Camera camera){
		double[][] depthBuffer = camera.depthBuffer;
		List<Point3D> points = new ArrayList<>();
		for (int i = 0; i < depthBuffer.length; i++){
			for (int j = 0; j < depthBuffer[i].length; j++){
				double w = 1/depthBuffer[i][j];
				double x = i*w;
				double y = j*w;
				x = x*Math.tan(camera.fov/2)/camera.aspectRatio;
				y = y*Math.tan(camera.fov/2);
				points.add(new Point3D(x, y, w));
			}
		}
		return points;
	}
	
	private static double[][] convertDepthBuffer(Camera cam1, Camera cam2){
		List<Point3D> p1 = getPoints(cam1);
		double[][] depthBuffer = new double[cam2.depthBuffer.length][cam2.depthBuffer[0].length];
		for (Point3D p : p1){
			double[] out = multiply(cam2.getProjectionMatrix(), new double[]{p.getX(), p.getY(), p.getZ()});
			depthBuffer[(int)Math.round(out[0])][(int)Math.round(out[1])] = 1/out[3];
		}
		return depthBuffer;
	}*/
	
	public static double[] convertPoint(double[] point, Camera cam1, Camera cam2){
		double w = 1/point[2];
		double x = (point[0]*2/WIDTH-1)*w*Math.tan(cam1.fov/2)/cam1.aspectRatio;
		double y = (point[1]*2/HEIGHT-1)*w*Math.tan(cam1.fov/2);
		//System.out.println("Inte: "+w+" "+x+" "+y);
		double[] out = multiply(cam2.getProjectionMatrix(true), new double[]{x, y, w});
		return new double[]{out[0]/out[3], out[1]/out[3], out[3]};
	}
	
	public static void main(String[] args){
		/*Camera cam1 = new Camera(0, 0, 0);
		Camera cam2 = new Camera(1, 0, 1);
		cam2.setRy(-Math.PI/2);
		
		Point3D a = new Point3D(0, 0, 1);
		System.out.println(a);
		
		double[] conv = multiply(cam1.getProjectionMatrix(true), new double[]{a.getX(), a.getY(), a.getZ(), 1});
		System.out.println(Arrays.toString(conv));
		
		double x = (conv[0]/conv[3]+1)*0.5*WIDTH;
		double y = (conv[1]/conv[3]+1)*0.5*HEIGHT;
		double w = 1/conv[3];
		
		System.out.println(x+" "+y+" "+w);
		
		double[] np = convertPoint(new double[]{x, y, w}, cam1, cam2);
		System.out.println(Arrays.toString(np));
		
		System.out.println("---------------");
		
		Point3D b = new Point3D(np[0], np[1], np[2]);
		System.out.println(b);

		//conv = multiply(cam2.getProjectionMatrix(true), new double[]{b.getX(), b.getY(), b.getZ(), 1});
		//System.out.println(Arrays.toString(conv));
		
		x = (conv[0]/conv[3]+1)*0.5*WIDTH;
		y = (conv[1]/conv[3]+1)*0.5*HEIGHT;
		w = 1/conv[3];
		System.out.println(x+" "+y+" "+w);
		
		np = convertPoint(new double[]{x, y, w}, cam2, cam1);
		System.out.println(Arrays.toString(np));
		
		System.exit(0);*/
		
		System.out.println("F1 -> SHOW_LINES");
		System.out.println("F2 -> FOLLOW_LIGHT");
		System.out.println("F3 -> LIGHT_AVAILABLE");
		System.out.println("F4 -> ROTATE_LIGHT");
		launch(args);
	}
}
