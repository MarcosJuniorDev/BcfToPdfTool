package com.bcf;

import com.bcf.model.BcfProject;
import com.bcf.parser.BcfReader;
import com.bcf.pdf.PdfReportGenerator;
import com.bcf.ui.MainWindow;

import javax.swing.SwingUtilities;
import java.io.File;

public class Main {

    private static final String APP_VERSION = "1.0.0";

    public static void main(String[] args) {
        if (args.length == 0) {
            // Launch GUI Mode if no arguments are provided
            SwingUtilities.invokeLater(() -> {
                MainWindow window = new MainWindow();
                window.setVisible(true);
            });
            return;
        }

        if (hasOption(args, "-h", "--help")) {
            printHelp();
            return;
        }

        if (hasOption(args, "-v", "--version")) {
            System.out.println("BcfToPdfTool v" + APP_VERSION);
            return;
        }

        String inputPath = null;
        String outputPath = null;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("-o") || arg.equals("--output")) {
                if (i + 1 < args.length) {
                    outputPath = args[++i];
                }
            } else if (!arg.startsWith("-")) {
                if (inputPath == null) {
                    inputPath = arg;
                } else if (outputPath == null) {
                    outputPath = arg;
                }
            }
        }

        if (inputPath == null) {
            System.err.println("Erro: Nenhum arquivo ou pasta BCF de entrada foi especificado.");
            System.err.println("Use --help para instruções de uso.");
            System.exit(1);
        }

        File inputFile = new File(inputPath);
        if (!inputFile.exists()) {
            System.err.println("Erro: O arquivo ou diretório de entrada não existe: " + inputFile.getAbsolutePath());
            System.exit(1);
        }

        if (outputPath == null) {
            String baseName = inputFile.getName();
            if (baseName.endsWith(".bcf.zip")) {
                baseName = baseName.substring(0, baseName.length() - 8);
            } else if (baseName.endsWith(".bcfzip")) {
                baseName = baseName.substring(0, baseName.length() - 7);
            } else if (baseName.endsWith(".bcf") || baseName.endsWith(".zip")) {
                baseName = baseName.substring(0, baseName.lastIndexOf('.'));
            }
            outputPath = new File(inputFile.getParentFile(), baseName + ".pdf").getPath();
        }

        File outputFile = new File(outputPath);

        System.out.println("========================================");
        System.out.println("        BCF to PDF Converter v" + APP_VERSION);
        System.out.println("========================================");
        System.out.println("Entrada: " + inputFile.getAbsolutePath());
        System.out.println("Saída:   " + outputFile.getAbsolutePath());
        System.out.println("Lendo arquivo BCF...");

        long startTime = System.currentTimeMillis();

        try {
            BcfReader reader = new BcfReader();
            BcfProject project = reader.read(inputFile);

            System.out.println("Projeto: " + (project.getName() != null ? project.getName() : "Sem nome"));
            System.out.println("Tópicos encontrados: " + project.getTopics().size());
            System.out.println("Gerando documento PDF...");

            PdfReportGenerator pdfGenerator = new PdfReportGenerator();
            pdfGenerator.generate(project, outputFile);

            long duration = System.currentTimeMillis() - startTime;
            System.out.println("Sucesso! PDF gerado com sucesso em: " + outputFile.getAbsolutePath());
            System.out.println("Tempo total: " + duration + " ms");
            System.out.println("========================================");

        } catch (Exception e) {
            System.err.println("Erro durante a conversão: " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private static boolean hasOption(String[] args, String... options) {
        for (String arg : args) {
            for (String opt : options) {
                if (arg.equalsIgnoreCase(opt)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void printHelp() {
        System.out.println("BCF to PDF Converter - Versão " + APP_VERSION);
        System.out.println("Uso CLI:");
        System.out.println("  java -jar BcfToPdfTool.jar <arquivo-ou-pasta-bcf> [arquivo-saida.pdf]");
        System.out.println("  java -jar BcfToPdfTool.jar <arquivo-ou-pasta-bcf> -o <arquivo-saida.pdf>");
        System.out.println();
        System.out.println("Uso Interface Gráfica:");
        System.out.println("  java -jar BcfToPdfTool.jar");
        System.out.println("  (Basta executar sem argumentos para abrir a janela de clique e arraste)");
        System.out.println();
        System.out.println("Formatos suportados:");
        System.out.println("  - Arquivos .bcf / .bcfzip / .zip");
        System.out.println("  - Diretórios descompactados contendo arquivos BCF (markup.bcf, snapshots, etc.)");
        System.out.println();
        System.out.println("Opções:");
        System.out.println("  -o, --output <caminho>  Especifica o caminho do arquivo PDF de saída");
        System.out.println("  -h, --help              Exibe esta ajuda");
        System.out.println("  -v, --version           Exibe a versão do utilitário");
    }
}
