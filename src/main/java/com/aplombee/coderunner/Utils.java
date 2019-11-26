package com.aplombee.coderunner;

import org.apache.wicket.util.file.File;
import org.apache.wicket.util.string.Strings;
import org.buildobjects.process.ProcBuilder;
import org.buildobjects.process.ProcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;


public class Utils implements Serializable {
    private static final Logger Log = LoggerFactory.getLogger("Utils");

    private final Set<String> unsupportedLibraries = new HashSet<>();

    public Utils() {
        unsupportedLibraries.add("java.io");
        unsupportedLibraries.add("java.net");
        unsupportedLibraries.add("java.security");
        unsupportedLibraries.add("java.awt");
        unsupportedLibraries.add("java.applet");
    }

    /**
     * @param srcLocation
     * @param fullClassName
     * @param argsArr       arguments array
     * @return result of process
     */
    public ProcessResult executeCodeRun(String srcLocation, String fullClassName, String[] argsArr) {
        try {
            if (argsArr == null) {
                argsArr = new String[0];
            }
            String[] argsTrimmed = Stream.of(argsArr).map(arg1 -> arg1.trim()).toArray(String[]::new);
            ProcResult procResult = new ProcBuilder("java")
                    .withArgs("-cp", srcLocation, fullClassName).withArgs(argsTrimmed)
                    .run();
            ProcessResult processResult = new ProcessResult();
            processResult.setOutput(procResult.getOutputString());
            return processResult;
        } catch (Throwable e) {
            Log.error("exception in runProgram(*)", e);
            ProcessResult processResult = new ProcessResult();
            processResult.setError(e.getMessage());
            return processResult;
        }
    }

    /**
     * Compiles the java file whose path is provided
     *
     * @param filePath path of java file
     * @return result of process
     */
    public ProcessResult executeCompile(String filePath) {
        try {
            ProcResult procResult = new ProcBuilder("javac")
                    .withArgs(filePath)
                    .run();
            ProcessResult processResult = new ProcessResult();
            processResult.setOutput(procResult.getOutputString());
            return processResult;

        } catch (Throwable e) {
            Log.error("exception in runcompile", e);
            ProcessResult result = new ProcessResult();
            result.setError(e.getMessage());
            return result;
        }
    }


    public String grepClassName(String src) {
        int start = src.indexOf("class");
        int startBrace = src.indexOf("{", start + 6);
        String classname = src.substring(start + 6, startBrace);
        String classNameMinusWhite = classname.trim();
        return classNameMinusWhite;
    }


    /**
     * fetches created src java file from source
     *
     * @param srcText
     * @return
     */
    public File srcFileWithData(String srcText, String srcDirPath) {
        try {
            String className = grepClassName(srcText);
            File srcFile = new File(srcDirPath, className + ".java");
            FileOutputStream fileOutputStream = new FileOutputStream(srcFile);
            fileOutputStream.write(srcText.getBytes());
            return srcFile;
        } catch (FileNotFoundException ex) {
            Log.error("file not found", ex);
            return null;
        } catch (IOException ex) {
            Log.error("exception in writing file", ex);
            return null;
        }
    }

    /**
     * converts comma searated arguments into String array arguments
     *
     * @param argsText
     * @return arguments array
     */
    public String[] argsArray(String argsText) {
        if (Strings.isEmpty(argsText)) {
            return new String[0];
        }
        String argsArr[] = argsText.split(",");
        String[] trimmed = Stream.of(argsArr).map(arg -> arg.trim()).toArray(String[]::new);
        return trimmed;
    }


    /**
     * checks if the code contains unsupported features, if there is unsupported feature,
     * it gets added to set of messages
     *
     * @param src
     * @return err messages
     */
    public Set<String> checkForUnSupported(String src) {
        Set<String>messages=new HashSet<>();
        boolean startsWith=src.startsWith("package");
        if(startsWith){
            messages.add("package not supported");
        }
        for (String check : unsupportedLibraries) {
            if (src.contains(check)) {
               messages.add(check);
            }
        }
        return messages;
    }


}
