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
#ifndef _MELOADER_TILESET_H
#define _MELOADER_TILESET_H


#include "meloader.h"


class CTileSet : public meloader::ITileSet
{
	struct Animation
	{
		meloader::Animation						Defination;
		std::vector<meloader::AnimationData>	vData;
	};

	int							m_iRef;
	meloader::TileSet			m_Defination;
	std::vector<Animation>		m_vAnimation;
	meloader::MapFile*			m_pFile;
	long						m_lFilePos;

public:
	CTileSet();

	meloader::result AddReferenz();
	meloader::result Initialize(const long lFilePos, meloader::MapFile* pFile);
	meloader::result Release();

	meloader::result Identify(char* pcIdent);
	meloader::result GetData(const char cIdent, void* pvData, const bool bData);
	meloader::result Have(const char cIdent, int* piHave);

	meloader::result GetAnimation(meloader::Animation* pAnimation, const int iIndex);
	meloader::result GetAnimationData(meloader::AnimationData* pAnimationData, const int iAnimationIndex, const int iDataIndex);
};


#endif // _MELOADER_TILESET_H