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

        // Fallof - inverse square law
        double distance = getDistanceToLight(hitPoint);
        double falloff  = intensity / (1.0 + 0.1 * distance + 0.01 * distance * distance);
        // First 0.1 - linear term
        // Second 0.1 - cuadratic term
        // These 2 constants control how fast light fades 

        // Diffuse
        double diffuse = Math.max(0.0, normal.dotProduct(toLight));

        // Specular — reflect toLight around normal, measure alignment with toCamera
        Vector3D reflection = normal.scale(2.0 * normal.dotProduct(toLight)).substract(toLight);
        double specular = Math.pow(Math.max(0.0, reflection.dotProduct(toCamera)), shininess);

        double specularStrength = 0.3;
        double diffuseShade = ambientLight + (1.0 - ambientLight) * diffuse * falloff;
        double specularShade = specularStrength * specular * falloff;

        return new Vector3D(
            Math.min(objectColor.getX() * diffuseShade * color.getX() + specularShade, 1.0),
            Math.min(objectColor.getY() * diffuseShade * color.getY() + specularShade, 1.0),
            Math.min(objectColor.getZ() * diffuseShade * color.getZ() + specularShade, 1.0)
        );
    }

    @Override
    public double getDistanceToLight(Vector3D hitPoint) {
        // Euclidean distance from hit point to light position
        Vector3D diff = position.substract(hitPoint);

        return Math.sqrt(
            diff.getX() * diff.getX() + 
            diff.getY() * diff.getY() + 
            diff.getZ() * diff.getZ());
    }

}