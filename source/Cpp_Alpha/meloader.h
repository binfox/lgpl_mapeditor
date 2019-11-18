//////////////////////////////////////////////////////////////////////////////////
//																				//
// Author:			TheShadow													//
// Programm:		MapEditor													//
// Homepage:		http://www.mapeditor.de.vu									//
// eMail:			software@blitzbase.de										//
//																				//
// About this file:																//
// Author:			DragonMaster												//
// Homepage:		http://www.dragon-master.net								//
// eMail:			stephan@dragon-master.net									//
// Description:		Dies ist die Header die eingebunden werden muss, um mit dem	//
//					Loader des MapEditors arbeiten zu können.					//
//					Hier werden alle nötigen Informationen Definiert.			//
//					Dies ist die Implementierung der ProVersion des				//
//					Map File Loaders.											//
//																				//
//////////////////////////////////////////////////////////////////////////////////
#ifndef _MELOADER_H
#define _MELOADER_H


#include <vector>
#include <string>


namespace meloader
{
	// Header enthält Hauptinformationen der Karte. Hier wird z.B. die Anzahl der Objekte
	// gespeichert. Header ist immer unverschlüsselt. CRC32-Checksumme wird erst ab Offset
	// [8] berechnet. SHA1-Fingerprint speichert Passwort-Hashwert.
	struct FileHeader
	{
		char			cUMF[3];			// UMF Kennung (universal map file)
		char			cFlags;				// Bit 0-5 Dateiformat
											// Bit 6 Erstellt mit ProVersion (0:nein, 1:ja)
											// Bit 7 Verschlüsselt (0:nein, 1:ja)
		int				iCheckSum;			// Check Summe
		unsigned char	ucFingerPrint[20];	// SHA1-Fingerprint (nur ProVersion)
		short			sNumBlocks;			// Anzahl Blöcke
		short			sNumLayer;			// Anzahl Layer
		short			sNumPictures;		// Anzahl Bildobjekte
		short			sNumGeoObj;			// Anzahl Geom. Objekte
		short			sNumTiles;			// Anzahl der Tilesets
		short			sNumAnimation;		// Anzahl Animationen
		short			sNumBaseData;		// Anzahl Basisdaten
		short			sNumDataLayer;		// Anzahl Daten Layer
		short			sNumMetaData;		// Anzahl Metadaten
		char			cReserve[18];		// Reserviert (nicht benutzt)
	};


	// Map-Datei besteht aus einzelnen Blöcken. Ein Block kann z.B. Objektdaten, Basisdaten,
	// Metadaten, Datenlayer usw. enthalten. Dabei belegt ein Block bestimmte Anzahl an 
	// Bytes. In Table stehen alle diese Größen untereinander. Wenn sich das Dateiformat
	// in Zukunft etwas ändern sollte (z.B. weil ein ganz neues Objekt eingebaut wurde),
	// wäre das trotzdem kein Problem die Datei auch mit dem alten Code zu laden!
	struct Table
	{
		int			iSize;				// Größe des Blocks
	};

	// Nach einer Table komm als Teil der Object Struktur eine 1Byte große Indentifikations
	// Nummer!


	//  Tileset-Definitionen stehen direkt hinter Table. Diese können nur
	// Animation-Definitionen enthalten.
	struct TileSet
	{
		// Konstant auf 100
		char		cMaskR;				// Farbmaske für Rot
		char		cMaskG;				// Farbmaske für Grün
		char		cMaskB;				// Farbmaske für Blau
		short		sFrameWidth;		// Framebreite
		short		sFrameHeight;		// Framehöhe
		short		sYOffset;			// Y-Korrektur
		short		sNumAnimations;		// Anzahl der Animationen
		short		sNumFrames;			// Anzahl der Frames
		char		szGraphicFile[12];	// Grafikdatei
	};


	//  Animation-Definition kann nur hinter einem Tileset stehen.
	struct Animation
	{
		// Konstant auf 101
		short		sNumFrames;			// Anzahl Frames (min. 1)
		short		sStartFrame;		// Nummer des Frames mit dem die Ani. beginnt
		short		sMode;				// Modus in dem sich die Animation befindet
		char		cReserve[6];		// Reserviert
	};

	// Weitere daten die hinter dieser Struktur liegen
	// Byte: 2	Animation Frame
	// Byte: 2	Animation Time
	struct AnimationData
	{
		short		Frame;				// Der Frame
		short		Time;				// Die Zeit die der Frame angezeigt wird
	};


