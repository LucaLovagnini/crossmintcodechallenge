package com.crossmint.challenge.model;

import lombok.Getter;

import java.util.Map;

@Getter
public class Cometh extends AbstractAstralObject {

    private final ComethDirection direction;
    private static final String COMETH_PATH = "/comeths";

    public Cometh(int row, int column, ComethDirection direction) {
        super(row, column, COMETH_PATH);
        this.direction = direction;
    }

    @Override
    protected void addSpecificAttributes(Map<String, Object> body) {
        body.put("direction", direction.getApiValue());
    }
}
