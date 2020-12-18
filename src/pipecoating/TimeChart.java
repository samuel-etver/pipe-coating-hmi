package pipecoating;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.font.TextLayout;
import java.util.Vector;
import javax.swing.JPanel;

public class TimeChart extends JPanel {
  private double mMinX = 0;
  private double mMaxX = 100;
  private double mMinY = 0;
  private double mMaxY = 100;
  private Color mCanvasColor = Color.black;
  private int mLeftMargin = 40;
  private int mTopMargin = 4;
  private int mRightMargin = 4;
  private int mBottomMargin = 20;
  private Image mImage = null;
  private Color mGridColor = Color.yellow;
  private boolean mGridEnabled = true;
  public final AxisMarkList xAxisMarks = new AxisMarkList();
  public final AxisMarkList yAxisMarks = new AxisMarkList();
  private Color mAxisMarksColor = Color.yellow;
  private int mAxisMarksLength = 2;
  public final SerieList series = new SerieList();


  public double getMinX() {
    return mMinX;
  }


  public void setMinX(double value) {
    mMinX = value;
  }


  public double getMaxX() {
    return mMaxX;
  }


  public void setMaxX(double value) {
    mMaxX = value;
  }


  public double getMinY() {
    return mMinY;
  }


  public void setMinY(double value) {
    mMinY = value;
  }


  public double getMaxY() {
    return mMaxY;
  }


  public void setMaxY(double value) {
    mMaxY = value;
  }


  public Margins getMargins() {
    return new Margins(mTopMargin, mLeftMargin,
                       mBottomMargin, mRightMargin);
  }


  public void setMargins(Margins margins) {
    mTopMargin = margins.top;
    mLeftMargin = margins.left;
    mBottomMargin = margins.bottom;
    mRightMargin = margins.right;
  }


  public void setMargins(int top, int left, int bottom, int right) {
    mTopMargin = top;
    mLeftMargin = left;
    mBottomMargin = bottom;
    mRightMargin = right;
  }


  public Color getCanvasColor() {
    return mCanvasColor;
  }


  public void setCanvasColor(Color color) {
    mCanvasColor = color;
  }


  public Color getGridColor() {
    return mGridColor;
  }


  public void setGridColor(Color color) {
    mGridColor = color;
  }


  public boolean isGridEnabled() {
    return mGridEnabled;
  }


  public void setGridEnabled(boolean enabled) {
    mGridEnabled = enabled;
  }


  public AxisMarkList getXAxisMarkList() {
    return xAxisMarks;
  }


  public AxisMarkList getYAxisMarkList() {
    return yAxisMarks;
  }


  public Color getAxisMarksColor() {
    return mAxisMarksColor;
  }


  public void setAxisMarksColor(Color color) {
    mAxisMarksColor = color;
  }


  public int getAxisMarksLength() {
    return mAxisMarksLength;
  }


  public void setAxisMarkLength(int length) {
    mAxisMarksLength = length;
  }


  public void paint(Graphics g) {
    createImage();
    makeImage();
    drawImage(g);
    paintBorder(g);
    paintChildren(g);
  }


  private void createImage() {
    final Insets insets = getInsets();

    final int w = getWidth() - insets.left - insets.right;
    final int h = getHeight() - insets.top - insets.bottom;

    creation: {
      if(w <= 0 || h <= 0) {
        mImage = null;
        break creation;
      }

      if(mImage != null) {
        if(mImage.getWidth(null)  == w && 
           mImage.getHeight(null) == h)
          break creation;
      }

      mImage = createImage(w, h);
    }
  }


  private void makeImage() {
    if(mImage != null) {
      final Graphics2D g = (Graphics2D)mImage.getGraphics();

      final int x = 0;
      final int y = 0;
      final int w = mImage.getWidth(null);
      final int h = mImage.getHeight(null);

      drawBackground(g, x, y, w, h);

      final int canvasX = x + mLeftMargin;
      final int canvasY = y + mTopMargin;
      final int canvasW = w - mLeftMargin - mRightMargin;
      final int canvasH = h - mTopMargin - mBottomMargin;

      drawCanvasBackground(g, canvasX, canvasY, canvasW, canvasH);
      drawCanvasFrame(g, canvasX, canvasY, canvasW, canvasH);
      drawGrid(g, canvasX, canvasY, canvasW, canvasH);
      drawSeries(g, canvasX, canvasY, canvasW, canvasH);
      drawAxis(g, canvasX, canvasY, canvasW, canvasH);
    }
  }


