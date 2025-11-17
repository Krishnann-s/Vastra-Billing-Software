🛑 License: Proprietary — All rights reserved. Use or distribution without written permission is prohibited.

# Vastra Billing System v1.0

A comprehensive Point of Sale (POS) and inventory management system for retail stores, built with JavaFX and SQLite.

## Features

### ✅ Implemented

- **Product Management**
  - Add products with automatic barcode generation
  - SKU/Barcode-based product lookup
  - Automatic stock management
  - Low stock alerts (when stock ≤ 5)
  - Category and brand organization
- **Barcode Integration**
  - Hardware barcode scanner support (keyboard wedge)
  - CODE-128 barcode generation
  - Printable barcode labels
- **Sales & Billing**
  - Quick checkout with barcode scanning
  - Multiple payment modes (Cash, Card, UPI)
  - GST tax calculation
  - Discount management
  - Automatic stock reduction on sale
- **Customer Management**
  - Customer registration with phone number
  - Loyalty points system (1 point per ₹100 spent)
  - Points redemption (100 points = ₹100 discount)
  - Customer tiers (Bronze, Silver, Gold)
- **Printing**
  - Thermal receipt printing
  - Barcode label printing (batch print for all products)
- **Reports & Analytics**
  - Daily sales reports
  - Inventory reports
  - Monthly revenue summaries
  - Excel export support

## System Requirements

- **Java**: JDK 17 or higher
- **Maven**: 3.8+
- **OS**: Windows 10/11, Linux, macOS
- **Hardware** (Optional):
  - USB Barcode Scanner (Keyboard wedge mode)
  - Thermal Printer (ESC/POS compatible)

## Installation

### 1. Clone or Extract the Project

```bash
cd Vastra
```

### 2. Build the Project

```bash
mvn clean install
```

### 3. Run the Application

```bash
mvn javafx:run
```

Or directly run the JAR:

```bash
java --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml -jar target/Vastra-1.0.jar
```

## Hardware Setup

### Barcode Scanner Configuration

1. **Connect USB barcode scanner** to your computer
2. **Configure scanner to Keyboard Wedge mode** (most scanners work in this mode by default)
3. **Test the scanner**: Open Notepad and scan a barcode - it should type the barcode number
4. **Scanner settings**:
   - Add Enter/Return suffix after scan (recommended)
   - No prefix required
   - Enable auto-scan mode if available

### Thermal Printer Setup

1. **Install printer drivers** from manufacturer
2. **Set as default printer** or configure in application settings
3. **Supported paper widths**: 58mm or 80mm thermal paper
4. **Test print** from application

## Usage Guide

### Starting a Sale

1. **Focus on Barcode Input Field**

   - The yellow field at the top should always be focused
   - Scanner will automatically input barcode and add product to cart

2. **Manual Product Search**

   - Type product name or SKU in the barcode field
   - Press Enter to search

3. **Add Customer** (Optional)

   - Click "Add Customer" button
   - Enter phone number
   - Create new customer or select existing one

4. **Apply Discounts**

   - Manual discount: Enter amount in discount field
   - Points redemption: Click "Redeem Points" button

5. **Complete Sale**
   - Click "Complete Sale" button
   - Select payment method
   - Bill will be printed automatically

### Adding Products

1. **Click "Add Product"** button
2. **Fill in product details**:
   - **Name** (Required): Product name
   - **Variant**: Size, color, etc.
   - **Category**: Product category
   - **Brand**: Brand name
   - **SKU**: Auto-generated or custom
   - **Selling Price** (Required): Price to customer
   - **GST%**: Default 18%
   - **Stock** (Required): Opening stock quantity
3. **Click "Save Product"**
   - Barcode is generated automatically
   - Barcode label image saved in `labels/` folder

### Printing Barcode Labels

1. **Click "Print Barcodes"** button in main screen
2. **All products will be printed** on barcode labels
3. **Configure label size** in BarcodeUtil.java if needed
4. **Stick labels on products**

### Customer Loyalty Program

**Points Earning**:

- Customers earn 1 point for every ₹100 spent
- Example: ₹500 purchase = 5 points

**Points Redemption**:

- Minimum 100 points required for redemption
- 1 point = ₹1 discount
- Points are deducted immediately on sale completion

**Customer Tiers**:

- Bronze: Default tier
- Silver: Coming soon
- Gold: Coming soon

### Stock Management

**Low Stock Alerts**:

- Products with stock ≤ reorder threshold (default 5) are flagged
- Check alerts by clicking "Low Stock" button

