package com.orangomango.rendering3d;

import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.canvas.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.layout.StackPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.event.EventHandler;
import javafx.scene.robot.Robot;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.scene.Cursor;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;

import java.util.*;
import java.util.function.Consumer;

import com.orangomango.rendering3d.model.*;

public class Engine3D{
	private int width, height;
	private Map<KeyCode, Boolean> keys = new HashMap<>();
	private EventHandler<MouseEvent> onMousePressed;
	private Robot robot;
	private Stage stage;
	private Color[][] canvas;
	private Camera camera;
	private List<Mesh> objects = new ArrayList<>();
	private List<Light> sceneLights = new ArrayList<>();
	private Map<KeyCode, Runnable> keyEvents = new HashMap<>();
	private Map<KeyCode, Boolean> keyEventsSingle = new HashMap<>();
	private int fps, frames;
	private Consumer<GraphicsContext> onUpdate;
	private boolean mouseMovement = true;

	// FLAGS
	public static boolean SHOW_LINES = false;
	public static boolean LIGHT_AVAILABLE = true;
	public static boolean FOLLOW_LIGHT = false;
	public static boolean SHADOWS = false;

	public Engine3D(Stage stage, int width, int height){
		this.width = width;
		this.height = height;
		this.stage = stage;
		this.canvas = new Color[this.width][this.height];

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

	public void setOnKey(KeyCode code, Runnable r, boolean singleClick){
		this.keyEvents.put(code, r);
		this.keyEventsSingle.put(code, singleClick);
	}

	public void setOnUpdate(Consumer<GraphicsContext> onUpdate){
		this.onUpdate = onUpdate;
	}

	public void toggleMouseMovement(){
		this.mouseMovement = !this.mouseMovement;
	}

	public void setOnMousePressed(EventHandler<MouseEvent> event){
		this.onMousePressed = event;
	}

	public void setCamera(Camera camera){
		this.camera = camera;
	}

	public List<Mesh> getObjects(){
		return this.objects;
	}

	public List<Light> getLights(){
		return this.sceneLights;
	}

	public Scene getScene(){
		StackPane pane = new StackPane();
		Canvas canvas = new Canvas(this.width, this.height);
		canvas.setFocusTraversable(true);
		canvas.setOnKeyPressed(e -> this.keys.put(e.getCode(), true));
		canvas.setOnKeyReleased(e -> this.keys.put(e.getCode(), false));

		if (this.onMousePressed != null){
			canvas.setOnMousePressed(this.onMousePressed);
		}

		GraphicsContext gc = canvas.getGraphicsContext2D();
		pane.getChildren().add(canvas);

		// The mouse will continuosly be put at the center of the stage
		// and a delta will be calculated for the camera movement
		this.robot = new Robot();
		Timeline mouse = new Timeline(new KeyFrame(Duration.millis(1000.0/15*2), e -> {
			if (this.stage.isFocused() && this.mouseMovement) this.robot.mouseMove(this.stage.getX()+this.width/2.0, this.stage.getY()+this.height/2.0);
		}));
		mouse.setCycleCount(Animation.INDEFINITE);
		mouse.play();

		AnimationTimer timer = new AnimationTimer(){
			@Override
			public void handle(long time){
				Engine3D.this.frames++;
				update(gc);
			}
		};
		timer.start();

		Scene scene = new Scene(pane, this.width, this.height);
		scene.setCursor(Cursor.NONE);

		return scene;
	}

	private void update(GraphicsContext gc){
		gc.clearRect(0, 0, this.width, this.height);
		for (int i = 0; i < this.width; i++){
			for (int j = 0; j < this.height; j++){
				this.canvas[i][j] = Color.CYAN;
			}
		}
		gc.setFill(Color.CYAN);
		gc.fillRect(0, 0, this.width, this.height);

		this.camera.clearDepthBuffer();
		
		if (SHADOWS){
			MeshVertex.VERTICES.clear();
			for (Light light : this.sceneLights){
				Camera lightCamera = light.getCamera();
				lightCamera.clearDepthBuffer();
				for (Mesh mesh : this.objects){
					mesh.update(lightCamera, null);
					mesh.render(null, null); // Update the light's depthbuffer
				}
			}
		}

		MeshVertex.VERTICES.clear();
		List<ProjectedTriangle> transparentTriangles = new ArrayList<>();
		for (Mesh mesh : this.objects){
			mesh.update(this.camera, this.sceneLights);
			transparentTriangles.addAll(mesh.render(this.canvas, SHOW_LINES ? gc : null));
		}

		// Render the transparent triangles
		transparentTriangles.sort((pt1, pt2) -> Double.compare(pt1.getMeanZ(), pt2.getMeanZ()));
		for (ProjectedTriangle pt : transparentTriangles){
			pt.render(canvas, SHOW_LINES ? gc : null);
		}

		// Render meshes
		if (!SHOW_LINES){
			for (int i = 0; i < this.width; i++){
				for (int j = 0; j < this.height; j++){
					Color color = this.canvas[i][j];
					if (color != null){
						gc.getPixelWriter().setColor(i, j, Color.color(color.getRed(), color.getGreen(), color.getBlue()));
					}
				}
			}
		}

		// Turn the camera according to the mouse
		if (this.mouseMovement){
			double sensibility = 0.4;
			Point2D mouse = this.robot.getMousePosition();
			Point2D center = new Point2D(this.stage.getX()+this.width/2.0, this.stage.getY()+this.height/2.0);
			this.camera.setRx(this.camera.getRx()+Math.toRadians((int)(center.getY()-mouse.getY())*sensibility));
			this.camera.setRy(this.camera.getRy()+Math.toRadians((int)(center.getX()-mouse.getX())*sensibility));
		}

		if (FOLLOW_LIGHT && this.sceneLights.size() >= 1){
			this.camera.setPosition(this.sceneLights.get(0).getCamera().getPosition());
			this.camera.setRx(this.sceneLights.get(0).getCamera().getRx());
			this.camera.setRy(this.sceneLights.get(0).getCamera().getRy());
		}

		// Exit application when the escape key is pressed
		if (this.keys.getOrDefault(KeyCode.ESCAPE, false)){
			System.exit(0);
		}

		for (KeyCode k : this.keyEvents.keySet()){
			if (this.keys.getOrDefault(k, false)){
				this.keyEvents.get(k).run();
				if (this.keyEventsSingle.get(k)) this.keys.put(k, false);
			}
		}

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
			if (this.sceneLights.size() >= 1){
				this.sceneLights.get(0).getCamera().setPosition(this.camera.getPosition());
				this.sceneLights.get(0).getCamera().setRx(this.camera.getRx());
				this.sceneLights.get(0).getCamera().setRy(this.camera.getRy());
				System.out.println("F4");
				this.keys.put(KeyCode.F4, false);
			}
		} else if (this.keys.getOrDefault(KeyCode.F5, false)){
			SHADOWS = !SHADOWS;
			System.out.println("F5");
			this.keys.put(KeyCode.F5, false);
		}

		// External update function
		if (this.onUpdate != null){
			gc.save();
			this.onUpdate.accept(gc);
			gc.restore();
		}

		// Info box
		gc.setFill(Color.BLACK);
		gc.save();
		gc.setGlobalAlpha(0.6);
		gc.fillRect(0.035*width, 0.025*height, 0.56*width, 0.13*height);
		gc.restore();
		gc.setFill(Color.WHITE);
		gc.setFont(new Font("sans-serif", 7));
		gc.fillText(this.camera+"\n"+String.format("FPS:%d", this.fps), 0.05*width, 0.075*height);
	}

