import java.io.Serializable;

public class RegisterReply implements Serializable {
    private String message;

    public RegisterReply() {
        this.message = "Hello";
    }

    public String getMessage() {
        return message;
    }
}
