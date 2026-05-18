import java.io.File;
import java.util.List;
import java.util.Vector;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class Raytracer {

    private Scene scene;
    private Camera camera;
    private Vector3D background = new Vector3D(0, 0, 0); // Bg Color (black)
    private static final int MAX_DEPTH = 3; // Max reflection bounces

    public Raytracer(Scene scene, Camera camera) {
        this.scene = scene;
        this.camera = camera;
    }

    public Camera getCamera() { return camera;}
    public Scene getScene() { return scene;}

    /**
     * Casts a shadow ray from the hit point toward the light.
     * Returns true if something blocks the path to the light.
     */
    private boolean isInShadow(Vector3D hitPoint, Vector3D toLight, double lightDistance) {

        // Offset the origin slightly along the normal to avoid self-intersection
        Vector3D shadowOrigin = new Vector3D(
            hitPoint.getX() + toLight.getX() * 1e-4,
            hitPoint.getY() + toLight.getY() * 1e-4,
            hitPoint.getZ() + toLight.getZ() * 1e-4
        );

        Ray shadowRay = new Ray(shadowOrigin, toLight);
        Intersection shadowHit = scene.intersect(shadowRay);

        // Only count as shadow if the blocker is closer than the light itself
        return shadowHit != null && shadowHit.getT() < lightDistance;
    }

    public Vector3D shade(Intersection hit) {
        Vector3D color = new Vector3D(0, 0, 0);
        Vector3D objectColor = hit.getObject().getColor();
        Vector3D normal = hit.getNormal();
        Vector3D hitPoint = hit.getPoint();
        double shininess = hit.getObject().getShininess();
        Vector3D cameraPos = camera.getPosition();

        double ambientLight = 0.15;

        for (Light light : scene.getLights()) {

            Vector3D toLight = light.getDirectionToLight(hitPoint);
            double lightDistance = light.getDistanceToLight(hitPoint);

            if (isInShadow(hitPoint, toLight, lightDistance)) {
                color = new Vector3D(
                    color.getX() + objectColor.getX() * ambientLight,
                    color.getY() + objectColor.getY() * ambientLight,
                    color.getZ() + objectColor.getZ() * ambientLight
                );
                continue;
            }

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

    /**
     * Computes the reflection direction of a ray bouncing off a surface
     * Formula: R = D - 2 * dot(D, N) * N
     * D is the incoming ray direction and N is the surface normal.
     * @param direction
     * @param normal
     * @return
     */
    private Vector3D reflect (Vector3D direction, Vector3D normal) {
        double dot = direction.dotProduct(normal);
        return direction.substract(normal.scale(2 * dot).normalize());
    }



    public Vector3D traceRay(Ray ray, int depth) {

        // Base case - stop recursing
        if (depth > MAX_DEPTH) return background;

        // Find the closest intersection of the ray with the scene
        Intersection hit = scene.intersect(ray);
        if (hit == null) return background;

        // Compute local shading (diffuse + specular + shadows)
        Vector3D localColor = shade(hit);
        double reflectivity = hit.getObject().getReflectivity();

        // If the object isn't reflective, return local color
        if (reflectivity <= 0.0) return localColor;

        // Offset the reflection origin to avoid self-intersection
        Vector3D normal = hit.getNormal();
        Vector3D hitPoint = hit.getPoint();
        Vector3D offsetOrigin = new Vector3D(
            hitPoint.getX() + normal.getX() * 1e-4,
            hitPoint.getY() + normal.getY() * 1e-4,
            hitPoint.getZ() + normal.getZ() * 1e-4
        );

        // Cast the reflection Ray
        Vector3D reflectDir = reflect(ray.getDirection(), normal);
        Ray reflectionRay = new Ray(offsetOrigin, reflectDir);
        Vector3D reflectedColor = traceRay(reflectionRay, depth + 1);

        // If reflection hits nothing (bg) don't darken the local color
        // Only blend when the reflected Ray hits
        boolean reflectionHits = scene.intersect(reflectionRay) != null;

        if (!reflectionHits) return localColor;

        // Blend localColor and reflectedColor based on reflectivity
        // finalColor = (1 - reflectivity) * localColor + reflectivity * reflectedColor
        return new Vector3D(
            (1 - reflectivity) * localColor.getX() + reflectivity * reflectedColor.getX(),
            (1 - reflectivity) * localColor.getY() + reflectivity * reflectedColor.getY(),
            (1 - reflectivity) * localColor.getZ() + reflectivity * reflectedColor.getZ()
        );

    }  
    


    public static void main(String[] args) throws Exception {

        // Image dimensions - Final version requires 4K
        int width = 1200; // 4096
        int height = 900; // 2160

        // Camera
        Camera camera = new Camera(new Vector3D(0, 0, 5), 60, (double)width / height);
        camera.setBackgroundColor(new Vector3D(0, 0, 0));

        // Scene
        Scene scene = new Scene();
        
        // Example: reflective red sphere
        Sphere mirrorSphere = new Sphere(new Vector3D(-2, 1, -2), 1, new Vector3D(1, 0, 0));
        mirrorSphere.setReflectivity(0.8);
        scene.addObject(mirrorSphere);

        Sphere mirrorSphere2 = new Sphere(new Vector3D(3,1, -2), 1.5 ,new Vector3D(0, 0, 1));
        mirrorSphere2.setReflectivity(0.8);
        scene.addObject(mirrorSphere2);

        Sphere s1 = new Sphere(new Vector3D(1, -3, -3), 1.5, new Vector3D(1, 0, 1));
        s1.setReflectivity(0.6);
        scene.addObject(s1);

        Sphere s2 = new Sphere(new Vector3D(-5, 3, -3), 1.5, new Vector3D(1, 1, 0));
        s2.setReflectivity(0.8);
        scene.addObject(s2);

        Sphere s3 = new Sphere(new Vector3D(0, 4, -5), 2, new Vector3D(1, 1, 1));
        s3.setReflectivity(1.0);
        scene.addObject(s3);

        // Placing the light above of the model, in front of the camera.
        /*
        */
       scene.addLight(new PointLight(
           new Vector3D(0, 2, 0),  // Position.
           new Vector3D(1.0, 1.0, 1.0),  // White light.
           1.0                            // Full intensity.
       ));


        /*
        scene.addLight(new DirectionalLight(
            new Vector3D(1, 1, 1).normalize(), // Direction the light travels
            new Vector3D(1, 1, 1),               // White
            1.0                                   // Intensity
        ));
        */

        List<Triangle> tea = OBJReader.load("teapot.obj", new Vector3D(1, 0.5, 1), new Vector3D(0, -0.5, -2));
        OBJReader.printBounds(tea);
        for (Triangle t : tea) {
            scene.addObject(t);
        }

        /*
        List<Triangle> tris = OBJReader.load("CottonCandy.obj", new Vector3D(1, 0.5, 1), new Vector3D(-2, 0, -1));
        OBJReader.printBounds(tris);
        for (Triangle t : tris) {
            scene.addObject(t);
        }
        */

        // Raytracer
        Raytracer raytracer = new Raytracer(scene, camera);

        // Image
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Render
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                Ray ray = camera.generateRay(x, y, width, height);

                // Pass depth 0 for initial ray
                Vector3D color = raytracer.traceRay(ray, 0);

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
