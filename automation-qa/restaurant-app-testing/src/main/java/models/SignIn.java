package models;

public class SignIn {

    private String email;
    private String password;

    private SignIn(SignInBuilder builder)
    {
        this.email = builder.email;
        this.password = builder.password;
    }

    public String getEmail(){
        return this.email;
    }

    public String getPassword(){
        return this.password;
    }

    public static class SignInBuilder{
        private String email;
        private String password;

        public SignInBuilder setEmail(String email)
        {
            this.email =email;
            return this;
        }

        public SignInBuilder setPassword(String password)
        {
            this.password = password;
            return this;
        }

        public SignIn build(){
            return new SignIn(this);
        }

    }
}
