package models;

public class ProfileUpdate {

    private String base64encodedImage;
    private String firstName;
    private String lastName;

    private ProfileUpdate(ProfileUpdateBuilder builder)
    {
        this.base64encodedImage = builder.base64encodedImage;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
    }

    public String getBase64encodedImage() {
        return base64encodedImage;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public static class ProfileUpdateBuilder{

        private String base64encodedImage;
        private String firstName;
        private String lastName;

        public ProfileUpdateBuilder setBase64encodedImage(String image)
        {
            this.base64encodedImage = image;
            return this;
        }

        public ProfileUpdateBuilder setFirstName(String firstName)
        {
            this.firstName = firstName;
            return this;
        }

        public ProfileUpdateBuilder setLastName(String lastName)
        {
            this.lastName = lastName;
            return this;
        }

        public ProfileUpdate build(){
            return new ProfileUpdate(this);
        }
    }
}

