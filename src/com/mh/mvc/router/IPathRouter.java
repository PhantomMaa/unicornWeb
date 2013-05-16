package com.mh.mvc.router;

import com.mh.mvc.filter.UrlFilter;

public interface IPathRouter {
	/**
	 * 对传递的url进行路由，返回是否拦截到
	 * @param path
	 * @param args
	 * @return
	 */
	public boolean route(String relativeUri, UrlFilter urlFilter);
}
