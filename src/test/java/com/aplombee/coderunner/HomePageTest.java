package com.aplombee.coderunner;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.file.Path;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Home Page wicket tests
 */
public class HomePageTest {
    private WicketTester tester;

    @Before
    public void setUp() {
        tester = new WicketTester(createMockApplication());
    }

    public String markupPath() {
        String relativeMarkupPath = "src/main/webapp/markup/";
        String currentDirectory = System.getProperty("user.dir");
        String markupPath;
        if (currentDirectory.endsWith("/")) {
            markupPath = currentDirectory + "" + relativeMarkupPath;
        } else {
            markupPath = currentDirectory + "/" + relativeMarkupPath;
        }
        System.out.println("markupPath=" + markupPath);
        return markupPath;
    }

    @Test
    public void homepageRendersSuccessfully() {
        //start and render the test page
        tester.startPage(HomePage.class);
        //assert rendered page class
        tester.assertRenderedPage(HomePage.class);
        tester.assertInvisible("output");
        tester.assertInvisible("compileErr");
        tester.assertComponent("fileform", Form.class);
        tester.assertVisible("fileform");
        tester.assertComponent("fileform:srcbox", TextArea.class);
        tester.assertVisible("fileform:srcbox");
        tester.assertComponent("fileform:argsbox", TextField.class);
        tester.assertComponent("fileform:submitbtn", Button.class);
        tester.assertVisible("fileform:submitbtn");
    }


    /**
     * when no arguments passed
     */
    @Test
    public void testSubmit_1() {
        tester.startPage(HomePage.class);
        TextArea<String> srcArea = (TextArea<String>) tester.getComponentFromLastRenderedPage("fileform:srcbox");
        tester.getSession().setAttribute(WicketApplication.SRC_DIR_PATH_KEY,WicketApplication.srcParentDirPath);
        String src = "public class HelloWorld {\n" +
                "\n" +
                "    public static void main(String[]args){        \n" +
                "       System.out.println(\"hello world\");\n" +
                "    }\n" +
                "\n" +
                "}";
        srcArea.setModelObject(src);
        tester.executeAjaxEvent("fileform:submitbtn", "click");
        MultiLineLabel outputCmp=(MultiLineLabel) tester.getLastRenderedPage().get("output");
        String actualOutputText=outputCmp.getDefaultModelObject().toString().trim();
        Assert.assertEquals("hello world",actualOutputText);
        tester.assertInvisible("unsupportedErrCon");
        tester.assertInvisible("compileErr");
    }


    /**
     * when arguments are passed
     */
    @Test
    public void testSubmit_ArgsRequired_1() {
        tester.startPage(HomePage.class);
        tester.getSession().setAttribute(WicketApplication.SRC_DIR_PATH_KEY,WicketApplication.srcParentDirPath);
        String src = "\n" +
                "public class Adder {\n" +
                "    public static void main(String[]args){\n" +
                "       int num1=Integer.parseInt(args[0]);\n" +
                "       int num2=Integer.parseInt(args[1]);\n" +
                "       int result=num1+num2;\n" +
                "       System.out.println(result);\n" +
                "    }\n" +
                "}";
        TextArea<String> srcArea = (TextArea<String>) tester.getComponentFromLastRenderedPage("fileform:srcbox");
        srcArea.setModelObject(src);
        TextField<String>argsbox=(TextField<String>)tester.getComponentFromLastRenderedPage("fileform:argsbox");
        argsbox.setModelObject("10,20");
        tester.executeAjaxEvent("fileform:submitbtn", "click");
        MultiLineLabel outputCmp=(MultiLineLabel) tester.getLastRenderedPage().get("output");
        String actualOutputText=outputCmp.getDefaultModelObject().toString().trim();
        Assert.assertEquals("30",actualOutputText);
        tester.assertInvisible("unsupportedErrCon");
        tester.assertInvisible("compileErr");
    }


