package hotel;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

class Customer {
    private final int id;
    private final String name;
    private final String contact;
    private final String email;
    private int loyaltyPoints;
    private int totalStays;
    private String tier;

    Customer(int id, String name, String contact, String email) {
        this.id = id;
        this.name = name;
        this.contact = contact;
        this.email = email;
        this.tier = "Bronze";
    }

    void awardPoints(double amountSpent) {
        int earned = (int) (amountSpent * getLoyaltyMultiplier() / 100.0);
        loyaltyPoints += earned;
        totalStays++;
        updateTier();
    }

    double redeemPoints(int points) {
        int used = Math.max(0, Math.min(points, loyaltyPoints));
        loyaltyPoints -= used;
        updateTier();
        return used;
    }

    double getLoyaltyMultiplier() {
        return switch (tier) {
            case "Gold" -> 2.0;
            case "Silver" -> 1.5;
            default -> 1.0;
        };
    }

    private void updateTier() {
        if (loyaltyPoints >= 1500) {
            tier = "Gold";
        } else if (loyaltyPoints >= 500) {
            tier = "Silver";
        } else {
            tier = "Bronze";
        }
    }

    int getId() {
        return id;
    }

    String getName() {
        return name;
    }

    String getContact() {
        return contact;
    }

    String getEmail() {
        return email;
    }

    int getLoyaltyPoints() {
        return loyaltyPoints;
    }

    int getTotalStays() {
        return totalStays;
    }

    String getTier() {
        return tier;
    }

    void restoreState(int loyaltyPoints, int totalStays, String tier) {
        this.loyaltyPoints = loyaltyPoints;
        this.totalStays = totalStays;
        this.tier = tier;
    }
}

class Booking {
    static final String ACTIVE = "Active";
    static final String CHECKOUT = "Checked Out";
    static final String DUE = "Due";
    static final String PAID = "Paid";

    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final int bookingId;
    private final Customer customer;
    private final Room room;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private final int nights;
    private double discount;
    private double total;
    private String status;
    private String paymentStatus;
    private String paymentMethod;

    Booking(int bookingId, Customer customer, Room room, LocalDate checkIn, LocalDate checkOut, int pointsToRedeem) {
        this.bookingId = bookingId;
        this.customer = customer;
        this.room = room;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.nights = Math.max(1, (int) ChronoUnit.DAYS.between(checkIn, checkOut));
        double baseBill = room.calculateTariff(this.nights);
        this.discount = Math.min(customer.redeemPoints(pointsToRedeem), baseBill * 0.20);
        this.total = baseBill - discount;
        this.status = ACTIVE;
        this.paymentStatus = DUE;
        this.paymentMethod = "Pending";
    }

    void checkout() {
        customer.awardPoints(total);
        room.setBooked(false);
        room.setGuestName("");
        room.setDemandScore(Math.min(100, room.getDemandScore() + 5));
        status = CHECKOUT;
    }

    void markPaid(String paymentMethod) {
        this.paymentStatus = PAID;
        this.paymentMethod = paymentMethod == null || paymentMethod.isBlank() ? "Counter" : paymentMethod;
    }

    String generateBill() {
        return "===== GRAND VISTA INVOICE =====\n"
            + "Invoice    : #" + bookingId + "\n"
            + "Guest      : " + customer.getName() + " (" + customer.getTier() + ")\n"
            + "Room       : #" + room.getRoomNumber() + " " + room.getRoomType() + "\n"
            + "Amenities  : " + room.getAmenitiesDescription() + "\n"
            + "Check-in   : " + checkIn.format(DISPLAY_FORMAT) + "\n"
            + "Check-out  : " + checkOut.format(DISPLAY_FORMAT) + "\n"
            + "Nights     : " + nights + "\n"
            + "Discount   : Rs." + (int) discount + "\n"
            + "Total      : Rs." + (int) total + "\n"
            + "Stay Status: " + status + "\n"
            + "Bill Status: " + paymentStatus + "\n"
            + "Method     : " + paymentMethod + "\n"
            + "===============================";
    }

    int getBookingId() {
        return bookingId;
    }

    Customer getCustomer() {
        return customer;
    }

    Room getRoom() {
        return room;
    }

    LocalDate getCheckIn() {
        return checkIn;
    }

    LocalDate getCheckOut() {
        return checkOut;
    }

    int getNights() {
        return nights;
    }

    double getDiscount() {
        return discount;
    }

    double getTotal() {
        return total;
    }

    String getStatus() {
        return status;
    }

    String getPaymentStatus() {
        return paymentStatus;
    }

    String getPaymentMethod() {
        return paymentMethod;
    }

    String getCheckInStr() {
        return checkIn.format(DISPLAY_FORMAT);
    }

    String getCheckOutStr() {
        return checkOut.format(DISPLAY_FORMAT);
    }

    void restoreState(String status, String paymentStatus, String paymentMethod) {
        this.status = status;
        this.paymentStatus = paymentStatus;
        this.paymentMethod = paymentMethod;
    }

    void restoreFinancials(double discount, double total) {
        this.discount = discount;
        this.total = total;
    }
}

class UserAccount {
    static final String ROLE_MANAGER = "Manager";
    static final String ROLE_CUSTOMER = "Customer";

    private final String username;
    private final String password;
    private final String role;
    private final String displayName;
    private final int customerId;

    UserAccount(String username, String password, String role, String displayName, int customerId) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.displayName = displayName;
        this.customerId = customerId;
    }

    String getUsername() {
        return username;
    }

    String getPassword() {
        return password;
    }

    String getRole() {
        return role;
    }

    String getDisplayName() {
        return displayName;
    }

    int getCustomerId() {
        return customerId;
    }

    boolean isManager() {
        return ROLE_MANAGER.equals(role);
    }

    boolean isCustomer() {
        return ROLE_CUSTOMER.equals(role);
    }
}
