public abstract class Object3D {
    // Abstract class for 3D objects in the scene

    private Vector3D position;

    // Constructor
    public Object3D (Vector3D position, Vector3D color) {
        this.position = position;
    }

    // Getters and setters
    public Vector3D getPosition() { return position;}

    public void setPosition(Vector3D position) { this.position = position; }

    // Abstract method to compute intersection with a ray
    public abstract Intersection intersect(Ray ray);

}   

