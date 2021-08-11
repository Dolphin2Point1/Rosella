package me.hydos.rosella.util;

public class BitUtils {

    public static int countBits(int number) {
        int count = 0;
        while(number != 0) {
            number &= (number - 1);
            count++;
        }
        return count;
    }

    public static int countBits(long number) {
        int count = 0;
        while(number != 0) {
            number &= (number - 1);
            count++;
        }
        return count;
    }
}
