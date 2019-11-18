
#pragma pack(1)

#include "TileSet.h"
#include <fstream>


CTileSet::CTileSet() : m_iRef(0),
					   m_pFile(0)
{
}


meloader::result CTileSet::AddReferenz()
{
	++m_iRef;
	return meloader::RES_OK;
}


meloader::result CTileSet::Release()
{
	if(!(--m_iRef))
	{
		delete this;
	}

	return meloader::RES_OK;
}


meloader::result CTileSet::Initialize(const long lFilePos,
									  meloader::MapFile* pFile)
{
#ifdef _DEBUG
	if(!pFile) { return meloader::RES_NOFILEINSTANZ; }
#endif


	// Datei öffnen und auf die Position setzen
	std::fstream fin(pFile->GetFileName().c_str(), std::ios::binary | std::ios::in);
	if(!fin.is_open()) { return meloader::RES_NOTOPENFILE; }
	fin.seekg(lFilePos, std::ios::beg);

	m_lFilePos	= lFilePos;
	m_pFile		= pFile;

	// TileSet Header lesen
	fin.read((char*)&this->m_Defination, sizeof(meloader::TileSet));

	// Animationen einlesen
	for(int c = 0; c < m_Defination.sNumAnimations; ++c)
	{
		// Object Identifizieren
		char cIdent;
		fin.read(&cIdent, 1);
		if(cIdent != meloader::IDENTOBJ_ANIMATION) { return meloader::RES_INVALIDOBJECT; }

		// Animationsinformationen einlesen
		CTileSet::Animation ani;
		fin.read((char*)&ani.Defination, sizeof(meloader::Animation));

		// Animationsdaten einlesen
		for(int d = 0; d < ani.Defination.sNumFrames; ++d)
		{
			meloader::AnimationData data;
			fin.read((char*)&data, sizeof(meloader::AnimationData));
			ani.vData.push_back(data);
		}

		this->m_vAnimation.push_back(ani);
	}

	return meloader::RES_OK;
}


meloader::result CTileSet::Identify(char* pcIdent)
{
#ifdef _DEBUG
	if(!pcIdent) { return meloader::RES_WRONGPARAMETER; }
#endif

	*pcIdent = meloader::IDENTOBJ_TILESET;
	return meloader::RES_OK;
}


meloader::result CTileSet::GetData(const char cIdent,
								   void* pvData,
								   const bool bData)
{
#ifdef _DEBUG
	if(!pvData) { return meloader::RES_WRONGPARAMETER; }
#endif

	if(cIdent == meloader::IDENTOBJ_TILESET)
	{
		memcpy(pvData, &m_Defination, sizeof(meloader::TileSet));
	}
	else return meloader::RES_WRONGIDENTIFIER;

	return meloader::RES_OK;
}


meloader::result CTileSet::Have(const char cIdent,
								int* piHave)
{
#ifdef _DEBUG
	if(!piHave) { return meloader::RES_WRONGPARAMETER; }
#endif

	*piHave = false;	// Std auf nicht vorhanden

	if(cIdent == meloader::IDENTOBJ_ANIMATION)
		*piHave = true;
	else
		return meloader::RES_WRONGIDENTIFIER;

	return meloader::RES_OK;
}


meloader::result CTileSet::GetAnimation(meloader::Animation* pAnimation,
										const int iIndex)
{
#ifdef _DEBUG
	if(!pAnimation) { return meloader::RES_WRONGPARAMETER; }

	if(iIndex-1 > this->m_vAnimation.size())
		return meloader::RES_WRONGANIMATIONINDEX;
#endif

	memcpy(pAnimation, &this->m_vAnimation[iIndex-1].Defination, sizeof(meloader::Animation));

	return meloader::RES_OK;
}


meloader::result CTileSet::GetAnimationData(meloader::AnimationData* pAnimationData,
											const int iAnimationIndex,
											const int iDataIndex)
{
#ifdef _DEBUG
	if(!pAnimationData) { return meloader::RES_WRONGPARAMETER; }

	if(iAnimationIndex-1 > this->m_vAnimation.size())
		return meloader::RES_WRONGANIMATIONINDEX;
	if(iDataIndex-1 > this->m_vAnimation[iAnimationIndex-1].vData.size())
		return meloader::RES_WRONGANIMATIONDATAINDEX;
#endif

	meloader::AnimationData* pData = &m_vAnimation[iAnimationIndex-1].vData[iDataIndex-1];
	memcpy(pAnimationData, pData, sizeof(meloader::AnimationData));

	return meloader::RES_OK;
}