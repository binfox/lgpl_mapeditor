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


/*
XML example:

<layer name='02' visible='1' masked='0' shape='rectangular' tilesetnumber='1'>
  <parallax x='5' y='56' />
  <position x='46' y='634' />
  <size x='435' y='453' />
  <data depth='8' encoding='rle'>9000fe0302010101</data>
</layer>
*/

require_once("BinaryData.inc");

class Layer
{
	var $_name;
	var $_isVisible;
	var $_isMasked;
	var $_shape;
	var $_tilesetNumber;
	var $_parallaxx;
	var $_parallaxy;
	var $_positionx;
	var $_positiony;
	var $_sizex;
	var $_sizey;
	var $_data;
	
	function setName($s) { $this->_name = $s; }
	function setVisible($b) { $this->_isVisible = $b; }
	function setMasked($b) { $this->_isMasked = $b; }
	function setShape($s) { $this->_shape = $s; }
	function setTilesetNumber($n) { $this->_tilesetNumber = $n; }
	function setParallax($x, $y) { $this->_parallaxx = $x; $this->_parallaxy = $y; }
	function setPosition($x, $y) { $this->_positionx = $x; $this->_positiony = $y; }
	function setSize($x, $y) { $this->_sizex = $x; $this->_sizey = $y; }
	function setData(&$data, $depth) { 
		$this->_data = new BinaryData();
		$this->_data->setRawData($data, $depth);
		$this->_data->setRLEEnabled(true);
	}

	function getName() { return $this->_name; }


	function XMLize($pretab=2, $BR="<br>", $TAB="&nbsp;&nbsp;")
	{
		$PRE = "";
		for($i=0; $i<$pretab; $i++)
			$PRE .= $TAB;
		
		$r = "$PRE<layer name='".$this->_name."' visible='".($this->_isVisible?"1":"0").
			 "' masked='".($this->_isMasked?"1":"0")."' shape='".$this->_shape."' tilesetnumber='".$this->_tilesetNumber."'>$BR";
		$r .= "$PRE$TAB<parallax x='".$this->_parallaxx."' y='".$this->_parallaxy."' />$BR";
		$r .= "$PRE$TAB<position x='".$this->_positionx."' y='".$this->_positiony."' />$BR";
		$r .= "$PRE$TAB<size x='".$this->_sizex."' y='".$this->_sizey."' />$BR";
		$encoding = "";									// this var will be used as a return
		$data = $this->_data->getAsString($encoding);	// here ($encoding is a referenced parameter which will be returned)
		$r .= "$PRE$TAB<data depth='".$this->_data->getDepth()."' encoding='$encoding' data='$data' />$BR";
		$r .= "$TAB</layer>$BR";
		return $r;
	}
	
	
	function parseBinaryBlock($data, $BR="<br>", $TAB="&nbsp;&nbsp;")
	{
		$t = unpack("Cisvisible/a12name/C2parallaxpos/V2pos/V2size/vtileset/Cmasked", $data);
		
		$r = "";
		$r .= $TAB."Visible: ${t['isvisible']}$BR";
		$r .= $TAB."Name: ${t['name']}$BR";
		$r .= $TAB."Parallax position: ${t['parallaxpos1']},${t['parallaxpos2']}$BR";	
		$r .= $TAB."Position: ${t['pos1']},${t['pos2']}$BR";
		$r .= $TAB."Size: <b>${t['size1']}x${t['size2']}</b>$BR";
		$r .= $TAB."Tileset number: ${t['tileset']}$BR";
		$r .= $TAB."Masked: ${t['masked']}$BR";

		$this->setShape("rectangular");
		$this->setVisible($t['isvisible']);
		$this->setName($t['name']);
		$this->setParallax($t['parallaxpos1'], $t['parallaxpos2']);
		$this->setPosition($t['pos1'], $t['pos2']);
		$this->setSize($t['size1'], $t['size2']);
		$this->setTilesetNumber($t['tileset']);
		$this->setMasked($t['masked']);

		return $r;
	}
	
	
	function parseBinaryBasedataBlock($data, $BR="<br>", $TAB="&nbsp;&nbsp;")
	{
		$t = unpack("Cdepth/Vlength/vreserved/A*data", $data);
		
		$r = "";
		$r .= $TAB."Depth: ${t['depth']}$BR";
		$r .= $TAB."Length: ${t['length']}$BR";
		$this->setData($t['data'], $t['depth']);	

		return $r;
	}	
}
?>