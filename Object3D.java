public abstract class Object3D {
    // Abstract class for 3D objects in the scene

    private Vector3D position;
    private Vector3D color;

    // Constructor
    public Object3D (Vector3D position, Vector3D color) {
        this.position = position;
        this.color = color;
    }

    // Getters and setters
    public Vector3D getPosition() { return position;}
    public Vector3D getColor() { return color;}

    public void setPosition(Vector3D position) { this.position = position; }
    public void setColor(Vector3D color) { this.color = color;}

    // Abstract method to compute intersection with a ray
    public abstract Intersection intersect(Ray ray);

}   