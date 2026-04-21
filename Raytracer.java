public class Raytracer {

    private Scene scene;
    private Camera camera;
    private Vector3D background = new Vector3D(1, 1, 1); // Bg Color (white)

    public Raytracer(Scene scene, Camera camera) {
        this.scene = scene;
        this.camera = camera;
    }

    public Camera getCamera() { return camera;}
    public Scene getScene() { return scene;}

    public Vector3D shade(Intersection hit) {
        return hit.getObject().getColor();
    }

    public Vector3D traceRay(Ray ray) {

        // Find the closest intersection of the ray with the scene
        Intersection hit = scene.intersect(ray);

        if (hit == null) return background;
        return hit.getObject().getColor();
            
    }    

}