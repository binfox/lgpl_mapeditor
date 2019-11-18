package win;

import java.awt.image.RGBImageFilter;

/**
 * <p>Title: map renderer</p>
 * <p>Description: map renderer for Mapeditor</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: dennisr</p>
 * @author denman
 * @version 1.0
 */

class TransparentFilter extends RGBImageFilter {
    private int rgb;  // transparente Farbe

    public TransparentFilter(int rgb) {
      // Speichern der transparenten Farbe
      this.rgb = rgb;
      // keine positionsabhängige Filterung
      canFilterIndexColorModel = false;
    }

    public int filterRGB(int x, int y, int rgb) {
      // Ist das zu filternde Pixel
      // gleich der transparenten Farbe?
      if (rgb == this.rgb)
        // Wenn ja, Alpha-Wert des Pixels 0 setzen
        rgb &=  0x00FFFFFF;
      return rgb;
    }
  }
