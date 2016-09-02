package org.example.webapp;

/**
 *
 * @author Kailash Bijayananda
 *
 */
public class FormEntity {

    @FormParam("name")
    private String name;

    @FormParam("custom")
    private Integer age;

    @FormParam("custom")
    private CustomType type;

    public CustomType getType() {
        return type;
    }

    public void setType(CustomType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return name + " - " + age + " - " + type;
    }
}
