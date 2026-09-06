package com.bcf.ui;

import com.bcf.model.BcfProject;
import com.bcf.parser.BcfReader;
import com.bcf.pdf.PdfReportGenerator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.List;

public class MainWindow extends JFrame {
    private static final String APP_VERSION = "1.0.0";
    private static final String APP_NAME = "BCF to PDF Converter";
    private JLabel dropLabel;

    public MainWindow() {
        setTitle(APP_NAME);
        setSize(500, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        setupUI();
    }

    private void setupUI() {
        // Top Header
        JLabel headerLabel = new JLabel("Conversor de BCF para PDF", SwingConstants.CENTER);
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        headerLabel.setBorder(new EmptyBorder(15, 0, 15, 0));
        add(headerLabel, BorderLayout.NORTH);

        // Central Drop Zone
        JPanel dropZonePanel = new JPanel(new BorderLayout());
        dropZonePanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        JPanel dropInnerPanel = new JPanel(new BorderLayout());
        dropInnerPanel.setBackground(new Color(245, 245, 250));
        dropInnerPanel.setBorder(new LineBorder(new Color(200, 200, 210), 2, true));

        dropLabel = new JLabel("<html><center>Arraste e solte o arquivo ou pasta BCF aqui<br><br><span style='font-size:10px; color:gray;'>(.bcf, .bcfzip ou .zip)</span></center></html>", SwingConstants.CENTER);
        dropLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        dropLabel.setForeground(new Color(80, 80, 80));
        
        dropInnerPanel.add(dropLabel, BorderLayout.CENTER);
        dropZonePanel.add(dropInnerPanel, BorderLayout.CENTER);

        // Setup Drag and Drop
        dropInnerPanel.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) return false;
                try {
                    Transferable t = support.getTransferable();
                    @SuppressWarnings("unchecked")
                    List<File> files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
                    if (!files.isEmpty()) {
                        File bcfFile = files.get(0);
                        handleFileDropped(bcfFile);
                        return true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        });

        add(dropZonePanel, BorderLayout.CENTER);

        // Footer
        JLabel footerLabel = new JLabel(APP_NAME + " - v" + APP_VERSION, SwingConstants.RIGHT);
        footerLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        footerLabel.setForeground(Color.GRAY);
        footerLabel.setBorder(new EmptyBorder(5, 10, 5, 10));
        add(footerLabel, BorderLayout.SOUTH);
    }

    private void handleFileDropped(File bcfFile) {
        String lowerName = bcfFile.getName().toLowerCase();
        if (!lowerName.matches(".*\\.(bcf|bcfzip|zip)$") && !bcfFile.isDirectory()) {
            JOptionPane.showMessageDialog(this,
                    "Por favor, selecione um arquivo .bcf, .bcfzip, .zip ou um diretório BCF.",
                    "Formato Inválido", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser(bcfFile.getParentFile());
        fileChooser.setDialogTitle("Salvar PDF como");

        String baseName = bcfFile.getName();
        if (baseName.toLowerCase().endsWith(".bcf.zip")) {
            baseName = baseName.substring(0, baseName.length() - 8);
        } else if (baseName.toLowerCase().endsWith(".bcfzip")) {
            baseName = baseName.substring(0, baseName.length() - 7);
        } else if (baseName.toLowerCase().endsWith(".bcf") || baseName.toLowerCase().endsWith(".zip")) {
            baseName = baseName.substring(0, baseName.lastIndexOf('.'));
        }
        fileChooser.setSelectedFile(new File(bcfFile.getParentFile(), baseName + ".pdf"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("Arquivos PDF (*.pdf)", "pdf"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File saveFile = fileChooser.getSelectedFile();
            if (!saveFile.getName().toLowerCase().endsWith(".pdf")) {
                saveFile = new File(saveFile.getParentFile(), saveFile.getName() + ".pdf");
            }
            convertBcfToPdf(bcfFile, saveFile);
        }
    }

    private void convertBcfToPdf(File inputFile, File outputFile) {
        dropLabel.setText("Convertendo... Aguarde.");
        dropLabel.setForeground(new Color(0, 100, 200));

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                BcfReader reader = new BcfReader();
                BcfProject project = reader.read(inputFile);
                PdfReportGenerator generator = new PdfReportGenerator();
                generator.generate(project, outputFile);
                return null;
            }

            @Override
            protected void done() {
                dropLabel.setText("<html><center>Arraste e solte o arquivo ou pasta BCF aqui<br><br><span style='font-size:10px; color:gray;'>(.bcf, .bcfzip ou .zip)</span></center></html>");
                dropLabel.setForeground(new Color(80, 80, 80));

                try {
                    get();
                    JOptionPane.showMessageDialog(MainWindow.this,
                            "PDF gerado com sucesso em:\n" + outputFile.getAbsolutePath(),
                            "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(MainWindow.this,
                            "Erro durante a conversão:\n" + e.getMessage(),
                            "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}
