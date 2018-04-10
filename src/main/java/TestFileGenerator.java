import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestFileGenerator {
    private static void expandFiles(String baseDir, String fileName) throws IOException {
        try (BufferedReader inputStream =
                     new BufferedReader(new FileReader(baseDir + "\\" + fileName + ".txt"))){

            String line = inputStream.readLine();
            // If this is not a container file, just expand this file
            if (line != null) {
                ArrayList<String> lines;
                if (!line.equals("#TestFileGenerator container#")) {
                    lines = expandFile(baseDir, fileName, false);
                } else {
                    lines = new ArrayList<>();
                    // This is a container file
                    do {
                        //System.out.println("line = " + line);
                        // Expand all not commented lines
                        if (line.length() > 0 && !line.substring(0, 1).equals("#")) {
                            ArrayList<String> newLines = expandFile(baseDir, line, false);
                            System.out.println("got " + newLines.size() + " lines from " + line);
                            lines.addAll(newLines);
                        }
                    } while ((line = inputStream.readLine()) != null);
                }
                String resultsFileName = "\\results_" + fileName + ".txt";
                PrintWriter outputStream = new PrintWriter(new FileWriter(baseDir + resultsFileName));
                for (String outputLine : lines) {
                    outputStream.println(outputLine);
                }
                System.out.println("Written " + lines.size() + " lines in " + resultsFileName);
                outputStream.close();
            }
        }
    }

    private static ArrayList<String> expandFile(String baseDir, String fileName, boolean expandResultFile) throws IOException {
        String inputBaseDir = baseDir;
        if (expandResultFile) {
            inputBaseDir += "\\results\\";
        }

        // Find if we need to expand something
        String expansionName = findExpansionName(inputBaseDir, fileName);

        ArrayList<String> outputLines;

        // If there is nothing to expand, just return all lines in the file
        String inputFileName = inputBaseDir + "\\" + fileName + ".txt";
        if (expansionName == null) {
            try (BufferedReader inputStream = new BufferedReader(new FileReader(inputFileName))) {
                outputLines = new ArrayList<>();
                String line;
                while ((line = inputStream.readLine()) != null) {
                    outputLines.add(line);
                }
                System.out.println("added " + outputLines.size() + " lines from " + inputFileName);
            }
        } else {
            System.out.println("expansionName = " + expansionName);

            try (BufferedReader inputStream = new BufferedReader(new FileReader(inputFileName))) {
                long lineCount = 0;
                String expansionFile = fileName + "." + expansionName;
                PrintWriter outputStream = new PrintWriter(
                        new FileWriter(baseDir + "\\results\\" + expansionFile + ".txt"));

                String line;
                while ((line = inputStream.readLine()) != null) {
                    // Only work on non-commented lines
                    if (line.length() > 0 && !line.substring(0, 1).equals("#")) {
                        String expansionFileName = baseDir + "\\variations\\" + expansionName + ".txt";
                        BufferedReader expansionInputStream =
                                new BufferedReader(new FileReader(expansionFileName));
                        //System.out.println("expansionFileName=" + expansionFileName);

                        //int column = Integer.parseInt(expansion.substring(expansion.length() - 1));

                        // Try replacing all columns from the expansion file
                        String expansionLine;
                        //System.out.println("expansionLine=" + expansionLine);
                        boolean lineExpanded = false;
                        while ((expansionLine = expansionInputStream.readLine()) != null) {
                            String newLine = line;
                            String[] columns = expansionLine.split("\\t");
                            for (int i = 0; i < columns.length; i++) {
                                //System.out.println("columns[" + i + "] = " + columns[i]);
                                newLine = newLine.replace("[" + expansionName + (i + 1) + "]", columns[i]);
                                //System.out.println("newLine=" + newLine);
                            }
                            // Write a new line only if it has been expanded
                            if (!newLine.equals(line)) {
                                //System.out.println(newLine);
                                outputStream.println(newLine);
                                lineCount++;
                                lineExpanded = true;
                            }
                        }
                        // Need to write out not expanded lines too
                        if (!lineExpanded) {
                            outputStream.println(line);
                            lineCount++;
                        }
                    }
                }

                outputStream.close();
                //inputStream.close();

                System.out.println("Written " + lineCount + " lines in " + expansionFile);

                // Recursively call yourself on expanded file to see if further expansion is needed
                outputLines = expandFile(baseDir, expansionFile, true);
            }
        }

        return outputLines;
    }

    private static String findExpansionName(String baseDir, String fileName) throws IOException {
        String expansionName = null;
        try (BufferedReader inputStream =
                     new BufferedReader(new FileReader(baseDir + "\\" + fileName + ".txt"))){

        	String line;
            while ((line = inputStream.readLine()) != null) {
                // Only work on non-commented lines
                if (line.length() > 0 && !line.substring(0, 1).equals("#")) {
                    //System.out.println("line = " + line);

                    Pattern pattern = Pattern.compile("\\[(\\S+)]");   // the pattern to search for
                    Matcher matcher = pattern.matcher(line);

                    // If we find a match, get expansion name from the regex group
                    if (matcher.find()) {
                        String expansion = matcher.group(1);
                        expansionName = expansion.substring(0, expansion.length() - 1);
                        break;
                    }
                }
            }
            // inputStream.close();
        }
        return expansionName;
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 2) {
            expandFiles(args[0], args[1]);
        } else {
            String baseDir = "C:\\samsung\\can-central-AB\\primary\\youtube\\tests\\filters";
            expandFiles(baseDir, "test-youtube-Find-VideoType");
            //expandFiles(baseDir, "test-youtube-Find-OrderBy");
            //expandFiles(baseDir, "test-youtube-all-old");

            //expandFiles(baseDir, "test-youtube-all");

            //expandFiles(baseDir, "testYouTube-Play"); // 2248 tests from 50 lines
            //expandFiles(baseDir, "testYouTube-Find-resourceType");
        }
    }
}
