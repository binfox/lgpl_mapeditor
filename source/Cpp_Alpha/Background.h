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
//																				//
//////////////////////////////////////////////////////////////////////////////////
#ifndef _MELOADER_BACKGROUND_H
#define _MELOADER_BACKGROUND_H


#include "meloader.h"


class CBackground : public meloader::IBackground
{
	int						m_iRef;
	meloader::Background	m_Defination;
	meloader::MapFile*		m_pFile;
	long					m_lFilePos;
	meloader::ISubData*		m_pMetaData;

public:
	CBackground();

	meloader::result AddReferenz();
	meloader::result Initialize(const long lFilePos, meloader::MapFile* pFile);
	meloader::result Release();

	meloader::result Identify(char* pcIdent);
	meloader::result GetData(const char cIdent, void* pvData, const bool bData);
	meloader::result Have(const char cIdent, int* piHave);

	meloader::result GetMetaData(meloader::ISubData** ppSubData);
};


#endif // _MELOADER_BACKGROUND_H