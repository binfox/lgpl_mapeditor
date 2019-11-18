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
#ifndef _MELOADER_OBJECT_H
#define _MELOADER_OBJECT_H


#include "meloader.h"


class CObject : public meloader::IObject
{
	int							m_iRef;
	meloader::MapFile*			m_pFile;
	long						m_lFilePos;
	char						m_cIdentifier;
	meloader::ISubData*			m_pMetaData;

	// Definition
	meloader::ObjectBlock		m_ObjectBlock;
	meloader::ObjectLine		m_ObjectLine;
	meloader::ObjectOval		m_ObjectOval;
	meloader::ObjectPicture		m_ObjectPicture;
	meloader::ObjectPoint		m_ObjectPoint;
	meloader::ObjectRectangular	m_ObjectRect;

public:
	CObject();

	meloader::result AddReferenz();
	meloader::result Initialize(const long lFilePos, meloader::MapFile* pFile);
	meloader::result Release();

	meloader::result Identify(char* pcIdent);
	meloader::result GetData(const char cIdent, void* pvData, const bool bData);
	meloader::result Have(const char cIdent, int* piHave);

	meloader::result IdentifyName(const char* szName);
	meloader::result GetMetaData(meloader::ISubData** ppSubData);
};


#endif // _MELOADER_OBJECT_H