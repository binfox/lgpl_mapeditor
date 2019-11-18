
#pragma pack(1)

#include "Background.h"
#include <fstream>
#include "SubData.h"


CBackground::CBackground() : m_iRef(0),
							 m_pFile(0),
							 m_lFilePos(0),
							 m_pMetaData(0)
{
}



meloader::result CBackground::AddReferenz()
{
	++m_iRef;
	return meloader::RES_OK;
}


meloader::result CBackground::Release()
{
	if(!(--m_iRef))
	{
		if(m_pMetaData)
		{
			m_pMetaData->Release();
			m_pMetaData = 0;
		}

		delete this;
	}

	return meloader::RES_OK;
}


meloader::result CBackground::Initialize(const long lFilePos,
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


	// Auslesen des Hintergrundes
	fin.read((char*)&m_Defination, sizeof(meloader::Background));


	// Prüfen ob Metadaten vorliegen
	char cConstant;
	fin.read(&cConstant, 1);

	if(cConstant == meloader::IDENTOBJ_METADATA)
	{
		// Metadaten sind vorhanden
		m_pMetaData = new CSubData();
		meloader::result res = m_pMetaData->Initialize(fin.tellg(), pFile, m_Defination.iCardWidth);
		if(res != meloader::RES_OK)
		{
			m_pMetaData->Release();
			return res;
		}
	}

	return meloader::RES_OK;
}


meloader::result CBackground::Identify(char* pcIdent)
{
#ifdef _DEBUG
	if(!pcIdent) { return meloader::RES_WRONGPARAMETER; }
#endif

	*pcIdent = meloader::IDENTOBJ_BACKGROUND;
	return meloader::RES_OK;
}


meloader::result CBackground::GetData(const char cIdent,
									  void* pvData,
									  const bool bData)
{
#ifdef _DEBUG
	if(!pvData) { return meloader::RES_WRONGPARAMETER; }
#endif

	switch(cIdent)
	{
	case meloader::IDENTOBJ_BACKGROUND:
		memcpy(pvData, &m_Defination, sizeof(meloader::Background));
		break;
	case meloader::IDENTOBJ_METADATA:
		{
			if(m_pMetaData)
			{
				meloader::result res = m_pMetaData->GetData(cIdent, pvData, bData);
				if(res != meloader::RES_OK) { return res; }
				break;
			}
			else
				return meloader::RES_WRONGIDENTIFIER;
		}
	default:
		return meloader::RES_WRONGIDENTIFIER;
	};

	return meloader::RES_OK;
}


meloader::result CBackground::Have(const char cIdent,
								   int* piHave)
{
#ifdef _DEBUG
	if(!piHave) { return meloader::RES_WRONGPARAMETER; }
#endif

	*piHave = false;

	if(m_pMetaData) *piHave = true;
	return meloader::RES_OK;
}


meloader::result CBackground::GetMetaData(meloader::ISubData** ppSubData)
{
#ifdef _DEBUG
	if(!ppSubData) { return meloader::RES_WRONGPARAMETER; }

	if(!this->m_pMetaData) { return meloader::RES_NOSUBDATASET; }
#endif

	*ppSubData = m_pMetaData;
	return meloader::RES_OK;
}