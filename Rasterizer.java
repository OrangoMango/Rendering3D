import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.image.Image;
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
	private static final Image IMAGE = new Image(Rasterizer.class.getResourceAsStream("dirt.png"));
	
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
			
			renderTriangle((int)a.getX(), (int)a.getY(), (int)b.getX(), (int)b.getY(), (int)c.getX(), (int)c.getY(), 0, 1, 0, 0, 1, 0, gc, IMAGE);
		}
		
		private void renderTriangle(int x1, int y1, int x2, int y2, int x3, int y3, double u1, double v1, double u2, double v2, double u3, double v3, GraphicsContext gc, Image image){
			if (y2 < y1){
				y1 = swap(y2, y2 = y1);
				x1 = swap(x2, x2 = x1);
				u1 = swap(u2, u2 = u1);
				v1 = swap(v2, v2 = v1);
			}
			if (y3 < y1){
				y1 = swap(y3, y3 = y1);
				x1 = swap(x3, x3 = x1);
				u1 = swap(u3, u3 = u1);
				v1 = swap(v3, v3 = v1);
			}
			if (y3 < y2){
				y2 = swap(y3, y3 = y2);
				x2 = swap(x3, x3 = x2);
				u2 = swap(u3, u3 = u2);
				v2 = swap(v3, v3 = v2);
			}
			
			int dx1 = x2-x1;
			int dy1 = y2-y1;
			double du1 = u2-u1;
			double dv1 = v2-v1;
			
			int dx2 = x3-x1;
			int dy2 = y3-y1;
			double du2 = u3-u1;
			double dv2 = v3-v1;
			
			double tex_u, tex_v;
			
			double dax_step = 0, dbx_step = 0, du1_step = 0, dv1_step = 0, du2_step = 0, dv2_step = 0;
			
			if (dy1 != 0) dax_step = dx1/(double)Math.abs(dy1);
			if (dy2 != 0) dbx_step = dx2/(double)Math.abs(dy2);
			
			if (dy1 != 0) du1_step = du1/Math.abs(dy1);
			if (dy1 != 0) dv1_step = dv1/Math.abs(dy1);
			
			if (dy2 != 0) du2_step = du2/Math.abs(dy2);
			if (dy2 != 0) dv2_step = dv2/Math.abs(dy2);
			
			if (dy1 != 0){
				for (int i = y1; i <= y2; i++){
					int ax = x1+(int)((i-y1)*dax_step);
					int bx = x1+(int)((i-y1)*dbx_step);
					
					double tex_su = u1+(i-y1)*du1_step;
					double tex_sv = v1+(i-y1)*dv1_step;
					
					double tex_eu = u1+(i-y1)*du2_step;
					double tex_ev = v1+(i-y1)*dv2_step;
					
					if (ax > bx){
						ax = swap(bx, bx = ax);
						tex_su = swap(tex_eu, tex_eu = tex_su);
						tex_sv = swap(tex_ev, tex_ev = tex_sv);
					}
					
					tex_u = tex_su;
					tex_v = tex_sv;
					
					double tstep = 1.0/(bx-ax);
					double t = 0.0;
					
					for (int j = ax; j < bx; j++){
						tex_u = (1-t)*tex_su+t*tex_eu;
						tex_v = (1-t)*tex_sv+t*tex_ev;
						
						gc.getPixelWriter().setColor(j, i, image.getPixelReader().getColor((int)(tex_u*image.getWidth()), (int)(tex_v*image.getHeight())));
						
						t += tstep;
					}
				}
			}
			
			dx1 = x3-x2;
			dy1 = y3-y2;
			du1 = u3-u2;
			dv1 = v3-v2;
			
			if (dy1 != 0) dax_step = dx1/(double)Math.abs(dy1);
			if (dy2 != 0) dbx_step = dx2/(double)Math.abs(dy2);
			
			du1_step = 0; dv1_step = 0;
			if (dy1 != 0) du1_step = du1/Math.abs(dy1);
			if (dy1 != 0) dv1_step = dv1/Math.abs(dy1);
			
			if (dy1 != 0){
				for (int i = y2; i <= y3; i++){
					int ax = x2+(int)((i-y2)*dax_step);
					int bx = x1+(int)((i-y1)*dbx_step);
					
					double tex_su = u2+(i-y2)*du1_step;
					double tex_sv = v2+(i-y2)*dv1_step;
					
					double tex_eu = u1+(i-y1)*du2_step;
					double tex_ev = v1+(i-y1)*dv2_step;
					
					if (ax > bx){
						ax = swap(bx, bx = ax);
						tex_su = swap(tex_eu, tex_eu = tex_su);
						tex_sv = swap(tex_ev, tex_ev = tex_sv);
					}
					
					tex_u = tex_su;
					tex_v = tex_sv;
					
					double tstep = 1.0/(bx-ax);
					double t = 0.0;
					
					for (int j = ax; j < bx; j++){
						tex_u = (1-t)*tex_su+t*tex_eu;
						tex_v = (1-t)*tex_sv+t*tex_ev;
						
						gc.getPixelWriter().setColor(j, i, image.getPixelReader().getColor((int)(tex_u*image.getWidth()), (int)(tex_v*image.getHeight())));
						
						t += tstep;
					}
				}
			}
		}
	}
	
	private static <T extends Number> T swap(T a, T b){
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
