package snw.bfm.util;

import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class Util {

    private Util() {}

    public static void removeAllPotionEffect(Player player) {
        Arrays.asList(PotionEffectType.values()).forEach(player::removePotionEffect);
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortDescend(@NotNull Map<K, V> map) {
        Validate.notNull(map);
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort((o1, o2) -> -((o1.getValue()).compareTo(o2.getValue())));

        Map<K, V> returnMap = new LinkedHashMap<>();
        list.forEach(entry -> returnMap.put(entry.getKey(), entry.getValue()));
        return returnMap;
    }
}
