package com.orangomango.blockworld.model;

import java.util.*;

import com.orangomango.rendering3d.Engine3D;
import com.orangomango.rendering3d.model.Mesh;
import com.orangomango.rendering3d.model.MeshGroup;

public class Chunk{
	public static final int CHUNK_SIZE = 8;
	private static final int HEIGHT_LIMIT = 2;
	
	private Block[][][] blocks = new Block[CHUNK_SIZE][CHUNK_SIZE][CHUNK_SIZE];
	private int x, y, z;
	private final World world;

	private static List<Block> pendingBlocks = new ArrayList<>();
	
	public Chunk(World world, int x, int y, int z){
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		PerlinNoise noise = new PerlinNoise(world.getSeed());
		float frequency = 0.1575f;

		for (int i = 0; i < CHUNK_SIZE; i++){ // x
			for (int j = 0; j < CHUNK_SIZE; j++){ // y
				for (int k = 0; k < CHUNK_SIZE; k++){ // z
					if (this.y < HEIGHT_LIMIT){
						this.blocks[i][j][k] = null;
					} else {
						float n = (noise.noise((i+this.x*CHUNK_SIZE)*frequency, 0, (k+this.z*CHUNK_SIZE)*frequency)+1)/2;
						int h = Math.round(n*(CHUNK_SIZE-1))+CHUNK_SIZE*HEIGHT_LIMIT;
						int pos = this.y*CHUNK_SIZE+j;
						if (pos >= h){
							this.blocks[i][j][k] = new Block(this, i, j, k, pos > h+3 ? "stone" : (pos == h ? "grass" : "dirt"));
						}
					}
				}
			}
		}

		// Trees generation
		Random random = world.getRandom();
		for (int i = 0; i < CHUNK_SIZE; i++){ // x
			for (int j = 0; j < CHUNK_SIZE; j++){ // z
				int h = 0;
				for (int k = 0; k < CHUNK_SIZE; k++){ // y
					if (this.blocks[i][k][j] == null) h++;
				}
				if (h > 0 && h < CHUNK_SIZE){
					if (random.nextInt(1000) < 7){
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
					}
				}
			}
		}

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
		//System.out.format("%d %d %d %s\n", x, y, z, block);
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
		for (MeshGroup mg : Engine3D.getInstance().getObjects()){
			if (mg.tag != null && mg.tag.equals(String.format("%d %d %d", chunk.getX(), chunk.getY(), chunk.getZ()))){
				mg.updateMesh(chunk.getMesh());
			}
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
