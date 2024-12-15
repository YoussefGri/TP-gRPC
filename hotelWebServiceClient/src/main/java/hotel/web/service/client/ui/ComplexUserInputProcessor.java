package hotel.web.service.client.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Predicate;

public abstract class ComplexUserInputProcessor <T>{
    protected String message;
    protected BufferedReader inputReader;
    protected Method parser;
    protected Predicate<String> isValid;
    protected T parameter;

    public ComplexUserInputProcessor (BufferedReader inputReader) throws NoSuchMethodException {
        this.inputReader = inputReader;
        setMessage();
        setParser();
        setValidityCriterion();
    }


    protected abstract void setMessage();

    protected abstract void setValidityCriterion();

    protected abstract void setParser() throws NoSuchMethodException;

    public T process() throws IOException {
        System.out.println(message);
        String userInput = inputReader.readLine();

        while (!isValid.test(userInput)) {
            System.err.println("Désolé, mauvaise entrée. Essayez à nouveau.");
            System.out.println();
            System.out.println(message);
            userInput = inputReader.readLine();
        }

        try {
            parameter = (T) parser.invoke(null, userInput);
        }
        catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e){
            e.printStackTrace();
        }
        return parameter;
    }
}
