package me.aandster.servlets;

import me.aandster.html.LoginPageHtmlGen;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        /* Response Set-Up */
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        /* HTML Generator Set-Up */
        LoginPageHtmlGen htmlGen = new LoginPageHtmlGen();

        /* Output page HTML from generator */
        out.print(htmlGen.generate());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}
