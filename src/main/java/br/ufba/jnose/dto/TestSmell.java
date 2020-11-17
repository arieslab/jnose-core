package br.ufba.jnose.dto;

import java.io.Serializable;

public class TestSmell implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String method;
    private String range;

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public String toString() {
        return "TestSmell{" +
                "name='" + name + '\'' +
                ", method='" + method + '\'' +
                ", range='[" + range + "]" +'\'' +
                '}';
    }
}