**Stock Reduction**:

- Stock automatically reduces when sale is completed
- Cannot sell more than available stock
- System prevents overselling

## Database

The application uses SQLite database stored at: `db/vastra.db`

### Tables

- **products**: Product catalog
- **customers**: Customer database
- **sales**: Sales transactions
- **sale_items**: Individual items in each sale
- **store_settings**: Application configuration
- **returns**: Product returns (coming soon)

### Backup

Regularly backup the `db/` folder to prevent data loss.

```bash
# Backup command
cp -r db/ backup/db_$(date +%Y%m%d)/
```

## Keyboard Shortcuts

- **F1**: Add Product
- **F2**: Add Customer
- **F3**: Complete Sale
- **F4**: Clear Cart
- **ESC**: Clear current field
- **Enter**: Process scanned barcode

## Troubleshooting

### Barcode Scanner Not Working

1. **Check scanner connection**: Ensure USB is connected
2. **Test in Notepad**: Scanner should type barcode numbers
3. **Focus issue**: Click on the yellow barcode input field
4. **Scanner configuration**: Ensure keyboard wedge mode is enabled

### Printer Not Printing

1. **Check printer connection**: Ensure printer is on and connected
2. **Printer drivers**: Install/update printer drivers
3. **Default printer**: Set thermal printer as default
4. **Paper**: Check thermal paper is loaded

### Product Not Found

1. **Check barcode**: Ensure barcode matches product SKU
2. **Product active**: Check if product is deactivated
3. **Database**: Verify product exists in database

### Low Stock Not Showing

1. **Check reorder threshold**: Default is 5 units
2. **Update inventory**: Ensure stock levels are accurate

## Configuration

### Store Settings

Edit store information in the database:

```sql
UPDATE store_settings SET value = 'Your Store Name' WHERE key = 'store_name';
UPDATE store_settings SET value = 'Your Address' WHERE key = 'store_address';
UPDATE store_settings SET value = '+91-XXXXXXXXXX' WHERE key = 'store_phone';
UPDATE store_settings SET value = 'GSTIN Number' WHERE key = 'store_gstin';
```

### Points Configuration

Modify loyalty points settings:

```sql
UPDATE store_settings SET value = '2' WHERE key = 'points_per_100_rupees';
UPDATE store_settings SET value = '50' WHERE key = 'min_points_redemption';
```

## File Structure

```
Vastra/
├── src/
│   ├── main/
│   │   ├── java/com/vastra/
│   │   │   ├── MainApp.java              # Application entry point
│   │   │   ├── dao/                      # Database access
│   │   │   │   ├── ProductDAO.java
│   │   │   │   ├── CustomerDAO.java
│   │   │   │   └── SalesDAO.java
│   │   │   ├── model/                    # Data models
│   │   │   ├── ui/controllers/           # UI controllers
│   │   │   │   ├── MainController.java
│   │   │   │   └── ProductFormController.java
│   │   │   └── util/                     # Utilities
│   │   │       ├── BarcodeUtil.java      # Barcode generation
│   │   │       ├── BarcodeScanner.java   # Scanner handler
│   │   │       ├── ThermalPrinterUtil.java
│   │   │       ├── DBUtil.java           # Database setup
│   │   │       └── ExcelReportUtil.java  # Report generation
│   │   └── resources/
│   │       ├── com/vastra/ui/
│   │       │   ├── fxml/                 # FXML layouts
│   │       │   └── css/                  # Stylesheets
├── db/                                   # Database files
├── labels/                               # Generated barcodes
├── pom.xml                               # Maven configuration
└── README.md                             # This file
```

## Future Enhancements

- [ ] Customer tier benefits
- [ ] Product returns/exchange management
- [ ] Multiple user accounts with roles
- [ ] Advanced reporting with charts
- [ ] Email bill functionality
- [ ] SMS notifications
- [ ] Cloud backup
- [ ] Multi-store support
- [ ] Barcode scanning from mobile app
- [ ] Integration with online payment gateways

## Support

For issues or questions:

1. Check this README
2. Review error logs in console
3. Check database connection
4. Verify hardware setup

## License

Proprietary software for internal use.

## Credits

- **JavaFX**: UI Framework
- **SQLite**: Database
- **ZXing**: Barcode generation
- **Apache POI**: Excel export
- **Maven**: Build tool

---

**Version**: 1.0  
**Last Updated**: November 2025  
**Developed for**: Windows retail environments
