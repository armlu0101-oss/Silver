package com.silver.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Filtro de sesion: protege todas las paginas del sistema excepto
 * login y recursos estaticos. Si no hay sesion activa, redirige al login.
 */
@WebFilter("/*")
public class SesionFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String path = req.getRequestURI().substring(req.getContextPath().length());

        boolean esPublico = path.equals("/login.jsp")
                || path.equals("/login")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/assets/");

        HttpSession session = req.getSession(false);
        boolean autenticado = session != null && session.getAttribute("usuarioId") != null;

        if (esPublico || autenticado) {
            chain.doFilter(request, response);
        } else {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
        }
    }
}
