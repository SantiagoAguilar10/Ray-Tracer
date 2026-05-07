import java.util.List;

public class Triangle extends Object3D {

    private Vector3D v0, v1, v2;
    private Vector3D n0, n1, n2; // Per-vertex normals (may be null if not provided)
    private static final double EPSILON = 1e-6;

    // Constructor without vertex normals (flat shading fallback)
    public Triangle(Vector3D v0, Vector3D v1, Vector3D v2, Vector3D color) {
        super(v0, color);
        this.v0 = v0; this.v1 = v1; this.v2 = v2;
        this.n0 = null; this.n1 = null; this.n2 = null;
    }

    // Constructor with vertex normals (Phong shading)
    public Triangle(Vector3D v0, Vector3D v1, Vector3D v2,
                    Vector3D n0, Vector3D n1, Vector3D n2, Vector3D color) {
        super(v0, color);
        this.v0 = v0; this.v1 = v1; this.v2 = v2;
        this.n0 = n0; this.n1 = n1; this.n2 = n2;
    }

    public List<Vector3D> getVertices() { return List.of(v0, v1, v2); }

    public boolean hasVertexNormals() { return n0 != null; }

    @Override
    public Intersection intersect(Ray ray) {

        Vector3D O = ray.getOrigin();
        Vector3D D = ray.getDirection();

        Vector3D edge1 = v1.substract(v0);
        Vector3D edge2 = v2.substract(v0);

        Vector3D P = D.crossProduct(edge2);
        double det = edge1.dotProduct(P);

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

        // Interpolate normal using barycentric coordinates (u, v, 1-u-v)
        Vector3D normal;
        if (hasVertexNormals()) {
            double w = 1.0 - u - v; // Weight of v0
            normal = new Vector3D(
                w * n0.getX() + u * n1.getX() + v * n2.getX(),
                w * n0.getY() + u * n1.getY() + v * n2.getY(),
                w * n0.getZ() + u * n1.getZ() + v * n2.getZ()
            ).normalize();
        } else {
            // Fallback to flat face normal
            normal = edge1.crossProduct(edge2).normalize();
        }

        return new Intersection(t, point, normal, this);
    }
}