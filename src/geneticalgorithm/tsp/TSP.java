package geneticalgorithm.tsp;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class TSP extends JFrame implements ActionListener, MouseListener{
	
	static int panelSizeX = 300;
	static int panelSizeY = 300;
	
	private JPanel paintPanel;
	
	private BufferedImage baseImage;
	private BufferedImage paintImage;
	
	private List<Coordinate> pointList = new ArrayList<Coordinate>();
	
	private volatile Path bestPath;
	private volatile double pathCost; 
	private volatile double startCost;
	private volatile boolean runFlag;
	
	private long startTime;
	
	private CountDownLatch latch; 
	
	private JFormattedTextField popSize;
	private JFormattedTextField keepTop;
	private JFormattedTextField mutRate;
	private JFormattedTextField pop;
	private JCheckBox allowTwins; 
	private JMenuBar menuBar;
	
	private int resetLevel;
	
	private volatile PrintWriter pw = null;
	private File outputFile = null;
	
	public synchronized void setRunFlag(boolean flag){
		this.runFlag = flag;
	}
	
	public synchronized void write(String data){
		if (pw == null) {
			try {
				pw = new PrintWriter(outputFile);
				pw.println("Thread,Time (ms),Cost (px)");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		pw.println(data);
		pw.flush();
	}
	
	public synchronized boolean getRunFlag(){
		return this.runFlag;
	}
	
	public synchronized void setBestPath(Path bestPath){
		this.bestPath = bestPath;
	}
	
	public synchronized void setPathCost(double pathCost){
		this.pathCost = pathCost;
	}
	
	public synchronized void setStartCost(double pathCost){
		if (this.startCost == Double.MAX_VALUE){
			this.startCost = pathCost;
		}
	}
	
	public synchronized double getStartCost(){
		return this.startCost;
	}
	
	public synchronized double getPathCost(){
		return this.pathCost;
	}
	
	public static void main(String[] args) {	
		new TSP();
	}
	
	public TSP(){
		
		try {
			outputFile = new File("data.csv");
			pw = new PrintWriter(outputFile);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		pw.println("Thread,Time (ms),Cost (px)");
		
		resetLevel = 1;
		pathCost = Double.MAX_VALUE;
		setStartCost(Double.MAX_VALUE);
		latch = new CountDownLatch(0);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// Add the panel that displays the points
		paintImage = new BufferedImage(panelSizeX, panelSizeY, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = paintImage.createGraphics();
		graphics.setPaint (new Color(255,255,255));
		graphics.fillRect ( 0, 0, paintImage.getWidth(), paintImage.getHeight() );
		
		paintPanel = new JPanel(){
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.drawImage(paintImage, 0, 0, null);
			}
		};
		paintPanel.setPreferredSize(new Dimension(panelSizeX,panelSizeY));
		paintPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
		paintPanel.addMouseListener(this);
		add(paintPanel);
		
		JPanel commands = new JPanel();
		
		// Add buttons
		Component spacer = Box.createRigidArea(new Dimension(0,5));
		commands.add(spacer);
		commands.setLayout(new BoxLayout(commands, BoxLayout.Y_AXIS));
		JButton runButton = new JButton("Run");
		JButton resetButton = new JButton("Reset");
		JButton randomPlacement = new JButton("Add 10 Points");
		runButton.setMaximumSize(new Dimension(150,30));
		resetButton.setMaximumSize(new Dimension(150,30));
		randomPlacement.setMaximumSize(new Dimension(150,30));
		commands.add(Box.createRigidArea(new Dimension(0,5)));
		commands.add(runButton);
		commands.add(Box.createRigidArea(new Dimension(0,5)));
		commands.add(resetButton);
		commands.add(Box.createRigidArea(new Dimension(0,5)));
		commands.add(randomPlacement);
		
		// Add text fields
		commands.add(Box.createRigidArea(new Dimension(0,10)));
	    JLabel popSizeLabel = new JLabel("Population Size:");
	    commands.add(popSizeLabel);
	    popSize = new JFormattedTextField(NumberFormat.getIntegerInstance());
	    popSize.setMaximumSize(new Dimension(250,20));
	    popSize.setValue(10);
	    commands.add(popSize);
	    
	    
	    JLabel keepTopLabel = new JLabel("Survivors:");
	    commands.add(keepTopLabel);
	    keepTop = new JFormattedTextField(NumberFormat.getIntegerInstance());
	    keepTop.setMaximumSize(new Dimension(250,20));
	    keepTop.setValue(4);
	    commands.add(keepTop);
	    
	    commands.add(spacer);
	    JLabel mutRateLabel = new JLabel("Mutation Rate:");
	    commands.add(mutRateLabel);
	    mutRate = new JFormattedTextField(NumberFormat.getNumberInstance());
	    mutRate.setMaximumSize(new Dimension(250,20));
	    mutRate.setValue(0.75);
	    commands.add(mutRate);
	    
	    commands.add(spacer);
	    JLabel popLabel = new JLabel("# Populations:");
	    commands.add(popLabel);
	    pop = new JFormattedTextField(NumberFormat.getIntegerInstance());
	    pop.setMaximumSize(new Dimension(250,20));
	    pop.setValue(1);
	    commands.add(pop);
	    
	    commands.add(spacer);
	    allowTwins = new JCheckBox("Allow Twins", true);
	    commands.add(allowTwins);
	    
	    add(commands, BorderLayout.EAST);
	    
	    // Add action listener to buttons
		runButton.addActionListener(this);
		resetButton.addActionListener(this);
		randomPlacement.addActionListener(this);
		
		
		menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");

		JMenuItem newMenuItem = new JMenuItem("Save");
		newMenuItem.addActionListener(new ActionListener() {
		      public void actionPerformed(ActionEvent e) {            
		          JFileChooser fileChooser = new JFileChooser();
		          int returnValue = fileChooser.showSaveDialog(null);
		          if (returnValue == JFileChooser.APPROVE_OPTION) {
		            File selectedFile = fileChooser.getSelectedFile();
		            TSP.this.toFile(selectedFile);
		          }
		      }    
		});
		fileMenu.add(newMenuItem);
		
		
		JMenuItem openMenuItem = new JMenuItem("Open");
		openMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
		          int returnValue = fileChooser.showOpenDialog(null);
		          if (returnValue == JFileChooser.APPROVE_OPTION) {
		            File selectedFile = fileChooser.getSelectedFile();
		            TSP.this.loadFile(selectedFile);
		          }
				
			}
		});
		fileMenu.add(openMenuItem);
		menuBar.add(fileMenu);
		
		JMenu outputMenu = new JMenu("Output");
		JMenuItem outputStream = new JMenuItem("Change Output File");
		outputStream.addActionListener(new ActionListener() {
		      public void actionPerformed(ActionEvent e) {            
		          JFileChooser fileChooser = new JFileChooser();
		          int returnValue = fileChooser.showSaveDialog(null);
		          if (returnValue == JFileChooser.APPROVE_OPTION) {
		            File selectedFile = fileChooser.getSelectedFile();
		            try {
						TSP.this.outputFile = selectedFile;
						TSP.this.pw = null;
					} catch (Exception e2) {
						e2.printStackTrace();
					}
		          }
		      }    
		});
		outputMenu.add(outputStream);
		menuBar.add(outputMenu);
		
		
		
		this.setJMenuBar(menuBar);
		
		
		
		
		
		setResizable(false);
		pack();
		setVisible(true);
	}
	
	public void toFile(File outFile){
		try{
			FileOutputStream fout = new FileOutputStream(outFile);
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(pointList);
			oos.close();
		} catch (Exception e) {
			System.err.println("Problem saving file");
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void loadFile(File inFile){
		try {
			FileInputStream fis = new FileInputStream(inFile);
			ObjectInputStream ois = new ObjectInputStream(fis);
			
			resetLevel = 1;
			reset();
			List<Coordinate> loadList = (List<Coordinate>) ois.readObject();
			ois.close();
			for (Coordinate c : loadList){
				addPoint(c);
			}
			paintPanel.repaint();
		} catch (Exception e) {
			System.err.println("Problem loading file");
			e.printStackTrace();
		}
		
	}
	
	private void addPoint(Coordinate c){
		pointList.add(c);
		Graphics2D g2d = paintImage.createGraphics();
        g2d.setPaint(Color.BLACK);
        Shape circle = new Ellipse2D.Double(c.x-2, c.y-2, 4, 4);
        g2d.draw(circle);
        g2d.fill(circle);
        g2d.dispose();
	}
	
	private synchronized boolean updatePath(Path newPath){
		if (newPath.getCost() < getPathCost()){
			long now = System.currentTimeMillis();
			long duration = now - startTime;
			setBestPath(newPath);
			setPathCost(newPath.getCost());
			DecimalFormat decimalFormat = new DecimalFormat("#.####");
			decimalFormat.setMinimumFractionDigits(4);
			setTitle( "Cost: "+decimalFormat.format((pathCost))+" px");
			write(Thread.currentThread().getId()+","+duration+","+newPath.getCost());
			return true;
		}
		return false;
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		if (!getRunFlag()){
			final int x = e.getX();
			final int y = e.getY();
		    Coordinate c = new Coordinate(x,y);
		    addPoint(c);
	        paintPanel.repaint();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if (e.getActionCommand().equals("Run") && validateTextFields() && pointList.size() > 1){
			resetLevel = 2;
	    	popSize.setEditable(false);
	    	keepTop.setEditable(false);
	    	mutRate.setEditable(false);
	    	pop.setEditable(false);
	    	allowTwins.setEnabled(false);
	    	menuBar.setEnabled(false);
	    	
	    	String popSizeText = popSize.getText().replace(",", "");
	    	String keepTopText = keepTop.getText().replace(",", "");
	    	String mutRateText = mutRate.getText().replace(",", "");
	    	String popText = pop.getText().replace(",", "");
	    	
	    	final int popSizeInt = Integer.parseInt(popSizeText);
	    	final int keepTopInt = Integer.parseInt(keepTopText);
	    	final double mutRate = Double.parseDouble(mutRateText);
	    	final int popInt = Integer.parseInt(popText);
	    	final boolean twins = allowTwins.isSelected();

			setRunFlag(true);
			latch = new CountDownLatch(popInt);
			startTime = System.currentTimeMillis();
			for (int i = 0; i < popInt; i++){
				new Thread(new Runnable() {
		            public void run() {
		            	runTSP(popSizeInt, keepTopInt, mutRate, twins);
		            	latch.countDown();
		            }
		        }).start();
			}
			
		}
		else if (e.getActionCommand().equals("Reset")){
			try{
				setRunFlag(false);
				latch.await();
				reset();
				repaint();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		else if (e.getActionCommand().equals("Add 10 Points")){
			try {
				if (!getRunFlag()){
					latch.await();
					Random random = new Random();
					int populateNum = 10;
					for (int i = 0; i < populateNum; i++){
						int x = random.nextInt(panelSizeX);
						int y = random.nextInt(panelSizeY);
						Coordinate c = new Coordinate(x,y);
			    	    addPoint(c);
					}
					repaint();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	}
	/**
	 * This method runs the logic to evolve a list of nodes into the shortest cost path
	 * 
	 * @param population_size - the fixed size of the population
	 * @param keep_top - How many of the top performing paths are crossed
	 *                   and mutated to fill the population of the next generation
	 * @param mutation_chance - The chance for mutation
	 */
	private void runTSP(int population_size, int keep_top, double mutation_chance, boolean allowTwins){
		baseImage = deepCopy(paintImage);
		Population pop = new Population(population_size, pointList);
		setStartCost(pop.getBest().getCost());
		while (getRunFlag()){
			pop.newGeneration(keep_top, mutation_chance, allowTwins);
			Path top = pop.getBest();
			boolean updated = updatePath(top);
			if (updated) {
				draw_path();
				repaint();
			}
		}
	}
	
    private void draw_path(){
    	paintImage = deepCopy(baseImage);
    	Graphics2D g2d = paintImage.createGraphics();
    	g2d.setPaint(Color.RED);
    	
    	for (int i = 0; i < bestPath.getPath().size()-1;i++){
    		Coordinate from = bestPath.getPath().get(i);
    		Coordinate to = bestPath.getPath().get(i+1);
    		g2d.drawLine(from.x, from.y, to.x, to.y);
    	}
    	Coordinate from = bestPath.getPath().get(0);
		Coordinate to = bestPath.getPath().get(bestPath.getPath().size()-1);
		g2d.drawLine(from.x, from.y, to.x, to.y);
        g2d.dispose();
    }
	
	public void reset(){

		if (resetLevel == 2){
			paintImage = deepCopy(baseImage);
			resetLevel--;
		}
		else if (resetLevel == 1) {
			paintImage = new BufferedImage(panelSizeX, panelSizeY, BufferedImage.TYPE_INT_ARGB);
			Graphics2D graphics = paintImage.createGraphics();
			graphics.setPaint (new Color(255,255,255));
			graphics.fillRect ( 0, 0, paintImage.getWidth(), paintImage.getHeight() );
			baseImage = deepCopy(paintImage);
			pointList.clear();
		}
		setTitle("");
		pathCost = Double.MAX_VALUE;
		bestPath = null;
    	popSize.setEditable(true);
    	keepTop.setEditable(true);
    	mutRate.setEditable(true);
    	menuBar.setEnabled(true);
    	pop.setEditable(true);
    	allowTwins.setEnabled(true);
    	setStartCost(Double.MAX_VALUE);
    	try{
    		pw.close();
    	} catch (Exception e) {
    		//Do nothing
    	}
    	pw = null;
	}
	
    private BufferedImage deepCopy(BufferedImage bi) {
		ColorModel cm = bi.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = bi.copyData(null);
		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
   	}
    
    private boolean validateTextFields(){
    	String errorText = "";
    	    	
    	String popSizeText = popSize.getText().replace(",", "");
    	String keepTopText = keepTop.getText().replace(",", "");
    	String mutRateText = mutRate.getText().replace(",", "");
    	String popText = pop.getText().replace(",", "");
    	
    	if (popSizeText.equals("")){
    		errorText += "Population Size was left blank\n";
    	}
    	if (keepTopText.equals("")){
    		errorText += "Survivors was left blank\n";
    	}
    	if (mutRateText.equals("")){
    		errorText += "Mutation Rate was left blank\n";
    	}
    	if (popText.equals("")){
    		errorText += "Number of Populations was left blank\n";
    	}
    	if (!errorText.equals("")){
    		JOptionPane.showMessageDialog(this, errorText);
    		return false;
    	}
    	
    	int popSizeInt = Integer.parseInt(popSizeText);
    	int keepTopInt = Integer.parseInt(keepTopText);
    	double mutRate = Double.parseDouble(mutRateText);
    	int popInt = Integer.parseInt(popText);
    	
    	if (popSizeInt < 1){
    		errorText += "Population Size must be at least 1\n";
    	}
    	if (keepTopInt < 1){
    		errorText += "Survivors must be at least 1\n";
    	}
    	if (popInt < 1){
    		errorText += "Number of Populations must be at least 1\n";
    	}
    	if (keepTopInt >= popSizeInt) {
    		errorText += "Survivors must be less than Population Size\n";
    	}
    	if (mutRate < 0 || mutRate > 0.99) {
    		errorText += "Mutation Rate must be between 0 and 0.99\n";
    	}
    	if (!errorText.equals("")){
    		JOptionPane.showMessageDialog(this, errorText);
    		return false;
    	}
    	
    	if (!allowTwins.isSelected()){
    		BigInteger uniquePaths;
    		BigInteger popSizeBigInt = BigInteger.valueOf(popSizeInt);
    		if (popSizeInt == 2){
    			uniquePaths = BigInteger.valueOf(1);
    		}
    		else{
    			uniquePaths = factorial(BigInteger.valueOf(pointList.size()-1)).divide(BigInteger.valueOf(2));
    		}
    		if (popSizeBigInt.compareTo(uniquePaths) > 0) {
    			JOptionPane.showMessageDialog(this, "Allow Twins is selected.\nThere population is too large to hold all unique paths.\n"+
    					                             "There are only "+uniquePaths.toString()+" unique paths for "+Integer.toString(pointList.size())+" points");
        		return false;
    		}
    	}
    	int cores = Runtime.getRuntime().availableProcessors();
    	if (popInt >= cores){

    		JLabel message = new JLabel("Running with as many or more populations than you have available cores ("+Integer.toString(cores)+") may slow down your computer.");

    		int reply = JOptionPane.showConfirmDialog(null,message , "WARNING",  JOptionPane.OK_CANCEL_OPTION);
    		if (reply == JOptionPane.CANCEL_OPTION){
    			return false;
    		}
    	}
    	
    	
    	return true;
    	
    }

	@Override
	public void mouseClicked(MouseEvent e) {
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	static BigInteger factorial(BigInteger n){    
		if (n.equals(BigInteger.valueOf(0)))
			return BigInteger.valueOf(1);    
		else    
			return(n.multiply(factorial(n.subtract(BigInteger.valueOf(1)))));    
	}    
	
	
   class MenuItemListener implements ActionListener {
	      public void actionPerformed(ActionEvent e) {            
	          JFileChooser fileChooser = new JFileChooser();
	          int returnValue = fileChooser.showOpenDialog(null);
	          if (returnValue == JFileChooser.APPROVE_OPTION) {
	            File selectedFile = fileChooser.getSelectedFile();
	            System.out.println(selectedFile.getName());
	          }
	      }    
	   }

}





