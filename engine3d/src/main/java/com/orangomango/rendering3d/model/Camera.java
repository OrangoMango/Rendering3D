package com.orangomango.rendering3d.model;

import javafx.geometry.Point3D;
import java.util.function.Consumer;
import com.orangomango.rendering3d.Engine3D;

public class Camera{
	private Point3D position;
	private double rx, ry;
	public double[][] depthBuffer;
	private double aspectRatio, fov, zFar, zNear;
	private int width, height;
	private boolean stateChanged = true;
	private double[][] savedMatrix = null;
	private Point3D[][] frustum;
	private double[][] projectionMatrix = null;
	private Consumer<Point3D> onPositionChanged;
	private Point3D lastPosition;

	public Camera(Point3D pos, int w, int h, double fov, double zFar, double zNear){
		this.position = pos;
		this.width = w;
		this.height = h;
		this.depthBuffer = new double[w][h];
		this.aspectRatio = (double)h/w;
		this.fov = fov;
		this.zNear = zNear;
		this.zFar = zFar;
		this.lastPosition = this.position;
	}

	public void setOnPositionChanged(Consumer<Point3D> c){
		this.onPositionChanged = c;
	}

	private void triggerPositionChange(){
		this.stateChanged = true;
		this.frustum = null;
		if (!this.position.equals(this.lastPosition)){
			this.lastPosition = this.position;
			if (this.onPositionChanged != null){
				this.onPositionChanged.accept(this.position);
			}
		}
	}

	private void rebuildCamera(){
		this.frustum = null;
		this.projectionMatrix = null;
	}

	public double getWidth(){
		return this.width;
	}

	public double getHeight(){
		return this.height;
	}

	public void setZnear(double zn){
		this.zNear = zn;
		rebuildCamera();
	}

	public double getZnear(){
		return this.zNear;
	}

	public void setZfar(double zf){
		this.zFar = zf;
		rebuildCamera();
	}

	public double getZfar(){
		return this.zFar;
	}

	public double getAspectRatio(){
		return this.aspectRatio;
	}

	public void setFov(double fov){
		this.fov = fov;
		rebuildCamera();
	}

	public double getFov(){
		return this.fov;
	}

	public void setPosition(Point3D p){
		this.position = p;
		triggerPositionChange();
	}

	public Point3D getPosition(){
		return this.position;
	}

	public void move(Point3D m){
		this.position = this.position.add(m);
		triggerPositionChange();
	}

	public void reset(){
		this.position = Point3D.ZERO;
		this.rx = 0;
		this.ry = 0;
		triggerPositionChange();
	}

	public void clearDepthBuffer(){
		for (int i = 0; i < this.width; i++){
			for (int j = 0; j < this.height; j++){
				this.depthBuffer[i][j] = 0;
			}
		}
	}

	public void lookAtCenter(){
		setRx(Math.atan(this.position.getY()/this.position.getZ()));
		setRy(Math.atan2(this.position.getZ(), this.position.getX())+Math.PI/2);
	}

	public double getRx(){
		return this.rx;
	}
	
	public void setRx(double rx){
		if (this.rx != rx){
			this.stateChanged = true;
			this.frustum = null;
		}
		this.rx = Math.max(-Math.PI/2, Math.min(Math.PI/2, rx));
	}
	
	public double getRy(){
		return this.ry;
	}
	
	public void setRy(double ry){
		if (this.ry != ry){
			this.stateChanged = true;
			this.frustum = null;
		}
		this.ry = ry;
	}