  private void drawBackground(Graphics2D g, int x, int y, int w, int h) {
    if(w > 0 && h > 0) {
      g.setBackground(getBackground());
      g.clearRect(x, y, w, h);
    }
  }


  private void drawCanvasBackground(Graphics2D g, int x, int y, int w, int h) {
    if(w > 0 && h > 0) {
      g.setBackground(mCanvasColor);
      g.clearRect(x, y, w, h);
    }
  }


  private void drawCanvasFrame(Graphics2D g, int x, int y, int w, int h) {
    if(w > 0 && h > 0) {
      g.setColor(mGridColor);
      g.drawRect(x, y, w, h);
    }
  }


  private void drawGrid(Graphics2D g, int x, int y, int w, int h) {
    if(!mGridEnabled ||
       w <= 0 || h <= 0 ||
       mMinX >= mMaxX || mMinY >= mMaxY) {
      return;
    }

    g.setColor(mGridColor);

    for(int i=xAxisMarks.size()-1; i>=0; i--) {
      final double value = xAxisMarks.getItem(i).value;
      if(isXVisible(value))  {
        final int pos = x + xLPtoDP(value, w);
        g.drawLine(pos, y, pos, y+h);
      }
    }

    for(int i=yAxisMarks.size()-1; i>=0; i--) {
      final double value = yAxisMarks.getItem(i).value;
      if(isYVisible(value)) {
        final int pos = y + yLPtoDP(value, h);
        g.drawLine(x, pos, x+w, pos);
      }
    }
  }


  private void drawSeries(Graphics2D g, int x, int y, int w, int h) {
    if(w <= 0 || h <= 0 ||
       mMinX >= mMaxX || mMinY >= mMaxY) {
      return;  
    }

    g.setClip(x, y, w, h);

    final int count = series.size();

    for(int i=0; i<count; i++) {
      final Serie serie = series.getSerie(i);
      if(serie.visible) {
        drawSerie(serie, g, x, y, w, h);
      }
    }

    g.setClip(null);
  }


  private void drawSerie(Serie serie, Graphics2D g, int x, int y, int w, int h) {
    g.setColor(serie.color);

    final DotList dots = serie.dots;

    final int count = dots.size();

    for(int i=0; i<count-1; i++) {
      final Dot dot1 = dots.getDot(i);
      final Dot dot2 = dots.getDot(i+1);

      if((dot1.flags & Dot.EMPTY) != Dot.EMPTY &&
         (dot2.flags & Dot.EMPTY) != Dot.EMPTY) {
        final int x1 = x + xLPtoDP(dot1.x, w);
        final int y1 = y + yLPtoDP(dot1.y, h);
        final int x2 = x + xLPtoDP(dot2.x, w);
        final int y2 = y + yLPtoDP(dot2.y, h);
        g.drawLine(x1, y1, x2, y2);
      }
    }
  }


