package com.rabidgremlin.fddreport;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.xml.datatype.XMLGregorianCalendar;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import com.rabidgremlin.fddreport.bindings.Project;
import com.rabidgremlin.fddreport.bindings.Project.Features.Feature;

public class ReportGenerator
{
  private static final Color COLOR_GOOD = new Color(168, 255, 168);
  private static final Color COLOR_BAD = new Color(255, 168, 168);

  private Project project;

  private int currentYear;
  private int currentMonth;
  private int currentDay;

  private SimpleDateFormat longFormat = new SimpleDateFormat("dd MMMM yyyy HH:mm");
  private SimpleDateFormat shortFormat = new SimpleDateFormat("dd MMMM yyyy");
  private Date now = new Date();

  public ReportGenerator(Project project)
  {
    this.project = project;
    Calendar now = Calendar.getInstance();
    currentYear = now.get(Calendar.YEAR);
    currentMonth = now.get(Calendar.MONTH) + 1;
    currentDay = now.get(Calendar.DAY_OF_MONTH);

  }

  private PdfPCell makeHeaderCell(String mileStone)
  {
    PdfPTable table = new PdfPTable(2);

    PdfPCell cell = new PdfPCell(new Paragraph(mileStone));
    cell.setColspan(2);
    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
    table.addCell(cell);

    cell = new PdfPCell(new Paragraph("Planned"));
    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
    table.addCell(cell);

    cell = new PdfPCell(new Paragraph("Actual"));
    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
    table.addCell(cell);

    cell = new PdfPCell(table);
    cell.setColspan(2);

    return cell;
  }

  private PdfPCell makePlannedDateCell(XMLGregorianCalendar date)
  {
    if (date == null)
    {
      return new PdfPCell(new Paragraph(""));
    }

    PdfPCell cell = new PdfPCell(new Paragraph(date.getDay() + "/" + date.getMonth() + "/" + date.getYear()));
    cell.setBackgroundColor(COLOR_GOOD);

    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
    return cell;
  }

  private PdfPCell makeActualDateCell(XMLGregorianCalendar plannedDate, XMLGregorianCalendar actualDate)
  {
    if (plannedDate == null && actualDate != null)
    {
      return new PdfPCell(new Paragraph("Missing planned date"));
    }

    if (actualDate == null)
    {
      PdfPCell cell = new PdfPCell(new Paragraph(""));

      if (plannedDate != null)
      {
        if (plannedDate.getYear() < currentYear)
        {
          cell.setBackgroundColor(COLOR_BAD);
        }
        else
        {
          if (plannedDate.getYear() == currentYear)
          {
            if (plannedDate.getMonth() < currentMonth)
            {
              cell.setBackgroundColor(COLOR_BAD);
            }
            else
            {
              if (plannedDate.getMonth() == currentMonth)
              {
                if (plannedDate.getDay() <= currentDay)
                {
                  cell.setBackgroundColor(COLOR_BAD);
                }
              }
            }
          }
        }
      }

      return cell;
    }

    PdfPCell cell = new PdfPCell(new Paragraph(actualDate.getDay() + "/" + actualDate.getMonth() + "/" + actualDate.getYear()));

    if (actualDate.toGregorianCalendar().after(plannedDate.toGregorianCalendar()))
    {
      cell.setBackgroundColor(COLOR_BAD);
    }
    else
    {
      cell.setBackgroundColor(COLOR_GOOD);
    }

    /*
     * cell.setBackgroundColor(COLOR_BAD); if (actualDate.getYear() <= plannedDate.getYear()) {
     * cell.setBackgroundColor(COLOR_GOOD); } else { if (actualDate.getYear() == plannedDate.getYear()) { if
     * (actualDate.getMonth() <= plannedDate.getMonth()) { cell.setBackgroundColor(COLOR_GOOD); } else { if
     * (actualDate.getMonth() == plannedDate.getMonth()) { if (actualDate.getDay() <= plannedDate.getDay()) {
     * cell.setBackgroundColor(COLOR_GOOD); } } } } }
     */

    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
    return cell;
  }

