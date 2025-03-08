package com.crossmint.challenge.model;

import lombok.Getter;

import java.util.Map;

@Getter
public class Soloon extends AbstractAstralObject {

    private final String color;
    private static final String SOLOON_PATH = "/soloon";

    public Soloon(int row, int column, String color) {
        super(row, column, SOLOON_PATH);
        this.color = color;
    }

    @Override
    protected void addSpecificAttributes(Map<String, Object> body) {
        body.put("color", color);
    }
}
