//written by tom
//ignore this class
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Simulation implements Runnable{
	
	private static final long serialVersionUID = 1L;
	private Thread thread;
	private boolean running;
	public Robot robot;
	private JFrame simulationFrame;
	private SimulationPanel simPanel;
	private static final int WIDTH = 1000;
	private static final int HEIGHT = 900;
	private final static Dimension OUTER_FRAME_DIMENSION = new Dimension(WIDTH, HEIGHT);
	private static final Dimension SIMULAITON_PANEL_DIMENSION = new Dimension(WIDTH,HEIGHT);
	
	public Simulation() {
		
		this.robot = new Robot(WIDTH/2, HEIGHT/2);
		this.simulationFrame = new JFrame("ARS_Simulation_2000");
		this.simulationFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.simulationFrame.setLayout(new BorderLayout());
		this.simulationFrame.setSize(OUTER_FRAME_DIMENSION);
		this.simulationFrame.getContentPane().setBackground(Color.BLUE);
		this.simulationFrame.setResizable(false);
		
		this.simPanel = new SimulationPanel(this.robot);
		this.simulationFrame.add(this.simPanel,BorderLayout.CENTER);
		
		this.simulationFrame.setVisible(true);
		
		this.simPanel.requestFocus();
		
		thread = new Thread(this);
		
		
		
		start();
	}
	
	private synchronized void start() {
		running = true;
		thread.start();
	}
	
	public synchronized void stop() {
		running = false;
		try {
			thread.join();
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	

	@Override
	public void run() {
		long lastTime = System.nanoTime();
		final double ns = 1000000000.0 / 10.0;//10 times per second
		double delta = 0;
		while(running) {
			long now = System.nanoTime();
			delta = delta + ((now-lastTime) / ns);
			lastTime = now;
			//System.out.println("lol"+delta);
			while (delta >= 1)//Make sure update is only happening 10 times a second
			{
				//System.out.println("nol"+delta);
				//handles all of the logic restricted time
				robot.update(0.1);
				delta--;
			}
			this.simPanel.repaint();
		}
	}
	
	private class SimulationPanel extends JPanel{
		
		private static final long serialVersionUID = 7532759100680848259L;
		Robot robot;
		
		SimulationPanel(Robot robot){
			
			this.robot = robot;
			setPreferredSize(SIMULAITON_PANEL_DIMENSION);
			
			addKeyListener(new KeyListener() {

				@Override
				public void keyTyped(KeyEvent e) {
					// TODO Auto-generated method stub
					char c = e.getKeyChar();
					if( c ==  'w') {
						//System.out.println("w");
						robot.incVelLeftWheel(1);
					} else if(c=='s') {
						//System.out.println("s");
						robot.incVelLeftWheel(-1);
					} else if(c=='o') {
						//System.out.println("o");
						robot.incVelRightWheel(1);
					} else if(c=='l') {
						//System.out.println("l");
						robot.incVelRightWheel(-1);
					} else if(c=='x') {
						//System.out.println("x");
						robot.stop();
					} else if(c=='t') {
						//System.out.println("t");
						robot.incVelLeftWheel(1);
						robot.incVelRightWheel(1);
					} else if(c=='g') {
						//System.out.println("g");
						robot.incVelLeftWheel(-1);
						robot.incVelRightWheel(-1);
					} 
				}

				@Override
				public void keyPressed(KeyEvent e) {
					// TODO Auto-generated method stub
				}

				@Override
				public void keyReleased(KeyEvent e) {
					// TODO Auto-generated method stub
					
				}
				
			});
			
			validate();
		}
		
		@Override
		public void paintComponent(Graphics g) {
			this.setBackground(Color.WHITE);
			Graphics2D g2d = (Graphics2D) g;
			drawRobot(g2d, robot);
		}
		
		private void drawRobot(Graphics2D g, Robot robot) {
			
			//System.out.println("Drawing");
			 // Store before changing.
	        Stroke tmpS = g.getStroke();
	        Color tmpC = g.getColor();
	        
	        g.setStroke(new BasicStroke(0, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
	        g.setColor(Color.BLACK);
	        g.drawLine(robot.posX+25, robot.posY+25, robot.posX+25+25, robot.posY+25);
	        g.drawOval(robot.posX, robot.posY, 50, 50);
	        
	     // Set values to previous when done.
	        g.setColor(tmpC);
	        g.setStroke(tmpS);
		}
	}
	
	
	public static void main(String [] args) {
		Simulation game = new Simulation();
	}
}
