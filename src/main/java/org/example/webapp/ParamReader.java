package org.example.webapp;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Kailash Bijayananda
 *
 */
public class ParamReader {
    private Action action;
    private HttpServletRequest request;
    private HttpServletResponse response;

    public ParamReader(Action action, HttpServletRequest request, HttpServletResponse response) {
        this.action = action;
        this.request = request;
        this.response = response;
    }

    public Object pathParamValue(String paramName, Class type) {
        String value = action.getParamValues().get(paramName);
        Object obj = TypeCaster.cast(value, type);

        return obj;
    }

    public Object formParamValue(String paramName, Class type) {
        String value = request.getParameter(paramName);
        Object obj = TypeCaster.cast(value, type);

        return obj;
    }

    public Object contextValue(Class type) {
        if (type.equals(HttpServletRequest.class))
            return request;
        else if (type.equals(HttpServletResponse.class))
            return response;
        return null;
    }
}
