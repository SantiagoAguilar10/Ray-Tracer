import java.io.File;
import java.util.List;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class Raytracer {

    private Scene scene;
    private Camera camera;
    private Vector3D background = new Vector3D(0, 0, 0); // Bg Color (white)

    public Raytracer(Scene scene, Camera camera) {
        this.scene = scene;
        this.camera = camera;
    }

    public Camera getCamera() { return camera;}
    public Scene getScene() { return scene;}

    public Vector3D shade(Intersection hit) {
        Vector3D color = new Vector3D(0, 0, 0);
        Vector3D objectColor = hit.getObject().getColor();
        Vector3D normal = hit.getNormal();
        Vector3D hitPoint = hit.getPoint();
        double shininess = hit.getObject().getShininess();
        Vector3D cameraPos = camera.getPosition();

        double ambientLight = 0.15;

        for (Light light : scene.getLights()) {
            Vector3D contribution = light.shade(hitPoint, normal, objectColor, ambientLight, cameraPos, shininess);
            color = new Vector3D(
                color.getX() + contribution.getX(),
                color.getY() + contribution.getY(),
                color.getZ() + contribution.getZ()
            );
        }

        return new Vector3D(
            Math.min(color.getX(), 1.0),
            Math.min(color.getY(), 1.0),
            Math.min(color.getZ(), 1.0)
        );
    }

    public Vector3D traceRay(Ray ray) {

        // Find the closest intersection of the ray with the scene
        Intersection hit = scene.intersect(ray);

        if (hit == null) return background;
        return shade(hit); 
    }  
    


    public static void main(String[] args) throws Exception {

        // Image dimensions
        int width = 1200;
        int height = 900;

        // Camera
        Camera camera = new Camera(new Vector3D(0, 0.3, 2), 60, (double)width / height);
        camera.setBackgroundColor(new Vector3D(0, 0, 0));

        // Scene
        Scene scene = new Scene();
        
        scene.addObject(new Sphere(new Vector3D(-5, 4, -7), 1, new Vector3D(1, 0, 0)));
        scene.addObject(new Sphere(new Vector3D(5,3, -10), 1 ,new Vector3D(0, 0, 1)));
        
        // Placing the light above of the model, in front of the camera.
        scene.addLight(new PointLight(
            new Vector3D(0, 0.3, 1.5),  // Position.
            new Vector3D(1.0, 1.0, 1.0),  // White light.
            1.0                            // Full intensity.
        ));

        List<Triangle> tea = OBJReader.load("teapot.obj", new Vector3D(1, 0.5, 1), new Vector3D(0, -0.5, -3));
        OBJReader.printBounds(tea);
        for (Triangle t : tea) {
            scene.addObject(t);
        }

        List<Triangle> tris = OBJReader.load("CottonCandy.obj", new Vector3D(1, 0.5, 1), new Vector3D(-2, 0, -1));
        OBJReader.printBounds(tris);
        for (Triangle t : tris) {
            scene.addObject(t);
        }

        // Raytracer
        Raytracer raytracer = new Raytracer(scene, camera);

        // Image
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Render
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                Ray ray = camera.generateRay(x, y, width, height);
                Vector3D color = raytracer.traceRay(ray);

                // Convert color to RGB
                int r = (int)(255 * color.getX());
                int g = (int)(255 * color.getY());
                int b = (int)(255 * color.getZ());

                int rgb = (r << 16) | (g << 8) | b;

                image.setRGB(x, y, rgb);
            }
        }

        // Save Image
        ImageIO.write(image, "png", new File("output.png"));

        System.out.println("Imagen generada: output.png");
    }
}
