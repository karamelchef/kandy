package se.kth.kandy.experiments;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import javax.ejb.Stateless;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * on Linux jpeg file is created under /payara41/glassfish/domains/domain1/config
 *
 * @author Hossein
 */
@Stateless
public class ChartGenerator {

  private static final Logger logger = Logger.getLogger(ChartGenerator.class);

  public void jpgXYChartProfitError(List<Integer[]> percentProfitErrorsPerDate, List<Float> reliabilities,
      Calendar[] experimentDates) {

    final XYSeriesCollection dataset = new XYSeriesCollection();
    for (int dateIndex = 0; dateIndex < experimentDates.length; dateIndex++) { // per experiment date
      final XYSeries percentProfitError = new XYSeries(experimentDates[dateIndex].getTime().toString());
      for (int slbIndex = 0; slbIndex < reliabilities.size(); slbIndex++) {
        percentProfitError.add(reliabilities.get(slbIndex), percentProfitErrorsPerDate.get(dateIndex)[slbIndex]);
      }
      dataset.addSeries(percentProfitError);
    }

    JFreeChart lineChartObject = ChartFactory.createXYLineChart(
        "Percent profit estimation error vs true cost",
        "Reliability Lower Bound (Slb)", "Percent profit estimation error",
        dataset, PlotOrientation.VERTICAL,
        true, true, false);

    int width = 640; /* Width of the image */

    int height = 480; /* Height of the image */

    String fileName = "percentProfitError.jpeg";
    File lineChart = new File(fileName);
    try {
      ChartUtilities.saveChartAsJPEG(lineChart, lineChartObject, width, height);
    } catch (IOException ex) {
      logger.error("unable to save chart");
      return;
    }
    logger.debug(fileName + " generated");
  }

  public void jpgLineChartTerminationRate(Integer percentTerminations[], List<Float> reliabilities)
      throws IOException {

    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    for (int j = 0; j < reliabilities.size(); j++) {
      dataset.addValue(percentTerminations[j], "early termination rate", reliabilities.get(j));
    }
    JFreeChart lineChartObject = ChartFactory.createLineChart(
        "Percent early termination rate of reliability estimation",
        "Reliability Lower Bound (Slb)", "Percent early termination rate",
        dataset, PlotOrientation.VERTICAL,
        true, true, false);

    int width = 640; /* Width of the image */

    int height = 480; /* Height of the image */

    String fileName = "earlyTerminationRate.jpeg";
    File lineChart = new File(fileName);
    ChartUtilities.saveChartAsJPEG(lineChart, lineChartObject, width, height);
    logger.debug(fileName + " created");
  }

  public void jpgLineChartCostPRE(BigDecimal[] relativeErrors, List<Float> reliabilities) throws IOException {

    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    for (int j = 0; j < reliabilities.size(); j++) {
      dataset.addValue(relativeErrors[j], "", reliabilities.get(j));
    }
    JFreeChart lineChartObject = ChartFactory.createLineChart(
        "Percent relative error of CostEstimation",
        "Reliability Lower Bound (Slb)", "Percent relative Error",
        dataset, PlotOrientation.VERTICAL,
        true, true, false);

    int width = 640; /* Width of the image */

    int height = 480; /* Height of the image */

    String fileName = "costRelativeError.jpeg";
    File lineChart = new File(fileName);
    ChartUtilities.saveChartAsJPEG(lineChart, lineChartObject, width, height);
    logger.debug(fileName + " created");
  }

  public void jpgScatterPlotCostPREdistribution(List<BigDecimal> relativeErrors) {
    XYSeries xyseries = new XYSeries("Relative error of sample requests");
    for (int i = 0; i < relativeErrors.size(); i++) {
      xyseries.add(i, relativeErrors.get(i));
    }
    final XYSeriesCollection dataset = new XYSeriesCollection();
    dataset.addSeries(xyseries);

    JFreeChart scatterChartObject = ChartFactory.createScatterPlot(
        "Relative error distribution in fixed Slb and Tr", "Request number", "Percent relative error",
        dataset, PlotOrientation.VERTICAL,
        true, true, false);

    int width = 640; /* Width of the image */

    int height = 480; /* Height of the image */

    String fileName = "relativeErrorDistribution.jpeg";
    File scatterChart = new File(fileName);
    try {
      ChartUtilities.saveChartAsJPEG(scatterChart, scatterChartObject, width, height);
    } catch (IOException ex) {
      logger.error("unable to save chart");
      return;
    }
    logger.debug(fileName + " generated");

  }

}
