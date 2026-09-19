package com.example.campuseatshttp.config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RateLimitFilter
        implements Filter {

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain)
            throws IOException,
            ServletException {

        HttpServletResponse res =
                (HttpServletResponse)
                        response;

        res.setHeader(
                "X-RateLimit-Limit",
                "100");

        res.setHeader(
                "X-RateLimit-Remaining",
                "99");

        chain.doFilter(
                request,
                response);
    }
}