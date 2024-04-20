
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rocketship;

import java.io.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import javax.sound.sampled.*;


public class RocketShip extends JFrame implements Runnable {

    boolean animateFirstTime = true;
    Image image;
    Graphics2D g;

    Image outerSpaceImage;    
    Rocket rocket; 
    double frameRate = 25.0;
    int timeCount;
    boolean gameOver; 
    int score;
        

    static RocketShip frame;
    public static void main(String[] args) {
        frame = new RocketShip();
        frame.setSize(Window.WINDOW_WIDTH, Window.WINDOW_HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public RocketShip() {
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.BUTTON1 == e.getButton()) {
                    //left button

// location of the cursor.
                    int xpos = e.getX();
                    int ypos = e.getY();

                }
                if (e.BUTTON3 == e.getButton()) {
                    //right button
                    reset();
                }
                repaint();
            }
        });

    addMouseMotionListener(new MouseMotionAdapter() {
      public void mouseDragged(MouseEvent e) {
        repaint();
      }
    });

    addMouseMotionListener(new MouseMotionAdapter() {
      public void mouseMoved(MouseEvent e) {

        repaint();
      }
    });

        addKeyListener(new KeyAdapter() {

            public void keyPressed(KeyEvent e) {
                if (gameOver)
                    return;
                
                if (e.VK_UP == e.getKeyCode()) {
                  rocket.IncreaseYSpeed(1);
                } else if (e.VK_DOWN == e.getKeyCode()) {
                  rocket.IncreaseYSpeed(-1);
                } else if (e.VK_LEFT == e.getKeyCode()) {
                  rocket.IncreaseXSpeed(-1);
                } else if (e.VK_RIGHT == e.getKeyCode()) {
                   rocket.IncreaseXSpeed(1);
                } else if (e.VK_SPACE == e.getKeyCode()) {
                  Missle.Create(rocket); 
                }

                repaint();
            }
        });
        init();
        start();
    }
    Thread relaxer;
////////////////////////////////////////////////////////////////////////////
    public void init() {
        requestFocus();
    }
////////////////////////////////////////////////////////////////////////////
    public void destroy() {
    }



////////////////////////////////////////////////////////////////////////////
    public void paint(Graphics gOld) {
        if (image == null || Window.xsize != getSize().width || Window.ysize != getSize().height) {
            Window.xsize = getSize().width;
            Window.ysize = getSize().height;
            image = createImage(Window.xsize, Window.ysize);
            g = (Graphics2D) image.getGraphics();
            Drawing.setDrawingInfo(g,this);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
        }
//fill background
        g.setColor(Color.cyan);
        g.fillRect(0, 0, Window.xsize, Window.ysize);

        int x[] = {Window.getX(0), Window.getX(Window.getWidth2()), Window.getX(Window.getWidth2()), Window.getX(0), Window.getX(0)};
        int y[] = {Window.getY(0), Window.getY(0), Window.getY(Window.getHeight2()), Window.getY(Window.getHeight2()), Window.getY(0)};
//fill border
        g.setColor(Color.black);
        g.fillPolygon(x, y, 4);
// draw border
        g.setColor(Color.red);
        g.drawPolyline(x, y, 5);

        if (animateFirstTime) {
            gOld.drawImage(image, 0, 0, null);
            return;
        }

        g.drawImage(outerSpaceImage,Window.getX(0),Window.getY(0),
                Window.getWidth2(),Window.getHeight2(),this);
            
        Star.Draw();
        Missle.Draw();
        rocket.Draw();
        PulseStar.Draw();
                if (gameOver)
        {
            g.setColor(Color.white);
            g.setFont(new Font("Andy",Font.PLAIN,50));
            g.drawString("GAME OVER",60,250);
        }
         g.setColor(Color.black);
        g.setFont(new Font("Andy",Font.PLAIN,27));
        g.drawString("score = " + score,20,48);
        
        gOld.drawImage(image, 0, 0, null);
    }

