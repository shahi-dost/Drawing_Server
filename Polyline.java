import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A multi-segment Shape, with straight lines connecting "joint" points -- (x1,y1) to (x2,y2) to (x3,y3) ...
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Spring 2016
 * @author CBK, updated Fall 2016
 */
public class Polyline implements Shape {
    // TODO: YOUR CODE HERE
    private ArrayList<Point> polySegments;

    private Color color;

    // creates a polyline with an empty polysegment list and  a color
    public Polyline(Color color) {
        this.polySegments = new ArrayList<>();
        this.color = color;
    }
    // creates a polyline with a list that has a starting point and a color
    public Polyline(Point p , Color color) {
        this.polySegments = new ArrayList<>();
        polySegments.add(p);
        this.color = color;
    }
// adds a point to the polysegment list
    public void add(Point p){
        polySegments.add(p);
    }

    @Override// move by dx and dy amount on both x and y
    public void moveBy(int dx, int dy) {
        for (Point allinList : polySegments) {
            allinList.x += dx;
            allinList.y += dy;
        }
    }
    @Override
    // gets the current color
    public Color getColor() {
        return color;
    }

    @Override
    // sets the color to a parameter
    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    // boolean if the x and y are contained in the polyline objeect
    public boolean contains(int x, int y) {
        for(int i =0; i < polySegments.size()-1; i++) {
            if (Segment.pointToSegmentDistance(x,y,polySegments.get(i).x,polySegments.get(i).y,polySegments.get(i+1).x,polySegments.get(i+1).y) <2) {
                return true;
            }
        }
        return false;
    }

    @Override
    // sets the fill color and as long as there are 2 or more points
    // it takes those points and draws a segment starting at that point until the next one in the list
    public void draw(Graphics g) {
        g.setColor(color);
        if (polySegments.size() >= 2) {
            for (int i = 0; i < polySegments.size() - 1; i++) {
                g.drawLine(polySegments.get(i).x, polySegments.get(i).y, polySegments.get(i + 1).x, polySegments.get(i + 1).y);//
            }
        }
        // list of x, ll list of y, total points in the polygon
    }

    @Override
    // takes all the points in polySegments and turns them into a string of "x y "
    // converts all relevant info in a string
    public String toString() {
        StringBuilder allPoints = new StringBuilder();
        for (int i = 0; i < polySegments.size() - 1; i++) {
            allPoints.append(polySegments.get(i).x).append(" ");
            allPoints.append(polySegments.get(i).y).append(" ");
        }
        return "freehand "+ color.getRGB() +" "+ allPoints;
    }
}