	// Hintergrundobjekt ist bei jeder Karte vorhanden und wird nach Tileset-Definitionen
	// gespeichert. Kann Metadaten enthalten. Füllfarbe kann auch als Maskierfarbe
	// interpretiert werden, wenn ein Bild maskiert dargestellt wird.
	struct Background
	{
		// Konstante auf 0
		char		cVisible;			// Sichtbarkeit (0:nein, 1:ja)
		char		szGraphicFile[12];	// Grafikdatei
		char		cParallaxX;			// Parallax-Wert X
		char		cParallaxY;			// Parallax-Wert Y
		int			iPositionX;			// Position X
		int			iPositionY;			// Position Y
		int			iCardWidth;			// Kartenbreite
		int			iCardHeight;		// Kartenhöhe
		char		cFillMode;			// Füllmodus
		char		cFillColorR;		// Füllfarbe Rot
		char		cFillColorG;		// Füllfarbe Grün
		char		cFillColorB;		// Füllfarbe Blau
	};


	// Layer Basis Struktur
	struct LayerBase
	{
		char		cVisible;			// Sichtbarkeit (0:nein, 1:ja)
		char		szName[12];			// Name des Layers
		char		cParallaxX;			// Parallax-Wert X
		char		cParallaxY;			// Parallax-Wert Y
		int			iPositionX;			// Position X
		int			iPositionY;			// Position Y
		int			iLayerWidth;		// Layerbreite
		int			iLayerHeight;		// Layerhöhe
		short		sTileSetNumber;		// Nummer ds TileSet
	};


	//  Layerobjekt kann Basisdaten, Metadaten oder Datenlayer enthalten.
	struct LayerRectangular : public meloader::LayerBase // Rechteckig
	{
		// Konstante ist auf 1
		char		cMasked;			// Maskiert (0:nein, 1:ja)
	};


	 // Layerobjekt kann Basisdaten, Metadaten oder Datenlayer enthalten.
	struct LayerIso1 : public meloader::LayerBase
	{
		// Konstante ist auf 2
	};


	//  Layerobjekt kann Basisdaten, Metadaten oder Datenlayer enthalten.
	struct LayerIso2 : public meloader::LayerBase
	{
		// Konstante ist auf 3
		char		cTileShift;			// TileShift (0:nein, 1:ja)
	};


	//  Layerobjekt kann Basisdaten, Metadaten oder Datenlayer enthalten.
	struct LayerHex1 : public meloader::LayerBase // Hexagonal
	{
		// Konstante ist auf 4
		char		cTileShift;			// TileShift (0:nein, 1:ja)
	};


	// Layerobjekt kann Basisdaten, Metadaten oder Datenlayer enthalten.
	struct LayerHex2 : public meloader::LayerBase
	{
		// Konstante ist auf 5
		char		cTileShift;			// TileShift (0:nein, 1:ja)
	};


	// Enthält keine Basisdaten oder Datenlayer! Kann aber Metadaten enthalten. Jeder
	// Layer bekommt eine eigene Nummer. Der erste Layer begint mit 1 - alle anderen
	// bekommen fortlaufende Nummer.
	struct LayerClon
	{
		// Konstante auf 6
		char		cVisible;			// Sichtbarkeit (0:nein, 1:ja)
		char		szName[12];			// Name des Layers
		char		cParallaxX;			// Parallax-Wert X
		char		cParallaxY;			// Parallax-Wert Y
		int			iPositionX;			// Position X
		int			iPositionY;			// Position Y
		int			iLayerNumber;		// Nummer des Layer
	};


	//  Kann nur hinter Layer-Objekten stehen (ausser Klonlayer).
	struct BaseData
	{
		// Konstante auf 102
		char		cDataDepth;			// Datentiefe (4, 8, 12 oder 16)
		int			iDataSize;			// Datengröße in Byte
		short		sReserve;			// Reserviert
	};
	// Nach dieser Struktur werden die Daten gespeichert!


	//  Kann nur hinter Layer-Objekten stehen (ausser Klonlayer).
	struct DataLayer
	{
		// Konstante auf 103
		char		cDataDepth;			// Datentiefe (4 oder 8)
		int			iDataSize;			// Datengröße in Byte
		short		sReserve;			// Reserviert
	};
	// Nach dieser Struktur werden die Daten direkt gespeichert!


