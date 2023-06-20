import java.awt.*;
import java.util.ArrayList;

public class Messages {
    //this will handle all of the backbone of the commands exchanged betweenthe communicators
    public String[] command;


    // takes in string and splits it into an array
    public Messages(String c) {
        this.command = c.split(" ");
    }

    // create method called update and take ina sketch object as param and if the first word is add then create shape
    public void update(Sketch s) {
        // first checks that the there is a command by checking the length
        if (command.length > 0) {
            // first to handle is the creation command made for when new clients connect and must be updated on the existing shapes
            if (command[0].equals("creation")) {
                //adds each shape type based on the command
                // within each if creates a new shape object and adds that new shape into the Treemap within sketch class (along with its ID)
                if (command[2].equals("ellipse")) {
                    Ellipse newEllipse = new Ellipse(Integer.parseInt(command[3]), Integer.parseInt(command[4]), Integer.parseInt(command[5]), Integer.parseInt(command[6]), new Color(Integer.parseInt(command[7])));
                    s.putShape(Integer.parseInt(command[1]), newEllipse);
                }
                if (command[2].equals("rectangle")) {
                    Rectangle newRect = new Rectangle(Integer.parseInt(command[3]), Integer.parseInt(command[4]), Integer.parseInt(command[5]), Integer.parseInt(command[6]), new Color(Integer.parseInt(command[7])));
                    s.putShape(Integer.parseInt(command[1]), newRect);
                }
                if (command[2].equals("segment")) {
                    Segment newSegment = new Segment(Integer.parseInt(command[3]), Integer.parseInt(command[4]), Integer.parseInt(command[5]), Integer.parseInt(command[6]), new Color(Integer.parseInt(command[7])));
                    s.putShape(Integer.parseInt(command[1]), newSegment);
                }
                if (command[2].equals("freehand")) {
                    Polyline newPoly = new Polyline(new Color(Integer.parseInt(command[3])));
                    for (int i = 4; i < command.length-1; i += 2) {
                        Point addingIn = new Point(Integer.parseInt(command[i]), Integer.parseInt(command[i + 1]));
                        newPoly.add(addingIn);
                    }
                    s.putShape(Integer.parseInt(command[1]), newPoly);
                }
                s.setId(Integer.parseInt(command[command.length - 1]));
            }
            //Next handles the move command that will retrieve the id, and x and y of each shape and calls the sketch class's moveShape function
            if (command[0].equals("move")) {
                System.out.println("move in messages");
                s.moveShape(Integer.parseInt(command[1]), Integer.parseInt(command[2]), Integer.parseInt(command[3]));
            }
            //next handles the delete command that will print out what it is doing
            // then it will use the sketch class's .removeShape method and send in the shape's id as a param
            if (command[0].equals("delete")) {
                System.out.println("delete in messages");
                s.removeShape(Integer.parseInt(command[1]));
            }
            // next handles the recolor command that will print out that its recoloring
            // then it uses the .recoloring function in sketch class and sends in the shape's id and creates new color object to then setcolor
            if (command[0].equals("recolor")) {
                System.out.println("recolor in messages");
                s.recoloring(Integer.parseInt(command[1]), new Color(Integer.parseInt(command[2])));
            }
            // next handles the add command and prints out that it is adding within the messages class
            if (command[0].equals("add")) {
                System.out.println("adding in messages");
                // there is an if statement for each shape type with creates a new object of each shape and uses the addShape
                // method of the sketch class and sends in the newly made shape
                if (command[2].equals("ellipse")) {
                    Ellipse newEllipse = new Ellipse(Integer.parseInt(command[3]), Integer.parseInt(command[4]), Integer.parseInt(command[5]), Integer.parseInt(command[6]), new Color(Integer.parseInt(command[7])));
                    s.addShape(newEllipse);
                }
                if (command[2].equals("rectangle")) {
                    Rectangle newRect = new Rectangle(Integer.parseInt(command[3]), Integer.parseInt(command[4]), Integer.parseInt(command[5]), Integer.parseInt(command[6]), new Color(Integer.parseInt(command[7])));
                    s.addShape(newRect);
                }
                if (command[2].equals("segment")) {
                    Segment newSegment = new Segment(Integer.parseInt(command[3]), Integer.parseInt(command[4]), Integer.parseInt(command[5]), Integer.parseInt(command[6]), new Color(Integer.parseInt(command[7])));
                    s.addShape(newSegment);
                }
                if (command[2].equals("freehand")) {
                    Polyline newPoly = new Polyline(new Color(Integer.parseInt(command[3])));
                    for (int i = 4; i < command.length-1; i += 2) {
                        Point addingIn = new Point(Integer.parseInt(command[i]), Integer.parseInt(command[i + 1]));
                        newPoly.add(addingIn);
                    }
                    s.addShape(newPoly);
                }
            }
        }
    }
}
