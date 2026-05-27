public abstract class Object3D {
    // Abstract class for 3D objects in the scene

    private Vector3D position;
    private Vector3D color;
    private double shininess;
    private double reflectivity;
    private double specularStrength;

    // Constructor
    public Object3D (Vector3D position, Vector3D color, double shininess, double reflectivity, double specularStrength) {
        this.position = position;
        this.color = color;
        this.shininess = shininess;
        this.reflectivity = reflectivity;
        this.specularStrength = specularStrength;
    }

    // Compatible constructor for Objects without shininess and reflectivity
    // Without specularStrength
    public Object3D(Vector3D position, Vector3D color, double shininess, double reflectivity) {
        this(position, color, shininess, reflectivity, 0.3);
    }

    // Without shininess and reflectivity
    public Object3D(Vector3D position, Vector3D color) {
        this(position, color, 32.0, 0.0, 0.3);
    }

    // Getters and setters
    public Vector3D getPosition() { return position;}
    public Vector3D getColor() { return color;}

    public double getShininess() { return shininess; }
    public void setShininess(double shininess) { this.shininess = shininess; }

    public void setPosition(Vector3D position) { this.position = position; }
    public void setColor(Vector3D color) { this.color = color;}

    public double getReflectivity() { return reflectivity;}
    public void setReflectivity(double reflectivity) { this.reflectivity = reflectivity;}

    public double getSpecularStrength() { return specularStrength;}
    public void setSpecularStrength(double specularStrength) { this.specularStrength = specularStrength;}

    // Abstract method to compute intersection with a ray
    public abstract Intersection intersect(Ray ray);

}