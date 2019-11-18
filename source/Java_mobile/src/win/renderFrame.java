package win;

import javax.swing.*;
import java.awt.*;

import java.awt.image.VolatileImage;
import java.awt.image.FilteredImageSource;

/**
 * <p>Title: map renderer</p>
 * <p>Description: map renderer for Mapeditor</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: dennisr</p>
 * @author denman
 * @version 1.0
 */

public class renderFrame extends JFrame {
  BorderLayout borderLayout1 = new BorderLayout();
  public static gameCanvas gfx = null;


  public renderFrame() {
    this.setSize(800,600);
    this.setDefaultCloseOperation(this.EXIT_ON_CLOSE);
    this.setResizable(false);
    this.getContentPane().setLayout(borderLayout1);
    this.show();
    this.createBufferStrategy(2);

    try {
      jbInit();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }

  void jbInit() throws Exception {
      gfx = new gameCanvas((short)60);
      gfx.setBackground(Color.BLACK);
      gfx.setSize(this.getSize());
      this.getContentPane().add(gfx);

  }




}
