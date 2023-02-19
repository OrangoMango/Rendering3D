package com.orangomango.rendering3d;

import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.canvas.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.scene.input.KeyCode;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;

import java.util.*;

import com.orangomango.rendering3d.model.Camera;
import com.orangomango.rendering3d.model.Mesh;
import com.orangomango.rendering3d.model.Light;

public class Engine3D{
	private int width, height;
	private static final int FPS = 6;
	private volatile int frames, fps;
	private Map<KeyCode, Boolean> keys = new HashMap<>();
	private double mouseX, mouseY, mouseOldX, mouseOldY;
	
	public static boolean SHOW_LINES = false, LIGHT_AVAILABLE = true, FOLLOW_LIGHT = false, LIGHT_ROTATION = false, SHADOWS = false;
	
	private List<Mesh> objects = new ArrayList<>();
	private List<Light> sceneLights = new ArrayList<>();
	private Camera camera;
	
	private static Engine3D instance = null;
	
	public Engine3D(int w, int h){
		if (instance == null){
			instance = this;
		} else {
			throw new IllegalStateException("Instance already created");
		}
		
		this.width = w;
		this.height = h;
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
	}
	
	public static Engine3D getInstance(){
		return instance;
	}
	
	public void setCamera(Camera camera){
		this.camera = camera;
	}
	
	public Scene getScene(){
		StackPane pane = new StackPane();
		Canvas canvas = new Canvas(width, height);
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
		
		Timeline loop = new Timeline(new KeyFrame(Duration.millis(1000.0/FPS), e -> update(gc)));
		loop.setCycleCount(Animation.INDEFINITE);
		loop.play();
		
		AnimationTimer timer = new AnimationTimer(){
			@Override
			public void handle(long time){
				Engine3D.this.frames++;
			}
		};
		timer.start();
		
		return new Scene(pane, width, height);
	}
	
	private void update(GraphicsContext gc){
		gc.clearRect(0, 0, width, height);
		gc.setFill(Color.CYAN);
		gc.fillRect(0, 0, width, height);
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
			System.out.println("F1");
			this.keys.put(KeyCode.F1, false);
		} else if (this.keys.getOrDefault(KeyCode.F2, false)){
			FOLLOW_LIGHT = !FOLLOW_LIGHT;
			System.out.println("F2");
			this.keys.put(KeyCode.F2, false);
		} else if (this.keys.getOrDefault(KeyCode.F3, false)){
			LIGHT_AVAILABLE = !LIGHT_AVAILABLE;
			System.out.println("F3");
			this.keys.put(KeyCode.F3, false);
		} else if (this.keys.getOrDefault(KeyCode.F4, false)){
			LIGHT_ROTATION = !LIGHT_ROTATION;
			System.out.println("F4");
			this.keys.put(KeyCode.F4, false);
		} else if (this.keys.getOrDefault(KeyCode.F5, false)){
			LIGHT_ROTATION = false;
			sceneLights.get(0).setPos(this.camera.getX(), this.camera.getY(), this.camera.getZ());
			sceneLights.get(0).setRy(this.camera.getRy());
			System.out.println("F5");
			this.keys.put(KeyCode.F5, false);
		} else if (this.keys.getOrDefault(KeyCode.F6, false)){
			SHADOWS = !SHADOWS;
			System.out.println("F6");
			this.keys.put(KeyCode.F6, false);
		} else if (this.keys.getOrDefault(KeyCode.R, false)){
			this.camera.reset();
			this.keys.put(KeyCode.R, false);
		} else if (this.keys.getOrDefault(KeyCode.L, false)){
			this.camera.lookAtCenter();
			System.out.println("L");
			this.keys.put(KeyCode.L, false);
		}

		if (SHADOWS){
			for (Light light : sceneLights){
				Camera lightCamera = light.getCamera();
				lightCamera.clearDepthBuffer();
				for (Mesh object : objects){
					if (lightCamera.stateChanged) object.cache.remove(lightCamera);
					object.showLines = false;
					object.evaluate(lightCamera);
					object.render(lightCamera, null, null);
				}
			}
		}

		for (Mesh object : objects){
			if (this.camera.stateChanged) object.cache.remove(this.camera);
			object.showLines = SHOW_LINES;
			object.evaluate(this.camera);
			object.render(this.camera, sceneLights, gc);
		}
		
		double lspeed = 5;
		if (LIGHT_ROTATION){
			for (Light light : sceneLights){
				double[] rotationV = multiply(getRotateY(0.01*40/FPS), new double[]{light.getPosition().getX(), light.getPosition().getY(), light.getPosition().getZ()});
				light.setPos(rotationV[0], rotationV[1], rotationV[2]);
				light.lookAtCenter();
			}
		}
		if (FOLLOW_LIGHT){
			this.camera.setPos(sceneLights.get(0).getPosition());
			this.camera.lookAtCenter();
		}
		
		gc.setFill(Color.BLACK);
		gc.setFont(new Font("sans-serif", 9));
		gc.fillText(this.camera.toString()+"\n"+String.format("FPS:%d (%d)\nLight: %s", this.fps, FPS, sceneLights.get(0).getPosition()), 0.05*width, 0.05*height);
	}
	
	public static double[] convertPoint(double[] point, Camera cam1, Camera cam2){
		double w = 1/point[2];
		double x = (point[0]*2/getInstance().getWidth()-1)*(w == 0 ? 1 : w);
		double y = (point[1]*2/getInstance().getHeight()-1)*(w == 0 ? 1 : w);
		
		x *= Math.tan(cam1.fov/2)/cam1.aspectRatio;
		y *= Math.tan(cam1.fov/2);
		
		double[] rotation = multiply(getRotateY(-cam1.getRy()), new double[]{x, y, w, 1});
		x = rotation[0];
		y = rotation[1];
		w = rotation[2];
		
		rotation = multiply(getRotateX(-cam1.getRx()), new double[]{x, y, w, 1});
		x = rotation[0];
		y = rotation[1];
		w = rotation[2];

		double[] translation = multiply(getTranslation(cam1.getX(), cam1.getY(), cam1.getZ()), new double[]{x, y, w, 1});
		x = translation[0];
		y = translation[1];
		w = translation[2];
		
		double[] out = multiply(cam2.getCompleteMatrix(), new double[]{x, y, w, 1});
		out[0] /= out[3] == 0 ? 1 : out[3];
		out[1] /= out[3] == 0 ? 1 : out[3];
		
		out[0] = (out[0]+1)*0.5*getInstance().getWidth();
		out[1] = (out[1]+1)*0.5*getInstance().getHeight();
		
		return new double[]{out[0], out[1], 1/out[3]};
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
	
	public static boolean isInScene(int x, int y){
		return x >= 0 && y >= 0 && x < getInstance().getWidth() && y < getInstance().getHeight();
	}
	
	public List<Mesh> getObjects(){
		return this.objects;
	}
	
	public List<Light> getLights(){
		return this.sceneLights;
	}
	
	public int getWidth(){
		return this.width;
	}
	
	public int getHeight(){
		return this.height;
	}
	
	public int getFPS(){
		return this.fps;
	}
}
