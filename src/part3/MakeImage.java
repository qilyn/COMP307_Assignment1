package part3;
/* Code for COMP 307 Assignment
 * Name: Vicki McKay
 * Usercode: mckayvick
 * ID: 300299717
 */

import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.io.*;


/** Simple editor to construct and edit pmb image files.
    Not very robust!!!
 */

public class MakeImage implements MouseListener, MouseMotionListener{
  // Fields
  private JFrame frame;
  private DrawingCanvas canvas;
  private JTextArea messageArea;

  private static int margin = 10;
  private static int wd = 20;

  private int rows = 10;
  private int cols = 10;
  private boolean[][] image = new boolean[rows][cols];
  private String category = "other";
  private String categoryName;
  private int otherCount=0;
  private int categoryCount=0;

  // Constructors
  /** Construct a new MakeImage object
   * and set up the GUI
   */
  public MakeImage(String cn){
    categoryName = cn;

    frame = new JFrame("MakeImage");
    frame.setSize(600, 400);


    //The graphics area
    canvas = new DrawingCanvas();
    canvas.addMouseListener(this);
    canvas.addMouseMotionListener(this);
    frame.getContentPane().add(canvas, BorderLayout.CENTER);

    //The message area, mainly for debugging.
    messageArea = new JTextArea(1, 80);     //one line text area for messages.
    frame.getContentPane().add(messageArea, BorderLayout.SOUTH);

    JPanel panel = new JPanel(new java.awt.GridLayout(2,0));
    frame.getContentPane().add(panel, BorderLayout.NORTH);

    addButton(panel, "Clear", new ActionListener() {public void actionPerformed(ActionEvent e){clear();}});
    addButton(panel, "Left",  new ActionListener() {public void actionPerformed(ActionEvent e){shift(-1, 0);}});
    addButton(panel, "Right", new ActionListener() {public void actionPerformed(ActionEvent e){shift(1, 0);}});
    addButton(panel, "Up",    new ActionListener() {public void actionPerformed(ActionEvent e){shift(0, -1);}});
    addButton(panel, "Down",  new ActionListener() {public void actionPerformed(ActionEvent e){shift(0,1);}});
    addButton(panel, "Load",  new ActionListener() {public void actionPerformed(ActionEvent e){load();}});
    addButton(panel, "SaveAs",new ActionListener() {public void actionPerformed(ActionEvent e){save(FileDialog.save());}});
    addButton(panel, "Quit",  new ActionListener() {public void actionPerformed(ActionEvent e){ frame.dispose();}});


    addButton(panel, "Save",  new ActionListener() {public void actionPerformed(ActionEvent e){save(null);}});
    panel.add(new JLabel("Class: ", SwingConstants.RIGHT));
    final JButton cbutton = new JButton(category);
    cbutton.addActionListener(new ActionListener (){
	public void actionPerformed(ActionEvent e){
	  category=(category==categoryName)?"other":categoryName;
	  cbutton.setText(category);
	}});
    panel.add(cbutton);


    final JSlider rowslider = new JSlider(1,70, 15);
    rowslider.addChangeListener(new ChangeListener(){
	public void stateChanged(ChangeEvent e){rows= rowslider.getValue();resize();}});
    panel.add(new JLabel("Rows: ", SwingConstants.RIGHT));
    panel.add(rowslider);

    final JSlider colslider = new JSlider(1,70, 15);
    colslider.addChangeListener(new ChangeListener(){
	public void stateChanged(ChangeEvent e){cols= colslider.getValue();resize();}});
    panel.add(new JLabel("Cols: ", SwingConstants.RIGHT));
    panel.add(colslider);



    frame.setVisible(true);
    redraw();
  }

  // GUI Methods

  /** Helper method for adding buttons */
  private JButton addButton(JPanel panel, String name, ActionListener listener){
    JButton button = new JButton(name);
    button.addActionListener(listener);
    panel.add(button);
    return button;
  }


  private boolean setting = true;

  /** Respond to mouse events */
  public void mousePressed(MouseEvent e) {
    if (inregion(e)){
      setting = !image[getRow(e)][getCol(e)];
    }
  }

  public void mouseReleased(MouseEvent e) {
  }

