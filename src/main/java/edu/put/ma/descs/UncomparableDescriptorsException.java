package edu.put.ma.descs;

public class UncomparableDescriptorsException extends Exception {

    private static final long serialVersionUID = -8290023113847964277L;

    public UncomparableDescriptorsException() {
    }

    public UncomparableDescriptorsException(final String message) {
        super(message);
    }

    public UncomparableDescriptorsException(final Throwable cause) {
        super(cause);
    }

    public UncomparableDescriptorsException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
