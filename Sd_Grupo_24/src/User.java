import java.util.Objects;

public class User {

    private String name;
    private String email;
    private String password;
    private int level;


    public User(String name, String email, String password, int level) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.level = level;

    }

    public User(String... userFields) {
        this.name = userFields[0];
        this.email = userFields[1];
        this.password = userFields[2];
        this.level = Integer.parseInt(userFields[3]);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final User other = (User) obj;

        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.email, other.email)) {
            return false;
        }
        if (!Objects.equals(this.password, other.password)) {
            return false;
        }
        return Objects.equals(this.level, other.level);
    }

    @Override
    public String toString() {
        return "User{\n Name = " + name + "\n Email = " + email + "\n Password = " + password + "\n Level = "
                + level + " \n}";
    }

    public String toCSV() {
        return String.join(",", name, email, password, String.valueOf(level));
    }

}