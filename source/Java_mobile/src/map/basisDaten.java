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
Basisdaten
Diese Daten enthalten die Hauptinformationen zu einem Layer. Nach diesen Werten wird ein Layer dann gezeichnet. Kann nur hinter Layer-Objekten stehen (ausser Klonlayer). Datenaufbau siehe hier.

Offset Byte Beschreibung
0 1 Konstante 102
1 1 Datentiefe (4, 8, 12 oder 16)
2 4 Datenlänge in Byte
6 2 *reserviert*
8 x Daten...
...

 Gesamtlänge 8+x Byte
*/

public class basisDaten {
  private byte[] data=null;
  private byte datenTiefe=0;
  private int datenLaenge =0;

  /**
   * layerData rect
   *
   * @param _data byte[]
   * @param _laenge int
   * @param _datenTiefe byte
   */
  public basisDaten(byte[] _data,int _laenge,byte _datenTiefe)
  {
    this.datenLaenge=_laenge;
    this.datenTiefe=_datenTiefe;
    this.data = new byte[datenLaenge];
    this.data=_data;
  }


  public final int getLaenge() {
    return datenLaenge;
  }

  public final byte[] getData() {
    return data;
  }

  public final byte getDatenTiefe() {
    return datenTiefe;
  }



}
