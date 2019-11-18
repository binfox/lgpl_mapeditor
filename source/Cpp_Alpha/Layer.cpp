
#pragma pack(1)

#include "Layer.h"
#include "SubData.h"
#include <fstream>
#include <string>


CLayer::CLayer() : m_iRef(0),
				   m_pFile(0),
				   m_lFilePos(0),
				   m_pSubObject(0)
{
}


meloader::result CLayer::AddReferenz()
{
	++m_iRef;
	return meloader::RES_OK;
}


meloader::result CLayer::Release()
{
	if(!(--m_iRef))
	{
		if(m_pSubObject)
		{
			m_pSubObject->Release();
			m_pSubObject = 0;
		}

		delete this;
	}

	return meloader::RES_OK;
}


meloader::result CLayer::Initialize(const long lFilePos,
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


	// Identifikation auslesen
	fin.read(&m_cIdentifier, 1);

	int iDataWidth = 0;
	// Definition auslesen
	switch(m_cIdentifier)
	{
	case meloader::IDENTOBJ_LAYERCLON:
		{
			meloader::LayerClon LayerClon;
			fin.read((char*)&LayerClon, sizeof(meloader::LayerClon));
			std::vector<meloader::ILayer*>::const_iterator iter = pFile->GetLayerList() + (LayerClon.iLayerNumber - 1);
			if(iter == pFile->GetLayerEList()) { return meloader::RES_INVALIDLAYERID; }
			
			char cIdentClon;
			(*iter)->Identify(&cIdentClon);
			switch(cIdentClon)
			{
			case meloader::IDENTOBJ_LAYERHEX1:
				m_LayerHex1.cVisible	= LayerClon.cVisible;
				m_LayerHex1.cParallaxX	= LayerClon.cParallaxX;
				m_LayerHex1.cParallaxY	= LayerClon.cParallaxY;
				m_LayerHex1.iPositionX	= LayerClon.iPositionX;
				m_LayerHex1.iPositionY	= LayerClon.iPositionY;
				strncpy(m_LayerHex1.szName, LayerClon.szName, 12);
				iDataWidth = m_LayerHex1.iLayerWidth;
				break;
			case meloader::IDENTOBJ_LAYERHEX2:
				m_LayerHex2.cVisible	= LayerClon.cVisible;
				m_LayerHex2.cParallaxX	= LayerClon.cParallaxX;
				m_LayerHex2.cParallaxY	= LayerClon.cParallaxY;
				m_LayerHex2.iPositionX	= LayerClon.iPositionX;
				m_LayerHex2.iPositionY	= LayerClon.iPositionY;
				strncpy(m_LayerHex2.szName, LayerClon.szName, 12);
				iDataWidth = m_LayerHex2.iLayerWidth;
				break;
			case meloader::IDENTOBJ_LAYERISO1:
				m_LayerIso1.cVisible	= LayerClon.cVisible;
				m_LayerIso1.cParallaxX	= LayerClon.cParallaxX;
				m_LayerIso1.cParallaxY	= LayerClon.cParallaxY;
				m_LayerIso1.iPositionX	= LayerClon.iPositionX;
				m_LayerIso1.iPositionY	= LayerClon.iPositionY;
				strncpy(m_LayerIso1.szName, LayerClon.szName, 12);
				iDataWidth = m_LayerIso1.iLayerWidth;
				break;
			case meloader::IDENTOBJ_LAYERISO2:
				m_LayerIso2.cVisible	= LayerClon.cVisible;
				m_LayerIso2.cParallaxX	= LayerClon.cParallaxX;
				m_LayerIso2.cParallaxY	= LayerClon.cParallaxY;
				m_LayerIso2.iPositionX	= LayerClon.iPositionX;
				m_LayerIso2.iPositionY	= LayerClon.iPositionY;
				strncpy(m_LayerIso2.szName, LayerClon.szName, 12);
				iDataWidth = m_LayerIso2.iLayerWidth;
				break;
			case meloader::IDENTOBJ_LAYERRECTANGULAR:
				m_LayerRect.cVisible	= LayerClon.cVisible;
				m_LayerRect.cParallaxX	= LayerClon.cParallaxX;
				m_LayerRect.cParallaxY	= LayerClon.cParallaxY;
				m_LayerRect.iPositionX	= LayerClon.iPositionX;
				m_LayerRect.iPositionY	= LayerClon.iPositionY;
				strncpy(m_LayerRect.szName, LayerClon.szName, 12);
				iDataWidth = m_LayerRect.iLayerWidth;
				break;
			default: return meloader::RES_INVALIDOBJECT;
			};

			this->m_cIdentifier = cIdentClon;
			break;
		}
	case meloader::IDENTOBJ_LAYERHEX1:
		fin.read((char*)&m_LayerHex1, sizeof(meloader::LayerHex1));
		iDataWidth = m_LayerHex1.iLayerWidth;
		break;
	case meloader::IDENTOBJ_LAYERHEX2:
		fin.read((char*)&m_LayerHex2, sizeof(meloader::LayerHex2));
		iDataWidth = m_LayerHex2.iLayerWidth;
		break;
	case meloader::IDENTOBJ_LAYERISO1:
		fin.read((char*)&m_LayerIso1, sizeof(meloader::LayerIso1));
		iDataWidth = m_LayerIso1.iLayerWidth;
		break;
	case meloader::IDENTOBJ_LAYERISO2:
		fin.read((char*)&m_LayerIso2, sizeof(meloader::LayerIso2));
		iDataWidth = m_LayerIso2.iLayerWidth;
		break;
	case meloader::IDENTOBJ_LAYERRECTANGULAR:
		fin.read((char*)&m_LayerRect, sizeof(meloader::LayerRectangular));
		iDataWidth = m_LayerRect.iLayerWidth;
		break;
	};

	// Prüfen ob ein Subobject vorliegt
	char cConstant;
	fin.read(&cConstant, 1);

	if(	cConstant == meloader::IDENTOBJ_BASEDATA ||
		cConstant == meloader::IDENTOBJ_DATALAYER ||
		cConstant == meloader::IDENTOBJ_METADATA)
	{
		m_pSubObject = new CSubData();
	}

	// Es sind Daten vorhanden
	if(m_pSubObject)
	{
		meloader::result res = m_pSubObject->Initialize(fin.tellg(), pFile, iDataWidth);
		if(res != meloader::RES_OK) { return res; }
	}

	// Prüfen ob auch die richtigen Subdaten gelesen wurden
	if(m_cIdentifier == meloader::IDENTOBJ_LAYERCLON)
	{
		char cIdent;
		m_pSubObject->Identify(&cIdent);

		if(cIdent != meloader::IDENTOBJ_METADATA)
			return meloader::RES_WRONGSUBDATA;
	}

	return meloader::RES_OK;
}