	public Point3D[][] getViewFrustum(){
		if (this.frustum != null){
			return this.frustum;
		}

		double stepX = Math.cos(this.rx)*Math.cos(this.ry+Math.PI/2);
		double stepY = -Math.sin(this.rx);
		double stepZ = Math.cos(this.rx)*Math.sin(this.ry+Math.PI/2);
		Point3D direction = new Point3D(stepX, stepY, stepZ);

		stepX = Math.cos(this.rx+Math.PI/2)*Math.cos(this.ry+Math.PI/2);
		stepY = -Math.sin(this.rx+Math.PI/2);
		stepZ = Math.cos(this.rx+Math.PI/2)*Math.sin(this.ry+Math.PI/2);
		Point3D vDirection = new Point3D(stepX, stepY, stepZ);

		Point3D eye = new Point3D(this.position.getX(), this.position.getY(), this.position.getZ());

		double nearPlaneHeight = this.zNear*Math.tan(this.fov/2)*2;
		double nearPlaneWidth = nearPlaneHeight/this.aspectRatio;

		Point3D horizontalDirection = direction.crossProduct(new Point3D(0, -1, 0)).normalize().multiply(nearPlaneWidth/2);
		Point3D verticalDirection = horizontalDirection.crossProduct(direction).normalize().multiply(nearPlaneHeight/2);
		
		// Points
		Point3D frontPoint = eye.add(direction.multiply(this.zNear));
		Point3D backPoint = eye.add(direction.multiply(this.zFar));

		Point3D rightPoint = frontPoint.add(horizontalDirection);
		Point3D leftPoint = frontPoint.add(horizontalDirection.multiply(-1));
		Point3D topPoint = frontPoint.add(verticalDirection);
		Point3D bottomPoint = frontPoint.add(verticalDirection.multiply(-1));

		// Normals
		Point3D frontNormal = frontPoint.subtract(eye).multiply(-1).normalize();
		Point3D backNormal = backPoint.subtract(eye).normalize();

		Point3D rightNormal = rightPoint.subtract(eye).crossProduct(vDirection).normalize();
		Point3D leftNormal = leftPoint.subtract(eye).crossProduct(vDirection).normalize().multiply(-1);

		stepX = Math.cos(this.rx+this.fov/2+Math.PI/2)*Math.cos(this.ry+Math.PI/2);
		stepY = -Math.sin(this.rx+this.fov/2+Math.PI/2);
		stepZ = Math.cos(this.rx+this.fov/2+Math.PI/2)*Math.sin(this.ry+Math.PI/2);
		Point3D topNormal = new Point3D(stepX, stepY, stepZ);

		stepX = Math.cos(this.rx-this.fov/2-Math.PI/2)*Math.cos(this.ry+Math.PI/2);
		stepY = -Math.sin(this.rx-this.fov/2-Math.PI/2);
		stepZ = Math.cos(this.rx-this.fov/2-Math.PI/2)*Math.sin(this.ry+Math.PI/2);
		Point3D bottomNormal = new Point3D(stepX, stepY, stepZ);

		this.frustum = new Point3D[][]{{frontNormal, frontPoint}, {backNormal, backPoint}, {rightNormal, rightPoint}, {leftNormal, leftPoint}, {topNormal, topPoint}, {bottomNormal, bottomPoint}};

		return this.frustum;
	}

	public boolean isVisible(MeshTriangle triangle){
		Point3D[][] frustum = getViewFrustum();
		Point3D v1 = triangle.getVertex1().getPosition();
		Point3D v2 = triangle.getVertex2().getPosition();
		Point3D v3 = triangle.getVertex3().getPosition();

		for (Point3D[] plane : frustum){
			double d1 = Engine3D.distanceToPlane(plane[0], plane[1], v1, plane[0].multiply(-1));
			double d2 = Engine3D.distanceToPlane(plane[0], plane[1], v2, plane[0].multiply(-1));
			double d3 = Engine3D.distanceToPlane(plane[0], plane[1], v3, plane[0].multiply(-1));
			if (d1 > 0 && d2 > 0 && d3 > 0){
				return false;
			}
		}
		return true;
	}

	public double[][] getViewMatrix(){
		if (this.stateChanged){
			this.savedMatrix = Engine3D.multiply(Engine3D.getTranslation(-this.position.getX(), -this.position.getY(), -this.position.getZ()),
				Engine3D.multiply(Engine3D.getRotateY(-getRy()), Engine3D.getRotateX(-getRx())));
			this.stateChanged = false;
		}
		return this.savedMatrix;
	}

	public double[][] getProjectionMatrix(){
		if (this.projectionMatrix != null){
			return this.projectionMatrix;
		}

		this.projectionMatrix = new double[][]{
			{aspectRatio*1/Math.tan(fov/2), 0, 0, 0},
			{0, 1/Math.tan(fov/2), 0, 0},
			{0, 0, 2/(zFar-zNear), -2*zNear/(zFar-zNear)-1},
			{0, 0, 1, 0}
		};
		return this.projectionMatrix;
	}

	@Override
	public String toString(){
		return String.format("Cx: %.2f Cy: %.2f Cz: %.2f | Rx: %.2f Ry: %.2f", this.position.getX(), this.position.getY(), this.position.getZ(), this.rx, this.ry);
	}
}
