package com.silver.controller;

import com.silver.model.Usuario;
import com.silver.service.UsuarioService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final UsuarioService usuarioService = new UsuarioService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String usuario = req.getParameter("usuario");
        String password = req.getParameter("password");

        UsuarioService.ResultadoLogin resultado = usuarioService.login(usuario, password);

        if (resultado.exito) {
            Usuario u = resultado.usuario;
            HttpSession session = req.getSession(true);
            session.setAttribute("usuarioId", u.getId());
            session.setAttribute("usuarioNombre", u.getNombre());
            session.setAttribute("usuarioRol", u.getRol());
            session.setMaxInactiveInterval(60 * 60); // 1 hora

            resp.sendRedirect(req.getContextPath() + "/dashboard");
        } else {
            req.setAttribute("error", resultado.mensaje);
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
        }
    }
}