	//  Kann hinter jedem Objekt stehen (ausser Tileset).
	struct MetaData
	{
		// Konstante auf 104
		char		cFormat;			// Datenformat (0:Binär, 1:ASCII)
		int			iSize;				// Datenlänge in Byte
		short		sReserve;			// Reserviert
	};
	// Nach dieser Struktur werden die Daten direkt gespeichert!


	//  Enthält keine Basisdaten oder Datenlayer! Kann aber Metadaten enthalten.
	struct ObjectPicture
	{
		// Konstante auf 7
		char		cVisible;			// Sichtbarkeit (0:nein, 1:ja)
		char		szName[12];			// Name des Bildes
		char		cParallaxX;			// Parallax-Wert X
		char		cParallaxY;			// Parallax-Wert Y
		int			iPositionX;			// Position X
		int			iPositionY;			// Position Y
		short		sTileSetNumber;		// Nummer des Tile Set
		short		sAnimTileNumber;	// Animations- Teilnummer
		short		sAnimStartFrame;	// Animationsstartframe
		char		cAnimMode;			// Animationsmodus
		char		cMasked;			// Maskiert (0:nein, 1:ja)
	};


	// Enthält keine Basisdaten oder Datenlayer! Kann aber Metadaten enthalten. Jeder
	// Layer bekommt eine eigene Nummer. Der erste Layer begint mit 1 - alle anderen 
	// bekommen fortlaufende Nummer.
	struct ObjectBlock
	{
		// Konstante auf 8
		char		cVisible;			// Sichtbarkeit (0:nein, 1:ja)
		char		szName[12];			// Name des Bildes
		char		cJustageX;			// Justage-Wert X
		char		cJustageY;			// Justage-Wert Y
		int			iTilePositionX;		// Teil Position X
		int			iTilePositionY;		// Teil Position Y
		short		sTileSetNumber;		// Nummer des Tile Set
		short		sAnimTileNumber;	// Animations- Teilnummer
		short		sAnimStartFrame;	// Animationsstartframe
		char		cAnimMode;			// Animationsmodus
		char		cMasked;			// Maskiert (0:nein, 1:ja)
		short		sLayerNumber;		// Nummer des Layers
	};


	//  Enthält keine Basisdaten oder Datenlayer! Kann aber Metadaten enthalten.
	struct ObjectPoint
	{
		// Konstante auf 9
		char		cVisible;			// Sichtbarkeit (0:nein, 1:ja)
		char		szName[12];			// Name des Bildes
		char		cParallaxX;			// Parallax-Wert X
		char		cParallaxY;			// Parallax-Wert Y
		int			iPositionX;			// Position X
		int			iPositionY;			// Position Y
	};


	//  Enthält keine Basisdaten oder Datenlayer! Kann aber Metadaten enthalten.
	struct ObjectLine
	{
		// Konstante auf 10
		char		cVisible;			// Sichtbarkeit (0:nein, 1:ja)
		char		szName[12];			// Name des Bildes
		char		cParallaxX;			// Parallax-Wert X
		char		cParallaxY;			// Parallax-Wert Y
		int			iStartPositionX;	// Startposition X
		int			iStartPositionY;	// Startposition Y
		int			iEndPositionX;		// Endposition X
		int			iEndPositionY;		// Endposition Y
	};


	//  Enthält keine Basisdaten oder Datenlayer! Kann aber Metadaten enthalten.
	struct ObjectRectangular
	{
		// Konstante auf 9
		char		cVisible;			// Sichtbarkeit (0:nein, 1:ja)
		char		szName[12];			// Name des Bildes
		char		cParallaxX;			// Parallax-Wert X
		char		cParallaxY;			// Parallax-Wert Y
		int			iPositionX;			// Position X
		int			iPositionY;			// Position Y
		int			iWidth;				// Breite
		int			iHeight;			// Höhe
	};


	//  Enthält keine Basisdaten oder Datenlayer! Kann aber Metadaten enthalten.
	struct ObjectOval
	{
		// Konstante auf 9
		char		cVisible;			// Sichtbarkeit (0:nein, 1:ja)
		char		szName[12];			// Name des Bildes
		char		cParallaxX;			// Parallax-Wert X
		char		cParallaxY;			// Parallax-Wert Y
		int			iPositionX;			// Position X
		int			iPositionY;			// Position Y
		int			iRadiusX;			// Radius X
		int			iRadiusY;			// Daius Y
	};


