import java.io.File;
import java.util.List;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class Raytracer {

    private Scene scene;
    private Camera camera;
    private Vector3D background = new Vector3D(0.0, 0.0, 0.1); // BG COLOR - Dark Blue Gray
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
        Vector3D objectColor = hit.getObject().getColor();
        Vector3D normal = hit.getNormal();
        Vector3D hitPoint = hit.getPoint();
        double shininess = hit.getObject().getShininess();
        double specStrength = hit.getObject().getSpecularStrength();
        double refractivity = hit.getObject().getRefractivity();
        Vector3D cameraPos = camera.getPosition();
        double ambientLight = 0.15;

        // Ambient applied ONCE — independent of lights
        Vector3D color = new Vector3D(
            objectColor.getX() * ambientLight,
            objectColor.getY() * ambientLight,
            objectColor.getZ() * ambientLight
        );

        // Transparent objects — specular highlights only
        if (refractivity > 0.5) {
            for (Light light : scene.getLights()) {
                Vector3D toLight = light.getDirectionToLight(hitPoint);
                Vector3D toCamera = cameraPos.substract(hitPoint).normalize();

                Vector3D reflection = normal.scale(2.0 * normal.dotProduct(toLight)).substract(toLight);
                double specular = Math.pow(Math.max(0.0, reflection.dotProduct(toCamera)), shininess);
                double specShade = specStrength * specular;

                color = new Vector3D(
                    Math.min(color.getX() + specShade, 1.0),
                    Math.min(color.getY() + specShade, 1.0),
                    Math.min(color.getZ() + specShade, 1.0)
                );
            }
            return color;
        }

        // Opaque objects — diffuse + specular per light, no ambient inside loop
        for (Light light : scene.getLights()) {
            Vector3D toLight = light.getDirectionToLight(hitPoint);
            double lightDistance = light.getDistanceToLight(hitPoint);

            // In shadow — skip this light entirely, ambient already applied above
            if (isInShadow(hitPoint, toLight, lightDistance)) continue;

            Vector3D contribution = light.shade(hitPoint, normal, objectColor,
                                                0.0,        // ← pass 0 ambient to lights
                                                cameraPos, shininess, specStrength);
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
        Vector3D normal = hit.getNormal();
        Vector3D hitPoint = hit.getPoint();

        // REFRACTION (takes priority over pure reflection)
        if (refractivity > 0.0) {

            double fresnelAmount = fresnel(ray.getDirection(), normal, refractiveIndex);

            // Reflection component — even glass reflects a little (Fresnel)
            Vector3D offsetReflect = new Vector3D(
                hitPoint.getX() + normal.getX() * 1e-4,
                hitPoint.getY() + normal.getY() * 1e-4,
                hitPoint.getZ() + normal.getZ() * 1e-4
            );
            Vector3D reflectDir = reflect(ray.getDirection(), normal);
            Vector3D reflectedColor = traceRay(new Ray(offsetReflect, reflectDir), depth + 1);

            // Refraction component
            Vector3D refractedColor = background;
            Vector3D refractDir = refract(ray.getDirection(), normal, refractiveIndex);

            if (refractDir != null) {
                double bias = 1e-4;
                Vector3D offsetRefract;

                // Offset along or against normal depending on ray exit direction
                if (refractDir.dotProduct(normal) > 0) {
                    offsetRefract = new Vector3D(
                        hitPoint.getX() + normal.getX() * bias,
                        hitPoint.getY() + normal.getY() * bias,
                        hitPoint.getZ() + normal.getZ() * bias
                    );
                } else {
                    offsetRefract = new Vector3D(
                        hitPoint.getX() - normal.getX() * bias,
                        hitPoint.getY() - normal.getY() * bias,
                        hitPoint.getZ() - normal.getZ() * bias
                    );
                }

                refractedColor = traceRay(new Ray(offsetRefract, refractDir), depth + 1);
            }

            // Blend reflection and refraction using Fresnel
            Vector3D refractColor = new Vector3D(
                fresnelAmount * reflectedColor.getX() + (1 - fresnelAmount) * refractedColor.getX(),
                fresnelAmount * reflectedColor.getY() + (1 - fresnelAmount) * refractedColor.getY(),
                fresnelAmount * reflectedColor.getZ() + (1 - fresnelAmount) * refractedColor.getZ()
            );

            // Blend with local color based on refractivity
            // refractivity = 1.0 means fully transparent — local color ignored
            return new Vector3D(
                (1 - refractivity) * localColor.getX() + refractivity * refractColor.getX(),
                (1 - refractivity) * localColor.getY() + refractivity * refractColor.getY(),
                (1 - refractivity) * localColor.getZ() + refractivity * refractColor.getZ()
            );
        }

        // REFLECTION ONLY (no refraction)
        if (reflectivity <= 0.0) return localColor;

        Vector3D offsetOrigin = new Vector3D(
            hitPoint.getX() + normal.getX() * 1e-4,
            hitPoint.getY() + normal.getY() * 1e-4,
            hitPoint.getZ() + normal.getZ() * 1e-4
        );

        Vector3D reflectDir = reflect(ray.getDirection(), normal);
        Ray reflectionRay = new Ray(offsetOrigin, reflectDir);
        Vector3D reflectedColor = traceRay(reflectionRay, depth + 1);

        // Metallic objects tint their reflections with their own color
        // Non-metallic (mirrors, floors, puddles) reflect cleanly without tinting
        Vector3D finalReflection;
        if (hit.getObject().isMetallic()) {
            Vector3D objColor = hit.getObject().getColor();
            finalReflection = new Vector3D(
                reflectedColor.getX() * objColor.getX(),
                reflectedColor.getY() * objColor.getY(),
                reflectedColor.getZ() * objColor.getZ()
            );
        } else {
            finalReflection = reflectedColor;
        }

        // Blend local color and reflection based on reflectivity
        // finalColor = (1 - reflectivity) * localColor + reflectivity * reflectedColor
        return new Vector3D(
            (1 - reflectivity) * localColor.getX() + reflectivity * finalReflection.getX(),
            (1 - reflectivity) * localColor.getY() + reflectivity * finalReflection.getY(),
            (1 - reflectivity) * localColor.getZ() + reflectivity * finalReflection.getZ()
        );
    }
    


    public static void main(String[] args) throws Exception {

        int width  = 4096; // 512
        int height = 2160; // 270

        Camera camera = new Camera(new Vector3D(0, 8, 45), 60, (double)width / height);

        Scene scene = new Scene();

        Vector3D pavement = new Vector3D(0.76, 0.7, 0.5);


        // FLOOR

        Triangle floorT1 = new Triangle(
            new Vector3D(-80, -3, -30),
            new Vector3D( 80, -3, -30),
            new Vector3D( 80, -3,  40),
            pavement);

        Triangle floorT2 = new Triangle(
            new Vector3D(-80, -3, -30),
            new Vector3D( 80, -3,  40),
            new Vector3D(-80, -3,  40),
            pavement);

        scene.addObject(floorT1);
        scene.addObject(floorT2);


        // MODELS
        // Run printBounds() first to calibrate Y offset and scale
        
        List<Triangle> poly = OBJReader.load("Models/Statue2.obj",
            new Vector3D(0.4, 0.6, 0.5),
            new Vector3D(-3, -3, 12),30.0, -90.0);
        OBJReader.printBounds(poly);
        poly.forEach(tri -> {
            tri.setRefractivity(0.0);
            tri.setShininess(1024);
            tri.setReflectivity(0.4);
            //tri.setRefractiveIndex(1);
            tri.setSpecularStrength(1.0);
        });
        poly.forEach(scene::addObject);
         

        List<Triangle> polyb = OBJReader.load("Models/bridge.obj",
            new Vector3D(0.5, 0.5, 0.5),
            new Vector3D(0, -8, -18),6.0, 90.0);
        OBJReader.printBounds(polyb);
        polyb.forEach( tri -> {
            tri.setRefractivity(0.0);
            tri.setShininess(128);
            tri.setReflectivity(0.4);
            tri.setSpecularStrength(0.9);
        });
        polyb.forEach(scene :: addObject);


        List<Triangle> poly2 = OBJReader.load("Models/rock.obj",
            new Vector3D(0.2, 0.2, 0.2),
            new Vector3D(20, 0, 0),0.004);
        OBJReader.printBounds(poly2);
        poly2.forEach( tri -> {
            tri.setRefractivity(0.0);
            tri.setShininess(16);
            tri.setReflectivity(0.0);
            tri.setSpecularStrength(0.0);
        });
        poly2.forEach(scene :: addObject);

        List<Triangle> poly3 = OBJReader.load("Models/Low_Rocks.obj",
            new Vector3D(1.0, 0.15, 0.15),
            new Vector3D(6, -3, 18),1.4, 60.0);
        OBJReader.printBounds(poly3);
        poly3.forEach( tri -> {
            tri.setRefractivity(0.9);
            tri.setShininess(1024);
            tri.setReflectivity(0.2);
            tri.setSpecularStrength(1.0);
            tri.setRefractiveIndex(1.77);
        });
        poly3.forEach(scene :: addObject);

        List<Triangle> poly4 = OBJReader.load("Models/rock.obj",
            new Vector3D(0.2, 0.2, 0.2),
            new Vector3D(-15, -2, 20),0.004);
        OBJReader.printBounds(poly4);
        poly4.forEach( tri -> {
            tri.setRefractivity(0.0);
            tri.setShininess(216);
            tri.setReflectivity(0.6);
            tri.setSpecularStrength(0.4);
        });
        poly4.forEach(scene :: addObject);

        List<Triangle> poly6 = OBJReader.load("Models/rock.obj",
            new Vector3D(0.2, 0.2, 0.2),
            new Vector3D(-45, 0, -10),0.007);
        OBJReader.printBounds(poly6);
        poly6.forEach( tri -> {
            tri.setRefractivity(0.0);
            tri.setShininess(16);
            tri.setReflectivity(0.0);
            tri.setSpecularStrength(0.0);
        });
        poly6.forEach(scene :: addObject);


        // LIGHTS
        
        scene.addLight(new PointLight(
            new Vector3D(30, 0, 40),
            new Vector3D(1.0, 1.0, 1.0),
            25.0
        ));

        scene.addLight(new PointLight(
            new Vector3D(-30, 4, 40),
            new Vector3D(1.0, 1.0, 1.0),
            15.0
        ));

        scene.addLight(new PointLight(
            new Vector3D(-25, -2, -25),
            new Vector3D(1.0, 0.0, 0.0),
            3.0
        ));

        scene.addLight(new PointLight(
            new Vector3D(25, -2, -25),
            new Vector3D(1.0, 0.0, 0.0),
            3.0
        ));


        scene.buildBVH();

        Raytracer raytracer = new Raytracer(scene, camera);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Ray ray = camera.generateRay(x, y, width, height);
                Vector3D color = raytracer.traceRay(ray, 0);

                int r = (int)(255 * Math.min(color.getX(), 1.0));
                int g = (int)(255 * Math.min(color.getY(), 1.0));
                int b = (int)(255 * Math.min(color.getZ(), 1.0));

                image.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }

        ImageIO.write(image, "png", new File("Scene3.png"));
        System.out.println("Generated Image: Scene3.png");
    }
}

