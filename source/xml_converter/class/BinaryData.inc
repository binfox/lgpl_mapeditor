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

class BinaryData
{
	var $_isRLEEnabled;
	var $_isXMLEnabled;
	
	var $_hexData;		// 00000003020202
	var $_XMLData;		// <tile n='3' v='03' /><tile n='4' v='02'><tile n='5' v='02' /><tile n='6' v='02' />
	var $_RLEData;		// 0300ff030302	
	
	var $_rawData;
	var $_depth;	// 4,8,12 or 16
	
	function setRawData($data, $depth) { $this->_rawData = $data; $this->_depth = $depth; }
	
	function setRLEEnabled($b) { $this->_isRLEEnabled = $b; }
	function setXMLEnabled($b) { $this->_isXMLEnabled = $b; }
	
	function getHexStringSize() { if (!isset($this->_hexData)) $this->getAsHexString(); return strlen($this->_hexData); }
	function getXMLStringSize() { if (!isset($this->_XMLData)) $this->getAsXMLString(); return strlen($this->_XMLData); }
	function getRLEStringSize() { if (!isset($this->_RLEData)) $this->getAsRLEString(); return strlen($this->_RLEData); }
	function getRawDataSize() { if (!isset($this->_rawData)) return 0; else return strlen($this->_rawData); }
	
	function getDepth() { return $this->_depth; }
	function getAsRawData() { return $this->_data; }
	function getAsHexString() { return $this->_hexData = bin2hex($this->_rawData); }
	function getAsXMLString() { return ""; }
	
	function getAsString(&$retmethod)
	{
		$comp = array();
		if (true)                 $comp["plain"] = array("size" => $this->getHexStringSize(), "data" => &$this->_hexData);
		if ($this->_isRLEEnabled) $comp["rle"] = array("size" => $this->getRLEStringSize(), "data" => &$this->_RLEData);
		if ($this->_isXMLEnabled) $comp["xml"] = array("size" => $this->getXMLStringSize(), "data" => &$this->_XMLData);

		$minm = "plain";					// min method
		$minv = $this->getHexStringSize();	// min value

		foreach($comp as $method => $arr)
			if ($arr["size"] < $minv) $minm = $method;

		$retmethod = $minm;
		return $comp[$minm]["data"];
	}
	
	
	function getAsRLEString() 
	{ 
		$MAXCOUNT = 127;				// RLE block max length
		$previousc = "";
		$verbatim = "";
		$ccount = 1;
		$previousc = $this->_rawData{0};
		$firstpassage = true;
		// ----------------------------
		// loop	
		for ($i=1; $i < strlen($this->_rawData); $i++)
		{
			$c = $this->_rawData{$i};
			// if a repetition is detected and $verbatim is not empty we have to unpop
			// one character from it. In this case $ccount has to be increased.
			if ($previousc == $c)
			{
				$ccount++;
				if (strlen($verbatim) > 0)
				{
					$verbatim = substr($verbatim, 0, -1);
					
					// modulo $MAXCOUNT------------
					$l = strlen($verbatim);
					for ($k = 0; $k < floor($l / $MAXCOUNT); $k ++)
						$this->_RLEData .= bin2hex(pack("c", - $MAXCOUNT)).bin2hex( substr($verbatim, $k*$MAXCOUNT, $MAXCOUNT) );
					if ($l%$MAXCOUNT > 0)
						$this->_RLEData .= bin2hex(pack("c", - ( $l%$MAXCOUNT ))).bin2hex( substr($verbatim, $MAXCOUNT*floor($l/$MAXCOUNT), $l%$MAXCOUNT));
					// ----------------------------
					
					$verbatim = "";
				}
			}
			else
			{
				if ($ccount > 1)
				{
					// modulo $MAXCOUNT------------
					$l = $ccount;
					for ($k = 0; $k < floor($l / $MAXCOUNT); $k ++)
						$this->_RLEData .= bin2hex(pack("c", $MAXCOUNT)).bin2hex($previousc);
					if ($l%$MAXCOUNT > 0)
						$this->_RLEData .= bin2hex(pack("c", $l%$MAXCOUNT)).bin2hex($previousc);
					// ----------------------------
				}
				if ($firstpassage) $verbatim = $previousc.$verbatim;
				$verbatim .= $c;
				$ccount=1;
			}
			$previousc = $c;
			$firstpassage = false;
		}
		// ----------------------------
		// ending code
		if ($ccount <= 1) 
		{
			// modulo $MAXCOUNT------------
			$l = strlen($verbatim);
			for ($k = 0; $k < floor($l / $MAXCOUNT); $k ++)
				$this->_RLEData .= bin2hex(pack("c", - $MAXCOUNT)).bin2hex( substr($verbatim, $k*$MAXCOUNT, $MAXCOUNT) );
			if ($l%$MAXCOUNT > 0)
				$this->_RLEData .= bin2hex(pack("c", - ( $l%$MAXCOUNT ))).bin2hex( substr($verbatim, $MAXCOUNT*floor($l/$MAXCOUNT), $l%$MAXCOUNT));
			// ----------------------------
			// OLD$this->_RLEData .= bin2hex(pack("c",(-strlen($verbatim)))).bin2hex($verbatim);
		}
		else 
		{
			// modulo $MAXCOUNT------------
			$l = $ccount;
			for ($k = 0; $k < floor($l / $MAXCOUNT); $k ++)
				$this->_RLEData .= bin2hex(pack("c", $MAXCOUNT)).bin2hex($previousc);
			if ($l%$MAXCOUNT > 0)
				$this->_RLEData .= bin2hex(pack("c", $l%$MAXCOUNT)).bin2hex($previousc);
			// ----------------------------
			//$this->_RLEData .= bin2hex(pack("c",$ccount)).bin2hex($previousc);
		}
		// ----------------------------
		return $this->_RLEData;
	}

	function test()
	{
		$r = "";
		$this->setRLEEnabled(true);
		$raw = pack("H*", "99989701010101ee5461020202aa1211100f");
		$this->setRawData($raw);
		$rle = $this->getAsRLEString();
		$r .= "Raw: ".bin2hex($raw)."<br>";
		$r .= "Rle: $rle<br>";
		return $r;
	}
}
?>