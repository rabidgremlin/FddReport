package com.rabidgremlin.fddreport.gui;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class FddReportGui
{

  public static void main(String[] args)
  {
    try
    {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

      SwingUtilities.invokeLater(new Runnable()
      {

        @Override
        public void run()
        {
          MainFrame main = new MainFrame();
        }

      });

    }
    catch (Throwable t)
    {
      t.printStackTrace();
    }
  }

}
