package com.mh.mvc.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.mh.mvc.router.IPathRouter;
import com.mh.mvc.router.impl.AMPPathRouter;
import com.mh.mvc.util.XMLReader;

public class UrlFilter implements Filter {

	private Map<String, String> actionMap;
	private List<IPathRouter> routerList;
	private ServletContext application;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private HttpSession session;

	
	public Map<String, String> getActionMap() {
		return actionMap;
	}

	public ServletContext getApplication() {
		return application;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public HttpSession getSession() {
		return session;
	}

	@Override
	public void destroy() {
		System.out.println("UrlFilter destroy");
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		String path = request.getContextPath();
		String uri = request.getRequestURI();
		String relativeUri = uri.substring(path.length(), uri.length());
		this.request = request;
		this.session = request.getSession();
		this.response = (HttpServletResponse) servletResponse;
		// 用户自定义的Router优先级最高，url先通过用户定义的
		Iterator<IPathRouter> iterator = routerList.iterator();
		while(iterator.hasNext()) {
			IPathRouter router = iterator.next();
			if(router.route(relativeUri, this)) {
				return ;
			}
		}
		// 拦截不到继续访问
		filterChain.doFilter(servletRequest, servletResponse);
	}

	public static void main(String[] args) {
		String ddd = "/Users/mahang/workspaces/my_project/.metadata/.plugins/org.eclipse.wst.server.core/tmp0/wtpwebapps/MacCMS2/WEB-INF/classes/";
		String sss = ddd + "unicorn-config.xml";
		System.out.println(sss);
	}
	@Override
	public void init(FilterConfig config) throws ServletException {
		System.out.println("UrlFilter init actions ...");
		application = config.getServletContext();
		String loadPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
		String loadFilePath = loadPath + "unicorn-config.xml";
		System.out.println(loadFilePath);
		ArrayList<String> actions = XMLReader.getNodeValues(loadFilePath, "actions");
		this.initActions(actions);
		ArrayList<String> routers = XMLReader.getNodeValues(loadFilePath, "routers");
		this.initRouters(routers);
	}

	private void initActions(ArrayList<String> actions) {
		actionMap = new HashMap<String, String>();
		for (int i = 0; i < actions.size(); i++) {
			String actionName = actions.get(i);
			String liteActionName = actionName.substring(actionName.lastIndexOf(".") + 1, actionName.length());
			actionMap.put(liteActionName, actionName);
			System.out.println("UrlFilter init action " + actionName);
		}
	}

	private void initRouters(ArrayList<String> routers) {
		routerList = new ArrayList<IPathRouter>();
		for (int i = 0; i < routers.size(); i++) {
			String routerName = routers.get(i);
			try {
				Class<?> clz = Class.forName(routers.get(i));
				// 单例模式通过方法获取对象实例
				IPathRouter router = (IPathRouter) clz.newInstance();
				routerList.add(router);
				System.out.println("UrlFilter init router " + routerName);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			}
		}
		// 最后把框架默认的Router加入进来
		routerList.add(new AMPPathRouter());
	}
}
