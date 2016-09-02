package org.example.webapp;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Kailash Bijayananda
 *
 */
public class AutoParam {
    private Parameter parameter;
    private Action action;
    private HttpServletRequest request;
    private HttpServletResponse response;

    private ParamReader paramReader;

    public AutoParam(Parameter parameter, Action action, HttpServletRequest request, HttpServletResponse response) {
        this.parameter = parameter;
        this.action = action;
        this.request = request;
        this.response = response;

        this.paramReader = new ParamReader(action, request, response);
    }

    public Action getAction() {
        return action;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public Object readValue() {
        Object value = null;

        if (parameter.isAnnotationPresent(PathParam.class)) {
            String paramName = parameter.getAnnotation(PathParam.class).value();
            value = paramReader.pathParamValue(paramName, parameter.getType());
        } else if (parameter.isAnnotationPresent(FormParam.class)) {
            String paramName = parameter.getAnnotation(FormParam.class).value();
            value = paramReader.formParamValue(paramName, parameter.getType());
        } else if (parameter.isAnnotationPresent(Context.class)) {
            value = paramReader.contextValue(parameter.getType());
        } else if (parameter.isAnnotationPresent(BeanParam.class)) {
            value = beanParamValue();
        }
        return value;
    }

    private Object beanParamValue() {
        try {
            Class type = parameter.getType();
            Object bean = type.newInstance();

            Field[] fields = type.getDeclaredFields();

            for (Field field : fields) {
                if (field.isAnnotationPresent(PathParam.class) || field.isAnnotationPresent(FormParam.class)) {
                    AutoBeanProperty beanProp = new AutoBeanProperty(this, field, bean);

                    Object value = beanProp.readValue();

                    PropertyDescriptor pd = new PropertyDescriptor(field.getName(), type);
                    pd.getWriteMethod().invoke(bean, value);
                }
            }

            return bean;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
