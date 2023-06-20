import java.util.ArrayList;
import java.util.List;
import java.awt.*;
import java.awt.event.*;
import java.util.Objects;
import java.util.TreeMap;

import javax.swing.*;

/**
 * Client-server graphical editor
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; loosely based on CS 5 code by Tom Cormen
 * @author CBK, winter 2014, overall structure substantially revised
 * @author Travis Peters, Dartmouth CS 10, Winter 2015; remove EditorCommunicatorStandalone (use echo server for testing)
 * @author CBK, spring 2016 and Fall 2016, restructured Shape and some of the GUI
 */

public class Editor extends JFrame {
    private static String serverIP = "localhost";            // IP address of sketch server
    // "localhost" for your own machine;
    // or ask a friend for their IP address

    private static final int width = 800, height = 800;        // canvas size

    // Current settings on GUI
    public enum Mode {
        DRAW, MOVE, RECOLOR, DELETE
    }

    private Mode mode = Mode.DRAW;                // drawing/moving/recoloring/deleting objects
    private String shapeType = "ellipse";        // type of object to add
    private Color color = Color.black;            // current drawing color

    // Drawing state
    // these are remnants of my implementation; take them as possible suggestions or ignore them
    private Shape curr = null;                    // current shape (if any) being drawn
    private Sketch sketch;                        // holds and handles all the completed objects
    private int movingId = -1;                    // current shape id (if any; else -1) being moved
    private Point drawFrom = null;                // where the drawing started
    private Point moveFrom = null;                // where object is as it's being dragged


    // Communication
    private EditorCommunicator comm;            // communication with the sketch server

    public Editor() {
        super("Graphical Editor");

        sketch = new Sketch();

        // Connect to server
        comm = new EditorCommunicator(serverIP, this);
        comm.start();

        // Helpers to create the canvas and GUI (buttons, etc.)
        JComponent canvas = setupCanvas();
        JComponent gui = setupGUI();

        // Put the buttons and canvas together into the window
        Container cp = getContentPane();
        cp.setLayout(new BorderLayout());
        cp.add(canvas, BorderLayout.CENTER);
        cp.add(gui, BorderLayout.NORTH);

        // Usual initialization
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }

    /**
     * Creates a component to draw into
     */
    private JComponent setupCanvas() {
        JComponent canvas = new JComponent() {
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawSketch(g);
            }
        };

        canvas.setPreferredSize(new Dimension(width, height));

