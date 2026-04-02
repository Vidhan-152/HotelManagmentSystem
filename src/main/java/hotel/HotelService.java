package hotel;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

class HotelService {
    private final ArrayList<Room> rooms = new ArrayList<>();
    private final ArrayList<Customer> customers = new ArrayList<>();
    private final ArrayList<Booking> bookings = new ArrayList<>();
    private final ArrayList<UserAccount> accounts = new ArrayList<>();
    private final FileStorage storage = new FileStorage();
    private int nextCustomerId = 1000;
    private int nextBookingId = 5000;

    HotelService() {
        loadOrSeed();
    }

    private void loadOrSeed() {
        FileStorage.Snapshot snapshot = storage.load();
        if (snapshot.rooms.isEmpty() && snapshot.customers.isEmpty() && snapshot.bookings.isEmpty() && snapshot.accounts.isEmpty()) {
            seedData();
            persist();
            return;
        }
        rooms.addAll(snapshot.rooms);
        customers.addAll(snapshot.customers);
        bookings.addAll(snapshot.bookings);
        accounts.addAll(snapshot.accounts);
        nextCustomerId = snapshot.nextCustomerId;
        nextBookingId = snapshot.nextBookingId;
        if (ensureDefaultManager()) {
            persist();
        }
    }

    private void seedData() {
        rooms.add(new StandardRoom(101, 1800));
        rooms.add(new StandardRoom(102, 1800));
        rooms.add(new StandardRoom(103, 2000));

        DeluxeRoom deluxe1 = new DeluxeRoom(201, 3200);
        deluxe1.setDemandScore(60);
        rooms.add(deluxe1);

        DeluxeRoom deluxe2 = new DeluxeRoom(202, 3500);
        deluxe2.setDemandScore(70);
        rooms.add(deluxe2);
        rooms.add(new DeluxeRoom(203, 3200));

        SuiteRoom suite1 = new SuiteRoom(301, 6500, true);
        suite1.setDemandScore(80);
        rooms.add(suite1);
        rooms.add(new SuiteRoom(302, 7000, false));
        rooms.add(new SuiteRoom(303, 6800, true));

        Customer priya = addCustomerInternal("Priya Sharma", "9876543210", "priya@email.com");
        priya.restoreState(1600, 4, "Gold");
        Customer arjun = addCustomerInternal("Arjun Mehta", "9123456789", "arjun@email.com");
        arjun.restoreState(700, 2, "Silver");
        addCustomerInternal("Sneha Iyer", "9988776655", "sneha@email.com");
        ensureDefaultManager();
    }

    private boolean ensureDefaultManager() {
        int before = accounts.size();
        accounts.removeIf(account -> account.isManager() || account.getUsername().equalsIgnoreCase("Vidhan"));
        accounts.add(new UserAccount("Vidhan", "123456", UserAccount.ROLE_MANAGER, "Vidhan", -1));
        return before != accounts.size() || accounts.stream().noneMatch(account ->
            account.isManager() && account.getUsername().equals("Vidhan") && account.getPassword().equals("123456"));
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
        if (findRoom(room.getRoomNumber()) != null) {
            return false;
        }
        rooms.add(room);
        persist();
        return true;
    }

    synchronized void updateDemand(int roomNumber, int score) {
        Room room = findRoom(roomNumber);
        if (room != null) {
            room.setDemandScore(score);
            persist();
        }
    }

    synchronized Customer addCustomer(String name, String contact, String email) {
        Customer customer = addCustomerInternal(name, contact, email);
        persist();
        return customer;
    }

    synchronized UserAccount registerCustomerAccount(String name, String contact, String email, String username, String password) {
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
        if (UserAccount.ROLE_MANAGER.equals(role)) {
            if ("Vidhan".equalsIgnoreCase(username.trim()) && "123456".equals(password)) {
                return accounts.stream()
                    .filter(UserAccount::isManager)
                    .filter(account -> account.getUsername().equalsIgnoreCase("Vidhan"))
                    .findFirst()
                    .orElse(new UserAccount("Vidhan", "123456", UserAccount.ROLE_MANAGER, "Vidhan", -1));
            }
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
        Customer customer = findCustomer(customerId);
        Room room = findRoom(roomNumber);
        if (customer == null || room == null || room.isBooked() || !checkOut.isAfter(checkIn)) {
            return null;
        }

        Booking booking = new Booking(nextBookingId++, customer, room, checkIn, checkOut, points);
        room.setBooked(true);
        room.setGuestName(customer.getName());
        bookings.add(booking);
        persist();
        return booking;
    }

    synchronized Booking checkout(int bookingId) {
        Booking booking = findBooking(bookingId);
        if (booking == null || !Booking.ACTIVE.equals(booking.getStatus())) {
            return null;
        }
        booking.checkout();
        persist();
        return booking;
    }

    synchronized Booking markInvoicePaid(int bookingId, String method) {
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
        return rooms.stream()
            .filter(room -> !room.isBooked())
            .filter(room -> "Any".equals(type) || room.getRoomType().equals(type))
            .filter(room -> room.calculateTariff(1) <= budget)
            .sorted(Comparator.comparingDouble(room -> room.calculateTariff(nights)))
            .toList();
    }
}
