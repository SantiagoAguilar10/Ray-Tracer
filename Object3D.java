public abstract class Object3D {
    // Abstract class for 3D objects in the scene

    private Vector3D position;
    private Vector3D color;
    private double shininess;

    // Constructor
    public Object3D (Vector3D position, Vector3D color, double shininess) {
        this.position = position;
        this.color = color;
        this.shininess = shininess;
    }

    // Compatible constructor for Objects without shininess
    public Object3D (Vector3D position, Vector3D color) {
        this(position, color, 32.0);
    }

    // Getters and setters
    public Vector3D getPosition() { return position;}
    public Vector3D getColor() { return color;}

    public double getShininess() { return shininess; }
    public void setShininess(double shininess) { this.shininess = shininess; }

    public void setPosition(Vector3D position) { this.position = position; }
    public void setColor(Vector3D color) { this.color = color;}

    // Abstract method to compute intersection with a ray
    public abstract Intersection intersect(Ray ray);

}   