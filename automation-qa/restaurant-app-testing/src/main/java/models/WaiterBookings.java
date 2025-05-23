package models;

public class WaiterBookings {
    private String clientType;
    private String customerEmail;
    private String date;
    private String guestsNumber;
    private String locationId;
    private String tableNumber;
    private String timeFrom;
    private String timeTo;

    private WaiterBookings(WaiterBookingsBuilder builder){
        this.clientType = builder.clientType;
        this.customerEmail = builder.customerEmail;
        this.date = builder.date;
        this.guestsNumber = builder.guestNumber;
        this.locationId = builder.locationId;
        this.tableNumber = builder.tableNumber;
        this.timeFrom = builder.timeFrom;
        this.timeTo = builder.timeTo;
    }

    public String getClientType() {
        return clientType;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getDate() {
        return date;
    }

    public String getGuestsNumber() {
        return guestsNumber;
    }

    public String getLocationId() {
        return locationId;
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public String getTimeFrom() {
        return timeFrom;
    }

    public String getTimeTo() {
        return timeTo;
    }

    public void setClientType(String clientType) {
        this.clientType = clientType;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setGuestsNumber(String guestNumber) {
        this.guestsNumber = guestNumber;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }

    public void setTimeFrom(String timeFrom) {
        this.timeFrom = timeFrom;
    }

    public void setTimeTo(String timeTo) {
        this.timeTo = timeTo;
    }

    public static class WaiterBookingsBuilder{
        private String clientType;
        private String customerEmail;
        private String date;
        private String guestNumber;
        private String locationId;
        private String tableNumber;
        private String timeFrom;
        private String timeTo;

        public WaiterBookingsBuilder setClientType(String clientType){
            if(clientType != null)
                this.clientType = clientType;
            return this;
        }

        public WaiterBookingsBuilder setCustomerEmail(String customerEmail){
            if(customerEmail != null)
                this.customerEmail = customerEmail;
            return this;
        }

        public WaiterBookingsBuilder setDate(String date){
            if(date != null)
                this.date = date;
            return this;
        }

        public WaiterBookingsBuilder setGuestsNumber(String guestsNumber){
            if(guestsNumber != null)
                this.guestNumber = guestsNumber;
            return this;
        }

        public WaiterBookingsBuilder setLocationId(String locationId){
            if(locationId != null)
                this.locationId = locationId;
            return this;
        }

        public WaiterBookingsBuilder setTableNumber(String tableNumber){
            if(tableNumber == null){
                this.tableNumber = "";
                return this;
            }
            this.tableNumber = tableNumber;
            return this;
        }

        public WaiterBookingsBuilder setTimeFrom(String timeFrom){
            if(timeFrom != null)
                this.timeFrom = timeFrom;
            return this;
        }

        public WaiterBookingsBuilder setTimeTo(String timeTo){
            if(timeTo != null)
                this.timeTo = timeTo;
            return this;
        }

        public WaiterBookings build(){
            return new WaiterBookings(this);
        }
    }

    @Override
    public String toString() {
        return clientType + " " + customerEmail + " " + date + " " + guestsNumber + " " +
                locationId + " " + tableNumber + " " + timeFrom + " " + timeTo;
    }
}
