import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/*
-Created by Jeremy Wang
-chutian.w@samsung.com
-04/17/2018
*/

@SuppressWarnings("UnusedAssignment")
public class TrainingDataConverter {
    private boolean hasHeader = false;

    private List<File> inputFiles;
    private String outputFile;

    private TrainingDataConverter(List<File> inputFiles, String outputFile) {
        this.inputFiles = inputFiles;
        this.outputFile = outputFile;
    }

    private void convertAll() {
        int count = 0;
        for (int i = 0; i < inputFiles.size(); i++) {
            count += convert(i);
        }
        System.out.println("Utterance count: " + count);
    }

    private String generatePlainUtterance(String str) {
        while (str.contains("[") && str.contains("]")) {
            str = str.substring(0, str.indexOf("[")) + str.substring(str.indexOf("]") + 1, str.length());
        }

        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            if (c != '(' && c != ')' && c != '{' && c != '}') {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    private int convert(int inputIndex) {
//  get me the weather in boston tomorrow ->
//      "[g:viv.twc.Weather] get me the weather in (boston)[v:viv.geo.LocalityName] (tomorrow)[v:viv.time.DateTimeExpression]"
        int count = 0;

        BufferedReader reader = null;
        BufferedWriter writer = null;
        try {
            reader = new BufferedReader(new FileReader(inputFiles.get(inputIndex)));
            writer = new BufferedWriter(new FileWriter(outputFile, true));
            System.out.println("Reading the " + inputIndex + "th file: " + inputFiles.get(inputIndex).getName());
            String line = reader.readLine();

            if (!hasHeader) {
                //create header
                writer.write("capsule,goal,utterance,tagged_utterance,enable,isFollowup\n");
                writer.flush();
                hasHeader = true;
            }

            //extract data into target format
            while (line != null) {
                if (line.contains("utterance")) {
                    StringBuilder sb = new StringBuilder();
                    String pruned_uttr = null;
                    String capsule = null;
                    String plain_uttr = null;
                    String goal = null;
                    String enabled = "true";
                    String isContinue = "false";

                    try {
                        pruned_uttr = line.substring(line.indexOf("(") + 1, line.lastIndexOf(")"));
                        int dotIndex = pruned_uttr.indexOf(".");
                        int dotIndex_Sec = pruned_uttr.indexOf(".", dotIndex + 1);

                        capsule = pruned_uttr.substring(dotIndex + 1, dotIndex_Sec);
                        int goalSpliter = pruned_uttr.indexOf("]");

                        if (pruned_uttr.contains(":continue")) {
                            isContinue = "true";
                            goalSpliter = pruned_uttr.indexOf(":continue");
                        }
                        goalSpliter = pruned_uttr.contains("#") ? pruned_uttr.indexOf("#") : goalSpliter;
                        goal = pruned_uttr.substring(dotIndex_Sec + 1, goalSpliter);

                        pruned_uttr = pruned_uttr.substring(pruned_uttr.indexOf("]") + 1);

                        plain_uttr = generatePlainUtterance(pruned_uttr);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        System.out.println(line);
                        line = reader.readLine();
                        continue;

                        //for yelp,facebook special cases only
//                        capsule = "facebook";
//                        goal = pruned_uttr.contains("PostFeedMessage") ? "PostFeedMessage" : "";
//
//                        isContinue = pruned_uttr.contains(":continue:") ? "true" : "false";
//
//                        pruned_uttr = pruned_uttr.substring(pruned_uttr.indexOf("]") + 1);
//                        plain_uttr = generatePlainUtterance(pruned_uttr);
                    }

                    sb.append(capsule);
                    sb.append(",");
                    sb.append(goal);
                    sb.append(",");

                    if (plain_uttr.contains(",")) {
                        sb.append("\"").append(plain_uttr).append("\"");
                        if (sb.charAt(sb.length() - 2) == '"') {
                            sb.deleteCharAt(sb.length() - 1);
                        }
                    } else {
                        sb.append(plain_uttr);
                        if (sb.charAt(sb.length() - 1) == '"') {
                            sb.deleteCharAt(sb.length() - 1);
                        }
                    }
                    sb.append(",");

                    if (pruned_uttr.contains(",")) {
                        sb.append("\"").append(pruned_uttr).append("\"");
                        if (sb.charAt(sb.length() - 2) == '"') {
                            sb.deleteCharAt(sb.length() - 1);
                        }
                    } else {
                        sb.append(pruned_uttr);
                        if (sb.charAt(sb.length() - 1) == '"') {
                            sb.deleteCharAt(sb.length() - 1);
                        }
                    }

                    sb.append(",");

                    int enableCheck = 2;
                    while (line != null && enableCheck > 0 && !line.contains("false")) {
                        line = reader.readLine();
                        enableCheck--;
                    }

                    if (line != null && line.contains("false")) {
                        enabled = "false";
                    }

                    sb.append(enabled);
                    sb.append(",");
                    sb.append(isContinue);
                    sb.append("\n");

                    writer.write(sb.toString());
                    writer.flush();
                    count++;
                }
                line = reader.readLine();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (reader != null)
                    reader.close();
                if (writer != null) {
                    writer.flush();
                    writer.close();
                }
                System.out.println("Terminated successfully");
            } catch (IOException ioe) {
                System.out.println("Error while terminating");
            }
        }
        return count;
    }

    public static void main(String args[]) {
        String inputDir = "C:\\samsung\\can-central-AB\\primary\\youtube\\resources\\en-US\\training";
        if (args.length == 1) {
            inputDir = args[0];
        }
        String outputFile = inputDir + "\\all_trainings.csv";

        List<File> inputFiles = new LinkedList<>();
        try {
            inputFiles = Files.walk(Paths.get(inputDir))
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }

        TrainingDataConverter bixbyDataConverter = new TrainingDataConverter(inputFiles, outputFile);
        bixbyDataConverter.convertAll();
    }
}