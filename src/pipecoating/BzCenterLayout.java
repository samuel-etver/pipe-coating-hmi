package pipecoating;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

public class BzCenterLayout implements LayoutManager {
  public static final int HORIZONTAL_LAYOUT = 0;
  public static final int VERTICAL_LAYOUT = 1;

  private int mLayoutType;

  private int mMinWidth = 0;
  private int mMinHeight = 0;

  private int mPreferredWidth = 0;
  private int mPreferredHeight = 0;

  private int mLeftInset = 0;
  private int mTopInset = 0;
  private int mRightInset = 0;
  private int mBottomInset = 0;

  private int mGap = 0;

  private boolean mSizeUnknown = true;


  public BzCenterLayout() {
    this(VERTICAL_LAYOUT);
  }


  public BzCenterLayout(int layoutType) {
    this.mLayoutType = layoutType;
  }


  public void addLayoutComponent(String name, Component comp) {
  }


  public void removeLayoutComponent(Component comp) {
  }


  public void setSizes(Container parent) {
    mSizeUnknown = false;

    mMinWidth = 0;
    mMinHeight = 0;

    mPreferredWidth = 0;
    mPreferredHeight = 0;

    final int n = parent.getComponentCount();

    for(int i=0; i<n; i++) {
      Component c = parent.getComponent(i);

      if(!c.isVisible()) continue;

      switch(mLayoutType) {
        case HORIZONTAL_LAYOUT:
          mPreferredWidth += c.getPreferredSize().getWidth();
          if(i > 0)
            mPreferredWidth += mGap;
          mPreferredHeight =
            Math.max(mPreferredHeight, (int)c.getPreferredSize().getHeight());
          mMinWidth += c.getMinimumSize().getWidth();
          if(i > 0)
            mMinWidth += mGap;
          mMinHeight =
            Math.max(mMinHeight, (int)c.getMinimumSize().getHeight());
          break;

        case VERTICAL_LAYOUT:
        default:
          mPreferredWidth =
            Math.max(mPreferredWidth, (int)c.getPreferredSize().getWidth());
          mPreferredHeight += c.getPreferredSize().getHeight();
          if(i > 0)
            mPreferredHeight += mGap;
          mMinWidth =
            Math.max(mMinWidth, (int)c.getMinimumSize().getWidth())  ;
          mMinHeight += c.getMinimumSize().getHeight();
          if(i > 0)
            mMinHeight += mGap;
      }
    }
  }


  public Dimension minimumLayoutSize(Container parent) {
    setSizes(parent);

    final Dimension size = new Dimension(0, 0);

    final Insets insets = parent.getInsets();

    size.width = mMinWidth + mLeftInset + mRightInset +
                 insets.left + insets.right;
    size.height = mMinHeight + mTopInset + mBottomInset +
                  insets.top + insets.bottom;

    return size;
  }


  public Dimension preferredLayoutSize(Container parent) {
    setSizes(parent);

    final Dimension size = new Dimension(0, 0);

    final Insets insets = parent.getInsets();

    size.width = mPreferredWidth + mLeftInset + mRightInset +
                 insets.left + insets.right;
    size.height = mPreferredHeight + mTopInset + mBottomInset +
                  insets.top + insets.bottom;

    return size;
  }


  public void layoutContainer(Container parent) {
    if(mSizeUnknown)
      setSizes(parent);

    switch(mLayoutType) {
      case HORIZONTAL_LAYOUT:
        layoutHorizontal(parent); break;
      case VERTICAL_LAYOUT:
      default:
        layoutVertical(parent);
    }
  }


  private void layoutHorizontal(Container parent) {
    int x = (parent.getWidth() - mPreferredWidth)/2;
    int y = parent.getHeight()/2;

    final int n = parent.getComponentCount();

    for(int i=0; i<n; i++) {
      Component c = parent.getComponent(i);

      if(!c.isVisible()) continue;

      c.setBounds(x,
                  y - mPreferredHeight/2,
                  (int)c.getPreferredSize().getWidth(),
                  mPreferredHeight);

      x += c.getWidth() + mGap;
    }
  }


  private void layoutVertical(Container parent) {
    int x = parent.getWidth()/2;
    int y = (parent.getHeight() - mPreferredHeight)/2;

    final int n = parent.getComponentCount();

    for(int i=0; i<n; i++) {
      Component c = parent.getComponent(i);

      if(!c.isVisible()) continue;

      c.setBounds(x - mPreferredWidth/2,
                  y,
                  mPreferredWidth,
                  (int)c.getPreferredSize().getHeight());

      y += c.getHeight() + mGap;
    }
  }


  public Insets getInsets() {
    return new Insets(mTopInset, mLeftInset, mBottomInset, mRightInset);
  }


  public void setInsets(int top, int left, int bottom, int right) {
    mTopInset = top;
    mLeftInset = left;
    mBottomInset = bottom;
    mRightInset = right;

    mSizeUnknown = true;
  }


  public void setInsets(Insets insets) {
    setInsets(insets.top, insets.left, insets.bottom, insets.right);
  }


  public int getLayoutType() {
    return mLayoutType;
  }


  public void setLayoutType(int type) {
    if(type != mLayoutType) {
      mLayoutType = type;
      mSizeUnknown = true;
    }
  }


  public int getGap() {
    return mGap;
  }


  public void setGap(int gap) {
    if(this.mGap != gap) {
      this.mGap = gap;
      mSizeUnknown = true;
    }
  }
}

