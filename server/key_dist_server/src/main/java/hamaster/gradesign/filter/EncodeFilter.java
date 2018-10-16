package hamaster.gradesign.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;

/**
 * 处理中文乱码
 * @author <a href="mailto:wangyeee@gmail.com">Wang Ye</a>
 * @deprecated JSF???
 */
@WebFilter(
	urlPatterns = { "/*" }, 
	initParams = {@WebInitParam(name = "encoding", value = "UTF-8")}
)
@Deprecated
public class EncodeFilter implements Filter {

	private String encoding;

    public EncodeFilter() {
    }

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig config) throws ServletException {
		this.encoding = config.getInitParameter("encoding");
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		request.setCharacterEncoding(encoding);
		response.setContentType(new StringBuilder("text/html;charset=").append(encoding).toString());  
		response.setCharacterEncoding(encoding);
		chain.doFilter(request, response);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {
		this.encoding = null;
	}
}
