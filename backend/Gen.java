import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
public class Gen {
  public static void main(String[] a) {
    BCryptPasswordEncoder e = new BCryptPasswordEncoder();
    String hash = e.encode("Test@123456");
    System.out.println(hash);
    System.out.println("match:" + e.matches("Test@123456", "$2a$10$auoJXA771voMcGFrdjwo..qHqvayiOMEsciuMguUi0g9SeHnADHQS"));
  }
}
