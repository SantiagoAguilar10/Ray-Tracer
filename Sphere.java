public class Sphere extends Object3D {

    private double radius;

    public Sphere(Vector3D position, Vector3D color, double radius) {
        super(position, color);
        this.radius = radius;
    }

    public double getRadius() { return radius;}
    public void setRadius(double radius) { this.radius = radius;}

    @Override
    public Intersection intersect(Ray ray) {
        Vector3D O = ray.getOrigin();
        Vector3D D = ray.getDirection();
        Vector3D C = getPosition();

        Vector3D OC = O.subtract(C);

        double a = D.dotProduct(D);
        double b = 2 * D.dotProduct(OC);
        double c = OC.dotProduct(OC) - radius * radius;

        double discriminant = b * b - 4 * a * c;

        if (discriminant < 0) {
            return null; // No intersection
        } else {
            double sqrtDisc = Math.sqrt(discriminant);
            double t1 = (-b - sqrtDisc) / (2 * a);
            double t2 = (-b + sqrtDisc) / (2 * a);

            double t = (t1 > 0) ? t1 : ((t2 > 0) ? t2 : -1);
            if (t < 0) return null; // Both intersections are behind the ray

            Vector3D point = ray.pointAlongRay(t);
            Vector3D normal = point.subtract(C).normalize();

            return new Intersection(t, point, normal, this);
        }
    }

}