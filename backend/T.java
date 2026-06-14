import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
public class T { public static void main(String[] a) {
  var e = new BCryptPasswordEncoder();
  var h = "$2a$10$auoJXA771voMcGFrdjwo..qHqvayiOMEsciuMguUi0g9SeHnADHQS";
  System.out.println(e.matches("password", h));
}}