    /**
     * when arguments are required but not sent
     */
    @Test
    public void testSubmit_ArgsRequired_2() {
        tester.startPage(HomePage.class);
        tester.getSession().setAttribute(WicketApplication.SRC_DIR_PATH_KEY,WicketApplication.srcParentDirPath);
        String src = "\n" +
                "public class Adder {\n" +
                "    public static void main(String[]args){\n" +
                "       int num1=Integer.parseInt(args[0]);\n" +
                "       int num2=Integer.parseInt(args[1]);\n" +
                "       int result=num1+num2;\n" +
                "       System.out.println(result);\n" +
                "    }\n" +
                "}";
        TextArea<String> srcArea = (TextArea<String>) tester.getComponentFromLastRenderedPage("fileform:srcbox");
        srcArea.setModelObject(src);
        tester.executeAjaxEvent("fileform:submitbtn", "click");
        MultiLineLabel outputCmp=(MultiLineLabel) tester.getLastRenderedPage().get("output");
        String actualOutputText=outputCmp.getDefaultModelObject().toString().trim();
        Assert.assertTrue(actualOutputText.contains(ArrayIndexOutOfBoundsException.class.getSimpleName()));
        tester.assertInvisible("unsupportedErrCon");
        tester.assertInvisible("compileErr");
    }


    /**
     * unsuported java.io is there
     */
    @Test
    public void testSubmit_Unsupported_1() {
        tester.startPage(HomePage.class);
        tester.getSession().setAttribute(WicketApplication.SRC_DIR_PATH_KEY,WicketApplication.srcParentDirPath);
        String src = "\n" +
                " import java.io.*" +
                "public class Adder {\n" +
                "    public static void main(String[]args){\n" +
                "       int num1=Integer.parseInt(args[0]);\n" +
                "       int num2=Integer.parseInt(args[1]);\n" +
                "       int result=num1+num2;\n" +
                "       System.out.println(result);\n" +
                "    }\n" +
                "}";
        TextArea<String> srcArea = (TextArea<String>) tester.getComponentFromLastRenderedPage("fileform:srcbox");
        srcArea.setModelObject(src);
        TextField<String>argsbox=(TextField<String>)tester.getComponentFromLastRenderedPage("fileform:argsbox");
        argsbox.setModelObject("10,20");
        tester.executeAjaxEvent("fileform:submitbtn", "click");
        tester.assertInvisible("output");
        tester.assertVisible("unsupportedErrCon");
        tester.assertInvisible("compileErr");
        Component errMsgCmp=tester.getComponentFromLastRenderedPage("unsupportedErrCon:err:1:errmsg");
        String errMsgDisplayed=errMsgCmp.getDefaultModelObjectAsString().trim();
        Assert.assertEquals(errMsgDisplayed,"java.io");
    }




    /**
     * unsuported java.io is there
     */
    @Test
    public void testCompileErr_1() {
        tester.startPage(HomePage.class);
        tester.getSession().setAttribute(WicketApplication.SRC_DIR_PATH_KEY,WicketApplication.srcParentDirPath);
        String src =
                "public class Adder {\n" +
                "    public static void main(String[]args){\n" +
                "       int num1=Integer.parseIntargs[0]);\n" +
                "       int num2=Integer.parseInt(args[1]);\n" +
                "       int result=num1+num2;\n" +
                "       System.out.println(result);\n" +
                "    }\n" +
                "}";
        TextArea<String> srcArea = (TextArea<String>) tester.getComponentFromLastRenderedPage("fileform:srcbox");
        srcArea.setModelObject(src);
        TextField<String>argsbox=(TextField<String>)tester.getComponentFromLastRenderedPage("fileform:argsbox");
        argsbox.setModelObject("10,20");
        tester.executeAjaxEvent("fileform:submitbtn", "click");
        tester.assertInvisible("output");
        tester.assertInvisible("unsupportedErrCon");
        tester.assertVisible("compileErr");
        Component errMsgCmp=tester.getComponentFromLastRenderedPage("compileErr:compilerOutput");
        String errMsgDisplayed=errMsgCmp.getDefaultModelObjectAsString().trim();
        Assert.assertTrue(!Strings.isEmpty(errMsgDisplayed));
    }

    public WebApplication createMockApplication() {
        MockApplication mockApp = new MockApplication() {
            @Override
            protected void init() {
                super.init();

                //
                //find markup in the provided file system path
                //
                String markupPath = markupPath();
                //
                //required for markup in webapp
                //
                getResourceSettings().getResourceFinders().add(new Path(markupPath));
            }


        };
        return mockApp;
    }

}
