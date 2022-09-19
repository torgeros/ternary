package torgeros.connect3;

import torgeros.connect3.Board.Field;

public class Util {
    public static Field[][] copyFieldArray(Field[][] original) {
        Field[][] copy = new Field[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = original[i].clone();
        }
        return copy;
    }
}
