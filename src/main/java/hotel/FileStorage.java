package hotel;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class FileStorage {
    private final Path dataDir = Path.of("data");
    private final Path roomsFile = dataDir.resolve("rooms.txt");
    private final Path customersFile = dataDir.resolve("customers.txt");
    private final Path bookingsFile = dataDir.resolve("bookings.txt");
    private final Path accountsFile = dataDir.resolve("accounts.txt");
    private final Path metaFile = dataDir.resolve("meta.txt");

    synchronized Snapshot load() {
        ensureDataDir();
        if (!Files.exists(metaFile)) {
            return Snapshot.empty();
        }

        try {
            Snapshot snapshot = Snapshot.empty();
            Map<Integer, Room> roomIndex = new HashMap<>();
            Map<Integer, Customer> customerIndex = new HashMap<>();

            for (String line : readLines(roomsFile)) {
                List<String> values = split(line);
                Room room = createRoom(values);
                room.setDemandScore(Integer.parseInt(values.get(4)));
                room.setBooked(Boolean.parseBoolean(values.get(5)));
                room.setGuestName(values.get(6));
                snapshot.rooms.add(room);
                roomIndex.put(room.getRoomNumber(), room);
            }

            for (String line : readLines(customersFile)) {
                List<String> values = split(line);
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

            for (String line : readLines(bookingsFile)) {
                List<String> values = split(line);
                Customer customer = customerIndex.get(Integer.parseInt(values.get(1)));
                Room room = roomIndex.get(Integer.parseInt(values.get(2)));
                if (customer == null || room == null) {
                    continue;
                }
                Booking booking = new Booking(
                    Integer.parseInt(values.get(0)),
                    customer,
                    room,
                    LocalDate.parse(values.get(3)),
                    LocalDate.parse(values.get(4)),
                    0
                );
                booking.restoreFinancials(Double.parseDouble(values.get(5)), Double.parseDouble(values.get(6)));
                booking.restoreState(values.get(7), values.get(8), values.get(9));
                snapshot.bookings.add(booking);
                if (Booking.ACTIVE.equals(booking.getStatus())) {
                    room.setBooked(true);
                    room.setGuestName(customer.getName());
                }
            }

            for (String line : readLines(accountsFile)) {
                List<String> values = split(line);
                snapshot.accounts.add(new UserAccount(
                    values.get(0),
                    values.get(1),
                    values.get(2),
                    values.get(3),
                    Integer.parseInt(values.get(4))
                ));
            }

            Map<String, String> meta = new HashMap<>();
            for (String line : readLines(metaFile)) {
                List<String> values = split(line);
                meta.put(values.get(0), values.get(1));
            }
            snapshot.nextCustomerId = Integer.parseInt(meta.getOrDefault("nextCustomerId", "1000"));
            snapshot.nextBookingId = Integer.parseInt(meta.getOrDefault("nextBookingId", "5000"));
            return snapshot;
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to load saved data.", ex);
        }
    }

    synchronized void save(Snapshot snapshot) {
        ensureDataDir();
        try {
            List<String> roomLines = new ArrayList<>();
            for (Room room : snapshot.rooms) {
                roomLines.add(join(
                    room.getRoomNumber(),
                    room.getRoomType(),
                    room.getBasePrice(),
                    room.hasPool(),
                    room.getDemandScore(),
                    room.isBooked(),
                    room.getGuestName()
                ));
            }

            List<String> customerLines = new ArrayList<>();
            for (Customer customer : snapshot.customers) {
                customerLines.add(join(
                    customer.getId(),
                    customer.getName(),
                    customer.getContact(),
                    customer.getEmail(),
                    customer.getLoyaltyPoints(),
                    customer.getTotalStays(),
                    customer.getTier()
                ));
            }

            List<String> bookingLines = new ArrayList<>();
            for (Booking booking : snapshot.bookings) {
                bookingLines.add(join(
                    booking.getBookingId(),
                    booking.getCustomer().getId(),
                    booking.getRoom().getRoomNumber(),
                    booking.getCheckIn(),
                    booking.getCheckOut(),
                    booking.getDiscount(),
                    booking.getTotal(),
                    booking.getStatus(),
                    booking.getPaymentStatus(),
                    booking.getPaymentMethod()
                ));
            }

            List<String> accountLines = new ArrayList<>();
            for (UserAccount account : snapshot.accounts) {
                accountLines.add(join(
                    account.getUsername(),
                    account.getPassword(),
                    account.getRole(),
                    account.getDisplayName(),
                    account.getCustomerId()
                ));
            }

            writeAtomically(roomsFile, roomLines);
            writeAtomically(customersFile, customerLines);
            writeAtomically(bookingsFile, bookingLines);
            writeAtomically(accountsFile, accountLines);
            writeAtomically(metaFile, List.of(
                join("nextCustomerId", snapshot.nextCustomerId),
                join("nextBookingId", snapshot.nextBookingId)
            ));
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to save data.", ex);
        }
    }

    private Room createRoom(List<String> values) {
        int roomNumber = Integer.parseInt(values.get(0));
        String roomType = values.get(1);
        double basePrice = Double.parseDouble(values.get(2));
        boolean hasPool = Boolean.parseBoolean(values.get(3));

        return switch (roomType) {
            case "Deluxe" -> new DeluxeRoom(roomNumber, basePrice);
            case "Suite" -> new SuiteRoom(roomNumber, basePrice, hasPool);
            default -> new StandardRoom(roomNumber, basePrice);
        };
    }

    private void ensureDataDir() {
        try {
            Files.createDirectories(dataDir);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to prepare data directory.", ex);
        }
    }

    private List<String> readLines(Path path) throws IOException {
        if (!Files.exists(path)) {
            return List.of();
        }
        return Files.readAllLines(path, StandardCharsets.UTF_8);
    }

    private void writeAtomically(Path target, List<String> lines) throws IOException {
        Path temp = target.resolveSibling(target.getFileName() + ".tmp");
        Files.write(temp, lines, StandardCharsets.UTF_8);
        Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    private String join(Object... values) {
        List<String> encoded = new ArrayList<>();
        for (Object value : values) {
            encoded.add(escape(String.valueOf(value)));
        }
        return String.join("\t", encoded);
    }

    private List<String> split(String line) {
        String[] raw = line.split("\t", -1);
        List<String> values = new ArrayList<>();
        for (String item : raw) {
            values.add(unescape(item));
        }
        return values;
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\t", "\\t").replace("\n", "\\n");
    }

    private String unescape(String value) {
        StringBuilder result = new StringBuilder();
        boolean escaped = false;
        for (char ch : value.toCharArray()) {
            if (escaped) {
                result.append(switch (ch) {
                    case 't' -> '\t';
                    case 'n' -> '\n';
                    default -> ch;
                });
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
