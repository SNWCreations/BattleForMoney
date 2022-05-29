/*
 * This file is part of RunForMoney.
 *
 * RunForMoney is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * RunForMoney is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with RunForMoney. If not, see <https://www.gnu.org/licenses/>.
 */

package snw.rfm;

import org.apache.commons.lang.Validate;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import snw.rfm.api.ItemEventListener;

import java.util.*;

public final class ItemRegistry {
    private static final Map<ItemStack, List<ItemEventListener>> itemEventListenerMap = new HashMap<>();
    private static final Map<String, ItemStack> registeredItems = new HashMap<>();

    public static void registerItem(@NotNull String name, @NotNull ItemStack item) throws IllegalStateException {
        Validate.notNull(name, "Internal name cannot be null");
        Validate.isTrue(!name.isEmpty(), "Internal name cannot be empty");
        Validate.notNull(item, "ItemStack cannot be null");
        Validate.isTrue(!item.getType().isAir(), "You cannot register air item");
        Validate.isTrue(name.split(" ").length == 1, "Internal name cannot have spaces");
        if (registeredItems.get(name) != null) {
            throw new IllegalStateException();
        }
        ItemStack processed = item.clone();
        processed.setAmount(1);
        registeredItems.put(name, processed);
    }

    public static void registerItem(@NotNull String name, @NotNull ItemStack item, @NotNull ItemEventListener listener) throws IllegalStateException {
        registerItem(name, item);
        registerItemEvent(item, listener);
    }

    public static void registerItemEvent(@NotNull ItemStack item, @NotNull ItemEventListener listener) {
        Validate.notNull(item, "ItemStack cannot be null");
        Validate.isTrue(!item.getType().isAir(), "You cannot register listener for air item");
        Validate.notNull(listener, "ItemEventListener cannot be null");
        ItemStack processed = item.clone();
        processed.setAmount(1);
        if (itemEventListenerMap.containsKey(processed)) {
            itemEventListenerMap.get(processed).add(listener);
        } else {
            itemEventListenerMap.put(processed, new ArrayList<>(Collections.singletonList(listener)));
        }
    }

    @NotNull
    public static List<ItemEventListener> getProcessorByItem(@NotNull ItemStack item) {
        Validate.notNull(item, "ItemStack cannot be null");
        ItemStack processed = item.clone();
        processed.setAmount(1);
        return itemEventListenerMap.getOrDefault(processed, new ArrayList<>());
    }

    @NotNull
    public static List<ItemEventListener> getProcessorByName(@NotNull String name) {
        ItemStack requestedItem = getRegisteredItemByName(name);
        Validate.notNull(requestedItem, "Internal name is not registered");
        return getProcessorByItem(requestedItem);
    }

    @NotNull
    public static Set<String> getRegisteredItemNames() {
        return registeredItems.keySet();
    }

    @Nullable
    public static ItemStack getRegisteredItemByName(@NotNull String name) {
        Validate.notNull(name, "Internal name cannot be null");
        return registeredItems.get(name);
    }

    // DANGEROUS METHOD, DEVELOPERS SHOULD NOT USE THIS METHOD
    public static void unregisterItem(@NotNull String itemName) {
        Validate.notNull(itemName, "Do you want to unregister 'null'? Sorry, you can't do that.");
        Validate.isTrue(!itemName.isEmpty(), "No empty item name!");
        registeredItems.remove(itemName);
    }
}
