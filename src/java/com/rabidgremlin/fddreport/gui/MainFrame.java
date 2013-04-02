package com.rabidgremlin.fddreport.gui;

import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

import com.rabidgremlin.fddreport.ReportGenerator;
import com.rabidgremlin.fddreport.XlsReader;
import com.rabidgremlin.fddreport.bindings.Project;

public class MainFrame extends JFrame
{
  private JButton btnGenerate;
  private JTextField txtInputFile;
  private JTextField txtOutputPath;
  private JButton btnSelectInputFile;
  private JButton btnSelectOutputPath;

  public MainFrame()
  {
    super("FDD Report");

    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

    setResizable(false);
    setIconImage(new ImageIcon(Thread.currentThread().getContextClassLoader().getResource("res/appicon.png")).getImage());

    createControls();
    setupListeners();

    loadSettings();

    setGenerateButtonState();

    pack();
    setLocationRelativeTo(null);

    setVisible(true);
  }

  private void setGenerateButtonState()
  {
    btnGenerate.setEnabled(!txtInputFile.getText().equals("") && !txtOutputPath.getText().equals(""));
  }

  private void setupListeners()
  {
    DocumentListener textChanged = new DocumentListener()
    {
      @Override
      public void removeUpdate(DocumentEvent arg0)
      {
        setGenerateButtonState();
      }

      @Override
      public void insertUpdate(DocumentEvent arg0)
      {
        setGenerateButtonState();
      }

      @Override
      public void changedUpdate(DocumentEvent arg0)
      {
        setGenerateButtonState();
      }
    };

    txtInputFile.getDocument().addDocumentListener(textChanged);
    txtOutputPath.getDocument().addDocumentListener(textChanged);

    btnSelectInputFile.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent arg0)
      {
        JFileChooser fc = new JFileChooser();
        fc.addChoosableFileFilter(new FileFilter()
        {

          @Override
          public boolean accept(File file)
          {
            return file.getName().toLowerCase().endsWith(".xls");
          }

          @Override
          public String getDescription()
          {
            return "Excel FDD report file";
          }
        });

        if (fc.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION)
        {
          txtInputFile.setText(fc.getSelectedFile().getAbsolutePath());
        }
      }
    });

    btnSelectOutputPath.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent arg0)
      {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (fc.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION)
        {
          txtOutputPath.setText(fc.getSelectedFile().getAbsolutePath());
        }
      }
    });

    btnGenerate.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent arg0)
      {
        try
        {
          generateReport();
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
    });

    this.addWindowListener(new WindowAdapter()
    {

      @Override
      public void windowClosing(WindowEvent e)
      {
        close(0);
      }
    });
  }

  private void createControls()
  {
    JPanel panel = new JPanel();
    panel.setBorder(new EmptyBorder(7, 7, 7, 7));
    panel.setLayout(new GridBagLayout());
    getContentPane().add(panel);

    Insets i = new Insets(2, 2, 2, 2);

    JLabel label = new JLabel("Input spreadsheet:");

    GridBagConstraints c = new GridBagConstraints();
    c.gridx = 0;
    c.gridy = 0;
    c.insets = i;
    panel.add(label, c);

    txtInputFile = new JTextField();
    txtInputFile.setColumns(50);

    c = new GridBagConstraints();
    c.gridx = 1;
    c.gridy = 0;
    c.insets = i;
    panel.add(txtInputFile, c);

    label = new JLabel("Output folder:");

    c = new GridBagConstraints();
    c.gridx = 0;
    c.gridy = 1;
    c.insets = i;
    panel.add(label, c);

    txtOutputPath = new JTextField();
    txtOutputPath.setColumns(50);

    c = new GridBagConstraints();
    c.gridx = 1;
    c.gridy = 1;
    c.insets = i;
    panel.add(txtOutputPath, c);

    btnGenerate = new JButton("Generate");

    c = new GridBagConstraints();
    c.gridx = 0;
    c.gridy = 3;
    c.insets = i;
    c.gridwidth = 3;
    c.anchor = GridBagConstraints.CENTER;
    panel.add(btnGenerate, c);

    btnSelectInputFile = new JButton("...");

    c = new GridBagConstraints();
    c.gridx = 2;
    c.gridy = 0;
    panel.add(btnSelectInputFile, c);

    btnSelectOutputPath = new JButton("...");

    c = new GridBagConstraints();
    c.gridx = 2;
    c.gridy = 1;
    panel.add(btnSelectOutputPath, c);

  }

  private void generateReport() throws Exception
  {

    SwingWorker<String, Void> reportWorker = new SwingWorker<String, Void>()
    {

      @Override
      protected String doInBackground() throws Exception
      {
        InputStream inp = new FileInputStream(txtInputFile.getText());

        XlsReader reader = new XlsReader();
        Project project = reader.loadProject(inp);

        System.out.println("Generating report for: " + project.getProjectName());

        ReportGenerator gen = new ReportGenerator(project);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

        String fileName = project.getProjectName() + " - FDD Report - " + df.format(new Date()) + ".pdf";
        File outputFile = new File(new File(txtOutputPath.getText()), fileName);

        FileOutputStream out = new FileOutputStream(outputFile);
        gen.generatePdf(out);
        out.close();

        return outputFile.getAbsolutePath();
      }

      @Override
      protected void done()
      {
        try
        {
          String outputFileName = get();
          JOptionPane.showMessageDialog(MainFrame.this.getContentPane(), "Report generated.\nSee " + outputFileName, "Done",
              JOptionPane.INFORMATION_MESSAGE);

          close(0);
        }
        catch (Exception e)
        {
		  e.printStackTrace();
          JOptionPane.showMessageDialog(MainFrame.this.getContentPane(), "Error generating report: " + e.getMessage(), "Error",
              JOptionPane.ERROR_MESSAGE);
          close(1);
        }

      }
    };

    btnGenerate.setEnabled(false);
    btnSelectInputFile.setEnabled(false);
    btnSelectOutputPath.setEnabled(false);
    txtInputFile.setEnabled(false);
    txtOutputPath.setEnabled(false);
    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

    reportWorker.execute();

  }

  private File getSettingsFileName()
  {
    return new File(new File(System.getProperty("user.home")), ".fddreport");
  }

  private void loadSettings()
  {
    try
    {
      File settingsFile = getSettingsFileName();
      if (settingsFile.exists())
      {
        System.out.println("Loading settinsg from " + settingsFile.getAbsolutePath());
        
        Properties settings = new Properties();
        settings.load(new FileInputStream(settingsFile));

        txtInputFile.setText(settings.getProperty("input.file"));
        txtOutputPath.setText(settings.getProperty("ouput.path"));
      }
      else
      {
        System.out.println("Settings file does not exist.");
      }
    }
    catch (Exception e)
    {
      System.out.println("Error loading settings... ");
      e.printStackTrace();
    }
  }

  private void close(int exitCode)
  {
    saveSettings();
    dispose();
    // System.exit(1);
  }

  private void saveSettings()
  {
    try
    {
      File settingsFile = getSettingsFileName();

      Properties settings = new Properties();

      settings.setProperty("input.file", txtInputFile.getText());
      settings.setProperty("ouput.path", txtOutputPath.getText());

      settings.store(new FileOutputStream(settingsFile), "Settings for FDD report generator");
      
      System.out.println("Settings file saved");     

    }
    catch (Exception e)
    {
      System.out.println("Error saving settings... ");
      e.printStackTrace();
    }
  }
}
