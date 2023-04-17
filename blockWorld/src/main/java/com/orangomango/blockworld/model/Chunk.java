package com.orangomango.blockworld.model;

import javafx.geometry.Point3D;

import java.util.*;

import com.orangomango.rendering3d.Engine3D;
import com.orangomango.rendering3d.model.Mesh;
import com.orangomango.rendering3d.model.MeshGroup;

public class Chunk{	
	public static final int CHUNK_SIZE = 4;
	public static final int HEIGHT_LIMIT = 2;
	public static final int WATER_HEIGHT = HEIGHT_LIMIT*CHUNK_SIZE+7;
	
	private Block[][][] blocks = new Block[CHUNK_SIZE][CHUNK_SIZE][CHUNK_SIZE];
	private int x, y, z;
	private World world;

	public static List<Block> pendingBlocks = new ArrayList<>(); // pendingBlocks cannot contain blocks with special settings (like water's yOffset)
	public static Atlas atlas = new Atlas("/atlas.json");
	
	public Chunk(World world, int x, int y, int z){
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		PerlinNoise noise = new PerlinNoise(world.getSeed());
		Random random = world.getRandom();
		float frequency = 0.1575f;
		float biomeFreq = 0.05f;

		for (int i = 0; i < CHUNK_SIZE; i++){ // x
			for (int j = 0; j < CHUNK_SIZE; j++){ // y
				for (int k = 0; k < CHUNK_SIZE; k++){ // z
					if (this.y < HEIGHT_LIMIT){
						this.blocks[i][j][k] = null;
					} else {
						float n = (noise.noise((i+this.x*CHUNK_SIZE)*frequency, 0, (k+this.z*CHUNK_SIZE)*frequency)+1)/2;
						float b = (noise.noise((i+this.x*CHUNK_SIZE)*biomeFreq, 0, (k+this.z*CHUNK_SIZE)*biomeFreq)+1)/2;
						// TODO Replace 16 with CHUNK_SIZE
						int h = Math.round(n*(16-1))+CHUNK_SIZE*HEIGHT_LIMIT; // air column
						if (world.superFlat) h = CHUNK_SIZE*HEIGHT_LIMIT+1;
						int pos = this.y*CHUNK_SIZE+j;
						if (pos >= h){
							String biome = b <= 0.4 || (pos > WATER_HEIGHT && random.nextInt(100) < 35) ? "sand" : (pos == h && pos <= WATER_HEIGHT ? "grass" : "dirt");
							this.blocks[i][j][k] = new Block(this, i, j, k, pos > h+3 ? "stone" : biome);
						}
					}
				}
			}
		}

		// Trees generation
		for (int i = 0; i < CHUNK_SIZE; i++){ // x
			for (int j = 0; j < CHUNK_SIZE; j++){ // z
				int h = 0;
				for (int k = 0; k < CHUNK_SIZE; k++){ // y
					if (this.blocks[i][k][j] == null) h++;
				}
				if (h > 0 && h < CHUNK_SIZE && h-1+this.y*CHUNK_SIZE < WATER_HEIGHT){
					if (random.nextInt(1000) < 12 && !this.blocks[i][h][j].getType().equals("sand") && !this.blocks[i][h][j].getType().equals("water")){
						int treeHeight = 5;
						for (int k = 0; k < treeHeight; k++){
							setBlock(new Block(this, i, h-1-k, j, "wood_log"), i, h-1-k, j);
						}
						// 5x5
						for (int kx = i-2; kx < i+3; kx++){
							for (int ky = j-2; ky < j+3; ky++){
								if (kx == i && ky == j) continue;
								setBlock(new Block(this, kx, h-1-(treeHeight-2), ky, "leaves"), kx, h-1-(treeHeight-2), ky);
							}
						}
						// 3x3
						for (int kx = i-1; kx < i+2; kx++){
							for (int ky = j-1; ky < j+2; ky++){
								if (kx == i && ky == j) continue;
								setBlock(new Block(this, kx, h-1-(treeHeight-1), ky, "leaves"), kx, h-1-(treeHeight-1), ky);
							}
						}
						setBlock(new Block(this, i, h-1-treeHeight, j, "leaves"), i, h-1-treeHeight, j);
					} else if (random.nextInt(1000) < 16){
						String flowerType = "flower_"+(random.nextBoolean() ? "red" : "yellow");
						if (this.blocks[i][h][j].getType().equals("sand")){
							flowerType = "bush";
						}
						setBlock(new Block(this, i, h-1, j, flowerType), i, h-1, j);
					}
				}
			}
		}
		
		// Water generation
		for (int i = 0; i < CHUNK_SIZE; i++){ // x
			for (int j = 0; j < CHUNK_SIZE; j++){ // z
				for (int k = 0; k < CHUNK_SIZE; k++){ // y
					if (this.blocks[i][k][j] == null && k+this.y*CHUNK_SIZE >= WATER_HEIGHT){
						Block waterBlock = new Block(this, i, k, j, "water");
						if (waterBlock.getY() == WATER_HEIGHT) waterBlock.setYOffset(0.2);
						setBlock(waterBlock, i, k, j);
					}
				}
			}
		}

		buildPendingBlocks();
	}
	
