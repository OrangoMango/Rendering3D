package com.orangomango.rendering3d.model;

import java.util.*;
import java.util.function.Predicate;

public class MeshGroup{
	private List<Mesh> meshes = new ArrayList<>();
	public Predicate<Camera> skipCondition;
	public String tag;

	public MeshGroup(List<Mesh> m){
		this.meshes = m;
	}

	public MeshGroup(Mesh mesh){
		this.meshes.add(mesh);
	}

	public List<Mesh> getMeshes(){
		return this.meshes;
	}

	public void updateMesh(List<Mesh> meshes){
		this.meshes = meshes;
	}
}
