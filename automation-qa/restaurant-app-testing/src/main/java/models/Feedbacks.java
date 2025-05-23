package models;

public class Feedbacks {

    private String cuisineComment;
    private String cuisineRating;
    private String reservationId;
    private String serviceComment;
    private String serviceRating;

    private Feedbacks(FeedbacksBuilder builder)
    {
        this.cuisineComment = builder.cuisineComment;
        this.cuisineRating = builder.cuisineRating;
        this.reservationId = builder.reservationId;
        this.serviceComment = builder.serviceComment;
        this.serviceRating = builder.serviceRating;
    }

    public String getCuisineComment() {
        return cuisineComment;
    }

    public String getCuisineRating() {
        return cuisineRating;
    }

    public String getReservationId() {
        return reservationId;
    }

    public String getServiceComment() {
        return serviceComment;
    }

    public String getServiceRating() {
        return serviceRating;
    }

    public static class FeedbacksBuilder
    {
        private String cuisineComment;
        private String cuisineRating;
        private String reservationId;
        private String serviceComment;
        private String serviceRating;


        public FeedbacksBuilder setCuisineComment(String cuisineComment) {
            this.cuisineComment = cuisineComment;
            return this;
        }

        public FeedbacksBuilder setCuisineRating(String cuisineRating) {
            this.cuisineRating = cuisineRating;
            return this;
        }

        public FeedbacksBuilder setReservationId(String reservationId) {
            this.reservationId = reservationId;
            return this;
        }

        public FeedbacksBuilder setServiceComment(String serviceComment) {
            this.serviceComment = serviceComment;
            return this;
        }

        public FeedbacksBuilder setServiceRating(String serviceRating) {
            this.serviceRating = serviceRating;
            return this;
        }

        public Feedbacks build(){
            return new Feedbacks(this);
        }
    }
}
