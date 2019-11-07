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

  private class PELoad {
    public int peNum;
    public int numChares;
    public int totalLoad;
    public int[] chareLoads;

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

    Color transparent = new Color(0,0,0,0);
    chart.setBackgroundPaint(transparent);

    CategoryPlot plot = chart.getCategoryPlot();
    plot.setBackgroundPaint(transparent);

    BarRenderer renderer = (BarRenderer) plot.getRenderer();
    renderer.setBarPainter(new StandardBarPainter());
    renderer.setDrawBarOutline(true);
    renderer.setShadowVisible(false);

    chartPanel = new ChartPanel(chart);
    chartPanel.setMinimumSize(new java.awt.Dimension(200, 800));
    add(chartPanel);
  }

  public void setData(int[] data) {
    int numPEs = data[0];
    int index = 1;
    ArrayList<PELoad> pes = new ArrayList<PELoad>();
    for (int p = 0; p < numPEs; p++) {
      PELoad pe = new PELoad();
      pe.peNum = data[index++];
      pe.numChares = data[index++];
      pe.chareLoads = new int[pe.numChares];
      for (int i = 0; i < pe.numChares; i++, index++) {
        pe.chareLoads[i] = data[index];
        pe.totalLoad += data[index];
      }
      pes.add(pe);
    }
    Collections.sort(pes, new PENumComparator());

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        dataset.setNotify(false);
        dataset.clear();
        for (PELoad pe : pes) {
          for (int i = 0; i < pe.numChares; i++) {
            dataset.addValue(pe.chareLoads[i], "PE" + pe.peNum + ":Chare" + i, "PE" + pe.peNum);
          }
        }
        dataset.setNotify(true);
      }
    });
  }
}