////////////////////////////////////////////////////////////////////////////
// needed for     implement runnable
    public void run() {
        while (true) {
            animate();
            repaint();
            double seconds = 1/frameRate;    //time that 1 frame takes.
            int miliseconds = (int) (1000.0 * seconds);
            try {
                Thread.sleep(miliseconds);
            } catch (InterruptedException e) {
            }
        }
    }
/////////////////////////////////////////////////////////////////////////
    public void reset() {
          gameOver = false;
          rocket = new Rocket();
          timeCount = 0;
          Star.Reset();
          Missle.Reset();
          PulseStar.Reset();
          }
/////////////////////////////////////////////////////////////////////////
    public void animate() {
        if (animateFirstTime) {
            animateFirstTime = false;
            if (Window.xsize != getSize().width || Window.ysize != getSize().height) {
                Window.xsize = getSize().width;
                Window.ysize = getSize().height;
            }
            outerSpaceImage = Toolkit.getDefaultToolkit().getImage("./outerSpace.jpg");
            Rocket.image = Toolkit.getDefaultToolkit().getImage("./rocket.GIF");  
            Star.image = Toolkit.getDefaultToolkit().getImage("./starAnim.GIF"); 
              
            reset();
        }
        if (gameOver)
            return;
        
        if (Star.CollideRocket(rocket))
            gameOver = true;
        
         if (PulseStar.CollideRocket(rocket))
            gameOver = true;
        Missle.CollideStars();
        
        if (timeCount % (int)(2*frameRate) ==((int)(2*frameRate) -1)){
            Star.Create(rocket.getXSpeed());
        }
        if (timeCount % (int)(3*frameRate) ==((int)(3*frameRate) -1)){
            PulseStar.Create();
        }
        rocket.Animate();
        Star.Animate(rocket.getXSpeed());
        Missle.Animate();
        timeCount++;
        PulseStar.Animate();
        Missle.CollideInvaders();
        
        
    }

////////////////////////////////////////////////////////////////////////////
    public void start() {
        if (relaxer == null) {
            relaxer = new Thread(this);
            relaxer.start();
        }
    }
////////////////////////////////////////////////////////////////////////////
    public void stop() {
        if (relaxer.isAlive()) {
            relaxer.stop();
        }
        relaxer = null;
    }

}

class Rocket {
public static Image image;

 
//static = class var
//private cant be access outside of class (public by default)
//variables for rocket. 
private static int maxSpeed = 10;
   private int xPos;
   private  int yPos;
   private  int ySpeed;
   private  int xSpeed;
   private boolean faceRight;
   
   public Rocket(){
       xPos = Window.getWidth2()/2;
       yPos = Window.getHeight2()/2;
       ySpeed = 0;
       faceRight = true;
       Rocket.image = Toolkit.getDefaultToolkit().getImage("./rocket.GIF"); 
   }
   public void IncreaseYSpeed(int speedInc){
       ySpeed += speedInc;
       if (ySpeed > 0){
            Rocket.image = Toolkit.getDefaultToolkit().getImage("./animRocket.GIF");
        }
        else if (ySpeed < 0){
            Rocket.image = Toolkit.getDefaultToolkit().getImage("./animRocket.GIF");
        }
        else {
            Rocket.image = Toolkit.getDefaultToolkit().getImage("./rocket.GIF");  
        }
   }
   public void IncreaseXSpeed(int speedInc){
       xSpeed += speedInc;
       
        if (xSpeed > 0){
            Rocket.image = Toolkit.getDefaultToolkit().getImage("./animRocket.GIF");
            faceRight = true;
        }
        else if (xSpeed < 0){
            Rocket.image = Toolkit.getDefaultToolkit().getImage("./animRocket.GIF");
            faceRight = false;
        }
        else {
                Rocket.image = Toolkit.getDefaultToolkit().getImage("./rocket.GIF");  
        }
        if (xSpeed > maxSpeed){
             xSpeed = maxSpeed;
         }
         else if (xSpeed < -maxSpeed){
              xSpeed = -maxSpeed;
         }
   }
   public int getXPos(){
       return (xPos);
   }
   public int getXSpeed(){
       return (xSpeed);
   }
    public int getYPos(){
       return (yPos);
   }
    public void Animate(){
        yPos += ySpeed;
        if (yPos < 0 ){
            ySpeed = 0;
            yPos = 0;
        }
        else if (yPos > Window.getHeight2()){
            ySpeed = 0;
            yPos = Window.getHeight2();
        }
    }
    
