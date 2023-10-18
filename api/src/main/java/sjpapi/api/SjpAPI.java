package sjpapi.api;

import org.json.JSONException;

import java.io.IOException;

public class SjpAPI {

    private SjpAPI() {
    }

    @SuppressWarnings("unused")
    public static String getWord(String word) throws IOException, JSONException {
        String json;
        String wordWithoutSpecialChar;
        wordWithoutSpecialChar = StringUtils.deleteSpecialChar(word);
        json = SjpHelper.translateFromCurlToJSON(CurlHelper.getOutputFromCurl(wordWithoutSpecialChar));
        return json;
    }

    @SuppressWarnings({"unused", "UnusedReturnValue"})
    public static SjpJSON getWordJSON(String word) throws IOException, JSONException {
        return SjpHelper.translateFromJSONToSjpJSON(CurlHelper.getOutputFromCurl(word));
    }

    public static void main(String[] args) {
        try {
            getWordJSON("kot");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
