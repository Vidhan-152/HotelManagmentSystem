package hotel;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class DashboardController {
    private static final String GOLD = "#DAA520";
    private static final String GREEN = "#2E8B57";
    private static final String RED = "#D65454";
    private static final String BLUE = "#3F7AE0";

    @FXML private StackPane contentPane;
    @FXML private Button overviewBtn;
    @FXML private Button roomsBtn;
    @FXML private Button customersBtn;
    @FXML private Button bookingsBtn;
    @FXML private Button billingBtn;
    @FXML private Button checkoutBtn;
    @FXML private Button smartBtn;
    @FXML private Button analyticsBtn;
    @FXML private Label sessionTitleLabel;
    @FXML private Label sessionSubtitleLabel;

    private HotelApp app;
    private HotelService service;
    private UserAccount currentUser;
    private List<Button> navButtons;

    void initializeApp(HotelApp app, HotelService service, UserAccount currentUser) {
        this.app = app;
        this.service = service;
        this.currentUser = currentUser;
        navButtons = List.of(overviewBtn, roomsBtn, customersBtn, bookingsBtn, billingBtn, checkoutBtn, smartBtn, analyticsBtn);
        configureSessionHeader();
        configureRoleAccess();
        showOverview();
    }

    @FXML private void showOverview() { setActive(overviewBtn); setContent(buildOverview()); }
    @FXML private void showRooms() { if (isManager()) { setActive(roomsBtn); setContent(buildRooms()); } }
    @FXML private void showCustomers() { if (isManager()) { setActive(customersBtn); setContent(buildCustomers()); } }
    @FXML private void showBookings() { setActive(bookingsBtn); setContent(buildBookings()); }
    @FXML private void showBilling() { if (isManager()) { setActive(billingBtn); setContent(buildBilling()); } }
    @FXML private void showCheckout() { if (isManager()) { setActive(checkoutBtn); setContent(buildCheckout()); } }
    @FXML private void showSmart() { setActive(smartBtn); setContent(buildSmartFinder()); }
    @FXML private void showAnalytics() { if (isManager()) { setActive(analyticsBtn); setContent(buildAnalytics()); } }

    @FXML
    private void handleLogout() {
        try {
            app.showAuthScene();
        } catch (Exception ex) {
            error("Unable to return to the login page.");
        }
    }

    private void configureSessionHeader() {
        sessionTitleLabel.setText(currentUser.getDisplayName());
        sessionSubtitleLabel.setText(currentUser.isManager()
            ? "Manager access"
            : "Customer ID: " + currentUser.getCustomerId());
    }

    private void configureRoleAccess() {
        boolean manager = isManager();
        roomsBtn.setVisible(manager);
        roomsBtn.setManaged(manager);
        customersBtn.setVisible(manager);
        customersBtn.setManaged(manager);
        billingBtn.setVisible(manager);
        billingBtn.setManaged(manager);
        checkoutBtn.setVisible(manager);
        checkoutBtn.setManaged(manager);
        analyticsBtn.setVisible(manager);
        analyticsBtn.setManaged(manager);
    }

    private boolean isManager() {
        return currentUser != null && currentUser.isManager();
    }

    private boolean isCustomer() {
        return currentUser != null && currentUser.isCustomer();
    }

    private void setActive(Button active) {
        for (Button button : navButtons) {
            button.getStyleClass().remove("nav-button-active");
        }
        active.getStyleClass().add("nav-button-active");
    }

    private void setContent(Node node) {
        contentPane.getChildren().setAll(node);
    }

    private Node buildOverview() {
        VBox root = page(
            "Overview",
            isManager()
                ? "Current hotel status and active guest stays."
                : "Your stay summary, room options, and booking activity."
        );

        TilePane stats = new TilePane();
        stats.setPrefColumns(4);
        stats.setHgap(16);
        stats.setVgap(16);
        if (isManager()) {
            stats.getChildren().addAll(
                statCard(String.valueOf(service.getTotalRooms()), "Rooms", BLUE),
                statCard(String.valueOf(service.getBookedRooms()), "Occupied", RED),
                statCard(String.valueOf(service.getPaidInvoicesCount()), "Paid Bills", GREEN),
                statCard("Rs." + (int) service.getPendingRevenue(), "Pending Billing", GOLD)
            );
        } else {
            Customer customer = service.findCustomerByUser(currentUser);
            List<Booking> myBookings = service.getBookingsForCustomer(currentUser.getCustomerId());
            long activeCount = myBookings.stream().filter(booking -> Booking.ACTIVE.equals(booking.getStatus())).count();
            stats.getChildren().addAll(
                statCard(customer == null ? "-" : customer.getTier(), "Membership", BLUE),
                statCard(String.valueOf(customer == null ? 0 : customer.getLoyaltyPoints()), "Loyalty Points", RED),
                statCard(String.valueOf(myBookings.size()), "My Bookings", GREEN),
                statCard(String.valueOf(activeCount), "Active Stays", GOLD)
            );
        }

        VBox activeCard = cardBox(isManager() ? "Active Stays" : "My Stays");
        List<Booking> activeBookings = isManager()
            ? service.getActiveBookings()
            : service.getBookingsForCustomer(currentUser.getCustomerId());
        if (activeBookings.isEmpty()) {
            activeCard.getChildren().add(muted(isManager() ? "No active bookings yet." : "You do not have any bookings yet."));
        } else {
            for (Booking booking : activeBookings) {
                if (isCustomer() && !Booking.ACTIVE.equals(booking.getStatus())) {
                    continue;
                }
                HBox row = new HBox(12);
                row.setAlignment(Pos.CENTER_LEFT);
                row.getStyleClass().add("list-row");
                Label guest = new Label(booking.getCustomer().getName());
                guest.getStyleClass().add("row-strong");
                Label room = new Label("Room #" + booking.getRoom().getRoomNumber() + " " + booking.getRoom().getRoomType());
                room.getStyleClass().add("row-muted");
                Label dates = new Label(booking.getCheckInStr() + " to " + booking.getCheckOutStr());
                dates.getStyleClass().add("row-muted");
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                Label amount = pill("Rs." + (int) booking.getTotal(), "gold-pill");
                Label stayState = pill(booking.getStatus(), "dark-pill");
                row.getChildren().addAll(guest, room, dates, spacer, stayState, amount);
                activeCard.getChildren().add(row);
            }
        }

        root.getChildren().addAll(stats, activeCard);
        return scroll(root);
    }

    private Node buildRooms() {
        SplitPane split = new SplitPane();
        split.setDividerPositions(0.7);

        VBox left = page("Room Management", "View rooms, availability, room type, and pricing.");
        TableView<Room> table = new TableView<>();
        table.getStyleClass().add("table-card");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().addAll(
            numberColumn("Room #", Room::getRoomNumber),
            textColumn("Type", Room::getRoomType),
            textColumn("Price", room -> "Rs." + (int) room.getBasePrice()),
            textColumn("Demand", room -> room.getDemandScore() + "%"),
            textColumn("Status", room -> room.isBooked() ? "Booked" : "Available"),
            textColumn("Amenities", Room::getAmenitiesDescription)
        );
        refreshRooms(table);
        VBox.setVgrow(table, Priority.ALWAYS);
        left.getChildren().add(table);

        VBox right = new VBox(16);
        right.setPadding(new Insets(20));

        VBox addCard = cardBox("Add Room");
        TextField roomNoField = prompt("Room number");
        ComboBox<String> typeCombo = combo("Standard", "Deluxe", "Suite");
        TextField priceField = prompt("Base price");
        ComboBox<String> poolCombo = combo("No Pool", "With Pool");
        poolCombo.setDisable(true);
        typeCombo.valueProperty().addListener((obs, oldValue, newValue) -> poolCombo.setDisable(!"Suite".equals(newValue)));
        Label status = muted("");
        Button saveBtn = accentButton("Save Room");
        saveBtn.setOnAction(event -> {
            try {
                int roomNo = Integer.parseInt(roomNoField.getText().trim());
                double price = Double.parseDouble(priceField.getText().trim());
                Room room = switch (typeCombo.getValue()) {
                    case "Deluxe" -> new DeluxeRoom(roomNo, price);
                    case "Suite" -> new SuiteRoom(roomNo, price, "With Pool".equals(poolCombo.getValue()));
                    default -> new StandardRoom(roomNo, price);
                };
                if (service.addRoom(room)) {
                    refreshRooms(table);
                    status.setText("Room saved successfully.");
                    roomNoField.clear();
                    priceField.clear();
                } else {
                    status.setText("Room number already exists.");
                }
            } catch (NumberFormatException ex) {
                error("Enter valid room number and price.");
            }
        });
        addCard.getChildren().addAll(formRow("Type", typeCombo), formRow("Room #", roomNoField), formRow("Price", priceField), formRow("Pool", poolCombo), saveBtn, status);

        VBox demandCard = cardBox("Dynamic Pricing");
        TextField demandRoomField = prompt("Room number");
        Slider slider = new Slider(0, 100, 35);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        Label sliderValue = muted("Demand: 35%");
        slider.valueProperty().addListener((obs, oldValue, newValue) -> sliderValue.setText("Demand: " + newValue.intValue() + "%"));
        Button updateBtn = subtleButton("Update Demand");
        updateBtn.setOnAction(event -> {
            try {
                service.updateDemand(Integer.parseInt(demandRoomField.getText().trim()), (int) slider.getValue());
                refreshRooms(table);
            } catch (NumberFormatException ex) {
                error("Enter a valid room number.");
            }
        });
        demandCard.getChildren().addAll(formRow("Room #", demandRoomField), slider, sliderValue, updateBtn);

        Accordion accordion = new Accordion(new TitledPane("Add / Edit", addCard), new TitledPane("Demand", demandCard));
        accordion.setExpandedPane(accordion.getPanes().get(0));
        right.getChildren().add(accordion);

        split.getItems().addAll(left, right);
        return split;
    }

    private Node buildCustomers() {
        HBox root = new HBox(18);
        VBox left = page("Customers", "Register guests and view loyalty details.");
        HBox.setHgrow(left, Priority.ALWAYS);

        TableView<Customer> table = new TableView<>();
        table.getStyleClass().add("table-card");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().addAll(
            numberColumn("ID", Customer::getId),
            textColumn("Name", Customer::getName),
            textColumn("Contact", Customer::getContact),
            textColumn("Email", Customer::getEmail),
            textColumn("Tier", Customer::getTier),
            textColumn("Points", customer -> String.valueOf(customer.getLoyaltyPoints()))
        );
        refreshCustomers(table);
        VBox.setVgrow(table, Priority.ALWAYS);
        left.getChildren().add(table);

        VBox right = cardBox("Register Guest");
        right.setPrefWidth(320);
        TextField nameField = prompt("Full name");
        TextField contactField = prompt("Phone number");
        TextField emailField = prompt("Email");
        Label output = muted("");
        Button addBtn = accentButton("Add Customer");
        addBtn.setOnAction(event -> {
            if (nameField.getText().isBlank() || contactField.getText().isBlank() || emailField.getText().isBlank()) {
                error("Please fill all customer details.");
                return;
            }
            Customer customer = service.addCustomer(nameField.getText().trim(), contactField.getText().trim(), emailField.getText().trim());
            refreshCustomers(table);
            output.setText("Saved customer ID: " + customer.getId());
            nameField.clear();
            contactField.clear();
            emailField.clear();
        });
        right.getChildren().addAll(formRow("Name", nameField), formRow("Contact", contactField), formRow("Email", emailField), addBtn, output);

        root.getChildren().addAll(left, right);
        return root;
    }

    private Node buildBookings() {
        HBox root = new HBox(18);
        VBox left = page("Bookings", isManager() ? "Create and review guest bookings." : "Create your booking and view your receipts.");
        left.setPrefWidth(420);

        VBox bookingCard = cardBox("Create Booking");
        TextField customerIdField = prompt("Customer ID");
        TextField roomField = prompt("Room number");
        DatePicker checkIn = new DatePicker(LocalDate.now());
        DatePicker checkOut = new DatePicker(LocalDate.now().plusDays(2));
        TextField pointsField = prompt("Redeem points");
        pointsField.setText("0");
        Label estimateLabel = muted("Estimated total will appear here.");
        TextArea receiptArea = new TextArea();
        receiptArea.setEditable(false);
        receiptArea.setPrefRowCount(12);
        receiptArea.getStyleClass().add("receipt-area");
        TableView<Room> customerAvailableRoomsTable = isCustomer() ? availableRoomsTable() : null;
        if (customerAvailableRoomsTable != null) {
            customerAvailableRoomsTable.setItems(FXCollections.observableArrayList(service.getAvailableRooms()));
        }

        Button estimateBtn = subtleButton("Calculate Estimate");
        estimateBtn.setOnAction(event -> {
            try {
                Room room = service.findRoom(Integer.parseInt(roomField.getText().trim()));
                if (room == null) {
                    error("Room not found.");
                    return;
                }
                int nights = Math.max(1, (int) ChronoUnit.DAYS.between(checkIn.getValue(), checkOut.getValue()));
                double total = room.calculateTariff(nights);
                estimateLabel.setText("Estimated: Rs." + (int) total + " for " + nights + " nights");
            } catch (Exception ex) {
                error("Check customer, room, and dates.");
            }
        });

        TableView<Booking> bookingsTable = bookingTable();
        refreshBookings(bookingsTable);

        Button confirmBtn = accentButton("Confirm Booking");
        confirmBtn.setOnAction(event -> {
            try {
                int customerId = isCustomer()
                    ? currentUser.getCustomerId()
                    : Integer.parseInt(customerIdField.getText().trim());
                Booking booking = service.createBooking(
                    customerId,
                    Integer.parseInt(roomField.getText().trim()),
                    checkIn.getValue(),
                    checkOut.getValue(),
                    Integer.parseInt(pointsField.getText().trim())
                );
                if (booking == null) {
                    error("Booking failed. Check IDs, dates, or availability.");
                    return;
                }
                receiptArea.setText(booking.generateBill());
                refreshBookings(bookingsTable);
                if (customerAvailableRoomsTable != null) {
                    customerAvailableRoomsTable.setItems(FXCollections.observableArrayList(service.getAvailableRooms()));
                }
            } catch (NumberFormatException ex) {
                error("Enter valid numeric values.");
            }
        });

        if (isManager()) {
            bookingCard.getChildren().add(formRow("Customer", customerIdField));
        } else {
            customerIdField.setText(String.valueOf(currentUser.getCustomerId()));
            customerIdField.setEditable(false);
            bookingCard.getChildren().add(formRow("Customer ID", customerIdField));
        }

        bookingCard.getChildren().addAll(
            formRow("Room", roomField),
            formRow("Check-in", checkIn),
            formRow("Check-out", checkOut),
            formRow("Points", pointsField),
            estimateBtn,
            estimateLabel,
            new Separator(),
            confirmBtn,
            receiptArea
        );
        left.getChildren().add(bookingCard);

        VBox right = page("Booking Register", isManager() ? "All guest bookings and stay records." : "Available rooms are shown before your booking history.");
        if (isCustomer()) {
            VBox availableCard = cardBox("Available Rooms");
            availableCard.getChildren().add(customerAvailableRoomsTable);
            right.getChildren().add(availableCard);
        }
        right.getChildren().add(bookingsTable);
        HBox.setHgrow(right, Priority.ALWAYS);

        root.getChildren().addAll(left, right);
        return root;
    }

    private Node buildBilling() {
        VBox root = page("Billing Management", "Track invoices, payment status, and collections.");

        TilePane billingStats = new TilePane();
        billingStats.setPrefColumns(3);
        billingStats.setHgap(16);
        billingStats.getChildren().addAll(
            statCard("Rs." + (int) service.getTotalRevenue(), "Gross Billing", BLUE),
            statCard("Rs." + (int) service.getPaidRevenue(), "Collected", GREEN),
            statCard("Rs." + (int) service.getPendingRevenue(), "Pending", GOLD)
        );

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        TableView<Booking> invoiceTable = bookingTable();
        invoiceTable.getColumns().add(textColumn("Bill", Booking::getPaymentStatus));
        invoiceTable.getColumns().add(textColumn("Method", Booking::getPaymentMethod));
        refreshBookings(invoiceTable);

        VBox invoicePane = new VBox(14, invoiceTable);
        invoicePane.setPadding(new Insets(12));

        VBox payBox = cardBox("Receive Payment");
        TextField bookingIdField = prompt("Booking ID");
        ComboBox<String> paymentMethod = combo("Cash", "Card", "UPI", "Bank Transfer");
        TextArea invoiceText = new TextArea();
        invoiceText.setEditable(false);
        invoiceText.setPrefRowCount(10);
        invoiceText.getStyleClass().add("receipt-area");
        Button loadBtn = subtleButton("Load Invoice");
        loadBtn.setOnAction(event -> {
            try {
                Booking booking = service.findBooking(Integer.parseInt(bookingIdField.getText().trim()));
                if (booking == null) {
                    error("Booking not found.");
                    return;
                }
                invoiceText.setText(booking.generateBill());
            } catch (NumberFormatException ex) {
                error("Enter a valid booking ID.");
            }
        });
        Button payBtn = accentButton("Mark As Paid");
        payBtn.setOnAction(event -> {
            try {
                Booking booking = service.markInvoicePaid(Integer.parseInt(bookingIdField.getText().trim()), paymentMethod.getValue());
                if (booking == null) {
                    error("Booking not found.");
                    return;
                }
                invoiceText.setText(booking.generateBill());
                setContent(buildBilling());
            } catch (NumberFormatException ex) {
                error("Enter a valid booking ID.");
            }
        });
        payBox.getChildren().addAll(formRow("Booking", bookingIdField), formRow("Method", paymentMethod), loadBtn, payBtn, invoiceText);

        tabs.getTabs().addAll(new Tab("Invoices", invoicePane), new Tab("Receive Payment", payBox));
        root.getChildren().addAll(billingStats, tabs);
        return root;
    }

    private Node buildCheckout() {
        HBox root = new HBox(18);

        VBox left = page("Check-out", "Close the stay, free the room, and award loyalty points.");
        TableView<Booking> activeTable = bookingTable();
        activeTable.setItems(FXCollections.observableArrayList(service.getActiveBookings()));
        left.getChildren().add(activeTable);
        HBox.setHgrow(left, Priority.ALWAYS);

        VBox right = cardBox("Process Check-out");
        right.setPrefWidth(360);
        TextField bookingIdField = prompt("Booking ID");
        TextArea output = new TextArea();
        output.setEditable(false);
        output.setPrefRowCount(16);
        output.getStyleClass().add("receipt-area");
        activeTable.setOnMouseClicked(event -> {
            Booking selected = activeTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                bookingIdField.setText(String.valueOf(selected.getBookingId()));
            }
        });
        Button checkoutAction = accentButton("Checkout Guest");
        checkoutAction.setOnAction(event -> {
            try {
                Booking booking = service.checkout(Integer.parseInt(bookingIdField.getText().trim()));
                if (booking == null) {
                    error("Active booking not found.");
                    return;
                }
                output.setText(booking.generateBill() + "\n\nUpdated loyalty points: " + booking.getCustomer().getLoyaltyPoints());
                activeTable.setItems(FXCollections.observableArrayList(service.getActiveBookings()));
            } catch (NumberFormatException ex) {
                error("Enter a valid booking ID.");
            }
        });
        right.getChildren().addAll(formRow("Booking", bookingIdField), checkoutAction, output);

        root.getChildren().addAll(left, right);
        return root;
    }

    private Node buildSmartFinder() {
        VBox root = page("Smart Room Finder", "Find available rooms that match guest budget and room type.");
        VBox form = cardBox("Guest Preference");
        TextField budgetField = prompt("Budget per night");
        ComboBox<String> typeCombo = combo("Any", "Standard", "Deluxe", "Suite");
        TextField nightsField = prompt("Nights");
        nightsField.setText("2");
        FlowPane results = new FlowPane(14, 14);
        results.setPadding(new Insets(6, 0, 0, 0));

        Button searchBtn = accentButton("Find Best Match");
        searchBtn.setOnAction(event -> {
            try {
                results.getChildren().clear();
                int nights = Integer.parseInt(nightsField.getText().trim());
                List<Room> rooms = service.getRecommendations(
                    Double.parseDouble(budgetField.getText().trim()),
                    typeCombo.getValue(),
                    nights
                );
                if (rooms.isEmpty()) {
                    results.getChildren().add(muted("No matching rooms."));
                    return;
                }
                int rank = 1;
                for (Room room : rooms) {
                    VBox roomCard = cardBox("#" + rank + " Room " + room.getRoomNumber() + " - " + room.getRoomType());
                    Label details = muted(
                        "Tariff: Rs." + (int) room.calculateTariff(nights)
                            + " | Demand: " + room.getDemandScore() + "% | " + room.getAmenitiesDescription()
                    );
                    details.setWrapText(true);
                    roomCard.getChildren().add(details);
                    roomCard.setPrefWidth(310);
                    results.getChildren().add(roomCard);
                    rank++;
                }
            } catch (NumberFormatException ex) {
                error("Enter valid budget and nights.");
            }
        });

        form.getChildren().addAll(formRow("Budget", budgetField), formRow("Type", typeCombo), formRow("Nights", nightsField), searchBtn);
        root.getChildren().addAll(form, results);
        return scroll(root);
    }

    private Node buildAnalytics() {
        VBox root = page("Analytics", "Occupancy, revenue, room type usage, and demand overview.");

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);
        grid.add(statCard(String.format("%.0f%%", service.getOccupancyPct()), "Occupancy", GOLD), 0, 0);
        grid.add(statCard("Rs." + (int) service.getPaidRevenue(), "Collected", GREEN), 1, 0);
        grid.add(statCard("Rs." + (int) service.getPendingRevenue(), "Due", RED), 2, 0);

        VBox typeBreakdown = cardBox("Room Type Breakdown");
        for (String type : List.of("Standard", "Deluxe", "Suite")) {
            long total = service.getAllRooms().stream().filter(room -> room.getRoomType().equals(type)).count();
            long booked = service.getAllRooms().stream().filter(room -> room.getRoomType().equals(type) && room.isBooked()).count();
            double pct = total == 0 ? 0 : booked * 100.0 / total;
            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            Label label = new Label(type);
            label.setMinWidth(90);
            Pane barFill = new Pane();
            barFill.setPrefWidth(3.0 * pct);
            barFill.getStyleClass().add("bar-fill");
            Region barBg = new Region();
            barBg.getStyleClass().add("bar-bg");
            StackPane bar = new StackPane(barBg, barFill);
            StackPane.setAlignment(barFill, Pos.CENTER_LEFT);
            bar.setPrefWidth(320);
            row.getChildren().addAll(label, bar, muted(booked + "/" + total));
            typeBreakdown.getChildren().add(row);
        }

        VBox demandGrid = cardBox("Demand Heat View");
        FlowPane tiles = new FlowPane(10, 10);
        for (Room room : service.getAllRooms()) {
            VBox tile = new VBox(4);
            tile.setAlignment(Pos.CENTER);
            tile.setPadding(new Insets(12));
            tile.getStyleClass().add(room.getDemandScore() >= 70 ? "hot-tile" : room.getDemandScore() >= 40 ? "warm-tile" : "cool-tile");
            tile.getChildren().addAll(
                new Label("Room " + room.getRoomNumber()),
                new Label(room.getRoomType()),
                pill(room.getDemandScore() + "%", "dark-pill"),
                pill(room.isBooked() ? "Booked" : "Free", room.isBooked() ? "danger-pill" : "success-pill")
            );
            tiles.getChildren().add(tile);
        }
        demandGrid.getChildren().add(tiles);

        root.getChildren().addAll(grid, typeBreakdown, demandGrid);
        return scroll(root);
    }

    private TableView<Booking> bookingTable() {
        TableView<Booking> table = new TableView<>();
        table.getStyleClass().add("table-card");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().addAll(
            numberColumn("ID", Booking::getBookingId),
            textColumn("Guest", booking -> booking.getCustomer().getName()),
            textColumn("Room", booking -> "#" + booking.getRoom().getRoomNumber()),
            textColumn("Check-in", Booking::getCheckInStr),
            textColumn("Check-out", Booking::getCheckOutStr),
            textColumn("Stay", Booking::getStatus),
            textColumn("Total", booking -> "Rs." + (int) booking.getTotal())
        );
        VBox.setVgrow(table, Priority.ALWAYS);
        return table;
    }

    private TableView<Room> availableRoomsTable() {
        TableView<Room> table = new TableView<>();
        table.getStyleClass().add("table-card");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().addAll(
            numberColumn("Room #", Room::getRoomNumber),
            textColumn("Type", Room::getRoomType),
            textColumn("Price/Night", room -> "Rs." + (int) room.calculateTariff(1)),
            textColumn("Amenities", Room::getAmenitiesDescription)
        );
        table.setPrefHeight(220);
        return table;
    }

    private void refreshRooms(TableView<Room> table) {
        table.setItems(FXCollections.observableArrayList(service.getAllRooms()));
    }

    private void refreshCustomers(TableView<Customer> table) {
        table.setItems(FXCollections.observableArrayList(service.getAllCustomers()));
    }

    private void refreshBookings(TableView<Booking> table) {
        List<Booking> bookings = isCustomer()
            ? service.getBookingsForCustomer(currentUser.getCustomerId())
            : service.getAllBookings();
        table.setItems(FXCollections.observableArrayList(bookings));
    }

    private VBox page(String title, String subtitle) {
        VBox box = new VBox(18);
        box.setPadding(new Insets(20));
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("page-title");
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("page-subtitle");
        box.getChildren().addAll(titleLabel, subtitleLabel);
        return box;
    }

    private VBox cardBox(String heading) {
        VBox box = new VBox(12);
        box.getStyleClass().add("card");
        if (!heading.isBlank()) {
            Label headingLabel = new Label(heading);
            headingLabel.getStyleClass().add("card-title");
            box.getChildren().add(headingLabel);
        }
        return box;
    }

    private VBox statCard(String value, String label, String color) {
        VBox box = new VBox(6);
        box.getStyleClass().add("stat-card");
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill: " + color + ";");
        valueLabel.getStyleClass().add("stat-value");
        Label labelLabel = new Label(label);
        labelLabel.getStyleClass().add("page-subtitle");
        box.getChildren().addAll(valueLabel, labelLabel);
        return box;
    }

    private HBox formRow(String label, Node node) {
        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("form-label");
        HBox row = new HBox(12, labelNode, node);
        row.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(node, Priority.ALWAYS);
        return row;
    }

    private TextField prompt(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        return field;
    }

    private ComboBox<String> combo(String... items) {
        ComboBox<String> box = new ComboBox<>();
        box.getItems().addAll(items);
        box.setValue(items[0]);
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    private Button accentButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("accent-button");
        return button;
    }

    private Button subtleButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("subtle-button");
        return button;
    }

    private Label pill(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().addAll("pill", styleClass);
        return label;
    }

    private Label muted(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("page-subtitle");
        return label;
    }

    private ScrollPane scroll(Node node) {
        ScrollPane scrollPane = new ScrollPane(node);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.getStyleClass().add("page-scroll");
        return scrollPane;
    }

    private void error(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private <T> TableColumn<T, Number> numberColumn(String title, NumberExtractor<T> extractor) {
        TableColumn<T, Number> column = new TableColumn<>(title);
        column.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(extractor.get(data.getValue())));
        return column;
    }

    private <T> TableColumn<T, String> textColumn(String title, TextExtractor<T> extractor) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setCellValueFactory(data -> new ReadOnlyStringWrapper(extractor.get(data.getValue())));
        return column;
    }

    @FunctionalInterface
    private interface NumberExtractor<T> {
        Number get(T value);
    }

    @FunctionalInterface
    private interface TextExtractor<T> {
        String get(T value);
    }
}
