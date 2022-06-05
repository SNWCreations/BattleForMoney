/*
 * This file is part of BattleForMoney.
 *
 * BattleForMoney is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * BattleForMoney is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with BattleForMoney. If not, see <https://www.gnu.org/licenses/>.
 */

package snw.bfm.util;

import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;

// 2022/4/5
public final class PlaceHolderString {
    private String value;

    public PlaceHolderString(@NotNull String value) {
        Validate.notNull(value, "Need a string!");
        this.value = value;
    }

    public PlaceHolderString replaceArgument(String argName, Object value) {
        this.value = this.value.replace("$" + argName + "$", String.valueOf(value));
        return this;
    }

    public PlaceHolderString replaceTranslate() {
        this.value = LanguageSupport.replacePlaceHolder(this.value);
        return this;
    }

    @Override
    public String toString() {
        return value;
    }
}
