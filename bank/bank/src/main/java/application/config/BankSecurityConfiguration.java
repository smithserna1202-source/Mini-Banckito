package application.config;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
@Configuration @EnableWebSecurity
public class BankSecurityConfiguration {
 private static final String[][] USERS={
  {"natural-customer","NATURAL_PERSON_CUSTOMER","C001"},
  {"company-customer","COMPANY_CUSTOMER","COMPANY001"},
  {"teller","TELLER","teller01"},
  {"commercial","COMMERCIAL_EMPLOYEE","commercial01"},
  {"company-operator","COMPANY_OPERATIONS_EMPLOYEE","company-operator"},
  {"company-supervisor","COMPANY_SUPERVISOR","supervisor01"},
  {"analyst","INTERNAL_ANALYST","analyst01"},
  {"administrator","ADMINISTRATOR","admin01"}
 };
 @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
 @Bean UserDetailsService userDetailsService(Environment env,PasswordEncoder encoder){
  String shared=env.getProperty("BANK_DEFAULT_PASSWORD");
  if(shared==null||shared.length()<12)throw new IllegalStateException("Set BANK_DEFAULT_PASSWORD to a value of at least 12 characters before starting Mini-Banckito");
  List<UserDetails> users=new ArrayList<>();
  for(String[] d:USERS){
   String username=env.getProperty("bank.security.users."+d[0],d[2]);
   String password=env.getProperty("BANK_PASSWORD_"+d[0].replace('-','_').toUpperCase(),shared);
   users.add(User.withUsername(username).password(encoder.encode(password)).roles(d[1]).build());
  }
  return new InMemoryUserDetailsManager(users);
 }
 @Bean SecurityFilterChain securityFilterChain(HttpSecurity http)throws Exception{
  return http.csrf(csrf->csrf.disable())
   .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
   .authorizeHttpRequests(a->a.requestMatchers("/api/health").permitAll().anyRequest().authenticated())
   .httpBasic(Customizer.withDefaults()).build();
 }
}
