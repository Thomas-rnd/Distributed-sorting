import java.io.Serializable;

public class SamplingKeyRequest implements Serializable {
    private String message;

    public RegisterRequest() {
        this.message = "samplingKeyRequest";
    }

    public String getMessage() {
        return message;
    }
}
