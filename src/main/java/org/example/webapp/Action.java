package org.example.webapp;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

/**
 * @author Kailash Bijayananda
 */
public class Action {
    private String path;
    private Method method;
    private List<String> paramTokens;
    private Map<String, String> paramValues;

    public Action(Method method, String basePath, String path) {
        this.method = method;

        basePath = basePath.endsWith("/") ? basePath : basePath + "/";
        path = path.startsWith("/") ? path.substring(1) : path;
        this.path = basePath + path;
        this.paramTokens = tokenize(path);
    }

    public boolean shouldValidate() {
        return method.isAnnotationPresent(Valid.class);
    }

    /**
     * will change path/{xxx}/param to path/asterisk/param could not write an asterisk followed by a slash in a multi line comment. lol
     */
    public String flatten() {
        StringBuffer sb = new StringBuffer();
        for (String pt : paramTokens) {
            sb.append(isDynamic(pt) ? "*/" : pt + "/");
        }
        String temp = sb.toString();
        return temp.endsWith("/") ? temp.substring(0, temp.length() - 1) : temp;
    }

    private List<String> tokenize(String pathInfo) {
        List<String> ptokens = new ArrayList<>();

        String[] split = pathInfo.split("/");

        int i = pathInfo.startsWith("/") ? 1 : 0;

        for (; i < split.length; i++) {
            String token = split[i];
            ptokens.add(token);
        }

        return ptokens;
    }

    public Map<String, String> getParamValues() {
        return paramValues;
    }

    public void setParamValues(Map<String, String> paramValues) {
        this.paramValues = paramValues;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<String> getParamTokens() {
        return paramTokens;
    }

    public void setParamTokens(List<String> paramTokens) {
        this.paramTokens = paramTokens;
    }

    public static boolean isDynamic(String token) {
        if (token.startsWith("{") && token.endsWith("}"))
            return true;
        return false;
    }

    public static String stripBraces(String token) {
        token = token.substring(1);
        token = token.substring(0, token.length() - 1);
        return token;
    }
}
