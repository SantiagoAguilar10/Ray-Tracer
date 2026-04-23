public class Vector3D {

    // Attributes
    private double x, y, z;

    //Constructor
    public Vector3D(double x, double y, double z) {
        setX(x);
        setY(y);
        setZ(z);
    }

    // Getters
    public double getX() { return x;}
    public double getY() { return y;}
    public double getZ() { return z;}

    // Setters
    public void setX(double x) { this.x = x;}
    public void setY(double y) { this.y = y;}
    public void setZ(double z) { this.z = z;}

    // Operations
    public Vector3D add(Vector3D other) {
        return new Vector3D(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    public Vector3D substract(Vector3D other) {
        return new Vector3D(this.x - other.x, this.y - other.y, this.z - other.z);
    }

    public Vector3D scale(double scalar) {
        return new Vector3D(this.x * scalar, this.y * scalar, this.z * scalar);
    }

    public double dotProduct(Vector3D other) {
        return this.x * other.x + this.y * other.y + this.z * other.z;
    }

    public Vector3D crossProduct(Vector3D other) {
        double crossX = this.y * other.z - this.z * other.y;
        double crossY = this.z * other.x - this.x * other.z;
        double crossZ = this.x * other.y - this.y * other.x;
        return new Vector3D(crossX, crossY, crossZ);
    }

    // Magnitude + Normalization
    public double magnitude() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public Vector3D normalize() {
        double mag = magnitude();
        if (mag == 0) {
            throw new ArithmeticException("Cannot normalize a zero vector");
        }
        return new Vector3D(x / mag, y / mag, z / mag);
    }
}