package com.aplombee.coderunner;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.util.file.File;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * page renders Form for entering Java source
 */
public class HomePage extends WebPage {
    private static final long serialVersionUID = 1L;
    private static final Logger Log = LoggerFactory.getLogger("HomePage");
    private String outputText;
    private MultiLineLabel outputField;

    private WebMarkupContainer compileErrMarker;
    private String compileErr;
    private WebMarkupContainer unsupportedErrContainer;
    private Utils utils1 = new Utils();
    private Set<String> unsupported = new HashSet<>();

    public Utils getUtils() {
        return utils1;
    }

    private boolean programRun = false;

    public HomePage(final PageParameters parameters) {
        super(parameters);
        add(compileErrMarker = new WebMarkupContainer("compileErr") {
            @Override
            protected void onInitialize() {
                super.onInitialize();
                add(new MultiLineLabel("compilerOutput", new LoadableDetachableModel<String>() {
                    @Override
                    protected String load() {
                        return compileErr;
                    }
                }));
                setOutputMarkupPlaceholderTag(true);
            }

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(!Strings.isEmpty(compileErr));
            }
        });

        add(new FileForm("fileform"));

        add(outputField = new MultiLineLabel("output",
                new LoadableDetachableModel<String>() {
                    @Override
                    protected String load() {
                        return outputText;
                    }
                }) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(programRun);
            }
        });
        outputField.setOutputMarkupPlaceholderTag(true);

        unsupportedErrContainer = new UnsupportedErrCon("unsupportedErrCon", new LoadableDetachableModel<Set<String>>() {
            @Override
            protected Set<String> load() {
                return unsupported;
            }
        });
        add(unsupportedErrContainer);
    }

    private class UnsupportedErrCon extends WebMarkupContainer {
        private DataView<String> errsView;

        public UnsupportedErrCon(final String id, final IModel<Set<String>> model) {
            super(id, model);
            setOutputMarkupPlaceholderTag(true);

        }

        @Override
        protected void onConfigure() {
            super.onConfigure();
            Set<String> unsupported = (Set) getDefaultModelObject();
            setVisible(!unsupported.isEmpty());
        }


        @Override
        protected void onBeforeRender() {
            super.onBeforeRender();
            List<String> messagesList = new ArrayList<>(unsupported);
            errsView = new DataView<String>("err", new ListDataProvider<>(messagesList)) {
                @Override
                protected void populateItem(Item<String> item) {
                    item.add(new Label("errmsg", item.getModel()));
                }
            };
            addOrReplace(errsView);
        }
    }

    public class FileForm extends Form<Void> {
        private TextArea<String> srcArea;
        private TextField<String> argsBox;
        private AjaxButton submitBtn;
        private String srcText, argsText;

        public FileForm(String id) {
            super(id);
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();
            srcArea = new TextArea<>("srcbox", new LoadableDetachableModel<String>() {
                @Override
                public String load() {
                    return srcText;
                }

                @Override
                public void setObject(String object) {
                    srcText = object;
                }
            });
            add(srcArea);
            srcArea.setOutputMarkupPlaceholderTag(true);

            argsBox = new TextField<>("argsbox", new LoadableDetachableModel<String>() {
                @Override
                protected String load() {
                    return argsText;
                }

                @Override
                public void setObject(String object) {
                    argsText = object;
                }
            });
            argsBox.setOutputMarkupPlaceholderTag(true);
            add(argsBox);


            submitBtn = new IndicatingAjaxButton("submitbtn") {
                @Override
                protected void onSubmit(AjaxRequestTarget target) {
                    try {
                        String trimmedSrc = srcText.trim();
                        compileErr = "";
                        outputText = "";
                        programRun = false;
                        target.add(outputField);
                        target.add(unsupportedErrContainer);
                        target.add(compileErrMarker);

                        String srcDirPath = (String) FileForm.this.getSession().getAttribute(WicketApplication.SRC_DIR_PATH_KEY);
                        if (Strings.isEmpty(srcDirPath)) {
                            throw new RuntimeException("src dir path not set");
                        }
                        unsupported = getUtils().checkForUnSupported(trimmedSrc);
                        if (!unsupported.isEmpty()) {
                            return;
                        }
                        String className = getUtils().grepClassName(trimmedSrc);
                        File srcFile = srcFileWithData(trimmedSrc);
                        ProcessResult compileResult = getUtils().executeCompile(srcFile.getAbsolutePath());
                        String argsArr[] = getUtils().argsArray(argsText);
                        if (!Strings.isEmpty(compileResult.getError())) {
                            compileErr = compileResult.getError();
                            srcFile.delete();
                            return;
                        }
                        ProcessResult runResult = getUtils().executeCodeRun(srcDirPath, className, argsArr);
                        if (!Strings.isEmpty(runResult.getError())) {
                            outputText = runResult.getError();
                        } else {
                            outputText = runResult.getOutput();
                        }
                        programRun = true;
                        srcFile.delete();
                    } catch (Throwable e) {
                        Log.error("exception in submit()", e);
                    }
                }

                @Override
                protected void onError(AjaxRequestTarget target) {
                    Log.info("inside error=" + getFeedbackMessages().first());
                }
            };
            add(submitBtn);
        }
    }

    /**
     * fetch created src java file created from source
     *
     * @param srcText
     * @return
     */
    public File srcFileWithData(String srcText) {
        String srcDirPath = (String) getSession().getAttribute(WicketApplication.SRC_DIR_PATH_KEY);
        File file = getUtils().srcFileWithData(srcText, srcDirPath);
        return file;
    }


}