	public static Color mixColors(Color color1, Color color2){
		double opacity = color1.getOpacity();
		double red = color1.getRed()*opacity+color2.getRed()*(1.0-opacity);
		double green = color1.getGreen()*opacity+color2.getGreen()*(1.0-opacity);
		double blue = color1.getBlue()*opacity+color2.getBlue()*(1.0-opacity);
		return Color.color(red, green, blue, (opacity+color2.getOpacity())/2);
	}

	public static <T> T swap(T a, T b){
		return a;
	}

	public static boolean isInScene(int x, int y, Camera camera){
		return x >= 0 && y >= 0 && x < camera.getWidth() && y < camera.getHeight();
	}

	public static double distanceToPlane(Point3D normal, Point3D planePoint, Point3D point, Point3D direction){
		if (normal.dotProduct(direction) == 0) throw new IllegalStateException("Debug: dp is 0");
		return (normal.dotProduct(planePoint)-normal.dotProduct(point))/normal.dotProduct(direction);
	}

	private static double[] revertPoint(double[] point, Camera cam1){
		double w = 1/point[2];
		double x = (point[0]*2/cam1.getWidth()-1)*(w == 0 ? 1 : w);
		double y = (point[1]*2/cam1.getHeight()-1)*(w == 0 ? 1 : w);
		
		x *= Math.tan(cam1.getFov()/2)/cam1.getAspectRatio();
		y *= Math.tan(cam1.getFov()/2);
		
		double[] rotation = multiply(getRotateX(cam1.getRx()), new double[]{x, y, w, 1});
		x = rotation[0];
		y = rotation[1];
		w = rotation[2];
		
		rotation = multiply(getRotateY(cam1.getRy()), new double[]{x, y, w, 1});
		x = rotation[0];
		y = rotation[1];
		w = rotation[2];

		double[] translation = multiply(getTranslation(cam1.getPosition().getX(), cam1.getPosition().getY(), cam1.getPosition().getZ()), new double[]{x, y, w, 1});
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
		
		out[0] = (out[0]+1)*0.5*cam2.getWidth();
		out[1] = (out[1]+1)*0.5*cam2.getHeight();
		
		return new double[]{out[0], out[1], 1/out[3]};
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
}
