# ChangeLog

## [0.11.6] - 2025-05-07
### Added
- Unit tests for all the components using vitest with code coverage
- API's Integration with frontend after backend is migrated to springboot and kubernetes(updated to new api endpoint)

### Fixed 
- Resolved bug related to change password validation message
- Resolved bug related to not display previous filtered tables when an invalid guest number is given
- Resolved bug related to registration first name field validation

### Notes
- Deployed in AWS S3 bucket(http://team-7-frontend-bucket.s3-website-ap-southeast-2.amazonaws.com)
- No new features are added
- Created documentation about the application(README.md)

## [0.9.3] - 2025-04-17
### Added
- Feature for a user to display all the items in the menu with sorting and filtering
- Feature for a user to see detailed description about a particular dish
- API's Integration to view all dishes in menu with sorting and to display particular dish details

- Feature for user to give feedback for their experience for service and culinary
- API's Integration to create and update feedbacks

- Feature for a user to update their profile information and change password
- API's Integration for updating profile data and to change password 

- Feature for a waiter to view all the reservations assigned to them 
- Feature for a waiter to create a new reservation,postpone a reservation and cancel it 
- API's Integration for CRUD operations related to waiter reservations

### Fixed
- Resolved bug to make time slots interactive and to display profile validation errors.
- Resolved bug for missing default values and redundant inputs during reservation flow.
- Resolved bug for wrong default rating in feedback and for missing date picker for waiter reservations.

### Notes
- Deployed in AWS S3 bucket(http://team-7-frontend-bucket.s3-website-ap-southeast-2.amazonaws.com)
- Made the application Responsive

## [0.5.0] - 2025-04-03
### Added 
- Feature for Sign-up and Sign-in
- API's Integration for sign-in and sign-up

- Feature for a customer landing page/home page which shows most popular dishes and different locations
- Feature for navbar before and after a user is logged in
- API's Integration for displaying most popular dishes and different locations

- Feature for a customer to browse through different locations of the restaurant
- Feature to display speciality dishes when customer navigates to a specific location
- Feature to display feedbacks for a location with pagination with sorting
- API's Integration for a particular location details,speciality dishes and feedbacks of the particular location

- Feature for a customer to go through the available tables and slots for making a reservation
- API Integration for table booking with different filters 

- Feature for a customer to make a reservation,edit the reservation and cancel it 
- Feature to display all the reservations for the customer
- API's Integration for CRUD operations related to customer reservations 

### Notes
- Deployed in AWS S3 bucket(http://team-7-frontend-bucket.s3-website-ap-southeast-2.amazonaws.com)
- Made the application Responsive