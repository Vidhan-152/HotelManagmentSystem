package hotel;

import java.time.LocalDate;
import java.util.*;

// =============================================
// HOTEL SERVICE - Business logic layer
// Uses: ArrayList, HashMap, Iterator, Collections.sort
// =============================================
class HotelService {

    // Collections to store all data in memory
    private ArrayList<Room>     rooms     = new ArrayList<>();
    private ArrayList<Customer> customers = new ArrayList<>();
    private ArrayList<Booking>  bookings  = new ArrayList<>();
    private HashMap<Integer, Customer> roomMap = new HashMap<>(); // roomNo -> guest

    private int nextCustomerId = 1000;
    private int nextBookingId  = 5000;

    HotelService() {
        seedData();
    }

    // Load default rooms and customers on startup
    private void seedData() {
        rooms.add(new StandardRoom(101, 1800));
        rooms.add(new StandardRoom(102, 1800));
        rooms.add(new StandardRoom(103, 2000));

        DeluxeRoom d1 = new DeluxeRoom(201, 3200); d1.setDemandScore(60); rooms.add(d1);
        DeluxeRoom d2 = new DeluxeRoom(202, 3500); d2.setDemandScore(70); rooms.add(d2);
        rooms.add(new DeluxeRoom(203, 3200));

        SuiteRoom s1 = new SuiteRoom(301, 6500, true); s1.setDemandScore(80); rooms.add(s1);
        rooms.add(new SuiteRoom(302, 7000, false));
        rooms.add(new SuiteRoom(303, 6800, true));

        customers.add(new Customer(nextCustomerId++, "Priya Sharma",  "9876543210", "priya@email.com"));
        customers.add(new Customer(nextCustomerId++, "Arjun Mehta",   "9123456789", "arjun@email.com"));
        customers.add(new Customer(nextCustomerId++, "Sneha Iyer",    "9988776655", "sneha@email.com"));

        customers.get(0).awardPoints(75000); // makes Gold
        customers.get(1).awardPoints(20000); // makes Silver
    }

    // ---- ROOM METHODS ----

    boolean addRoom(Room room) {
        // Use Iterator to check for duplicate room number
        Iterator<Room> it = rooms.iterator();
        while (it.hasNext()) {
            if (it.next().getRoomNumber() == room.getRoomNumber()) return false;
        }
        rooms.add(room);
        return true;
    }

    boolean removeRoom(int roomNum) {
        Iterator<Room> it = rooms.iterator();
        while (it.hasNext()) {
            Room r = it.next();
            if (r.getRoomNumber() == roomNum) {
                if (r.isBooked()) return false;
                it.remove(); // Safe removal using Iterator
                return true;
            }
        }
        return false;
    }

    Room findRoom(int roomNum) {
        for (Room r : rooms) if (r.getRoomNumber() == roomNum) return r;
        return null;
    }

    ArrayList<Room> getAllRooms() { return rooms; }

    ArrayList<Room> getAvailableRooms() {
        ArrayList<Room> list = new ArrayList<>();
        Iterator<Room> it = rooms.iterator();
        while (it.hasNext()) { Room r = it.next(); if (!r.isBooked()) list.add(r); }
        return list;
    }

    // Sort rooms by base price using Collections.sort
    ArrayList<Room> getRoomsSortedByPrice() {
        ArrayList<Room> sorted = new ArrayList<>(rooms);
        Collections.sort(sorted, Comparator.comparingDouble(Room::getBasePrice));
        return sorted;
    }

    void updateDemand(int roomNum, int score) {
        Room r = findRoom(roomNum);
        if (r != null) r.setDemandScore(score);
    }

    // ---- CUSTOMER METHODS ----

    Customer addCustomer(String name, String contact, String email) {
        Customer c = new Customer(nextCustomerId++, name, contact, email);
        customers.add(c);
        return c;
    }

    Customer findCustomer(int id) {
        for (Customer c : customers) if (c.getId() == id) return c;
        return null;
    }

    ArrayList<Customer> getAllCustomers() { return customers; }

    // ---- BOOKING METHODS ----

    Booking createBooking(int customerId, int roomNum,
                          LocalDate in, LocalDate out, int points) {
        Customer c = findCustomer(customerId);
        Room r = findRoom(roomNum);
        if (c == null || r == null || r.isBooked()) return null;

        Booking b = new Booking(nextBookingId++, c, r, in, out, points);
        r.setBooked(true);
        r.setGuestName(c.getName());
        roomMap.put(roomNum, c);
        bookings.add(b);
        return b;
    }

    Booking checkout(int bookingId) {
        for (Booking b : bookings) {
            if (b.getBookingId() == bookingId && b.getStatus().equals(Booking.ACTIVE)) {
                b.checkout();
                roomMap.remove(b.getRoom().getRoomNumber());
                return b;
            }
        }
        return null;
    }

    Booking findBooking(int id) {
        for (Booking b : bookings) if (b.getBookingId() == id) return b;
        return null;
    }

    ArrayList<Booking> getAllBookings()    { return bookings; }

    ArrayList<Booking> getActiveBookings() {
        ArrayList<Booking> list = new ArrayList<>();
        for (Booking b : bookings) if (b.getStatus().equals(Booking.ACTIVE)) list.add(b);
        return list;
    }

    // ---- ANALYTICS ----

    int getTotalRooms()     { return rooms.size(); }
    int getBookedRooms()    { return (int) rooms.stream().filter(Room::isBooked).count(); }
    int getAvailableCount() { return getTotalRooms() - getBookedRooms(); }
    double getOccupancyPct(){ return getTotalRooms() > 0 ? getBookedRooms() * 100.0 / getTotalRooms() : 0; }

    double getTotalRevenue() {
        return bookings.stream()
            .filter(b -> b.getStatus().equals(Booking.CHECKOUT))
            .mapToDouble(Booking::getTotal).sum();
    }

    // Smart recommendation - Unique Feature
    ArrayList<Room> getRecommendations(double budget, String type, int nights) {
        ArrayList<Room> result = new ArrayList<>();
        for (Room r : rooms) {
            if (r.isBooked()) continue;
            if (r.calculateTariff(1) > budget) continue;
            if (!type.equals("Any") && !r.getRoomType().equals(type)) continue;
            result.add(r);
        }
        Collections.sort(result, Comparator.comparingDouble(r -> r.calculateTariff(nights)));
        return result;
    }
}
