package notifications;

public class Token {
    //a registration token
    //an id is issued that allows th gcm connection servers to the client app
    //allows it to receive texts


    public Token() {
    }

    String token;

    public Token(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
