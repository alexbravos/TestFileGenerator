import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestFileGenerator {
    private static void expandFile(String baseDir, String fileName, boolean expandResultFile) throws IOException {
        String inputBaseDir = baseDir;
        if (expandResultFile) {
            inputBaseDir += "\\results\\";
        }

        // Find if we need to expand something
        String expansionName = findExpansionName(inputBaseDir, fileName);

        if (expansionName != null) {
            System.out.println("expansionName = " + expansionName);

            try (BufferedReader inputStream =
                         new BufferedReader(new FileReader(inputBaseDir + "\\" + fileName + ".txt"))) {

                long lineCount = 0;
                String expansionFile = fileName + "." + expansionName;
                PrintWriter outputStream = new PrintWriter(
                        new FileWriter(baseDir + "\\results\\" + expansionFile + ".txt"));

                String line;
                while ((line = inputStream.readLine()) != null) {
                    if (line.length() > 0) {
                        String expansionFileName = baseDir + "\\" + expansionName + ".txt";
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
                                System.out.println(newLine);
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
                expandFile(baseDir, expansionFile, true);
            }
        }
    }

    private static String findExpansionName(String baseDir, String fileName) throws IOException {
        String expansionName = null;
        try (BufferedReader inputStream =
                     new BufferedReader(new FileReader(baseDir + "\\" + fileName + ".txt"))){

        	String line;
            while ((line = inputStream.readLine()) != null) {
                if (line.length() > 0) {
                    //System.out.println("line = " + line);

                    Pattern pattern = Pattern.compile("\\[(\\S+)]");   // the pattern to search for
                    Matcher matcher = pattern.matcher(line);

                    // if we find a match, get expansion from the regex group
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
            expandFile(args[0], args[1], false);
        } else {
            expandFile("C:\\samsung\\can-central-AB\\primary\\youtube\\tests", "testYouTube", false); // 2248 tests from 50 lines
            //expandFile("C:\\samsung\\can-central-AB\\primary\\youtube\\tests\\small", "testYouTube", false);
            //expandFile("C:\\samsung\\can-central-AB\\primary\\youtube\\tests", "testDataTemplate", false);
        }
    }
}
