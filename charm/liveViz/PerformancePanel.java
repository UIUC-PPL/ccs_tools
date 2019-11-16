package charm.liveViz;

import java.awt.*;
import java.util.*;

import org.jfree.*;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.*;
import org.jfree.data.category.*;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import org.jfree.chart.renderer.category.*;

import javax.swing.SwingUtilities;
import java.lang.reflect.InvocationTargetException;

class PerformancePanel extends Panel {
  private JFreeChart chart;
  private ChartPanel chartPanel;
  private XYSeriesCollection dataset;
  private XYSeries dataseries;

  public PerformancePanel() {
    setLayout(new BorderLayout());
    dataset = new XYSeriesCollection();
    dataseries = new XYSeries("Performance");
    dataset.addSeries(dataseries);
    chart = ChartFactory.createXYLineChart(
        "",                         // chart title
        "Iteration",                // domain axis label
        "Time per Iteration (ms)",  // range axis label
        dataset,                    // data
        PlotOrientation.VERTICAL,   // the plot orientation
        false,                      // legend
        true,                       // tooltips
        false                       // urls
      );

    Color transparent = new Color(0,0,0,0);
    chart.setBackgroundPaint(transparent);

    XYPlot plot = chart.getXYPlot();
    plot.setBackgroundPaint(transparent);

    chartPanel = new ChartPanel(chart);
    chartPanel.setMinimumSize(new java.awt.Dimension(200, 800));
    add(chartPanel);
  }

  public void addData(int[] data) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        dataset.setNotify(false);
        int numPoints = data[0];
        if (numPoints > 0) {
          int newMax = 0;
          for (int i = 0; i < numPoints; i++) {
            int x = data[i * 2 + 1];
            int y = data[i * 2 + 2];
            dataseries.add(x, y);
            newMax = x;
          }
          XYPlot plot = chart.getXYPlot();
          NumberAxis domain = (NumberAxis)plot.getDomainAxis();
          domain.setRange(newMax - 1000, newMax);
        }
        dataset.setNotify(true);
      }
    });
  }
}
