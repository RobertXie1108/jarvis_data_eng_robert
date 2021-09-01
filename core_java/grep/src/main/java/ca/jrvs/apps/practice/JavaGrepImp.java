package ca.jrvs.apps.practice;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaGrepImp implements JavaGrep {

    private String regex;
    private String rootDir;
    private String outputFile;
    private Logger jgrepLogger;

    public JavaGrepImp() {
        jgrepLogger = LoggerFactory.getLogger(JavaGrepImp.class);
    }

    @Override
    public void process() throws IOException {
        List<String> lines = new ArrayList<>();

        for (File file : listFiles(rootDir)) {
            for (String line : readLines(file)) {
                if (containsPattern(line)) {
                    lines.add(file.getName() + ": " + line);
                }
            }
        }
        writeToFile(lines);
    }

    @Override
    public List<File> listFiles(String rootDir) {
        List<File> fileList = new ArrayList<>();
        File root = new File(rootDir);
        File[] filesToCheck;
        if (root.isDirectory()) {
            filesToCheck = root.listFiles();
            if (filesToCheck != null) {
                for (File file : filesToCheck) {
                    if (file.isFile()) {
                        fileList.add(file);
                    } else if (file.isDirectory()) {
                        fileList.addAll(listFiles(file.getAbsolutePath()));
                    }
                }
            }
        }
        return fileList;
    }

    @Override
    public List<String> readLines(File inputFile) {
        List<String> lines = new ArrayList<>();
        String line;
        // Try to read one line then, while the current line isn't null,
        // add it to the list and get the next, otherwise return
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            line = reader.readLine();
            while (line != null) {
                lines.add(line);
                line = reader.readLine();
            }
        } catch (IOException ex) {
            jgrepLogger.error(ex.getMessage());
        }
        return lines;
    }

    @Override
    public boolean containsPattern(String line) {
        return Pattern.compile(regex).matcher(line).find();
    }

    @Override
    public void writeToFile(List<String> lines) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

        for (String line : lines) {
            writer.write(line + "\n");
        }
        writer.close();
    }

    @Override
    public String getRootPath() {
        return rootDir;
    }

    @Override
    public void setRootPath(String rootPath) {
        rootDir = rootPath;
    }

    @Override
    public String getRegex() {
        return regex;
    }

    @Override
    public void setRegex(String regex) {
        this.regex = regex;
    }

    @Override
    public String getOutFile() {
        return outputFile;
    }

    @Override
    public void setOutFile(String outFile) {
        outputFile = outFile;
    }

    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger("Main");
        if (args.length != 3) {
            throw new IllegalArgumentException("USAGE: regex search_root output_file");
        }
        JavaGrepImp jgrep = new JavaGrepImp();
        jgrep.setRegex(args[0]);
        jgrep.setRootPath(args[1]);
        jgrep.setOutFile(args[2]);

        try {
            jgrep.process();
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }
}
