package com.orangomango.rendering3d;

import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.canvas.*;
import javafx.scene.paint.Color;
import javafx.scene.layout.StackPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.event.EventHandler;
import javafx.scene.robot.Robot;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.scene.Cursor;
import javafx.geometry.Point2D;

import java.util.*;

import com.orangomango.rendering3d.model.Camera;
import com.orangomango.rendering3d.model.Mesh;

public class Engine3D{
	private int width, height;
	private Map<KeyCode, Boolean> keys = new HashMap<>();
	private EventHandler<MouseEvent> onMousePressed;
	private Robot robot;
	private Stage stage;
	private Color[][] canvas;
	private Camera camera;
	private List<Mesh> objects = new ArrayList<>();

	public Engine3D(Stage stage, int width, int height){
		this.width = width;
		this.height = height;
		this.stage = stage;
		this.canvas = new Color[this.width][this.height];
	}

	public void setOnMousePressed(EventHandler<MouseEvent> event){
		this.onMousePressed = event;
	}

	public void setCamera(Camera camera){
		this.camera = camera;
	}

	public void addObject(Mesh mesh){
		this.objects.add(mesh);
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
			if (this.stage.isFocused()) this.robot.mouseMove(this.stage.getX()+this.width/2.0, this.stage.getY()+this.height/2.0);
		}));
		mouse.setCycleCount(Animation.INDEFINITE);
		mouse.play();

		AnimationTimer timer = new AnimationTimer(){
			@Override
			public void handle(long time){
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
		gc.fillRect(0, 0, width, height);

		for (Mesh mesh : this.objects){
			mesh.update(this.camera);
			mesh.render(this.canvas);
		}

		// Render meshes
		for (int i = 0; i < this.width; i++){
			for (int j = 0; j < this.height; j++){
				Color color = this.canvas[i][j];
				if (color != null){
					gc.getPixelWriter().setColor(i, j, color);
				}
			}
		}

		// Turn camera according to the mouse
		double sensibility = 0.4;
		Point2D mouse = this.robot.getMousePosition();
		Point2D center = new Point2D(this.stage.getX()+this.width/2.0, this.stage.getY()+this.height/2.0);
		this.camera.setRx(this.camera.getRx()+Math.toRadians((int)(center.getY()-mouse.getY())*sensibility));
		this.camera.setRy(this.camera.getRy()+Math.toRadians((int)(center.getX()-mouse.getX())*sensibility));

		// Exit application when the escape key is pressed
		if (this.keys.getOrDefault(KeyCode.ESCAPE, false)){
			System.exit(0);
		}
	}

	public static Color mixColors(Color color1, Color color2){
		double alpha = color1.getOpacity();
		double red = color1.getRed()*alpha+color2.getRed()*(1.0-alpha);
		double green = color1.getGreen()*alpha+color2.getGreen()*(1.0-alpha);
		double blue = color1.getBlue()*alpha+color2.getBlue()*(1.0-alpha);
		return Color.color(red, green, blue);
	}

	public static <T> T swap(T a, T b){
		return a;
	}

	public static boolean isInScene(int x, int y, Camera camera){
		return x >= 0 && y >= 0 && x < camera.getWidth() && y < camera.getHeight();
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
