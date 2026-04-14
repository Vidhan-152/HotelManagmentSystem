package hotel;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

class HotelService {
    private static final Pattern NAME_PATTERN = Pattern.compile("[A-Za-z][A-Za-z .'-]{1,79}");
    private static final Pattern CONTACT_PATTERN = Pattern.compile("\\+?[0-9]{10,15}");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final ArrayList<Room> rooms = new ArrayList<>();
    private final ArrayList<Customer> customers = new ArrayList<>();
    private final ArrayList<Booking> bookings = new ArrayList<>();
    private final ArrayList<UserAccount> accounts = new ArrayList<>();
    private final FileStorage storage = new FileStorage();
    private int nextCustomerId = 1000;
    private int nextBookingId = 5000;

    HotelService() {
        loadFromStorage();
    }

    private void loadFromStorage() {
        FileStorage.Snapshot snapshot = storage.load();
        rooms.addAll(snapshot.rooms);
        customers.addAll(snapshot.customers);
        bookings.addAll(snapshot.bookings);
        accounts.addAll(snapshot.accounts);
        nextCustomerId = snapshot.nextCustomerId;
        nextBookingId = snapshot.nextBookingId;
    }

    private Customer addCustomerInternal(String name, String contact, String email) {
        Customer customer = new Customer(nextCustomerId++, name, contact, email);
        customers.add(customer);
        return customer;
    }

    private void persist() {
        FileStorage.Snapshot snapshot = FileStorage.Snapshot.empty();
        snapshot.rooms.addAll(rooms);
        snapshot.customers.addAll(customers);
        snapshot.bookings.addAll(bookings);
        snapshot.accounts.addAll(accounts);
        snapshot.nextCustomerId = nextCustomerId;
        snapshot.nextBookingId = nextBookingId;
        storage.save(snapshot);
    }

    synchronized List<Room> getAllRooms() {
        return new ArrayList<>(rooms);
    }

    synchronized List<Customer> getAllCustomers() {
        return new ArrayList<>(customers);
    }

    synchronized List<Booking> getAllBookings() {
        return new ArrayList<>(bookings);
    }

    synchronized List<Booking> getBookingsForCustomer(int customerId) {
        return bookings.stream()
            .filter(booking -> booking.getCustomer().getId() == customerId)
            .toList();
    }

    synchronized List<Booking> getActiveBookings() {
        return bookings.stream().filter(booking -> Booking.ACTIVE.equals(booking.getStatus())).toList();
    }

    synchronized List<Room> getAvailableRooms() {
        return rooms.stream()
            .filter(room -> !room.isBooked())
            .sorted(Comparator.comparingInt(Room::getRoomNumber))
            .toList();
    }

    synchronized boolean addRoom(Room room) {
        validateRoom(room);
        if (findRoom(room.getRoomNumber()) != null) {
            return false;
        }
        rooms.add(room);
        persist();
        return true;
    }

    synchronized void updateDemand(int roomNumber, int score) {
        validatePositiveId(roomNumber, "Room number");
        Room room = findRoom(roomNumber);
        if (room == null) {
            throw new IllegalArgumentException("Room not found.");
        }
        room.setDemandScore(score);
        persist();
    }

    synchronized Customer addCustomer(String name, String contact, String email) {
        validateCustomerFields(name, contact, email);
        Customer customer = addCustomerInternal(name, contact, email);
        persist();
        return customer;
    }

    synchronized UserAccount registerCustomerAccount(String name, String contact, String email, String username, String password) {
        validateCustomerFields(name, contact, email);
        if (username == null || username.isBlank() || password == null || password.isBlank() || findAccount(username) != null) {
            return null;
        }
        Customer customer = addCustomerInternal(name, contact, email);
        UserAccount account = new UserAccount(username.trim(), password, UserAccount.ROLE_CUSTOMER, customer.getName(), customer.getId());
        accounts.add(account);
        persist();
        return account;
    }

    synchronized UserAccount authenticate(String username, String password, String role) {
        if (username == null || password == null || role == null) {
            return null;
        }

        UserAccount account = accounts.stream()
            .filter(candidate -> candidate.getRole().equals(role))
            .filter(candidate -> candidate.getUsername().equalsIgnoreCase(username.trim()))
            .findFirst()
            .orElse(null);
        if (account == null) {
            return null;
        }
        if (!account.getPassword().equals(password)) {
            return null;
        }
        if (!account.getRole().equals(role)) {
            return null;
        }
        return account;
    }

    synchronized Booking createBooking(int customerId, int roomNumber, LocalDate checkIn, LocalDate checkOut, int points) {
        validatePositiveId(customerId, "Customer ID");
        validatePositiveId(roomNumber, "Room number");
        validateBookingDates(checkIn, checkOut);
        if (points < 0) {
            throw new IllegalArgumentException("Redeem points cannot be negative.");
        }

        Customer customer = findCustomer(customerId);
        Room room = findRoom(roomNumber);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found.");
        }
        if (room == null) {
            throw new IllegalArgumentException("Room not found.");
        }
        if (room.isBooked()) {
            throw new IllegalArgumentException("Selected room is already booked.");
        }

