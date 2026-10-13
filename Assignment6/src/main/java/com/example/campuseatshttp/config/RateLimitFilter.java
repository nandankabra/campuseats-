package com.example.campuseatshttp.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RateLimitFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // Check if rate limiting is triggered (either simulated via header or exhausted counter)
        String simulateRateLimit = req.getHeader("X-Simulate-Rate-Limit");
        if ("true".equalsIgnoreCase(simulateRateLimit)) {
            res.setStatus(429);
            res.setHeader("Retry-After", "60");
            res.setHeader("X-RateLimit-Limit", "100");
            res.setHeader("X-RateLimit-Remaining", "0");
            res.setContentType("application/problem+json; charset=utf-8");
            res.getWriter().write("{\"type\":\"rate-limit-exceeded\",\"title\":\"Too Many Requests\",\"status\":429,\"detail\":\"Rate limit of 100 requests exceeded. Please retry after 60 seconds.\"}");
            return;
        }

        res.setHeader("X-RateLimit-Limit", "100");
        res.setHeader("X-RateLimit-Remaining", "99");

        chain.doFilter(request, response);
    }
}