  private PdfPCell makePercCompleteCell(Feature feature)
  {
    int perc = 0;

    if (feature.getDomainWalkthroughActual() != null)
    {
      perc += 1;
    }
    if (feature.getDesignActual() != null)
    {
      perc += 40;
    }
    if (feature.getDesignReviewActual() != null)
    {
      perc += 3;
    }
    if (feature.getCodeActual() != null)
    {
      perc += 45;
    }
    if (feature.getCodeReviewActual() != null)
    {
      perc += 10;
    }
    if (feature.getPromoteToBuildActual() != null)
    {
      perc += 1;
    }

    PdfPCell cell = new PdfPCell(new Paragraph("" + perc + "%"));

    if (perc == 100)
    {
      cell.setBackgroundColor(COLOR_GOOD);
    }

    cell.setHorizontalAlignment(Element.ALIGN_CENTER);

    return cell;
  }

  public void generatePdf(OutputStream out) throws Exception
  {
    Document document = new Document(PageSize.A3.rotate());
    document.addTitle("Status report for " + project.getProjectName());
    document.addCreator("FddReport");

    PdfWriter writer = PdfWriter.getInstance(document, out);
    document.open();
    // document.add(new Paragraph("Hello World"));

    setupHeaderAndFooter(document);

    generateCoverPage(document);
    document.newPage();
    generateActualsVsPlannedChart(document, writer);
    document.newPage();
    generateFeatureList(document);

    document.close();
  }

  private void setupHeaderAndFooter(Document document)
  {
    HeaderFooter footer = new HeaderFooter(new Phrase("Report generated: " + longFormat.format(now) + "         Page: "), true);
    footer.setBorder(Rectangle.NO_BORDER);
    document.setFooter(footer);
  }

  private void generateCoverPage(Document document) throws Exception
  {

    Font font1 = new Font(Font.HELVETICA, 50, Font.BOLDITALIC);
    Font font2 = new Font(Font.HELVETICA, 150, Font.BOLDITALIC);

    document.add(new Paragraph("Project Status Report", font1));
    document.add(new Paragraph(project.getProjectName(), font2));
    document.add(new Paragraph("\n" + shortFormat.format(now), font1));
  }

  private void generateFeatureList(Document document) throws DocumentException
  {
    PdfPTable table = new PdfPTable(14);
    table.setWidthPercentage(100);
    table.setHeaderRows(1);
    table.setWidths(new int[]{ 26, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 4 });

    table.addCell("Feature");
    table.addCell(makeHeaderCell("Domain W."));
    table.addCell(makeHeaderCell("Design"));
    table.addCell(makeHeaderCell("Design Review"));
    table.addCell(makeHeaderCell("Code"));
    table.addCell(makeHeaderCell("Code Review"));
    table.addCell(makeHeaderCell("Promote to build"));
    table.addCell("% comp.");
    // table.completeRow();

    for (Feature feature : project.getFeatures().getFeature())
    {
      table.addCell(feature.getName());

      table.addCell(makePlannedDateCell(feature.getDomainWalkthroughPlanned()));
      table.addCell(makeActualDateCell(feature.getDomainWalkthroughPlanned(), feature.getDomainWalkthroughActual()));

      table.addCell(makePlannedDateCell(feature.getDesignPlanned()));
      table.addCell(makeActualDateCell(feature.getDesignPlanned(), feature.getDesignActual()));

      table.addCell(makePlannedDateCell(feature.getDesignReviewPlanned()));
      table.addCell(makeActualDateCell(feature.getDesignReviewPlanned(), feature.getDesignReviewActual()));

      table.addCell(makePlannedDateCell(feature.getCodePlanned()));
      table.addCell(makeActualDateCell(feature.getCodePlanned(), feature.getCodeActual()));

      table.addCell(makePlannedDateCell(feature.getCodeReviewPlanned()));
      table.addCell(makeActualDateCell(feature.getCodeReviewPlanned(), feature.getCodeReviewActual()));

      table.addCell(makePlannedDateCell(feature.getPromoteToBuildPlanned()));
      table.addCell(makeActualDateCell(feature.getPromoteToBuildPlanned(), feature.getPromoteToBuildActual()));

      table.addCell(makePercCompleteCell(feature));
    }

    document.add(table);
  }

  private long getDifference(Calendar a, Calendar b, TimeUnit units)
  {
    return units.convert(b.getTimeInMillis() - a.getTimeInMillis(), TimeUnit.MILLISECONDS);
  }

