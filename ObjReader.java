import java.io.*;
import java.util.*;

public class OBJReader {


    /**
     * Loads a 3D model from an Obj file in path and reads it´s vertices and faces to create the geometry using triangles.
     * This geometry is then returned as a list of triangles with the specified positions.
     * This is rendered in the Raytracer class.
     * @param path
     * @param color
     * @return List<Triangle>
     */

    /**
     * This model does not support textures, normals or other advanced features contained in the OBJ file.
     */
    public static List<Triangle> load(String path, Vector3D color) {

        List<Vector3D> vertices = new ArrayList<>();
        List<Triangle> triangles = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {

            String line; // Reads the file line by line.

            while ((line = br.readLine()) != null) {

                line = line.trim();

                // Vertices
                if (line.startsWith("v ")) {
                    String[] parts = line.split("\\s+"); // Split by whitespace

                    // The vertex line format: x, y, z
                    double x = Double.parseDouble(parts[1]);
                    double y = Double.parseDouble(parts[2]);
                    double z = Double.parseDouble(parts[3]);

                    vertices.add(new Vector3D(x, y, z));
                }

                // Faces (Triangles)
                else if (line.startsWith("f ")) {
                    String[] parts = line.split("\\s+");

                    // The face line format: f v1 v2 v3.
                    // Where v1 v2 v3 are the vertex indices that form the triangle.
                    int i1 = Integer.parseInt(parts[1]) - 1;
                    int i2 = Integer.parseInt(parts[2]) - 1;
                    int i3 = Integer.parseInt(parts[3]) - 1;

                    // Builds the triangle using the vertices and specified color.
                    Triangle tri = new Triangle(
                        vertices.get(i1),
                        vertices.get(i2),
                        vertices.get(i3),
                        color
                    );
                    // Adds the triangle to the list of triangles that form the geometry.
                    triangles.add(tri);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    
        return triangles;
    }
}