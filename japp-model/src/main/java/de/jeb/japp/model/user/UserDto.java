package de.jeb.japp.model.user;

public class UserDto {
    private String fullName;
    private String email;

    public UserDto() {
    }

    public UserDto(String fullName, String email) {
        this.fullName = fullName;
        this.email = email;
    }

    public static UserDto from(User user) {
        return new UserDto(user.getFullName(), user.getEmail());
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
