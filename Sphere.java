public class Sphere extends Object3D {

    private double radius;

    // Minimal constructor
    public Sphere(Vector3D position, double radius,Vector3D color) {
        super(position, color);
        this.radius = radius;
    }

    // Full constructor
    public Sphere(Vector3D position, double radius, Vector3D color, double shininess, double reflectivity, double specularStrength, double refractivity, double refractiveIndex) {
        super(position, color, shininess, reflectivity, specularStrength, refractivity, refractiveIndex);
        this.radius = radius;
    }

    public double getRadius() { return radius;}
    public void setRadius(double radius) { this.radius = radius;}

    @Override
    public Intersection intersect(Ray ray) {
        Vector3D O = ray.getOrigin();
        Vector3D D = ray.getDirection();
        Vector3D C = getPosition();

        Vector3D OC = O.substract(C);

        double a = D.dotProduct(D);
        double b = 2 * D.dotProduct(OC);
        double c = OC.dotProduct(OC) - radius * radius;

        double discriminant = b * b - 4 * a * c;
        if (discriminant < 0) return null;

        double sqrtDisc = Math.sqrt(discriminant);
        double t1 = (-b - sqrtDisc) / (2 * a);
        double t2 = (-b + sqrtDisc) / (2 * a);

        double t;
        boolean inside;

        if (t1 > 1e-6) {
            // Ray hits front face from outside
            t = t1;
            inside = false;
        } else if (t2 > 1e-6) {
            // Ray is inside the sphere — hits back face
            t = t2;
            inside = true;
        } else {
            return null;
        }

        Vector3D point  = ray.pointAlongRay(t);
        // Flip normal if hitting from inside
        Vector3D normal = inside
            ? C.substract(point).normalize()  // Points inward
            : point.substract(C).normalize(); // Points outward

        return new Intersection(t, point, normal, this, inside);
    }

}