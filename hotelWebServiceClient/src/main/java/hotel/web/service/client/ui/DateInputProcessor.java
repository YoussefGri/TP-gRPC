package hotel.web.service.client.ui;

import java.io.BufferedReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

public class DateInputProcessor extends ComplexUserInputProcessor<String> {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$"); // Vérification du format YYYY-MM-DD

    public DateInputProcessor(BufferedReader inputReader) throws NoSuchMethodException {
        super(inputReader);
    }

    @Override
    protected void setMessage() {
        message = "(format : YYYY-MM-DD) ";
    }

    @Override
    protected void setValidityCriterion() {
        isValid = s -> {
            // Vérifier si la chaîne de date correspond au format YYYY-MM-DD
            if (!DATE_PATTERN.matcher(s).matches()) {
                System.out.println("Format de date invalide. Utilisez le format YYYY-MM-DD.");
                return false;
            }

            // Vérifier si la date saisie est valide
            try {
                DATE_FORMAT.parse(s);
                return true;
            } catch (ParseException e) {
                System.out.println("La date saisie n'est pas valide.");
                return false;
            }
        };
    }

    @Override
    protected void setParser() throws NoSuchMethodException {
        // Pas besoin de parser, renvoie simplement la chaîne de date
        parser = String.class.getMethod("valueOf", Object.class);
    }
}