        Booking booking = new Booking(nextBookingId++, customer, room, checkIn, checkOut, points);
        room.setBooked(true);
        room.setGuestName(customer.getName());
        bookings.add(booking);
        persist();
        return booking;
    }

    synchronized Booking checkout(int bookingId) {
        validatePositiveId(bookingId, "Booking ID");
        Booking booking = findBooking(bookingId);
        if (booking == null || !Booking.ACTIVE.equals(booking.getStatus())) {
            return null;
        }
        if (!Booking.PAID.equals(booking.getPaymentStatus())) {
            throw new IllegalArgumentException("Cannot check out guest until payment is marked as paid.");
        }
        booking.checkout();
        persist();
        return booking;
    }

    synchronized Booking markInvoicePaid(int bookingId, String method) {
        validatePositiveId(bookingId, "Booking ID");
        Booking booking = findBooking(bookingId);
        if (booking == null) {
            return null;
        }
        booking.markPaid(method);
        persist();
        return booking;
    }

    synchronized Room findRoom(int roomNumber) {
        return rooms.stream().filter(room -> room.getRoomNumber() == roomNumber).findFirst().orElse(null);
    }

    synchronized Customer findCustomer(int customerId) {
        return customers.stream().filter(customer -> customer.getId() == customerId).findFirst().orElse(null);
    }

    synchronized Customer findCustomerByUser(UserAccount account) {
        if (account == null || !account.isCustomer()) {
            return null;
        }
        return findCustomer(account.getCustomerId());
    }

    synchronized Booking findBooking(int bookingId) {
        return bookings.stream().filter(booking -> booking.getBookingId() == bookingId).findFirst().orElse(null);
    }

    private UserAccount findAccount(String username) {
        if (username == null) {
            return null;
        }
        return accounts.stream()
            .filter(account -> account.getUsername().equalsIgnoreCase(username.trim()))
            .findFirst()
            .orElse(null);
    }

    synchronized int getTotalRooms() {
        return rooms.size();
    }

    synchronized long getBookedRooms() {
        return rooms.stream().filter(Room::isBooked).count();
    }

    synchronized long getAvailableCount() {
        return getTotalRooms() - getBookedRooms();
    }

    synchronized double getOccupancyPct() {
        return getTotalRooms() == 0 ? 0 : getBookedRooms() * 100.0 / getTotalRooms();
    }

    synchronized double getTotalRevenue() {
        return bookings.stream().mapToDouble(Booking::getTotal).sum();
    }

    synchronized double getPaidRevenue() {
        return bookings.stream()
            .filter(booking -> Booking.PAID.equals(booking.getPaymentStatus()))
            .mapToDouble(Booking::getTotal)
            .sum();
    }

    synchronized double getPendingRevenue() {
        return bookings.stream()
            .filter(booking -> Booking.DUE.equals(booking.getPaymentStatus()))
            .mapToDouble(Booking::getTotal)
            .sum();
    }

    synchronized long getPaidInvoicesCount() {
        return bookings.stream().filter(booking -> Booking.PAID.equals(booking.getPaymentStatus())).count();
    }

    synchronized List<Room> getRecommendations(double budget, String type, int nights) {
        if (budget <= 0) {
            throw new IllegalArgumentException("Budget must be greater than 0.");
        }
        if (nights <= 0) {
            throw new IllegalArgumentException("Nights must be at least 1.");
        }
        return rooms.stream()
            .filter(room -> !room.isBooked())
            .filter(room -> "Any".equals(type) || room.getRoomType().equals(type))
            .filter(room -> room.calculateTariff(1) <= budget)
            .sorted(Comparator.comparingDouble(room -> room.calculateTariff(nights)))
            .toList();
    }

    private void validateCustomerFields(String name, String contact, String email) {
        String normalizedName = requireTrimmed(name, "Name");
        String normalizedContact = requireTrimmed(contact, "Contact");
        String normalizedEmail = requireTrimmed(email, "Email");

        if (!NAME_PATTERN.matcher(normalizedName).matches()) {
            throw new IllegalArgumentException("Name must be 2 to 80 characters and use letters only.");
        }
        if (!CONTACT_PATTERN.matcher(normalizedContact).matches()) {
            throw new IllegalArgumentException("Contact number must contain 10 digits.");
        }
        if (!EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            throw new IllegalArgumentException("Enter a valid email address.");
        }
    }

    private void validateRoom(Room room) {
        if (room == null) {
            throw new IllegalArgumentException("Room details are required.");
        }
        validatePositiveId(room.getRoomNumber(), "Room number");
        if (room.getBasePrice() <= 0) {
            throw new IllegalArgumentException("Room price must be greater than 0.");
        }
    }

    private void validateBookingDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new IllegalArgumentException("Check-in and check-out dates are required.");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Check-out date must be after check-in date.");
        }
    }

    private void validatePositiveId(int value, String label) {
        if (value <= 0) {
            throw new IllegalArgumentException(label + " must be greater than 0.");
        }
    }

    private String requireTrimmed(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " is required.");
        }
        return value.trim();
    }
}
