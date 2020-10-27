package pt.smartthought.url.shortner.api.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import pt.smartthought.url.shortner.metrics.MetricsFacade;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;


@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class MetricsFilter extends OncePerRequestFilter {
	static final String UNKNOWN_HTTP_STATUS_CODE = "";

	private final MetricsFacade metrics;
	private final RequestMappingHandlerMapping requestMappingHandlerMapping;

	@Override
	public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
		throws IOException, ServletException {

		String endpoint = getTemplatedServletPath(request, request.getServletPath());
		String method = request.getMethod();

		long start = System.currentTimeMillis();
		try {
			chain.doFilter(request, response);
		}finally {
			long finish = System.currentTimeMillis();
			long timeElapsed = finish - start;
			String status = getStatusCode(response);

			metrics.registerRequest(endpoint, method, status);
			metrics.registerRequestLatency(endpoint, method, status, timeElapsed);
		}
	}

	private String getStatusCode(ServletResponse servletResponse) {
		if (!(servletResponse instanceof HttpServletResponse)) {
			return UNKNOWN_HTTP_STATUS_CODE;
		}

		return Integer.toString(((HttpServletResponse) servletResponse).getStatus());
	}

	private String getTemplatedServletPath(HttpServletRequest request, String servletPath) {
		try {
			HandlerExecutionChain requestHandler = requestMappingHandlerMapping.getHandler(request);
			if (requestHandler != null) {
				requestMappingHandlerMapping.getHandler(request).getHandler();
				return request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE).toString();
			}
		} catch(Exception e){
			log.debug("Failed to retrieve handler method.");
		}

		return attemptManualTemplatedPathLookup(request).orElse(servletPath);
	}

	private Optional<String> attemptManualTemplatedPathLookup(HttpServletRequest request){
		Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping.getHandlerMethods();
		for (Object value : handlerMethods.keySet()) {
			PatternsRequestCondition patternsRequestCondition = ((RequestMappingInfo) value).getPatternsCondition().getMatchingCondition(request);
			if (patternsRequestCondition != null){
				String pattern = patternsRequestCondition.getPatterns().iterator().next();
				return Optional.ofNullable(pattern);
			}
		}

		return Optional.empty();
	}
}
