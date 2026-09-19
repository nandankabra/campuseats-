package com.example.campuseatshttp.config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuthFilter
        implements Filter {

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain)
            throws IOException,
            ServletException {

        HttpServletRequest req =
                (HttpServletRequest) request;

        String auth =
                req.getHeader(
                        "Authorization");

        if (auth == null ||
                !auth.startsWith(
                        "Bearer ")) {

            HttpServletResponse res =
                    (HttpServletResponse)
                            response;

            res.sendError(401);

            return;
        }

        chain.doFilter(
                request,
                response);
    }
}