import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
public class VerifyPwd {
  public static void main(String[] args) {
    var e = new BCryptPasswordEncoder();
    var h = "$2a$10$auoJXA771voMcGFrdjwo..qHqvayiOMEsciuMguUi0g9SeHnADHQS";
    for (String p : new String[]{"password","123456","admin","6419966wx"}) {
      System.out.println(p + " -> " + e.matches(p, h));
    }
  }
}
