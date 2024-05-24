package org.flbohn.conversationsimulator_backend.conversation.types;

public enum Grade {
    ONE(1.0),
    TWO(2.0),
    THREE(3.0),
    FOUR(4.0),
    FIVE(5.0),
    SIX(6.0),
    UNRATED(-1.0);

    private final double numericValue;

    Grade(double numericValue) {
        this.numericValue = numericValue;
    }

    public double getNumericValue() {
        return numericValue;
    }
}
