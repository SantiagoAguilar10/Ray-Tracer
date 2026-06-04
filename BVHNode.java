import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BVHNode {

    private AABB bounds;
    private BVHNode left;
    private BVHNode right;
    private List<Triangle> triangles; // Non-null only in leaf nodes

    private static final int LEAF_SIZE = 4; // Max triangles per leaf

    /**
     * Builds a BVH subtree over the given list of triangles using median split.
     */
    public BVHNode(List<Triangle> tris) {

        // Compute bounds over all triangles in this node
        bounds = tris.get(0).getBounds();
        for (int i = 1; i < tris.size(); i++) {
            bounds = bounds.expand(tris.get(i).getBounds());
        }

        // Leaf condition — few enough triangles, store them directly
        if (tris.size() <= LEAF_SIZE) {
            this.triangles = new ArrayList<>(tris);
            this.left = null;
            this.right = null;
            return;
        }

        // Pick the longest axis to split on
        Vector3D extent = bounds.extent();
        int axis; // 0 = X, 1 = Y, 2 = Z
        if (extent.getX() >= extent.getY() && extent.getX() >= extent.getZ()) {
            axis = 0;
        } else if (extent.getY() >= extent.getZ()) {
            axis = 1;
        } else {
            axis = 2;
        }

        // Sort triangles by their centroid along the chosen axis
        final int sortAxis = axis;
        tris.sort(Comparator.comparingDouble(t -> t.getBounds().centroid().getX() * (sortAxis == 0 ? 1 : 0)
                                                + t.getBounds().centroid().getY() * (sortAxis == 1 ? 1 : 0)
                                                + t.getBounds().centroid().getZ() * (sortAxis == 2 ? 1 : 0)));

        // Split at the median
        int mid = tris.size() / 2;
        this.left = new BVHNode(tris.subList(0, mid));
        this.right = new BVHNode(tris.subList(mid, tris.size()));
        this.triangles = null;
    }

    public AABB getBounds() { return bounds; }
    public boolean isLeaf() { return triangles != null; }

    /**
     * Traverses the BVH and returns the closest intersection with the ray.
     */
    public Intersection intersect(Ray ray, double tMin, double tMax) {

        // If the ray misses this node's bounding box, skip entirely
        if (!bounds.intersect(ray, tMin, tMax)) return null;

        // Leaf — test all triangles directly
        if (isLeaf()) {
            Intersection closest = null;
            double minT = tMax;

            for (Triangle tri : triangles) {
                Intersection hit = tri.intersect(ray);
                if (hit != null && hit.getT() > tMin && hit.getT() < minT) {
                    minT = hit.getT();
                    closest = hit;
                }
            }
            return closest;
        }

        // Internal node - recurse into both children, keep closest hit
        Intersection leftHit = left.intersect(ray, tMin, tMax);
        Intersection rightHit = right.intersect(ray, tMin, tMax);

        if (leftHit == null) return rightHit;
        if (rightHit == null) return leftHit;
        return leftHit.getT() < rightHit.getT() ? leftHit : rightHit;
    }
}