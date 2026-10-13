package com.example.campuseatshttp.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String auth = req.getHeader("Authorization");

        if (auth == null || !auth.startsWith("Bearer ")) {
            res.setStatus(401);
            res.setContentType("application/problem+json; charset=utf-8");
            res.getWriter().write("{\"type\":\"unauthorized\",\"title\":\"Unauthorized\",\"status\":401,\"detail\":\"Authentication required. Provide a valid Bearer token in the Authorization header.\"}");
            return;
        }

        chain.doFilter(request, response);
    }
}