package com.orangomango.blockworld.model;

import javafx.geometry.Point3D;
import javafx.geometry.Point2D;

import com.orangomango.rendering3d.model.Mesh;

public class Block{
	public static final double LIQUID_OFFSET = 0.2;
	public static final int MAX_LIGHT_INTENSITY = 15;

	private int x, y, z;
	private World world;
	private Mesh mesh;
	private String type;
	private double yOffset;
	private int light = MAX_LIGHT_INTENSITY;
	private BlockMesh blockMesh;

	public Block(Chunk chunk, int x, int y, int z, String type){
		this.world = chunk.getWorld();
		this.x = x+chunk.getX()*Chunk.CHUNK_SIZE;
		this.y = y+chunk.getY()*Chunk.CHUNK_SIZE;
		this.z = z+chunk.getZ()*Chunk.CHUNK_SIZE;
		this.type = type;
		this.blockMesh = Atlas.MAIN_ATLAS.getBlockMesh(this.type);
	}

	public Block(World world, int gx, int gy, int gz, String type){
		this.world = world;
		this.x = gx;
		this.y = gy;
		this.z = gz;
		this.type = type;
		this.blockMesh = Atlas.MAIN_ATLAS.getBlockMesh(this.type);
	}

	public void setLight(int intensity){
		this.light = intensity;
	}

	public void setupFaces(){
		if (isSprite() || this.mesh == null) return;
		this.mesh.clearHiddenFaces();
		Block block = this.world.getBlockAt(this.x+1, this.y, this.z);
		if (this.blockMesh.getCullingMap().get("R") && block != null && (!block.isTransparent() || (isTransparent() && block.getType().equals(this.type))) && block.yOffset <= this.yOffset){
			this.mesh.addHiddenFace(2);
			this.mesh.addHiddenFace(3);
		}
		block = this.world.getBlockAt(this.x, this.y+1, this.z);
		if (this.blockMesh.getCullingMap().get("D") && block != null && (!block.isTransparent() || (isTransparent() && block.getType().equals(this.type)))){
			this.mesh.addHiddenFace(8);
			this.mesh.addHiddenFace(9);
		}
		block = this.world.getBlockAt(this.x, this.y, this.z+1);
		if (this.blockMesh.getCullingMap().get("B") && block != null && (!block.isTransparent() || (isTransparent() && block.getType().equals(this.type))) && block.yOffset <= this.yOffset){
			this.mesh.addHiddenFace(4);
			this.mesh.addHiddenFace(5);
		}
		block = this.world.getBlockAt(this.x-1, this.y, this.z);
		if (this.blockMesh.getCullingMap().get("L") && block != null && (!block.isTransparent() || (isTransparent() && block.getType().equals(this.type))) && block.yOffset <= this.yOffset){
			this.mesh.addHiddenFace(6);
			this.mesh.addHiddenFace(7);
		}
		block = this.world.getBlockAt(this.x, this.y-1, this.z);
		if (this.blockMesh.getCullingMap().get("T") && block != null && (!block.isTransparent() || (isTransparent() && block.getType().equals(this.type))) && (!isLiquid() || block.isLiquid())){
			this.mesh.addHiddenFace(10);
			this.mesh.addHiddenFace(11);
		}
		block = this.world.getBlockAt(this.x, this.y, this.z-1);
		if (this.blockMesh.getCullingMap().get("F") && block != null && (!block.isTransparent() || (isTransparent() && block.getType().equals(this.type))) && block.yOffset <= this.yOffset){
			this.mesh.addHiddenFace(0);
			this.mesh.addHiddenFace(1);
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
		if ((top == null || !top.isLiquid()) && isLiquid()){
			this.yOffset = LIQUID_OFFSET;
		} else {
			this.yOffset = 0;
		}

		Point3D[] vertices = this.blockMesh.getVertices();
		int[][] faces = this.blockMesh.getFacesPoints();
		Point3D[] blockVertices = new Point3D[vertices.length];
		int[][] blockFaces = new int[faces.length][3];

		System.arraycopy(vertices, 0, blockVertices, 0, vertices.length);
		System.arraycopy(faces, 0, blockFaces, 0, faces.length);

		if (isSprite()){
			/*this.mesh = new Mesh(blockVertices, blockFaces,
				null, Atlas.MAIN_ATLAS.getImages().get(this.type), Atlas.MAIN_ATLAS.getBlockFaces().get(this.type), new Point2D[]{
				new Point2D(0, 0), new Point2D(0, 1), new Point2D(1, 1), new Point2D(1, 0)
			}, new int[][]{
				{0, 1, 2}, {0, 2, 3}, {0, 1, 2}, {0, 2, 3}
			});
			this.mesh.setShowAllFaces(true);*/
		} else {
			this.mesh = new Mesh(blockVertices, blockFaces, null, this.blockMesh.getImages(), this.blockMesh.getImageIndices(), this.blockMesh.getTex(), this.blockMesh.getFacesTex());
			this.mesh.setSkipCondition(cam -> {
				Point3D pos = new Point3D(this.x, this.y, this.z);
				return pos.distance(cam.getPosition()) > ChunkManager.RENDER_DISTANCE*Chunk.CHUNK_SIZE;
			});
		}
		this.mesh.translate(this.x, this.y+this.yOffset, this.z);
		this.mesh.build();
		this.mesh.setTransparentProcessing(isTransparent());
		this.mesh.setShowAllFaces(isTransparent());
		return this.mesh;
	}

	// Called once every frame
	public void update(){
		// Set light
		if (this.mesh != null){
			for (int i = 0; i < this.mesh.getTriangles().length; i++){
				this.mesh.getTriangles()[i].setLight((double)this.light/MAX_LIGHT_INTENSITY);
			}
		}
	}

	public void removeMesh(){
		this.mesh = null;
	}

	public boolean isTransparent(){
		return Atlas.MAIN_ATLAS.isTransparent(this.type);
	}

	public boolean isLiquid(){
		return Atlas.MAIN_ATLAS.isLiquid(this.type);
	}

	public boolean isSprite(){
		return Atlas.MAIN_ATLAS.isSprite(this.type);
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

	public int getId(){
		return Atlas.MAIN_ATLAS.getBlockId(this.type);
	}

	public void setYOffset(double value){
		this.yOffset = value;
	}
}