meloader::result CLayer::Identify(char* pcIdent)
{
#ifdef _DEBUG
	if(!pcIdent)  { return meloader::RES_WRONGPARAMETER; }
#endif

	*pcIdent = m_cIdentifier;
	return meloader::RES_OK;
}


meloader::result CLayer::GetData(const char cIdent,
								 void* pvData,
								 const bool bData)
{
#ifdef _DEBUG
	if(!pvData) { return meloader::RES_WRONGPARAMETER; }
#endif

	switch(cIdent)
	{
	case meloader::IDENTOBJ_LAYERHEX1:
		memcpy(pvData, &m_LayerHex1, sizeof(meloader::LayerHex1));
		break;
	case meloader::IDENTOBJ_LAYERHEX2:
		memcpy(pvData, &m_LayerHex2, sizeof(meloader::LayerHex2));
		break;
	case meloader::IDENTOBJ_LAYERISO1:
		memcpy(pvData, &m_LayerIso1, sizeof(meloader::LayerIso1));
		break;
	case meloader::IDENTOBJ_LAYERISO2:
		memcpy(pvData, &m_LayerIso2, sizeof(meloader::LayerIso2));
		break;
	case meloader::IDENTOBJ_LAYERRECTANGULAR:
		memcpy(pvData, &m_LayerRect, sizeof(meloader::LayerRectangular));
		break;
	default:
		{
			if(m_pSubObject)
			{
				meloader::result res = m_pSubObject->GetData(cIdent, pvData, bData);
				if(res != meloader::RES_OK) { return res; }
			}
			else
				return meloader::RES_WRONGIDENTIFIER;
		}
	};

	return meloader::RES_OK;
}


meloader::result CLayer::Have(const char cIdent,
							  int* piHave)
{
#ifdef _DEBUG
	if(!piHave) { return meloader::RES_WRONGPARAMETER; }
#endif

	*piHave = false;

	char cSubIdent;
	m_pSubObject->Identify(&cSubIdent);

	if(cIdent == cSubIdent)	*piHave = true;
	else return meloader::RES_WRONGIDENTIFIER;

	return meloader::RES_OK;
}


meloader::result CLayer::IdentifyName(const char* szName)
{
#ifdef _DEBUG
	if(!szName) { return meloader::RES_WRONGPARAMETER; }
#endif

	std::string strName(szName);

	switch(m_cIdentifier)
	{
	case meloader::IDENTOBJ_LAYERHEX1:
		if(strName != m_LayerHex1.szName)	return meloader::RES_WRONGNAME;
	case meloader::IDENTOBJ_LAYERHEX2:
		if(strName != m_LayerHex2.szName)	return meloader::RES_WRONGNAME;
	case meloader::IDENTOBJ_LAYERISO1:
		if(strName != m_LayerIso1.szName)	return meloader::RES_WRONGNAME;
	case meloader::IDENTOBJ_LAYERISO2:
		if(strName != m_LayerIso2.szName)	return meloader::RES_WRONGNAME;
	case meloader::IDENTOBJ_LAYERRECTANGULAR:
		if(strName != m_LayerRect.szName)	return meloader::RES_WRONGNAME;
	};

	return meloader::RES_OK;
}


meloader::result CLayer::QuerySubData(const char cIdent,
									  meloader::ISubData** ppSubData)
{
#ifdef _DEBUG
	if(!ppSubData) { return meloader::RES_WRONGPARAMETER; }
	if(!this->m_pSubObject) { return meloader::RES_NOSUBDATASET; }
	
	char sub;
	m_pSubObject->Identify(&sub);
	if(sub != cIdent) { return meloader::RES_WRONGIDENTIFIER; }
#endif

	*ppSubData = m_pSubObject;
	return meloader::RES_OK;
}