        canvas.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent event) {
                handlePress(event.getPoint());
            }

            public void mouseReleased(MouseEvent event) {
                handleRelease();
            }
        });

        canvas.addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent event) {
                handleDrag(event.getPoint());
            }
        });

        return canvas;
    }

    /**
     * Creates a panel with all the buttons
     */
    private JComponent setupGUI() {
        // Select type of shape
        String[] shapes = {"ellipse", "freehand", "rectangle", "segment"};
        JComboBox<String> shapeB = new JComboBox<String>(shapes);
        shapeB.addActionListener(e -> shapeType = (String) ((JComboBox<String>) e.getSource()).getSelectedItem());

        // Select drawing/recoloring color
        // Following Oracle example
        JButton chooseColorB = new JButton("choose color");
        JColorChooser colorChooser = new JColorChooser();
        JLabel colorL = new JLabel();
        colorL.setBackground(Color.black);
        colorL.setOpaque(true);
        colorL.setBorder(BorderFactory.createLineBorder(Color.black));
        colorL.setPreferredSize(new Dimension(25, 25));
        JDialog colorDialog = JColorChooser.createDialog(chooseColorB,
                "Pick a Color",
                true,  //modal
                colorChooser,
                e -> {
                    color = colorChooser.getColor();
                    colorL.setBackground(color);
                },  // OK button
                null); // no CANCEL button handler
        chooseColorB.addActionListener(e -> colorDialog.setVisible(true));

        // Mode: draw, move, recolor, or delete
        JRadioButton drawB = new JRadioButton("draw");
        drawB.addActionListener(e -> mode = Mode.DRAW);
        drawB.setSelected(true);
        JRadioButton moveB = new JRadioButton("move");
        moveB.addActionListener(e -> mode = Mode.MOVE);
        JRadioButton recolorB = new JRadioButton("recolor");
        recolorB.addActionListener(e -> mode = Mode.RECOLOR);
        JRadioButton deleteB = new JRadioButton("delete");
        deleteB.addActionListener(e -> mode = Mode.DELETE);
        ButtonGroup modes = new ButtonGroup(); // make them act as radios -- only one selected
        modes.add(drawB);
        modes.add(moveB);
        modes.add(recolorB);
        modes.add(deleteB);
        JPanel modesP = new JPanel(new GridLayout(1, 0)); // group them on the GUI
        modesP.add(drawB);
        modesP.add(moveB);
        modesP.add(recolorB);
        modesP.add(deleteB);

        // Put all the stuff into a panel
        JComponent gui = new JPanel();
        gui.setLayout(new FlowLayout());
        gui.add(shapeB);
        gui.add(chooseColorB);
        gui.add(colorL);
        gui.add(modesP);
        return gui;
    }

    /**
     * Getter for the sketch instance variable
     */
    public Sketch getSketch() {
        return sketch;
    }

    /**
     * Draws all the shapes in the sketch,
     * along with the object currently being drawn in this editor (not yet part of the sketch)
     */
    public void drawSketch(Graphics g) {
        // if the sketch exists create a for loop and as long as it has
        // an id at that number it gets the shape and draws it
        if(sketch!=null){
            for (int i = 0; sketch.getShape(i) != null; i++) {
                sketch.getShape(i).draw(g);
            }
        }
        // Draw the current shape if it exists
        if (curr != null) {
            curr.draw(g);
        }
    }

    // Helpers for event handlers

    /**
     * Helper method for press at point
     * In drawing mode, start a new object;
     * in moving mode, (request to) start dragging if clicked in a shape;
     * in recoloring mode, (request to) change clicked shape's color
     * in deleting mode, (request to) delete clicked shape
     * drawFrom = p;
     * moveFrom = p;
     * if (mode == Mode.DRAW && Objects.equals(shapeType, "ellipse")) {
     * curr = new Ellipse(drawFrom.x, drawFrom.y, moveFrom.x, moveFrom.y, color);
     * }
     * if (mode == Mode.DRAW && Objects.equals(shapeType, "rectangle")) {
     * curr = new Rectangle(drawFrom.x, drawFrom.y, moveFrom.x, moveFrom.y, color);
     * }
     * if (mode == Mode.DRAW && Objects.equals(shapeType, "segment")) {
     * curr = new Segment(drawFrom.x, drawFrom.y, moveFrom.x, moveFrom.y, color);
     * }
     * if (mode == Mode.DRAW && Objects.equals(shapeType, "freehand")) {
     * curr = new Polyline(color);
     * }
     */
    private void handlePress(Point p) {
        System.out.println("we have pressed");
        // in the drawing mode it takes the shape and creates it with just the upper left bound and as the color set
        // for segment and freehand it draws the starting point
        if (mode == Mode.DRAW) {
            drawFrom = p;
            if (mode == Mode.DRAW && Objects.equals(shapeType, "ellipse")) {
                curr = new Ellipse(drawFrom.x, drawFrom.y, color);
            }
            if (mode == Mode.DRAW && Objects.equals(shapeType, "rectangle")) {
                curr = new Rectangle(drawFrom.x, drawFrom.y, color);
            }
            if (mode == Mode.DRAW && Objects.equals(shapeType, "segment")) {
                curr = new Segment(drawFrom.x, drawFrom.y, color);
            }
            if (mode == Mode.DRAW && Objects.equals(shapeType, "freehand")) {
                curr = new Polyline(p, color);
            }
        } else {
            // sets the id to the object who is currently being pressed
            int id = sketch.getID(p.x, p.y);
            if (id != -2){ // because the getID function will return -2 if it is not within the shape, we check for it
                // moving id assumes the same id
                movingId = id;
            }
            // if the mode is MOVE we have to update the moveFrom to the new point that we will move it to
            if (mode == Mode.MOVE) {
                moveFrom = p;
            }
            // if the mode is RECOLOR we have to send out a message to the editorComm telling it to recolor and send
            // its movingID we got through a sketch class function as well as its color decided by the editor artist
            if (mode == Mode.RECOLOR) {
                comm.send("recolor " +movingId+" " + color.getRGB());
                //+ sketch.getShape(movingId) + " "
            }
            // if the mode is DELETE then we simply send the id of the current shape along with the word "delete" to
            // the editorComm
            if (mode == Mode.DELETE) {
                comm.send("delete " + movingId);
            }
        }
        // repaint at the end because we changed many aspects of the shapes on the canvas
        repaint();
    }

    /**
     * Helper method for drag to new point
     * In drawing mode, update the other corner of the object;
     * in moving mode, (request to) drag the object
     */
    private void handleDrag(Point p) {
        // TODO: YOUR CODE HERE
        // if mode Draw then we actually make the shapes with the new point that we dragged the shape to
        if (mode == Mode.DRAW) {
            if (Objects.equals(shapeType, "ellipse")) {
                ((Ellipse) curr).setCorners(drawFrom.x, drawFrom.y, p.x, p.y);
            }
            if (Objects.equals(shapeType, "rectangle")) {
                ((Rectangle) curr).setCorners(drawFrom.x, drawFrom.y, p.x, p.y);
            }
            if (Objects.equals(shapeType, "segment")) {
                ((Segment) curr).setEnd(p.x, p.y);
            }
            if (Objects.equals(shapeType, "freehand")) {
                ((Polyline) curr).add(p);
            }
            // repaint because we changed aspects of the shapes on the canvas
            repaint();
        } // if we are in MOVE and it contains the point then we update the x and y to move to and send the
        // editorComm to move as well as its id and the newly calculated x and y
        else if (mode == Mode.MOVE && sketch.getShape(movingId).contains(p.x, p.y)) {
            int x = p.x - moveFrom.x;
            int y = p.y - moveFrom.y;
            moveFrom = p;
            comm.send("move " + movingId + " " + x + " " + y);
            repaint();
        }
    }

    /**
     * Helper method for release
     * In drawing mode, pass the add new object request on to the server;
     * in moving mode, release it
     */
    private void handleRelease() {
        System.out.println("we have released");
        if (mode == Mode.DRAW) {
            // we send the add command here because we want to update other editors once we have finished dragging/moving
            // the new shape
            comm.send("add " + movingId+" "+ curr.toString());
            // we set curr to null so that it is empty and not predetermined
            curr = null;
        }
        // this should stop the shape from following the mouse
        if (mode == Mode.MOVE) {
            moveFrom = null;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Editor();
            }
        });
    }
}
