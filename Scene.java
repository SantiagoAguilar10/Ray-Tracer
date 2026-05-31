import java.util.ArrayList;
import java.util.List;

public class Scene {

    private List<Object3D> objects; // Spheres and other non-triangle objects
    private List<Triangle> triangles; // Collected separately for BVH
    private List<Light> lights;
    private BVHNode bvh; // Built once when buildBVH() is called

    private final double nearplane = 1e-6;
    private final double farplane  = 1000.0;

    public Scene() {
        objects   = new ArrayList<>();
        triangles = new ArrayList<>();
        lights    = new ArrayList<>();
    }

    public void addObject(Object3D object) {
        if (object instanceof Triangle) {
            triangles.add((Triangle) object); // Route triangles to BVH
        } else {
            objects.add(object); // Spheres etc. stay in the regular list
        }
    }

    public void addLight(Light light)    { lights.add(light); }
    public List<Object3D> getObjects()   { return objects; }
    public List<Light> getLights()       { return lights; }

    /**
     * Builds the BVH over all triangles added so far.
     * Call this ONCE after loading all OBJ files and before rendering.
     */
    public void buildBVH() {
        if (!triangles.isEmpty()) {
            bvh = new BVHNode(new ArrayList<>(triangles));
            System.out.println("BVH built over " + triangles.size() + " triangles.");
        }
    }

    public Intersection intersect(Ray ray) {
        Intersection closest = null;
        double minDistance   = Double.POSITIVE_INFINITY;

        // Test non-triangle objects (spheres) directly
        for (Object3D object : objects) {
            Intersection hit = object.intersect(ray);
            if (hit != null) {
                double t = hit.getT();
                if (hit.isInside() && object.getRefractivity() > 0.0) continue;
                if (t > nearplane && t < farplane && t < minDistance) {
                    minDistance = t;
                    closest     = hit;
                }
            }
        }

        // Test triangles via BVH
        if (bvh != null) {
            Intersection bvhHit = bvh.intersect(ray, nearplane, minDistance);
            if (bvhHit != null && bvhHit.getT() < minDistance) {
                closest = bvhHit;
            }
        }

        return closest;
    }
}