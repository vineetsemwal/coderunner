package com.aplombee.coderunner;

import org.buildobjects.process.ProcBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Set;

public class UtilsTest {

    @Test
    public void testGrepClassName() {
        String src = "class HelloWorld{\n" +
                "\n" +
                "public static void main(String[]args){";
        Utils utils = new Utils();
        String fetched = utils.grepClassName(src);
        String expected = "HelloWorld";
        Assert.assertEquals(expected, fetched);
    }

    @Test
    public void testExecuteCompile() throws Throwable {
        Utils utils = new Utils();
        File file = new File("HelloWorld.java");
        System.out.println(file.getAbsolutePath());
        FileOutputStream fos = new FileOutputStream(file);
        String src = "public class HelloWorld {\n" +
                "\n" +
                "    public static void main(String[]args){        \n" +
                "       System.out.println(\"hello world\");\n" +
                "    }\n" +
                "\n" +
                "}";
        fos.write(src.getBytes());
        String absolutePath = file.getAbsolutePath();
        ProcessResult result = utils.executeCompile(absolutePath);
        Assert.assertNull(result.getError());
        file.delete();
    }

    /**
     * run code with no arguments
     * @throws Throwable
     */
    @Test
    public void testExecuteRun_1() throws Throwable {
        Utils utils = new Utils();
        File file = new File("HelloWorld.java");
        System.out.println(file.getAbsolutePath());
        FileOutputStream fos = new FileOutputStream(file);

        String src = "public class HelloWorld {\n" +
                "\n" +
                "    public static void main(String[]args){        \n" +
                "       System.out.println(\"hello world\");\n" +
                "    }\n" +
                "\n" +
                "}";
        fos.write(src.getBytes());
        utils.executeCompile(file.getAbsolutePath());
        String srcDir = System.getProperty("user.dir");
        ProcessResult result = utils.executeCodeRun(srcDir, "HelloWorld", null);
        Assert.assertEquals("hello world", result.getOutput().trim());
        file.delete();
    }

    @Test
    public void testExecuteRun_2() throws Throwable {
        Utils utils = new Utils();
        File file = new File("Adder.java");
        System.out.println(file.getAbsolutePath());
        FileOutputStream fos = new FileOutputStream(file);
        String src = "\n" +
                "public class Adder {\n" +
                "    public static void main(String[]args){\n" +
                "       int num1=Integer.parseInt(args[0]);\n" +
                "       int num2=Integer.parseInt(args[1]);\n" +
                "       int result=num1+num2;\n" +
                "       System.out.println(result);\n" +
                "    }\n" +
                "}";
        fos.write(src.getBytes());
        new ProcBuilder("javac")
                .withArgs(file.getAbsolutePath())
                .run();
        String srcDir = System.getProperty("user.dir");
        String argsArr[]={"10","20"};
        ProcessResult result = utils.executeCodeRun(srcDir, "Adder", argsArr);
        Assert.assertEquals("30", result.getOutput().trim());
        file.delete();
    }

    @Test
    public void testSrcFileWithData() {
        String src = "public class HelloWorld {\n" +
                "\n" +
                "    public static void main(String[]args){        \n" +
                "       System.out.println(\"hello world\");\n" +
                "    }\n" +
                "\n" +
                "}";
        Utils utils = new Utils();
        String srcDir = System.getProperty("user.dir");
        System.out.println("srcdir=" + srcDir);
        String expectedFilePath = srcDir + "/" + "HelloWorld.java";
        File result = utils.srcFileWithData(src, srcDir);
        String actualFilePath = result.getAbsolutePath();
        Assert.assertEquals(expectedFilePath, actualFilePath);
    }

    @Test
    public void testArgsArray() {
        Utils utils = new Utils();
        String src = "one, two ,three, four";
        String[] result = utils.argsArray(src);
        Assert.assertEquals(4,result.length);
        Assert.assertEquals("one", result[0]);
        Assert.assertEquals("two", result[1]);
        Assert.assertEquals("three", result[2]);
        Assert.assertEquals("four", result[3]);

    }


    @Test
    public void testCheckForUnsupported_1(){
        Utils utils=new Utils();
        String src =
                "public class HelloWorld {\n" +
                "\n" +
                "    public static void main(String[]args){        \n" +
                "       System.out.println(\"hello world\");\n" +
                "}";
        Set<String> result =utils.checkForUnSupported(src);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testCheckForUnsupported_2(){
        Utils utils=new Utils();
        String src = "package ex;" +
                " import java.io;" +
                "public class HelloWorld {\n" +
                "\n" +
                "    public static void main(String[]args){        \n" +
                "       System.out.println(\"hello world\");\n" +
                "}";
       Set<String> result =utils.checkForUnSupported(src);
       Assert.assertEquals(result.size(),2);
       Assert.assertTrue(result.contains("java.io"));
       Assert.assertTrue(result.contains("package not supported"));
    }

}