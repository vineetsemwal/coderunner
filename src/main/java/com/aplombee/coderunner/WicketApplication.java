package com.aplombee.coderunner;

import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.Session;
import org.apache.wicket.core.util.file.WebApplicationPath;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.util.file.Folder;
import org.apache.wicket.util.time.Duration;

/**
 * Application object for your web application.
 * If you want to run this application without deploying, run the Start class.
 * 
 * @see com.aplombee.Start#main(String[])
 */
public class WicketApplication extends WebApplication
{
	public int dirCounter;

	public static final String srcParentDirPath = "online_test";

	private Folder srcParentDir;

	public static final String SRC_DIR_PATH_KEY="srcdirpath";

	/**
	 * @see org.apache.wicket.Application#getHomePage()
	 */
	@Override
	public Class<? extends WebPage> getHomePage()
	{
		return HomePage.class;
	}

	/**
	 * @see org.apache.wicket.Application#init()
	 */
	@Override
	public void init()
	{
		super.init();
		String realContextPath=getServletContext().getRealPath("/");
		srcParentDir =new Folder(realContextPath,srcParentDirPath);
		srcParentDir.remove();
		srcParentDir.mkdir();
		//find markups in webapp
		//
		getResourceSettings().getResourceFinders().add(new WebApplicationPath(getServletContext(), "markup"));
		getResourceSettings().setResourcePollFrequency(Duration.seconds(10));

	}


	@Override
	public Session newSession(Request request, Response response) {
		Session session=super.newSession(request, response);
		dirCounter++;
		Folder srcDir=new Folder(srcParentDir,dirCounter+"");
		srcDir.mkdir();
		session.setAttribute(SRC_DIR_PATH_KEY,srcDir.getAbsolutePath());
		return session;
	}

	@Override
	public RuntimeConfigurationType getConfigurationType() {
		return RuntimeConfigurationType.DEVELOPMENT;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		srcParentDir.remove();
	}
}
