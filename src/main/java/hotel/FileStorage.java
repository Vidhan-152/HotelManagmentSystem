package hotel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class FileStorage {
    private final File dataDir = new File("data");
    private final File roomsFile = new File(dataDir, "rooms.txt");
    private final File customersFile = new File(dataDir, "customers.txt");
    private final File bookingsFile = new File(dataDir, "bookings.txt");
    private final File accountsFile = new File(dataDir, "accounts.txt");
    private final File metaFile = new File(dataDir, "meta.txt");

    synchronized Snapshot load() {
        ensureDataDir();

        if (!metaFile.exists()) {
            return Snapshot.empty();
        }

        Snapshot snapshot = Snapshot.empty();
        Map<Integer, Room> roomIndex = new HashMap<>();
        Map<Integer, Customer> customerIndex = new HashMap<>();

        try {
            loadRooms(snapshot, roomIndex);
            loadCustomers(snapshot, customerIndex);
            loadBookings(snapshot, roomIndex, customerIndex);
            loadAccounts(snapshot);
            loadMeta(snapshot);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to load saved data.", ex);
        }

        return snapshot;
    }

    synchronized void save(Snapshot snapshot) {
        ensureDataDir();

        try {
            writeRooms(snapshot);
            writeCustomers(snapshot);
            writeBookings(snapshot);
            writeAccounts(snapshot);
            writeMeta(snapshot);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to save data.", ex);
        }
    }

    private void loadRooms(Snapshot snapshot, Map<Integer, Room> roomIndex) throws IOException {
        List<String> lines = readLines(roomsFile);

        for (int i = 0; i < lines.size(); i++) {
            List<String> values = split(lines.get(i));

            int roomNumber = Integer.parseInt(values.get(0));
            String roomType = values.get(1);
            double basePrice = Double.parseDouble(values.get(2));
            boolean hasPool = Boolean.parseBoolean(values.get(3));
            int demandScore = Integer.parseInt(values.get(4));
            boolean booked = Boolean.parseBoolean(values.get(5));
            String guestName = values.get(6);

            Room room;
            if (roomType.equals("Deluxe")) {
                room = new DeluxeRoom(roomNumber, basePrice);
            } else if (roomType.equals("Suite")) {
                room = new SuiteRoom(roomNumber, basePrice, hasPool);
            } else {
                room = new StandardRoom(roomNumber, basePrice);
            }

            room.setDemandScore(demandScore);
            room.setBooked(booked);
            room.setGuestName(guestName);

            snapshot.rooms.add(room);
            roomIndex.put(room.getRoomNumber(), room);
        }
    }

    private void loadCustomers(Snapshot snapshot, Map<Integer, Customer> customerIndex) throws IOException {
        List<String> lines = readLines(customersFile);

        for (int i = 0; i < lines.size(); i++) {
            List<String> values = split(lines.get(i));

            Customer customer = new Customer(
                Integer.parseInt(values.get(0)),
                values.get(1),
                values.get(2),
                values.get(3)
            );

            customer.restoreState(
                Integer.parseInt(values.get(4)),
                Integer.parseInt(values.get(5)),
                values.get(6)
            );

            snapshot.customers.add(customer);
            customerIndex.put(customer.getId(), customer);
        }
    }

    private void loadBookings(Snapshot snapshot, Map<Integer, Room> roomIndex, Map<Integer, Customer> customerIndex) throws IOException {
        List<String> lines = readLines(bookingsFile);

        for (int i = 0; i < lines.size(); i++) {
            List<String> values = split(lines.get(i));

            int bookingId = Integer.parseInt(values.get(0));
            int customerId = Integer.parseInt(values.get(1));
            int roomNumber = Integer.parseInt(values.get(2));

            Customer customer = customerIndex.get(customerId);
            Room room = roomIndex.get(roomNumber);

            if (customer == null || room == null) {
                continue;
            }

            Booking booking = new Booking(
                bookingId,
                customer,
                room,
                LocalDate.parse(values.get(3)),
                LocalDate.parse(values.get(4)),
                0
            );

            booking.restoreFinancials(
                Double.parseDouble(values.get(5)),
                Double.parseDouble(values.get(6))
            );
            booking.restoreState(values.get(7), values.get(8), values.get(9));

            snapshot.bookings.add(booking);

            if (Booking.ACTIVE.equals(booking.getStatus())) {
                room.setBooked(true);
                room.setGuestName(customer.getName());
            }
        }
    }

    private void loadAccounts(Snapshot snapshot) throws IOException {
        List<String> lines = readLines(accountsFile);

        for (int i = 0; i < lines.size(); i++) {
            List<String> values = split(lines.get(i));

            UserAccount account = new UserAccount(
                values.get(0),
                values.get(1),
                values.get(2),
                values.get(3),
                Integer.parseInt(values.get(4))
            );

            snapshot.accounts.add(account);
        }
    }

    private void loadMeta(Snapshot snapshot) throws IOException {
        List<String> lines = readLines(metaFile);

        snapshot.nextCustomerId = 1000;
        snapshot.nextBookingId = 5000;

        for (int i = 0; i < lines.size(); i++) {
            List<String> values = split(lines.get(i));

            if (values.get(0).equals("nextCustomerId")) {
                snapshot.nextCustomerId = Integer.parseInt(values.get(1));
            } else if (values.get(0).equals("nextBookingId")) {
                snapshot.nextBookingId = Integer.parseInt(values.get(1));
            }
        }
    }

    private void writeRooms(Snapshot snapshot) throws IOException {
        List<String> lines = new ArrayList<>();

        for (int i = 0; i < snapshot.rooms.size(); i++) {
            Room room = snapshot.rooms.get(i);
            String line = join(
                String.valueOf(room.getRoomNumber()),
                room.getRoomType(),
                String.valueOf(room.getBasePrice()),
                String.valueOf(room.hasPool()),
                String.valueOf(room.getDemandScore()),
                String.valueOf(room.isBooked()),
                room.getGuestName()
            );
            lines.add(line);
        }

        writeLines(roomsFile, lines);
    }

    private void writeCustomers(Snapshot snapshot) throws IOException {
        List<String> lines = new ArrayList<>();

        for (int i = 0; i < snapshot.customers.size(); i++) {
            Customer customer = snapshot.customers.get(i);
            String line = join(
                String.valueOf(customer.getId()),
                customer.getName(),
                customer.getContact(),
                customer.getEmail(),
                String.valueOf(customer.getLoyaltyPoints()),
                String.valueOf(customer.getTotalStays()),
                customer.getTier()
            );
            lines.add(line);
        }

        writeLines(customersFile, lines);
    }

    private void writeBookings(Snapshot snapshot) throws IOException {
        List<String> lines = new ArrayList<>();

        for (int i = 0; i < snapshot.bookings.size(); i++) {
            Booking booking = snapshot.bookings.get(i);
            String line = join(
                String.valueOf(booking.getBookingId()),
                String.valueOf(booking.getCustomer().getId()),
                String.valueOf(booking.getRoom().getRoomNumber()),
                String.valueOf(booking.getCheckIn()),
                String.valueOf(booking.getCheckOut()),
                String.valueOf(booking.getDiscount()),
                String.valueOf(booking.getTotal()),
                booking.getStatus(),
                booking.getPaymentStatus(),
                booking.getPaymentMethod()
            );
            lines.add(line);
        }

        writeLines(bookingsFile, lines);
    }

    private void writeAccounts(Snapshot snapshot) throws IOException {
        List<String> lines = new ArrayList<>();

        for (int i = 0; i < snapshot.accounts.size(); i++) {
            UserAccount account = snapshot.accounts.get(i);
            String line = join(
                account.getUsername(),
                account.getPassword(),
                account.getRole(),
                account.getDisplayName(),
                String.valueOf(account.getCustomerId())
            );
            lines.add(line);
        }

        writeLines(accountsFile, lines);
    }

    private void writeMeta(Snapshot snapshot) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(join("nextCustomerId", String.valueOf(snapshot.nextCustomerId)));
        lines.add(join("nextBookingId", String.valueOf(snapshot.nextBookingId)));
        writeLines(metaFile, lines);
    }

    private void ensureDataDir() {
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
    }

    private List<String> readLines(File file) throws IOException {
        List<String> lines = new ArrayList<>();

        if (!file.exists()) {
            return lines;
        }

        BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8));
        try {
            String line = reader.readLine();
            while (line != null) {
                lines.add(line);
                line = reader.readLine();
            }
        } finally {
            reader.close();
        }

        return lines;
    }

    private void writeLines(File file, List<String> lines) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8));
        try {
            for (int i = 0; i < lines.size(); i++) {
                writer.write(lines.get(i));
                writer.newLine();
            }
        } finally {
            writer.close();
        }
    }

    private String join(String a, String b) {
        return escape(a) + "\t" + escape(b);
    }

    private String join(String a, String b, String c, String d, String e, String f, String g) {
        return escape(a) + "\t" + escape(b) + "\t" + escape(c) + "\t" + escape(d) + "\t"
            + escape(e) + "\t" + escape(f) + "\t" + escape(g);
    }

    private String join(String a, String b, String c, String d, String e) {
        return escape(a) + "\t" + escape(b) + "\t" + escape(c) + "\t" + escape(d) + "\t" + escape(e);
    }

    private String join(String a, String b, String c, String d, String e, String f, String g, String h, String i, String j) {
        return escape(a) + "\t" + escape(b) + "\t" + escape(c) + "\t" + escape(d) + "\t" + escape(e)
            + "\t" + escape(f) + "\t" + escape(g) + "\t" + escape(h) + "\t" + escape(i) + "\t" + escape(j);
    }

    private List<String> split(String line) {
        String[] raw = line.split("\t", -1);
        List<String> values = new ArrayList<>();

        for (int i = 0; i < raw.length; i++) {
            values.add(unescape(raw[i]));
        }

        return values;
    }

    private String escape(String value) {
        String result = value;
        result = result.replace("\\", "\\\\");
        result = result.replace("\t", "\\t");
        result = result.replace("\n", "\\n");
        return result;
    }

    private String unescape(String value) {
        StringBuilder result = new StringBuilder();
        boolean escaped = false;

        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);

            if (escaped) {
                if (ch == 't') {
                    result.append('\t');
                } else if (ch == 'n') {
                    result.append('\n');
                } else {
                    result.append(ch);
                }
                escaped = false;
            } else if (ch == '\\') {
                escaped = true;
            } else {
                result.append(ch);
            }
        }

        if (escaped) {
            result.append('\\');
        }

        return result.toString();
    }

    static class Snapshot {
        final ArrayList<Room> rooms = new ArrayList<>();
        final ArrayList<Customer> customers = new ArrayList<>();
        final ArrayList<Booking> bookings = new ArrayList<>();
        final ArrayList<UserAccount> accounts = new ArrayList<>();
        int nextCustomerId;
        int nextBookingId;

        static Snapshot empty() {
            Snapshot snapshot = new Snapshot();
            snapshot.nextCustomerId = 1000;
            snapshot.nextBookingId = 5000;
            return snapshot;
        }
    }
}
