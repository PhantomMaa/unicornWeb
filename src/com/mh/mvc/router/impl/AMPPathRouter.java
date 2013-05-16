package com.mh.mvc.router.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import com.mh.mvc.action.ActionSupport;
import com.mh.mvc.filter.UrlFilter;
import com.mh.mvc.router.IPathRouter;

public class AMPPathRouter implements IPathRouter{
	
	/**
	 * 匹配规则为：
	 * 1、符合/Action/method/param格式，
	 * 2、并且Action在actionMap中的确存在
	 * 3、method在此Action中存在
	 */
	@Override
	public boolean route(String relativeUri, UrlFilter urlFilter) {
		Map<String, String> actionMap = urlFilter.getActionMap();
		Pattern pattern = Pattern.compile("^/\\w+/\\w+/\\S*");
		Matcher matcher = pattern.matcher(relativeUri);
		// 检查url是否是此Router类要处理的，/Action/Method/Param 格式的将会被检查合格，返回true
		if(matcher.matches()) {
			String[] params = relativeUri.split("/");
			String actionName = params[1];
			String fullActionName = actionMap.get(actionName);
			if(null == fullActionName) {
				return false;
			}
			try {
				Class<?> clz = Class.forName(fullActionName);
				ActionSupport actionSupport = (ActionSupport) clz.newInstance();
				String actionMethodName = params[2];
				Method[] methods = clz.getMethods();
				for(int i = 0; i < methods.length; i++) {
					Method method = methods[i];
					String methodName = method.getName();
					if(methodName.equals(actionMethodName)) {
						// 拦截Action/Method/Param方式的请求，并构建ActionSupport类的属性
						if(params.length > 3) {
							this.boxingRequest(urlFilter.getRequest(), params[3]);
						}
						// 在Action开始服务之前，首先注入必须的属性
						this.initActionSupport(actionSupport, urlFilter);
						return this.invokeActionMethod(actionSupport, method, urlFilter);
					}
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	// 调用Action里的方法，抛目标方法执行失败的异常
	private boolean invokeActionMethod(ActionSupport actionSupport, Method method, UrlFilter urlFilter) 
			throws InvocationTargetException {
		try {
			method.invoke(actionSupport, new Object[] {});
			return true;	// 成功执行Action里的方法，才返回成功
		} catch (SecurityException e) {
			e.printStackTrace();
			return false;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return false;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * 把url中得param加入到request的attribute里
	 * @param request
	 * @param parameter
	 */
	private void boxingRequest(HttpServletRequest request, String parameter) {
		String[] parameters = parameter.split("&");
		for (int i = 0; i < parameters.length; i++) {
			String param = parameters[i];
			String[] key_value = param.split("=");
			if(key_value.length == 2) {
				request.setAttribute(key_value[0], key_value[1]);
			}
		}
	}
	/**
	 * 初始化ActionSupport类中所需的request、response、session、application等对象
	 * @param obj
	 * @param urlFilter
	 */
	private void initActionSupport(ActionSupport actionSupport, UrlFilter urlFilter) {
		actionSupport.setRequest(urlFilter.getRequest());
		actionSupport.setResponse(urlFilter.getResponse());
		actionSupport.setSession(urlFilter.getSession());
		actionSupport.setApplication(urlFilter.getApplication());
	}
}
