package hotel;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

// =============================================
// CUSTOMER - Encapsulation + Loyalty System (Unique Feature)
// =============================================
class Customer {

    private int id;
    private String name;
    private String contact;
    private String email;
    private int loyaltyPoints;
    private int totalStays;
    private String tier; // Bronze, Silver, Gold

    Customer(int id, String name, String contact, String email) {
        this.id = id;
        this.name = name;
        this.contact = contact;
        this.email = email;
        this.loyaltyPoints = 0;
        this.totalStays = 0;
        this.tier = "Bronze";
    }

    // Award points after checkout: Rs.100 spent = 1 point
    void awardPoints(double amountSpent) {
        int earned = (int)(amountSpent * getLoyaltyMultiplier() / 100);
        loyaltyPoints += earned;
        totalStays++;
        updateTier();
    }

    // Redeem points for discount: 1 point = Rs.1
    double redeemPoints(int points) {
        int used = Math.min(points, loyaltyPoints);
        loyaltyPoints -= used;
        updateTier();
        return used * 1.0;
    }

    double getLoyaltyMultiplier() {
        if (tier.equals("Gold"))   return 2.0;
        if (tier.equals("Silver")) return 1.5;
        return 1.0;
    }

    private void updateTier() {
        if (loyaltyPoints >= 1500)     tier = "Gold";
        else if (loyaltyPoints >= 500) tier = "Silver";
        else                           tier = "Bronze";
    }

    // Getters
    int getId()            { return id; }
    String getName()       { return name; }
    String getContact()    { return contact; }
    String getEmail()      { return email; }
    int getLoyaltyPoints() { return loyaltyPoints; }
    int getTotalStays()    { return totalStays; }
    String getTier()       { return tier; }

    public String toString() {
        return "ID:" + id + " | " + name + " | " + tier + " | " + loyaltyPoints + " pts";
    }
}

// =============================================
// BOOKING - Links Customer to Room
// =============================================
class Booking {

    static final String ACTIVE   = "Active";
    static final String CHECKOUT = "Checked Out";

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private int bookingId;
    private Customer customer;
    private Room room;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private int nights;
    private double total;
    private double discount;
    private String status;

    Booking(int id, Customer customer, Room room,
            LocalDate checkIn, LocalDate checkOut, int pointsToRedeem) {
        this.bookingId = id;
        this.customer  = customer;
        this.room      = room;
        this.checkIn   = checkIn;
        this.checkOut  = checkOut;
        this.status    = ACTIVE;

        this.nights = (int) ChronoUnit.DAYS.between(checkIn, checkOut);
        if (this.nights <= 0) this.nights = 1;

        // Runtime Polymorphism: correct calculateTariff() called based on actual room type
        double baseBill = room.calculateTariff(nights);

        this.discount = customer.redeemPoints(pointsToRedeem);
        this.discount = Math.min(discount, baseBill * 0.20); // max 20% off
        this.total    = baseBill - discount;
    }

    void checkout() {
        customer.awardPoints(total);
        room.setBooked(false);
        room.setGuestName("");
        room.setDemandScore(Math.min(100, room.getDemandScore() + 5));
        this.status = CHECKOUT;
    }

    String generateBill() {
        return  "===== RECEIPT =====\n"
              + "Booking  : #" + bookingId + "\n"
              + "Guest    : " + customer.getName() + " (" + customer.getTier() + ")\n"
              + "Room     : #" + room.getRoomNumber() + " " + room.getRoomType() + "\n"
              + "Amenities: " + room.getAmenitiesDescription() + "\n"
              + "Check-in : " + checkIn.format(FMT) + "\n"
              + "Check-out: " + checkOut.format(FMT) + "\n"
              + "Nights   : " + nights + "\n"
              + "Discount : Rs." + (int)discount + "\n"
              + "-------------------\n"
              + "TOTAL    : Rs." + (int)total + "\n"
              + "Status   : " + status + "\n"
              + "===================";
    }

    // Getters
    int getBookingId()      { return bookingId; }
    Customer getCustomer()  { return customer; }
    Room getRoom()          { return room; }
    int getNights()         { return nights; }
    double getTotal()       { return total; }
    double getDiscount()    { return discount; }
    String getStatus()      { return status; }
    String getCheckInStr()  { return checkIn.format(FMT); }
    String getCheckOutStr() { return checkOut.format(FMT); }
}
