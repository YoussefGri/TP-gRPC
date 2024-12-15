package hotel.web.service.client.ui;

import java.io.BufferedReader;

public class StringInputProcessor extends ComplexUserInputProcessor<String> {

    public StringInputProcessor(BufferedReader inputReader) throws NoSuchMethodException {
        super(inputReader);
    }

    @Override
    protected void setMessage() {
        message = "(char)";
    }

    @Override
    protected void setValidityCriterion() {
        // Pour une chaîne de caractères, nous acceptons tout sauf une chaîne vide.
        isValid = s -> s != null && !s.trim().isEmpty();
    }

    @Override
    protected void setParser() throws NoSuchMethodException {
        // Pas besoin de parser une chaîne de caractères, donc nous renvoyons simplement la chaîne elle-même.
        parser = String.class.getMethod("valueOf", Object.class);
    }
}
