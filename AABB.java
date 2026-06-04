public class AABB {

    private Vector3D min; // Corner with smallest x, y, z
    private Vector3D max; // Corner with largest x, y, z

    public AABB(Vector3D min, Vector3D max) {
        this.min = min;
        this.max = max;
    }

    public Vector3D getMin() { return min; }
    public Vector3D getMax() { return max; }

    /**
     * Returns the center point of this bounding box.
     * Used by BVH to sort objects along an axis.
     */
    public Vector3D centroid() {
        return new Vector3D(
            (min.getX() + max.getX()) * 0.5,
            (min.getY() + max.getY()) * 0.5,
            (min.getZ() + max.getZ()) * 0.5
        );
    }

    /**
     * Returns the length of the box along each axis.
     * BVH uses this to pick the longest axis to split on.
     */
    public Vector3D extent() {
        return max.substract(min);
    }

    /**
     * Expands this AABB to also contain another AABB.
     * Used when building the BVH to compute parent node bounds.
     */
    public AABB expand(AABB other) {
        return new AABB(
            min.min(other.min),
            max.max(other.max)
        );
    }

    /**
     * Ray-AABB intersection using the slab method.
     * Tests the ray against the three axis-aligned slab pairs and
     * checks whether the resulting t-intervals overlap.
     *
     * Returns true if the ray hits the box within [tMin, tMax].
     */
    public boolean intersect(Ray ray, double tMin, double tMax) {
        Vector3D origin = ray.getOrigin();
        Vector3D direction = ray.getDirection();

        // Test each axis slab
        for (int axis = 0; axis < 3; axis++) {
            double o, d, bMin, bMax;

            if (axis == 0) {
                o = origin.getX(); d = direction.getX();
                bMin = min.getX(); bMax = max.getX();
            } else if (axis == 1) {
                o = origin.getY(); d = direction.getY();
                bMin = min.getY(); bMax = max.getY();
            } else {
                o = origin.getZ(); d = direction.getZ();
                bMin = min.getZ(); bMax = max.getZ();
            }

            // Avoid division by zero for rays parallel to this slab
            if (Math.abs(d) < 1e-10) {
                // Ray is parallel - if origin is outside the slab, no hit
                if (o < bMin || o > bMax) return false;
                continue;
            }

            double invD = 1.0 / d;
            double t0 = (bMin - o) * invD;
            double t1 = (bMax - o) * invD;

            // Ensure t0 is the near hit and t1 is the far hit
            if (t0 > t1) { double tmp = t0; t0 = t1; t1 = tmp; }

            tMin = Math.max(tMin, t0);
            tMax = Math.min(tMax, t1);

            // If the interval collapses, the ray misses the box
            if (tMax <= tMin) return false;
        }
        return true;
    }
}
