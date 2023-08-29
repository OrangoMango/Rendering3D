package com.orangomango.blockworld.model;

import javafx.geometry.Point3D;
import javafx.geometry.Point2D;

import com.orangomango.rendering3d.model.Mesh;

public class Block{
	private int x, y, z;
	private World world;
	private Mesh mesh;
	private String type;
	private int id;
	private boolean transparent;
	private boolean sprite;
	private double yOffset;
	private boolean liquid;

	public static final double LIQUID_OFFSET = 0.2;

	public Block(Chunk chunk, int x, int y, int z, String type){
		this.world = chunk.getWorld();
		this.x = x+chunk.getX()*Chunk.CHUNK_SIZE;
		this.y = y+chunk.getY()*Chunk.CHUNK_SIZE;
		this.z = z+chunk.getZ()*Chunk.CHUNK_SIZE;
		this.type = type;
		setupSettings();
	}

	private void setupSettings(){
		this.id = Atlas.MAIN_ATLAS.getBlockId(this.type);
		this.transparent = Atlas.MAIN_ATLAS.isTransparent(this.type);
		this.sprite = Atlas.MAIN_ATLAS.isSprite(this.type);
		this.liquid = Atlas.MAIN_ATLAS.isLiquid(this.type);
	}

	public void setupFaces(){
		if (this.sprite) return;
		mesh.clearHiddenFaces();
		Block block = this.world.getBlockAt(this.x+1, this.y, this.z);
		if (block != null && (!block.isTransparent() || (this.transparent && block.getType().equals(this.type))) && block.yOffset >= this.yOffset){
			mesh.addHiddenFace(2);
			mesh.addHiddenFace(3);
		}
		block = this.world.getBlockAt(this.x, this.y+1, this.z);
		if (block != null && (!block.isTransparent() || (this.transparent && block.getType().equals(this.type)))){
			mesh.addHiddenFace(8);
			mesh.addHiddenFace(9);
		}
		block = this.world.getBlockAt(this.x, this.y, this.z+1);
		if (block != null && (!block.isTransparent() || (this.transparent && block.getType().equals(this.type))) && block.yOffset >= this.yOffset){
			mesh.addHiddenFace(4);
			mesh.addHiddenFace(5);
		}
		block = this.world.getBlockAt(this.x-1, this.y, this.z);
		if (block != null && (!block.isTransparent() || (this.transparent && block.getType().equals(this.type))) && block.yOffset >= this.yOffset){
			mesh.addHiddenFace(6);
			mesh.addHiddenFace(7);
		}
		block = this.world.getBlockAt(this.x, this.y-1, this.z);
		if (block != null && (!block.isTransparent() || (this.transparent && block.getType().equals(this.type)))){
			mesh.addHiddenFace(10);
			mesh.addHiddenFace(11);
		}
		block = this.world.getBlockAt(this.x, this.y, this.z-1);
		if (block != null && (!block.isTransparent() || (this.transparent && block.getType().equals(this.type))) && block.yOffset >= this.yOffset){
			mesh.addHiddenFace(0);
			mesh.addHiddenFace(1);
		}
	}

	/**
	 * Block is declared like this:
	 * F  F  R  R  B  B  L  L  D  D  U  U
	 * 00 01 02 03 04 05 06 07 08 09 10 11
	 */
	public Mesh getMesh(){
		if (this.mesh != null) return this.mesh;

		Block top = this.world.getBlockAt(this.x, this.y-1, this.z);
		if ((top == null || !top.isLiquid()) && this.liquid){
			this.yOffset = LIQUID_OFFSET;
		} else {
			this.yOffset = 0;
		}

		if (this.sprite){
			this.mesh = new Mesh(new Point3D[]{
				new Point3D(this.x, this.y, this.z), new Point3D(this.x, 1+this.y, this.z), new Point3D(1+this.x, 1+this.y, this.z),
				new Point3D(1+this.x, this.y, this.z), new Point3D(this.x, this.y, 1+this.z), new Point3D(this.x, 1+this.y, 1+this.z),
				new Point3D(1+this.x, 1+this.y, 1+this.z), new Point3D(1+this.x, this.y, 1+this.z)}, new int[][]{
					{0, 1, 6}, {0, 6, 7}, {4, 5, 2}, {4, 2, 3}
			}, null, Atlas.MAIN_ATLAS.getImages().get(this.type), Atlas.MAIN_ATLAS.getBlockFaces().get(this.type), new Point2D[]{
				new Point2D(0, 0), new Point2D(0, 1), new Point2D(1, 1), new Point2D(1, 0)
			}, new int[][]{
				{0, 1, 2}, {0, 2, 3}, {0, 1, 2}, {0, 2, 3}
			});
			this.mesh.setShowAllFaces(true);
		} else {
			this.mesh = new Mesh(new Point3D[]{
				new Point3D(this.x, this.y+this.yOffset, this.z), new Point3D(this.x, 1+this.y, this.z), new Point3D(1+this.x, 1+this.y, this.z),
				new Point3D(1+this.x, this.y+this.yOffset, this.z), new Point3D(this.x, this.y+this.yOffset, 1+this.z), new Point3D(this.x, 1+this.y, 1+this.z),
				new Point3D(1+this.x, 1+this.y, 1+this.z), new Point3D(1+this.x, this.y+this.yOffset, 1+this.z)}, new int[][]{
					{0, 1, 2}, {0, 2, 3}, {3, 2, 6},
					{3, 6, 7}, {7, 6, 5}, {7, 5, 4},
					{4, 5, 1}, {4, 1, 0}, {1, 5, 6},
					{1, 6, 2}, {4, 0, 3}, {4, 3, 7}
			}, null, Atlas.MAIN_ATLAS.getImages().get(this.type), Atlas.MAIN_ATLAS.getBlockFaces().get(this.type), new Point2D[]{
				new Point2D(0, 0), new Point2D(0, 1), new Point2D(1, 1), new Point2D(1, 0)
			}, new int[][]{
				{0, 1, 2}, {0, 2, 3}, {0, 1, 2}, {0, 2, 3},
				{0, 1, 2}, {0, 2, 3}, {0, 1, 2}, {0, 2, 3},
				{0, 1, 2}, {0, 2, 3}, {0, 1, 2}, {0, 2, 3},
				{0, 1, 2}, {0, 2, 3}, {0, 1, 2}, {0, 2, 3}
			});
			this.mesh.setSkipCondition(cam -> {
				Point3D pos = new Point3D(this.x, this.y, this.z);
				Point3D camPos = new Point3D(cam.getPosition().getX(), cam.getPosition().getY(), cam.getPosition().getZ());
				return pos.distance(camPos) > ChunkManager.RENDER_DISTANCE*Chunk.CHUNK_SIZE;
			});
		}
		this.mesh.setTransparentProcessing(this.transparent);
		this.mesh.setShowAllFaces(this.transparent);
		return this.mesh;
	}

	public void removeMesh(){
		this.mesh = null;
	}

	public boolean isTransparent(){
		return this.transparent;
	}

	public boolean isLiquid(){
		return this.liquid;
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

	public String getType(){
		return this.type;
	}

	public void setYOffset(double value){
		this.yOffset = value;
	}
}