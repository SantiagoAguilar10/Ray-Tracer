public class DirectionalLight extends Light {

    private Vector3D direction;

    public DirectionalLight(Vector3D direction, Vector3D color, double intensity) {
        super(color, intensity);
        this.direction = direction.normalize();
    }

    @Override
    public Vector3D getDirectionToLight(Vector3D hitPoint) { return direction; }

    @Override
    public Vector3D shade(Vector3D hitPoint, Vector3D normal, Vector3D objectColor, double ambientLight, Vector3D cameraPosition, double shininess) {

        Vector3D toCamera = cameraPosition.substract(hitPoint).normalize();

        double diffuse = Math.max(0.0, normal.dotProduct(direction));

        Vector3D reflection = normal.scale(2.0 * normal.dotProduct(direction)).substract(direction);
        double specular = Math.pow(Math.max(0.0, reflection.dotProduct(toCamera)), shininess);

        double specularStrength = 0.3; // How strong the highlight is (0.0 - 1.0)
        double diffuseShade  = ambientLight + (1.0 - ambientLight) * diffuse * intensity;
        double specularShade = specularStrength * specular * intensity;

        return new Vector3D(
            Math.min(objectColor.getX() * diffuseShade * color.getX() + specularShade, 1.0),
            Math.min(objectColor.getY() * diffuseShade * color.getY() + specularShade, 1.0),
            Math.min(objectColor.getZ() * diffuseShade * color.getZ() + specularShade, 1.0)
        );
    }
}