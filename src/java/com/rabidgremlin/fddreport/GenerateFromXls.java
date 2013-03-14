package com.rabidgremlin.fddreport;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import com.rabidgremlin.fddreport.bindings.Project;

public class GenerateFromXls
{

  public static void main(String[] args)
  {
    try
    {
      InputStream inp = new FileInputStream("C:\\Users\\jack\\Documents\\My Dropbox\\#ClearPoint Projects (1)\\GDTR-1001 Event Management\\Project Management\\Phase1 FDD Status\\GDTPhase1FeatureMileStones.xls");

      XlsReader reader = new XlsReader();
      Project project = reader.loadProject(inp);

      System.out.println("Generating report for: " + project.getProjectName());

      ReportGenerator gen = new ReportGenerator(project);

      FileOutputStream out = new FileOutputStream("C:\\Users\\jack\\Desktop\\test.pdf");
      gen.generatePdf(out);
      out.close();

    }
    catch (Exception e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

}
