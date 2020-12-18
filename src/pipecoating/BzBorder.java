package pipecoating;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.border.AbstractBorder;


public class BzBorder extends AbstractBorder {
  public static final int BEVEL_NONE = 0;
  public static final int BEVEL_RAISED = 1;
  public static final int BEVEL_LOWERED = 2;
  public static final int BEVEL_SPACE = 3;

  private int mInnerBorderWidth = 0;
  private int mOuterBorderWidth = 0;
  private Color mInnerBorderColor = Color.black;
  private Color mOuterBorderColor = Color.black;

  private int mInnerBevelType = BEVEL_NONE;
  private int mOuterBevelType = BEVEL_NONE;
  private int mInnerBevelWidth = 1;
  private int mOuterBevelWidth = 1;

  private Color mHighlightColor = null;
  private Color mShadowColor = null;

  
  public int getInnerBorderWidth() {
    return mInnerBorderWidth;
  }


  public void setInnerBorderWidth(int width) {
    mInnerBorderWidth = width;
  }


  public int getOuterBorderWidth() {
    return mOuterBorderWidth;
  }


  public void setOuterBorderWidth(int width) {
    mOuterBorderWidth = width;
  }


  public Color getInnerBorderColor() {
    return mInnerBorderColor;
  }


  public void setInnerBorderColor(Color color) {
    mInnerBorderColor = color;
  }


  public Color getOuterBorderColor() {
    return mOuterBorderColor;
  }


  public void setOuterBorderColor(Color color) {
    mOuterBorderColor = color;
  }


  public int getInnerBevelType() {
    return mInnerBevelType;
  }


  public void setInnerBevelType(int type) {
    mInnerBevelType = type;
  }


  public int getOuterBevelType() {
    return mOuterBevelType;
  }


  public void setOuterBevelType(int type) {
    mOuterBevelType = type;
  }


  public int getInnerBevelWidth() {
    return mInnerBevelWidth;
  }


  public void setInnerBevelWidth(int width) {
    mInnerBevelWidth = width;
  }


  public int getOuterBevelWidth() {
    return mOuterBevelWidth;
  }


  public void setOuterBevelWidth(int width) {
    mOuterBevelWidth = width;
  }


  public Color getHighlightColor() {
    return mHighlightColor;
  }


  public void setHighlightColor(Color color) {
    mHighlightColor = color;
  }


  public Color getShadowColor() {
    return mShadowColor;
  }


  public void setShadowColor(Color color) {
    mShadowColor = color;
  }


  private int getWidth() {
    int width = 0;

    if(mOuterBorderWidth > 0) {
      width += mOuterBorderWidth;
    }

    if(mOuterBevelType != BEVEL_NONE) {
      if(mOuterBevelWidth > 0) {
        width += mOuterBevelWidth;
      }
    }

    if(mInnerBevelType != BEVEL_NONE) {
      if(mInnerBevelWidth > 0) {
        width += mInnerBevelWidth;
      }
    }

    if(mInnerBorderWidth > 0) {
      width += mInnerBorderWidth;
    }

    return width;
  }


  public Insets getBorderInsets(Component c) {
    final int width = getWidth();
    return new Insets(width, width, width, width);
  }


  public Insets getBorderInsets(Component c, Insets insets) {
    final Insets currInsets = getBorderInsets(c);

    insets.left = currInsets.left;
    insets.top = currInsets.top;
    insets.right = currInsets.right;
    insets.bottom = currInsets.bottom;

    return currInsets;
  }


  public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
    Color highlightColor = this.mHighlightColor;
    Color shadowColor = this.mShadowColor;

    if(highlightColor == null) {
      highlightColor = c.getBackground().brighter();
    }
    if(shadowColor == null) {
      shadowColor = c.getBackground().darker();
    }

    if(mOuterBorderWidth > 0) {
      draw3DFrame(g, x, y, w, h, mOuterBorderColor, mOuterBorderColor,
                  mOuterBorderWidth);

      x += mOuterBorderWidth; w -= 2*mOuterBorderWidth;
      y += mOuterBorderWidth; h -= 2*mOuterBorderWidth;
    }

    if(mOuterBevelType != BEVEL_NONE) {
      if(mOuterBevelWidth > 0) {
        boolean f = true;

        Color color1 = null;
        Color color2 = null;

        switch(mOuterBevelType) {
          case BEVEL_RAISED:
            color1 = highlightColor;
            color2 = shadowColor;
            break;

          case BEVEL_LOWERED:
            color1 = shadowColor;
            color2 = highlightColor;
            break;

          default:
            f = false;
        }

        if(f) {
          draw3DFrame(g, x, y, w, h, color1, color2, mOuterBevelWidth);
        }

        x += mOuterBevelWidth; w -= 2*mOuterBevelWidth;
        y += mOuterBevelWidth; h -= 2*mOuterBevelWidth;
      }
    }

    if(mInnerBevelType != BEVEL_NONE) {
      if(mInnerBevelWidth > 0) {
        boolean f = true;

        Color color1 = null;
        Color color2 = null;

        switch(mInnerBevelType) {
          case BEVEL_RAISED:
            color1 = highlightColor;
            color2 = shadowColor;
            break;

          case BEVEL_LOWERED:
            color1 = shadowColor;
            color2 = highlightColor;
            break;

          default:
            f = false;
        }

        if(f) {
          draw3DFrame(g, x, y, w, h, color1, color2, mInnerBevelWidth);
        }

        x += mInnerBevelWidth; w -= 2*mInnerBevelWidth;
        y += mInnerBevelWidth; h -= 2*mInnerBevelWidth;
      }
    }

    if(mInnerBorderWidth > 0)
      draw3DFrame(g, x, y, w, h, mInnerBorderColor, mInnerBorderColor,
                  mInnerBorderWidth);
  }


  private void draw3DFrame(Graphics g, int x, int y, int w, int h,
                           Color color1, Color color2, int width) {
    for(int i=0; i<width; i++) {        
      if(w <= 0 || h <= 0) {
        break;
      }

      g.setColor(color1);
      g.drawLine(x, y+h-1, x, y);
      g.drawLine(x, y, x+w-1, y);
      g.setColor(color2);
      g.drawLine(x+w-1, y, x+w-1, y+h-1);
      g.drawLine(x+w-1, y+h-1, x, y+h-1);

      x++; w -= 2;
      y++; h -= 2;
    }
  }
}
