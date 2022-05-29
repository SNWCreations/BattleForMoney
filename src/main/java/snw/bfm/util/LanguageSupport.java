/*
 * This file is part of RunForMoney.
 *
 * RunForMoney is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * RunForMoney is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with RunForMoney. If not, see <https://www.gnu.org/licenses/>.
 */

package snw.bfm.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;

import snw.bfm.BattleForMoney;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

// The i18n support
// by SNWCreations, 2022/4/3
public final class LanguageSupport {
    private static final Map<String, String> langStrings = new HashMap<>();

    // private constructor.
    private LanguageSupport() {
        throw new UnsupportedOperationException("No snw.rfm.util.LanguageSupport instances for you!");
    }

    public static String replacePlaceHolder(String stringToProcess) {
        Validate.notNull(stringToProcess, "No string to process?");
        String result = stringToProcess;
        for (Map.Entry<String, String> entry : langStrings.entrySet()) {
            result = result.replace("$" + entry.getKey() + "$", entry.getValue());
        }
        return result;
    }

    public static void loadLanguage(String localeCode) {
        Validate.notNull(localeCode, "Please provide a locale code. see http://www.lingoes.net/en/translator/langcode.htm .");
        InputStream i = BattleForMoney.getInstance().getResource("lang/" + localeCode + ".json");
        if (i == null) {
            BattleForMoney.getInstance().getLogger().warning("Cannot load language \"" + localeCode + "\" because it does not exists.");
            return;
        }

        langStrings.clear();
        JsonElement conf = new JsonParser().parse(new InputStreamReader(i, StandardCharsets.UTF_8));
        for (Map.Entry<String, JsonElement> data : conf.getAsJsonObject().entrySet()) {
            if (data.getValue().isJsonPrimitive() && data.getValue().getAsJsonPrimitive().isString()) {
                langStrings.put(data.getKey(), data.getValue().getAsString());
            } else {
                BattleForMoney.getInstance().getLogger().warning("Cannot load key-value pair \"" + data.getKey() + "\" because the value is not a String.");
            }
        }
    }

    @NotNull
    public static String getTranslation(String translateKey) {
        Validate.notNull(translateKey, "No key?");
        return langStrings.getOrDefault(translateKey, translateKey);
    }
}
