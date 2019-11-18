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
#ifndef _MELOADER_SUBDATA_H
#define _MELOADER_SUBDATA_H


#include "meloader.h"


class CSubData : public meloader::ISubData
{
	int						m_iRef;
	meloader::MapFile*		m_pFile;
	long					m_lFilePos;
	unsigned char*			m_pucData;
	char					m_cIdentifier;
	int						m_iDataWidth;

	// SubData Definitionen
	meloader::MetaData		m_MetaData;
	meloader::BaseData		m_BaseData;
	meloader::DataLayer		m_DataLayer;

public:
	CSubData();

	meloader::result AddReferenz();
	meloader::result Initialize(const long lFilePos, meloader::MapFile* pFile);
	meloader::result Release();

	meloader::result Identify(char* pcIdent);
	meloader::result GetData(const char cIdent, void* pvData, const bool bData);
	meloader::result Have(const char cIdent, int* piHave);

	meloader::result Initialize(const long lFilePos, meloader::MapFile* pFile, const int iDataWidth);
	meloader::result GetValue(int* piValue, const int x, const int y);
};


#endif // _MELOADER_SUBDATA_H