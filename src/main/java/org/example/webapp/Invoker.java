package org.example.webapp;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;

/**
 *
 * @author Kailash Bijayananda
 *
 */
public class Invoker {
    private BaseServlet servlet;
    private Action action;
    private HttpServletRequest request;
    private HttpServletResponse response;

    public Invoker(Action action, BaseServlet servlet, HttpServletRequest request, HttpServletResponse response) {
        this.action = action;
        this.request = request;
        this.response = response;
        this.servlet = servlet;
    }

    private List<Parameter> readActionParameters() {
        return Arrays.asList(action.getMethod().getParameters());
    }

    private List<Object> readActionValues() {
        List<Object> values = new ArrayList<>();

        for (Parameter p : readActionParameters()) {
            values.add(readValue(p));
        }
        return values;
    }

    private Object readValue(Parameter p) {
        AutoParam autoParam = new AutoParam(p, action, request, response);
        return autoParam.readValue();
    }

    public Object invoke() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Object result = action.getMethod().invoke(servlet, readActionValues().toArray());
        return result;
    }

    public void validate() {
        for (Parameter p : readActionParameters()) {
            if (p.isAnnotationPresent(BeanParam.class)) {

            } else {
                Object value = readValue(p);

                if (p.isAnnotationPresent(NotNull.class)) {
                    if (value == null) {
                        String msg = p.getAnnotation(NotNull.class).message();
                        throw new ValidationException(msg);
                    }
                }
            }
        }
    }
}
