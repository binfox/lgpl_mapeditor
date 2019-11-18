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
Layer (Rechteckig)
Layerobjekt kann Basisdaten, Metadaten oder Datenlayer enthalten.

Offset Byte Beschreibung
0 1 Konstante 1
1 1 Sichtbar (0=nein, 1=ja)
2 12 Name
14 1 Parallax X
15 1 Parallax Y
16 4 Position X
20 4 Position Y
24 4 Layerbreite
28 4 Layerhöhe
32 2 Tilesetnummer
34 1 Maskiert (0=nein, 1=ja)


 Gesamtlänge 35 Byte
*/


public class layer {
  private String layerName ="";
  private byte tileSetNummer=0;
  private boolean visible=true,masked=false;
  private int parallaxX=0,parallaxY=0,positionX=0,positionY=0,layerWidth=0,layerHeight=0;

  /**
   * layer
   *
   * @param _visible boolean
   * @param _name String
   * @param _parallaxX int
   * @param _parallaxY int
   * @param _positionX int
   * @param _positionY int
   * @param _layerWidth int
   * @param _layerHeight int
   * @param _tileSetNummer byte
   * @param _masked boolean
   */
  public layer(boolean _visible,String _name,int _parallaxX,int _parallaxY,int _positionX,int _positionY,int _layerWidth,int _layerHeight,byte _tileSetNummer,boolean _masked)
  {
    this.layerName=_name;
    this.visible=_visible;
    this.parallaxX=_parallaxX;
    this.parallaxY=_parallaxY;
    this.positionX=_positionX;
    this.positionY=_positionY;
    this.layerHeight=_layerHeight;
    this.layerWidth=_layerWidth;
    this.tileSetNummer=_tileSetNummer;
    this.masked=_masked;
  }



  public final String getLayerName() {
    return layerName;
  }

  public final byte getTileSetNummer() {
    return tileSetNummer;
  }

  public final boolean isVisible() {
    return this.visible;
  }

  public final boolean isMask() {
    return this.masked;
  }

  public final int[] getParallax() {
    return new int[] {
        this.parallaxX, this.parallaxY};
  }

  public final int[] getPosition() {
    return new int[] {
        this.positionX, this.positionY};
  }

  public final int[] getLayerSize() {
    return new int[] {
        this.layerWidth, this.layerHeight};
  }




}
