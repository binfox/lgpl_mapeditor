<?
// -----------------------------------------------------------------------
// map2xml.php - Converts MAPEDITOR's .map files to .xml files
// Classes to parse .map files in PHP are also shipped
//
// Copyright (C) 2004, 2005 Alexandre v't Westende
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but without any garanty; without even the implied warranty of
// merchantability or fitness for a particular purpose.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this file; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
// -----------------------------------------------------------------------




require_once("Layer.inc");
require_once("BinaryData.inc");
require_once("Background.inc");
require_once("Tileset.inc");

// <mapeditordata version='1.2' proversion='0' encrypted='0'>
//   <background visible='1' file='http://myurl/image.png'>
//     <parallax x='5' y='56' />
//     <position x='46' y='634' />
//     <size x='435' y='453' />
//     <fill mode='2' r='45' g='64' b='0' />
//   </background>
//   <tileset file='http://myurl/tiles1.png' yfactor='0' framecount='7'>
//     <mask r='255' g='0' b='255' />
//     <framesize x='16' y='16' />
//     <animation framecount='2'>
//       ..
//     </animation>
//   </tileset>
//
// 	 <layer name='01' visible='1' masked='0' shape='rectangular' tilesetnumber='0'>
//     <parallax x='5' y='56' />
//     <position x='46' y='634' />
//     <size x='435' y='453' />
//	   <data depth='8' codage='plain'>050d6efa6f050d6efa6f050d6efa6f050d6efa6f050d6efa6f..050d6efa6f050d6efa6f</data>
//     <metadata isbinary='0'>This is my metadata for the layer 01</metadata>
//   </layer>
// 	 <layer name='02' visible='1' masked='0' shape='rectangular' tilesetnumber='1'>
//     <parallax x='5' y='56' />
//     <position x='46' y='634' />
//     <size x='435' y='453' />
//	   <data depth='8' codage='rle'>9000fe0302010101</data>
//   </layer>
// 	 <object name='slantline' visible='0' type='line'>
//     <parallax x='5' y='56' />
//     <startposition x='0' y='0' />
//     <endposition x='46' y='634' />
//     <metadata isbinary='0'>This is an invisible line</metadata>
//   </layer>
// </mapeditordata>
//
// Overhead is roughly 1000 bytes, plus hexadecimal representation of the data, which can be RLE encoded if needed.

class Map
{
	var $_xsize;
	var $_ysize;
	var $_idcircuit;
	var $_name;
	var $_mapeditorversion;
	var $_mapeditorisproversion;
	
	var $_arrLayers;
	var $_arrTilesets;
	var $_background;

	var $_arrTiling;
	var $_arrArrows;
	var $_arrMeta;
	var $_success;

	function Map() { $this->_arrLayers = array(); $this->_arrTilesets = array(); $this->_success = false;}
	function &addLayer(&$o) { $this->_arrLayers[$o->getName()] = $o; return $this->_arrLayers[$o->getName()]; }
	function &addTileset(&$o) { $this->_arrTilesets[$o->_file] = $o; return $this->_arrTilesets[$o->_file]; }
	function setName($name) { $this->_name = $name; }
	function setSize($x,$y) { $this->_xsize = $x; $this->_ysize = $y; }
	function setCircuitID($idcircuit) { $this->_idcircuit = $idcircuit; }
	function setMapEditorVersion($version) { $this->_mapeditorversion = $version; }
	function setMapEditorIsProVersion($b) { $this->_mapeditorisproversion = $b; }

	function wasParsingOK() { return $this->_success; }

