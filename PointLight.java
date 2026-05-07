public class PointLight extends Light {
    
    private Vector3D position;
    
    public PointLight(Vector3D position, Vector3D color, double intensity) {
        super(color, intensity);
        this.position = position;
    }
    
    public Vector3D getPosition() { return position; }
    
    @Override
    public Vector3D getDirectionToLight(Vector3D hitPoint) {
        return position.substract(hitPoint).normalize();
    }
    
    
    @Override
    public Vector3D shade(Vector3D hitPoint, Vector3D normal, Vector3D objectColor, double ambientLight, Vector3D cameraPosition, double shininess) {
            
        Vector3D toLight  = getDirectionToLight(hitPoint);
        Vector3D toCamera = cameraPosition.substract(hitPoint).normalize();

        // Diffuse
        double diffuse = Math.max(0.0, normal.dotProduct(toLight));

        // Specular — reflect toLight around normal, measure alignment with toCamera
        Vector3D reflection = normal.scale(2.0 * normal.dotProduct(toLight)).substract(toLight);
        double specular = Math.pow(Math.max(0.0, reflection.dotProduct(toCamera)), shininess);

        double diffuseShade  = ambientLight + (1.0 - ambientLight) * diffuse * intensity;
        double specularShade = specular * intensity;

        return new Vector3D(
            Math.min(objectColor.getX() * diffuseShade * color.getX() + specularShade, 1.0),
            Math.min(objectColor.getY() * diffuseShade * color.getY() + specularShade, 1.0),
            Math.min(objectColor.getZ() * diffuseShade * color.getZ() + specularShade, 1.0)
        );
    }
}