import java.io.*;
import java.net.Socket;

/**
 * Handles communication between the server and one client, for SketchServer
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; revised Winter 2014 to separate SketchServerCommunicator
 */
public class SketchServerCommunicator extends Thread {
	private Socket sock;					// to talk with client
	private BufferedReader in;				// from client
	private PrintWriter out;				// to client
	private SketchServer server;			// handling communication for

	public SketchServerCommunicator(Socket sock, SketchServer server) {
		this.sock = sock;
		this.server = server;
	}

	/**
	 * Sends a message to the client
	 * @param msg
	 */
	public void send(String msg) {
		out.println(msg);
	}
	
	/**
	 * Keeps listening for and handling (your code) messages from the client
	 */
	public void run() {
		try {
			System.out.println("someone connected");
			
			// Communication channel
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintWriter(sock.getOutputStream(), true);

			// Tell the client the current state of the world
			// get the sketch from the server, loop through all shapes in sketch and do out.println and add the shape
			// to the new editor by doing out.println to communicate to all editors
			try{
				Sketch skeetch = server.getSketch();
				for(int i = 0; skeetch.getShape(i)!=null;i++){
					out.println("creation "+ i +" "+skeetch.getShape(i).toString()+" "+skeetch.getID());
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			// Keep getting and handling messages from the client
			try {
				// Handle messages
				// use message class to comunicate to sketchserver
				String line;
				while ((line = in.readLine()) != null) {
					System.out.println("printing from sketch com");
					System.out.println(line);
					// broadcasts and handles all the requests in the messages class
					server.broadcast(line);
					Messages handled = new Messages(line);
					handled.update(server.getSketch());
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}

			// Clean up -- note that also remove self from server's list so it doesn't broadcast here
			server.removeCommunicator(this);
			out.close();
			in.close();
			sock.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}