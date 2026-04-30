import java.io.*;
import java.util.*;

public class OBJReader {

    /**
     * Loads a 3D model from an OBJ file at the given path and reads its vertices and faces
     * to create geometry using triangles.
     * Supports face formats: v, v/vt, v/vt/vn, v//vn
     * Supports triangles and quads (quads are fan-triangulated).
     *
     * @param path  Path to the .obj file
     * @param color Color to apply to all triangles
     * @return List of Triangle objects representing the model's geometry
     */
    public static List<Triangle> load(String path, Vector3D color) {

        List<Vector3D> vertices = new ArrayList<>();
        List<Triangle> triangles = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {

            String line;

            while ((line = br.readLine()) != null) {

                line = line.trim();

                // Skip empty lines and comments
                if (line.isEmpty() || line.startsWith("#")) continue;

                // Vertices
                if (line.startsWith("v ")) {
                    String[] parts = line.split("\\s+");

                    double x = Double.parseDouble(parts[1]);
                    double y = Double.parseDouble(parts[2]);
                    double z = Double.parseDouble(parts[3]);

                    vertices.add(new Vector3D(x, y, z));
                }

                // Faces
                else if (line.startsWith("f ")) {
                    String[] parts = line.split("\\s+");

                    // Parse each vertex token, handling formats:
                    // "v", "v/vt", "v/vt/vn", "v//vn"
                    int[] idx = new int[parts.length - 1];
                    for (int i = 0; i < idx.length; i++) {
                        idx[i] = Integer.parseInt(parts[i + 1].split("/")[0]) - 1;
                    }

                    // Fan-triangulate the face (handles triangles, quads, and n-gons)
                    for (int i = 1; i < idx.length - 1; i++) {
                        Triangle tri = new Triangle(
                            vertices.get(idx[0]),
                            vertices.get(idx[i]),
                            vertices.get(idx[i + 1]),
                            color
                        );
                        triangles.add(tri);
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("Error reading OBJ file: " + e.getMessage());
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.err.println("Error parsing OBJ file (malformed number): " + e.getMessage());
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            System.err.println("Error parsing OBJ file (vertex index out of bounds): " + e.getMessage());
            e.printStackTrace();
        }

        return triangles;
    }

    // Debugging method - very useful btw
    public static void printBounds(List<Triangle> triangles) {
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE;
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
        System.out.println("  Center: (" + (minX+maxX)/2 + ", " + (minY+maxY)/2 + ", " + (minZ+maxZ)/2 + ")");
        }
}