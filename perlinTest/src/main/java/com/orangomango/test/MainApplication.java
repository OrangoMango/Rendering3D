package com.orangomango.test;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.scene.canvas.*;
import javafx.scene.paint.Color;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;

public class MainApplication extends Application{
	private static final int WIDTH = 600;
	private static final int HEIGHT = 600;

	private StringProperty frequency;
	private StringProperty seed;
	private DoubleProperty blockSize;
	
	public void start(Stage stage){
		stage.setTitle("PerlinNoise test");
		
		HBox pane = new HBox();
		Canvas canvas = new Canvas(WIDTH, HEIGHT);
		GraphicsContext gc = canvas.getGraphicsContext2D();
		pane.getChildren().add(canvas);
		
		Timeline loop = new Timeline(new KeyFrame(Duration.millis(1000.0/5), e -> update(gc)));
		loop.setCycleCount(Animation.INDEFINITE);
		loop.play();
		
		GridPane grid = new GridPane();
		Slider blockSlider = new Slider(1, 30, 15);
		blockSlider.setBlockIncrement(1);
		blockSlider.setSnapToTicks(true);
		blockSlider.setMajorTickUnit(1);
		blockSlider.setShowTickLabels(true);
		this.blockSize = blockSlider.valueProperty();
		TextField seedInput = new TextField(Integer.toString((int)System.currentTimeMillis()));
		this.seed = seedInput.textProperty();
		TextField freqInput = new TextField("0.1575");
		this.frequency = freqInput.textProperty();
		
		grid.add(blockSlider, 0, 0);
		grid.add(seedInput, 0, 1);
		grid.add(freqInput, 0, 2);
		
		pane.getChildren().add(grid);
		
		stage.setScene(new Scene(pane, WIDTH+200, HEIGHT));
		stage.setResizable(false);
		stage.show();
	}
	
	private void update(GraphicsContext gc){
		gc.clearRect(0, 0, WIDTH, HEIGHT);
		String text = this.seed.get();
		int num = 0;
		try {
			num = Integer.parseInt(text);
		} catch (NumberFormatException ex){
			this.seed.set(Integer.toString(num));
		}
		PerlinNoise noise = new PerlinNoise(num);
		int blockSize = (int)this.blockSize.get();
		float freq = Float.parseFloat(this.frequency.get());
		for (int i = 0; i < WIDTH; i += blockSize){
			for (int j = 0; j < HEIGHT; j += blockSize){
				float n = (noise.noise(i/blockSize*freq, 0, j/blockSize*freq)+1)/2;
				Color color = Color.color(n, n, n);
				for (int x = i; x < i+blockSize; x++){
					for (int y = j; y < j+blockSize; y++){
						gc.getPixelWriter().setColor(x, y, color);
					}
				}
			}
		}
		
		gc.setStroke(Color.RED);
		double size = 16*blockSize;
		for (int i = 0; i < WIDTH/size; i++){
			for (int j = 0; j < HEIGHT/size; j++){
				gc.strokeRect(i*size, j*size, size, size);
			}
		}
	}
	
	public static void main(String[] args){
		launch(args);
	}
}
