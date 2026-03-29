package hotel;

// =============================================
// ROOM CLASSES - Abstract base + 3 subtypes
// Demonstrates: Abstraction, Inheritance, Polymorphism, Encapsulation
// =============================================

// Interface for amenities (manual requirement)
interface Amenities {
    boolean provideWifi();
    boolean provideBreakfast();
    String getAmenitiesDescription();
}

// Abstract base class - cannot be instantiated directly
abstract class Room implements Amenities {

    // Encapsulated private fields
    private int roomNumber;
    private String roomType;
    private double basePrice;
    private boolean isBooked;
    private String guestName;
    private int demandScore; // 0-100, used for dynamic pricing

    Room(int roomNumber, String roomType, double basePrice) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.basePrice = basePrice;
        this.isBooked = false;
        this.guestName = "";
        this.demandScore = 30;
    }

    // Abstract - each subclass calculates tariff differently (Polymorphism)
    abstract double calculateTariff(int nights);

    // Dynamic pricing multiplier based on demand (Unique Feature)
    double getDynamicMultiplier() {
        return 1.0 + (demandScore / 200.0);
    }

    // Getters
    int getRoomNumber()    { return roomNumber; }
    String getRoomType()   { return roomType; }
    double getBasePrice()  { return basePrice; }
    boolean isBooked()     { return isBooked; }
    String getGuestName()  { return guestName; }
    int getDemandScore()   { return demandScore; }

    // Setters
    void setBooked(boolean b)      { this.isBooked = b; }
    void setGuestName(String name) { this.guestName = name != null ? name : ""; }
    void setDemandScore(int s)     { this.demandScore = Math.max(0, Math.min(100, s)); }
    void setBasePrice(double p)    { if (p > 0) this.basePrice = p; }

    public String toString() {
        return "Room #" + roomNumber + " | " + roomType + " | Rs." + (int)basePrice
             + "/night | " + (isBooked ? "BOOKED (" + guestName + ")" : "AVAILABLE")
             + " | Demand: " + demandScore + "%";
    }
}

// ---- Standard Room ----
class StandardRoom extends Room {
    StandardRoom(int num, double price) {
        super(num, "Standard", price);
    }

    @Override
    double calculateTariff(int nights) {
        return getBasePrice() * getDynamicMultiplier() * nights;
    }

    @Override public boolean provideWifi()      { return true; }
    @Override public boolean provideBreakfast() { return false; }
    @Override public String getAmenitiesDescription() { return "Wi-Fi, AC"; }
}

// ---- Deluxe Room ----
class DeluxeRoom extends Room {
    private static final double SURCHARGE = 500.0;

    DeluxeRoom(int num, double price) {
        super(num, "Deluxe", price);
    }

    @Override
    double calculateTariff(int nights) {
        return (getBasePrice() * getDynamicMultiplier() + SURCHARGE) * nights;
    }

    @Override public boolean provideWifi()      { return true; }
    @Override public boolean provideBreakfast() { return true; }
    @Override public String getAmenitiesDescription() { return "Wi-Fi, AC, Breakfast"; }
}

// ---- Suite Room ----
class SuiteRoom extends Room {
    private static final double LUXURY = 1500.0;
    private boolean hasPool;

    SuiteRoom(int num, double price, boolean hasPool) {
        super(num, "Suite", price);
        this.hasPool = hasPool;
    }

    @Override
    double calculateTariff(int nights) {
        double multiplier = 1.0 + (getDemandScore() / 150.0);
        double poolExtra = hasPool ? 2000.0 : 0.0;
        return (getBasePrice() * multiplier + LUXURY + poolExtra) * nights;
    }

    @Override public boolean provideWifi()      { return true; }
    @Override public boolean provideBreakfast() { return true; }
    @Override public String getAmenitiesDescription() {
        return "Wi-Fi, AC, Breakfast, Butler" + (hasPool ? ", Pool" : "");
    }
}
