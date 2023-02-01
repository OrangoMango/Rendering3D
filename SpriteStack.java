import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.image.Image;
import javafx.scene.canvas.*;
import javafx.scene.paint.Color;
import javafx.animation.*;
import javafx.util.Duration;

public class SpriteStack extends Application{
	private static final double WIDTH = 600;
	private static final double HEIGHT = 600;
	private volatile int frames, fps;
	private static final int FPS = 40;
	
	private static final String IMAGE_NAME = "temple.png";
	private static Image IMAGE = new Image(SpriteStack.class.getResourceAsStream(IMAGE_NAME));
	
	private double angle = 0;
	
	static {
		IMAGE = new Image(SpriteStack.class.getResourceAsStream(IMAGE_NAME), IMAGE.getWidth()*5, IMAGE.getHeight()*5, true, true);
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

		stage.setTitle("SpriteStack");
		StackPane pane = new StackPane();
		Canvas canvas = new Canvas(WIDTH, HEIGHT);
		GraphicsContext gc = canvas.getGraphicsContext2D();
		pane.getChildren().add(canvas);
		
		Timeline loop = new Timeline(new KeyFrame(Duration.millis(1000.0/FPS), e -> update(gc)));
		loop.setCycleCount(Animation.INDEFINITE);
		loop.play();
		
		AnimationTimer timer = new AnimationTimer(){
			@Override
			public void handle(long time){
				SpriteStack.this.frames++;
			}
		};
		timer.start();
		
		Thread game = new Thread(() -> {
			while (true){
				try {
					this.angle += 5;
					Thread.sleep(100);
				} catch (InterruptedException ex){
					ex.printStackTrace();
				}
			}
		});
		game.setDaemon(true);
		game.start();
		
		stage.setScene(new Scene(pane, WIDTH, HEIGHT));
		stage.show();
	}
	
	private void update(GraphicsContext gc){
		gc.clearRect(0, 0, WIDTH, HEIGHT);
		gc.setFill(Color.BLACK);
		gc.fillRect(0, 0, WIDTH, HEIGHT);
		
		int sprites = 32; // temple -> 32, car -> 8
		
		gc.save();
		gc.translate(200+IMAGE.getWidth()/sprites/2, 200+IMAGE.getHeight()/2);
		gc.rotate(this.angle);
		gc.translate(-IMAGE.getWidth()/sprites/2, -IMAGE.getHeight()/2);
		gc.setStroke(Color.RED);
		gc.strokeRect(0, 0, IMAGE.getWidth()/sprites, IMAGE.getHeight());
		for (int i = 0; i < sprites; i++){
			gc.drawImage(IMAGE, IMAGE.getWidth()/sprites*i, 0, IMAGE.getWidth()/sprites, IMAGE.getHeight(), i*Math.cos(Math.toRadians(this.angle+90))*5, -i*Math.sin(Math.toRadians(this.angle+90))*5, IMAGE.getWidth()/sprites, IMAGE.getHeight());
		}
		gc.restore();
		
		gc.setFill(Color.WHITE);
		gc.fillText(String.format("FPS: %d", fps), 30, 30);
	}
	
	public static void main(String[] args){
		launch(args);
	}
}
