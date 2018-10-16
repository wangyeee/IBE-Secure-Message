package hamaster.gradesign.filter;

import hamaster.gradesign.jsf.bean.ViewUserBean;

import java.io.IOException;

import javax.faces.FactoryFinder;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet Filter implementation class SessionFilter
 */
@WebFilter({ "/SessionFilter", "/id.xhtml" })
public class SessionFilter implements Filter {

    /**
     * Default constructor. 
     */
    public SessionFilter() {
    }

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		FacesContext context = getFacesContext(request, response);
		Object viewUserBean = context.getApplication().getExpressionFactory().createValueExpression(context.getELContext(), "#{viewUserBean}", ViewUserBean.class).getValue(context.getELContext());
		if (viewUserBean == null || ((ViewUserBean) viewUserBean).getUser() == null) {
			((HttpServletResponse) response).sendRedirect("login.xhtml");
			return;
		}
		chain.doFilter(request, response);
	}

	public FacesContext getFacesContext(ServletRequest request, ServletResponse response) {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		if (facesContext != null) {
			return facesContext;
		}
		FacesContextFactory contextFactory = (FacesContextFactory) FactoryFinder.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
		LifecycleFactory lifecycleFactory = (LifecycleFactory) FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
		Lifecycle lifecycle = lifecycleFactory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);
		ServletContext servletContext = ((HttpServletRequest) request).getSession().getServletContext();
		facesContext = contextFactory.getFacesContext(servletContext, request, response, lifecycle);
		InnerFacesContext.setFacesContextAsCurrentInstance(facesContext);
		if (null == facesContext.getViewRoot()) {
			facesContext.setViewRoot(new UIViewRoot());
		}
		return facesContext;
	}

	private abstract static class InnerFacesContext extends FacesContext {
		protected static void setFacesContextAsCurrentInstance(FacesContext facesContext) {
			FacesContext.setCurrentInstance(facesContext);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig config) throws ServletException {
	}
}
