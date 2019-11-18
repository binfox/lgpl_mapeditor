package map;

import java.awt.Image;
import win.*;
import java.awt.image.VolatileImage;

/**
 * <p>Title: map renderer</p>
 *
 * <p>Description: map renderer for Mapeditor</p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: dennisr</p>
 * @author denman
 * @version 1.0
 */


/*
Tileset
Tileset-Definitionen stehen direkt hinter Table. Diese können nur Animation-Definitionen enthalten.

Offset Byte Beschreibung
0 1 Konstante 100
1 1 Maskierung R
2 1 Maskierung G
3 1 Maskierung B
4 2 Framebreite
6 2 Framehöhe
8 2 Y-Korrektur
10 2 Anzahl Animationen
12 2 Anzahl Frames
14 12 Grafikdatei

 Gesamtlänge 26 Byte
*/

public class tileSet {
  private int yCorr=0,animCount=0,frameCount=0,frameWidth=0,frameHeight=0;
  private byte ColorR=0,ColorG=0,ColorB=0;
  private String tileFile ="";
  private static VolatileImage tiles[] = null;


  /**
   * tileSet
   *
   * @param _ColorR byte
   * @param _ColorG byte
   * @param _ColorB byte
   * @param _frameWidth int
   * @param _frameHeight int
   * @param _yCorr int
   * @param _frameCount int
   * @param _animCount int
   * @param _tileFile String
   */
  public tileSet(byte _ColorR,byte _ColorG,byte _ColorB,int _frameWidth,int _frameHeight,int _yCorr,int _frameCount,int _animCount,String _tileFile) {
  this.ColorR=_ColorR;
  this.ColorG=_ColorG;
  this.ColorB=_ColorB;
  this.frameWidth=_frameWidth;
  this.frameHeight=_frameHeight;
  this.yCorr=_yCorr;
  this.frameCount=_frameCount;
  this.animCount=_animCount;
  this.tileFile=_tileFile;

  //createTileArray(String Filename,int _frameWidth,int _frameHeight,int _frameCount)
  ///this.tiles=renderFrame.createTileArray(tileFile,frameWidth,frameHeight,frameCount);
  }

  public void setTiles(VolatileImage[] tiles) {
    this.tiles = tiles;
  }

  public int getFrameCount() {
      return frameCount;
    }

    public String getTileFile() {
      return tileFile;
    }

    public int getAnimeCount() {
      return animCount;
    }

    public int getYCorr() {
      return yCorr;
    }

  public VolatileImage[] getTiles() {
    return tiles;
  }

  public final byte[] getColors() {
       return new byte[]{this.ColorR,this.ColorG,this.ColorB};
     }

     public final int[] getTileSize() {
         return new int[] {
             this.frameWidth, this.frameHeight};
       }


}