  private void generateActualsVsPlannedChart(Document document, PdfWriter writer) throws Exception
  {
    WeeklyCounter counter = new WeeklyCounter(project.getStartDate());

    for (Feature feature : project.getFeatures().getFeature())
    {
      counter.incrementPlanned(feature.getPromoteToBuildPlanned());
      if (feature.getPromoteToBuildActual() != null)
      {
        counter.incrementActual(feature.getPromoteToBuildActual());
      }
    }

    DefaultCategoryDataset dataset = new DefaultCategoryDataset();

    List<WeekCount> counts = counter.getCounts();
    int firstWeek = counts.get(0).weekNumber - 1;

    // int plannedCount = 0;
    // int actualCount = 0;
    int previousWeek = firstWeek + 1;
    /*
     * for (WeekCount count : counts) { int currentWeek = count.weekNumber - firstWeek; while (previousWeek < currentWeek) {
     * dataset.setValue(plannedCount, "Planned", "" + previousWeek); if ( (actualCount != 0 && currentWeek <= 5) ||
     * (currentWeek==1)) { dataset.setValue(actualCount, "Actual", "" + previousWeek); }
     * 
     * 
     * previousWeek++; }
     * 
     * plannedCount += count.plannedCount; actualCount += count.actualCount; dataset.setValue(plannedCount, "Planned", "" +
     * currentWeek); if ((actualCount != 0 && currentWeek <= 5) || (currentWeek==1)) { dataset.setValue(actualCount, "Actual",
     * "" + currentWeek); }
     * 
     * previousWeek = currentWeek; }
     */

    // TODO actually calculate this
    Calendar startDate = project.getStartDate().toGregorianCalendar();
    Calendar now = Calendar.getInstance();
    int weeksSinceStart = (int)(getDifference(startDate,now,TimeUnit.DAYS) / 7)+1;
    System.out.println("Weeks into project: " + weeksSinceStart);

    int plannedCount = 0;
    int actualCount = 0;
    int lastWeek = 1;
    for (WeekCount count : counts)
    {
      int currentWeek = count.weekNumber - firstWeek;

      while (lastWeek < currentWeek)
      {
        dataset.setValue(plannedCount, "Planned", "" + lastWeek);
        if (lastWeek < weeksSinceStart)
        {
          System.out.println("Last week is: " + lastWeek);
          dataset.setValue(actualCount, "Actual", "" + lastWeek);
        }
        lastWeek++;
      }

      plannedCount += count.plannedCount;
      actualCount += count.actualCount;

      dataset.setValue(plannedCount, "Planned", "" + currentWeek);

      if (currentWeek < weeksSinceStart)
      {
        dataset.setValue(actualCount, "Actual", "" + currentWeek);
      }
    }

    // JFreeChart chart = ChartFactory.createBarChart("Business distribution", "Location", "Business", dataset,
    // PlotOrientation.VERTICAL,
    // false, true, false);

    JFreeChart chart = ChartFactory.createLineChart(project.getProjectName() + " Features Complete - Planned vs Actual",
        "Week", "Number of features", dataset, PlotOrientation.VERTICAL, false, false, false);

    CategoryPlot plot = chart.getCategoryPlot();
    plot.setBackgroundPaint(Color.white);
    plot.setRangeGridlinePaint(Color.gray);
    plot.setOutlineVisible(false);

    NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
    rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
    rangeAxis.setAutoRangeIncludesZero(true);

    LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
    renderer.setSeriesPaint(0, Color.BLUE);
    renderer.setSeriesPaint(1, Color.BLUE);

    renderer.setSeriesStroke(0, new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[]{ 10.0f,
        6.0f }, 0.0f));
    renderer.setSeriesStroke(1, new BasicStroke(5.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[]{ 1.0f },
        0.0f));

    Rectangle pageSize = document.getPageSize();

    float width = pageSize.getWidth();
    float height = pageSize.getHeight();

    PdfContentByte cb = writer.getDirectContent();
    PdfTemplate tp = cb.createTemplate(width, height);

    Graphics2D g2d = tp.createGraphics(width, height, new DefaultFontMapper());
    // Rectangle2D r2d = new Rectangle2D.Double(document.leftMargin(), 0-document.bottomMargin(), width, height);
    // Rectangle2D r2d = new Rectangle2D.Double(0,document.bottom(), width, height);

    Rectangle2D r2d = new Rectangle2D.Double(0, 0, width, height - document.bottomMargin() * 2);

    chart.draw(g2d, r2d);

    g2d.dispose();

    cb.addTemplate(tp, 0, 0);
  }
}