	// Füllmodus
	const char	FILLMODE_NONE				= 0;	// Leer
	const char	FILLMODE_COLOR				= 1;	// Farbe
	const char	FILLMODE_PICTURE			= 2;	// Ist eine Bitmap
	const char	FILLMODE_PICTUREMASKED		= 3;	// BItmap ist Maskiert


	// Format der Daten (Metadaten)
	const char	METADF_BINARY				= 0;	// Metadatenformat ist Binär
	const char	METADF_ASCII				= 1;	// Metadatebformat ist in ASCII Form


	// Animationsmodus
	const char	ANIMODE_STD					= 0;	// Standard Modus
	const char	ANIMODE_PAUSE				= 1;	// Animation ist pausiert
	const char	ANIMODE_FOREWARD			= 2;	// Animation läuft vorwärts
	const char	ANIMODE_BACKWARD			= 3;	// Animation läuft rückwärts


	// Identifikationsnummern der Objecte
	const char	IDENTOBJ_TILESET			= 100;
	const char	IDENTOBJ_ANIMATION			= 101;
	const char	IDENTOBJ_BASEDATA			= 102;
	const char	IDENTOBJ_DATALAYER			= 103;
	const char	IDENTOBJ_METADATA			= 104;
	const char	IDENTOBJ_BACKGROUND			= 0;
	const char	IDENTOBJ_LAYERRECTANGULAR	= 1;
	const char	IDENTOBJ_LAYERISO1			= 2;
	const char	IDENTOBJ_LAYERISO2			= 3;
	const char	IDENTOBJ_LAYERHEX1			= 4;
	const char	IDENTOBJ_LAYERHEX2			= 5;
	const char	IDENTOBJ_LAYERCLON			= 6;
	const char	IDENTOBJ_OBJECTPICTURE		= 7;
	const char	IDENTOBJ_OBJECTBLOCK		= 8;
	const char	IDENTOBJ_OBJECTPOINT		= 9;
	const char	IDENTOBJ_OBJECTLINE			= 10;
	const char	IDENTOBJ_OBJECTRECT			= 11;
	const char	IDENTOBJ_OBJECTOVAL			= 12;


	// Der Rückgabetype
	typedef long	result;

	const result	RES_OK						= 0;	// Alles OK
	const result	RES_UNKNOWN					= 1;	// Unbekanter Fehler
	const result	RES_WRONGPARAMETER			= 2;	// Ein oder mehr Parameter sind inkorrekt
	const result	RES_NOTOPENFILE				= 3;	// Fehler beim öffnen der Datei
	const result	RES_NOTAUMFFILE				= 4;	// Datei ist kein Universal Map File (UMF)
	const result	RES_NOFILEINSTANZ			= 5;	// Es wurde keine Dateiinstanz angegeben
	const result	RES_INVALIDOBJECT			= 6;	// Es wurde ein falsches Objekt gefunden
	const result	RES_WRONGIDENTIFIER			= 7;	// Falsche Identifizierung
	const result	RES_WRONGSUBDATA			= 8;	// Für ein Object wurden falsche Sub Daten eingelesen
	const result	RES_NOTENOUGHTMEMORY		= 9;	// Es ist nicht genug Speicher vorhanden
	const result	RES_INVALIDFILESIZE			= 10;	// Die Datei hat nicht die berechnete Größe
	const result	RES_SECONDBACKGROUND		= 11;	// Es ist ein zweiter Hintergrund vorhanden
	const result	RES_NOINTERFACESET			= 12;	// Das Interface für diese Anfrage wurde nicht geladen
	const result	RES_INVALIDTILESETNUMBER	= 13;	// Es wurde eine falsche TileSet Nummer angegeben
	const result	RES_WRONGNAME				= 14;	// Es wurde ein falscher Name zur Identifzierung angegeben
	const result	RES_WRONGANIMATIONINDEX		= 15;	// Es wurde ein falsche Animationsindex gesetzt
	const result	RES_WRONGANIMATIONDATAINDEX	= 16;	// Es wurde ein falsche Index für die Animationsdaten gesetzt
	const result	RES_INVALIDKOORD			= 17;	// Es wurden ungültige XY Koordinaten angegeben
	const result	RES_INVALIDLAYERID			= 18;	// Die Layer ID des Clon ist nicht korrekt
	const result	RES_NOTALLBLOCKSSAVED		= 19;	// Es wurden nicht alle Blöcke in der Datei gespeichert die angegeben wurden
	const result	RES_NOSUBDATASET			= 20;	// Das Objekt besitzt keine Subdaten
	const result	RES_NOPASSWORDSET			= 21;	// Es wurde kein Passwort gesetzt, Datei ist aber verschlüsselt
	const result	RES_INVALIDSHA1FINGERPRINT	= 22;	// Der SHA1 Fingerprint konnt nicht erzeugt werden
	const result	RES_INVALIDSHA1RESULT		= 23;	// Das Ergebnis des SHA1 Fingerpirnts konnt nicht gelesen werden
	const result	RES_INVALIDPASSWORD			= 24;	// Das Passwort ist nicht korrekt


