import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.canvas.*;
import javafx.scene.paint.Color;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.geometry.Point2D;

public class Rasterizer extends Application{
	private static final double WIDTH = 600;
	private static final double HEIGHT = 600;
	private volatile int frames, fps;
	private static final int FPS = 40;
	
	private Triangle triangle;
	
	private static class Triangle{
		private Point2D a, b, c;
		private Point2D ta, tb, tc;
		
		public Triangle(Point2D a, Point2D b, Point2D c, Point2D ta, Point2D tb, Point2D tc){
			this.a = a;
			this.b = b;
			this.c = c;
			this.ta = ta;
			this.tb = tb;
			this.tc = tc;
		}
		
		public void render(GraphicsContext gc){
			gc.setStroke(Color.WHITE);
			gc.setLineWidth(1);
			
			gc.strokePolygon(new double[]{a.getX(), b.getX(), c.getX()}, new double[]{a.getY(), b.getY(), c.getY()}, 3);
			
			renderTriangle(a.getX(), a.getY(), b.getX(), b.getY(), c.getX(), c.getY(), gc);
		}
		
		private void renderTriangle(double x1, double y1, double x2, double y2, double x3, double y3, GraphicsContext gc){
			double[] swap = new double[2];
			if (y2 < y1){
				y1 = swap(y2, y2 = y1);
				x1 = swap(x2, x2 = x1);
			}
			if (y3 < y1){
				y1 = swap(y3, y3 = y1);
				x1 = swap(x3, x3 = x1);
			}
			if (y3 < y2){
				y2 = swap(y3, y3 = y2);
				x2 = swap(x3, x3 = x2);
			}
			
			
		}
	}
	
	private static double swap(double a, double b){
		return a;
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

		stage.setTitle("Rasterizer");
		StackPane pane = new StackPane();
		Canvas canvas = new Canvas(WIDTH, HEIGHT);
		GraphicsContext gc = canvas.getGraphicsContext2D();
		pane.getChildren().add(canvas);
		
		this.triangle = new Triangle(new Point2D(100, 400), new Point2D(400, 200), new Point2D(500, 250), null, null, null);
		
		Timeline loop = new Timeline(new KeyFrame(Duration.millis(1000.0/FPS), e -> update(gc)));
		loop.setCycleCount(Animation.INDEFINITE);
		loop.play();
		
		AnimationTimer timer = new AnimationTimer(){
			@Override
			public void handle(long time){
				Rasterizer.this.frames++;
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
		
		this.triangle.render(gc);
		
		gc.setFill(Color.WHITE);
		gc.fillText(String.format("FPS: %d", fps), 30, 30);
	}
	
	public static void main(String[] args){
		launch(args);
	}
}
