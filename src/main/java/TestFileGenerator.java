import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestFileGenerator {
	
//	static List<String> loadParameters(String parameterFileName) throws IOException {
//		BufferedReader parameterInputStream = null;
//		List<String> parameterList = new ArrayList<>();
//
//        try {
//        	parameterInputStream = new BufferedReader(new FileReader(parameterFileName));
//
//            String feedMessage;
//            while ((feedMessage = parameterInputStream.readLine()) != null) {
//            	parameterList.add(feedMessage);
//            }
//        } finally {
//            if (parameterInputStream != null) {
//            	parameterInputStream.close();
//            }
//            if (parameterInputStream != null) {
//            	parameterInputStream.close();
//            }
//        }
//
//        return parameterList;
//	}
	
	private static void generateTestData(String baseDir, String fileName) throws IOException {
		BufferedReader testDataTemplateInputStream = null;
        PrintWriter uttTestDataOutputStream = null;

        try {
        	testDataTemplateInputStream = new BufferedReader(new FileReader(baseDir + "\\" + fileName + ".txt"));

            String line;
            boolean expanded = false;
            while ((line = testDataTemplateInputStream.readLine()) != null) {
                System.out.println("line=" + line);

                Pattern pattern = Pattern.compile("\\[(\\S+)]");   // the pattern to search for
                Matcher matcher = pattern.matcher(line);

                // if we find a match, get the group
                if (matcher.find()) {
                    expanded = true;
                    String expansion = matcher.group(1);
                    String expansionName = expansion.substring(0, expansion.length() - 1);
                    System.out.println("expansionName=" + expansionName);
                    String expansionFileName = baseDir + "\\" + expansionName + ".txt";
                    BufferedReader expansionInputStream = new BufferedReader(new FileReader(expansionFileName));
                    //System.out.println("expansionFileName=" + expansionFileName);

                    int column = Integer.parseInt(expansion.substring(expansion.length() - 1));
                    String expansionLine; // = expansionInputStream.readLine();
                    //System.out.println("expansionLine=" + expansionLine);
                    while ((expansionLine = expansionInputStream.readLine()) != null) {
                        String newLine = line;
                        String[] columns = expansionLine.split("\\t");
                        //int i = 0;
                        for (int i = 0; i < columns.length; i++) {
                            System.out.println("columns[" + i + "]=" + columns[i]);
                            newLine = newLine.replace("[" + expansionName + (i + 1) + "]", columns[i]);
                            //System.out.println("newLine=" + newLine);
                        }
                        System.out.println("newLine=" + newLine);
                        if (uttTestDataOutputStream == null) {
                            uttTestDataOutputStream = new PrintWriter(new FileWriter(baseDir + "\\" + fileName + ".gen.txt"));
                        }
                        uttTestDataOutputStream.println(newLine);
                    }
                }
            }
            testDataTemplateInputStream.close();
            uttTestDataOutputStream.close();

            if (expanded) {
                generateTestData(baseDir, fileName + ".gen");
            }
            //if (line.contains())
//            String uttTemplate;
//            while ((uttTemplate = uttTemplateInputStream.readLine()) != null) {
//            	// Process data
//            	// For each uttTemplate, expand test utterances by substituting feedMessage and generating test data
//            	if (!uttTemplate.contains("PARAM_NAME")) {
//            		uttExpandedOutputStream.println(uttTemplate);
//            		continue; // No need to expand
//            	}
//            	for (String feedMessage : parameterList) {
//            		String expandedTestData = testDataTemplateStr.replaceAll("UTTERANCE", expandedUtt);
//            		expandedTestData = expandedTestData.replaceAll("PARAM_VALUE", feedMessage);
//            		uttTestDataOutputStream.println(expandedTestData);
//            	}
//            }
        } finally {
            if (testDataTemplateInputStream != null) {
            	testDataTemplateInputStream.close();
            }
            if (uttTestDataOutputStream != null) {
            	uttTestDataOutputStream.close();
            }
        }
    }
	
	public static void main(String[] args) throws IOException {
		//String uttTemplateFileName = ; //args[0];
		generateTestData("C:\\samsung\\can-central-AB\\primary\\youtube\\tests", "testDataTemplate");
    }
	
}
