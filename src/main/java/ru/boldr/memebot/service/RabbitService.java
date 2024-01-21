package ru.boldr.memebot.service;


import lombok.AllArgsConstructor;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;


public class RabbitService {
    @AllArgsConstructor
    static class Type<T> {
        T name;
       
    }


    public static void main(String[] argv) throws Exception {
        System.out.println(checkNumberIsPrime(11));



    }

    public static int reverse(int numb) {
        int local = 0;
        while (numb > 0) {
            local *= 10;
            int pp =  numb % 10;
            local += pp;
            numb /= 10;

        }
        return local;
    }

    public static int sqrt(int numb) {
        for (int i = 1; i < numb; i++) {
            if (i*i == numb) return i;
            if (i*i > numb) return i-1;
        }
        return 0;
    }

    public static <T, R> R get(T item, Function<T, R> function) {
        return function.apply(item);
    }

    public static boolean checkNumberIsPrime(int number) {
        int factors = 0;
        int counter = 1;

        while(counter <= number) {
            if(number % counter == 0) {
                factors++;
            }
            counter++;
        }
        return (factors == 2);
    }
}


