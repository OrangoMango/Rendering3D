package com.orangomango.rendering3d.model;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;

import java.util.*;

public class MeshGroup{
	private List<Mesh> meshes = new ArrayList<>();
	public String tag;

	public MeshGroup(List<Mesh> m){
		this.meshes = m;
		setupGroup();
	}

	public MeshGroup(Mesh mesh){
		this.meshes.add(mesh);
		setupGroup();
	}
	
	private void setupGroup(){
		for (Mesh m : this.meshes){
			m.setMeshGroup(this);
		}
	}

	public List<Mesh> getMeshes(){
		return this.meshes;
	}

	public void updateMesh(List<Mesh> meshes){
		this.meshes = meshes;
		setupGroup();
	}
}
