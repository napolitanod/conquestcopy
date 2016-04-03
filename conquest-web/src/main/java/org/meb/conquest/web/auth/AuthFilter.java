package org.meb.conquest.web.auth;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ocpsoft.pretty.PrettyContext;
import com.ocpsoft.pretty.faces.config.PrettyConfig;
import com.ocpsoft.pretty.faces.config.PrettyConfigurator;
import com.ocpsoft.pretty.faces.config.mapping.UrlMapping;
import com.ocpsoft.pretty.faces.util.PrettyURLBuilder;

@WebFilter(filterName = "AuthFilter", urlPatterns = { "/en/deck", "/pl/deck", "/de/deck", "/fr/deck", "/en/deck/*", "/pl/deck/*",
		"/de/deck/*", "/fr/deck/*" })
public class AuthFilter implements Filter {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(AuthFilter.class);

	@Inject
	private AuthToken authUser;

	public void init(FilterConfig config) throws ServletException {
	}

	public void destroy() {
	}

	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;

		if (authUser.isSignedIn()) {
			chain.doFilter(request, response);
		} else {
			StringBuilder origin = new StringBuilder();
			origin.append(request.getRequestURI());
			if (request.getQueryString() != null) {
				origin.append("/").append(request.getQueryString());
			}
			request.getSession().setAttribute("origin", origin.toString());

			ServletContext servletContext = request.getServletContext();
			PrettyConfig prettyConfig = (PrettyConfig) servletContext.getAttribute(PrettyContext.CONFIG_KEY);
			if (prettyConfig == null) {
				PrettyConfigurator configurator = new PrettyConfigurator(servletContext);
				configurator.configure();
				prettyConfig = configurator.getConfig();
			}

			PrettyURLBuilder builder = new PrettyURLBuilder();
			UrlMapping mapping = prettyConfig.getMappingById("signin");
			Pattern pattern = Pattern.compile("/(en|pl|de|fr)(/.*|$).*", Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(origin.toString());
			String language = null;
			if (matcher.matches()) {
				language = matcher.group(1);
			} else {
				language = "en";
			}
			String targetUrl = builder.build(mapping, true, new Object[] { language });
			targetUrl = response.encodeRedirectURL(targetUrl);
			response.sendRedirect(targetUrl);
		}
	}
}
