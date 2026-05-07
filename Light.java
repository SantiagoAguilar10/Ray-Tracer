public abstract class Light {

    protected Vector3D color;
    protected double intensity;

    public Light(Vector3D color, double intensity) {
        this.color = color;
        this.intensity = intensity;
    }

    public Vector3D getColor()    { return color; }
    public double getIntensity()  { return intensity; }

    /**
     * Returns the direction from the hit point toward the light source.
     * For point lights this depends on position; for directional it's constant.
     */
    public abstract Vector3D getDirectionToLight(Vector3D hitPoint);

    /**
     * Computes this light's shaded color contribution on a surface.
     */
    public abstract Vector3D shade(Vector3D hitPoint, Vector3D normal, Vector3D objectColor, double ambientLight);
}