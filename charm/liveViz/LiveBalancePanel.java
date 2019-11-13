package charm.liveViz;

import java.awt.*;
import java.util.*;

import org.jfree.*;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.*;
import org.jfree.data.category.*;

import org.jfree.chart.renderer.category.*;

import javax.swing.SwingUtilities;
import java.lang.reflect.InvocationTargetException;

class LiveBalancePanel extends Panel {
  private JFreeChart chart;
  private ChartPanel chartPanel;
  private DefaultCategoryDataset dataset;
  private Comparator<PELoad> comparator;

  private boolean showChareLoads, showBGLoad, showIdleTime;

  public static enum Orientation {
    HORIZONTAL, VERTICAL
  }

  public static enum SortOrder {
    BY_PE, BY_LOAD
  }

  private class PELoad {
    public int peNum;
    public int numChares;
    public int[] chareLoads;
    public int bg;
    public int totalLoad;
    public int idle;
  }

  private class PELoadComparator implements Comparator<PELoad> {
    public int compare(PELoad pe1, PELoad pe2) {
      return pe1.totalLoad - pe2.totalLoad;
    }
  }

  private class PENumComparator implements Comparator<PELoad> {
    public int compare(PELoad pe1, PELoad pe2) {
      return pe1.peNum - pe2.peNum;
    }
  }

  public LiveBalancePanel() {
    setLayout(new BorderLayout());
    dataset = new DefaultCategoryDataset();
    chart = ChartFactory.createStackedBarChart(
        "",  // chart title
        "PE",                  // domain axis label
        "CPU Load",                     // range axis label
        dataset,                     // data
        PlotOrientation.HORIZONTAL,    // the plot orientation
        false,                        // legend
        true,                        // tooltips
        false                        // urls
      );
    comparator = new PENumComparator();

    Color transparent = new Color(0,0,0,0);
    chart.setBackgroundPaint(transparent);

    CategoryPlot plot = chart.getCategoryPlot();
    plot.setBackgroundPaint(transparent);

    BarRenderer renderer = (BarRenderer) plot.getRenderer();
    renderer.setBarPainter(new StandardBarPainter());
    renderer.setDrawBarOutline(true);
    renderer.setShadowVisible(false);

    showChareLoads = showBGLoad = showIdleTime = false;

    chartPanel = new ChartPanel(chart);
    chartPanel.setMinimumSize(new java.awt.Dimension(200, 800));
    add(chartPanel);
  }

  public void setData(int[] data) {
    int numPEs = data[0];
    int index = 1;
    int totalChares = 0;
    ArrayList<PELoad> pes = new ArrayList<PELoad>();
    for (int p = 0; p < numPEs; p++) {
      PELoad pe = new PELoad();
      pe.peNum = data[index++];
      pe.bg = data[index++];
      if (showBGLoad) { pe.totalLoad += pe.bg; }
      pe.idle = data[index++];
      pe.numChares = data[index++];
      totalChares += pe.numChares;
      pe.chareLoads = new int[pe.numChares];
      for (int i = 0; i < pe.numChares; i++, index++) {
        pe.chareLoads[i] = data[index];
        pe.totalLoad += data[index];
      }
      pes.add(pe);
    }
    Collections.sort(pes, comparator);

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        dataset.setNotify(false);
        dataset.clear();
        for (PELoad pe : pes) {
          if (showBGLoad) {
            dataset.addValue(pe.bg, "BG", "PE" + pe.peNum);
          }
          int objLoad = 0;
          for (int i = 0; i < pe.numChares; i++) {
            if (showChareLoads) {
              dataset.addValue(pe.chareLoads[i], "PE" + pe.peNum + ":Chare" + i, "PE" + pe.peNum);
            }
            objLoad += pe.chareLoads[i];
          }
          if (!showChareLoads) {
            dataset.addValue(objLoad, "PE" + pe.peNum + ":ObjLoad", "PE" + pe.peNum);
          }
          if (showIdleTime) {
            dataset.addValue(pe.idle, "PE" + pe.peNum + ":Idle", "PE" + pe.peNum);
          }
        }

        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        if (showBGLoad) {
          renderer.setSeriesPaint(0, Color.BLACK);
        }
        dataset.setNotify(true);
      }
    });
  }

  public void rotate() {
    CategoryPlot p = chart.getCategoryPlot();
    if (p.getOrientation() == PlotOrientation.HORIZONTAL) {
      setOrientation(Orientation.VERTICAL);
    } else {
      setOrientation(Orientation.HORIZONTAL);
    }
  }

  public void setOrientation(Orientation o) {
    if (o == Orientation.HORIZONTAL) {
      CategoryPlot p = chart.getCategoryPlot();
      p.setOrientation(PlotOrientation.HORIZONTAL);
    } else {
      CategoryPlot p = chart.getCategoryPlot();
      p.setOrientation(PlotOrientation.VERTICAL);
    }
  }
  public void setSortOrder(SortOrder o) {
    if (o == SortOrder.BY_PE) {
      comparator = new PENumComparator();
    } else {
      comparator = new PELoadComparator();
    }
  }

  public void showChares() { showChareLoads = true; }
  public void hideChares() { showChareLoads = false; }

  public void showBG() { showBGLoad = true; }
  public void hideBG() { showBGLoad = false; }

  public void showIdle() { showIdleTime = true; }
  public void hideIdle() { showIdleTime = false; }
}
