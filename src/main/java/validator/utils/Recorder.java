package validator.utils;

public class Recorder<T> {

    private T t;
    private RecordingObject recorder;

    public Recorder(T t, RecordingObject recorder) {
        this.t = t;
        this.recorder = recorder;
    }

    public String getCurrentPropertyName() {
        return toPropertyName(recorder.getCurrentPropertyName());
    }

    private static String toPropertyName(String getterName) {
        if (!getterName.matches("^get.*")) {
            throw new IllegalArgumentException("Called a method that is not a getter " + getterName);
        }

        String firstLetterLowercase = getterName.substring(3, 4)
                                                .toLowerCase();

        if (getterName.length() == 4) {
            return firstLetterLowercase;
        }
        else {
            return firstLetterLowercase + getterName.substring(4);
        }
    }

    public T getObject() {
        return t;
    }
}
