package android.vic.MapManager;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Created by yangzy on 2017/7/2.
 */
class NoRepeatRandom {
    private static Set<Integer> integerSet = new HashSet<>();
    private static Random random = new Random();

    static int gen() {
        int num = random.nextInt();
        while (integerSet.contains(num)) {
            num = random.nextInt();
        }
        integerSet.add(num);
        return num;
    }
}
