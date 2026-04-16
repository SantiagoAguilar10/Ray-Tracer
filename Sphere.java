public class Sphere extends Object3D {
    /**
     * Equations of Ray-Sphere Intersection:
     * 
     * O = ray origin
     * D = ray direction
     * C = sphere center
     * t = distance along ray to intersection
     */

    // Convert into quadratic form and solve for t
    /**
     * a = D . D
     * b = 2 * D . (O - C)
     * c = (O - C) . (O - C) - r^2
     */

    // Discriminant

    /**
     * < 0: no intersection
     * = 0: one intersection (tangent)
     * > 0: two intersections (entering and exiting)
     */
}