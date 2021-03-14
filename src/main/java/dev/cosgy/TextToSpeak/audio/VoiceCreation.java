////////////////////////////////////////////////////////////////////////////////
//  Copyright 2021 Cosgy Dev                                                   /
//                                                                             /
//     Licensed under the Apache License, Version 2.0 (the "License");         /
//     you may not use this file except in compliance with the License.        /
//     You may obtain a copy of the License at                                 /
//                                                                             /
//        http://www.apache.org/licenses/LICENSE-2.0                           /
//                                                                             /
//     Unless required by applicable law or agreed to in writing, software     /
//     distributed under the License is distributed on an "AS IS" BASIS,       /
//     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied./
//     See the License for the specific language governing permissions and     /
//     limitations under the License.                                          /
////////////////////////////////////////////////////////////////////////////////

package dev.cosgy.TextToSpeak.audio;

import dev.cosgy.TextToSpeak.Bot;
import dev.cosgy.TextToSpeak.settings.UserSettings;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.UUID;

public class VoiceCreation {
    private Bot bot;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    String dic = "/var/lib/mecab/dic/open-jtalk/naist-jdic";
    String vDic = "/usr/share/hts-voice";
    ArrayList<String> voices = new ArrayList<>();
    String testVoice = "/usr/share/hts-voice/mei_normal.htsvoice";

    public void Init(Bot bot){
        this.bot = bot;
        FilenameFilter filter = (file, str) -> {
            // 拡張子を指定する
            return str.endsWith("htsvoice");
        };
        vDic = bot.getConfig().getVoiceDirectory();
        dic = bot.getConfig().getDictionary();

        File dir = new File(vDic);
        File[] list = dir.listFiles(filter);
        for (File file : list) {
            voices.add(file.getName().replace(".htsvoice", ""));
        }

        logger.debug("声データ："+ voices.toString());
    }

    public String CreateVoice(User user, String message) {
        UserSettings settings = bot.getUserSettingsManager().getSettings(user.getIdLong());
        Process p = null;
        UUID fileId = UUID.randomUUID();
        String fileName = "wav" + File.separator + fileId + ".wav";

        File file = new File(vDic+ File.separator+ settings.getVoice() + ".htsvoice");
        logger.debug("読み込む声データ:"+ file.toString());

        String[] Command = {"open_jtalk", "-x", dic, "-m", file.toString(), "-ow", fileName, "-r", String.valueOf(settings.getSpeed()), "-jf", String.valueOf(settings.getIntonation()), "-a", String.valueOf(settings.getVoiceQualityA()), "-fm", String.valueOf(settings.getVoiceQualityFm()), CreateTmpText(fileId, message.replaceAll("[\r\n]", " "))};

        Runtime runtime = Runtime.getRuntime(); // ランタイムオブジェクトを取得する
        try {
            p = runtime.exec(Command); // 指定したコマンドを実行する
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            p.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return fileName;
    }

    private String CreateTmpText(UUID id, String message) {
        String tmp_dir = "tmp" + File.separator + id + ".txt";
        try (PrintWriter writer = new PrintWriter(tmp_dir, "UTF-8")) {
            writer.write(message);
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return tmp_dir;
    }

    public ArrayList<String> getVoices(){
        return voices;
    }
}