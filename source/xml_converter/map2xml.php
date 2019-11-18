<?
// map2xml.php v1.0 - Converts MAPEDITOR's .map files to .xml files
//                    Classes to parse .map files in PHP are also shipped
//                    Interactive version (upload .map to server, download .xml)
// -----------------------------------------------------------------------

$ROOT = "./class";

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


if (!isset($_REQUEST['showparsing']) && !isset($_REQUEST['showxml'])) $_REQUEST['showxml'] = true;

$s = "";

echo "<p>map2xml.php v1.1 - Converts MAPEDITOR's .map files to .xml$BR";
echo "Free software (LGPL) - <i>Copyleft 2004, 2005 Alexandre v't Westende</i></p>";

echo "<a name=top /><form name=form1 action=".$_SERVER['SCRIPT_NAME']." method=post enctype=multipart/form-data>";
echo "Upload .map to convert: <input type=file name=file maxsize=128'>";
echo "<input type=hidden name=MAX_FILE_SIZE value=150000>";
echo "<input type=submit name=submit value=ok>";

echo " [ <input type=checkbox name=showxml ".(isset($_REQUEST['showxml']) ? "checked" : "")."> ";
if (isset($_REQUEST['showxml'])) echo "<a href=#xml>Show XML?</a> ]";
else echo "Show XML? ]";

echo " [ <input type=checkbox name=showparsing ".(isset($_REQUEST['showparsing']) ? "checked" : "")."> ";
if (isset($_REQUEST['showparsing'])) echo "<a href=#parsing>Show parsing?</a> ]";
else echo "Show parsing? ]";

echo "</form><hr>";
//if (!isset($_REQUEST['file'])) die;

// -------------------------------------------------------------------------------
if (!isset($_FILES['file']['tmp_name']))
{
	echo "error: Please choose a .map to convert to xml";
	die;
}
if (!is_uploaded_file($_FILES['file']['tmp_name']))
{
	
	echo "Error: file ".$_FILES['file']['name']." was not uploaded :(<br>Reason: ";
	switch ($_FILES['file']['error'])
	{
		case 1:
			echo "file size exceeds PHP directive <i>upload_max_filesize</i>, please tell the webmaster or administrator to check php.ini";
			break;
		case 2:
			echo "file size exceeds ".$_REQUEST['MAX_FILE_SIZE']." bytes (your map is made of ".$_FILES['file']['name']." bytes)";
			break;
		case 3:
			echo "partial upload. Please try again!";
			break;
		case 4:
			echo "no file was uploaded. Please try again!";
			break;
		default:
			echo "unknown reason :/ Please contact the webmaster or administrator";
			break;
	}
	die;
}

$m = new Map();
$r = $m->parseFile($_FILES['file']['tmp_name'], $BR, $TAB, $HR);
if (!$m->wasParsingOK()) { 
	echo "Error: the file does not seems to be a valid map file<br>"; 
}

echo "File being read: ".$_FILES['file']['name']."$BR$BR";

if (isset($_REQUEST['showparsing']) || (!$m->wasParsingOK())) echo "<a name=parsing /><p>".$r;
if (isset($_REQUEST['showxml']) && ($m->wasParsingOK())) 
	echo "<a name=xml /><h2>XML file to copy/paste</h2><a href=#top>Goto top (upper in the page)</a><p>".$m->XMLize()."</p>";

echo "</body></html>";
?>