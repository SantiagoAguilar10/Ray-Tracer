import java.io.File;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

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
    


    public static void main(String[] args) throws Exception {

        // Image dimensions
        int width = 1200;
        int height = 900;

        // Camera
        Camera camera = new Camera(new Vector3D(0, 0, 0), 60,  (double)width / height);
        camera.setBackgroundColor(new Vector3D(0, 0, 0));

        // Scene
        Scene scene = new Scene();
        scene.addObject(new Sphere(new Vector3D(0, 0, -5), 1, new Vector3D(1, 0, 0)));
        scene.addObject(new Sphere(new Vector3D(4,0, -10), 1 ,new Vector3D(0, 0, 1)));
        scene.addObject(new Triangle(new Vector3D(-2, -1, -4), new Vector3D(-1, 1, -4), new Vector3D(-3, 1, -4), new Vector3D(0, 1, 0)));

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
