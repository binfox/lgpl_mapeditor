package map;

import java.io.*;
import java.io.FileInputStream;
import java.util.zip.Adler32;
import win.*;

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
public class convertMapToRes {

 private FileInputStream is = null;
 public int bx=0; int by =0;
 private File file=null;
 private int layerCnt=0;
 private int animeCnt=0;
 private int tileCnt=0;
 private String filename="";


 /* vars from mapmaker */
  long checksum;
  int count_anim;
  int count_base;
  int count_data;
  int count_geo;
  int count_image;
  int count_layer;
  int count_meta;
  int count_tile;
  int count_total;
  byte crypted =0;
  long fsize;
  int maxsize;
  byte proversion=0;
  int subversion=0;
  int version=0;


  public convertMapToRes(String fname) {
    filename=fname;
  }

  public int readInt(byte[] buffer, int offset) {
    int n = 0;
    for (int i = 3; i >=0 ; i--) {
      n += ( (0xFF & buffer[offset + i]) << 8 * (0 + i));
    }
    return n;
  }

  public int readShort(byte[] buffer, int offset) {
    short n = 0;
    for (int i = 1; i>=0; i--) {
      n += ( (0xFF & buffer[offset + i]) << 8 * (0 + i));
    }
    return n;
  }




  public void openFile()
  {
    try {

    file=new File(filename);//KeyMakerDBF/"../KeyMakerNokia128x128/key.map"
    //System.err.println("open file :"+file.getAbsolutePath());
    //System.err.println("    file :"+file.getParent());
  }
  catch (Exception ex) { System.err.println("File not found"); }
  }

  /*
   orginal code from mapmaker :

   ;---------------------------------------------------------------------
   ;name:   map file name
   ;passw:  map password
   ;check:  checksum   (0=no, 1=yes)
   ;RETURN: error code (0=ok, 1=file not found, 2=file size corrupted, 3=no read access, 4=not valid file, 5=checksum problem, 6=password problem, 7=load image problem)
   ;        error code 7 do not free data or images!!!
   ;---------------------------------------------------------------------

   werte bereiche in bb:

   Byte       1 Ganzzahl 0 +255
   Short      2 Ganzzahl 0 +65535
   Integer    4 Ganzzahl -2147483648   +2147483647
   Float      4 Kommazahl   -2 Mrd. +2 Mrd.
   Line       anz+2  String - -
   String     anz+4  String -

   in java:

   For byte, from -128 to 127, inclusive
   For short, from -32768 to 32767, inclusive
   For int, from -2147483648 to 2147483647, inclusive
   For long, from -9223372036854775808 to 9223372036854775807, inclusive
   For char, from '\u0000' to '\uffff' inclusive, that is, from 0 to 65535

   Const layer_back  =00
   Const layer_map   =01
   Const layer_iso1  =02
   Const layer_iso2  =03
   Const layer_hex1  =04
   Const layer_hex2  =05
   Const layer_clone =06
   Const layer_image =07
   Const layer_block =08
   Const layer_point =09
   Const layer_line  =10
   Const layer_rect  =11
   Const layer_oval  =12


   // to a Byte Arrayint originalInt =...;
   ByteBuffer myBuffer = ByteBuffer.allocate(4);
   myBuffer.putInt( originalInt );
   byte myArray[] = myBuffer.array();


   // and back to an int
   ByteBuffer myOtherBuffer = ByteBuffer.wrap(myArray);
   int finalInt = myOtherBuffer.getInt();

   */