  private void drawAxis(Graphics2D g, int x, int y, int w, int h) {
    if(w <= 0 || h <= 0 ||
       mMinX >= mMaxX || mMinY >= mMaxY) {
        return;
    }

    g.setFont(getFont());

    for(int i=xAxisMarks.size()-1; i>=0; i--) {
      final AxisMark mark = xAxisMarks.getItem(i);

      final double value = mark.value;
      final String text = mark.text;

      if(isXVisible(value)) {
        final int pos = x + xLPtoDP(value, w);
        g.setColor(mAxisMarksColor);
        g.drawLine(pos, y + h, pos, y + h + mAxisMarksLength);

        final TextLayout layout =
          new TextLayout(text, g.getFont(), g.getFontRenderContext());
        g.setColor(getForeground());
        g.drawString(text, (int)(pos - layout.getBounds().getWidth()/2),
                     (int)(y + h + mAxisMarksLength + 2 +
                     layout.getBounds().getHeight()));
      }
    }

    for(int i=yAxisMarks.size()-1; i>=0; i--) {
      final AxisMark mark = yAxisMarks.getItem(i);

      final double value = mark.value;
      final String text = mark.text;

      if(isYVisible(value)) {
        final int pos = y + yLPtoDP(value, h);
        g.setColor(mAxisMarksColor);
        g.drawLine(x, pos, x-mAxisMarksLength, pos);

        final TextLayout layout =
          new TextLayout(text, g.getFont(), g.getFontRenderContext());
        g.setColor(getForeground());
        g.drawString(text, (int)(x - mAxisMarksLength - 2 -
                     layout.getBounds().getWidth()),
                     (int)(pos + layout.getBounds().getHeight()/2));
      }
    }
  }


  private void drawImage(Graphics g) {
    if(mImage != null) {
      final Insets insets = getInsets();
      
      final int x = insets.left;
      final int y = insets.top;

      g.drawImage(mImage, x, y, null);
    }
  }


  private boolean isXVisible(double x) {
    return x >= mMinX && x <= mMaxX;
  }


  private boolean isYVisible(double y) {
    return y >= mMinY && y <= mMaxY;
  }


  private int xLPtoDP(double x, int w) {
    return (int)((x-mMinX)*(double)w/(mMaxX - mMinX));
  }


  private int yLPtoDP(double y, int h) {
    return h - (int)((y-mMinY)*(double)h/(mMaxY - mMinY));
  }


  public static class Margins {
    public final int top;
    public final int left;
    public final int bottom;
    public final int right;
    
    public Margins(int top, int left, int bottom, int right) {
      this.top = top;
      this.left = left;
      this.bottom = bottom;
      this.right = right;
    }
  }


  public static class AxisMark {
    public final double value;
    public final String text;
    
    public AxisMark(double value, String text) {
      this.value = value;
      this.text = text;
    }
  }


  static public class AxisMarkList {
    private final Vector mList = new Vector();
    
    public int size() {
      return mList.size();
    }


    public AxisMark getItem(int index) {
      return (AxisMark)mList.get(index);
    }


    public void setItem(int index, AxisMark item) {
      mList.set(index, item);
    }


    public void addItem(AxisMark item) {
      mList.addElement(item);
    }


    public void addItem(double value, String text) {
      addItem(new AxisMark(value, text));
    }


    public void clear() {
      mList.clear();
    }


    public void delete(int index) {
      mList.remove(index);
    }
  }


  public static class Dot {
    public static final int EMPTY = 1;

    public double x;
    public double y;
    public int flags;


    public Dot(double x, double y) {
      this.x = x;
      this.y = y;
    }


    public Dot(double x, double y, int flags) {
      this(x, y);
      this.flags = flags;
    }
  }


  public static class DotList {
    private final Vector mList = new Vector();

    public int size() {
      return mList.size();
    }


    public Dot getDot(int index) {
      return (Dot)mList.get(index);
    }


    public void setDot(int index, Dot dot) {
      mList.set(index, dot);
    }


    public void delete(int index) {
      mList.remove(index);
    }


    public void add(Dot dot) {
      mList.addElement(dot);
    }


    public void add(double x, double y) {
      add(new Dot(x, y));
    }


    public void add(int x, int y) {
      add(new Dot(x, y));
    }
  }


  public static class Serie {
    public final DotList dots = new DotList();
    public Color color = Color.red;
    public boolean visible = true;
  }


  public static class SerieList {
    private final Vector mList = new Vector();

    public int size() {
      return mList.size();
    }


    public Serie getSerie(int index) {
      return (Serie)mList.get(index);
    }


    public void setSerie(int index, Serie serie) {
      mList.set(index, serie);
    }


    public void clear() {
      mList.clear();
    }


    public void add(Serie serie) {
      mList.add(serie);
    }
  }
}
