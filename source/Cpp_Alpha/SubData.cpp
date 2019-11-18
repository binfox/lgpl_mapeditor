
#pragma pack(1)

#include "SubData.h"
#include <fstream>
#include "sha1.h"


CSubData::CSubData() : m_iRef(0),
					   m_pFile(0),
					   m_lFilePos(0),
					   m_pucData(0)
{
}


meloader::result CSubData::AddReferenz()
{
	++m_iRef;
	return meloader::RES_OK;
}


meloader::result CSubData::Release()
{
	if(!(--m_iRef))
	{
		if(m_pucData)
		{
			delete[] m_pucData;
			m_pucData = 0;
		}
		delete this;
	}

	return meloader::RES_OK;
}


meloader::result CSubData::Initialize(const long lFilePos,
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

	int iDataSize = 0;

	switch(m_cIdentifier)
	{
	case meloader::IDENTOBJ_BASEDATA:
		fin.read((char*)&m_BaseData, sizeof(meloader::BaseData));
		iDataSize = m_BaseData.iDataSize;
		break;
	case meloader::IDENTOBJ_DATALAYER:
		fin.read((char*)&m_DataLayer, sizeof(meloader::DataLayer));
		iDataSize = m_DataLayer.iDataSize;
		break;
	case meloader::IDENTOBJ_METADATA:
		fin.read((char*)&m_MetaData, sizeof(meloader::MetaData));
		iDataSize = m_MetaData.iSize;
		break;
	};


	// Auslesen der Daten
	if(iDataSize)
	{
		// Speicher für die Daten besorgen
		m_pucData = new unsigned char[iDataSize];
		if(!m_pucData) { return meloader::RES_NOTENOUGHTMEMORY; }

		fin.read((char*)m_pucData, iDataSize);

		// Daten entschlüssel, wenn Datei verschlüssel
		unsigned char ucSHA1[sha1::SHA1HashSize];
		if(m_pFile->GetSHA1(ucSHA1))
		{
			for(int i = 0; i < iDataSize; ++i)
			{
				this->m_pucData[i] = this->m_pucData[i] ^ (i % sha1::SHA1HashSize);
			}
		}
	}

	return meloader::RES_OK;
}


meloader::result CSubData::Identify(char* pcIdent)
{
#ifdef _DEBUG
	if(!pcIdent) { return meloader::RES_WRONGPARAMETER; }
#endif

	*pcIdent = m_cIdentifier;
	return meloader::RES_OK;
}


meloader::result CSubData::GetData(const char cIdent,
								   void* pvData,
								   const bool bData)
{
#ifdef _DEBUG
	if(!pvData) { return meloader::RES_WRONGPARAMETER; }
#endif

	if(cIdent == meloader::IDENTOBJ_METADATA)
	{
		if(bData)	memcpy(pvData, m_pucData, m_MetaData.iSize);
		else		memcpy(pvData, &m_MetaData, sizeof(meloader::IDENTOBJ_METADATA));
	}
	else if(cIdent == meloader::IDENTOBJ_BASEDATA)
	{
		if(bData)	memcpy(pvData, m_pucData, m_BaseData.iDataSize);
		else		memcpy(pvData, &m_BaseData, sizeof(meloader::BaseData));
	}
	else if(cIdent == meloader::IDENTOBJ_DATALAYER)
	{
		if(bData)	memcpy(pvData, m_pucData, m_DataLayer.iDataSize);
		else		memcpy(pvData, &m_DataLayer, sizeof(meloader::DataLayer));
	}
	else
		return meloader::RES_WRONGIDENTIFIER;

	return meloader::RES_OK;
}


meloader::result CSubData::Have(const char cIdent,
								int* piHave)
{
#ifdef _DEBUG
	if(!piHave) { return meloader::RES_WRONGPARAMETER; }
#endif

	*piHave = false;

	return meloader::RES_OK;
}


meloader::result CSubData::Initialize(const long lFilePos, 
									  meloader::MapFile* pFile,
									  const int iDataWidth)
{
	m_iDataWidth = iDataWidth;

	return Initialize(lFilePos, pFile);
}


meloader::result CSubData::GetValue(int* piValue,
									const int x,
									const int y)
{
#ifdef _DEBUG
	if(!piValue) { return meloader::RES_WRONGPARAMETER; }
#endif

	int iDataDepth = 0;
	switch(this->m_cIdentifier)
	{
	case meloader::IDENTOBJ_BASEDATA:
		iDataDepth = m_BaseData.cDataDepth;
		break;
	case meloader::IDENTOBJ_DATALAYER:
		iDataDepth = m_DataLayer.cDataDepth;
		break;
	case meloader::IDENTOBJ_METADATA:
		iDataDepth = 8;
		break;
	};

	int offset = 0, mode = 0;
	switch(iDataDepth)
	{
	case 4:
		{
			int offset	= (y * m_iDataWidth + x) / 2;
			int mode	= ((y * m_iDataWidth + x) & 1) * 4;
#ifdef _DEBUG
			if(offset >= this->m_BaseData.iDataSize) { return meloader::RES_INVALIDKOORD; }
#endif
			*piValue	= (this->m_pucData[offset] >> mode) & 15;
			break;
		}
	case 8:
		{
			int offset	= y * m_iDataWidth + x;
#ifdef _DEBUG
			if(offset >= this->m_BaseData.iDataSize) { return meloader::RES_INVALIDKOORD; }
#endif
			*piValue	= this->m_pucData[offset];
			break;
		}
	case 12:
		{
			int offset	= ((y * m_iDataWidth + x) * 3) / 2;
			int mode	= ((y * m_iDataWidth + x) & 1) * 4;
#ifdef _DEBUG
			if(offset >= this->m_BaseData.iDataSize) { return meloader::RES_INVALIDKOORD; }
#endif
			*piValue	= (this->m_pucData[offset] >> mode) & 4095;
			break;
		}
	case 16:
		{
			int offset	= (y * m_iDataWidth + x) * 2;
#ifdef _DEBUG
			if(offset >= this->m_BaseData.iDataSize) { return meloader::RES_INVALIDKOORD; }
#endif
			*piValue	= this->m_pucData[offset];
			break;
		}
	};

	return meloader::RES_OK;
}