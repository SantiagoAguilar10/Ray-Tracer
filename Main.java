import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class Main {

    public static void main(String[] args) throws Exception {

        int width = 800;
        int height = 600;

        // Cámara
        Camera camera = new Camera(new Vector3D(0, 0, 0), 60,  (double)width / height);
        camera.setBackgroundColor(new Vector3D(0, 0, 0));

        // Escena
        Scene scene = new Scene();
        scene.addObject(new Sphere(new Vector3D(0, 0, -5), 2, new Vector3D(1, 0, 0)));

        // Raytracer
        Raytracer raytracer = new Raytracer(scene, camera);

        // Imagen
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Render
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                Ray ray = camera.generateRay(x, y, width, height);
                Vector3D color = raytracer.traceRay(ray);

                // Convertir de [0,1] a [0,255]
                int r = (int)(255 * color.getX());
                int g = (int)(255 * color.getY());
                int b = (int)(255 * color.getZ());

                int rgb = (r << 16) | (g << 8) | b;

                image.setRGB(x, y, rgb);
            }
        }

        // Guardar imagen
        ImageIO.write(image, "png", new File("output.png"));

        System.out.println("Imagen generada: output.png");
    }
}