import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.canvas.*;
import javafx.scene.paint.Color;
import javafx.scene.image.*;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.geometry.Point3D;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;

import java.util.*;
import java.io.*;

public class Rendering3D extends Application{
	private static final double WIDTH = 600;
	private static final double HEIGHT = 600;
	private Map<KeyCode, Boolean> keys = new HashMap<>();
	private volatile int frames, fps;
	private static final int FPS = 40;
	
	private static double aspectRatio = HEIGHT/WIDTH;
	private static double fov = Math.toRadians(45);
	private static double zFar = 50;
	private static double zNear = 1;
	private static final double[][] PROJECTION_MATRIX = {
		{aspectRatio*1/Math.tan(fov/2), 0, 0, 0},
		{0, 1/Math.tan(fov/2), 0, 0},
		{0, 0, 2/(zFar-zNear), -2*zNear/(zFar-zNear)-1},
		{0, 0, 1, 0}
	};
	
	private List<Cube> cubes = new ArrayList<>();
	private static double cx, cy, cz, rx, ry;
	private double mouseX, mouseY, mouseOldX, mouseOldY;
	private static final Image IMAGE = new Image(Rendering3D.class.getResourceAsStream("dirt.png"));
	
	private static class Cube {
		private Point3D[] points;
		private Point2D[][] projected;
		private int[][] faces;
		private Color color;
		private double angle;
		private Point2D[] textureVertex;
		private int[][] textureFaces;
		
		public Cube(Point3D[] points, int[][] faces, Point2D[] textureCoords, int[][] vertexFaces){
			this.color = Color.WHITE; //Color.color(Math.random(), Math.random(), Math.random());
			this.points = points;
			this.projected = new Point2D[faces.length][3]; // TO FIX
			this.faces = faces;
			this.textureVertex = textureCoords;
			this.textureFaces = vertexFaces;
		}
		
		public Point3D[] getPoints(){
			return this.points;
		}
		
		private Point3D[][] getTrianglePoints(){
			Point3D[][] output = new Point3D[this.faces.length][3];
			for (int i = 0; i < output.length; i++){
				Point3D[] tr = new Point3D[3];
				tr[0] = getPoints()[this.faces[i][0]];
				tr[1] = getPoints()[this.faces[i][1]];
				tr[2] = getPoints()[this.faces[i][2]];
				output[i] = tr;
			}
			return output;
		}
		
		private void setProjectedPoint(int f, int tr, Point2D point){
			this.projected[f][tr] = point;
		}
		
		private void evaluate(){
			int i = 0;
			for (Point3D[] points : getTrianglePoints()){				
				double[][] cam = multiply(multiply(getTranslation(-cx, -cy, -cz), multiply(getRotateX(rx), getRotateY(ry))), PROJECTION_MATRIX);
				
				// Apply transforms
				double[] p1 = new double[]{points[0].getX(), points[0].getY(), points[0].getZ(), 1};
				double[] p2 = new double[]{points[1].getX(), points[1].getY(), points[1].getZ(), 1};
				double[] p3 = new double[]{points[2].getX(), points[2].getY(), points[2].getZ(), 1};
				
				// Rotate
				p1 = multiply(getRotateX(this.angle), p1);
				p2 = multiply(getRotateX(this.angle), p2);
				p3 = multiply(getRotateX(this.angle), p3);
				p1 = multiply(getRotateY(this.angle), p1);
				p2 = multiply(getRotateY(this.angle), p2);
				p3 = multiply(getRotateY(this.angle), p3);
				
				// Translate
				p1 = multiply(getTranslation(0, 0, 4), p1);
				p2 = multiply(getTranslation(0, 0, 4), p2);
				p3 = multiply(getTranslation(0, 0, 4), p3);
				
				Point3D point1 = new Point3D(p1[0], p1[1], p1[2]);
				Point3D point2 = new Point3D(p2[0], p2[1], p2[2]);
				Point3D point3 = new Point3D(p3[0], p3[1], p3[2]);
				/*Point3D normal = getNormal(point2.subtract(point1), point3.subtract(point1));
				normal.multiply(1/normal.magnitude());
				double dot = dotProduct(normal, point1.subtract(cx, cy, cz));*/
				
				Point3D normal = point2.subtract(point1).crossProduct(point3.subtract(point1));
				normal.normalize();
				
				double dot = normal.dotProduct(point1.subtract(cx, cy, cz));
				
				if (dot < 0){
					// Project 3D -> 2D
					p1 = multiply(cam, p1);
					p2 = multiply(cam, p2);
					p3 = multiply(cam, p3);
					
					// Scale
					double px1 = p1[0]/(p1[3] == 0 ? 1 : p1[3]);
					double py1 = p1[1]/(p1[3] == 0 ? 1 : p1[3]);
					double px2 = p2[0]/(p2[3] == 0 ? 1 : p2[3]);
					double py2 = p2[1]/(p2[3] == 0 ? 1 : p2[3]);
					double px3 = p3[0]/(p3[3] == 0 ? 1 : p3[3]);
					double py3 = p3[1]/(p3[3] == 0 ? 1 : p3[3]);
					double pz1 = p1[2];
					if (px1 > 1 || px1 < -1 || py1 > 1 || py1 < -1 || pz1 > 1 || pz1 < -1){
						setProjectedPoint(i, 0, null);
						setProjectedPoint(i, 1, null);
						setProjectedPoint(i, 2, null);
						i++;
						continue;
					}
					
					px1 += 1;
					py1 += 1;
					px1 *= 0.5*WIDTH;
					py1 *= 0.5*HEIGHT;
					px2 += 1;
					py2 += 1;
					px2 *= 0.5*WIDTH;
					py2 *= 0.5*HEIGHT;
					px3 += 1;
					py3 += 1;
					px3 *= 0.5*WIDTH;
					py3 *= 0.5*HEIGHT;
					
					setProjectedPoint(i, 0, new Point2D(px1, py1));
					setProjectedPoint(i, 1, new Point2D(px2, py2));
					setProjectedPoint(i, 2, new Point2D(px3, py3));
				} else {
					setProjectedPoint(i, 0, null);
					setProjectedPoint(i, 1, null);
					setProjectedPoint(i, 2, null);
				}
				
				i++;
			}
			this.angle += 0.01;
		}
		
