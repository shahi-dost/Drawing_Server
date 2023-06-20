import java.awt.*;
import java.util.Map;
import java.util.TreeMap;

public class Sketch {

    private TreeMap<Integer, Shape> shapesMap;
    private int nextId = -1;

    // shapesmap becames a new TreeMap
    public Sketch() {
        shapesMap = new TreeMap<>();
    }

    // takes the x and y and looks up the id of the shape in the shapesMap if its not there returns -2
    public int getID(int x, int y) {
        for (int a : shapesMap.descendingKeySet()) {
            if (shapesMap.get(a).contains(x, y)) {
                return a;
            }
        }
        return -2;
    }

    // gets the id
    public int getID() {
        return nextId;
    }

    // sets the id to the parameter
    public void setId(int id) {
        nextId = id;
    }

    //puts a shape in a specific spot based eon its id
    public void putShape(int id, Shape shape) {
        shapesMap.put(id, shape);
    }

    // gets the shape at thet id parameter
    public Shape getShape(int id) {
        return shapesMap.get(id);
    }

    // looks up the shape based on id and changes the color to the parameter
    public void recoloring(int id, Color color) {
        shapesMap.get(id).setColor(color);
    }

    // adds the shape and gives it the next open Id and puts it into the shapeMap
    public synchronized void addShape(Shape shape) {
        nextId = nextId + 1;
        System.out.println(nextId);
        shapesMap.put(nextId, shape);
    }

    // moves a shape in the shapesMap by the parameters
    public void moveShape(int id, int newX, int newY) {
        shapesMap.get(id).moveBy(newX, newY);
    }

    // removes the shape from the drawing by sending it into to oblivion
    public void removeShape(int id) {
        shapesMap.get(id).moveBy(100000, 100000);
    }

    // returns the shapesMap as a string
    public String toString() {
        return shapesMap.toString();
    }
}
