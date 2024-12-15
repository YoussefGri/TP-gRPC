package hotel.web.service.client.ui;

import java.io.BufferedReader;

public class IntegerInputProcessor extends ComplexUserInputProcessor<Integer>{
    public IntegerInputProcessor(BufferedReader inputReader) throws NoSuchMethodException {
        super(inputReader);
    }

    @Override
    protected void setMessage() {
        message = "(nombre entier) ";
    }

    @Override
    protected void setValidityCriterion() {
        isValid = s -> {
            try {
                Integer.parseInt(s);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        };
    }

    @Override
    protected void setParser() {
        try {
            parser = Integer.class.getMethod("parseInt", String.class);
        } catch (SecurityException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
}
