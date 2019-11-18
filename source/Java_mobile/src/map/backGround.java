package map;

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
Hintergrund
Hintergrundobjekt ist bei jeder Karte vorhanden und wird nach Tileset-Definitionen gespeichert. Kann Metadaten enthalten. Füllfarbe kann auch als Maskierfarbe interpretiert werden, wenn ein Bild maskiert dargestellt wird.

Offset Byte Beschreibung
0 1 Konstante 0
1 1 Sichtbar (0=nein, 1=ja)
2 12 Grafikdatei
14 1 Parallax X
15 1 Parallax Y
16 4 Position X
20 4 Position Y
24 4 Kartenbreite
28 4 Kartenhöhe
32 1 Füllmodus (0=Ohne, 1=Farbe, 2=Bild, 3=Bild maskiert)
33 1 Füllfarbe R
34 1 Füllfarbe G
35 1 Füllfarbe B

 Gesamtlänge 36 Byte
*/

public class backGround {
  private String BgDatei ="";
  private boolean visible=true,masked=false;
  private byte fuellModus=0,ColorR=0,ColorG=0,ColorB=0;
  private int parallaxX=0,parallaxY=0,positionX=0,positionY=0,mapWidth=0,mapHeight=0;

  /**
   * backGround
   *
   * @param _visible boolean
   * @param _BgDatei String
   * @param _parallaxX int
   * @param _parallaxY int
   * @param _positionX int
   * @param _positionY int
   * @param _mapWidth int
   * @param _mapHeight int
   * @param _fuellModus byte
   * @param _ColorR byte
   * @param _ColorG byte
   * @param _ColorB byte
   */
  public backGround(boolean _visible,String _BgDatei,int _parallaxX,int _parallaxY,int _positionX,int _positionY,int _mapWidth,int _mapHeight,byte _fuellModus,byte _ColorR,byte _ColorG,byte _ColorB) {
  this.visible=_visible;
  this.BgDatei=_BgDatei;
  this.parallaxX=_parallaxX;
  this.parallaxY=_parallaxY;
  this.positionX=_positionX;
  this.positionY=_positionY;
  this.mapWidth=_mapWidth;
  this.mapWidth=_mapWidth;
  this.fuellModus=_fuellModus;
  this.ColorR=_ColorR;
  this.ColorG=_ColorG;
  this.ColorB=_ColorB;
  }


  public final byte[] getColors() {
    return new byte[]{this.ColorR,this.ColorG,this.ColorB};
  }

  public final boolean isMasked() {
    return masked;
  }

  public final byte getFuellModus() {
    return fuellModus;
  }

  public final boolean isVisible() {
    return visible;
  }

  public final String getBgDatei() {
    return BgDatei;
  }

  public final int[] getParallax() {
   return new int[] {
       this.parallaxX, this.parallaxY};
 }

 public final int[] getPosition() {
   return new int[] {
       this.positionX, this.positionY};
 }

 public final int[] getBgSize() {
   return new int[] {
       this.mapWidth, this.mapHeight};
 }



}
