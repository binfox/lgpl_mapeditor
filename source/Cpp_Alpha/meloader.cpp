
#pragma pack(1)

#include "meloader.h"
#include <fstream>
#include <string>
#include "Background.h"
#include "TileSet.h"
#include "Layer.h"
#include "Object.h"
#include "sha1.h"


template<class I> void SafeRelease(I*& pInterface)
{
	if(pInterface)
	{
		pInterface->Release();
		pInterface = 0;
	}
}


meloader::MapFile::MapFile() : m_pBackground(0)
{
}


meloader::MapFile::~MapFile()
{
	Close();
}



meloader::result meloader::MapFile::Open(const char* szMap,
										 const char* szPwd)
{
#ifdef _DEBUG
	// Prüfen der Parameter
	if(!szMap) { return meloader::RES_WRONGPARAMETER; }
#endif

	// Öffnen der Datei
	std::fstream fin(szMap, std::ios::binary | std::ios::in);
	if(!fin.is_open()) { return meloader::RES_NOTOPENFILE; }

	// Header einlesen
	fin.read((char*)&m_Header, sizeof(meloader::FileHeader));

	// Prüfen ob es eine korrekte Datei ist
	if(	m_Header.cUMF[0] != 'U' ||
		m_Header.cUMF[1] != 'M' ||
		m_Header.cUMF[2] != 'F')
		return meloader::RES_NOTAUMFFILE;

	// Passwort überprüfen, wenn verschlüsselt
	if(m_Header.cFlags & 0x80)
	{
#ifdef _DEBUG
		if(!szPwd) { return meloader::RES_NOPASSWORDSET; }
#endif

		sha1::SHA1Context sha;
		if(sha1::SHA1Input(&sha, szPwd, strlen(szPwd)) != sha1::shaSuccess)
			return meloader::RES_INVALIDSHA1FINGERPRINT;

		unsigned char ucDigest[sha1::SHA1HashSize];
		if(sha1::SHA1Result(&sha, ucDigest) != sha1::shaSuccess)
			return meloader::RES_INVALIDSHA1RESULT;

		// Prüfen ob Passwort stimmt
		for(int dig = 0; dig < sha1::SHA1HashSize; ++dig)
		{
			if(ucDigest[dig] != m_Header.ucFingerPrint[dig])
				return meloader::RES_INVALIDPASSWORD;
		}
	}

	
	// Anzahl der Table-Objekte kalkulieren
	int iTables = m_Header.sNumBlocks;

	long lFileSize = sizeof(meloader::FileHeader) + (iTables * sizeof(meloader::Table));

	// Tableobjekte auslesen
	std::vector<meloader::Table> vTable;
	for(int c = 0; c < iTables; ++c)
	{
		meloader::Table table;
		fin.read((char*)&table, sizeof(meloader::Table));
		vTable.push_back(table);

		lFileSize += table.iSize;
	}


	// Prüfen ob die Datei die richtige größe hat
	long temp = fin.tellg();
	fin.seekg(0, std::ios::end);
	if(lFileSize != fin.tellg())
		return meloader::RES_INVALIDFILESIZE;
	fin.seekg(temp, std::ios::beg);

	m_strFileName = szMap;
	
	// Auslesen der Informationen
	for(int iTable = 0; !fin.eof(); ++iTable)
	{
		char cIdentifier;
		fin.read(&cIdentifier, 1);

		if(fin.eof()) break;

		meloader::result res;
		switch(cIdentifier)
		{
		case meloader::IDENTOBJ_BACKGROUND:
			{
				// Prüfen ob der Hintergrund schon geladen wurde
				if(m_pBackground)
				{
					Close();
					return meloader::RES_SECONDBACKGROUND;
				}

				m_pBackground = new CBackground();
				res = m_pBackground->Initialize(fin.tellg(), this);
				if(res != meloader::RES_OK)
				{
					Close();
					return res;
				}
				break;
			}
		case meloader::IDENTOBJ_LAYERCLON:
		case meloader::IDENTOBJ_LAYERHEX1:
		case meloader::IDENTOBJ_LAYERHEX2:
		case meloader::IDENTOBJ_LAYERISO1:
		case meloader::IDENTOBJ_LAYERISO2:
		case meloader::IDENTOBJ_LAYERRECTANGULAR:
			{
				meloader::ILayer* pNew = new CLayer();
				res = pNew->Initialize(fin.tellg(), this);
				if(res != meloader::RES_OK)
				{
					Close();
					return res;
				}

				m_vLayer.push_back(pNew);
				break;
			}
		case meloader::IDENTOBJ_OBJECTBLOCK:
		case meloader::IDENTOBJ_OBJECTLINE:
		case meloader::IDENTOBJ_OBJECTOVAL:
		case meloader::IDENTOBJ_OBJECTPICTURE:
		case meloader::IDENTOBJ_OBJECTPOINT:
		case meloader::IDENTOBJ_OBJECTRECT:
			{
				meloader::IObject* pNew = new CObject();
				res = pNew->Initialize(fin.tellg(), this);
				if(res != meloader::RES_OK)
				{
					Close();
					return res;
				}

				m_vObject.push_back(pNew);
				break;
			}
		case meloader::IDENTOBJ_TILESET:
			{
				meloader::ITileSet* pNew = new CTileSet();
				res = pNew->Initialize(fin.tellg(), this);
				if(res != meloader::RES_OK)
				{
					Close();
					return res;
				}

				m_vTileSet.push_back(pNew);
				break;
			}
		};

		// Neue Position setzen
		fin.seekg(vTable[iTable].iSize - 1, std::ios::cur);
	}	

	if(iTable != iTables){ return meloader::RES_NOTALLBLOCKSSAVED; }

	return meloader::RES_OK;
}


