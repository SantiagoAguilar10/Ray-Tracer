import java.util.ArrayList;

public class Scene {
    // Container of all objects, lights, and camera in the scene

    private ArrayList<Object3D> objects;
    private Camera camera;

    public Scene() {
        this.objects = new ArrayList<>();
        this.camera = null;
    }

    public void addObject(Object3D object) {
        objects.add(object);
    }
    
}
