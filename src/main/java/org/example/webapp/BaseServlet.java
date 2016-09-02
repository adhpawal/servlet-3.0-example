package org.example.webapp;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.annotation.security.DenyAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * <pre>
 *  1. Read WebServlet annotation for base path. 
 *  2. Read all Path annotated methods. 
 *  3. Flatten the path value such as /path/{id}/something will turn into /path/asterisk/something. 
 *  4. Flatten the URL path similarly. 
 *  5. Compare both flattened values
 * 
 * </pre>
 *
 * @author Kailash Bijayananda
 */
public class BaseServlet extends HttpServlet {

    private static final long serialVersionUID = -5588335126697167555L;

    private Action match(List<Action> options, String url) {
        String split[] = url.startsWith("/") ? url.substring(1).split("/") : url.split("/");

        List<Action> matching = options.stream().filter(pi -> pi.getParamTokens().size() == split.length).collect(Collectors.toList());

        for (Action pi : matching) {
            Map<String, String> paramValues = new HashMap<>();
            StringBuffer temp = new StringBuffer();

            for (int i = 0; i < pi.getParamTokens().size(); i++) {
                String ptoken = pi.getParamTokens().get(i);

                if (Action.isDynamic(ptoken)) {
                    temp.append("*/");
                    paramValues.put(Action.stripBraces(ptoken), split[i]);
                } else {
                    temp.append(split[i] + "/");
                }
            }
            String flattendUrl = temp.toString();
            flattendUrl = flattendUrl.endsWith("/") ? flattendUrl.substring(0, flattendUrl.length() - 1) : flattendUrl;

            if (pi.flatten().equals(flattendUrl)) {
                pi.setParamValues(paramValues);
                return pi;
            }
        }

        return null;
    }

    private List<Action> fetchActionInfos(HttpServletRequest request) {
        List<Action> actionInfos = new ArrayList<>();

        // TODO multiple path implementation
        String basePath = this.getClass().getAnnotation(WebServlet.class).value()[0];

        for (Method method : this.getClass().getMethods()) {
            if (method.isAnnotationPresent(Path.class)) {
                switch (request.getMethod()) {
                case HttpMethod.GET:
                    if (method.isAnnotationPresent(GET.class)) {
                        actionInfos.add(new Action(method, basePath, method.getAnnotation(Path.class).value()));
                    }
                    break;
                case HttpMethod.POST:
                    if (method.isAnnotationPresent(POST.class)) {
                        actionInfos.add(new Action(method, basePath, method.getAnnotation(Path.class).value()));
                    }
                    break;
                case HttpMethod.PUT:
                    if (method.isAnnotationPresent(PUT.class)) {
                        actionInfos.add(new Action(method, basePath, method.getAnnotation(Path.class).value()));
                    }
                    break;
                case HttpMethod.DELETE:
                    if (method.isAnnotationPresent(DELETE.class)) {
                        actionInfos.add(new Action(method, basePath, method.getAnnotation(Path.class).value()));
                    }
                    break;
                case HttpMethod.OPTIONS:
                    if (method.isAnnotationPresent(OPTIONS.class)) {
                        actionInfos.add(new Action(method, basePath, method.getAnnotation(Path.class).value()));
                    }
                    break;
                default:
                    throw new NotAllowedException("Method not allowed");
                }
            }
        }
        return actionInfos;
    }

    private void fixContentTypeHeader(Method method, HttpServletResponse resp) {
        if (method.isAnnotationPresent(Produces.class)) {
            resp.setContentType(method.getAnnotation(Produces.class).value()[0]);
        }
    }

    /**
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    private void process(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            List<Action> actionInfos = fetchActionInfos(req);

            String pathInfo = req.getPathInfo() == null ? "" : req.getPathInfo();
            Action action = match(actionInfos, pathInfo);

            if (action == null) {
                throw new WebApplicationException("Not Found", HttpServletResponse.SC_NOT_FOUND);
            }

            authorize(req, action);

            fixContentTypeHeader(action.getMethod(), resp);

            Invoker invoker = new Invoker(action, this, req, resp);

            if (action.shouldValidate()) {
                invoker.validate();
            }

            Object result = invoker.invoke();

            processResponse(action.getMethod(), result, resp.getWriter());
        } catch (Exception e) {
            handle(e, req, resp);
        }
    }

    /**
     * Authorize on the action level
     * 
     * @author <a href="kailashraj@lftechnology.com">Kailash Raj Bijayananda</a>
     * @param req
     * @param action
     */
    private void authorize(HttpServletRequest req, Action action) {
        Method method = action.getMethod();
        if (method.isAnnotationPresent(DenyAll.class)) {
            throw new WebApplicationException(Status.FORBIDDEN.getStatusCode());
        }

        if (method.isAnnotationPresent(RolesAllowed.class)) {
            RolesAllowed rolesAnnotation = method.getAnnotation(RolesAllowed.class);
            String[] rolesAllowed = rolesAnnotation.value();

            for (String role : rolesAllowed) {
                if (req.isUserInRole(role)) {
                    return;
                }
            }
            throw new WebApplicationException(Status.FORBIDDEN.getStatusCode());
        }
    }

    private WebApplicationException findWebApplicationException(Throwable e) {
        if (e == null)
            return null;
        if (e.getCause() instanceof WebApplicationException || e instanceof WebApplicationException) {
            return (WebApplicationException) e;
        }

        return findWebApplicationException(e.getCause());
    }

    /**
     * Try to find instance of WebApplicationException first
     *
     * @author Kailash Bijayananda
     */
    private void handle(Exception e, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

        WebApplicationException webException = findWebApplicationException(e);

        if (webException != null)
            resp.setStatus(webException.getResponse().getStatus());

        String message = webException == null ? e.getMessage() : webException.getMessage();
        if (resp.getContentType() != null && resp.getContentType().equals(MediaType.APPLICATION_JSON)) {
            JsonObjectBuilder errorResponseJson = Json.createObjectBuilder();
            errorResponseJson.add("error", message);
            resp.getWriter().write(errorResponseJson.build().toString());
        } else {
            resp.sendError(resp.getStatus(), message);
        }
    }

    /**
     * @author <a href="mailto:nirajrajbhandari@lftechnology.com">Niraj Rajbhandari</a>
     */
    private void processResponse(Method method, Object result, PrintWriter pw) throws IOException {
        if (method.getReturnType().equals(Void.class)) {
            return;
        }
        if (method.isAnnotationPresent(Produces.class)
                && method.getAnnotation(Produces.class).value()[0].equals(MediaType.APPLICATION_JSON)) {
        } else {
            if (result != null)
                pw.write(result.toString());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        process(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        process(req, resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        process(req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        process(req, resp);
    }
}
