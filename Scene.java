import java.util.ArrayList;
import java.util.List;

public class Scene {

    private List<Object3D> objects;
    private List<PointLight> lights;

    public Scene() {
        objects = new ArrayList<>();
        lights  = new ArrayList<>();
    }

    public void addObject(Object3D object) { objects.add(object); }
    public void addLight(PointLight light)  { lights.add(light); }

    public List<Object3D> getObjects() { return objects; }
    public List<PointLight> getLights() { return lights; }

    private final double nearplane = 1e-6;
    private final double farplane  = 1000.0;

    public Intersection intersect(Ray ray) {
        Intersection closestIntersection = null;
        double minDistance = Double.POSITIVE_INFINITY;

        for (Object3D object : objects) {
            Intersection intersection = object.intersect(ray);
            if (intersection != null) {
                double t = intersection.getT();
                if (t > nearplane && t < farplane && t < minDistance) {
                    minDistance = t;
                    closestIntersection = intersection;
                }
            }
        }
        return closestIntersection;
    }
}