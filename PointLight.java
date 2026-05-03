public class PointLight {

    private Vector3D position;
    private Vector3D color;     // RGB intensity, e.g. (1, 1, 1) for white
    private double intensity;

    public PointLight(Vector3D position, Vector3D color, double intensity) {
        this.position = position;
        this.color = color;
        this.intensity = intensity;
    }

    // Getters
    public Vector3D getPosition() { return position; }
    public Vector3D getColor()    { return color; }
    public double getIntensity()  { return intensity; }

    /**
     * Computes the flat-shaded color contribution of this light on a surface.
     *
     * Flat shading uses a constant normal per triangle (no interpolation),
     * so the entire triangle receives the same shading value.
     *
     * @param hitPoint      The point on the surface that was hit
     * @param normal        The surface normal of the triangle (flat, not interpolated)
     * @param objectColor   The base color of the object
     * @param ambientLight  Ambient light intensity (0.0 - 1.0), prevents fully black shadows
     * @return              The shaded color as a Vector3D (RGB in 0-1 range)
     */
    public Vector3D shade(Vector3D hitPoint, Vector3D normal, Vector3D objectColor, double ambientLight) {

        // Direction from the hit point to the light source.
        Vector3D toLight = position.substract(hitPoint).normalize();

        // Lambertian diffuse: how much the surface faces the light
        // dot product of the normal and light direction, clamped to [0, 1].
        double diffuse = Math.max(0.0, normal.dotProduct(toLight));

        // Combine ambient + diffuse, scaled by light intensity.
        double shade = ambientLight + (1.0 - ambientLight) * diffuse * intensity;
        shade = Math.min(shade, 1.0); // Clamp to 1.0

        // Multiply the object's base color by the shade factor and light color.
        return new Vector3D(
            objectColor.getX() * shade * color.getX(),
            objectColor.getY() * shade * color.getY(),
            objectColor.getZ() * shade * color.getZ()
        );
    }
}