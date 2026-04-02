package hotel;

interface Amenities {
    boolean provideWifi();
    boolean provideBreakfast();
    String getAmenitiesDescription();
}

abstract class Room implements Amenities {
    private final int roomNumber;
    private final String roomType;
    private double basePrice;
    private boolean booked;
    private String guestName;
    private int demandScore;

    Room(int roomNumber, String roomType, double basePrice) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.basePrice = basePrice;
        this.guestName = "";
        this.demandScore = 30;
    }

    abstract double calculateTariff(int nights);

    double getDynamicMultiplier() {
        return 1.0 + (demandScore / 200.0);
    }

    int getRoomNumber() {
        return roomNumber;
    }

    String getRoomType() {
        return roomType;
    }

    double getBasePrice() {
        return basePrice;
    }

    boolean isBooked() {
        return booked;
    }

    String getGuestName() {
        return guestName;
    }

    int getDemandScore() {
        return demandScore;
    }

    void setBooked(boolean booked) {
        this.booked = booked;
    }

    void setGuestName(String guestName) {
        this.guestName = guestName == null ? "" : guestName;
    }

    void setDemandScore(int demandScore) {
        this.demandScore = Math.max(0, Math.min(100, demandScore));
    }

    void setBasePrice(double basePrice) {
        if (basePrice > 0) {
            this.basePrice = basePrice;
        }
    }

    boolean hasPool() {
        return false;
    }
}

class StandardRoom extends Room {
    StandardRoom(int roomNumber, double basePrice) {
        super(roomNumber, "Standard", basePrice);
    }

    @Override
    double calculateTariff(int nights) {
        return getBasePrice() * getDynamicMultiplier() * nights;
    }

    @Override
    public boolean provideWifi() {
        return true;
    }

    @Override
    public boolean provideBreakfast() {
        return false;
    }

    @Override
    public String getAmenitiesDescription() {
        return "Wi-Fi, AC";
    }
}

class DeluxeRoom extends Room {
    DeluxeRoom(int roomNumber, double basePrice) {
        super(roomNumber, "Deluxe", basePrice);
    }

    @Override
    double calculateTariff(int nights) {
        return (getBasePrice() * getDynamicMultiplier() + 500.0) * nights;
    }

    @Override
    public boolean provideWifi() {
        return true;
    }

    @Override
    public boolean provideBreakfast() {
        return true;
    }

    @Override
    public String getAmenitiesDescription() {
        return "Wi-Fi, AC, Breakfast";
    }
}

class SuiteRoom extends Room {
    private final boolean hasPool;

    SuiteRoom(int roomNumber, double basePrice, boolean hasPool) {
        super(roomNumber, "Suite", basePrice);
        this.hasPool = hasPool;
    }

    @Override
    double calculateTariff(int nights) {
        double multiplier = 1.0 + (getDemandScore() / 150.0);
        double poolExtra = hasPool ? 2000.0 : 0.0;
        return (getBasePrice() * multiplier + 1500.0 + poolExtra) * nights;
    }

    @Override
    public boolean provideWifi() {
        return true;
    }

    @Override
    public boolean provideBreakfast() {
        return true;
    }

    @Override
    public String getAmenitiesDescription() {
        return "Wi-Fi, AC, Breakfast, Butler" + (hasPool ? ", Pool" : "");
    }

    @Override
    boolean hasPool() {
        return hasPool;
    }
}
