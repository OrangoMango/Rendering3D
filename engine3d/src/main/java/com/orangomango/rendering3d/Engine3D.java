package com.orangomango.rendering3d;

import javafx.beans.property.SimpleStringProperty;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.canvas.*;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.robot.Robot;

import java.util.*;
import java.util.function.Consumer;

import com.orangomango.rendering3d.model.Camera;
import com.orangomango.rendering3d.model.Mesh;
import com.orangomango.rendering3d.model.MeshGroup;
import com.orangomango.rendering3d.model.Light;

public class Engine3D{
	private int width, height;
	private volatile int frames, fps;
	private Map<KeyCode, Boolean> keys = new HashMap<>();
	private Robot robot;
	private Stage stage;
	private Map<KeyCode, Runnable> keyEvents = new HashMap<>();
	private Map<KeyCode, Boolean> keyEventsSingle = new HashMap<>();
	private Consumer<GraphicsContext> onUpdate;
	private EventHandler<MouseEvent> onMousePressed;
	private boolean mouseMovement = true;
	private Scene scene;
	public SimpleStringProperty extraText = new SimpleStringProperty();
	private int renderedMeshes = 0; // This variable is used only for performance information
	
	private static Image POINTER = new Image(Engine3D.class.getResourceAsStream("/pointer.png"));
	
	public static boolean SHOW_LINES = false;
	public static boolean LIGHT_AVAILABLE = true;
	public static boolean FOLLOW_LIGHT = false;
	public static boolean LIGHT_ROTATION = false;
	public static boolean SHADOWS = false;
	public static boolean SHOW_POINTER = true;
	public static boolean DEBUG = true;
	
	private List<MeshGroup> objects = new ArrayList<>();
	private List<Light> sceneLights = new ArrayList<>();
	private Camera camera;
	
	private static Engine3D instance = null;
	public static Color[][] canvas;
	
	public Engine3D(Stage stage, int w, int h){
		if (instance == null){
			instance = this;
		} else {
			throw new IllegalStateException("Instance already created");
		}
		
		this.width = w;
		this.height = h;
		this.stage = stage;
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
		canvas = new Color[this.width][this.height];
	}

	public void setOnKey(KeyCode code, Runnable r, boolean singleClick){
		this.keyEvents.put(code, r);
		this.keyEventsSingle.put(code, singleClick);
	}

	public void setOnUpdate(Consumer<GraphicsContext> cons){
		this.onUpdate = cons;
	}

	public void setOnMousePressed(EventHandler<MouseEvent> event){
		this.onMousePressed = event;
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
		if (this.onMousePressed != null){
			canvas.setOnMousePressed(this.onMousePressed);
			//canvas.setOnMouseDragged(this.onMousePressed);
		}
		GraphicsContext gc = canvas.getGraphicsContext2D();
		pane.getChildren().add(canvas);
		
		this.robot = new Robot();
		
		Timeline mouse = new Timeline(new KeyFrame(Duration.millis(1000.0/15*2), e -> {
			if (this.stage.isFocused() && this.mouseMovement) this.robot.mouseMove(this.stage.getX()+this.width/2.0, this.stage.getY()+this.height/2.0);
		}));
		mouse.setCycleCount(Animation.INDEFINITE);
		mouse.play();
		
		/*Timeline loop = new Timeline(new KeyFrame(Duration.millis(1000.0/6), e -> update(gc)));
		loop.setCycleCount(Animation.INDEFINITE);
		loop.play();*/
		
		AnimationTimer timer = new AnimationTimer(){
			@Override
			public void handle(long time){
				Engine3D.this.frames++;
				update(gc);
			}
		};
		timer.start();
		
		scene = new Scene(pane, width, height);
		scene.setCursor(Cursor.NONE);
		
		return scene;
	}
	
