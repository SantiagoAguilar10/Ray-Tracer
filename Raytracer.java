import java.io.File;
import java.util.List;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class Raytracer {

    private Scene scene;
    private Camera camera;
    private Vector3D background = new Vector3D(0.1, 0.1, 0.15); // BG COLOR - Dark Blue Gray
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

        if (shadowHit == null) return false;
        if (shadowHit.getT() >= lightDistance) return false; // Hit is beyond the light, so it doesn't block
        if (shadowHit.getObject().getRefractivity() > 0.0) return false; // Transparent objects don't block light

        // Hit something solid before reaching the light
        return true;
    }

    public Vector3D shade(Intersection hit) {
        Vector3D color = new Vector3D(0, 0, 0);
        Vector3D objectColor = hit.getObject().getColor();
        Vector3D normal = hit.getNormal();
        Vector3D hitPoint = hit.getPoint();
        double shininess = hit.getObject().getShininess();
        double specStrength = hit.getObject().getSpecularStrength();
        double refractivity = hit.getObject().getRefractivity();
        Vector3D cameraPos = camera.getPosition();
        double ambientLight = 0.25;

        // Transparent objects — only compute specular highlights, skip diffuse/shadow
        // Their color comes from refraction in traceRay, not from local shading
        if (refractivity > 0.5) {
            for (Light light : scene.getLights()) {
                Vector3D toLight = light.getDirectionToLight(hitPoint);
                Vector3D toCamera = cameraPos.substract(hitPoint).normalize();

                // Only specular — gives the glassy highlight without darkening
                Vector3D reflection = normal.scale(2.0 * normal.dotProduct(toLight)).substract(toLight);
                double specular = Math.pow(Math.max(0.0, reflection.dotProduct(toCamera)), shininess);
                double specShade = specStrength * specular;

                color = new Vector3D(
                    Math.min(color.getX() + specShade, 1.0),
                    Math.min(color.getY() + specShade, 1.0),
                    Math.min(color.getZ() + specShade, 1.0)
                );
            }
            // Return specular highlights over a nearly transparent base
            return color;
        }

        // Opaque objects — full shading as before
        for (Light light : scene.getLights()) {
            Vector3D toLight      = light.getDirectionToLight(hitPoint);
            double   lightDistance = light.getDistanceToLight(hitPoint);

            if (isInShadow(hitPoint, toLight, lightDistance)) {
                color = new Vector3D(
                    color.getX() + objectColor.getX() * ambientLight,
                    color.getY() + objectColor.getY() * ambientLight,
                    color.getZ() + objectColor.getZ() * ambientLight
                );
                continue;
            }

            Vector3D contribution = light.shade(
                hitPoint, normal, objectColor,
                ambientLight, cameraPos,
                shininess, specStrength
            );
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


    /**
     * Computes the refracted ray direction using Snell's law.
     * Returns null if total internal reflection occurs (no refraction possible).
     *
     * @param direction     Incoming ray direction
     * @param normal        Surface normal at hit point
     * @param ior           Refractive index of the material
     */
    private Vector3D refract(Vector3D direction, Vector3D normal, double ior) {

        double cosi = Math.max(-1.0, Math.min(1.0, direction.dotProduct(normal)));
        double etai = 1.0; // IOR of air (outside)
        double etat = ior; // IOR of the material (inside)
        Vector3D n = normal;

        // If cosi > 0 the ray is inside the object — flip normal and swap IORs
        if (cosi > 0) {
            n = normal.scale(-1);
            double temp = etai; etai = etat; etat = temp;
        } else {
            cosi = -cosi;
        }

        double eta = etai / etat;
        double k   = 1 - eta * eta * (1 - cosi * cosi);

        // k < 0 means total internal reflection — no refracted ray exists
        if (k < 0) return null;

        // Snell's law: refractedDir = eta * direction + (eta * cosi - sqrt(k)) * n
        return direction.scale(eta).add(n.scale(eta * cosi - Math.sqrt(k))).normalize();
    }

    /**
     * Computes the Fresnel effect — how much light reflects vs refracts
     * based on the viewing angle. Returns a value between 0 (all refraction)
     * and 1 (all reflection).
     *
     * Uses Schlick's approximation for performance.
     */
    private double fresnel(Vector3D direction, Vector3D normal, double ior) {
        double cosi = Math.max(-1.0, Math.min(1.0, direction.dotProduct(normal)));
        double etai = 1.0;
        double etat = ior;

        if (cosi > 0) { double temp = etai; etai = etat; etat = temp; }

        double sint = etai / etat * Math.sqrt(Math.max(0.0, 1 - cosi * cosi));

        // Total internal reflection
        if (sint >= 1.0) return 1.0;

        double cost = Math.sqrt(Math.max(0.0, 1 - sint * sint));
        cosi = Math.abs(cosi);

        // Schlick approximation
        double rs = ((etat * cosi) - (etai * cost)) / ((etat * cosi) + (etai * cost));
        double rp = ((etai * cosi) - (etat * cost)) / ((etai * cosi) + (etat * cost));
        return (rs * rs + rp * rp) / 2.0;
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
        double refractivity = hit.getObject().getRefractivity();
        double refractiveIndex = hit.getObject().getRefractiveIndex();

        // If the object isn't reflective, return local color
        // if (reflectivity <= 0.0) return localColor;

        // Offset the reflection origin to avoid self-intersection
        Vector3D normal = hit.getNormal();
        Vector3D hitPoint = hit.getPoint();

        if (refractivity > 0.0) {

            double fresnelAmount = fresnel(ray.getDirection(), normal, refractiveIndex);

            // Reflection component (even glass can reflect, a little bit)
            Vector3D reflectedColor = new Vector3D(0, 0, 0);
            Vector3D offsetReflect = new Vector3D(
                hitPoint.getX() + normal.getX() * 1e-4,
                hitPoint.getY() + normal.getY() * 1e-4,
                hitPoint.getZ() + normal.getZ() * 1e-4
            );

            Vector3D reflectDir = reflect(ray.getDirection(), normal);
            reflectedColor = traceRay(new Ray(offsetReflect, reflectDir), depth + 1);

            // Refraction component
            Vector3D refractedColor = background;
            Vector3D refractDir = refract(ray.getDirection(), normal, refractiveIndex);

            
            if (refractDir != null) {

                double bias = 1e-4;

                Vector3D offsetRefract;

                // If the refracted ray exits the object:
                if (refractDir.dotProduct(normal) > 0) {
                    offsetRefract = new Vector3D(
                        hitPoint.getX() + normal.getX() * bias,
                        hitPoint.getY() + normal.getY() * bias,
                        hitPoint.getZ() + normal.getZ() * bias
                    );
                } 

                else {
                    offsetRefract = new Vector3D(
                        hitPoint.getX() - normal.getX() * bias,
                        hitPoint.getY() - normal.getY() * bias,
                        hitPoint.getZ() - normal.getZ() * bias
                    );
                }

                refractedColor = traceRay(
                    new Ray(offsetRefract, refractDir),
                    depth + 1
                );
            }
            

            /*
            if (refractDir != null && depth == 0) {
                Vector3D offsetRefract = new Vector3D(
                    hitPoint.getX() - normal.getX() * 1e-4,
                    hitPoint.getY() - normal.getY() * 1e-4,
                    hitPoint.getZ() - normal.getZ() * 1e-4
                );
                Ray testRay = new Ray(offsetRefract, refractDir);
                Intersection testHit = scene.intersect(testRay);
                if (testHit == null) {
                    System.out.println("Refracted ray hit: BACKGROUND");
                } else {
                    System.out.println("Refracted ray hit: " + testHit.getObject().getClass().getSimpleName()
                        + " at t=" + testHit.getT());
                }
            }
            */

            // Blend reflection and refraction. -- Using Fresnel*
            Vector3D refractColor = new Vector3D(
                fresnelAmount * reflectedColor.getX() + (1 - fresnelAmount) * refractedColor.getX(),
                fresnelAmount * reflectedColor.getY() + (1 - fresnelAmount) * refractedColor.getY(),
                fresnelAmount * reflectedColor.getZ() + (1 - fresnelAmount) * refractedColor.getZ()
            );

            // Blend with localColor (based on refractivity)
            // If refractivity is 1. It means full transparent, so there's no local color
            return new Vector3D(
                (1 - refractivity) * localColor.getX() + refractivity * refractColor.getX(),
                (1 - refractivity) * localColor.getY() + refractivity * refractColor.getY(),
                (1 - refractivity) * localColor.getZ() + refractivity * refractColor.getZ()
            );

        }

        // Reflection Only. No Refraction.
        if (reflectivity <= 0.0) return localColor;

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

        // Tint reflection for metallic look
        /*
         */
        Vector3D objColor = hit.getObject().getColor();
        Vector3D tintedReflection = new Vector3D(
            reflectedColor.getX() * objColor.getX(),
            reflectedColor.getY() * objColor.getY(),
            reflectedColor.getZ() * objColor.getZ()
        );

        // Blend localColor and reflectedColor based on reflectivity
        // finalColor = (1 - reflectivity) * localColor + reflectivity * reflectedColor
        return new Vector3D(
            (1 - reflectivity) * localColor.getX() + reflectivity * tintedReflection.getX(),
            (1 - reflectivity) * localColor.getY() + reflectivity * tintedReflection.getY(),
            (1 - reflectivity) * localColor.getZ() + reflectivity * tintedReflection.getZ()
        );

    }  
    


    public static void main(String[] args) throws Exception {

        int width  = 1200;
        int height = 900;

        // Experiment

        
         
        // Camera pulled back to see the full room
        Camera camera = new Camera(new Vector3D(0, 0, 4), 60, (double)width / height);
        camera.setBackgroundColor(new Vector3D(0, 0, 0));

        Scene scene = new Scene();

        // WALLS - each wall is two triangles forming a rectangle
        // Room spans X: -8 to 8, Y: -2 to 8, Z: -10 to 8

        // Floor (Y = -2) - gray
        Vector3D floorColor = new Vector3D(0.7, 0.7, 0.7);
        scene.addObject(new Triangle(
            new Vector3D(-8, -2, -10), new Vector3D( 8, -2, -10), new Vector3D( 8, -2, 8), floorColor));
        scene.addObject(new Triangle(
            new Vector3D(-8, -2, -10), new Vector3D( 8, -2,  8), new Vector3D(-8, -2, 8), floorColor));

        // Ceiling (Y = 8) - dark gray
        Vector3D ceilColor = new Vector3D(0.8, 0.8, 0.8);
        scene.addObject(new Triangle(
            new Vector3D(-8, 8, -10), new Vector3D( 8, 8,  8), new Vector3D( 8, 8, -10), ceilColor));
        scene.addObject(new Triangle(
            new Vector3D(-8, 8, -10), new Vector3D(-8, 8,  8), new Vector3D( 8, 8,  8), ceilColor));

        // Back wall (Z = -10) - off white
        Vector3D backColor = new Vector3D(0.95, 0.95, 0.9);
        scene.addObject(new Triangle(
            new Vector3D(-8, -2, -10), new Vector3D( 8, 8, -10), new Vector3D( 8, -2, -10), backColor));
        scene.addObject(new Triangle(
            new Vector3D(-8, -2, -10), new Vector3D(-8, 8, -10), new Vector3D( 8,  8, -10), backColor));

        // Left wall (X = -8) - red tint
        Vector3D leftColor = new Vector3D(0.8, 0.2, 0.2);
        scene.addObject(new Triangle(
            new Vector3D(-8, -2, -10), new Vector3D(-8, -2, 8), new Vector3D(-8, 8, -10), leftColor));
        scene.addObject(new Triangle(
            new Vector3D(-8, -2,  8),  new Vector3D(-8,  8, 8), new Vector3D(-8, 8, -10), leftColor));

        // Right wall (X = 8) - blue tint
        Vector3D rightColor = new Vector3D(0.2, 0.2, 0.8);
        scene.addObject(new Triangle(
            new Vector3D(8, -2, -10), new Vector3D(8, 8, -10), new Vector3D(8, -2, 8), rightColor));
        scene.addObject(new Triangle(
            new Vector3D(8, 8, -10),  new Vector3D(8, 8,  8),  new Vector3D(8, -2, 8), rightColor));

        // SPHERES - spread out so reflections show wall colors

        // Red reflective sphere - left side
        
        Sphere s1 = new Sphere(new Vector3D(-4, 0, -3), 1.5, new Vector3D(1, 0, 0));
        s1.setReflectivity(0.8);
        s1.setShininess(128);
        s1.setSpecularStrength(0.9);
        scene.addObject(s1);
         

        // Blue reflective sphere — right side
        
        Sphere s2 = new Sphere(new Vector3D(4, 0, -3), 1.5, new Vector3D(0, 0, 1));
        s2.setReflectivity(0.8);
        s2.setShininess(128);
        s2.setSpecularStrength(0.9);
        scene.addObject(s2);
         

        // White mirror sphere — back center, perfect mirror
        
        Sphere s3 = new Sphere(new Vector3D(0, 1, -7), 2, new Vector3D(1, 1, 1));
        s3.setReflectivity(1.0);
        s3.setShininess(256);
        s3.setSpecularStrength(1.0);
        scene.addObject(s3);
         

        // Yellow sphere — upper left, less reflective
        
        Sphere s4 = new Sphere(new Vector3D(-3, 3, -5), 1, new Vector3D(1, 1, 0));
        s4.setReflectivity(0.4);
        s4.setShininess(64);
        scene.addObject(s4);
         

        // Magenta sphere — lower center foreground
        
        Sphere s5 = new Sphere(new Vector3D(1, -0.5, 0), 1, new Vector3D(1, 0, 1));
        s5.setReflectivity(0.6);
        s5.setShininess(96);
        scene.addObject(s5);
         

        
        // TEAPOT - center of the scene, sitting on the floor
        List<Triangle> tea = OBJReader.load("teapot.obj", new Vector3D(1, 0.5, 1), new Vector3D(0, -2, -4));
        for (Triangle t : tea) scene.addObject(t);

        // Glass sphere — fully transparent, IOR 1.5
        Sphere glass = new Sphere(new Vector3D( 0, 0, -4), 1.0, new Vector3D(1.0, 1.0, 1.0),   64, 0.0, 0.5, 1.0, 1.5 );
        Sphere water = new Sphere(new Vector3D( 2, 0, -4), 1.0, new Vector3D(0.6, 0.8, 1.0),   32, 0.0, 0.3, 0.9, 1.33);
        Sphere diamond = new Sphere(new Vector3D(-2, 0, -4), 0.8, new Vector3D(0.9, 0.95, 1.0), 128, 0.0, 0.8, 1.0, 2.4 );

        scene.addObject(glass);
        scene.addObject(water);
        scene.addObject(diamond);

        
        // LIGHTS
        // Main light above center
        scene.addLight(new PointLight(
            new Vector3D(0, 5, -2),
            new Vector3D(1, 1, 1),
            2.0
        ));

        // Secondary fill light from the front-right to reduce harsh shadows
        scene.addLight(new PointLight(
            new Vector3D(0, 3, 6),
            new Vector3D(1, 1, 1), // White light
            1.0
        ));

        // Side Light to show wall colors in reflections
        scene.addLight(new PointLight(
            new Vector3D(-6, 2, -4),
            new Vector3D(1, 0.5, 0.5), // Warm light
            0.8
        ));
        
        

        // Raytracer
        Raytracer raytracer = new Raytracer(scene, camera);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Ray ray = camera.generateRay(x, y, width, height);
                Vector3D color = raytracer.traceRay(ray, 0);

                int r = (int)(255 * color.getX());
                int g = (int)(255 * color.getY());
                int b = (int)(255 * color.getZ());

                image.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }

        ImageIO.write(image, "png", new File("output.png"));
        System.out.println("Generated Image: output.png");
    }
}
