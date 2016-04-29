package fel.cvut.cz.rest.translationservices;

/**
 * Created by marek on 29.4.16.
 */
public class TranslationRequest {

    private String language;

    private String text;

    public TranslationRequest(String language, String text) {
        this.language = language;
        this.text = text;
    }

    public TranslationRequest() {
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
