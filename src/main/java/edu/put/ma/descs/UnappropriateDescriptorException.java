package edu.put.ma.descs;

public class UnappropriateDescriptorException extends Exception {

    private static final long serialVersionUID = 6963147184330404534L;

    public UnappropriateDescriptorException() {
    }

    public UnappropriateDescriptorException(String message) {
        super(message);
    }

    public UnappropriateDescriptorException(Throwable cause) {
        super(cause);
    }

    public UnappropriateDescriptorException(String message, Throwable cause) {
        super(message, cause);
    }

}
