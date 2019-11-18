package win;

import java.io.File;
import map.*;

import java.awt.*;
import java.awt.image.VolatileImage;
import java.awt.image.FilteredImageSource;

/**
 * <p>Title: map renderer</p>
 * <p>Description: map renderer for Mapeditor</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: dennisr</p>
 * @author denman
 * @version 1.0
 */

public class main {
  public static renderFrame rf;
  private static String path="samples/RPG/Data/";

  public static backGround bg = null;
  public static anime [] anim = null;
  public static layer [] rect=null;
  public static basisDaten [] ldata=null;
  public static tileSet [] tset=null;

  public static boolean Paused=false;
  private static Toolkit tlk = Toolkit.getDefaultToolkit();



  public main() {
   System.setProperty("sun.java2d.d3d","false");
   System.setProperty("sun.java2d.translaccel","true");
   System.setProperty("sun.java2d.ddscale","true");
   System.setProperty("sun.java2d.ddforcevram", "true");
   System.setProperty("sun.java2d.ddoffscreen","true");

   rf = new renderFrame();
   initAll();
   rf.gfx.createBuffers();
   initTilesets();
   rf.gfx.start();

  }

  public static final void initAll()
  {
    File file = new File("");
    path = file.getAbsolutePath()+"/samples/RPG/Data/";
    file=new File(path+"Jan1.map");
    System.err.println("Path :"+path+"Jan1.map" );
    if(file==null)
    {System.err.println("map file null"); System.exit(1);}
    else System.err.println("map file found");

    convertMapToRes cmr = new map.convertMapToRes(path+"Jan1.map");
    cmr.openFile();
    cmr.readData();
  }

  public  final void initTilesets()
  {
    for(int a=0;a<tset.length;a++)
    {
    tset[a].setTiles( createTileArray(tset[a].getTileFile(),tset[a].getTileSize()[0],tset[a].getTileSize()[1],tset[a].getFrameCount(),a) ) ;
    }
  }

  public final VolatileImage[] createTileArray(String Filename,int _frameWidth,int _frameHeight,int _frameCount,int aktnum)
  {
    int frameWidth= _frameWidth;
    int frameHeight= _frameHeight;
    int frameCount= _frameCount;
    int num = aktnum;

    System.err.println("Image name:"+path+Filename);
    Image img=tlk.getImage("samples/RPG/Data/"+Filename);

    while(!tlk.prepareImage(img,-1,-1,rf))
    {  }

   Color trans=new Color(tset[num].getColors()[0]&255,tset[0].getColors()[1]&255,tset[0].getColors()[2]&255);

    //while(img.getWidth(rf)==-1)
    //  { /* wait for tileimage */ }


    Image des=rf.createImage(new FilteredImageSource(img.getSource(),new TransparentFilter(trans.getRGB())));

    System.err.println("Image Width/Height:"+img.getWidth(rf)+"/"+img.getHeight(rf));
    System.err.println("Image frame Width/Height:"+frameWidth+"/"+frameHeight);

    int y_tiles = (int)img.getHeight(rf)/frameHeight;
    int x_tiles = (int)img.getWidth(rf)/frameWidth;

    VolatileImage tiles[] = new VolatileImage[frameCount];
    System.err.println("Image tile count:"+frameCount);

      for (int a = y_tiles - 1; a >= 0; a--)
        for (int i = x_tiles - 1; i >= 0; i--) {
          int mod = i + (a * x_tiles); // akt. + bereits gez. reihen

          tiles[mod] = rf.getGraphicsConfiguration().createCompatibleVolatileImage( frameWidth,frameHeight);
          Graphics2D gr = null;

          gr = tiles[mod].createGraphics();
          gr.setClip(0, 0,frameWidth, frameHeight);
          gr.drawImage(des, 0- (frameWidth * i), 0- (frameHeight * a), rf);
          //gr.setColor(Color.yellow);
          //gr.drawString(""+(char)(mod+1),0,0);

          gr.dispose();
        }
    return tiles;
  }



  public static void main(String[] args) {
    main main1 = new main();
  }

}