	function parseFile($file, $BR="<br>", $TAB="&nbsp;&nbsp;", $HR="<hr>") 
	{
		$f = fopen($file, "rb");
		$ret = "";
		
		// ------------------------------------------------------------
		// HEADER
		// UMF
		$s = fread($f, 3);
		$ret .= "Signature: $s...";
		if ($s == "UMF") $ret .= "OK$BR";
		else {
			$ret .= "Error!$BR";
			$this->_success = false;
			return $ret;
		}
		
		// Version
		$c = fgetc($f);
		$ret .= "Version: ";
		if (($c & 1) == 1) {
			$ret .= "1.0-1.1$BR";
			$this->setMapEditorVersion("1.1");	
		}
		else {
			$ret .= "1.2+$BR";
			$this->setMapEditorVersion("1.2");
		}
		
		// Counters
		$ret .= "${HR}Counters$BR";
		fseek($f, 28);
		$nbBlocks 	= array_pop(unpack("v", fread($f, 2)));
		$nbLayers	= array_pop(unpack("v", fread($f, 2)));
		$nbImage	= array_pop(unpack("v", fread($f, 2)));
		$nbGeoObjects	= array_pop(unpack("v", fread($f, 2)));
		$nbTilesets	= array_pop(unpack("v", fread($f, 2)));
		$nbAnims	= array_pop(unpack("v", fread($f, 2)));
		$nbBaseDatas	= array_pop(unpack("v", fread($f, 2)));
		$nbDataLayers	= array_pop(unpack("v", fread($f, 2)));
		$nbMetaDatas	= array_pop(unpack("v", fread($f, 2)));
		
		$ret .= "Number of blocks: $nbBlocks$BR";
		$ret .= "Number of layers: $nbLayers$BR";
		$ret .= "Number of images: $nbImage$BR";
		$ret .= "Number of geometrical objects: $nbGeoObjects$BR";
		$ret .= "Number of tilesets: $nbTilesets$BR";
		$ret .= "Number of anims: $nbAnims$BR";
		$ret .= "Number of base data: $nbBaseDatas$BR";
		$ret .= "Number of data layers: $nbDataLayers$BR";
		$ret .= "Number of metadata: $nbMetaDatas$BR";
		$ret .= $BR;
		
		// ---------------------------------------------------------------
		// LENGTHS TABLE
		fseek($f, 64);
		$ret .= "${HR}Blocks size (bytes)$BR";
		$lg = array();
		for($i = 1; $i <= $nbBlocks; $i++)
		{
			$lg[$i] = array_pop(unpack("V", fread($f, 4)));
			$ret .= "Block $i size: ".$lg[$i]."$BR";
		}
		$ret .= $BR;
	
		// ---------------------------------------------------------------
		// BLOCKS
		$layername = "";
		$currentobject = "";
		
		for($i = 1; $i <= $nbBlocks; $i++)
		{
			$c = array_pop(unpack("C", fread($f, 1)));
			$s = fread($f, $lg[$i]-1);
			$ret .= "${HR}Reading block $i: ";
			$buffer="";
			switch($c)
			{
				case 100:
					$type = "Tileset";
					$o = new Tileset();
					$buffer .= $o->parseBinaryBlock($s, $BR, $TAB);
					$currentobject = &$this->addTileset($o);
					break;
				case 101:
					$type = "Animation";
					$o = new Animation();
					$buffer .= $o->parseBinaryBlock($s, $BR, $TAB);
					$currentobject->addAnimation($o);
					break;
				case 105:
					$type = "Default data value";
					break;
				case 0:
					$type = "Background";
					$this->_background = new Background();
					$buffer .= $this->_background->parseBinaryBlock($s, $BR, $TAB);
					break;
				case 1:
					$type = "Layer (rectangular)";
					$o = new Layer();
					$buffer .= $o->parseBinaryBlock($s, $BR, $TAB);				
					$currentobject = &$this->addLayer($o);
					break;
				case 2:
					$type = "Layer (isometric 1)";
					break;
				case 3:
					$type = "Layer (isometric 2)";
					break;
				case 4:
					$type = "Layer (hexagonal 1)";
					break;
				case 5:
					$type = "Layer (hexagonal 2)";
					break;
				case 6:
					$type = "Layer (clone)";
					break;
				case 102:
					$type = "Basedata";				
					$buffer .= $currentobject->parseBinaryBasedataBlock($s, $BR, $TAB);
					break;
				case 103:
					$type = "Datalayer";
					break;
				case 104:
					$type = "Metadata";
					break;
				case 7:
					$type = "Object (image)";
					break;
				case 8:
					$type = "Object (block)";
					break;
				case 9:
					$type = "Object (point)";
					break;
				case 10:
					$type = "Object (line)";
					break;
				case 11:
					$type = "Object (rect)";
					break;
				case 12:
					$type = "Object (oval)";
					break;
				default:
					$ret .= "<b>ERROR! Wrong block type. Quitting.</b>";
					$this->_success = false;
					return $ret;
			}
			$ret .= $type." (".(strlen($s)+1)." bytes)"."$BR";
			$ret .= $buffer.$BR;
		}
		$this->_success = true;
		fclose($f);
		return $ret;
	}

	function XMLize($BR="<br>", $TAB="&nbsp;&nbsp;")
	{
		$r = "<mapeditordata version='".$this->_mapeditorversion."' proversion='".($this->_mapeditorisproversion ? "1" : "0")."' encrypted='0'>$BR";
		if (isset($this->_background)) $r .= $this->_background->XMLize(1, $BR, $TAB);
		foreach($this->_arrTilesets as $o) $r .= $o->XMLize(1, $BR, $TAB);
		foreach($this->_arrLayers as $o) $r .= $o->XMLize(1, $BR, $TAB);

		$r .= "</mapeditordata>$BR";
		$r = str_replace("<", "&lt;", $r);
		$r = str_replace(">", "&gt;", $r);
		$r = str_replace("&lt;br&gt;", "<br>", $r);
		return $r;
	}

}
?>