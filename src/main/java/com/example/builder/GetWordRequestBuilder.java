package com.example.builder;

import java.math.BigInteger;

import com.example.generated.NumberToWords;

public class GetWordRequestBuilder {

    public NumberToWords getWords(String number) {
    	NumberToWords request = new NumberToWords();
        request.setUbiNum(new BigInteger(number));

        return request;
    }
}