  public void readData()
  {
    ByteArrayInputStream bin = null;
    DataInputStream din = null;


    fsize = (int)file.length();
    System.err.println("filesize :"+fsize);
    byte [] all =null;

    try {
      is = new FileInputStream(file);
      all=new byte[is.available()];
      is.read(all);
      bin = new ByteArrayInputStream(all);
      din = new DataInputStream(bin);

      is.close();
      din.reset();

    }
    catch (Exception ex) { ex.printStackTrace();
    }


    try
    {

    //;HEADER-------------------------------------------------------------

    if( din.readByte()!=85 || din.readByte()!=77 || din.readByte()!=70 ) //3. byte
    System.err.println("falscher header !!!");
    else System.err.println("header ok !!!");

    version=din.readByte(); //4. byte
    System.err.println("version :"+version);

    Adler32 crcx = new Adler32();
    crcx.update( all,8,all.length-8 );
    System.err.println("CRC32-Checksumme :"+crcx.getValue());


    byte [] byteMod=new byte[4];
    System.err.println("anzahl:"+din.read(byteMod));

    checksum =readInt(byteMod,0);
    System.err.println("CRC32-Checksumme datei:"+checksum);

    if( (version & 128)==1 ) crypted=1;
    System.err.println("cryptet :"+crypted);

    if( (version & 64)==1 ) proversion=1;
    System.err.println("proversion :"+proversion);

    subversion=version&63;
    System.err.println("subversion :"+subversion);

    //skip 20
    //System.err.println("SHA1-Fingerprint (nur PROversion) skip bytes:"+din.skipBytes(20));
    din.skipBytes(20);

    byteMod=new byte[2];  // alles short (2 byte) werte

    din.read(byteMod);
    count_total= readShort(byteMod,0);
    System.err.println("Anzahl Blöcke ( +1 bg ):"+count_total);

    din.read(byteMod);
    count_layer= readShort(byteMod,0);
    System.err.println("Anzahl Layerobjekte (:2 == layer koll):"+count_layer);
    main.rect = new layer[count_layer];

    din.read(byteMod);
    count_image=readShort(byteMod,0);
    System.err.println("Anzahl Bildobjekte:"+count_image);

    din.read(byteMod);
    count_geo  =readShort(byteMod,0);
    System.err.println("Anzahl geom. Objekte:"+count_geo);

    din.read(byteMod);
    count_tile =readShort(byteMod,0);
    System.err.println("Anzahl Tilesets:"+count_tile);
    main.tset = new tileSet[count_tile];

    din.read(byteMod);
    count_anim =readShort(byteMod,0);
    System.err.println("Anzahl Animationen:"+count_anim);
    main.anim=new anime[count_anim];

    din.read(byteMod);
    count_base =readShort(byteMod,0);
    System.err.println("Anzahl Basisdaten:"+count_base);
    main.ldata = new basisDaten[count_base];

    din.read(byteMod);
    count_data =readShort(byteMod,0);
    System.err.println("Anzahl Datenlayer:"+count_data);

    din.read(byteMod);
    count_meta =readShort(byteMod,0);
    System.err.println("Anzahl Metadaten:"+count_meta);

   // skip 18 bytes reserviert
   din.skip(18);

   // header end

   // table anfang
   maxsize=count_total*4+64;
   if(maxsize>fsize) { System.err.println("filesize falsch"); System.exit(1);}
   System.err.println("maxsize:"+maxsize);

  int map_block [] = new int [count_total];
  int map_offset [] = new int [count_total];

  byteMod=new byte[4];

  for(int i=0;i<count_total;i++)
  {
    din.read(byteMod);
    map_block[i]=readInt(byteMod,0);
    //System.err.println("map_block[i] :"+map_block[i]+" offset:"+maxsize);
    map_offset[i]=maxsize;
    maxsize+=map_block[i];
    //System.err.println("for count_total i :"+i);
  }
  //System.err.println("maxsize mod 4 :"+(maxsize % 4));
  if ((maxsize % 4)>0 ) maxsize=(maxsize+4)-(maxsize % 4);
  if (maxsize != fsize) { System.err.println("fsize falsch ! :"+fsize+" maxsize :"+maxsize); System.exit(1); }
  else System.err.println("fsize richtig !");


  /* blöcke abfragen ...*/
  for(int i=0;i<count_total;i++)
    {
      din.reset();
      din.skipBytes(map_offset[i]);
        byte code = (byte)din.readUnsignedByte();
        System.err.println("********  aktueller code:"+code+"\n");

        switch (code)
        {

          /*
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

           Gesamtlänge 26 Byte */

          case 100: // tileset

            byte ColorR = (byte) din.readUnsignedByte();
            byte ColorG = (byte) din.readUnsignedByte();
            byte ColorB = (byte) din.readUnsignedByte();

            byteMod = new byte[2];

            din.read(byteMod);
            short frameWidth = (short) readShort(byteMod, 0);
            din.read(byteMod);
            short frameHeight = (short) readShort(byteMod, 0);
            din.read(byteMod);
            short yCor = (short) readShort(byteMod, 0);
            din.read(byteMod);
            short animCount = (short) readShort(byteMod, 0);
            din.read(byteMod);
            short frameCount = (short) readShort(byteMod, 0);

            String tileFile = "";

            for (int a = 0; a < 12; a++) {
              char tmp = (char) din.readByte();
              if (tmp > 31 && tmp < 123)
                tileFile += tmp;
            }
            System.err.println("Tile filename:" + tileFile);
            System.err.println("Tile breite/hoehe :" + frameWidth + "/" + frameHeight+" anzahlFrames: "+frameCount+" +anzahlAnime: "+(frameCount+animCount));
            System.err.println("Tile farben rgb :" + (ColorR&255) + "/" + (ColorG&255) + "/" + (ColorB&255) );

//tileSet(byte _ColorR,byte _ColorG,byte _ColorB,int _frameWidth,int _frameHeight,int _yCorr,int _frameCount,int _animCount,String _tileFile)
            main.tset[tileCnt]=new tileSet(ColorR,ColorG,ColorB,frameWidth,frameHeight,yCor,frameCount,animCount,tileFile);
            tileCnt++;

            break;

            case 101:
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
             byteMod=new byte[2];

             din.read(byteMod);
             frameCount = (short)(readShort(byteMod,0));
             din.read(byteMod);
             short startFrame = (short)(readShort(byteMod,0));

             byte modus= (byte)din.readByte();
             byte standardData= (byte)din.readByte();

             byteMod=new byte[4];

             din.read(byteMod);
             int millisImProg = readInt(byteMod,0);

             byteMod=new byte[2];
             din.read(byteMod);
             short aktFrameNrImProg = (short)(readShort(byteMod,0));

             short data [] = new short[frameCount<<1] ;// mal 2 wegen dem frame und der zeit ,immer hintereinander
             for(int a=0;a<(frameCount<<1);a+=2)
             {
                din.read(byteMod); //frame einlesen
                data[a] = (short)(readShort(byteMod,0));

                din.read(byteMod); //frame (anime) zeit einlesen
                data[a+1] = (short)(readShort(byteMod,0));

                System.err.println("## Frame nr/wert/time :"+a+" / "+data[a]+" / "+data[a+1]);
             }

             System.err.println("\n***** Animation frames:"+frameCount+" startframe:"+startFrame+" modus:"+modus);

              //anime(int _frameCount,int _startFrame,byte _modus,byte _standardData,short []_data)
              main.anim[animeCnt]=new anime(frameCount,startFrame,modus,standardData,data);
              animeCnt++;
            break;

            /*
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

            case 0: //background
            boolean visible=false;
            byte sichtbar= (byte)din.readByte();
            if(sichtbar==1)
              visible=true;

            String bgFile = "";

            for (int a = 0; a < 12; a++) {
              char tmp = (char) din.read();
              if (tmp > 31 && tmp < 123)
                bgFile += tmp;
            }

            byte ParallaxX =(byte)din.readUnsignedByte();
            byte ParallaxY =(byte)din.readUnsignedByte();
            System.err.println("Paralax X/Y :"+ParallaxX+"/"+ParallaxY);

            byteMod=new byte[4];

            din.read(byteMod);
            int PosX = readInt(byteMod,0);
            din.read(byteMod);
            int PosY = readInt(byteMod,0);

            din.read(byteMod);
            int mapWidth = readInt(byteMod,0);
            din.read(byteMod);
            int mapHeight = readInt(byteMod,0);

            // Füllmodus (0=Ohne, 1=Farbe, 2=Bild, 3=Bild maskiert)
            byte fuellModus = (byte) din.readUnsignedByte();

            // füllfarbe
            ColorR = (byte) din.readUnsignedByte();
            ColorG = (byte) din.readUnsignedByte();
            ColorB = (byte) din.readUnsignedByte();


            System.err.println("bg filename:" + bgFile);
            System.err.println("bg breite/hoehe :" + mapWidth + "/" + mapHeight);
            System.err.println("bg farben rgb :" + (ColorR&255) + "/" + (ColorG&255) + "/" + (ColorB&255) );

//backGround(boolean _visible,String _BgDatei,int _parallaxX,int _parallaxY,int _positionX,int _positionY,int _mapWidth,int _mapHeight,byte _fuellModus,byte _ColorR,byte _ColorG,byte _ColorB) {
            main.bg=new backGround(visible,bgFile,ParallaxX,ParallaxY,PosX,PosY,mapWidth,mapHeight,fuellModus,ColorR,ColorG,ColorB);

            break;

            /*
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

            case 1: // layer rechteck
            visible=false;
            byte vis= (byte)din.readByte();
            if(vis==1)
              visible=true;

            String layerName = "";

            for (int a = 0; a < 12; a++) {
              char tmp = (char) din.read();
              if (tmp > 31 && tmp < 123)
                {
                  layerName+=tmp;
                }
            }

            ParallaxX =(byte)din.readUnsignedByte();
            ParallaxY =(byte)din.readUnsignedByte();

            byteMod=new byte[4];

            din.read(byteMod);
            int posX = readInt(byteMod,0);
            din.read(byteMod);
            int posY = readInt(byteMod,0);

            din.read(byteMod);
            int layerBreite = readInt(byteMod,0);
            din.read(byteMod);
            int layerHoehe = readInt(byteMod,0);

            byteMod=new byte[2];
            din.read(byteMod);
            byte TileSetNum = (byte)(readShort(byteMod,0)-1);

            byte maskiert = (byte) din.readUnsignedByte();
            boolean mask=false;
            if(maskiert==1)
              mask=true;

            System.err.println("Maskiert:"+maskiert);
            System.err.println("Layer Name:" + layerName);
            System.err.println("Tile Set Nummer :" + TileSetNum);
            System.err.println("Layer breite/hoehe :" + layerBreite + "/" + layerHoehe);

            //layer(boolean _visible,String _name,int _parallaxX,int _parallaxY,int _positionX,int _positionY,int _layerWidth,int _layerHeight,byte _tileSetNummer,boolean _masked)
            main.rect[layerCnt] = new layer(visible,layerName,ParallaxX,ParallaxY,posX,posY,layerBreite,layerHoehe,TileSetNum,mask);

            /* layerCnt wird unten bei data erhöht */

            break;

            /*
             Offset Byte Beschreibung
             0 1 Konstante 102
             1 1 Datentiefe (4, 8, 12 oder 16)
             2 4 Datenlänge in Byte
             6 2 *reserviert*
             8 x Daten...
             ...

            Gesamtlänge 8+x Byte
             */

          case 102: // basisdaten nur nach layer !!!
          byte datenTiefe = (byte) din.readUnsignedByte();
          boolean koll=false;

          byteMod=new byte[4];
          din.read(byteMod);
          int datenLaenge = readInt(byteMod,0);

          din.readUnsignedByte();// reserviert 2x
          din.readUnsignedByte();

          byteMod=new byte[datenLaenge];
          din.read(byteMod);

          System.err.println("  Daten Tiefe (bit):" + datenTiefe);
          System.err.println("  Datenlaenge :" + datenLaenge);
          System.err.println("  Kollissions Layer (1 bit data):" + koll);


          //basisDaten(byte[] _data,int _laenge,byte _datenTiefe)
          main.ldata[layerCnt] = new basisDaten(byteMod,datenLaenge,datenTiefe);
          layerCnt++;

          break;

          /*
            Datenlayer
           Datenlayer enthält unsichtbare Informationen zu einem Layer. Jedes Tile kann einen Wert besitzen.
           Kann nur hinter Layer-Objekten stehen (ausser Klonlayer). Datenaufbau siehe hier.

           Offset Byte Beschreibung
           0 1 Konstante 103
           1 1 Datentiefe (4 oder 8)
           2 4 Datenlänge in Byte
           6 2 *reserviert*
           8 x Daten...
           ...
            Gesamtlänge 8+x Byte
           */

           case 103: // datenlayer nur nach layer !!!
             datenTiefe = (byte) din.readUnsignedByte();

             byteMod=new byte[4];
             din.read(byteMod);
             datenLaenge = readInt(byteMod,0);

             din.readUnsignedByte();// reserviert 2x
             din.readUnsignedByte();

             byteMod=new byte[datenLaenge];
             din.read(byteMod);

             System.err.println("datenlayer laenge/bit :"+datenLaenge+" / "+datenTiefe);
           break;

          default:
            System.err.println("********  aktueller code unbekannt:"+code);
          break;

        }

    }


  }
  catch (Exception ex) {  System.err.println("fehler :"+ex.toString()); ex.printStackTrace(); }

  //String fname=file.getName();
  //fname=fname.substring(0,fname.length()-4);

  //System.out.println("ausgabe path :"+file.getParent()+"\\res\\"+fname+".res");
  //byteTool bta = new byteTool(file.getParent()+"/res/"+fname+".res");
  //bta.writeData();
  }



}
