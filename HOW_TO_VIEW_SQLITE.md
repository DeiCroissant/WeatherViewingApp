# 🔍 Hướng Dẫn Xem SQLite Database

## 📋 TÓM TẮT - 4 CÁCH

| Phương pháp | Độ khó | Ưu điểm | Nhược điểm |
|------------|---------|---------|------------|
| **1. Database Inspector** | ⭐ Dễ | Real-time, trong Android Studio | Cần API 26+ |
| **2. Debug Activity** | ⭐ Dễ nhất | Xem ngay trong app | Phải thêm code |
| **3. Device Explorer + DB Browser** | ⭐⭐ Trung bình | Chi tiết, chỉnh sửa được | Cần export file |
| **4. ADB Command** | ⭐⭐⭐ Khó | Mạnh mẽ, scripting | Dùng terminal |

---

## 🎯 **CÁCH 1: Database Inspector (KHUYÊN DÙNG)**

### Bước 1: Chạy App
```
Run app trên emulator/device (Shift + F10)
```

### Bước 2: Mở Database Inspector
1. Menu: **View** → **Tool Windows** → **App Inspection**
2. Hoặc nhấn tab **App Inspection** ở dưới cùng màn hình

### Bước 3: Xem Database
1. Tab **Database Inspector**
2. Chọn app: `com.example.weatherviewingapp`
3. Mở `WeatherApp.db`
4. Click bảng `favorite_cities`
5. Xem dữ liệu real-time! 🎉

### Chạy SQL Query
```sql
SELECT * FROM favorite_cities;
SELECT COUNT(*) FROM favorite_cities;
```

**📸 Screenshot:**
```
App Inspection Tab
└── Database Inspector
    └── com.example.weatherviewingapp
        └── WeatherApp.db
            └── favorite_cities
                ├── id | city_name
                ├── 1  | Hanoi
                ├── 2  | Tokyo
                └── 3  | Paris
```

---

## 🎯 **CÁCH 2: Debug Activity (MỚI - ĐÃ THÊM VÀO APP)**

### Sử Dụng:
1. Chạy app
2. Nhấn **Menu (⋮)** ở góc trên
3. Chọn **"🔍 Debug Database"**
4. Xem thông tin database ngay trong app!

### Hiển Thị:
```
📊 DATABASE INFO
================

Database: WeatherApp.db
Table: favorite_cities
Total Cities: 3

Columns:
  - id (INTEGER PRIMARY KEY)
  - city_name (TEXT UNIQUE)

Cities List:
================
1. Hanoi
2. Tokyo
3. Paris
```

**⚠️ LƯU Ý:** Xóa `DebugDatabaseActivity` trước khi release app!

---

## 🎯 **CÁCH 3: Device File Explorer + DB Browser**

### Bước 1: Export Database

1. **Mở Device File Explorer:**
   - Menu: `View` → `Tool Windows` → `Device File Explorer`
   - Hoặc icon 📱 bên phải

2. **Navigate đến database:**
   ```
   /data/data/com.example.weatherviewingapp/databases/WeatherApp.db
   ```

3. **Click phải** vào `WeatherApp.db` → **Save As...**

4. **Lưu vào Desktop**

### Bước 2: Tải DB Browser

**Download:** https://sqlitebrowser.org/

**Platforms:**
- Windows: `.msi` installer
- macOS: `.dmg` file
- Linux: apt/snap package

### Bước 3: Mở Database

1. Chạy **DB Browser for SQLite**
2. `File` → `Open Database`
3. Chọn file `WeatherApp.db` vừa export
4. Tab **Browse Data** → Chọn table `favorite_cities`

### Tính Năng DB Browser:
- ✅ Xem tất cả bảng
- ✅ Chỉnh sửa dữ liệu
- ✅ Chạy SQL queries
- ✅ Export sang CSV/JSON
- ✅ Xem structure & indexes

---

## 🎯 **CÁCH 4: ADB Command Line**

### Bước 1: Mở Terminal/PowerShell

```powershell
# Check device connected
adb devices
```

### Bước 2: Vào SQLite Shell

```powershell
# Enter device shell
adb shell

# Navigate to database
cd /data/data/com.example.weatherviewingapp/databases

# Open SQLite
sqlite3 WeatherApp.db
```

### Bước 3: SQL Commands