    public void Draw() {
         if (faceRight)
         Drawing.drawImage(Rocket.image,Window.getX(getXPos()),Window.getYNormal(getYPos()),0.0,1.0,1.0 );
         else 
              Drawing.drawImage(Rocket.image,Window.getX(getXPos()),Window.getYNormal(getYPos()),0.0,-1.0,1.0 );
    }
    
}
class Star {
    public static Image image; 
    public static ArrayList<Star> stars = new ArrayList<Star>();
    
    private int xPos;
    private int yPos;
    private double xScale;
    private double yScale;
    
     public Star(){
       xPos = Window.getWidth2()/2;
       yPos = Window.getHeight2()/2;
       xScale = 0.6;
       yScale = 0.6;
     }
     public static void Draw(){
         for (Star star : stars)
              Drawing.drawImage(Star.image,Window.getX(star.xPos),Window.getYNormal(star.yPos),0.0,star.xScale,star.yScale);
     }
     public static void Reset(){
         stars.clear();
     }
 
     public static void Animate(int xSpeed){
          for (int i=0;i<stars.size();i++) {
            stars.get(i).xPos -= xSpeed;
            if (stars.get(i).xPos < 0) {
                stars.remove(i);
                i--;
            }
            else if (stars.get(i).xPos > Window.getWidth2()) {
                stars.remove(i);
                i--;
            }
        }
     }
     public static void Create(int xSpeed){
         if (xSpeed > 0){
        Star star = new Star();
        star.xPos = Window.getWidth2();
        star.yPos = (int)(Math.random()*Window.getHeight2());
        star.xScale =  (int)(Math.random()*3)+0.6;
        star.yScale =  star.xScale;
        stars.add(star);
         }
         else if (xSpeed < 0){
        Star star = new Star();
        star.xPos = 0;
        star.yPos = (int)(Math.random()*Window.getHeight2());
        star.xScale =  (int)(Math.random()*3)+0.6;
        star.yScale =  star.xScale;
        stars.add(star);
         }
     }
      public static boolean CollideRocket(Rocket rocket){
          for (Star star : stars) {
         if (star.xPos+20  > rocket.getXPos()&&
             star.xPos-20 < rocket.getXPos() &&
             star.yPos+20 >rocket.getYPos()  &&
              star.yPos-20 < rocket.getYPos() )
             return(true);
          }
       return (false);

      }
     public static Star CollideMissle(Missle missle){
          for (Star star : stars){
          if (star.xPos+20  > missle.getXPos()&&
             star.xPos-20 < missle.getXPos() &&
             star.yPos+20 > missle.getYPos()  &&
              star.yPos-20 < missle.getYPos() )         
              return (star);
         }
         return null;   
    }
     public static void RemoveStar(Star star){
         stars.remove(star);
     }
 }
class Missle{
    public static ArrayList<Missle> missles = new ArrayList<Missle>();
    
    private int xPos;
    private int yPos;
    private  int xSpeed;
    
