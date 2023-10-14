package sjpapi.api;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static sjpapi.api.StringUtils.htmlToPolishLetter;

public class SjpWrapper {

    public String getWrapped(String curlOutput) throws JSONException, UnsupportedEncodingException {
        List<List<String>> wordsFromCurlOutput = regexForInfo(curlOutput);
        return translateArrayToJSON(wordsFromCurlOutput);
    }

    public SjpJSON getWrappedJSON(String curlOutput) throws JSONException, UnsupportedEncodingException {
        List<List<String>> wordsFromCurlOutput = regexForInfo(curlOutput);
        return translateArrayToSjpJSON(wordsFromCurlOutput);
    }

    private List<List<String>> regexForInfo(String curlOutput) {

        List<List<String>> wrappedCurl = new ArrayList<>();
        if (Boolean.TRUE.equals(isInDictionary(curlOutput))) {

            final String regex = "<h1[^>]*>(.+?)<\\/h1>.<p[^>]*>(.+?)<.+?(?=.*)href=\"\\/(.+?)\".+?znaczenie.+?<p[^>]*>(.+?)<\\/p>";
            final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
            final Matcher matcher = pattern.matcher(curlOutput);

            wrappedCurl = new ArrayList<>();
            while (matcher.find()) {
                List<String> definitionOfWord = new ArrayList<>();
                for (int j = 1; j <= matcher.groupCount(); j++) {
                    definitionOfWord.add(matcher.group(j));
                }
                wrappedCurl.add(definitionOfWord);
            }
        }
        return wrappedCurl;

    }

    private Boolean isInDictionary(String curlOutput) {

        return !curlOutput.contains("nie występuje w słowniku");
    }

    private String translateArrayToJSON(List<List<String>> wordsFromCurlOutput) throws JSONException, UnsupportedEncodingException {
        String allWrappedWord;
        JSONObject jsonObject = new JSONObject();
        if (wordsFromCurlOutput.isEmpty()) {
            jsonObject.put("name", "-1");
            jsonObject.put("count", "-1");
            jsonObject.put("variant", "-1");
            jsonObject.put("canBeUsed", false);
            jsonObject.put("meaning", "-1");
        } else {
            jsonObject.put("name", wordsFromCurlOutput.get(0).get(0));
            jsonObject.put("count", wordsFromCurlOutput.size());
            for (int i = 0; i < wordsFromCurlOutput.size(); i++) {
                jsonObject.put("canBeUsed" + "[" + i + "]", translateCanBeUsed(wordsFromCurlOutput.get(i).get(1)));
                jsonObject.put("variant" + "[" + i + "]", htmlToPolishLetter(wordsFromCurlOutput.get(i).get(2)));
                jsonObject.put("meaning" + "[" + i + "]", wrappedDescription(wordsFromCurlOutput.get(i).get(3)));
            }
        }
        allWrappedWord = jsonObject.toString();
        return allWrappedWord;
    }

    public SjpJSON translateArrayToSjpJSON(List<List<String>> wordsFromCurlOutput) throws JSONException, UnsupportedEncodingException {
        SjpJSON sjpJSON = SjpJSON.empty();
        if (wordsFromCurlOutput.isEmpty()) {
            return sjpJSON;
        } else {
            sjpJSON = new SjpJSON(
                    wordsFromCurlOutput.get(0).get(0),
                    wordsFromCurlOutput.size(),
                    wordsFromCurlOutput.stream()
                            .map(word -> word.get(1))
                            .map(this::translateCanBeUsed)
                            .toList(),
                    wordsFromCurlOutput.stream()
                            .map(word -> word.get(2))
                            .map(word -> {
                                try {
                                    return htmlToPolishLetter(word);
                                } catch (UnsupportedEncodingException e) {
                                    throw new RuntimeException(e);
                                }
                            })
                            .toList(),
                    wordsFromCurlOutput.stream()
                            .map(word -> word.get(3))
                            .map(this::wrappedDescription)
                            .toList()
            );
        }
        return sjpJSON;
    }

    private Boolean translateCanBeUsed(String oneParamOfWordFromCurl) {
        return !oneParamOfWordFromCurl.startsWith("niedopuszczalne");
    }


    private boolean isDescriptionGoodFormat(String description) {
        return !StringUtils.hasSpecyficHTMLTags(description) && !description.contentEquals("KOMENTARZE:") && !description.contentEquals("POWIĄZANE HASŁA:");
    }

    private String wrappedDescription(String description) {
        String desc = description;
        if (!isDescriptionGoodFormat(desc)) {
            desc = "BAD FORMAT";
        } else {
            desc = changeBrTagOnNewLine(desc);
            desc = StringUtils.unescapeHTML(desc, 0);
        }

        return desc;
    }

    private String changeBrTagOnNewLine(String descritption) {
        descritption = descritption.replaceAll("(?i)<br */?>", "\n");
        return descritption;
    }


}
