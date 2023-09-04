package com.orangomango.blockworld.util;

import javafx.geometry.Point3D;

import com.orangomango.blockworld.model.Chunk;

public class Util{
	public static Point3D getChunkPos(Point3D pos){
		int chunkX = (int)(pos.getX()/Chunk.CHUNK_SIZE);
		int chunkY = (int)(pos.getY()/Chunk.CHUNK_SIZE);
		int chunkZ = (int)(pos.getZ()/Chunk.CHUNK_SIZE);
		return new Point3D(chunkX, chunkY, chunkZ);
	}
}