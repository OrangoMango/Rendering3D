package com.orangomango.blockworld;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.Font;

import com.orangomango.rendering3d.Engine3D;
import com.orangomango.rendering3d.model.Light;
import com.orangomango.rendering3d.model.MeshVertex;
import com.orangomango.blockworld.model.*;
import com.orangomango.blockworld.util.Util;
import com.orangomango.blockworld.entity.Player;

/**
 * Minecraft-clone using a 3D engine made from scratch in Java/JavaFX
 * 
 * @author OrangoMango (https://orangomango.github.io)
 * @version 1.0
 */
public class MainApplication extends Application{
	private static final int WIDTH = 320;
	private static final int HEIGHT = 180;
	private static final int CHUNKS = 9;

	private static Image POINTER = new Image(MainApplication.class.getResourceAsStream("/images/pointer.png"));
	private static final String[] inventoryBlocks = new String[]{"wood", "cactus", "debug", "torch", "wood_log", "leaves", "cobblestone", "bricks", "glass"};
	public static Engine3D ENGINE;

	private int currentBlock = 0;
	private Color backgroundColor = Color.CYAN;
	private double time = 1;
	private boolean amTime = false;
	
	@Override
	public void start(Stage stage){
		stage.setTitle("BlockWorld");

		ENGINE = new Engine3D(stage, WIDTH, HEIGHT);

		Player player = new Player(0, -15, 0, WIDTH, HEIGHT);
		ENGINE.setCamera(player.getCamera());

		Light light = new Light(1);
		ENGINE.getLights().add(light);

		World world = new World((int)System.currentTimeMillis(), false);

		ChunkManager manager = new ChunkManager(world, CHUNKS);
		manager.deleteSavedWorld();

		Thread dayNight = new Thread(() -> {
			int direction = -1;
			final double inc = 0.0008;
			while (true){
				try {
					this.time += inc*direction;
					if (this.time < 0 || this.time > 1){
						direction *= -1;
						this.amTime = !this.amTime;
					}
					light.setFixedIntensity(Math.min(Math.max(0, this.time), 1));
					double b = this.backgroundColor.getBrightness()+inc*direction;
					b = Math.min(Math.max(0, b), 1);
					this.backgroundColor = Color.hsb(this.backgroundColor.getHue(), this.backgroundColor.getSaturation(), b);
					ENGINE.setBackgroundColor(this.backgroundColor);
					Thread.sleep(250);
				} catch (InterruptedException ex){
					ex.printStackTrace();
				}
			}
		});
		dayNight.setDaemon(true);
		dayNight.start();

		// Chunk managing
		player.setOnChunkPositionChanged(chunkPos -> {
			manager.manage(chunkPos);
		});

		// Ray-casting
		ENGINE.setOnMousePressed(e -> {
			Block block = null;

			double stepX = Math.cos(player.getRx())*Math.cos(player.getRy()+Math.PI/2);
			double stepY = -Math.sin(player.getRx());
			double stepZ = Math.cos(player.getRx())*Math.sin(player.getRy()+Math.PI/2);

			int lastX = 0;
			int lastY = 0;
			int lastZ = 0;

			for (double i = 0; i <= 10; i += 0.01){
				int lX = (int)(player.getX()+i*stepX);
				int lY = (int)(player.getY()+i*stepY);
				int lZ = (int)(player.getZ()+i*stepZ);
				block = world.getBlockAt(lX, lY, lZ);
				if (block == null || i == 0 || block.isLiquid()){
					lastX = lX;
					lastY = lY;
					lastZ = lZ;
				}
				if (block != null && !block.isLiquid()) break;
			}
			if (block != null){
				boolean chunkUpdate = false;
				Block down = null; // The underneath liquid block
				if (e.getButton() == MouseButton.PRIMARY){
					world.removeBlockAt(block.getX(), block.getY(), block.getZ());
					int chunkX = block.getX() / Chunk.CHUNK_SIZE;
					int chunkY = block.getY() / Chunk.CHUNK_SIZE;
					int chunkZ = block.getZ() / Chunk.CHUNK_SIZE;
					chunkUpdate = true;
					down = world.getBlockAt(block.getX(), block.getY()+1, block.getZ());
				} else if (e.getButton() == MouseButton.SECONDARY && lastX >= 0 && lastY >= 0 && lastZ >= 0){
					world.setBlockAt(lastX, lastY, lastZ, inventoryBlocks[this.currentBlock]);
					int chunkX = lastX / Chunk.CHUNK_SIZE;
					int chunkY = lastY / Chunk.CHUNK_SIZE;
					int chunkZ = lastZ / Chunk.CHUNK_SIZE;
					chunkUpdate = true;
					down = world.getBlockAt(lastX, lastY+1, lastZ);
				}
				if (chunkUpdate){
					if (down != null && down.isLiquid()){
						down.removeMesh(); // Update liquid blocks in case the user placed a block on top of them
					}

					// TODO (maybe don't update the entire chunk)
					for (int i = -1; i < 2; i++){
						for (int j = -1; j < 2; j++){
							for (int k = -1; k < 2; k++){
								Chunk chunk = world.getChunkAt(block.getX()/Chunk.CHUNK_SIZE+i, block.getY()/Chunk.CHUNK_SIZE+k, block.getZ()/Chunk.CHUNK_SIZE+j);
								if (chunk != null){
									chunk.updateMesh();
								}
							}
						}
					}
				}
			}
		});

		// Player movement
		final double speed = 0.4;
		ENGINE.setOnKey(KeyCode.W, () -> player.move(speed*Math.cos(player.getRy()+Math.PI/2), 0, speed*Math.sin(player.getRy()+Math.PI/2)), false);
		ENGINE.setOnKey(KeyCode.A, () -> player.move(-speed*Math.cos(player.getRy()), 0, -speed*Math.sin(player.getRy())), false);
		ENGINE.setOnKey(KeyCode.S, () -> player.move(-speed*Math.cos(player.getRy()+Math.PI/2), 0, -speed*Math.sin(player.getRy()+Math.PI/2)), false);
		ENGINE.setOnKey(KeyCode.D, () -> player.move(speed*Math.cos(player.getRy()), 0, speed*Math.sin(player.getRy())), false);
		ENGINE.setOnKey(KeyCode.SPACE, () -> player.move(0, -speed, 0), false);
		ENGINE.setOnKey(KeyCode.SHIFT, () -> player.move(0, speed, 0), false);

		// Inventory
		ENGINE.setOnKey(KeyCode.DIGIT1, () -> this.currentBlock = 0, true);
		ENGINE.setOnKey(KeyCode.DIGIT2, () -> this.currentBlock = 1, true);
		ENGINE.setOnKey(KeyCode.DIGIT3, () -> this.currentBlock = 2, true);
		ENGINE.setOnKey(KeyCode.DIGIT4, () -> this.currentBlock = 3, true);
		ENGINE.setOnKey(KeyCode.DIGIT5, () -> this.currentBlock = 4, true);
		ENGINE.setOnKey(KeyCode.DIGIT6, () -> this.currentBlock = 5, true);
		ENGINE.setOnKey(KeyCode.DIGIT7, () -> this.currentBlock = 6, true);
		ENGINE.setOnKey(KeyCode.DIGIT8, () -> this.currentBlock = 7, true);
		ENGINE.setOnKey(KeyCode.DIGIT9, () -> this.currentBlock = 8, true);

		// Settings
		ENGINE.setOnKey(KeyCode.O, ENGINE::toggleMouseMovement, true);
		ENGINE.setOnKey(KeyCode.P, manager::saveWorld, true);
		ENGINE.setOnKey(KeyCode.R, player.getCamera()::reset, true);

		ENGINE.setOnPreUpdate(gc -> {
			for (Chunk chunk : world.getChunks()){
				for (int i = 0; i < Chunk.CHUNK_SIZE; i++){
					for (int j = 0; j < Chunk.CHUNK_SIZE; j++){
						for (int k = 0; k < Chunk.CHUNK_SIZE; k++){
							Block block = chunk.getBlockAt(i, j, k);
							if (block != null){
								block.update();
							}
						}
					}
				}
			}
		});

		ENGINE.setOnUpdate(gc -> {
			gc.setFill(Color.BLACK);
			gc.setTextAlign(TextAlignment.RIGHT);
			gc.setFont(new Font("sans-serif", 11));
			String text = "Projected: "+MeshVertex.getProjectedVerticesCount();
			text += "\nView: "+MeshVertex.getViewVerticesCount();
			text += "\n"+Util.formatTime(this.time, this.amTime);
			gc.fillText(text, WIDTH*0.95, HEIGHT*0.1);

			// Show pointer
			double pointerSize = 26*player.getCamera().getAspectRatio();
			gc.drawImage(POINTER, WIDTH/2.0-pointerSize/2, HEIGHT/2.0-pointerSize/2, pointerSize, pointerSize);
		});
		
		stage.setResizable(false);
		stage.setScene(ENGINE.getScene());
		stage.show();
	}
	
	public static void main(String[] args){		
		launch(args);
	}
}
