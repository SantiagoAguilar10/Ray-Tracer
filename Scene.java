import java.util.ArrayList;
import java.util.List;

public class Scene {

    private List<Object3D> objects;

    public Scene() { objects = new ArrayList<>();}

    public void addObject(Object3D object) { objects.add(object);}

    public List<Object3D> getObjects() { return objects;}

    public Intersection intersect(Ray ray) {
        Intersection closestIntersection = null;
        double minDistance = Double.POSITIVE_INFINITY;

        for (Object3D object : objects) {
            Intersection intersection = object.intersect(ray);

            if (intersection != null) {
                double t = intersection.getT();

                // Prevents Intersections Behind or Very Close (Numerical Noise)
                if (t > 1e-6 && t < minDistance) {
                    minDistance = t;
                    closestIntersection = intersection;
                }
            }
        }

        return closestIntersection;
    }
}