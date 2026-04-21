public class Intersection {

    // Attributes
    private double t;
    private Vector3D point;
    private Vector3D normal;
    private Object3D object;

    // Constructor
    public Intersection(double t, Vector3D point, Vector3D normal, Object3D object) {
        this.t = t;
        this.point = point;
        this.normal = normal;
        this.object = object;
    }

    // Getters
    public double getT() { return t;}
    public Vector3D getPoint() { return point;}
    public Vector3D getNormal() { return normal;}
    public Object3D getObject() { return object;}

    public Vector3D getColor() { return object.getColor();}
    
    // Setters
    public void setT(double t) { this.t = t;}
    public void setPoint(Vector3D point) { this.point = point;}
    public void setNormal(Vector3D normal) { this.normal = normal;}
    public void setObject(Object3D object) { this.object = object;}

}