     public Missle(int _xSpeed, int _xPos, int _yPos){
         xSpeed = _xSpeed;
         xPos = _xPos;
         yPos = _yPos;
     }
      public int getXPos(){
       return (xPos);
   }
   public int getYPos(){
       return (yPos);
   }
    public int getXSpeed(){
       return (xSpeed);
   }
      public static void Draw(){
        for (Missle missle : missles)
              Drawing.drawCircle(Window.getX(missle.xPos),Window.getYNormal(missle.yPos),0.0, 2.0,0.5,Color.yellow);
     }
    public static void Animate(){
          for (int i=0;i<missles.size();i++) {
            missles.get(i).xPos += missles.get(i).xSpeed;
            if (missles.get(i).xPos < 0) {
                missles.remove(i);
                i--;
            }
            else if (missles.get(i).xPos > Window.getWidth2()) {
                missles.remove(i);
                i--;
            }
        }
     }
    public static void Reset(){
         missles.clear();
     }
     public static void Create(Rocket rocket){
         if (rocket.getXSpeed() > 0){
        Missle missle = new Missle(5,Window.getWidth2()/2,rocket.getYPos());
        missles.add(missle);
         }
         else if (rocket.getXSpeed() < 0){
       Missle missle = new Missle(-5,Window.getWidth2()/2,rocket.getYPos());
        missles.add(missle);
         }
         else {
             Missle missle = new Missle(5,Window.getWidth2()/2,rocket.getYPos());
               missles.add(missle);
         }
     }
        public static void CollideStars(){  
           Star star = null;
            for (int i=0;i<missles.size();i++){
                if((star = Star.CollideMissle(missles.get(i)))!= null){
                missles.remove(i);
                i--;
                }
                if(star != null)
                    Star.RemoveStar(star);
            } 
        }
         public static void CollideInvaders(){  
           PulseStar pulsestar = null;
            for (int i=0;i<missles.size();i++){
                if((pulsestar = PulseStar.CollideMissle(missles.get(i)))!= null){
                missles.remove(i);
                i--;
                }
                if(pulsestar != null)
                    PulseStar.RemovePulseStar(pulsestar);
            } 
        }
}
class PulseStar {
    public static Image image; 
    public static ArrayList<PulseStar> pulsestars = new ArrayList<PulseStar>();
    
    private int xPos;
    private int yPos;
    private int rotation;
    private int rotSpeed;
    private int life;
         
     public PulseStar(){
       xPos = (int)(Math.random()*Window.getWidth2()/2);
       yPos = (int)(Math.random()*Window.getHeight2()/2);
       rotation = 0;
       rotSpeed = (int)(Math.random()*20-10);
       life = (int)(Math.random()*200+50);
     }
     public static void Draw(){
         for (PulseStar pulsestar : pulsestars)
              Drawing.drawSquare(Window.getX(pulsestar.xPos),Window.getYNormal(pulsestar.yPos),pulsestar.rotation,2.0,2.,Color.white);
     }
     public static void Reset(){
         pulsestars.clear();
     }
 
     public static void Animate(){
          for (int i=0;i<pulsestars.size();i++) {
            pulsestars.get(i).rotation += pulsestars.get(i).rotSpeed;
            pulsestars.get(i).life--;
            if (pulsestars.get(i).life <= 0){
                pulsestars.remove(i);
                i--;
                    }
             }
     }
     public static void Create(){
        PulseStar pulsestar = new PulseStar();
        pulsestars.add(pulsestar);         
     }
      public static boolean CollideRocket(Rocket rocket){
            for (PulseStar pulsestar : pulsestars) {
         if (pulsestar.xPos+20  > rocket.getXPos()&&
             pulsestar.xPos-20 < rocket.getXPos() &&
             pulsestar.yPos+20 >rocket.getYPos()  &&
              pulsestar.yPos-20 < rocket.getYPos() )
             return(true);
          }
       return (false);

      }
     public static PulseStar CollideMissle(Missle missle){
          for (PulseStar pulsestar : pulsestars){
          if (pulsestar.xPos+20  > missle.getXPos()&&
             pulsestar.xPos-20 < missle.getXPos() &&
             pulsestar.yPos+20 > missle.getYPos()  &&
              pulsestar.yPos-20 < missle.getYPos() )         
              return (pulsestar);
         }
         return null;   
    }
     public static void RemovePulseStar(PulseStar pulsestar){
         pulsestars.remove(pulsestar);
     }
 }
   
 

////////////////////////////////////////////////////////////////////////////

class Window {
    private static final int XBORDER = 20;
    
    private static final int YBORDER = 20;
    
    private static final int TOP_BORDER = 40;
    private static final int BOTTOM_BORDER = 20;
    
