/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fel.cvut.cz.rest.translationservices;


import com.memetix.mst.language.Language;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.net.ssl.HttpsURLConnection;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Logger;

@Path("/yandex")
@RequestScoped
public class YandexTranslateService implements TranslateService {

    //Encoding type
    private final String ENCODING = "UTF-8";
    private final String PARAM_API_KEY = "key=",
            PARAM_LANG_PAIR = "&lang=",
            PARAM_TEXT = "&text=";
    private final String TRANSLATION_LABEL = "text";
    private final String SERVICE_URL = "https://translate.yandex.net/api/v1.5/tr.json/translate?";
    private final String API_KEY = "trnsl.1.1.20160430T100502Z.384c7901968beda9.1f87b6385937d72bce12e068ac45ca3f3024d494";
    private String referrer;

    @Inject
    private Logger log;

    /*
    *   Example input JSON:
    *   {
    *      "text" : "Hi there!",
    *      "languageFrom" : "en",
    *      "languageTo" : "cs"
    *   }
    *
    *   Example of output JSON:
    *   {
    *      "text" : "Nazdárek!",
    *   }
    *
    * */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public String translate(TranslationRequest request) {

        String translation;
        try {
            translation = execute("The quick brown fox jumps over the lazy dog.", Language.valueOf(request.getLanguageFrom()), Language.valueOf(request.getLanguageTo()));
            log.info("Translation: \"" + request.getText() + "\" : \"" + translation + "\" : " + request.getLanguageFrom() + " -> " + request.getLanguageTo() + " with Yandex translator.");
            return "{\"text\" : \"" + translation + "\"}";
        } catch (Exception e) {
            log.info("Couldn't translate \"" + request.getText() + "\" : " + request.getLanguageFrom() + " -> " + request.getLanguageTo() + " with Yandex translator.");
            return "{\"error\" : \"Could not translate!\"}";
        }
    }

    /**
     * Translates text from a given Language to another given Language using Yandex.
     *
     * @param text The String to translate.
     * @param from The language code to translate from.
     * @param to   The language code to translate to.
     * @return The translated String.
     * @throws Exception on error.
     */
    public String execute(final String text, final Language from, final Language to) throws Exception {
        validateServiceState(text);
        final String params =
                PARAM_API_KEY + URLEncoder.encode(API_KEY, ENCODING)
                        + PARAM_LANG_PAIR + URLEncoder.encode(from.toString(), ENCODING) + URLEncoder.encode("-", ENCODING) + URLEncoder.encode(to.toString(), ENCODING)
                        + PARAM_TEXT + URLEncoder.encode(text, ENCODING);
        final URL url = new URL(SERVICE_URL + params);
        return retrievePropArrString(url, TRANSLATION_LABEL).trim();
    }

    private void validateServiceState(final String text) throws Exception {
        final int byteLength = text.getBytes(ENCODING).length;
        if (byteLength > 10240) { // TODO What is the maximum text length allowable for Yandex?
            throw new RuntimeException("TEXT_TOO_LARGE");
        }
        validateServiceState();
    }

    /**
     * Forms an HTTPS request, sends it using GET method and returns the result of the request as a String.
     *
     * @param url The URL to query for a String response.
     * @return The translated String.
     * @throws Exception on error.
     */
    private String retrieveResponse(final URL url) throws Exception {
        final HttpsURLConnection uc = (HttpsURLConnection) url.openConnection();
        if (referrer != null)
            uc.setRequestProperty("referer", referrer);
        uc.setRequestProperty("Content-Type", "text/plain; charset=" + ENCODING);
        uc.setRequestProperty("Accept-Charset", ENCODING);
        uc.setRequestMethod("GET");

        try {
            final int responseCode = uc.getResponseCode();
            final String result = inputStreamToString(uc.getInputStream());
            if (responseCode != 200) {
                throw new Exception("Error from Yandex API: " + result);
            }
            return result;
        } finally {
            if (uc != null) {
                uc.disconnect();
            }
        }
    }

    /**
     * Forms a request, sends it using the GET method and returns the value with the given label from the
     * resulting JSON response.
     */
    private String retrievePropString(final URL url, final String jsonValProperty) throws Exception {
        final String response = retrieveResponse(url);
        JSONObject jsonObj = (JSONObject) JSONValue.parse(response);
        return jsonObj.get(jsonValProperty).toString();
    }

    /**
     * Forms a request, sends it using the GET method and returns the contents of the array of strings
     * with the given label, with multiple strings concatenated.
     */
    private String retrievePropArrString(final URL url, final String jsonValProperty) throws Exception {
        final String response = retrieveResponse(url);
        String[] translationArr = jsonObjValToStringArr(response, jsonValProperty);
        String combinedTranslations = "";
        for (String s : translationArr) {
            combinedTranslations += s;
        }
        return combinedTranslations.trim();
    }

    // Helper method to parse a JSONObject containing an array of Strings with the given label.
    private String[] jsonObjValToStringArr(final String inputString, final String subObjPropertyName) throws Exception {
        JSONObject jsonObj = (JSONObject) JSONValue.parse(inputString);
        JSONArray jsonArr = (JSONArray) jsonObj.get(subObjPropertyName);
        return jsonArrToStringArr(jsonArr.toJSONString(), null);
    }

    // Helper method to parse a JSONArray. Reads an array of JSONObjects and returns a String Array
    // containing the toString() of the desired property. If propertyName is null, just return the String value.
    private String[] jsonArrToStringArr(final String inputString, final String propertyName) throws Exception {
        final JSONArray jsonArr = (JSONArray) JSONValue.parse(inputString);
        String[] values = new String[jsonArr.size()];

        int i = 0;
        for (Object obj : jsonArr) {
            if (propertyName != null && propertyName.length() != 0) {
                final JSONObject json = (JSONObject) obj;
                if (json.containsKey(propertyName)) {
                    values[i] = json.get(propertyName).toString();
                }
            } else {
                values[i] = obj.toString();
            }
            i++;
        }
        return values;
    }

    /**
     * Reads an InputStream and returns its contents as a String.
     * Also effects rate control.
     *
     * @param inputStream The InputStream to read from.
     * @return The contents of the InputStream as a String.
     * @throws Exception on error.
     */
    private String inputStreamToString(final InputStream inputStream) throws Exception {
        final StringBuilder outputBuilder = new StringBuilder();

        try {
            String string;
            if (inputStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, ENCODING));
                while (null != (string = reader.readLine())) {
                    // TODO Can we remove this?
                    // Need to strip the Unicode Zero-width Non-breaking Space. For some reason, the Microsoft AJAX
                    // services prepend this to every response
                    outputBuilder.append(string.replaceAll("\uFEFF", ""));
                }
            }
        } catch (Exception ex) {
            throw new Exception("[yandex-translator-api] Error reading translation stream.", ex);
        }
        return outputBuilder.toString();
    }

    //Check if ready to make request, if not, throw a RuntimeException
    private void validateServiceState() throws Exception {
        if (API_KEY == null || API_KEY.length() < 27) {
            throw new RuntimeException("INVALID_API_KEY - Please set the API Key with your Yandex API Key");
        }
    }

    public void setReferrer(String referrer) {
        this.referrer = referrer;
    }

}
