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

//     <animation startframe='1' mode='paused'>
//       <frame tile='2' timems='500' />
//       <frame tile='4' timems='500' />
//       <frame tile='5' timems='500' />
//       <frame tile='1' timems='500' />
//       <frame tile='0' timems='2500' />
//     </animation>
*/

class Animation
{
	var $_startframe;
	var $_mode;
	var $_frames;

	function Animation() { $this->_frames = array(); }
	function addFrame($tile, $timems) { array_push($this->_frames, array("tile" => $tile, "timems" => $timems)); }
	function setStartFrame($n) { $this->_startframe = $n; }
	function setMode($n) 
	{ 
		switch($n) 
		{
			case 1:
				$this->_mode = "paused"; 
				break;
			case 2:
				$this->_mode = "forward";
				break;
			case 3:
				$this->_mode = "backward";
				break;
			default:
				echo "Warning: unknown mode in Animation: $n<br>";
		}
	}
	

	function XMLize($pretab=4, $BR="<br>", $TAB="&nbsp;&nbsp;")
	{
		$PRE = "";
		for($i=0; $i<$pretab; $i++)	$PRE .= $TAB;
		
		$r = "$PRE<animation startframe='".$this->_startframe."' mode='".($this->_mode)."'>$BR";
		foreach($this->_frames as $arr)
			$r .= "$PRE$TAB<frame tile='".$arr["tile"]."' timems='".$arr["timems"]."' />$BR";
		$r .= "$PRE</animation>$BR";
		return $r;
	}
	
	function parseBinaryBlock($data, $BR="<br>", $TAB="&nbsp;&nbsp;")
	{
		$t = unpack("vframecount/vstartframe/Cmode/Cdefaulttilevalue/Vreserved1/vreserved2", $data);

		$r = "";
		$r .= $TAB."FrameCount: ${t['framecount']}$BR";
		$r .= $TAB."StartFrame: ${t['startframe']}$BR";
		$r .= $TAB."Mode: ${t['mode']}$BR";
		$r .= $TAB."DefaultTileValue: ${t['defaulttilevalue']}$BR";
		$r .= $TAB."Reserved1: ${t['reserved1']}$BR";
		$r .= $TAB."Reserved2: ${t['reserved2']}$BR";
		
		$this->setStartFrame($t['startframe']);
		$this->setMode($t['mode']);
		
		$pattern = "";
		for ($i = 0; $i < $t['framecount']; $i++)
			$pattern .= "vtile$i/vtimems$i/";
		
		$pattern = substr($pattern, 0, -1);		// remove trailing /
		$pattern = "vframecount/vstartframe/Cmode/Cdefaulttilevalue/Vreserved1/vreserved2/".$pattern;
		$t2 = unpack($pattern, $data);
		
		for ($i = 0; $i < $t['framecount']; $i++)
		{
			$r .= $TAB.$TAB."Frame($i): tile ".$t2["tile$i"]." ; time ".$t2["timems$i"]." ms$BR";
			$this->addFrame($t2["tile$i"], $t2["timems$i"]);
		}
		return $r;
	}	
}
?>