public class Triangle extends Object3D {

    private Vector3D v0, v1, v2;
    private static final double EPSILON = 1e-6; // Small constant to handle near-zero values

    public Triangle(Vector3D v0, Vector3D v1, Vector3D v2, Vector3D color) {
        super(v0, color);
        this.v0 = v0;
        this.v1 = v1;
        this.v2 = v2;
    }

    @Override
    public Intersection intersect(Ray ray) {

        Vector3D O = ray.getOrigin();
        Vector3D D = ray.getDirection();

        Vector3D edge1 = v1.substract(v0);
        Vector3D edge2 = v2.substract(v0);

        Vector3D P = D.crossProduct(edge2);
        double det = edge1.dotProduct(P);

        // If the determinant is near zero, the ray lies in the plane of the triangle or is parallel to it.
        if (Math.abs(det) < EPSILON) return null;

        double invDet = 1.0 / det;

        Vector3D T = O.substract(v0);

        double u = T.dotProduct(P) * invDet;
        if (u < 0 || u > 1) return null;

        Vector3D Q = T.crossProduct(edge1);

        double v = D.dotProduct(Q) * invDet;
        if (v < 0 || (u + v) > 1) return null;

        double t = edge2.dotProduct(Q) * invDet;
        if (t < EPSILON) return null;

        Vector3D point = ray.pointAlongRay(t);

        // Normal del triángulo
        Vector3D normal = edge1.crossProduct(edge2).normalize();

        return new Intersection(t, point, normal, this);
    }
}