meloader::result meloader::MapFile::Close()
{
	SafeRelease(m_pBackground);

	std::vector<meloader::ITileSet*>::iterator tileset = m_vTileSet.begin();
	for(; tileset != m_vTileSet.end(); ++tileset)
		SafeRelease(*tileset);

	std::vector<meloader::ILayer*>::iterator layer = m_vLayer.begin();
	for(layer = m_vLayer.begin(); layer != m_vLayer.end(); ++layer)
		SafeRelease(*layer);

	std::vector<meloader::IObject*>::iterator object = m_vObject.begin();
	for(object = m_vObject.begin(); object != m_vObject.end(); ++object)
		SafeRelease(*object);

	m_strFileName.clear();
	return meloader::RES_OK;
}


bool meloader::MapFile::GetSHA1(unsigned char* pucSHA1) const
{
	if((m_Header.cFlags & 0x80) && pucSHA1)
	{
		memcpy(pucSHA1, m_Header.ucFingerPrint, sha1::SHA1HashSize);
		return true;
	}
	
	return false;
}


meloader::result meloader::MapFile::QueryBackground(meloader::IBackground** ppBackground)
{
#ifdef _DEBUG
	if(!ppBackground) { return meloader::RES_WRONGPARAMETER; }
	if(!m_pBackground){ return meloader::RES_NOINTERFACESET; }
#endif

	*ppBackground = m_pBackground;
	m_pBackground->AddReferenz();
	return meloader::RES_OK;
}


meloader::result meloader::MapFile::QueryLayer(meloader::ILayer** ppLayer,
											   const char cIdent,
											   const char* szName)
{
#ifdef _DEBUG
	if(!ppLayer) { return meloader::RES_WRONGPARAMETER; }
	if(!szName)  { return meloader::RES_WRONGPARAMETER; }
#endif

	std::vector<meloader::ILayer*>::iterator layer = m_vLayer.begin();
	for(; layer != m_vLayer.end(); ++layer)
	{
		char cConstant;
		(*layer)->Identify(&cConstant);

		if(cConstant == cIdent)
		{
			if((*layer)->IdentifyName(szName) == meloader::RES_OK)
			{
				*ppLayer = *layer;
				return meloader::RES_OK;
			}
		}
	}

	return meloader::RES_WRONGIDENTIFIER;
}


meloader::result meloader::MapFile::QueryObject(meloader::IObject** ppObject,
												const char cIdent,
												const char* szName)
{
#ifdef _DEBUG
	if(!ppObject) { return meloader::RES_WRONGPARAMETER; }
	if(!szName)  { return meloader::RES_WRONGPARAMETER; }
#endif

	std::vector<meloader::IObject*>::iterator object = m_vObject.begin();
	for(; object != m_vObject.end(); ++object)
	{
		char cConstant;
		(*object)->Identify(&cConstant);

		if(cConstant == cIdent)
		{
			if((*object)->IdentifyName(szName) == meloader::RES_OK)
			{
				*ppObject = *object;
				return meloader::RES_OK;
			}
		}
	}

	return meloader::RES_WRONGIDENTIFIER;
}


meloader::result meloader::MapFile::QueryTileSet(meloader::ITileSet** ppTileSet,
												 const int iNumber)
{
#ifdef _DEBUG
	if(!ppTileSet) { return meloader::RES_WRONGPARAMETER; }

	if((iNumber-1) > m_vTileSet.size())
		return meloader::RES_INVALIDTILESETNUMBER;
#endif

	*ppTileSet = m_vTileSet[iNumber];
	(*ppTileSet)->AddReferenz();
	
	return meloader::RES_OK;
}