		public void render(GraphicsContext gc){
			gc.setStroke(this.color);
			gc.setLineWidth(1);
			
			for (int i = 0; i < projected.length; i++){
				Point2D p1 = projected[i][0];
				Point2D p2 = projected[i][1];
				Point2D p3 = projected[i][2];
				
				if (p1 == null || p2 == null || p3 == null) continue;
				
				gc.strokeLine(p1.getX(), HEIGHT-p1.getY(), p2.getX(), HEIGHT-p2.getY());
				gc.strokeLine(p2.getX(), HEIGHT-p2.getY(), p3.getX(), HEIGHT-p3.getY());
				gc.strokeLine(p1.getX(), HEIGHT-p1.getY(), p3.getX(), HEIGHT-p3.getY());

				Point2D t1 = this.textureVertex[this.textureFaces[i][0]];
				Point2D t2 = this.textureVertex[this.textureFaces[i][1]];
				Point2D t3 = this.textureVertex[this.textureFaces[i][2]];
			}
		}
	}
	
	@Override
	public void start(Stage stage){
		Thread counter = new Thread(() -> {
			while (true){
				try {
					this.fps = Math.min(this.frames, FPS);
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
			//rx += (mouseOldY-e.getY())/100;
			ry += Math.toRadians(e.getX()-mouseOldX);
			this.mouseOldX = e.getX();
			this.mouseOldY = e.getY();
		});
		GraphicsContext gc = canvas.getGraphicsContext2D();
		pane.getChildren().add(canvas);
		
		for (int i = 0; i < 1; i++){
			for (int j = 0; j < 1; j++){
				cubes.add(new Cube(new Point3D[]{
					new Point3D(i*2, 0, j*2), new Point3D(i*2, 1, j*2), new Point3D(1+i*2, 1, j*2),
					new Point3D(1+i*2, 0, j*2), new Point3D(i*2, 0, 1+j*2), new Point3D(i*2, 1, 1+j*2), 
					new Point3D(1+i*2, 1, 1+j*2), new Point3D(1+i*2, 0, 1+j*2)}, new int[][]{
						{0, 1, 2}, {0, 2, 3}, {3, 2, 6},
						{3, 6, 7}, {7, 6, 5}, {7, 5, 4},
						{4, 5, 1}, {4, 1, 0}, {1, 5, 6},
						{1, 6, 2}, {4, 0, 3}, {4, 3, 7}
				}, new Point2D[]{
					new Point2D(0, 1), new Point2D(0, 0), new Point2D(1, 0), new Point2D(1, 1)
				}, new int[][]{
					{0, 1, 2}, {1, 3, 2}, {0, 1, 2}, {1, 3, 2},
					{0, 1, 2}, {1, 3, 2}, {0, 1, 2}, {1, 3, 2},
					{0, 1, 2}, {1, 3, 2}, {0, 1, 2}, {1, 3, 2},
					{0, 1, 2}, {1, 3, 2}, {0, 1, 2}, {1, 3, 2}
				}));
			}
		}
		
		//cubes.add(loadCubeFromFile(new File("/home/paul/projects/JavaFX3D/teddy.obj")));
		
		Timeline loop = new Timeline(new KeyFrame(Duration.millis(1000.0/FPS), e -> update(gc)));
		loop.setCycleCount(Animation.INDEFINITE);
		loop.play();
		
		AnimationTimer timer = new AnimationTimer(){
			@Override
			public void handle(long time){
				Rendering3D.this.frames++;
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
		gc.save();
		//gc.translate(300, 220);
		
		if (this.keys.getOrDefault(KeyCode.W, false)){
			moveCamera(0, 0, 0.1);
			this.keys.put(KeyCode.W, true);
		} else if (this.keys.getOrDefault(KeyCode.A, false)){
			moveCamera(-0.1, 0, 0);
			this.keys.put(KeyCode.A, true);
		} else if (this.keys.getOrDefault(KeyCode.S, false)){
			moveCamera(0, 0, -0.1);
			this.keys.put(KeyCode.S, true);
		} else if (this.keys.getOrDefault(KeyCode.D, false)){
			moveCamera(0.1, 0, 0);
			this.keys.put(KeyCode.D, true);
		} else if (this.keys.getOrDefault(KeyCode.SPACE, false)){
			moveCamera(0, 0.1, 0);
			this.keys.put(KeyCode.SPACE, true);
		} else if (this.keys.getOrDefault(KeyCode.Z, false)){
			moveCamera(0, -0.1, 0);
			this.keys.put(KeyCode.Z, true);
		} else if (this.keys.getOrDefault(KeyCode.R, false)){
			cx = 0;
			cy = 0;
			cz = 0;
			rx = 0;
			ry = 0;
			this.keys.put(KeyCode.R, true);
		}
		
		gc.setFill(Color.WHITE);
		gc.fillText(String.format("%.2f %.2f FPS:%d\nCx: %.2f Cy: %.2f Cz: %.2f", Math.toDegrees(rx), Math.toDegrees(ry), fps, cx, cy, cz), 30, 30);
		
		for (Cube cube : cubes){
			cube.evaluate();
			cube.render(gc);
		}
		gc.restore();
	}
	
	private void moveCamera(double tx, double ty, double tz){
		cx += tx;
		cy += ty;
		cz += tz;
	}
	
	private static double[][] getRotateX(double angle){
		return new double[][]{
			{1, 0, 0, 0},
			{0, Math.cos(angle), -Math.sin(angle), 0},
			{0, Math.sin(angle), Math.cos(angle), 0},
			{0, 0, 0, 1}
		};
	}
	
	private static double[][] getRotateY(double angle){
		return new double[][]{
			{Math.cos(angle), 0, Math.sin(angle), 0},
			{0, 1, 0, 0},
			{-Math.sin(angle), 0, Math.cos(angle), 0},
			{0, 0, 0, 1}
		};
	}
	
	private static double[][] getRotateZ(double angle){
		return new double[][]{
			{Math.cos(angle), -Math.sin(angle), 0, 0},
			{Math.sin(angle), Math.cos(angle), 0, 0},
			{0, 0, 1, 0},
			{0, 0, 0, 1}
		};
	}
	
	private static double[][] getTranslation(double tx, double ty, double tz){
		return new double[][]{
			{1, 0, 0, tx},
			{0, 1, 0, ty},
			{0, 0, 1, tz},
			{0, 0, 0, 1}
		};
	}
	
	private static double[] multiply(double[][] mat, double[] vect){
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
	
	private static double[][] multiply(double[][] mat1, double[][] mat2){
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
	
	private Cube loadCubeFromFile(File file){
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			List<Point3D> points = new ArrayList<>();
			List<int[]> faces = new ArrayList<>();
			String line;
			while ((line = reader.readLine()) != null){
				if (line.startsWith("v")){
					points.add(new Point3D(Double.parseDouble(line.split(" ")[1]), Double.parseDouble(line.split(" ")[2]), Double.parseDouble(line.split(" ")[3])));
				} else if (line.startsWith("f")){
					faces.add(new int[]{Integer.parseInt(line.split(" ")[1])-1, Integer.parseInt(line.split(" ")[2])-1, Integer.parseInt(line.split(" ")[3])-1});
				}
			}
			reader.close();
			
			Point3D[] ps = new Point3D[points.size()];
			for (int i = 0; i < ps.length; i++){
				ps[i] = points.get(i);
			}
			
			int[][] fs = new int[faces.size()][3];
			for (int i = 0; i < fs.length; i++){
				fs[i] = faces.get(i);
			}
			
			return new Cube(ps, fs, null, null);
			
		} catch (IOException ex){
			ex.printStackTrace();
			return null;
		}
	}
	
	public static void main(String[] args){
		launch(args);
	}
}
