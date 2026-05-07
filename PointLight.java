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
    @Override
    public Vector3D shade(Vector3D hitPoint, Vector3D normal, Vector3D objectColor, double ambientLight) {
        Vector3D toLight = getDirectionToLight(hitPoint);
        double diffuse = Math.max(0.0, normal.dotProduct(toLight));
        double shade = ambientLight + (1.0 - ambientLight) * diffuse * intensity;
        shade = Math.min(shade, 1.0);

        return new Vector3D(
            objectColor.getX() * shade * color.getX(),
            objectColor.getY() * shade * color.getY(),
            objectColor.getZ() * shade * color.getZ()
        );
    }
}