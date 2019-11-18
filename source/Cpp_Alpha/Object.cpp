
#pragma pack(1)

#include "Object.h"
#include "SubData.h"
#include <fstream>
#include <string>


CObject::CObject() : m_iRef(0),
					 m_pFile(0),
					 m_lFilePos(0),
					 m_pMetaData(0)
{
}


meloader::result CObject::AddReferenz()
{
	++m_iRef;
	return meloader::RES_OK;
}


meloader::result CObject::Release()
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


meloader::result CObject::Initialize(const long lFilePos,
									 meloader::MapFile* pFile)
{
#ifdef _DEBUG
	if(!pFile) { return meloader::RES_NOFILEINSTANZ; }
#endif


	// Datei öffnen und auf die Position setzen
	std::fstream fin(pFile->GetFileName().c_str(), std::ios::binary | std::ios::in);
	if(!fin.is_open()) { return meloader::RES_NOTOPENFILE; }
	fin.seekg(lFilePos-1, std::ios::beg);

	m_lFilePos	= lFilePos;
	m_pFile		= pFile;

	
	// Auslesen der Identifikation
	fin.read((char*)&m_cIdentifier, 1);


	// Auslesen der Definition
	switch(m_cIdentifier)
	{
	case meloader::IDENTOBJ_OBJECTBLOCK:
		fin.read((char*)&m_ObjectBlock, sizeof(meloader::ObjectBlock));
		break;
	case meloader::IDENTOBJ_OBJECTLINE:
		fin.read((char*)&m_ObjectLine, sizeof(meloader::ObjectLine));
		break;
	case meloader::IDENTOBJ_OBJECTOVAL:
		fin.read((char*)&m_ObjectOval, sizeof(meloader::ObjectOval));
		break;
	case meloader::IDENTOBJ_OBJECTPICTURE:
		fin.read((char*)&m_ObjectPicture, sizeof(meloader::ObjectPicture));
		break;
	case meloader::IDENTOBJ_OBJECTPOINT:
		fin.read((char*)&m_ObjectPoint, sizeof(meloader::IDENTOBJ_OBJECTPOINT));
		break;
	case meloader::IDENTOBJ_OBJECTRECT:
		fin.read((char*)&m_ObjectRect, sizeof(meloader::ObjectRectangular));
		break;
	};


	// Prüfen ob MetaDaten vorhanden sind
	char cSubIdent;
	fin.read(&cSubIdent, 1);

	if(cSubIdent == meloader::IDENTOBJ_METADATA)
	{
		// Meta Daten sind vorhanden
		m_pMetaData = new CSubData();

		meloader::result res = m_pMetaData->Initialize(fin.tellg(), pFile, 0);
		if(res != meloader::RES_OK) { return res; }
	}

	return meloader::RES_OK;
}


meloader::result CObject::Identify(char* pcIdent)
{
#ifdef _DEBUG
	if(!pcIdent) { return meloader::RES_WRONGPARAMETER; }
#endif

	*pcIdent = m_cIdentifier;
	return meloader::RES_OK;
}


meloader::result CObject::GetData(const char cIdent,
								  void* pvData,
								  const bool bData)
{
#ifdef _DEBUG
	if(!pvData) { return meloader::RES_WRONGPARAMETER; }
#endif

	switch(cIdent)
	{
	case meloader::IDENTOBJ_OBJECTBLOCK:
		memcpy(pvData, &m_ObjectBlock, sizeof(meloader::ObjectBlock));
		break;
	case meloader::IDENTOBJ_OBJECTLINE:
		memcpy(pvData, &m_ObjectLine, sizeof(meloader::ObjectLine));
		break;
	case meloader::IDENTOBJ_OBJECTOVAL:
		memcpy(pvData, &m_ObjectOval, sizeof(meloader::ObjectOval));
		break;
	case meloader::IDENTOBJ_OBJECTPICTURE:
		memcpy(pvData, &m_ObjectPicture, sizeof(meloader::ObjectPicture));
		break;
	case meloader::IDENTOBJ_OBJECTPOINT:
		memcpy(pvData, &m_ObjectPoint, sizeof(meloader::ObjectPoint));
		break;
	case meloader::IDENTOBJ_OBJECTRECT:
		memcpy(pvData, &m_ObjectRect, sizeof(meloader::ObjectRectangular));
		break;
	default:
		{
			if(m_pMetaData)
			{
				meloader::result res = m_pMetaData->GetData(cIdent, pvData, bData);
				if(res != meloader::RES_OK) { return res; }
			}
			else
				return meloader::RES_WRONGIDENTIFIER;
		}
	};

	return meloader::RES_OK;
}


meloader::result CObject::Have(const char cIdent,
							   int* piHave)
{
#ifdef _DEBUG
	if(!piHave) { return meloader::RES_WRONGPARAMETER; }
#endif

	if(cIdent == meloader::IDENTOBJ_METADATA)
		*piHave = true;
	else
		*piHave = false;

	return meloader::RES_OK;
}


meloader::result CObject::IdentifyName(const char* szName)
{
#ifdef _DEBUG
	if(!szName) { return meloader::RES_WRONGPARAMETER; }
#endif

	std::string strName(szName);

	switch(m_cIdentifier)
	{
	case meloader::IDENTOBJ_OBJECTBLOCK:
		if(strName != m_ObjectBlock.szName)		return meloader::RES_WRONGNAME;
	case meloader::IDENTOBJ_OBJECTLINE:
		if(strName != m_ObjectLine.szName)		return meloader::RES_WRONGNAME;
	case meloader::IDENTOBJ_OBJECTOVAL:
		if(strName != m_ObjectOval.szName)		return meloader::RES_WRONGNAME;
	case meloader::IDENTOBJ_OBJECTPICTURE:
		if(strName != m_ObjectPicture.szName)	return meloader::RES_WRONGNAME;
	case meloader::IDENTOBJ_OBJECTPOINT:
		if(strName != m_ObjectPoint.szName)		return meloader::RES_WRONGNAME;
	case meloader::IDENTOBJ_OBJECTRECT:
		if(strName != m_ObjectRect.szName)		return meloader::RES_WRONGNAME;
	};

	return meloader::RES_OK;
}


meloader::result CObject::GetMetaData(meloader::ISubData** ppSubData)
{
#ifdef _DEBUG
	if(!ppSubData) { return meloader::RES_WRONGPARAMETER; }

	if(!this->m_pMetaData)
		return meloader::RES_NOSUBDATASET;
#endif

	*ppSubData = m_pMetaData;
	return meloader::RES_OK;
}