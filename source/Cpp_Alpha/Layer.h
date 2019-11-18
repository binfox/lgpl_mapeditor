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
// Description:		Definiert das Objekt für ein TileSet. Es liest die			//
//					Informationen des TileSet aus und speichert sie. Dabei		//
//					werden zugleich auch die Animationsinformationen ausgelesen.//
//																				//
//////////////////////////////////////////////////////////////////////////////////
#ifndef _MELOADER_LAYERRECT_H
#define _MELOADER_LAYERRECT_H


#include "meloader.h"


class CLayer : public meloader::ILayer
{
	int								m_iRef;
	meloader::MapFile*				m_pFile;
	long							m_lFilePos;
	meloader::ISubData*				m_pSubObject;
	char							m_cIdentifier;

	// Layer Definitionen
	meloader::LayerHex1				m_LayerHex1;
	meloader::LayerHex2				m_LayerHex2;
	meloader::LayerIso1				m_LayerIso1;
	meloader::LayerIso2				m_LayerIso2;
	meloader::LayerRectangular		m_LayerRect;

public:
	CLayer();

	meloader::result AddReferenz();
	meloader::result Initialize(const long lFilePos, meloader::MapFile* pFile);
	meloader::result Release();

	meloader::result Identify(char* pcIdent);
	meloader::result GetData(const char cIdent, void* pvData, const bool bData);
	meloader::result Have(const char cIdent, int* piHave);

	meloader::result IdentifyName(const char* szName);
	meloader::result QuerySubData(const char cIdent, meloader::ISubData** ppSubData);
};


#endif // _MELOADER_LAYERRECT_H