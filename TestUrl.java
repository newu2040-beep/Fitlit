import java.net.URI;
public class TestUrl {
    public static void main(String[] args) throws Exception {
        URI uri = new URI("https://generativelanguage.googleapis.com/").resolve("v1beta/models/gemini-1.5-flash:generateContent");
        System.out.println(uri.toString());
    }
}
