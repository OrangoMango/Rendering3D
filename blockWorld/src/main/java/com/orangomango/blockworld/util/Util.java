package com.orangomango.blockworld.util;

import javafx.geometry.Point3D;

import com.orangomango.blockworld.model.Chunk;

public class Util{
	public static String formatTime(double value, boolean amTime){
		// 0 -> 00:00 PM
		// 1 -> 12:00 PM
		if (!amTime){
			value = 1-value;
		}
		int hours = (int)(value*12);
		int minutes = (int)((value*12-hours)*59);
		if (!amTime){
			hours += 12;
		}
		return String.format("%02d:%02d", hours, minutes);
	}

	public static Point3D getChunkPos(Point3D pos){
		int chunkX = (int)(pos.getX()/Chunk.CHUNK_SIZE);
		int chunkY = (int)(pos.getY()/Chunk.CHUNK_SIZE);
		int chunkZ = (int)(pos.getZ()/Chunk.CHUNK_SIZE);
		return new Point3D(chunkX, chunkY, chunkZ);
	}
}