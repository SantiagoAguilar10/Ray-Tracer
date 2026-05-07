public class DirectionalLight extends Light {

    private Vector3D direction; // Direction the light travels (toward the scene)

    public DirectionalLight(Vector3D direction, Vector3D color, double intensity) {
        super(color, intensity);
        // Store the normalized direction pointing TOWARD the light (opposite of travel)
        this.direction = direction.normalize();
    }

    @Override
    public Vector3D getDirectionToLight(Vector3D hitPoint) {
        return direction; // Constant for all points — no position involved
    }

    @Override
    public Vector3D shade(Vector3D hitPoint, Vector3D normal, Vector3D objectColor, double ambientLight) {
        double diffuse = Math.max(0.0, normal.dotProduct(direction));
        double shade = ambientLight + (1.0 - ambientLight) * diffuse * intensity;
        shade = Math.min(shade, 1.0);

        return new Vector3D(
            objectColor.getX() * shade * color.getX(),
            objectColor.getY() * shade * color.getY(),
            objectColor.getZ() * shade * color.getZ()
        );
    }
}