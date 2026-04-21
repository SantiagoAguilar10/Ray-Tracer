public class Camera {

    private Vector3D position;
    private double fov; // in degrees
    private double aspectRatio; // width / height

    public Camera(Vector3D position, double fov, double aspectRatio) {
        this.position = position;
        this.fov = fov;
        this.aspectRatio = aspectRatio;
    }

    // Getters
    public Vector3D getPosition() { return position;}
    public double getAspectRatio() { return aspectRatio;}
    public double getFov() { return fov;}

    public Vector3D getBackgroundColor() {
        return new Vector3D(0, 0, 0); // Default to black background
    }

    public Vector3D setBackgroundColor(Vector3D color) {
        return color; // Placeholder for setting background color
    }

    // Setters
    public void setPosition(Vector3D position) { this.position = position;}
    public void setFov(double fov) { this.fov = fov;}
    public void setAspectRatio(double aspectRatio) { this.aspectRatio = aspectRatio;}


    public Ray generateRay(double pixelX, double pixelY, int imageWidth, int imageHeight) {
        // Convert pixel coordinates to normalized device coordinates (NDC)
        double ndcX = (pixelX + 0.5) / imageWidth; // Center of the pixel
        double ndcY = (pixelY + 0.5) / imageHeight;

        // Convert NDC to screen space coordinates
        double screenX = (2 * ndcX - 1) * aspectRatio * Math.tan(Math.toRadians(fov / 2));
        double screenY = (1 - 2 * ndcY) * Math.tan(Math.toRadians(fov / 2));

        // Create a ray from the camera position through the pixel on the screen
        Vector3D rayDirection = new Vector3D(screenX, screenY, -1).normalize(); // Assuming the camera looks towards negative Z
        return new Ray(position, rayDirection);
    }

}
