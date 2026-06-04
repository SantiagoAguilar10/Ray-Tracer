import java.io.*;
import java.util.*;

public class OBJReader {

    // Full overload — offset, scale, and Y rotation
    public static List<Triangle> load(String path, Vector3D color, Vector3D offset, double scale, double rotateY) {

        List<Vector3D> vertices = new ArrayList<>();
        List<Vector3D> normals = new ArrayList<>();
        List<Triangle> triangles = new ArrayList<>();

        double rad = Math.toRadians(rotateY);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                // Vertices — scale, rotate, then offset
                if (line.startsWith("v ")) {
                    String[] p = line.split("\\s+");
                    double x = Double.parseDouble(p[1]) * scale;
                    double y = Double.parseDouble(p[2]) * scale;
                    double z = Double.parseDouble(p[3]) * scale;

                    // Rotate around Y axis
                    double rx =  x * cos + z * sin;
                    double rz = -x * sin + z * cos;

                    vertices.add(new Vector3D(
                        rx + offset.getX(),
                        y  + offset.getY(),
                        rz + offset.getZ()
                    ));
                }

                // Vertex normals — rotate but don't scale or offset
                // Vertex normals
                else if (line.startsWith("vn ")) {
                    String[] p = line.split("\\s+");
                    double nx = Double.parseDouble(p[1]);
                    double ny = Double.parseDouble(p[2]);
                    double nz = Double.parseDouble(p[3]);

                    double mag = Math.sqrt(nx * nx + ny * ny + nz * nz);

                    // Skip zero-length normals — use a default up vector instead
                    if (mag < 1e-10) {
                        normals.add(new Vector3D(0, 1, 0)); // Safe fallback
                    } else {
                        normals.add(new Vector3D(nx / mag, ny / mag, nz / mag));
                    }
                }

                // Faces
                else if (line.startsWith("f ")) {
                    String[] parts = line.split("\\s+");

                    int[] vi = new int[parts.length - 1];
                    int[] ni = new int[parts.length - 1];
                    boolean hasNormals = false;

                    for (int i = 0; i < vi.length; i++) {
                        String[] tokens = parts[i + 1].split("/");
                        vi[i] = Integer.parseInt(tokens[0]) - 1;

                        if (tokens.length == 3 && !tokens[2].isEmpty()) {
                            ni[i] = Integer.parseInt(tokens[2]) - 1;
                            hasNormals = true;
                        }
                    }

                    // Fan-triangulate
                    for (int i = 1; i < vi.length - 1; i++) {
                        Triangle tri;
                        if (hasNormals && !normals.isEmpty()) {
                            tri = new Triangle(
                                vertices.get(vi[0]), vertices.get(vi[i]), vertices.get(vi[i+1]),
                                normals.get(ni[0]),  normals.get(ni[i]),  normals.get(ni[i+1]),
                                color
                            );
                        } else {
                            tri = new Triangle(
                                vertices.get(vi[0]), vertices.get(vi[i]), vertices.get(vi[i+1]),
                                color
                            );
                        }
                        triangles.add(tri);
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("Error reading OBJ file: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Malformed number in OBJ file: " + e.getMessage());
        } catch (IndexOutOfBoundsException e) {
            System.err.println("Index out of bounds in OBJ file: " + e.getMessage());
        }

        return triangles;
    }

    // Without rotation — defaults to 0 degrees
    public static List<Triangle> load(String path, Vector3D color, Vector3D offset, double scale) {
        return load(path, color, offset, scale, 0.0);
    }

    // Without scale or rotation
    public static List<Triangle> load(String path, Vector3D color, Vector3D offset) {
        return load(path, color, offset, 1.0, 0.0);
    }

    // Minimal — no offset, scale, or rotation
    public static List<Triangle> load(String path, Vector3D color) {
        return load(path, color, new Vector3D(0, 0, 0), 1.0, 0.0);
    }

    public static void printBounds(List<Triangle> triangles) {
        double minX = Double.MAX_VALUE,  minY = Double.MAX_VALUE,  minZ = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;

        for (Triangle t : triangles) {
            for (Vector3D v : t.getVertices()) {
                minX = Math.min(minX, v.getX()); maxX = Math.max(maxX, v.getX());
                minY = Math.min(minY, v.getY()); maxY = Math.max(maxY, v.getY());
                minZ = Math.min(minZ, v.getZ()); maxZ = Math.max(maxZ, v.getZ());
            }
        }

        System.out.println("Bounds:");
        System.out.println("  X: " + minX + " to " + maxX);
        System.out.println("  Y: " + minY + " to " + maxY);
        System.out.println("  Z: " + minZ + " to " + maxZ);
        System.out.println("  Center: (" + (minX+maxX)/2 + ", "  + (minY+maxY)/2 + ", " + (minZ+maxZ)/2 + ")");
    }
}