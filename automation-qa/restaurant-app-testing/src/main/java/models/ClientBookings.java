package models;


import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClientBookings {
    public String locationId;
    public String tableNumber;
    public String date;
    public String guestsNumber;
    public String timeFrom;
    public String timeTo;

    private ClientBookings(ClientBookingsBuilder builder){
        this.locationId = builder.locationId;
        this.tableNumber = builder.tableNumber;
        this.date = builder.date;
        this.guestsNumber = builder.guestsNumber;
        this.timeFrom = builder.timeFrom;
        this.timeTo = builder.timeTo;
    }

    public String getTimeTo() {
        return timeTo;
    }

    public String getTimeFrom() {
        return timeFrom;
    }

    public String getGuestsNumber() {
        return guestsNumber;
    }

    public String getDate() {
        return date;
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public String getLocationId() {
        return locationId;
    }

    public static class ClientBookingsBuilder{
        private String locationId;
        private String tableNumber;
        private String date;
        private String guestsNumber;
        private String timeFrom;
        private String timeTo;

        public ClientBookingsBuilder setLocationId(String locationId){
            this.locationId = locationId;
            return this;
        }

        public ClientBookingsBuilder setTableNumber(String tableNumber){
            this.tableNumber = tableNumber;
            return this;
        }

        public ClientBookingsBuilder setDate(String date){
            this.date = date;
            return this;
        }

        public ClientBookingsBuilder setGuestsNumber(String guestsNumber){
            this.guestsNumber = guestsNumber;
            return this;
        }

        public ClientBookingsBuilder setTimeFrom(String timeFrom){
            this.timeFrom = timeFrom;
            return this;
        }

        public ClientBookingsBuilder setTimeTo(String timeTo){
            this.timeTo = timeTo;
            return this;
        }

        public ClientBookings build(){
            return new ClientBookings(this);
        }
    }

    @Override
    public String toString() {
        return locationId + " " + tableNumber + " " + date + " " + guestsNumber + " " + timeFrom + " " + timeTo;
    }
}
