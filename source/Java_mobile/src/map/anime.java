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
Animation
Animation-Definition kann nur hinter einem Tileset stehen.

Offset Byte Beschreibung
0 1 Konstante 101
1 2 Frameanzahl (ab 1)
3 2 Startframe (ab 1)
5 1 Modus (1=Pause, 2=Vorwärts, 3=Rückwärts)
6 1 Standard Datenwert (ab v1.2)
7 4 *reserviert* (Millisekunden im Programm)
11 2 *reserviert* (Aktuelle Framenr im Programm)
... 2 Animation Frame
... 2 Animation Zeit
...

 Gesamtlänge 13+x Byte
*/

public class anime {
  private int frameCount=0,startFrame=0;
  private byte modus=0,standardData=0;
  private short data [] = null;

  public anime(int _frameCount,int _startFrame,byte _modus,byte _standardData,short []_data) {
    this.frameCount=_frameCount;
    this.startFrame=_startFrame;
    this.modus=_modus;
    this.standardData=_standardData;
    this.data=_data;
  }
}
