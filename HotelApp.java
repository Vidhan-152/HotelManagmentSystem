package hotel;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.ArrayList;

public class HotelApp extends Application {

    HotelService service = new HotelService();

    // Colors - plain hex only (no rgba - not supported in JavaFX CSS)
    static final String GOLD  = "#C9A84C";
    static final String DARK  = "#1A1A2E";
    static final String PANEL = "#16213E";
    static final String CARD  = "#0F3460";
    static final String CARD2 = "#0a2040";
    static final String GREEN = "#4CAF50";
    static final String RED   = "#F44336";
    static final String MUTED = "#9E9E9E";
    static final String WHITE = "#ECECEC";
    static final String BLUE  = "#58A6FF";

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + DARK + ";");

        VBox sidebar = buildSidebar();
        root.setLeft(sidebar);

        StackPane content = new StackPane();
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: " + DARK + ";");
        root.setCenter(content);

        Button[] navBtns = sidebar.getChildren().stream()
            .filter(n -> n instanceof Button).toArray(Button[]::new);

        Runnable[] panels = {
            () -> content.getChildren().setAll(overviewPanel()),
            () -> content.getChildren().setAll(roomPanel()),
            () -> content.getChildren().setAll(customerPanel()),
            () -> content.getChildren().setAll(bookingPanel()),
            () -> content.getChildren().setAll(checkoutPanel()),
            () -> content.getChildren().setAll(smartPanel()),
            () -> content.getChildren().setAll(analyticsPanel())
        };

        for (int i = 0; i < navBtns.length; i++) {
            final int idx = i;
            navBtns[i].setOnAction(e -> {
                for (Button b : navBtns) b.setStyle(navOff());
                navBtns[idx].setStyle(navOn());
                panels[idx].run();
            });
        }

        navBtns[0].setStyle(navOn());
        panels[0].run();

        stage.setScene(new Scene(root, 1150, 720));
        stage.setTitle("Grand Vista - Hotel Management System");
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();
    }

    // ---- SIDEBAR ----
    VBox buildSidebar() {
        VBox box = new VBox(2);
        box.setPrefWidth(190);
        box.setStyle("-fx-background-color: " + PANEL + ";");

        Label name = new Label("GRAND VISTA");
        name.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + GOLD + ";");
        Label sub = new Label("Hotel Management");
        sub.setStyle("-fx-font-size: 11px; -fx-text-fill: " + MUTED + ";");
        VBox logoBox = new VBox(4, name, sub);
        logoBox.setPadding(new Insets(22, 16, 18, 16));
        box.getChildren().add(logoBox);

        for (String lbl : new String[]{"Overview","Rooms","Customers","Bookings","Check-out","Smart Finder","Analytics"}) {
            Button b = new Button(lbl);
            b.setMaxWidth(Double.MAX_VALUE);
            b.setAlignment(Pos.CENTER_LEFT);
            b.setStyle(navOff());
            box.getChildren().add(b);
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        Label footer = new Label("Java OOP Project");
        footer.setStyle("-fx-font-size: 10px; -fx-text-fill: " + MUTED + "; -fx-padding: 12 16 16 16;");
        box.getChildren().addAll(spacer, footer);
        return box;
    }

    String navOn() {
        return "-fx-background-color: #1e3a6e; -fx-text-fill: " + GOLD + "; -fx-font-size: 13px;"
             + "-fx-padding: 10 12 10 18; -fx-font-weight: bold; -fx-cursor: hand;"
             + "-fx-background-radius: 0; -fx-alignment: CENTER-LEFT;";
    }
    String navOff() {
        return "-fx-background-color: transparent; -fx-text-fill: " + WHITE + "; -fx-font-size: 13px;"
             + "-fx-padding: 10 12 10 18; -fx-cursor: hand;"
             + "-fx-background-radius: 0; -fx-alignment: CENTER-LEFT;";
    }

    // ---- HELPERS ----
    Label title(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + WHITE + ";");
        return l;
    }
    Label muted(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: " + MUTED + ";");
        return l;
    }
    Label colored(String t, String color) {
        Label l = new Label(t);
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: " + color + ";");
        return l;
    }
    TextField field(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle("-fx-background-color: " + CARD2 + "; -fx-text-fill: " + WHITE + ";"
                  + "-fx-prompt-text-fill: " + MUTED + "; -fx-background-radius: 4; -fx-padding: 7 10;");
        tf.setMaxWidth(Double.MAX_VALUE);
        return tf;
    }
    ComboBox<String> combo(String... items) {
        ComboBox<String> cb = new ComboBox<>();
        cb.getItems().addAll(items);
        cb.setValue(items[0]);
        cb.setMaxWidth(Double.MAX_VALUE);
        return cb;
    }
    DatePicker datePicker() {
        DatePicker dp = new DatePicker();
        dp.setMaxWidth(Double.MAX_VALUE);
        return dp;
    }
    Button goldBtn(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: " + GOLD + "; -fx-text-fill: #1a1a2e; -fx-font-weight: bold;"
                 + "-fx-padding: 8 18; -fx-background-radius: 5; -fx-cursor: hand;");
        b.setMaxWidth(Double.MAX_VALUE);
        return b;
    }
    Button blueBtn(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: #1565C0; -fx-text-fill: white; -fx-font-weight: bold;"
                 + "-fx-padding: 8 18; -fx-background-radius: 5; -fx-cursor: hand;");
        b.setMaxWidth(Double.MAX_VALUE);
        return b;
    }
    VBox card(String heading) {
        VBox c = new VBox(10);
        c.setStyle("-fx-background-color: " + CARD + "; -fx-background-radius: 8; -fx-padding: 14;");
        if (!heading.isEmpty()) {
            Label h = new Label(heading);
            h.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + WHITE + ";");
            c.getChildren().addAll(h, new Separator());
        }
        return c;
    }
    HBox row(String label, javafx.scene.Node ctrl) {
        Label l = new Label(label);
        l.setMinWidth(115);
        l.setStyle("-fx-text-fill: " + WHITE + "; -fx-font-size: 12px;");
        HBox r = new HBox(10, l, ctrl);
        HBox.setHgrow(ctrl, Priority.ALWAYS);
        r.setAlignment(Pos.CENTER_LEFT);
        return r;
    }
    VBox statTile(String value, String label, String color) {
        VBox tile = new VBox(4);
        tile.setStyle("-fx-background-color: " + CARD + "; -fx-background-radius: 8; -fx-padding: 16;");
        Label v = new Label(value);
        v.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        Label l = new Label(label);
        l.setStyle("-fx-font-size: 11px; -fx-text-fill: " + MUTED + ";");
        tile.getChildren().addAll(v, l);
        return tile;
    }
    GridPane statsGrid(VBox... tiles) {
        GridPane g = new GridPane();
        g.setHgap(12);
        for (int i = 0; i < tiles.length; i++) {
            g.add(tiles[i], i, 0);
            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.ALWAYS);
            cc.setPercentWidth(100.0 / tiles.length);
            g.getColumnConstraints().add(cc);
        }
        return g;
    }
    <T> TableView<T> makeTable() {
        TableView<T> t = new TableView<>();
        t.setStyle("-fx-background-color: " + CARD + ";");
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        t.setPlaceholder(new Label("No records."));
        VBox.setVgrow(t, Priority.ALWAYS);
        return t;
    }
    ScrollPane scroll(javafx.scene.Node node) {
        ScrollPane sp = new ScrollPane(node);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: " + DARK + "; -fx-background-color: " + DARK + ";");
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(sp, Priority.ALWAYS);
        return sp;
    }
    Label statusLbl() { return new Label(""); }
    void ok(Label l, String msg)  { l.setText(msg); l.setStyle("-fx-font-size: 12px; -fx-text-fill: " + GREEN + ";"); }
    void err(Label l, String msg) { l.setText(msg); l.setStyle("-fx-font-size: 12px; -fx-text-fill: " + RED + ";"); }
    void popup(String msg) { Alert a = new Alert(Alert.AlertType.ERROR); a.setTitle("Error"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait(); }

    // =============================================
    // OVERVIEW
    // =============================================
    ScrollPane overviewPanel() {
        VBox root = new VBox(14);
        root.getChildren().addAll(title("Overview"), muted("Live hotel statistics."));
        root.getChildren().add(statsGrid(
            statTile(String.valueOf(service.getTotalRooms()),             "Total Rooms", BLUE),
            statTile(String.valueOf(service.getBookedRooms()),            "Occupied",    RED),
            statTile(String.valueOf(service.getAvailableCount()),         "Available",   GREEN),
            statTile(String.format("%.0f%%", service.getOccupancyPct()), "Occupancy",   GOLD)
        ));
        VBox staysCard = card("Active Stays");
        ArrayList<Booking> active = service.getActiveBookings();
        if (active.isEmpty()) {
            staysCard.getChildren().add(muted("No active bookings right now."));
        } else {
            for (Booking b : active) {
                HBox r = new HBox(12);
                r.setAlignment(Pos.CENTER_LEFT);
                r.setPadding(new Insets(5, 0, 5, 0));
                Label dot = new Label("●"); dot.setStyle("-fx-text-fill: " + GREEN + "; -fx-font-size: 9px;");
                Label nm = new Label(b.getCustomer().getName()); nm.setStyle("-fx-text-fill: " + WHITE + "; -fx-font-weight: bold;"); nm.setMinWidth(140);
                Label rm = new Label("Room #" + b.getRoom().getRoomNumber() + " " + b.getRoom().getRoomType()); rm.setStyle("-fx-text-fill: " + MUTED + ";"); rm.setMinWidth(150);
                Label dt = new Label(b.getCheckInStr() + " to " + b.getCheckOutStr()); dt.setStyle("-fx-text-fill: " + MUTED + ";");
                Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
                Label amt = new Label("Rs." + (int) b.getTotal()); amt.setStyle("-fx-text-fill: " + GOLD + "; -fx-font-weight: bold;");
                r.getChildren().addAll(dot, nm, rm, dt, sp, amt);
                staysCard.getChildren().add(r);
            }
        }
        root.getChildren().add(staysCard);
        return scroll(root);
    }

    // =============================================
    // ROOMS
    // =============================================
    HBox roomPanel() {
        HBox root = new HBox(14);

        VBox left = new VBox(10);
        HBox.setHgrow(left, Priority.ALWAYS);
        left.getChildren().addAll(title("Room Management"), muted("View and manage all rooms."));

        TableView<Room> tbl = makeTable();
        TableColumn<Room, Number> numCol = new TableColumn<>("Room #");
        numCol.setCellValueFactory(d -> new javafx.beans.property.SimpleIntegerProperty(d.getValue().getRoomNumber()));
        TableColumn<Room, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getRoomType()));
        TableColumn<Room, String> priceCol = new TableColumn<>("Base Price");
        priceCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty("Rs." + (int) d.getValue().getBasePrice()));
        TableColumn<Room, String> demandCol = new TableColumn<>("Demand%");
        demandCol.setCellFactory(c -> new TableCell<>() {
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) { setText(null); return; }
                Room r = getTableRow().getItem();
                setText(r.getDemandScore() + "%");
                String col = r.getDemandScore() >= 70 ? RED : r.getDemandScore() >= 40 ? "#FF9800" : GREEN;
                setStyle("-fx-text-fill: " + col + "; -fx-font-weight: bold;");
            }
        });
        TableColumn<Room, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellFactory(c -> new TableCell<>() {
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) { setText(null); return; }
                Room r = getTableRow().getItem();
                setText(r.isBooked() ? "BOOKED" : "AVAILABLE");
                setStyle("-fx-text-fill: " + (r.isBooked() ? RED : GREEN) + "; -fx-font-weight: bold;");
            }
        });
        TableColumn<Room, String> amenCol = new TableColumn<>("Amenities");
        amenCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getAmenitiesDescription()));
        tbl.getColumns().addAll(numCol, typeCol, priceCol, demandCol, statusCol, amenCol);
        tbl.setItems(FXCollections.observableArrayList(service.getAllRooms()));
        left.getChildren().add(tbl);

        VBox right = new VBox(12);
        right.setPrefWidth(255);

        VBox addCard = card("Add New Room");
        TextField numF = field("Room number");
        ComboBox<String> typeC = combo("Standard", "Deluxe", "Suite");
        TextField priceF = field("Price per night");
        CheckBox poolCB = new CheckBox("Has Private Pool");
        poolCB.setStyle("-fx-text-fill: " + WHITE + ";");
        poolCB.setVisible(false);
        typeC.setOnAction(e -> poolCB.setVisible(typeC.getValue().equals("Suite")));
        Label addSt = statusLbl();
        Button addBtn = goldBtn("Add Room");
        addBtn.setOnAction(e -> {
            try {
                int num = Integer.parseInt(numF.getText().trim());
                double price = Double.parseDouble(priceF.getText().trim());
                if (price <= 0) { err(addSt, "Price must be positive."); return; }
                Room r = switch (typeC.getValue()) {
                    case "Deluxe" -> new DeluxeRoom(num, price);
                    case "Suite"  -> new SuiteRoom(num, price, poolCB.isSelected());
                    default       -> new StandardRoom(num, price);
                };
                if (service.addRoom(r)) {
                    tbl.setItems(FXCollections.observableArrayList(service.getAllRooms()));
                    numF.clear(); priceF.clear();
                    ok(addSt, "Room " + num + " added!");
                } else err(addSt, "Room number already exists.");
            } catch (NumberFormatException ex) { err(addSt, "Enter valid numbers."); }
        });
        addCard.getChildren().addAll(row("Room #:", numF), row("Type:", typeC), row("Price:", priceF), poolCB, addBtn, addSt);

        VBox demCard = card("Dynamic Pricing");
        demCard.getChildren().add(muted("Adjust demand (0-100) to change room price."));
        TextField dRoomF = field("Room number");
        Slider slider = new Slider(0, 100, 30);
        slider.setShowTickLabels(true); slider.setMajorTickUnit(25);
        Label dVal = new Label("Demand: 30%");
        dVal.setStyle("-fx-text-fill: " + GOLD + "; -fx-font-size: 12px;");
        slider.valueProperty().addListener((o, ov, nv) -> dVal.setText("Demand: " + nv.intValue() + "%"));
        Label dSt = statusLbl();
        Button dBtn = blueBtn("Update Demand");
        dBtn.setOnAction(e -> {
            try {
                int rn = Integer.parseInt(dRoomF.getText().trim());
                service.updateDemand(rn, (int) slider.getValue());
                tbl.setItems(FXCollections.observableArrayList(service.getAllRooms()));
                ok(dSt, "Updated room " + rn);
            } catch (NumberFormatException ex) { err(dSt, "Enter valid room number."); }
        });
        demCard.getChildren().addAll(row("Room #:", dRoomF), slider, dVal, dBtn, dSt);

        right.getChildren().addAll(addCard, demCard);
        root.getChildren().addAll(left, right);
        return root;
    }

    // =============================================
    // CUSTOMERS
    // =============================================
    HBox customerPanel() {
        HBox root = new HBox(14);
        VBox left = new VBox(10);
        HBox.setHgrow(left, Priority.ALWAYS);
        left.getChildren().addAll(title("Customers"), muted("Register guests and view loyalty status."));

        TableView<Customer> tbl = makeTable();
        TableColumn<Customer, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(d -> new javafx.beans.property.SimpleIntegerProperty(d.getValue().getId()));
        TableColumn<Customer, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getName()));
        TableColumn<Customer, String> conCol = new TableColumn<>("Contact");
        conCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getContact()));
        TableColumn<Customer, String> tierCol = new TableColumn<>("Tier");
        tierCol.setCellFactory(c -> new TableCell<>() {
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) { setText(null); return; }
                String t = getTableRow().getItem().getTier();
                setText(t);
                String col = t.equals("Gold") ? GOLD : t.equals("Silver") ? "#BDBDBD" : "#CD7F32";
                setStyle("-fx-text-fill: " + col + "; -fx-font-weight: bold;");
            }
        });
        TableColumn<Customer, String> ptsCol = new TableColumn<>("Points");
        ptsCol.setCellFactory(c -> new TableCell<>() {
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) { setText(null); return; }
                setText(getTableRow().getItem().getLoyaltyPoints() + " pts");
                setStyle("-fx-text-fill: " + BLUE + ";");
            }
        });
        TableColumn<Customer, String> staysCol = new TableColumn<>("Stays");
        staysCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(String.valueOf(d.getValue().getTotalStays())));
        tbl.getColumns().addAll(idCol, nameCol, conCol, tierCol, ptsCol, staysCol);
        tbl.setItems(FXCollections.observableArrayList(service.getAllCustomers()));
        left.getChildren().add(tbl);

        VBox right = new VBox(12);
        right.setPrefWidth(255);

        VBox regCard = card("Register Guest");
        TextField nameF = field("Full name");
        TextField conF  = field("Phone number");
        TextField emF   = field("Email address");
        Label regSt = statusLbl();
        Button regBtn = goldBtn("Register");
        regBtn.setOnAction(e -> {
            String nm = nameF.getText().trim(), cn = conF.getText().trim();
            if (nm.isEmpty() || cn.isEmpty()) { err(regSt, "Name and contact required."); return; }
            Customer c = service.addCustomer(nm, cn, emF.getText().trim());
            tbl.setItems(FXCollections.observableArrayList(service.getAllCustomers()));
            nameF.clear(); conF.clear(); emF.clear();
            ok(regSt, c.getName() + " added (ID: " + c.getId() + ")");
        });
        regCard.getChildren().addAll(row("Name:", nameF), row("Contact:", conF), row("Email:", emF), regBtn, regSt);

        VBox loyCard = card("Loyalty Tiers");
        for (String[] t : new String[][]{{"Gold","1500+ pts","2x earn",GOLD},{"Silver","500-1499","1.5x earn","#BDBDBD"},{"Bronze","0-499 pts","1x earn","#CD7F32"}}) {
            HBox r = new HBox(10);
            r.setPadding(new Insets(5, 0, 5, 0));
            Label tl = new Label(t[0]); tl.setMinWidth(55); tl.setStyle("-fx-text-fill: " + t[3] + "; -fx-font-weight: bold;");
            r.getChildren().addAll(tl, new VBox(2, colored(t[1], WHITE), muted(t[2])));
            loyCard.getChildren().add(r);
        }
        loyCard.getChildren().addAll(new Separator(), muted("Rs.100 = 1 pt  |  1 pt = Rs.1 off (max 20%)"));

        right.getChildren().addAll(regCard, loyCard);
        root.getChildren().addAll(left, right);
        return root;
    }

    // =============================================
    // BOOKINGS
    // =============================================
    HBox bookingPanel() {
        HBox root = new HBox(14);

        VBox left = new VBox(10);
        left.setPrefWidth(370);
        left.getChildren().addAll(title("New Booking"), muted("Book a room for a guest."));

        VBox form = card("");
        TextField cidF = field("Customer ID");
        Label cidInfo  = muted("Enter ID to look up customer");
        TextField rnF  = field("Room number");
        Label rnInfo   = muted("Enter room number to see details");
        DatePicker inDP  = datePicker(); inDP.setValue(LocalDate.now());
        DatePicker outDP = datePicker(); outDP.setValue(LocalDate.now().plusDays(2));
        TextField ptsF   = field("Loyalty points to redeem");
        ptsF.setText("0");
        Label estimate = new Label("Estimated Total: —");
        estimate.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + GOLD + ";");

        cidF.focusedProperty().addListener((o, ov, nv) -> {
            if (!nv) {
                try {
                    Customer c = service.findCustomer(Integer.parseInt(cidF.getText().trim()));
                    if (c != null) { cidInfo.setText("Found: " + c.getName() + " | " + c.getTier() + " | " + c.getLoyaltyPoints() + " pts"); cidInfo.setStyle("-fx-text-fill: " + GREEN + "; -fx-font-size: 11px;"); }
                    else { cidInfo.setText("Customer not found"); cidInfo.setStyle("-fx-text-fill: " + RED + "; -fx-font-size: 11px;"); }
                } catch (Exception ignored) {}
            }
        });
        rnF.focusedProperty().addListener((o, ov, nv) -> {
            if (!nv) {
                try {
                    Room r = service.findRoom(Integer.parseInt(rnF.getText().trim()));
                    if (r == null) { rnInfo.setText("Room not found"); rnInfo.setStyle("-fx-text-fill: " + RED + "; -fx-font-size: 11px;"); }
                    else if (r.isBooked()) { rnInfo.setText("Room is already BOOKED"); rnInfo.setStyle("-fx-text-fill: " + RED + "; -fx-font-size: 11px;"); }
                    else { rnInfo.setText("Available: " + r.getRoomType() + " | Rs." + (int) r.getBasePrice() + "/night | Demand: " + r.getDemandScore() + "%"); rnInfo.setStyle("-fx-text-fill: " + GREEN + "; -fx-font-size: 11px;"); }
                } catch (Exception ignored) {}
            }
        });

        Button estBtn = blueBtn("Calculate Estimate");
        estBtn.setOnAction(e -> {
            try {
                Room r = service.findRoom(Integer.parseInt(rnF.getText().trim()));
                LocalDate in = inDP.getValue(), out = outDP.getValue();
                int pts = Integer.parseInt(ptsF.getText().trim());
                if (r != null && in != null && out != null && out.isAfter(in)) {
                    int nights = (int) java.time.temporal.ChronoUnit.DAYS.between(in, out);
                    double bill = r.calculateTariff(nights); // Runtime Polymorphism
                    double disc = Math.min(pts, bill * 0.20);
                    estimate.setText("Estimated: Rs." + (int)(bill - disc) + " for " + nights + " nights");
                }
            } catch (Exception ignored) { estimate.setText("Cannot estimate — check inputs."); }
        });

        TextArea receipt = new TextArea();
        receipt.setEditable(false); receipt.setVisible(false); receipt.setPrefRowCount(12);
        receipt.setStyle("-fx-control-inner-background: " + CARD2 + "; -fx-text-fill: " + WHITE + "; -fx-font-family: monospace; -fx-font-size: 11px;");

        Button bookBtn = goldBtn("Confirm Booking");
        bookBtn.setOnAction(e -> {
            try {
                LocalDate in = inDP.getValue(), out = outDP.getValue();
                if (in == null || out == null || !out.isAfter(in)) { popup("Check-out must be after check-in."); return; }
                Booking b = service.createBooking(
                    Integer.parseInt(cidF.getText().trim()),
                    Integer.parseInt(rnF.getText().trim()),
                    in, out, Integer.parseInt(ptsF.getText().trim())
                );
                if (b == null) { popup("Booking failed. Check customer ID, room number, and availability."); return; }
                receipt.setText(b.generateBill()); receipt.setVisible(true);
                cidF.clear(); rnF.clear(); ptsF.setText("0");
                cidInfo.setText("Enter ID to look up customer"); cidInfo.setStyle("-fx-text-fill: " + MUTED + "; -fx-font-size: 11px;");
                rnInfo.setText("Enter room number to see details"); rnInfo.setStyle("-fx-text-fill: " + MUTED + "; -fx-font-size: 11px;");
                estimate.setText("Estimated Total: —");
            } catch (NumberFormatException ex) { popup("Enter valid numeric values."); }
        });

        form.getChildren().addAll(
            row("Customer ID:", cidF), cidInfo, new Separator(),
            row("Room #:", rnF), rnInfo, new Separator(),
            row("Check-in:", inDP), row("Check-out:", outDP), new Separator(),
            row("Points:", ptsF), estBtn, estimate, new Separator(),
            bookBtn, receipt
        );

        ScrollPane formScroll = new ScrollPane(form);
        formScroll.setFitToWidth(true);
        formScroll.setStyle("-fx-background: " + DARK + "; -fx-background-color: " + DARK + ";");
        formScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(formScroll, Priority.ALWAYS);
        left.getChildren().add(formScroll);

        VBox right = new VBox(10);
        HBox.setHgrow(right, Priority.ALWAYS);
        right.getChildren().addAll(title("All Bookings"), buildBookingsTable());

        root.getChildren().addAll(left, right);
        return root;
    }

    TableView<Booking> buildBookingsTable() {
        TableView<Booking> tbl = makeTable();
        TableColumn<Booking, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty("#" + d.getValue().getBookingId()));
        TableColumn<Booking, String> guestCol = new TableColumn<>("Guest");
        guestCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getCustomer().getName()));
        TableColumn<Booking, String> roomCol = new TableColumn<>("Room");
        roomCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty("#" + d.getValue().getRoom().getRoomNumber() + " " + d.getValue().getRoom().getRoomType()));
        TableColumn<Booking, String> inCol = new TableColumn<>("Check-in");
        inCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getCheckInStr()));
        TableColumn<Booking, String> outCol = new TableColumn<>("Check-out");
        outCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getCheckOutStr()));
        TableColumn<Booking, String> totalCol = new TableColumn<>("Total");
        totalCol.setCellFactory(c -> new TableCell<>() {
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) { setText(null); return; }
                setText("Rs." + (int) getTableRow().getItem().getTotal());
                setStyle("-fx-text-fill: " + GOLD + "; -fx-font-weight: bold;");
            }
        });
        TableColumn<Booking, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellFactory(c -> new TableCell<>() {
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) { setText(null); return; }
                String s = getTableRow().getItem().getStatus();
                setText(s);
                setStyle("-fx-text-fill: " + (s.equals(Booking.ACTIVE) ? GREEN : BLUE) + ";");
            }
        });
        tbl.getColumns().addAll(idCol, guestCol, roomCol, inCol, outCol, totalCol, statusCol);
        tbl.setItems(FXCollections.observableArrayList(service.getAllBookings()));
        return tbl;
    }

    // =============================================
    // CHECKOUT
    // =============================================
    HBox checkoutPanel() {
        HBox root = new HBox(14);
        VBox left = new VBox(10);
        HBox.setHgrow(left, Priority.ALWAYS);
        left.getChildren().addAll(title("Check-out"), muted("Select an active booking to check out."));

        TableView<Booking> tbl = makeTable();
        TableColumn<Booking, String> idCol = new TableColumn<>("Booking #");
        idCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty("#" + d.getValue().getBookingId()));
        TableColumn<Booking, String> guestCol = new TableColumn<>("Guest (Tier)");
        guestCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getCustomer().getName() + " (" + d.getValue().getCustomer().getTier() + ")"));
        TableColumn<Booking, String> roomCol = new TableColumn<>("Room");
        roomCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty("#" + d.getValue().getRoom().getRoomNumber() + " " + d.getValue().getRoom().getRoomType()));
        TableColumn<Booking, String> nightsCol = new TableColumn<>("Nights");
        nightsCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(String.valueOf(d.getValue().getNights())));
        TableColumn<Booking, String> totalCol = new TableColumn<>("Amount");
        totalCol.setCellFactory(c -> new TableCell<>() {
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) { setText(null); return; }
                setText("Rs." + (int) getTableRow().getItem().getTotal());
                setStyle("-fx-text-fill: " + GOLD + "; -fx-font-weight: bold;");
            }
        });
        tbl.getColumns().addAll(idCol, guestCol, roomCol, nightsCol, totalCol);
        tbl.setItems(FXCollections.observableArrayList(service.getActiveBookings()));
        left.getChildren().add(tbl);

        VBox right = new VBox(12);
        right.setPrefWidth(280);
        right.getChildren().addAll(title("Process"), muted("Click a row or type booking ID."));

        VBox form = card("");
        TextField bidF = field("Booking ID");
        tbl.setOnMouseClicked(e -> {
            Booking sel = tbl.getSelectionModel().getSelectedItem();
            if (sel != null) bidF.setText(String.valueOf(sel.getBookingId()));
        });
        TextArea receipt = new TextArea();
        receipt.setEditable(false); receipt.setVisible(false); receipt.setPrefRowCount(16);
        receipt.setStyle("-fx-control-inner-background: " + CARD2 + "; -fx-text-fill: " + WHITE + "; -fx-font-family: monospace; -fx-font-size: 11px;");
        Button coBtn = goldBtn("Confirm Check-out");
        coBtn.setOnAction(e -> {
            try {
                Booking b = service.checkout(Integer.parseInt(bidF.getText().trim()));
                if (b == null) { popup("No active booking found with that ID."); return; }
                receipt.setText(b.generateBill() + "\n\nPoints awarded! Balance: "
                    + b.getCustomer().getLoyaltyPoints() + " pts | Tier: " + b.getCustomer().getTier());
                receipt.setVisible(true);
                tbl.setItems(FXCollections.observableArrayList(service.getActiveBookings()));
                bidF.clear();
            } catch (NumberFormatException ex) { popup("Enter a valid booking ID."); }
        });
        form.getChildren().addAll(row("Booking ID:", bidF), new Separator(), coBtn, receipt);
        right.getChildren().add(form);
        root.getChildren().addAll(left, right);
        return root;
    }

    // =============================================
    // SMART FINDER (Unique Feature)
    // =============================================
    ScrollPane smartPanel() {
        VBox root = new VBox(14);
        Label t = new Label("Smart Room Finder");
        t.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + GOLD + ";");
        root.getChildren().addAll(t, muted("Find the best room within your budget. Sorted by best value using dynamic pricing."));

        VBox form = card("Your Preferences");
        TextField budgetF = field("Max budget per night (Rs.)");
        ComboBox<String> typeC = combo("Any", "Standard", "Deluxe", "Suite");
        TextField nightsF = field("Number of nights"); nightsF.setText("2");
        Button searchBtn = goldBtn("Search Best Rooms"); searchBtn.setMaxWidth(200);
        form.getChildren().addAll(row("Budget/Night:", budgetF), row("Type:", typeC), row("Nights:", nightsF), searchBtn);

        VBox results = card("Results");
        results.getChildren().add(muted("Enter preferences above and click Search."));

        searchBtn.setOnAction(e -> {
            try {
                double budget = Double.parseDouble(budgetF.getText().trim());
                int nights    = Integer.parseInt(nightsF.getText().trim());
                ArrayList<Room> recs = service.getRecommendations(budget, typeC.getValue(), nights);
                results.getChildren().clear();
                Label rTitle = new Label(recs.size() + " room(s) found — best value first");
                rTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + WHITE + ";");
                results.getChildren().addAll(rTitle, new Separator());
                if (recs.isEmpty()) { results.getChildren().add(muted("No rooms match. Try a higher budget or select 'Any' type.")); return; }
                int rank = 1;
                for (Room r : recs) {
                    VBox tile = new VBox(6);
                    tile.setPadding(new Insets(12));
                    tile.setStyle("-fx-background-color: " + CARD2 + "; -fx-background-radius: 6;");
                    HBox top = new HBox(10); top.setAlignment(Pos.CENTER_LEFT);
                    Label rankL = new Label("#" + rank);
                    rankL.setStyle("-fx-background-color: " + (rank == 1 ? GOLD : "#2a3a5a") + "; -fx-text-fill: "
                                 + (rank == 1 ? "#1a1a2e" : WHITE) + "; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 10;");
                    Label roomL = new Label("Room #" + r.getRoomNumber() + "  " + r.getRoomType());
                    roomL.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + WHITE + ";");
                    Region sp2 = new Region(); HBox.setHgrow(sp2, Priority.ALWAYS);
                    // Runtime Polymorphism - correct calculateTariff() called for each room type
                    Label price = new Label("Rs." + (int) r.calculateTariff(nights) + " total");
                    price.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + GOLD + ";");
                    top.getChildren().addAll(rankL, roomL, sp2, price);
                    double nightly = r.calculateTariff(1);
                    double savings = budget - nightly;
                    Label det = muted("Rs." + (int) nightly + "/night  |  Demand: " + r.getDemandScore() + "%  |  " + r.getAmenitiesDescription());
                    Label sav = new Label(savings >= 0 ? "Within budget (Rs." + (int) savings + " to spare)" : "Above budget by Rs." + (int)(-savings) + " after demand pricing");
                    sav.setStyle("-fx-font-size: 11px; -fx-text-fill: " + (savings >= 0 ? GREEN : "#FF9800") + ";");
                    tile.getChildren().addAll(top, det, sav);
                    if (rank == 1) {
                        Label best = new Label("BEST VALUE PICK");
                        best.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: " + GOLD + "; -fx-background-color: #2a3a1a; -fx-padding: 3 8; -fx-background-radius: 8;");
                        tile.getChildren().add(best);
                    }
                    results.getChildren().add(tile);
                    rank++;
                }
            } catch (NumberFormatException ex) { popup("Enter valid numbers for budget and nights."); }
        });

        root.getChildren().addAll(form, results);
        return scroll(root);
    }

    // =============================================
    // ANALYTICS
    // =============================================
    ScrollPane analyticsPanel() {
        VBox root = new VBox(14);
        root.getChildren().addAll(title("Analytics"), muted("Live hotel occupancy and revenue overview."));
        root.getChildren().add(statsGrid(
            statTile(String.format("%.0f%%", service.getOccupancyPct()), "Occupancy", GOLD),
            statTile(String.valueOf(service.getBookedRooms()),            "Occupied",  RED),
            statTile(String.valueOf(service.getAvailableCount()),         "Available", GREEN),
            statTile("Rs." + (int) service.getTotalRevenue(),            "Revenue",   BLUE)
        ));

        VBox typeCard = card("Room Type Breakdown");
        String[] types = {"Standard","Deluxe","Suite"};
        String[] cols  = {GREEN, BLUE, GOLD};
        for (int i = 0; i < types.length; i++) {
            final String tp = types[i];
            long total  = service.getAllRooms().stream().filter(r -> r.getRoomType().equals(tp)).count();
            long booked = service.getAllRooms().stream().filter(r -> r.getRoomType().equals(tp) && r.isBooked()).count();
            double pct  = total > 0 ? booked * 100.0 / total : 0;
            HBox r = new HBox(10); r.setAlignment(Pos.CENTER_LEFT); r.setPadding(new Insets(5, 0, 5, 0));
            Label tl = new Label(tp); tl.setMinWidth(70); tl.setStyle("-fx-text-fill: " + WHITE + ";");
            Pane bg   = new Pane(); bg.setPrefSize(280, 12); bg.setStyle("-fx-background-color: #2a3a5a; -fx-background-radius: 5;");
            Pane fill = new Pane(); fill.setPrefSize(280 * pct / 100, 12); fill.setStyle("-fx-background-color: " + cols[i] + "; -fx-background-radius: 5;");
            StackPane bar = new StackPane(bg, fill); bar.setAlignment(Pos.CENTER_LEFT);
            r.getChildren().addAll(tl, bar, muted(booked + "/" + total));
            typeCard.getChildren().add(r);
        }

        VBox demCard = card("Demand Overview");
        demCard.getChildren().add(muted("Each tile shows demand score and booking status."));
        FlowPane tiles = new FlowPane(8, 8);
        tiles.setPadding(new Insets(6, 0, 0, 0));
        for (Room r : service.getAllRooms()) {
            int d = r.getDemandScore();
            String bg  = d >= 70 ? "#3a1010" : d >= 40 ? "#3a2a10" : "#103a10";
            String col = d >= 70 ? RED : d >= 40 ? "#FF9800" : GREEN;
            VBox tile = new VBox(3); tile.setAlignment(Pos.CENTER); tile.setPadding(new Insets(10)); tile.setMinWidth(72);
            tile.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 8;");
            tile.getChildren().addAll(
                colored("#" + r.getRoomNumber(), WHITE),
                muted(r.getRoomType().substring(0, 3)),
                colored(d + "%", col),
                colored(r.isBooked() ? "BOOKED" : "FREE", r.isBooked() ? RED : GREEN)
            );
            tiles.getChildren().add(tile);
        }
        demCard.getChildren().add(tiles);
        root.getChildren().addAll(typeCard, demCard);
        return scroll(root);
    }

    public static void main(String[] args) { launch(args); }
}
