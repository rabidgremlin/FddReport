package com.rabidgremlin.fddreport;

import java.io.InputStream;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.rabidgremlin.fddreport.bindings.Project;
import com.rabidgremlin.fddreport.bindings.Project.Features;
import com.rabidgremlin.fddreport.bindings.Project.Features.Feature;

public class XlsReader
{

  public Project loadProject(InputStream inp) throws Exception
  {
    Project project = new Project();

    Workbook wb = WorkbookFactory.create(inp);

    extractProjectDetails(project, wb);
    extractFeatures(project, wb);

    return project;
  }

  private void extractProjectDetails(Project project, Workbook wb) throws Exception
  {
    Sheet sheet = wb.getSheetAt(0);
    project.setProjectName(sheet.getRow(0).getCell(1).getStringCellValue());
    project.setStartDate(makeXMLDate(sheet.getRow(1).getCell(1).getDateCellValue()));
  }

  private void extractFeatures(Project project, Workbook wb) throws Exception
  {
    Sheet sheet = wb.getSheetAt(2);

    Features features = new Features();
    project.setFeatures(features);

    int rowCount = 1;
    while (true)
    {
      Row currentRow = sheet.getRow(rowCount++);
      if (currentRow == null || currentRow.getCell(0) == null)
      {
        break;
      }
      
      Feature feature = new Feature();
      feature.setName(currentRow.getCell(0).getStringCellValue());
      
      if (!cellNotBlank(currentRow,12))
      {
        throw new Exception("Promote to build date must be specified for feature '" + feature.getName() +"' on row " + rowCount);
      }
      
      feature.setPromoteToBuildPlanned(makeXMLDate(currentRow.getCell(12).getDateCellValue()));
      
      if (cellNotBlank(currentRow,2))
      {
        feature.setDomainWalkthroughPlanned(makeXMLDate(currentRow.getCell(2).getDateCellValue()));
      }
      
      if (cellNotBlank(currentRow,3))
      {
        feature.setDomainWalkthroughActual(makeXMLDate(currentRow.getCell(3).getDateCellValue()));
      }
      
      if (cellNotBlank(currentRow,4))
      {
        feature.setDesignPlanned(makeXMLDate(currentRow.getCell(4).getDateCellValue()));
      }
      
      if (cellNotBlank(currentRow,5))
      {
        feature.setDesignActual(makeXMLDate(currentRow.getCell(5).getDateCellValue()));
      }
      
      if (cellNotBlank(currentRow,6))
      {
        //System.out.println("CT" + Cell.CELL_TYPE_BLANK);
        //System.out.println("==:" + currentRow.getCell(6).getCellType());
        
        feature.setDesignReviewPlanned(makeXMLDate(currentRow.getCell(6).getDateCellValue()));
      }
      
      if (cellNotBlank(currentRow,7))
      {
        feature.setDesignReviewActual(makeXMLDate(currentRow.getCell(7).getDateCellValue()));
      }
      
      if (cellNotBlank(currentRow,8))
      {
        feature.setCodePlanned(makeXMLDate(currentRow.getCell(8).getDateCellValue()));
      }
      
      if (cellNotBlank(currentRow,9))
      {
        feature.setCodeActual(makeXMLDate(currentRow.getCell(9).getDateCellValue()));
      }
      
      if (cellNotBlank(currentRow,10))
      {
        feature.setCodeReviewPlanned(makeXMLDate(currentRow.getCell(10).getDateCellValue()));
      }
      
      if (cellNotBlank(currentRow,11))
      {
        feature.setCodeReviewActual(makeXMLDate(currentRow.getCell(11).getDateCellValue()));
      }
      
      if (cellNotBlank(currentRow,13))
      {
        feature.setPromoteToBuildActual(makeXMLDate(currentRow.getCell(13).getDateCellValue()));
      }
      
      
      
      features.getFeature().add(feature);
    }

    // project.setProjectName(sheet.getRow(0).getCell(1).getStringCellValue());
    // project.setStartDate(makeXMLDate(sheet.getRow(1).getCell(1).getDateCellValue()));
  }
  
  private boolean cellNotBlank(Row row, int cellNum)
  {
    return row.getCell(cellNum) != null && row.getCell(cellNum).getCellType() != Cell.CELL_TYPE_BLANK;
  }

  private XMLGregorianCalendar makeXMLDate(Date date) throws Exception
  {
    GregorianCalendar c = new GregorianCalendar();
    c.setTime(date);
    return DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
  }

}