	public Chunk(World world, int x, int y, int z, int[][][] input){
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		
		for (int i = 0; i < CHUNK_SIZE; i++){ // x
			for (int j = 0; j < CHUNK_SIZE; j++){ // y
				for (int k = 0; k < CHUNK_SIZE; k++){ // z
					int id = input[i][j][k];
					if (id == 0){
						this.blocks[i][j][k] = null;
					} else {
						this.blocks[i][j][k] = new Block(this, i, j, k, atlas.getBlockType(id));
						if (this.blocks[i][j][k].getType().equals("water") && this.blocks[i][j][k].getY() == WATER_HEIGHT) this.blocks[i][j][k].setYOffset(0.2);
					}
				}
			}
		}
		
		buildPendingBlocks();
	}
	
	private void buildPendingBlocks(){
		// Build pending blocks generated from other chunks
		Iterator<Block> iterator = pendingBlocks.iterator();
		while (iterator.hasNext()){
			Block block = iterator.next();
			if (containsBlock(block.getX()-this.x*CHUNK_SIZE, block.getY()-this.y*CHUNK_SIZE, block.getZ()-this.z*CHUNK_SIZE)){
				setBlock(block, block.getX()-this.x*CHUNK_SIZE, block.getY()-this.y*CHUNK_SIZE, block.getZ()-this.z*CHUNK_SIZE);
				iterator.remove();
			}
		}
	}

	private boolean containsBlock(int x, int y, int z){
		return x >= 0 && y >= 0 && z >= 0 && x < CHUNK_SIZE && y < CHUNK_SIZE && z < CHUNK_SIZE;
	}
	
	public Block getBlockAt(int x, int y, int z){
		if (containsBlock(x, y, z)){
			return this.blocks[x][y][z];
		} else {
			return null;
		}
	}
	
	public void setBlock(Block block, int x, int y, int z){
		if (containsBlock(x, y, z)){
			this.blocks[x][y][z] = block;
		} else if (block != null){
			Chunk chunk = this.world.getChunkAt(block.getX()/CHUNK_SIZE, block.getY()/CHUNK_SIZE, block.getZ()/CHUNK_SIZE);
			if (chunk == null){
				pendingBlocks.add(block);
			} else if (block.getX() >= 0 && block.getY() >= 0 && block.getZ() >= 0){
				chunk.setBlock(block, block.getX() % CHUNK_SIZE, block.getY() % CHUNK_SIZE, block.getZ() % CHUNK_SIZE);
				updateMesh(chunk);
			}
		}
	}
	
	public int getX(){
		return this.x;
	}
	
	public int getY(){
		return this.y;
	}
	
	public int getZ(){
		return this.z;
	}
	
	public World getWorld(){
		return this.world;
	}

	public static void updateMesh(Chunk chunk){
		boolean found = false;
		for (MeshGroup mg : Engine3D.getInstance().getObjects()){
			if (mg.tag != null && mg.tag.equals(World.getChunkTag(chunk.getX(), chunk.getY(), chunk.getZ()))){
				mg.updateMesh(chunk.getMesh());
				found = true;
			}
		}
		if (!found){
			Engine3D.getInstance().getObjects().add(ChunkManager.getMeshGroup(chunk));
		}
		chunk.setupFaces();
	}
	
	public List<Mesh> getMesh(){
		List<Mesh> output = new ArrayList<>();
		for (int i = 0; i < CHUNK_SIZE; i++){
			for (int j = 0; j < CHUNK_SIZE; j++){
				for (int k = 0; k < CHUNK_SIZE; k++){
					if (this.blocks[i][j][k] != null) output.add(this.blocks[i][j][k].getMesh());
				}
			}
		}
		
		Point3D[] bounds = new Point3D[]{
			new Point3D(this.x, this.y, this.z), new Point3D(this.x, 1+this.y, this.z), new Point3D(1+this.x, 1+this.y, this.z),
			new Point3D(1+this.x, this.y, this.z), new Point3D(this.x, this.y, 1+this.z), new Point3D(this.x, 1+this.y, 1+this.z),
			new Point3D(1+this.x, 1+this.y, 1+this.z), new Point3D(1+this.x, this.y, 1+this.z)};
		for (int i = 0; i < bounds.length; i++){
			bounds[i] = bounds[i].multiply(CHUNK_SIZE);
		}
		
		/*Mesh chunkBound = new Mesh(null, bounds, new int[][]{
				{0, 1, 2}, {0, 2, 3}, {3, 2, 6},
				{3, 6, 7}, {7, 6, 5}, {7, 5, 4},
				{4, 5, 1}, {4, 1, 0}, {1, 5, 6},
				{1, 6, 2}, {4, 0, 3}, {4, 3, 7}
		}, null, new int[12][], null, null, null, null); // Everything is null because it's rendered only as a wireframe
		chunkBound.wireframe = true;
		chunkBound.setShowLines(true);
		chunkBound.skipCondition = cam -> {
			int camX = (int)(cam.getX()/Chunk.CHUNK_SIZE);
			int camY = (int)(cam.getY()/Chunk.CHUNK_SIZE);
			int camZ = (int)(cam.getZ()/Chunk.CHUNK_SIZE);
			return !(camX == this.x && camY == this.y && camZ+1 == this.z);
		};
		output.add(chunkBound);*/
		return output;
	}

	public void setupFaces(){
		for (int i = 0; i < CHUNK_SIZE; i++){ // x
			for (int j = 0; j < CHUNK_SIZE; j++){ // y
				for (int k = 0; k < CHUNK_SIZE; k++){ // z
					if (this.blocks[i][j][k] != null) this.blocks[i][j][k].setupFaces();
				}
			}
		}
	}

	@Override
	public String toString(){
		return String.format("Chunk at %d %d %d", this.x, this.y, this.z);
	}
}