    private static final int YTITLE = 30;
    private static final int WINDOW_BORDER = 8;
    static final int WINDOW_WIDTH = 2*(WINDOW_BORDER + XBORDER) + 600;
    static final int WINDOW_HEIGHT = YTITLE + WINDOW_BORDER + 600;
    static int xsize = -1;
    static int ysize = -1;
    
/////////////////////////////////////////////////////////////////////////
    public static int getX(int x) {
        return (x + XBORDER + WINDOW_BORDER);
    }

    public static int getY(int y) {
//        return (y + YBORDER + YTITLE );
        return (y + TOP_BORDER + YTITLE );
        
    }

    public static int getYNormal(int y) {
//          return (-y + YBORDER + YTITLE + getHeight2());
      return (-y + TOP_BORDER + YTITLE + getHeight2());
        
    }
    
    public static int getWidth2() {
        return (xsize - 2 * (XBORDER + WINDOW_BORDER));
    }

    public static int getHeight2() {
//        return (ysize - 2 * YBORDER - WINDOW_BORDER - YTITLE);
        return (ysize - (BOTTOM_BORDER + TOP_BORDER) - WINDOW_BORDER - YTITLE);
    }    
}

class Drawing {
    private static Graphics2D g;
    private static RocketShip mainClassInst;

    public static void setDrawingInfo(Graphics2D _g,RocketShip _mainClassInst) {
        g = _g;
        mainClassInst = _mainClassInst;
    }
////////////////////////////////////////////////////////////////////////////
    public static void drawCircle(int xpos,int ypos,double rot,double xscale,double yscale,Color color)
    {
        g.translate(xpos,ypos);
        g.rotate(rot  * Math.PI/180.0);
        g.scale( xscale , yscale );

        g.setColor(color);
        g.fillOval(-10,-10,20,20);

        g.scale( 1.0/xscale,1.0/yscale );
        g.rotate(-rot  * Math.PI/180.0);
        g.translate(-xpos,-ypos);
    }
////////////////////////////////////////////////////////////////////////////
    public static void drawImage(Image image,int xpos,int ypos,double rot,double xscale, double yscale) {
        int width = image.getWidth(mainClassInst);
        int height = image.getHeight(mainClassInst);
        g.translate(xpos,ypos);
        g.rotate(rot  * Math.PI/180.0);
        g.scale( xscale , yscale );

        g.drawImage(image,-width/2,-height/2,
        width,height,mainClassInst);

        g.scale( 1.0/xscale,1.0/yscale );
        g.rotate(-rot  * Math.PI/180.0);
        g.translate(-xpos,-ypos);
    }    

////////////////////////////////////////////////////////////////////////////
public static void drawSquare(int xpos,int ypos,double rot,double xscale,double yscale,Color color)
    {
        g.translate(xpos,ypos);
        g.rotate(rot  * Math.PI/180.0);
        g.scale( xscale , yscale );

        g.setColor(color);
        g.fillRect(-10,-10,20,20);

        g.scale( 1.0/xscale,1.0/yscale );
        g.rotate(-rot  * Math.PI/180.0);
        g.translate(-xpos,-ypos);
    }
}
////////////////////////////////////////////////////////////////////////////



class sound implements Runnable {
    Thread myThread;
    File soundFile;
    public boolean donePlaying = false;
    sound(String _name)
    {
        soundFile = new File(_name);
        myThread = new Thread(this);
        myThread.start();
    }
    public void run()
    {
        try {
        AudioInputStream ais = AudioSystem.getAudioInputStream(soundFile);
        AudioFormat format = ais.getFormat();
    //    System.out.println("Format: " + format);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        SourceDataLine source = (SourceDataLine) AudioSystem.getLine(info);
        source.open(format);
        source.start();
        int read = 0;
        byte[] audioData = new byte[16384];
        while (read > -1){
            read = ais.read(audioData,0,audioData.length);
            if (read >= 0) {
                source.write(audioData,0,read);
            }
        }
        donePlaying = true;

        source.drain();
        source.close();
        }
        catch (Exception exc) {
            System.out.println("error: " + exc.getMessage());
            exc.printStackTrace();
        }
    }

}
