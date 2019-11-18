package win;

import java.io.File;
import javax.swing.filechooser.*;

/**
 * <p>Title: map renderer</p>
 *
 * <p>Description: map renderer for Mapeditor</p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: dennisr</p>
 * @author denman
 * @version 1.0
 */
public class filter extends FileFilter{
  /**
   * accept
   *
   * @param f File
   * @return boolean
   */
  public boolean accept(File f)
    {
       if (f.isDirectory()) {
           return true;
       }


       String extension = getExtension(f);
       if (extension != null) {
           if ( extension.equals("map") || extension.equals("res") )
           {
             return true;
           }
             else
           {
               return false;
           }
         }


       return false;
   }

   public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }



  /**
   * getDescription
   *
   * @return String
   */
  public String getDescription() {
    return "map files";
  }

}