	private void update(GraphicsContext gc){
		// Clear canvas
		gc.clearRect(0, 0, width, height);
		for (int i = 0; i < this.width; i++){
			for (int j = 0; j < this.height; j++){
				this.canvas[i][j] = null;
			}
		}
		gc.setFill(Color.CYAN);
		gc.fillRect(0, 0, width, height);
		this.camera.clearDepthBuffer();
		this.renderedMeshes = 0;
		
		if (this.keys.getOrDefault(KeyCode.ESCAPE, false)){
			System.exit(0);
		}

		for (KeyCode k : this.keyEvents.keySet()){
			if (this.keys.getOrDefault(k, false)){
				this.keyEvents.get(k).run();
				if (this.keyEventsSingle.get(k)) this.keys.put(k, false);
			}
		}
		
		if (DEBUG){
			if (this.keys.getOrDefault(KeyCode.F1, false)){
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
				sceneLights.get(0).setRx(this.camera.getRx());
				sceneLights.get(0).setRy(this.camera.getRy());
				System.out.println("F5");
				this.keys.put(KeyCode.F5, false);
			} else if (this.keys.getOrDefault(KeyCode.F6, false)){
				SHADOWS = !SHADOWS;
				System.out.println("F6");
				this.keys.put(KeyCode.F6, false);
			}
		}

		if (SHADOWS){
			for (Light light : sceneLights){
				Camera lightCamera = light.getCamera();
				lightCamera.clearDepthBuffer();
				boolean stateChanged = lightCamera.stateChanged;
				for (MeshGroup mg : this.objects){
					for (Mesh object : mg.getMeshes()){
						if (stateChanged) object.cache.remove(lightCamera);
						object.evaluate(lightCamera);
						object.render(lightCamera, null, null);
					}
				}
			}
		}

		if (this.mouseMovement){
			double sensibility = 0.4;
			Point2D mouse = this.robot.getMousePosition();
			Point2D center = new Point2D(this.stage.getX()+this.width/2.0, this.stage.getY()+this.height/2.0);
			this.camera.setRx(this.camera.getRx()+Math.toRadians((int)(center.getY()-mouse.getY())*sensibility));
			this.camera.setRy(this.camera.getRy()+Math.toRadians((int)(center.getX()-mouse.getX())*sensibility));
		}

		boolean stateChanged = this.camera.stateChanged;
		for (MeshGroup mg : this.objects){
			this.renderedMeshes++;
			for (Mesh object : mg.getMeshes()){
				if (object.skipCondition != null && object.skipCondition.test(this.camera)) continue;
				if (stateChanged) object.cache.remove(this.camera);
				object.setShowLines(SHOW_LINES);
				object.evaluate(this.camera);
				object.render(this.camera, sceneLights, gc);
			}
		}
		
		for (int i = 0; i < this.width; i++){
			for (int j = 0; j < this.height; j++){
				Color color = this.canvas[i][j];
				if (color != null){
					gc.getPixelWriter().setColor(i, j, color);
				}
			}
		}
		
		if (LIGHT_ROTATION){
			for (Light light : sceneLights){
				double[] rotationV = multiply(getRotateY(0.01*40/6), new double[]{light.getPosition().getX(), light.getPosition().getY(), light.getPosition().getZ()});
				light.setPos(rotationV[0], rotationV[1], rotationV[2]);
				light.lookAtCenter();
			}
		}
		if (FOLLOW_LIGHT){
			this.camera.setPos(sceneLights.get(0).getPosition());
			this.camera.lookAtCenter();
		}
		
		if (SHOW_POINTER){
			double cursorSize = 26*this.camera.aspectRatio;
			gc.drawImage(POINTER, this.width/2.0-cursorSize/2, this.height/2.0-cursorSize/2, cursorSize, cursorSize);
		}

		if (this.onUpdate != null){
			this.onUpdate.accept(gc);
		}

		gc.setFill(Color.BLACK);
		gc.save();
		gc.setGlobalAlpha(0.6);
		gc.fillRect(0.035*width, 0.025*height, 0.45*width, 0.2*height);
		gc.restore();
		gc.setFill(Color.WHITE);
		gc.setFont(new Font("sans-serif", 13));
		gc.fillText(this.camera.toString()+"\n"+String.format("FPS:%d\n%s", this.fps, this.extraText.get()), 0.05*width, 0.075*height);
	}

	public int getRenderedMeshes(){
		return this.renderedMeshes;
	}

	public void toggleMouseMovement(){
		this.mouseMovement = !this.mouseMovement;
		scene.setCursor(this.mouseMovement ? Cursor.NONE : Cursor.DEFAULT);
	}
	
	public static double[] revertPoint(double[] point, Camera cam1){
		double w = 1/point[2];
		double x = (point[0]*2/getInstance().getWidth()-1)*(w == 0 ? 1 : w);
		double y = (point[1]*2/getInstance().getHeight()-1)*(w == 0 ? 1 : w);
		
		x *= Math.tan(cam1.fov/2)/cam1.aspectRatio;
		y *= Math.tan(cam1.fov/2);
		
		double[] rotation = multiply(getRotateX(cam1.getRx()), new double[]{x, y, w, 1});
		x = rotation[0];
		y = rotation[1];
		w = rotation[2];
		
		rotation = multiply(getRotateY(cam1.getRy()), new double[]{x, y, w, 1});
		x = rotation[0];
		y = rotation[1];
		w = rotation[2];

		double[] translation = multiply(getTranslation(cam1.getX(), cam1.getY(), cam1.getZ()), new double[]{x, y, w, 1});
		x = translation[0];
		y = translation[1];
		w = translation[2];
		
		return new double[]{x, y, w};
	}
	
	public static double[] convertPoint(double[] point, Camera cam1, Camera cam2){
		double[] reverted = revertPoint(point, cam1);
		double x = reverted[0];
		double y = reverted[1];
		double w = reverted[2];
		
		double[] out = multiply(multiply(cam2.getViewMatrix(), cam2.getProjectionMatrix()), new double[]{x, y, w, 1});
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
			{Math.cos(angle), 0, -Math.sin(angle), 0},
			{0, 1, 0, 0},
			{Math.sin(angle), 0, Math.cos(angle), 0},
			{0, 0, 0, 1}
		};
	}
	
	public static double[][] getRotateZ(double angle){
		return new double[][]{
			{Math.cos(angle), Math.sin(angle), 0, 0},
			{-Math.sin(angle), Math.cos(angle), 0, 0},
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
	
	public static boolean isOutside(double p, double bound){
		return p > bound || p < -bound;
	}
	
	public List<MeshGroup> getObjects(){
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
}
