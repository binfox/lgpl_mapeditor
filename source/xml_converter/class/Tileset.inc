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

//   <tileset file='http://myurl/tiles1.png' yfactor='0' framecount='7'>
//     <mask r='255' g='0' b='255' />
//     <framesize x='16' y='16' />
//     <animation startframe='1' mode='paused'>
//       <frame tile='2' timems='500' />
//       <frame tile='4' timems='500' />
//       <frame tile='5' timems='500' />
//       <frame tile='1' timems='500' />
//       <frame tile='0' timems='2500' />
//     </animation>
//     <animation startframe='1' mode='forward'>
//       <frame tile='1' timems='250' />
//       <frame tile='6' timems='250' />
//     </animation>
//   </tileset>
*/

require_once("Animation.inc");

class Tileset
{
	var $_file;
	var $_yfactor;
	var $_framecount;
	var $_maskr;
	var $_maskg;
	var $_maskb;
	var $_framewidth;
	var $_frameheight;

	var $_arrAnimations;

	function Tileset() { $this->_arrAnimations = array(); }
	function setFile($s) { $this->_file = $s; }
	function setMask($r, $g, $b) { $this->_maskr = $r; $this->_maskg; $this->_maskb; }
	function setFrameSize($x, $y) { $this->_framewidth = $x; $this->_frameheight = $y; }
	function setYFactor($yfactor) { $this->_yfactor = $yfactor; }
	function setFrameCount($n) { $this->_framecount = $n; }

	function &addAnimation(&$o) { array_push($this->_arrAnimations, $o); return $this->_arrAnimations[sizeof($this->_arrAnimations)-1]; }

	function XMLize($pretab=2, $BR="<br>", $TAB="&nbsp;&nbsp;")
	{
		$PRE = "";
		for($i=0; $i<$pretab; $i++)	$PRE .= $TAB;
		
		$r = "$PRE<tileset file='".$this->_file."' yfactor='".($this->_yfactor).
			 "' framecount='".($this->_framecount)."'>$BR";
		$r .= "$PRE$TAB<mask r='".$this->_maskr."' g='".$this->_maskg."' b='".$this->_maskb."' />$BR";
		$r .= "$PRE$TAB<framesize x='".$this->_framewidth."' y='".$this->_frameheight."' />$BR";
		
		foreach($this->_arrAnimations as $a)
			$r .= $a->XMLize($pretab+1, $BR, $TAB);
		
		$r .= "$TAB</tileset>$BR";
		return $r;
	}
	
	
	function parseBinaryBlock($data, $BR="<br>", $TAB="&nbsp;&nbsp;")
	{
		$t = unpack("C3mask/v2framesize/vyfactor/vanimcount/vframecount/a12file", $data);

		$r = "";
		$r .= $TAB."MaskRGB: ${t['mask1']}/${t['mask2']}/${t['mask3']}$BR";
		$r .= $TAB."Tiles dimension: ${t['framesize1']}x${t['framesize2']}$BR";
		$r .= $TAB."Y-factor: ${t['yfactor']}$BR";
		$r .= $TAB."Animations: ${t['animcount']}$BR";
		$r .= $TAB."Tiles: ${t['framecount']}$BR";
		$r .= $TAB."File: ${t['file']}$BR";
		
		$this->setFile($t['file']);
		$this->setMask($t['mask1'], $t['mask2'], $t['mask3']);
		$this->setFrameSize($t['framesize1'], $t['framesize2']);
		$this->setYFactor($t['yfactor']);
		$this->setFrameCount($t['framecount']);
		
		return $r;
	}	
}
?>