	class MapFile;
	class ISubData;
	class IMapObject
	{
	public:
		virtual meloader::result AddReferenz() = 0;
		virtual meloader::result Initialize(const long lFilePos, MapFile* pFile) = 0;
		virtual meloader::result Release() = 0;

		virtual meloader::result Identify(char* pcIdent) = 0;
		virtual meloader::result GetData(const char cIdent, void* pvData, const bool bData = false) = 0;
		virtual meloader::result Have(const char cIdent, int* piHave) = 0;
	};


	class ILayer : public meloader::IMapObject
	{
	public:
		virtual meloader::result IdentifyName(const char* szName) = 0;
		virtual meloader::result QuerySubData(const char cIdent, meloader::ISubData** ppSubData) = 0;
	};


	class IObject : public meloader::IMapObject
	{
	public:
		virtual meloader::result IdentifyName(const char* szName) = 0;
		virtual meloader::result GetMetaData(meloader::ISubData** ppSubData) = 0;
	};


	class ITileSet : public meloader::IMapObject
	{
	public:
		virtual meloader::result GetAnimation(meloader::Animation* pAnimation, const int iIndex) = 0;
		virtual meloader::result GetAnimationData(meloader::AnimationData* pAnimationData, const int iAnimationIndex, const int iDataIndex) = 0;
	};


	class ISubData : public meloader::IMapObject
	{
	public:
		virtual meloader::result Initialize(const long lFilePos, MapFile* pFile, const int iDataWidth) = 0;
		virtual meloader::result GetValue(int* piValue, const int x, const int y) = 0;
	};


	class IBackground : public meloader::IMapObject
	{
	public:
		virtual meloader::result GetMetaData(meloader::ISubData** ppSubData) = 0;
	};


	class MapFile
	{
		meloader::FileHeader				m_Header;		// Header der Datei
		std::string							m_strFileName;	// Name der geöffneten Datei

		meloader::IBackground*				m_pBackground;	// Der Hintergrund
		std::vector<meloader::ITileSet*>	m_vTileSet;		// Liste der TileSet's
		std::vector<meloader::IObject*>		m_vObject;		// Liste der Objekte der Datei
		std::vector<meloader::ILayer*>		m_vLayer;		// Liste der Layer in der Date

	public:
		MapFile();
		~MapFile();

		meloader::result	Open(const char* szMap, const char* szPwd);
		meloader::result	Close();

		std::string			GetFileName() const { return m_strFileName; }
		bool				GetSHA1(unsigned char* pucSHA1) const;

		meloader::result	QueryBackground(meloader::IBackground** ppBackground);
		meloader::result	QueryLayer(meloader::ILayer** ppLayer, const char cIdent, const char* szName);
		meloader::result	QueryObject(meloader::IObject** ppObject, const char cIdent, const char* szName);
		meloader::result	QueryTileSet(meloader::ITileSet** ppTileSet, const int iNumber);

		// Direkter Zugriff
		std::vector<meloader::ITileSet*>::const_iterator	GetTileSetList()	const { return m_vTileSet.begin(); }
		std::vector<meloader::ITileSet*>::const_iterator	GetTileSetEList()	const { return m_vTileSet.end(); }

		std::vector<meloader::IObject*>::const_iterator		GetObjectList()		const { return m_vObject.begin(); }
		std::vector<meloader::IObject*>::const_iterator		GetObjectEList()	const { return m_vObject.end(); }

		std::vector<meloader::ILayer*>::const_iterator		GetLayerList()		const { return m_vLayer.begin(); }
		std::vector<meloader::ILayer*>::const_iterator		GetLayerEList()		const { return m_vLayer.end(); }
	};
};


#endif // _MELOADER_H