package win;

import java.awt.Canvas;
import java.awt.image.BufferStrategy;
import java.awt.image.VolatileImage;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;

/**
 * <p>Title: map renderer</p>
 * <p>Description: map renderer for Mapeditor</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: dennisr</p>
 * @author denman
 * @version 1.0
 */

public class gameCanvas extends Canvas implements Runnable {
  private static short sleep = 0; //sleeptime für das rendering
  private Thread runner = null;
  private BufferStrategy bs = null;
  private static VolatileImage buffer=null;
  private long time = 0;
  private static int sizeBufferx=800,sizeBuffery=600;

  public gameCanvas(short time) {
  sleep=time;

  }

  /**
   * createBuffers
   * erstellt buffer und bufferimage
   */
  public void createBuffers()
  {
    System.err.println("size buffer image:"+getWidth()+"/"+getHeight());
    buffer = main.rf.getGraphicsConfiguration().createCompatibleVolatileImage(sizeBufferx,sizeBuffery);
    Graphics g = buffer.getGraphics();//255/103/139
    g.setColor(Color.black);
    g.fillRect(0,0,sizeBufferx,sizeBuffery);
    this.createBufferStrategy(2);
    bs=this.getBufferStrategy();

    g.dispose();
    if(bs!=null)
    System.out.println("bufferstrategy created ...");
    else
    { System.out.println("bufferstrategy null..."); System.exit(1); }
  }

  /**
   * draw
   * zeichnet alles aufs bufferimage und dann auf den buffer
   */
  public void draw()
  {
    if(buffer!=null)
    {
      /*alles zeichnen lassen*/
      int aktB = 0; // zeichnen faengt immer oben links an
      int aktH = 0;
      int xKoord = 0;
      int yKoord = 0;
      int maxx=main.rect[0].getLayerSize()[0];
      int maxy=main.rect[0].getLayerSize()[1];
      byte tilesetNum=main.rect[0].getTileSetNummer();
      int paralaxx=main.rect[0].getParallax()[0];
      int paralaxy=main.rect[0].getParallax()[1];
      byte tilesizex=(byte)main.tset[tilesetNum].getTileSize()[0];
      byte tilesizey=(byte)main.tset[tilesetNum].getTileSize()[1];
      byte bitTiefe=main.ldata[0].getDatenTiefe();
      int animecnt = main.tset[0].getAnimeCount();

      //System.err.println("tileset name :"+main.tset[tilesetNum].getTileFile());
      //System.err.println("layer name :"+main.rect[0].getLayerName()+" mask:"+mask);

      sizeBufferx=tilesizex*maxx;
      sizeBuffery=tilesizey*maxy;

      aktB=0;
      aktH=0;

      if(buffer.getWidth()!=sizeBufferx || buffer.getHeight()!=sizeBuffery)
      {
        buffer=main.rf.getGraphicsConfiguration().createCompatibleVolatileImage(sizeBufferx,sizeBuffery);
        System.err.println("buffersize new :"+sizeBufferx+"/"+sizeBuffery);
      }


   Graphics2D g = buffer.createGraphics();
    while (yKoord < maxy)
    {
     xKoord = 0;
     while (xKoord < maxx)
     {

        int aktTileNum=(main.ldata[0].getData()[yKoord*maxx+xKoord]&255)-1;
        if(aktTileNum!=0 && aktTileNum>=animecnt)
        aktTileNum-=animecnt;

        g.setClip(aktB,aktH,tilesizex,tilesizey);
        if(aktTileNum!=0)
        g.drawImage(main.tset[0].getTiles()[aktTileNum],aktB,aktH,this);
        //if(aktTileNum==0)
        //g.fillRect(aktB,aktH,tilesizex,tilesizey);

      xKoord++;
      aktB += tilesizex;

     }

    yKoord++;
    aktB = 0;
    aktH += tilesizey;
    }
    g.dispose();

    bs.getDrawGraphics().drawImage(buffer,paralaxx,paralaxy,this);
    }
    else System.err.println("buffer == null");
  }

  public void paint()
  {
    if(bs!=null)
    {
      bs.show();
    }
  }

  public void paint(Graphics g)
  {
    if(bs!=null && !bs.contentsLost())
    bs.show();
  }

  /**
   * run
   */
  public void run() {
    try {
      while (!main.Paused) {
        time = System.currentTimeMillis();

        draw();
        Graphics2D g2=(Graphics2D)buffer.getGraphics();
        g2.setColor(Color.white);
        g2.setClip(0,0,120,40);
        g2.drawString("Render time:"+(System.currentTimeMillis() - time),20,10);
        g2.dispose();
        paint();

        time = System.currentTimeMillis() - time;
        //System.err.println("runing time for draw :"+time);
        if (time < sleep)
          runner.sleep(sleep - (int)time);
      }

    }
    catch (InterruptedException ie) {
      ie.printStackTrace();
     System.err.println("thread error");
    }
  }

  public void start()
  {
        runner = new Thread(this);
        runner.start();
  }

}
