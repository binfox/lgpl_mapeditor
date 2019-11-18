<?
// map2xml.php v1.0 - Converts MAPEDITOR's .map files to .xml files
//                    Classes to parse .map files in PHP are also shipped
// -----------------------------------------------------------------------

$ROOT = "./class";
$CIRCUITDIR = "./data";

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

require_once("$ROOT/Map.inc");

$BR = "<br>";
$HR = "---";
$TAB = "&nbsp;|---";

$maps = array();
if ($handle = opendir($CIRCUITDIR)) {
    while (false !== ($file = readdir($handle))) {
		if (substr($file, -4) == ".map")
			array_push($maps, $file);
    }
    closedir($handle);
}

$s = "";

echo "<p>map2xml.php v1.0 - Converts MAPEDITOR's .map files to .xml$BR";
echo "Free software (LGPL) - <i>Copyleft 2004, 2005 Alexandre v't Westende</i></p>";


foreach($maps as $file)
	if (isset($_GET['file']) && $_GET['file'] == $file)	$s .= "<option selected>$file</option>"; else $s .= "<option>$file</option>";

echo "<a name=top /><form name=form1 action=".$_SERVER['SCRIPT_NAME'].">";
echo "Map to parse: ";
echo "<select name=file>$s</select>";
if ($s == "") 
{
	echo "<p><span style='color: red;'>Please place some .map files in the $CIRCUITDIR/ directory!</span><br>(this is a converter and there is nothing to convert for the time being...)</p>";
}


echo "<input type=submit name=submit value=ok>";

echo " [ <input type=checkbox name=showxml ".(isset($_GET['showxml']) ? "checked" : "")."> ";
if (isset($_GET['showxml'])) echo "<a href=#xml>Show XML?</a> ]";
else echo "Show XML? ]";

echo " [ <input type=checkbox name=showparsing ".(isset($_GET['showparsing']) ? "checked" : "")."> ";
if (isset($_GET['showparsing'])) echo "<a href=#parsing>Show parsing?</a> ]";
else echo "Show parsing? ]";

echo "</form><hr>";
if (!isset($_GET['file'])) die;

// -------------------------------------------------------------------------------

$r = "File being read: ".$_GET['file']."$BR$BR";

$m = new Map();
$r .= $m->parseFile($CIRCUITDIR.'/'.$_GET['file'], $BR, $TAB, $HR);

if (isset($_GET['showparsing'])) echo "<a name=parsing /><p>".$r;
if (isset($_GET['showxml'])) 
	echo "<a name=xml /><h2>XML file to copy/paste</h2><a href=#top>Goto top (upper in the page)</a><p>".$m->XMLize()."</p>";

echo "</body></html>";
?>