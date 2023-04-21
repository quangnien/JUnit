package com.example.demo.basic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class CalculatorTest {

    Caculator caculator;

    @Test
    public void testMultiply() {
        caculator = new Caculator();
        assertEquals(20, caculator.multiply(4,5));
    }
}
