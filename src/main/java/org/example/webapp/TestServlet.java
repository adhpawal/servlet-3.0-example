package org.example.webapp;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

/**
 * @author Kailash Bijayananda
 */
@WebServlet("/test/*")
public class TestServlet extends BaseServlet {
    private static final long serialVersionUID = 8305045744394194422L;

    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public void index(@Context HttpServletRequest req, @Context HttpServletResponse resp) throws ServletException, IOException {
        String sessionValue = (String) req.getSession().getAttribute("sess");
        if (sessionValue == null) {
            req.getSession().setAttribute("sess", "jpt session value test");
        }
        resp.getWriter().write("from index method, session value : " + sessionValue);
    }

    @Path("/hello")
    @GET
    public void hello(@Context HttpServletRequest req, @Context HttpServletResponse resp) throws ServletException, IOException {
        // resp.getWriter().write("from hello method");

        req.setAttribute("name", "Adele");
        req.getRequestDispatcher("/WEB-INF/hello.jsp").forward(req, resp);
    }

    @Path("/hello")
    @POST
    public void helloPost(@Context HttpServletRequest req, @Context HttpServletResponse resp) throws ServletException, IOException {
        resp.getWriter().write("hello post request, naam is : " + req.getParameter("naam"));

    }

    @Path("/a/pretty/effing/long/url/profile/{user}/{age}")
    @GET
    public void profile(@PathParam("user") String user, @PathParam("age") Integer age, @Context HttpServletRequest req,
            @Context HttpServletResponse resp) throws ServletException, IOException {

        req.setAttribute("name", user);
        req.setAttribute("age", age);

        req.getRequestDispatcher("/WEB-INF/hello.jsp").forward(req, resp);
    }
}
