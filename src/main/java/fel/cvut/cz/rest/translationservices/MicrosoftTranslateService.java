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
import com.memetix.mst.translate.Translate;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.logging.Logger;

@Path("/microsoft")
@RequestScoped
public class MicrosoftTranslateService implements TranslateService {

    private final String CLIENT_ID = "12345789741852963";
    private final String CLIENT_SECRET = "heslonamicrosoftapi123";

    @Inject
    private Logger log;

    /*
    * Example input JSON:
    * {
    *    "text" : "Hi there!",
    *    "languageFrom" : "en",
    *    "languageTo" : "cs"
    *  }
    *
    *  Output JSON:
    *
    *  {
    *     "text": "Nazdárek!"
    *  }
    *
    * */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public String translate(TranslationRequest request) {


        Translate.setClientId(CLIENT_ID);
        Translate.setClientSecret(CLIENT_SECRET);

        String translatedText = null;
        try {
            translatedText = Translate.execute(request.getText(),
                    Language.fromString(request.getLanguageFrom()),
                    Language.fromString(request.getLanguageTo()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        log.info("Translation: \"" + request.getText() + "\" : " + request.getLanguageFrom() + " -> " + request.getLanguageTo() + " MS translator.");

        return "{ \"text\" : \"" + translatedText + "\"}";
    }

}
