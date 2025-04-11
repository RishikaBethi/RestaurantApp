package models;

public class SignUp {

    private String firstName;
    private String lastName;
    private String email;
    private String password;

    private SignUp(SignUpBuilder builder)
    {
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.email = builder.email;
        this.password = builder.password;
    }

    public String getFirstName(){
        return this.firstName;
    }

    public String getLastName(){
        return this.lastName;
    }

    public String getEmail(){
        return this.email;
    }

    public String getPassword(){
        return this.password;
    }


    public static class SignUpBuilder{

        private String firstName;
        private String lastName;
        private String email;
        private String password;

        public SignUpBuilder setFirstName(String firstName)
        {
            this.firstName = firstName;
            return this;
        }

        public SignUpBuilder setLastName(String lastName)
        {
            this.lastName = lastName;
            return this;
        }

        public SignUpBuilder setEmail(String email)
        {
            this.email = email;
            return this;
        }

        public SignUpBuilder setPassword(String password)
        {
            this.password = password;
            return this;
        }

        public SignUp build(){
            return new SignUp(this);
        }
    }
}
