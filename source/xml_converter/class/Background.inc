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

//   <background visible='1' file='http://myurl/image.png'>
//     <parallax x='5' y='56' />
//     <position x='46' y='634' />
//     <size x='435' y='453' />
//     <fill mode='2' r='45' g='64' b='0' />
//   </background>
*/

class Background
{
	var $_isVisible;
	var $_file;
	var $_parallaxx;
	var $_parallaxy;
	var $_positionx;
	var $_positiony;
	var $_sizex;
	var $_sizey;
	var $_fillmode;
	var $_fillr;
	var $_fillg;
	var $_fillb;
	
	function setFile($s) { $this->_file = $s; }
	function setVisible($b) { $this->_isVisible = $b; }
	function setParallax($x, $y) { $this->_parallaxx = $x; $this->_parallaxy = $y; }
	function setPosition($x, $y) { $this->_positionx = $x; $this->_positiony = $y; }
	function setSize($x, $y) { $this->_sizex = $x; $this->_sizey = $y; }
	function setFill($mode, $r, $g, $b) { 
		$this->_fillmode = $mode;
		$this->_fillr = $r;
		$this->_fillg = $g;
		$this->_fillb = $b;
	}

	function getFile() { return $this->_file; }


	function XMLize($pretab=2, $BR="<br>", $TAB="&nbsp;&nbsp;")
	{
		$PRE = "";
		for($i=0; $i<$pretab; $i++)
			$PRE .= $TAB;
		
		$r = "$PRE<background visible='".($this->_isVisible?"1":"0")."' file='".$this->_file."'>$BR";
		$r .= "$PRE$TAB<parallax x='".$this->_parallaxx."' y='".$this->_parallaxy."' />$BR";
		$r .= "$PRE$TAB<position x='".$this->_positionx."' y='".$this->_positiony."' />$BR";
		$r .= "$PRE$TAB<size x='".$this->_sizex."' y='".$this->_sizey."' />$BR";
		$r .= "$PRE$TAB<fill mode='".$this->_fillmode."' r='".$this->_fillr."' g='".$this->_fillg."' b='".$this->_fillb."' />$BR";
		$r .= "$TAB</background>$BR";
		return $r;
	}
	
	function parseBinaryBlock($data, $BR="<br>", $TAB="&nbsp;&nbsp;")
	{
		$t = unpack("Cisvisible/a12image/C2parallaxpos/V2pos/V2mapsize/Cfillmode/C3fillcolor", $data);
		
		$r = "";
		$r .= $TAB."Visible: ${t['isvisible']}$BR";
		$r .= $TAB."Background: <b>${t['image']}</b>$BR";
		$r .= $TAB."Parallax position: ${t['parallaxpos1']},${t['parallaxpos2']}$BR";	
		$r .= $TAB."Background position: ${t['pos1']},${t['pos2']}$BR";
		$r .= $TAB."Map size: ${t['mapsize1']}x${t['mapsize2']}$BR";
		$r .= $TAB."Fill mode: ";
		switch($t['fillmode']) {
			case 0: $r .= "without"; break;
			case 1: $r .= "color"; break;
			case 2: $r .= "image"; break;
			case 3: $r .= "masked image"; break;
			default: $r .= "ERROR!"; break;
		}
		$r .= $BR;
		$r .= $TAB."Fill colorRGB: ${t['fillcolor1']}/${t['fillcolor2']}/${t['fillcolor3']}$BR";

		$this->setVisible($t['isvisible']);
		$this->setFile($t['image']);
		$this->setParallax($t['parallaxpos1'], $t['parallaxpos2']);
		$this->setPosition($t['pos1'], $t['pos2']);
		$this->setSize($t['mapsize1'], $t['mapsize2']);
		$this->setFill($t['fillmode'], $t['fillcolor1'], $t['fillcolor2'], $t['fillcolor3']);

		return $r;
	}
}
?>