import java.io.Serializable;

public class RegisterRequest implements Serializable {
    private String message;

    public RegisterRequest() {
        this.message = "register";
    }

    public String getMessage() {
        return message;
    }
}
