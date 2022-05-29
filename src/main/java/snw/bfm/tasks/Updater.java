/*
 * This file is part of RunForMoney.
 *
 * RunForMoney is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * RunForMoney is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with RunForMoney. If not, see <https://www.gnu.org/licenses/>.
 */

package snw.bfm.tasks;

import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import snw.bfm.BattleForMoney;
import snw.bfm.util.LanguageSupport;
import snw.bfm.util.PlaceHolderString;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Updater extends Thread {

    public Updater() {
        super("RFM Update Checker");
    }

    @Override
    public void run() {
        log(LanguageSupport.getTranslation("update.start"));

        StringBuilder response;
        try {
            URL url = new URL("https://api.github.com/repos/SNWCreations/RunForMoney/releases/latest");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);
            InputStream in = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            logFail(LanguageSupport.getTranslation("update.failed.connect"));
            e.printStackTrace();
            return;
        }

        String recievedVersion;
        try {
            recievedVersion = new JsonParser().parse(response.toString()).getAsJsonObject().get("name").getAsString();
        } catch (JsonParseException e) {
            logFail(LanguageSupport.getTranslation("update.failed.invalid_data"));
            e.printStackTrace();
            return;
        }

        if (recievedVersion.startsWith("v")) {
            recievedVersion = recievedVersion.substring(1);
        }

        int versionDifference = getVersionDifference(recievedVersion);
        if (versionDifference == -1) {
            log(
                    new PlaceHolderString(LanguageSupport.getTranslation("update.result.old"))
                            .replaceArgument("currentVersion", BattleForMoney.getInstance().getDescription().getVersion())
                            .replaceArgument("latestVersion", recievedVersion)
                            .toString()
            );
        } else if (versionDifference == 0) {
            log(LanguageSupport.getTranslation("update.result.uptodate"));
        } else if (versionDifference == 1) {
            log(LanguageSupport.getTranslation("update.result.future"));
        } else {
            log(
                    new PlaceHolderString(LanguageSupport.getTranslation("update.result.impossible"))
                            .replaceArgument("result", versionDifference)
                            .replaceArgument("latestVersion", recievedVersion)
                            .replaceArgument("currentVersion", BattleForMoney.getInstance().getDescription().getVersion())
                            .toString()
            );
        }

    }

    private void log(Level level, String message) {
        Logger.getLogger("RunForMoney").log(level, "[RunForMoney] " + message);
    }

    private void log(String message) {
        log(Level.INFO, message);
    }

    private void logFail(String message) {
        log(Level.WARNING, LanguageSupport.getTranslation("update.failed.message_prefix") + message);
    }

    // -1 = 过期
    // 0 = 最新版
    // 1 = 未来版
    private static int getVersionDifference(String versionToCompare) {
        String current = BattleForMoney.getInstance().getDescription().getVersion();
        if (current.equals(versionToCompare))
            return 0;
        if (current.split("\\.").length != 3 || versionToCompare.split("\\.").length != 3)
            return -1;

        int curMaj = Integer.parseInt(current.split("\\.")[0]);
        int curMin = Integer.parseInt(current.split("\\.")[1]);
        String curPatch = current.split("\\.")[2];

        int relMaj = Integer.parseInt(versionToCompare.split("\\.")[0]);
        int relMin = Integer.parseInt(versionToCompare.split("\\.")[1]);
        String relPatch = versionToCompare.split("\\.")[2];

        if (curMaj < relMaj)
            return -1;
        if (curMaj > relMaj)
            return 1;
        if (curMin < relMin)
            return -1;
        if (curMin > relMin)
            return 1;

        // 以下比较是否是 SNAPSHOT 版，虽然我平常不发 SNAPSHOT 。。。但总要考虑
        int curPatchN = Integer.parseInt(curPatch.split("-")[0]);
        int relPatchN = Integer.parseInt(relPatch.split("-")[0]);
        if (curPatchN < relPatchN)
            return -1;
        if (curPatchN > relPatchN)
            return 1;
        if (!relPatch.contains("-") && curPatch.contains("-"))
            return -1;
        if (relPatch.contains("-") && curPatch.contains("-"))
            return 0;

        return 1;
    }
}
