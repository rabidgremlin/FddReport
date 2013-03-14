package com.rabidgremlin.fddreport;

import java.io.FileOutputStream;
import java.io.FileReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import com.rabidgremlin.fddreport.bindings.Project;

public class Main
{

  /**
   * @param args
   */
  public static void main(String[] args)
  {
    try
    {
      JAXBContext context = JAXBContext.newInstance("com.rabidgremlin.fddreport.bindings");
      Unmarshaller unmarshaller = context.createUnmarshaller();
      Project project = (Project) unmarshaller.unmarshal(new FileReader(args[0]));

      System.out.println("Generating report for: " + project.getProjectName());

      ReportGenerator gen = new ReportGenerator(project);

      FileOutputStream out = new FileOutputStream(args[1]);
      gen.generatePdf(out);
      out.close();

    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

}
