package com.orangomango.blockworld;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.input.KeyCode;

import com.orangomango.rendering3d.Engine3D;
import com.orangomango.rendering3d.model.Light;
import com.orangomango.blockworld.model.Player;
import com.orangomango.blockworld.model.World;

/**
 * Minecraft-clone using a 3D engine made from scratch in Java/JavaFX
 * 
 * @author OrangoMango (https://orangomango.github.io)
 * @version 1.0
 */
public class MainApplication extends Application{
	private static final int WIDTH = 320;
	private static final int HEIGHT = 180;
	private static final int CHUNKS = 5;
	
	@Override
	public void start(Stage stage){
		stage.setTitle("BlockWorld");
		
		Engine3D engine = new Engine3D(stage, WIDTH, HEIGHT);
		Player player = new Player(0, 15, 0, WIDTH, HEIGHT);
		engine.setCamera(player.getCamera());

		Light light = new Light();
		engine.getLights().add(light);

		World world = new World((int)System.currentTimeMillis(), false);

		// Ray-casting
		// ...

		// Player movement
		final double speed = 0.4;
		engine.setOnKey(KeyCode.W, () -> player.move(speed*Math.cos(player.getRy()+Math.PI/2), 0, speed*Math.sin(player.getRy()+Math.PI/2)), false);
		engine.setOnKey(KeyCode.A, () -> player.move(-speed*Math.cos(player.getRy()), 0, -speed*Math.sin(player.getRy())), false);
		engine.setOnKey(KeyCode.S, () -> player.move(-speed*Math.cos(player.getRy()+Math.PI/2), 0, -speed*Math.sin(player.getRy()+Math.PI/2)), false);
		engine.setOnKey(KeyCode.D, () -> player.move(speed*Math.cos(player.getRy()), 0, speed*Math.sin(player.getRy())), false);
		engine.setOnKey(KeyCode.SPACE, () -> player.move(0, -speed, 0), false);
		engine.setOnKey(KeyCode.SHIFT, () -> player.move(0, speed, 0), false);

		engine.setOnKey(KeyCode.O, engine::toggleMouseMovement, true);

		engine.setOnUpdate(gc -> {

		});
		
		stage.setResizable(false);
		stage.setScene(engine.getScene());
		stage.show();
	}
	
	public static void main(String[] args){		
		launch(args);
	}
}