  public void mouseClicked(MouseEvent e) {
    messageArea.setText("Clicked at ("+ e.getX() +" "+ e.getY() +")");
    if (inregion(e)){
      int c =getCol(e);
      int r =getRow(e);
      image[r][c] = !image[r][c];
      redraw(r,c, true);
    }
  }
  public void mouseEntered(MouseEvent e) {}  //needed to satisfy interface
  public void mouseExited(MouseEvent e) {}   //needed to satisfy interface

  public void mouseMoved(MouseEvent e) {}   //needed to satisfy interface
  public void mouseDragged(MouseEvent e) {
    if (inregion(e)){
      int c =getCol(e);
      int r =getRow(e);
      if (image[r][c] != setting){
	image[r][c]= setting;
	redraw(r,c, true);
      }
    }
  }

  // Other Methods

  private boolean inregion(MouseEvent e){
      int c =getCol(e);
      if (c<0 || c>=cols) return false;
      int r =getRow(e);
      if (r<0 || r>=rows) return false;
      return true;
  }

  private int getCol(MouseEvent e){
    return (e.getX()-margin)/wd;
  }

  private int getRow(MouseEvent e){
    return (e.getY()-margin)/wd;
  }

  /** <Method description>
   */
  public void redraw(){
    canvas.clear(false);
    canvas.drawRect(margin-1,margin-1, 1+cols*wd, 1+rows*wd,false);
    for (int r=0; r<rows; r++){
      for (int c=0; c<cols; c++)
	redraw(r, c, false);
    }
    canvas.display();
  }

  public void redraw(int r, int c, boolean redisplay){
    if (image[r][c])
      canvas.fillRect(margin+c*wd, margin+r*wd, wd, wd, redisplay);
    else{
      canvas.eraseRect(margin+c*wd, margin+r*wd, wd, wd, redisplay);
      canvas.drawRect(margin+c*wd, margin+r*wd, wd, wd, redisplay);
    }
  }

  private void shift(int dx, int dy){
    boolean[][] newimage = new boolean[rows+2][cols+2];
    for (int r=0; r<rows; r++){
      for (int c=0; c<cols; c++)
	newimage[r+1+dy][c+1+dx]=image[r][c];
    }
    for (int r=0; r<rows; r++){
      for (int c=0; c<cols; c++)
	image[r][c]=newimage[r+1][c+1];
    }
    redraw();
  }


  private void clear(){
    image = new boolean[rows][cols];
    redraw();
  }

  private void resize(){
    boolean[][] newimage = new boolean[rows][cols];
    for (int r=0; r<image.length; r++){
      if (r<rows)
	for (int c=0; c<image[r].length; c++)
	  if (c < cols)
	    newimage[r][c]=image[r][c];
    }
    image = newimage;
    redraw();
  }

  private void save(String fname){
    try{
      if (fname==null){
	do
	  fname = category+((category==categoryName)?(++categoryCount):(++otherCount))+".pbm";
	while (new File(fname).exists());
      }
      PrintWriter f = new PrintWriter(new FileWriter(fname));
      f.println("P1");
      f.println("#"+category);
      f.println(rows + " "+ cols);
      int count=0;
      for (int r=0; r<rows; r++){
	for (int c=0; c<cols; c++){
	  f.print(image[r][c]?"1":"0");
	  if (++count> 70){f.println();count=0;}
	}
      }
      f.close();
    }
    catch(Exception e){System.out.format("Save to file %s failed\n", fname);}
  }

  public void load(){
    boolean[][] newimage = null;
    try{
      java.util.regex.Pattern bit = java.util.regex.Pattern.compile("[01]");
      Scanner f = new Scanner(new File(FileDialog.open()));
      if (!f.next().equals("P1")) System.out.println("Not a P1 PBM file" );
      category = f.next().substring(1);
      if (!category.equals("other")) categoryName=category;
      rows = f.nextInt();
      cols = f.nextInt();

      newimage = new boolean[rows][cols];
      for (int r=0; r<rows; r++){
	for (int c=0; c<cols; c++){
	  newimage[r][c] = (f.findWithinHorizon(bit,0).equals("1"));
	}
      }
      f.close();
    }
    catch(IOException e){System.out.println("Load from file failed"); }
    if (newimage!=null) {
      image=newimage;
      redraw();
    }
  }


  // Main
  public static void main(String[] args){
    new MakeImage((args.length >0)?args[0]:"Yes");
  }


}
