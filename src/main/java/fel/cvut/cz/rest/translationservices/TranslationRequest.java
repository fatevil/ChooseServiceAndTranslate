package fel.cvut.cz.rest.translationservices;

/**
 * Created by marek on 29.4.16.
 */
public class TranslationRequest {

    private String languageFrom;

    private String languageTo;

    private String text;

    public TranslationRequest(String languageFrom, String languageTo, String text) {
        this.languageFrom = languageFrom;
        this.languageTo = languageTo;
        this.text = text;
    }

    public TranslationRequest() {
    }

    public String getLanguageFrom() {
        return languageFrom;
    }

    public void setLanguageFrom(String languageFrom) {
        this.languageFrom = languageFrom;
    }

    public String getLanguageTo() {
        return languageTo;
    }

    public void setLanguageTo(String languageTo) {
        this.languageTo = languageTo;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
