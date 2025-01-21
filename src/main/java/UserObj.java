
import java.util.*;

public class UserObj {
    private String username;
    private String password;
    private String email;
    private String rollno;
    private String Dob;

    public String getUsername() {
        return username;
    }

    public String getDob(){return Dob;}
    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getRollno() {
        return rollno;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setDob(String Dob){this.Dob=Dob;}
    public void setPassword(String password) {
        this.password= password;
    }

    public void setEmail(String email) {
        this.email= email;
    }

    public void setRollno(String rollno) {
        this.rollno= rollno;
    }
}