```sql
-- Xem tất cả bảng
.tables

-- Xem structure của bảng
.schema favorite_cities

-- Query dữ liệu
SELECT * FROM favorite_cities;

-- Query có điều kiện
SELECT * FROM favorite_cities WHERE city_name LIKE 'H%';

-- Count
SELECT COUNT(*) FROM favorite_cities;

-- Insert (test)
INSERT INTO favorite_cities (city_name) VALUES ('TestCity');

-- Delete
DELETE FROM favorite_cities WHERE city_name = 'TestCity';

-- Thoát
.exit
```

### Bước 4: Exit Shell

```powershell
exit  # Exit adb shell
```

---

## 🛠️ **Debugging Tips**

### Kiểm tra Database có tồn tại không:

```powershell
adb shell ls -la /data/data/com.example.weatherviewingapp/databases/
```

### Export database về máy:

```powershell
adb pull /data/data/com.example.weatherviewingapp/databases/WeatherApp.db C:\Users\YourName\Desktop\
```

### Push database lên device:

```powershell
adb push C:\Users\YourName\Desktop\WeatherApp.db /data/data/com.example.weatherviewingapp/databases/
```

### Clear app data (xóa database):

```powershell
adb shell pm clear com.example.weatherviewingapp
```

---

## 📊 **SQLite Queries Hữu Ích**

### 1. Xem tất cả cities:
```sql
SELECT * FROM favorite_cities ORDER BY city_name;
```

### 2. Count cities:
```sql
SELECT COUNT(*) as total_cities FROM favorite_cities;
```

### 3. Search city:
```sql
SELECT * FROM favorite_cities WHERE city_name LIKE '%Ha%';
```

### 4. Add test data:
```sql
INSERT INTO favorite_cities (city_name) VALUES 
('London'),
('Berlin'),
('Madrid');
```

### 5. Delete all cities:
```sql
DELETE FROM favorite_cities;
```

### 6. Get table info:
```sql
PRAGMA table_info(favorite_cities);
```

---

## ❗ Troubleshooting

### "Permission Denied" khi access database:

**Giải pháp:**
- Chỉ hoạt động trên **debug build**
- Không hoạt động trên **release build** (vì security)
- Dùng emulator hoặc rooted device

### Database không tồn tại:

**Nguyên nhân:**
- App chưa chạy lần nào
- Database chưa được tạo

**Giải pháp:**
```java
// Trong app, thêm 1 city để trigger database creation
DatabaseHelper db = new DatabaseHelper(this);
db.addCity("Test");
```

### Database Inspector không hiển thị:

**Yêu cầu:**
- Android Studio 4.1+
- minSdk 26+ (Android 8.0+)
- Debug build
- App đang chạy

---

## 📱 **App Structure**

```
SQLite Database: WeatherApp.db
└── Table: favorite_cities
    ├── Column: id (INTEGER PRIMARY KEY AUTOINCREMENT)
    └── Column: city_name (TEXT NOT NULL UNIQUE)
```

**Database Location:**
```
/data/data/com.example.weatherviewingapp/databases/WeatherApp.db
```

**Managed by:**
- `DatabaseHelper.java` (SQLiteOpenHelper)
- `SettingsActivity.java` (UI)

---

## 🎓 **Best Practices**

1. **Luôn đóng database sau khi dùng:**
   ```java
   db.close();
   ```

2. **Dùng transactions cho nhiều operations:**
   ```java
   db.beginTransaction();
   try {
       // Multiple operations
       db.setTransactionSuccessful();
   } finally {
       db.endTransaction();
   }
   ```

3. **Xóa Debug Activity trước release:**
   - Xóa `DebugDatabaseActivity.java`
   - Xóa đăng ký trong `AndroidManifest.xml`
   - Xóa menu item trong `menu_main.xml`

---

## 🚀 **Quick Start**

**Phương pháp nhanh nhất:**

1. ✅ **Chạy app** (Shift + F10)
2. ✅ **Thêm vài cities** trong Settings
3. ✅ **Mở Menu** → **"🔍 Debug Database"**
4. ✅ **Xem dữ liệu** ngay trong app!

**Hoặc:**

1. ✅ **View** → **Tool Windows** → **App Inspection**
2. ✅ **Database Inspector** tab
3. ✅ Chọn app → `WeatherApp.db` → `favorite_cities`

---

## 📚 **Tài Liệu Tham Khảo**

- **Android Database Inspector:** https://developer.android.com/studio/inspect/database
- **SQLite Tutorial:** https://www.sqlitetutorial.net/
- **DB Browser:** https://sqlitebrowser.org/
- **ADB Commands:** https://developer.android.com/studio/command-line/adb

---

**Chúc bạn debug thành công! 🔍💾**
