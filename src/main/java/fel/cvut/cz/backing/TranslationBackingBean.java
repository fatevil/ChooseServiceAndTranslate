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
package fel.cvut.cz.backing;

import fel.cvut.cz.qualifier.GoogleAPI;
import fel.cvut.cz.qualifier.YandexAPI;
import fel.cvut.cz.translationservices.TranslateService;

import javax.inject.Inject;
import javax.inject.Named;

@Named("translation")
public class TranslationBackingBean {

    /*
     * Both of these injections is of the same base type: TranslationService, however, CDI is using the qualifiers to help
     * narrow which of the two implementations should be injected.
     */

    @Inject
    @GoogleAPI
    private TranslateService googleTranslator;

    @Inject
    @YandexAPI
    private TranslateService yandexTranslator;

    public String translateWithGoogle(String message, String language) {
        return googleTranslator.translate(message, language);
    }

    public String translateWithYandex(String message, String language) {
        return yandexTranslator.translate(message, language);
    }
}
