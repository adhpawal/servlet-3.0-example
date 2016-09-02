package org.example.webapp;

import java.lang.reflect.Field;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Kailash Bijayananda
 *
 */
public class AutoBeanProperty {
    private AutoParam autoParam;
    private Field field;
    private Object bean;

    private Action action;
    private HttpServletRequest request;
    private HttpServletResponse response;

    public AutoBeanProperty(AutoParam autoParam, Field field, Object bean) {
        this.autoParam = autoParam;
        this.field = field;
        this.bean = bean;

        this.action = autoParam.getAction();
        this.request = autoParam.getRequest();
        this.response = autoParam.getResponse();
    }

    public Object readValue() {
        ParamReader reader = new ParamReader(action, request, response);
        Object value = null;

        if (field.isAnnotationPresent(PathParam.class)) {
            String paramName = field.getAnnotation(PathParam.class).value();
            value = reader.pathParamValue(paramName, field.getType());
        } else if (field.isAnnotationPresent(FormParam.class)) {
            String paramName = field.getAnnotation(FormParam.class).value();
            value = reader.formParamValue(paramName, field.getType());
        } else if (field.isAnnotationPresent(Context.class)) {
            value = reader.contextValue(field.getType());
        }

        return value;
    }

    public AutoParam getAutoParam() {
        return autoParam;
    }

    public void setAutoParam(AutoParam autoParam) {
        this.autoParam = autoParam;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public Object getBean() {
        return bean;
    }

    public void setBean(Object bean) {
        this.bean = bean;
    }

}
